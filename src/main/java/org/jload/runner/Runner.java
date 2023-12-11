package org.jload.runner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.jload.model.ShapeTuple;
import org.jload.output.CheckRatioFilter;
import org.jload.user.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/*
The strategy to run the testing
*/
public class Runner {
    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    private static int timeOut;
    private static int duration;
    private static int spawnRate;
    private static int userNum;
    private static Set<Class<?>> definedUsers;
    private static ConcurrentHashMap<String, List<User>> activeUsers;
    private static ScheduledExecutorService scheduledExecutorService;
    private static ScheduledFuture<?> runnableFuture;
    private static int loop;
    private static final AtomicBoolean testFlag = new AtomicBoolean(true);
    private static final ExecutorService userExecutor = Executors.newVirtualThreadPerTaskExecutor();
    private static final AtomicBoolean isFirstRequestSent = new AtomicBoolean(false);
    private Thread timingThread;

    /*
    //FOR TEST
    private static final ExecutorService userExecutor = Executors.newThreadPerTaskExecutor(new ThreadFactory() {
        private int count = 0;
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "UserThread-" + count++);
        }
    });

     */
    private static Set<String> assignedThread;
    static final Object lock = new Object(); //For interrupt timing if the shape returns null

    //Usable in pkg
    Runner(int loopTime, int userNum, int spawnRate, int duration) {
        definedUsers = Env.getUsers();
        loop = loopTime;
        Runner.duration = duration * 1000;
        Runner.spawnRate = spawnRate;
        Runner.userNum = userNum;
        timeOut = Integer.MAX_VALUE;
        activeUsers = new ConcurrentHashMap<>();
        assignedThread = ConcurrentHashMap.newKeySet();
    }

    /*
    Initial the users declared in jLoadFile
    */
    private User initUser(Class<?> User) {
        User userInstance = null;
        try {
            Constructor<?> constructor = User.getDeclaredConstructor();
            constructor.setAccessible(true);
            userInstance = (User) constructor.newInstance();

            if (userInstance.getUserParamHost().isBlank() && Env.getHost() != null) {
                setHost(userInstance);
            }

            logger.debug("Instance of {} created: {}", User.getName(), userInstance);
        } catch (Exception e) {
            logger.error("Error creating an instance of class {}: {}", User.getName(), e.getMessage(), e);
        }
        return userInstance;
    }

    private void setHost(User userInstance) {
        userInstance.setUserParamHost(Env.getHost());
    }

    /*
    Two ways running the testing
    * loop times
    * Shape (add or dispose users at different time)
    */
    public void run() throws InterruptedException {
        timingTest();
        Runnable runExecution = loop == 0 ? this::executeInShapeControl : this::executeInLoop;
        runExecution.run();
        //Wait for the timing
        timingThread.join();
        endTesting();
    }

    private void timingTest() {
        timingThread = Thread.startVirtualThread(() -> {
            while (!isFirstRequestSent.get() && testFlag.get()) {
                // Waiting for the first request
            }
            logger.debug("Testing start Time: {}", System.currentTimeMillis());
            synchronized (lock) {
                try {
                    lock.wait(duration);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
            logger.debug("Timing thread: TIME UP");
        });
    }

    /*
    Get the customized shape or default shape,
    check the condition each seconds to add or dispose users
    */
    private void executeInShapeControl() {
        scheduledExecutorService = Executors.newScheduledThreadPool(1);
        LoadTestShape loadTestShape = Env.initShape();
        runnableFuture = scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                logger.debug("Active Users: {}", printOutActiveUsr());
                List<ShapeTuple> shapeTuples = loadTestShape.tick();
                if (shapeTuples == null) {
                    logger.debug("shapeTuples returns null");
                    setTestFlag(false);
                } else {
                    adjustUser(shapeTuples);
                }
            } catch (Exception e) {
                logger.error("Error in Runner {}", e.getMessage(), e);
                throw new RuntimeException("Testing stopped due to an error: " + e.getMessage(), e);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void setTestFlag(boolean value) {
        synchronized (lock) {
            testFlag.set(value);
            lock.notifyAll(); //Interrupt the sleep when the shape return null
        }
        logger.debug("Set the testing flag to {}", testFlag.get());
    }

    /*
    Active user count for each user class
    */
    private List<String> printOutActiveUsr() {
        List<String> info = new ArrayList<>();
        for (Map.Entry<String, List<User>> entry : activeUsers.entrySet()) {
            info.add(entry.getKey() + " currentRunning: " + String.valueOf(entry.getValue().size()));
        }
        return info;
    }

    /*
    Adjust running user numbers
    */
    private synchronized void adjustUser(List<ShapeTuple> tick) {
        logger.debug("Shapes: {}", tick);
        for (ShapeTuple shapeTuple : tick) {
            logger.trace(String.valueOf(shapeTuple));
            long currNum = countActiveUser(shapeTuple.getUserCls());
            long desiredNum = shapeTuple.getClsTotalNum();
            long spawnRate = shapeTuple.getSpawnRate();
            long difference = desiredNum - currNum;
            if (difference > 0) {
                for (int i = 0; i < difference && i < spawnRate; i++) {
                    addUser(shapeTuple.getUserCls());
                }
            } else if (difference < 0) {
                for (int i = 0; i < -difference && i < spawnRate; i++) {
                    disposeUser(shapeTuple.getUserCls());
                }
            }
        }
    }

    /*
    Shut the user of certain class which means remove the earliest created one
    */
    private static void disposeUser(String clsName) {
        List<User> users = activeUsers.get(clsName);
        if (!users.isEmpty()) {
            User usr = users.get(0);
            usr.setTaskFlag(false);
            users.remove(usr);
            if (users.isEmpty()) {
                activeUsers.remove(clsName);
            }
        }
    }

    public static void disposeUser(User user) {
        user.setTaskFlag(false);
        String uName = Env.getClsName(user);
        List<User> users = activeUsers.get(uName);
        users.remove(user);
        if (users.isEmpty()) {
            activeUsers.remove(uName);
        }
    }

    /*
    Add the user of certain class
    */
    private void addUser(String clsName) {
        User user = null;
        for (Class<?> cls : definedUsers) {
            if (getClsName(cls).equals(clsName)) {
                user = initUser(cls);
                userExecutor.submit(user);    //start the user
            }
        }
        if (user != null) {
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
        if (user == null) {
            logger.error("No such user class: {}", clsName);
        }
    }

    /*
    Get the class name without pkg name
     */
    private String getClsName(Class<?> cls) {
        String name = null;
        int lastDot = cls.getName().lastIndexOf(".");
        name = cls.getName().substring(lastDot + 1);
        return name;
    }

    /*
    Count the active user number of certain type
     */
    private long countActiveUser(String clsName) {
        if (activeUsers.get(clsName) != null) {
            return activeUsers.get(clsName).size();
        }
        return 0;
    }

    /*
    Execute the testing with loop times
    Each user and their tasks will execute once only then end the testing
    */
    private void executeInLoop() {
        for (Class<?> user : definedUsers) {
            if (Env.chooseUser) {
                if (Env.chosenUsers.get(getClsName(user)) == null) {
                    continue;
                }
            }
            addUser(getClsName(user));
        }

        //Execute until all the tasks are finished
        while (!activeUsers.isEmpty()) {
            synchronized (lock) {
                try {
                    lock.wait(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        setTestFlag(false);
    }

    /*
    Shut down the threads in thread pool
     */
    private static <T extends ExecutorService> void shutdownThreads(T threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(timeOut, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /*
    Stop all the running mission
     */
    static void shutdownHook() {
        logger.info("Shutting Down");
        if (runnableFuture != null) {
            runnableFuture.cancel(true);
            shutdownThreads(scheduledExecutorService);
        }
        endTesting();
    }

    /*
    Shut down all the ExecutorService in each active user
     */
    static void shutdownAllUsers() {
        if (activeUsers != null) {
            for (Map.Entry<String, List<User>> entry : activeUsers.entrySet()) {
                String key = entry.getKey();
                while (!entry.getValue().isEmpty()) {
                    disposeUser(key);
                }
                activeUsers.remove(key);
            }
            shutdownThreads(userExecutor);
        }
    }

    /*
    The process of ending test for shape control testing
     */
    private static void endTesting() {
        logger.debug("Executing endTesting");
        if (scheduledExecutorService != null) {
            runnableFuture.cancel(true);
            shutdownThreads(scheduledExecutorService);
        }

        // Shutdown all user
        shutdownAllUsers();

        CheckRatioFilter.getCheckingFuture().cancel(true);
        shutdownThreads(CheckRatioFilter.getScheduledCheckService());
        CheckRatioFilter.printAll();

        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        context.close();
    }

    /*
    Pass the related parameter to Env for the default testing strategy
    */
    public static int getDuration() {
        return duration;
    }

    public static int getSpawnRate() {
        return spawnRate;
    }

    public static int getUserNum() {
        return userNum;
    }

    /*
    Return the number of user threads
     */
    public static int getActiveUsersCount() {
        int allUser = 0;
        for (Map.Entry<String, List<User>> entry : activeUsers.entrySet()) {
            allUser += entry.getValue().size();
        }
        return allUser;
    }

    public static void addPlatformThread(String platFormThreadName) {
        assignedThread.add(platFormThreadName);
    }

    public static int getUsedPlatformThreadCount() {
        return assignedThread.size();
    }

    public static ConcurrentHashMap<String, List<User>> getActiveUsers() {
        return activeUsers;
    }

    public static int getLoop() {
        return loop;
    }

    public static AtomicBoolean getIsFirstRequestSent() {
        return isFirstRequestSent;
    }
}
