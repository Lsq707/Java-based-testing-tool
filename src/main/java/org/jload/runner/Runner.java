package org.jload.runner;

import org.jload.jLoadFile;
import org.jload.tasks.Task;
import org.jload.user.User;
import org.jload.output.OutputTheCsv;
import org.jload.jLoadFile;

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
    private static int LOOP;
    private static int USER_NUM;
    private static int TIME_OUT;
    private static int TESTING_TIME;
    private static int USR_PER_SECOND;
    private static List<User> users = new ArrayList<>();
    private int userIter;

    //Constructor START
    public Runner(){
        initialTheUsers();
        LOOP = 1;
        USR_PER_SECOND = 0;
        USER_NUM = users.size();
        TESTING_TIME = Integer.MAX_VALUE;
        TIME_OUT = Integer.MAX_VALUE;
    }

    public Runner(String fileName) throws IOException {
        initialTheUsers();
        LOOP = 1;
        OutputTheCsv.createFile(fileName);
        USR_PER_SECOND = 0;
        USER_NUM = users.size();
        TESTING_TIME = Integer.MAX_VALUE;
        TIME_OUT = Integer.MAX_VALUE;
    }

    public Runner(int loopTime){
        initialTheUsers();
        LOOP = 1;
        USR_PER_SECOND = 0;
        USER_NUM = users.size();
        TESTING_TIME = Integer.MAX_VALUE;
        TIME_OUT = Integer.MAX_VALUE;
    }

    public Runner(int loopTime,String fileName) throws IOException {
        initialTheUsers();
        LOOP = loopTime;
        OutputTheCsv.createFile(fileName);
        USR_PER_SECOND = 0;
        USER_NUM = users.size();
        TESTING_TIME = Integer.MAX_VALUE;
        TIME_OUT = Integer.MAX_VALUE;
    }

    public Runner(String fileName, int userPerSecond, int testingTime) throws IOException {
        initialTheUsers();
        LOOP = 0;
        OutputTheCsv.createFile(fileName);
        TESTING_TIME = testingTime;
        USER_NUM = users.size();
        USR_PER_SECOND = userPerSecond;
        TIME_OUT = Integer.MAX_VALUE;
    }
    //Constructors END

    public static int getUserNum(){
        return USER_NUM;
    }

    //Initial the created users
    private void initialTheUsers(){
        Class<?>[] classes = jLoadFile.class.getDeclaredClasses();

        for (Class<?> c : classes) {
            if (User.class.isAssignableFrom(c) && !c.isInterface() && !Modifier.isAbstract(c.getModifiers())) {
                try {
                    Constructor<?> constructor = c.getDeclaredConstructor();
                    constructor.setAccessible(true);
                    User userInstance = (User) constructor.newInstance();
                    users.add(userInstance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //Set users to run
    public void Run() throws InterruptedException {
        Runnable runExecution = LOOP == 0 ? this::executeInTimeControll : this::executeInLoop;
        runExecution.run();
    }

    private void executeInTimeControll(){
        ExecutorService poolExecutor = Executors.newVirtualThreadPerTaskExecutor();
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        //Control the user numbers per seconds
        AtomicInteger threadCount = new AtomicInteger(0);
        ScheduledFuture<?> runnableFuture = scheduledExecutorService.scheduleAtFixedRate(() -> {
            while (threadCount.get() < USR_PER_SECOND) {
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
        for(userIter = 0 ;userIter < LOOP;userIter++){
            for(User user : users) {
                poolExecutor.submit(user);
            }
        }
        ShutdownThreads(poolExecutor);
        ShutdownUserExecutor();
        CloseFile();
    }

    //Set tasks to run
    public static <T extends User> void RunUsers(T user) throws InterruptedException, InvocationTargetException, IllegalAccessException {
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
            TimeUnit.SECONDS.sleep(TESTING_TIME);
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
            if (!threadPool.awaitTermination(TIME_OUT,TimeUnit.SECONDS)) {
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
        OutputTheCsv.closeFile();
    }


}
