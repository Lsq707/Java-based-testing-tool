package org.jload.runner;

import org.jload.model.ShapeTuple;
import org.jload.user.User;

import org.jload.user.WaitTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/*
The strategy to run the testing
*/
public class Runner {
    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    private static int loop;
    private static int timeOut;
    private static int testingTime;
    private static int spawnRate;
    private static int userNum;
    private static List<Class<?>> definedUsers;
    private static ConcurrentHashMap<String, List<User>> activeUsers;
    static ScheduledExecutorService scheduledExecutorService;
    static ScheduledFuture<?> runnableFuture;
    private static volatile Boolean testFlag = true;
    private long startTime;

    //Usable in pkg
     Runner(int loopTime, int userNum, int spawnRate, int testingTime,String CsvFileName, String htmlFile) throws IOException {
        definedUsers = Env.getUsers();
        loop = loopTime;
        Runner.testingTime = testingTime*1000;
        Runner.spawnRate = spawnRate;
        Runner.userNum = userNum;
        timeOut = Integer.MAX_VALUE;
        activeUsers = new ConcurrentHashMap<>();
    }

    /*
    Initial the users declared in jLoadFile
    */

    private User initUser(Class<?> User){
        User userInstance = null;
        try {
            Constructor<?> constructor = User.getDeclaredConstructor();
            constructor.setAccessible(true);
            userInstance = (User) constructor.newInstance();

            if(userInstance.getUserParamHost().isBlank() && Env.host != null){
                setHost(userInstance);
            }

            logger.info("Instance of {} created: {}", User.getName(), userInstance);
        } catch (Exception e) {
            logger.error("Error creating an instance of class {}: {}", User.getName(), e.getMessage(), e);
        }
        return userInstance;
    }

    private void setHost(User userInstance) {
        userInstance.setUserParamHost(Env.host);
    }

    /*
    Two ways running the testing
    * loop times
    * Shape (add or dispose users at different time)
    */
    public void run() throws InterruptedException {
        Runnable runExecution = loop == 0 ? this::executeInShapeControl : this::executeInLoop;
        runExecution.run();
    }

    /*
    Get the customized shape or default shape,
    check the condition each seconds to add or dispose users
    */
    private void executeInShapeControl() {
        LoadTestShape loadTestShape = Env.initShape();
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        startTime = System.currentTimeMillis();
        runnableFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {

            //printOutActiveUsr();

            List<ShapeTuple> shapeTuples = loadTestShape.tick();
            if(shapeTuples == null)
                testFlag = false;   //End the testing
            else adjustUser(shapeTuples);
        }, 0, 1, TimeUnit.SECONDS);
        // Run for the scheduled time or schedule strategy
        while (true){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            long duration = System.currentTimeMillis() - startTime;
            if(!testFlag || duration >= testingTime)
                break;
        }
        runnableFuture.cancel(true);
        shutdownThreads(scheduledExecutorService);
        // Stop the test
        endTesting();
    }

    /*
    FOR TEST
    */
    private void printOutActiveUsr(){
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Test Duration: " + String.valueOf(duration));
        for (Map.Entry<String, List<User>> entry :activeUsers.entrySet()) {
            System.out.println(entry.getKey() + " currentRunning: " + String.valueOf(entry.getValue().size()));
        }
    }

    /*
    Adjust running user numbers
    */
    private synchronized void adjustUser(List<ShapeTuple> tick) {
        for (ShapeTuple shapeTuple : tick) {
            long currNum = countActiveUser(shapeTuple.getUserCls());
            long desiredNum = shapeTuple.getClsTotalNum();
            long spawnRate = shapeTuple.getSpawnRate();
            long difference = desiredNum - currNum;
            if (difference > 0) {
                for (int i = 0; i < difference && i <= spawnRate; i++)
                    addUser(shapeTuple.getUserCls());
            } else if (difference < 0) {
                for (int i = 0; i < -difference && i <= spawnRate; i++)
                    disposeUser(shapeTuple.getUserCls());
            }
        }
    }

    /*
    Shut the user of certain class which means remove the earliest created one
    */
    private static void disposeUser(String clsName){
        List<User> users = activeUsers.get(clsName);
        if(!users.isEmpty()) {
            User usr = users.get(0);
            usr.setTaskFlag(false);
            shutdownThreads(usr.getClient().getClientExecutor());
            usr.getClient().closeClient();
            users.remove(usr);
            if (users.isEmpty())
                activeUsers.remove(clsName);
        }
    }

    private static void disposeUser(User user){
        user.setTaskFlag(false);
        String uName = Env.getClsName(user);
        List<User> users = activeUsers.get(uName);
        shutdownThreads(user.getClient().getClientExecutor());
        user.getClient().closeClient();
        users.remove(user);
        if (users.isEmpty())
            activeUsers.remove(uName);
    }

    /*
    Add the user of certain class
    */
    private void addUser(String clsName){
        User user = null;
        for (Class<?> cls : definedUsers) {
            if(getClsName(cls).equals(clsName)) {
                user = initUser(cls);
                //Thread.ofVirtual().start(user);  //start the user
                Thread.ofPlatform().start(user);
            }
        }
        if(user != null) {
            List<User> usersList;
            if (activeUsers.containsKey(clsName)) {
                usersList = activeUsers.get(clsName);
            } else {
                usersList = new ArrayList<User>();
                activeUsers.put(clsName, usersList);
            }
            assert usersList != null;
            usersList.add(user);
        }
        if(user == null)
            logger.error("No such user class: {}", clsName);
    }

    /*
    Get the class name without pkg name
     */
    private String getClsName(Class<?> cls){
        String name = null;
        int lastDot = cls.getName().lastIndexOf(".");
        name = cls.getName().substring(lastDot+1);
        return name;
    }

    /*
    Count the active user number of certain type
     */
    private long countActiveUser(String clsName){
        if(activeUsers.get(clsName) != null)
            return activeUsers.get(clsName).size();
        return 0;
    }

    /*
    Execute the testing with loop times
    Each user and their tasks will execute once only then end the testing
    */
    private void executeInLoop(){
        //For user to store the variables that able to be used by other method
        int loopItr;
        for(loopItr = 0 ; loopItr < loop; loopItr++){
            for(Class<?> user: definedUsers){
                addUser(getClsName(user));
            }
        }

        //Execute until all the tasks are finished
        while (!activeUsers.isEmpty()){}
        shutdownAllUsers();

    }

    /*
    Run the tasks in certain user
    There are two ways running tasks
    * In loop times: the tasks in certain user will only execute once
    * In shape control: The task in each user will execute constantly until the taskFlag which defined in endTesting()
    */
    public static <T extends User> void runUsers(T user) throws InterruptedException, InvocationTargetException, IllegalAccessException {
        ExecutorService taskExecutor = user.getClient().getClientExecutor();
        WaitTime waitTime = user.getWaitTimeStrategy();
        //int test = 0;
        while (user.getTaskFlag()) {
            Method task = user.getTaskSet().getNextMethod();
            if (!task.isAccessible()) {
                task.setAccessible(true);
            }
            //taskExecutor.submit(() -> task.invoke(user));
            try {
                task.invoke(user);
            }catch (Exception e) {
                logger.error("Excpetion!!!", e);
            }
            //test += 1;
            /*if(waitTime.getWaitTime() == 0)
                Thread.sleep(waitTime.getWaitTime()+1);*/
            Thread.sleep(waitTime.getWaitTime());

        //If defined loop times the tasks will only do once and then dispose the user
            if(loop != 0) {
                logger.info("Dispose user");
                disposeUser(user);
            }
        }
        logger.info("Task flag finish");
    }
    /*
    Shut down the threads in thread pool
     */
    private static <T extends ExecutorService> void shutdownThreads(T threadPool){
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(timeOut,TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /*
<<<<<<< HEAD
    Stop all the running mission
     */
    static void shutdownHook(){
        if(runnableFuture != null) {
            runnableFuture.cancel(true);
            shutdownThreads(scheduledExecutorService);
        }
        shutdownAllUsers();
    }

    /*
    Shut down all the ExecutorService in each active user
     */
    static void shutdownAllUsers(){
        for (Map.Entry<String, List<User>> entry :activeUsers.entrySet()) {
            String key = entry.getKey();
            while(!entry.getValue().isEmpty()){
                disposeUser(key);
            }
            activeUsers.remove(key);
        }
    }

    /*
    The process of ending test for shape control testing
     */
    private void endTesting(){

        // Shutdown all user
        shutdownAllUsers();
        logger.info("The testing END");
    }

    /*
    Pass the related parameter to Env for the default testing strategy
    */
    static int getTestingTime(){
        return testingTime;
    }

    static int getSpawnRate(){
        return spawnRate;
    }

    static int getUserNum(){return userNum;}

}
