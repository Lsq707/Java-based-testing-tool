package org.jload.runner;

import org.jload.model.ShapeTuple;
import org.jload.output.CsvOutputFilter;
import org.jload.response.Statistics;
import org.jload.tasks.Task;
import org.jload.user.User;
import org.jload.output.CsvOutput;
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
    private ConcurrentHashMap<String, List<User>> activeUsers;
    private ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture<?> runnableFuture;
    private static volatile Boolean testFlag = true;
    private long startTime;


    //Constructor START
    public Runner(int loopTime, String fileName, int userNum, int spawnRate, int testingTime) throws IOException {
        definedUsers = Env.getUserClass();
        loop = loopTime;
        CsvOutput.createFile(fileName);
        Runner.testingTime = testingTime*1000;
        Runner.spawnRate = spawnRate;
        Runner.userNum = userNum;
        timeOut = Integer.MAX_VALUE;
        activeUsers = new ConcurrentHashMap<>();
    }


    public Runner(String fileName) throws IOException {
        this(fileName,0,0,Integer.MAX_VALUE/1000);
    }


    public Runner(int loopTime,String fileName) throws IOException {
        this(loopTime,fileName,0,0,Integer.MAX_VALUE);
        validateLoopTime(loopTime);
    }

    public Runner(String fileName, int userNum, int spawnRate, int testingTime) throws IOException {
        this(0,fileName,userNum,spawnRate,testingTime);
    }
    //Constructors END

    /*
    Initial the users declared in jLoadFile
     */
    private User initUser(Class<?> User){
        User userInstance = null;
        try {
            Constructor<?> constructor = User.getDeclaredConstructor();
            constructor.setAccessible(true);
            userInstance = (User) constructor.newInstance();
            logger.info("Instance of {} created: {}", User.getName(), userInstance);
        } catch (Exception e) {
            logger.error("Error creating an instance of class {}: {}", User.getName(), e.getMessage(), e);
        }
        return userInstance;
    }

    /*
    Two ways running the testing
    * loop times
    * Shape (add or dispose users at different time)
     */
    public void run() throws InterruptedException {
        Runnable runExecution = loop == 0 ? this::executeInShapeControl : this::executeInLoop;
        Statistics.registerFilter(new CsvOutputFilter());
        runExecution.run();
        // Close resources
        closeFile();
    }

    /*
    Get the customized shape or default shape,
    check the condition each seconds to add or dispose users
     */
    private void executeInShapeControl(){
        startTime = System.currentTimeMillis();
        LoadTestShape loadTestShape = Env.initShape();
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        runnableFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {
            printOutActiveUsr();
            List<ShapeTuple> shapeTuples = loadTestShape.tick();
            if(shapeTuples == null) {
                testFlag = false; //End the testing
            }
            else adjustUser(shapeTuples);
        }, 0, 1, TimeUnit.SECONDS);
        // Run for the scheduled time or schedule strategy
        while (true){
            long duration = System.currentTimeMillis() - startTime;
            if(!testFlag || duration >= testingTime)
                break;
        }
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
                for (int i = 0; i < difference && i < spawnRate; i++)
                    addUser(shapeTuple.getUserCls());
            } else if (difference < 0) {
                for (int i = 0; i < -difference && i < spawnRate; i++)
                    disposeUser(shapeTuple.getUserCls());
            }
        }
    }

    /*
    Shut the user of certain class
     */
    private void disposeUser(String clsName){
        List<User> users = activeUsers.get(clsName);
        if(!users.isEmpty()) {
            User usr = users.get(0);
            usr.setTaskFlag(false);
            shutdownThreads(usr.getClient().getClientExecutor());
            users.remove(0);
        }
    }

    /*
    Add the user of certain class
     */
    private void addUser(String clsName){
        User user = null;
        for (Class<?> cls : definedUsers) {
            if(getClsName(cls).equals(clsName)) {
                user = initUser(cls);
                Thread.ofVirtual().start(user);
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
        ExecutorService poolExecutor = Executors.newVirtualThreadPerTaskExecutor();
        //For user to store the variables that able to be used by other method
        int userItr;
        for(userItr = 0 ; userItr < loop; userItr++){
            for(Class<?> user: definedUsers){
                poolExecutor.submit(initUser(user));
            }
        }
        shutdownThreads(poolExecutor);
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
        Method[] userTasks = user.getClass().getDeclaredMethods();
        while (user.getTaskFlag()) {
            for (Method task : userTasks) {
                if (!task.isAccessible()) {
                    task.setAccessible(true);
                }
                if (task.isAnnotationPresent(Task.class)) {
                    taskExecutor.submit(() -> task.invoke(user));
                    Thread.sleep(waitTime.getWaitTime());
                }
            }
            //If defined loop times the tasks will only do once
            if(loop != 0)
                user.setTaskFlag(false);
        }
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
    Shut down all the ExecutorService in each active user
     */
    private void shutdownAllUsers(){
        for (Map.Entry<String, List<User>> entry :activeUsers.entrySet()) {
            for(User user :entry.getValue())
                shutdownThreads(user.getClient().getClientExecutor());
        }
    }

    /*
    Close the CSV file
     */
    private void closeFile(){
        CsvOutput.closeFile();
    }

    /*
    The process of ending test for shape control testing
     */
    private void endTesting(){
        runnableFuture.cancel(true);

        // Shutdown all user
        shutdownAllUsers();
        shutdownThreads(scheduledExecutorService);
        logger.info("The testing END");
    }

    /*
    Validate the user input
    */
    private void validateLoopTime(int loopTime) {
        if (loopTime <= 0) {
            throw new IllegalArgumentException("Loop time must be greater than 0");
        }
        logger.error("The loop times must greater than 0");
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
