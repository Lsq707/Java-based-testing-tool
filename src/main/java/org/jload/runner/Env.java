package org.jload.runner;

import org.jload.exceptions.ShapeException;
import org.jload.model.ResponseStat;
import org.jload.model.ShapeTuple;
import org.jload.output.CheckRatioFilter;
import org.jload.output.CsvOutput;
import org.jload.output.JMeterCsvOutputFilter;
import org.jload.output.HtmlReport;
import org.jload.response.Statistics;

import org.jload.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
Process global parameters
*/
public class Env {
    private static final Logger logger = LoggerFactory.getLogger(Env.class);
    private static final HashMap<String, Object> userVariables = new HashMap<>();
    private static Class<?> shapeClass;
    private static Set<Class<?>> definedUsers = getUserClass();
    private static String htmlFilePath;
    private static String host;
    public static String HtmlCsvPath;
    //public static String RequestCsvPath;
    public static double checkFailRatio = -1;
    public static double checkAvgResponseTime = -1;
    public static DecimalFormat df = new DecimalFormat("0.00");
    public static Map<String, Integer> chosenUsers = new HashMap<>();
    public static String chosenShape;
    //Chose users from cmd line or not
    public static boolean chooseUser = false;
    public static boolean safeExit = false;
    public static Set<String> taskTag;

    /*
    Get the customized shape class in jLoadFile
    */
    private static Class<?> getShapeClass() throws ShapeException {
        List<Class<?>> scanShapeClass = ClassScanner.getClasses("LoadTestShape");
        if (scanShapeClass.size() > 1) {
            if (chosenShape == null) {
                throw new ShapeException("Only one customized shape allowed");
                //logger.error("Only one customized shape allowed");
            } else {
                for (Class<?> shapeClass : scanShapeClass) {
                    if (getClsName(shapeClass).equals(chosenShape)) {
                        return shapeClass;
                    }
                }
                throw new ShapeException("Specified Shape doesn't exist");
                //logger.error("Specified Shape doesn't exist");
            }
            //return null;
        }
        if (scanShapeClass.isEmpty()) {
            return null;
        }
        return scanShapeClass.get(0);
    }

    /*
    Initial the shape class declared or default
     */
    public static LoadTestShape initShape() {
        LoadTestShape loadTestShape = null;
        if (shapeClass == null) {
            try {
                loadTestShape = defaultShape();
            } catch (ShapeException e) {
                logger.error("Error in Env class: {}", e.getMessage(), e);
            }
        } else if (LoadTestShape.class.isAssignableFrom(shapeClass) && !shapeClass.isInterface() && !Modifier.isAbstract(shapeClass.getModifiers())) {
            try {
                Constructor<?> constructor = shapeClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                loadTestShape = (LoadTestShape) constructor.newInstance();
                logger.info("Instance of {} created: {}", shapeClass.getName(), loadTestShape);
            } catch (Exception e) {
                logger.error("Error creating an instance of class {}: {}", shapeClass.getName(), e.getMessage(), e);
            }
        }
        return loadTestShape;
    }

    /*
     Get the user class in jLoadFile
    */
    private static Set<Class<?>> getUserClass() {

        definedUsers = new HashSet<>();
        List<Class<?>> Users = ClassScanner.getClasses("User");
        for (Class<?> cls : Users) {
            try {
                if (User.class.isAssignableFrom(cls) && !cls.isInterface() && !Modifier.isAbstract(cls.getModifiers())) {
                    definedUsers.add(cls);
                    logger.debug("User class {} defined", cls.getName());
                }
            } catch (Exception e) {
                logger.error("Error getting the definition of class {}: {}", cls.getName(), e.getMessage(), e);
            }
        }
        if (definedUsers.isEmpty()) {
            logger.error("Error in specified userName or user class - Caused by definedUser is zero");
        }
        return definedUsers;
    }

    public static Set<Class<?>> getUsers() {
        return definedUsers;
    }

    public static LoadTestShape defaultShape() throws ShapeException {
        logger.info("Using defaultShape");
        int testingTime = Runner.getTestingTime();
        int computeUserCount = Runner.getUserNum();
        int spawnRate = Runner.getSpawnRate();
        int userCount = Runner.getUserNum();
        logger.debug("Default shape arguments-chosenUser: {}", chosenUsers);
        Map<String, Integer> userCounts = new HashMap<>();

        //Specified user class
        Set<Class<?>> chosenClass = new HashSet<>();
        Set<Class<?>> unassignedClass = new HashSet<>();
        if (chosenUsers.isEmpty()) { //No specified user class Distribute the metrics among user class evenly -u -uc both null
            chosenClass = definedUsers;
            Map<String, Integer> unassignedUserCounts = calculateUserCounts(userCount, chosenClass);
            userCounts.putAll(unassignedUserCounts);
        } else if (chooseUser) { //Condition1: -u has parameters
            for (Class<?> cls : definedUsers) {
                String name = getClsName(cls);
                if (chosenUsers.containsKey(name)) {
                    chosenClass.add(cls);
                    int count = chosenUsers.get(name);
                    if (count > -1) {
                        userCounts.put(name, chosenUsers.get(name)); //Condition2: -u has parameters -uc also
                        computeUserCount -= count;
                    } else {
                        unassignedClass.add(cls);
                    }
                }
            }
        } else { //Condition3: -uc has parameters -u has no
            for (Class<?> cls : definedUsers) {
                String name = getClsName(cls);
                if (chosenUsers.containsKey(name)) {
                    chosenClass.add(cls);
                    int count = chosenUsers.get(name);
                    userCounts.put(name, count);
                    computeUserCount -= count;
                    if (computeUserCount < 0) {
                        throw new ShapeException("Error in user count distribution");
                    }
                } else {
                    unassignedClass.add(cls);
                }
            }
        }
        //Distribute user count
        if (computeUserCount != 0 && !unassignedClass.isEmpty()) {
            Map<String, Integer> unassignedUserCounts = calculateUserCounts(computeUserCount, unassignedClass);
            userCounts.putAll(unassignedUserCounts);
            logger.debug("Default shape arguments-DistributedUserCount: " + userCounts);
        }

        if (userCounts.isEmpty() && unassignedClass.isEmpty()) {
            throw new ShapeException("No testing User found");
        }

        int finalUsersSize = userCounts.size();
        logger.debug("Default shape arguments-userCount: {}", userCounts);
        return new LoadTestShape() {
            @Override
            public List<ShapeTuple> tick() {
                if (getRunTime() > (double) testingTime / 1000) {
                    return null;
                }
                List<ShapeTuple> results = new ArrayList<>();

                //Assign SpawnRate
                int ratePerUser = spawnRate / finalUsersSize;
                int remainingRate = spawnRate % finalUsersSize;

                Map<String, List<User>> activeUsersSnapshot = new HashMap<>(Runner.getActiveUsers());

                for (Map.Entry<String, Integer> entry : userCounts.entrySet()) {
                    String key = entry.getKey();
                    int value = entry.getValue();
                    if (value == 0) {
                        logger.warn("{} has no quota", key);
                    }
                    int currentRatePerUser = ratePerUser;
                    if (remainingRate > 0) {
                        currentRatePerUser++;
                        remainingRate--;
                    }

                    // Check if it needs to generate if not add its rate to remainingRate
                    if (activeUsersSnapshot.containsKey(key) && activeUsersSnapshot.get(key).size() == value) {
                        remainingRate += currentRatePerUser;
                        results.add(new ShapeTuple(key, value, -1)); // -1 is the symbol of finishing generate
                        continue;
                    }
                    results.add(new ShapeTuple(key, value, currentRatePerUser));
                }
                boolean allDone = true;

                //Distribute the remaining rate
                while (remainingRate > 0) {
                    for (ShapeTuple shapeTuple : results) {
                        int currentRate = shapeTuple.getSpawnRate();
                        if (currentRate != -1) {
                            allDone = false;
                            shapeTuple.setSpawnRate(currentRate++);
                            remainingRate--;
                        }
                    }
                    if (allDone) {
                        break;
                    }
                }
                return results;
            }
        };
    }

    private static Map<String, Integer> calculateUserCounts(int totalUserCount, Set<Class<?>> unassignedUsers) {
        Map<String, Integer> userCounts = new HashMap<>();
        int usersPerShape = totalUserCount / unassignedUsers.size();
        int remainingUsers = totalUserCount % unassignedUsers.size();

        for (Class<?> userClass : unassignedUsers) {
            int thisUserCount = usersPerShape;
            if (remainingUsers > 0) {
                thisUserCount++;
                remainingUsers--;
            }
            userCounts.put(getClsName(userClass), thisUserCount);
        }

        return userCounts;
    }

    /*
    Add Hook to close the resources when the program was interrupted
     */
    public static void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!safeExit) {
                logger.info("Shutdown hook is running...");
                Runner.shutdownHook();
                closeFile();
            }
        }));
    }

    /*
    Initial the related class
    Start the testing
     */
    public static void startTesting(EnvBuilder builder) throws IOException, InterruptedException, ShapeException {
        //Init the Users
        shapeClass = getShapeClass();
        host = builder.getHost();
        htmlFilePath = builder.getHtmlFile();

        //Whether to add ratio check
        Statistics.registerFilter(new CheckRatioFilter());

        //Whether generate CSV result

        if (builder.getCsvFileName() != null) {
            //setRequestCsvPath(builder.getCsvFileName());
            //CsvOutput.createRequestCsvFile(RequestCsvPath);
            //Statistics.registerFilter(new RequestCsvOutputFilter());
        }

        //Whether to gengerate Html
        if (htmlFilePath != null && builder.getCsvFileName() != null) {
            setHtmlCsvPath(builder.getCsvFileName());
            CsvOutput.createHtmlCsvFile(HtmlCsvPath);
            Statistics.registerFilter(new JMeterCsvOutputFilter());
        }

        Runner runner = builder.runnerBuild();
        //Start test
        runner.run();

        closeFile();
    }

    /*
    private static void setRequestCsvPath(String path) {
        RequestCsvPath = path + "_Request.csv";
    }
     */

    private static void setHtmlCsvPath(String path) {
        HtmlCsvPath = path + "_Result.csv";
    }

    /*
    Get the user Variable defined before
     */
    public static Object getVariable(String name) {
        return userVariables.get(name);
    }

    /*
    Define the user Variable that can be used by other users or tasks
     */
    public static void putVariable(String name, Object variable) {
        userVariables.put(name, variable);
    }

    /*
    Get the class name without pkg name
    */
    public static String getClsName(Class<?> cls) {
        String name = null;
        int lastDot = cls.getName().lastIndexOf(".");
        name = cls.getName().substring(lastDot + 1);
        return name;
    }

    public static String getClsName(User user) {
        String name = null;
        int lastDot = user.getClass().getName().lastIndexOf(".");
        name = user.getClass().getName().substring(lastDot + 1);
        return name;
    }

    /*
    Validate the user input
    */
    private static void validateLoopTime(int loopTime) {
        if (loopTime <= 0) {
            throw new IllegalArgumentException("Loop time must be greater than 0");
        }
        logger.error("The loop times must greater than 0");
    }

    /*
    Close the CSV file
    */
    private static void closeFile() {
        safeExit = true;
        CsvOutput.closeFile();
        //Generate html Repo
        if (HtmlCsvPath != null && htmlFilePath != null) {
            HtmlReport.generateHtml(HtmlCsvPath, htmlFilePath);
            logger.info("Generating html File");
        }
    }

    public static String getHost() {
        return host;
    }

    public static void setHost(String host) {
        Env.host = host;
    }
}
