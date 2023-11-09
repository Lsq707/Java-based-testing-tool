package org.jload.runner;

import org.jload.tasks.Task;
import org.jload.user.User;
import org.jload.output.CsvOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/*
Assign the Virtualthreads and run the application
 */
public class Runner {
    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    private static int loop;
    private static int userNum;
    private static int timeOut;
    private static int testingTime;
    private static int usrPerSecond;
    private static List<User> users = new ArrayList<>();
    private int userIter;

    //Constructor START
    public Runner(){
        initUsers();
        loop = 1;
        usrPerSecond = 0;
        userNum = users.size();
        testingTime = Integer.MAX_VALUE;
        timeOut = Integer.MAX_VALUE;
    }

    public Runner(String fileName) throws IOException {
        initUsers();
        loop = 1;
        CsvOutput.createFile(fileName);
        usrPerSecond = 0;
        userNum = users.size();
        testingTime = Integer.MAX_VALUE;
        timeOut = Integer.MAX_VALUE;
    }

    public Runner(int loopTime){
        initUsers();
        loop = 1;
        usrPerSecond = 0;
        userNum = users.size();
        testingTime = Integer.MAX_VALUE;
        timeOut = Integer.MAX_VALUE;
    }

    public Runner(int loopTime,String fileName) throws IOException {
        initUsers();
        loop = loopTime;
        CsvOutput.createFile(fileName);
        usrPerSecond = 0;
        userNum = users.size();
        testingTime = Integer.MAX_VALUE;
        timeOut = Integer.MAX_VALUE;
    }

    public Runner(String fileName, int userPerSecond, int testingTime) throws IOException {
        initUsers();
        loop = 0;
        CsvOutput.createFile(fileName);
        Runner.testingTime = testingTime;
        userNum = users.size();
        usrPerSecond = userPerSecond;
        timeOut = Integer.MAX_VALUE;
    }
    //Constructors END

    public static int getUserNum(){
        return userNum;
    }

    //Initial the created users
    private void initUsers(){
        List<Class<?>> Users = ClassScanner.getClasses();
            for (Class<?> cls : Users) {
                try {
                    if (User.class.isAssignableFrom(cls) && !cls.isInterface() && !Modifier.isAbstract(cls.getModifiers())) {
                        Constructor<?> constructor = cls.getDeclaredConstructor();
                        constructor.setAccessible(true);
                        User userInstance = (User) constructor.newInstance();
                        users.add(userInstance);
                        logger.info("Instance of {} created: {}", cls.getName(), userInstance);
                    }
                } catch (Exception e) {
                    logger.error("Error creating an instance of class {}: {}", cls.getName(), e.getMessage(), e);
                }
            }
    }

    //Set users to run
    public void run() throws InterruptedException {
        Runnable runExecution = loop == 0 ? this::executeInTimeControll : this::executeInLoop;
        runExecution.run();
    }

    private void executeInTimeControll(){
        ExecutorService poolExecutor = Executors.newVirtualThreadPerTaskExecutor();
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        //Control the user numbers per seconds
        AtomicInteger threadCount = new AtomicInteger(0);
        ScheduledFuture<?> runnableFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {
            while (threadCount.get() < usrPerSecond) {
                poolExecutor.submit((Runnable) users.get(userIter % users.size()));
                userIter++;
                threadCount.incrementAndGet();
            }
            threadCount.set(0);
        }, 0, 1, TimeUnit.SECONDS);

        // Run for the desired time
        ExecuteTime();

        // Stop the scheduled task
        runnableFuture.cancel(true);

        // Shutdown and await termination of the executor services
        ShutdownThreads(poolExecutor);
        ShutdownUserExecutor();
        ShutdownThreads(scheduledExecutorService);

        // Close resources
        CloseFile();
    }

    private void executeInLoop(){
        ExecutorService poolExecutor = Executors.newVirtualThreadPerTaskExecutor();
        for(userIter = 0 ; userIter < loop; userIter++){
            for(User user : users) {
                poolExecutor.submit(user);
            }
        }
        ShutdownThreads(poolExecutor);
        ShutdownUserExecutor();
        CloseFile();
    }

    //Set tasks to run
    public static <T extends User> void runUsers(T user) throws InterruptedException, InvocationTargetException, IllegalAccessException {
        ExecutorService taskExecutor = user.getClient().getClientExecutor();
        long waitTime = user.getwaitTimeStrategy().getWaitTime();
        Method[] userTasks = user.getClass().getDeclaredMethods();
        for(Method task : userTasks) {
            if (!task.isAccessible()) {
                task.setAccessible(true);
            }
            if(task.isAnnotationPresent(Task.class)){
                taskExecutor.submit(() -> task.invoke(user));
                Thread.sleep(waitTime);
            }
        }

    }

    //How long the testing will be
    private void ExecuteTime(){
        try {
            TimeUnit.SECONDS.sleep(testingTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void WaitTime(long waitTime){
        try {
            //System.out.println("Sleep");
            TimeUnit.SECONDS.sleep(waitTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //Shut down the threads
    private <T extends ExecutorService> void ShutdownThreads(T threadPool){
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

    private void ShutdownUserExecutor(){
        for (User user : users) {
            ShutdownThreads(user.getClient().getClientExecutor());
        }
    }

    //CloseFile
    private void CloseFile(){
        CsvOutput.closeFile();
    }


}
