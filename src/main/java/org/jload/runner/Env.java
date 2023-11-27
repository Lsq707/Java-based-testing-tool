package org.jload.runner;

import org.jload.model.ResponseStat;
import org.jload.model.ShapeTuple;
import org.jload.output.CheckRatioFilter;
import org.jload.output.CsvOutput;
import org.jload.output.JMeterCsvOutputFilter;
import org.jload.output.HtmlReport;
import org.jload.output.RequestCsvOutputFilter;
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
import java.util.List;

/*
Process global parameters
*/
public class Env {
    private static final Logger logger = LoggerFactory.getLogger(Env.class);
    static HashMap<String, Object> userVariables = new HashMap<>();
    static final Class<?> shapeClass = getShapeClass();
    private static List<Class<?>> definedUsers;
    private static String htmlFilePath;
    static String host;
    public static String HtmlCsvPath;
    public static String RequestCsvPath;
    public static double checkFailRatio = -1;
    public static double checkAvgResponseTime = -1;
    public static List<ResponseStat> responseStats = new ArrayList<>();
    public static DecimalFormat df = new DecimalFormat("#.##");

    /*
    Get the customized shape class in jLoadFile
    */
    private static Class<?> getShapeClass() {
        List<Class<?>> scanShapeClass = ClassScanner.getClasses("LoadTestShape");
        if (scanShapeClass.size() > 1) {
            logger.error("Only one customized shape allowed");
            return null;
        }
        if (scanShapeClass.isEmpty()) {
            logger.info("Using default shape");
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
            loadTestShape = defaultShape();
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
    private static void getUserClass() {
        boolean chosenUser = !(EnvBuilder.chosenUsers == null);
        definedUsers = new ArrayList<>();
        List<Class<?>> Users = ClassScanner.getClasses("User");
        for (Class<?> cls : Users) {
            try {
                if (User.class.isAssignableFrom(cls) && !cls.isInterface() && !Modifier.isAbstract(cls.getModifiers())) {
                    if (chosenUser) {
                        definedUsers.add(cls);
                    } else {
                        definedUsers.add(cls);
                    }
                    logger.info("User class {} defined", cls.getName());
                }
            } catch (Exception e) {
                logger.error("Error getting the definition of class {}: {}", cls.getName(), e.getMessage(), e);
            }
        }
    }

    public static List<Class<?>> getUsers() {
        return definedUsers;
    }

    private static LoadTestShape defaultShape() {
        int testingTime = Runner.getTestingTime();
        int spawnRate = Runner.getSpawnRate();
        int userCount = Runner.getUserNum();
        int usersSize = definedUsers.size();

        if (usersSize == 0) {
            logger.error("No testing User found");
            throw new IllegalStateException("No defined users available.");
        }

        int usersPerShape = userCount / usersSize;
        final int[] remainingUsers = {userCount % usersSize};
        int ratePerUser = spawnRate / usersSize;
        final int[] remainingRate = {spawnRate % usersSize}; // Remaining spawn rate after equal distribution

        return new LoadTestShape() {
            @Override
            public List<ShapeTuple> tick() {
                if (getRunTime() > testingTime) {
                    return null;
                }
                List<ShapeTuple> results = new ArrayList<>();
                for (Class<?> userClass : definedUsers) {
                    int thisUserCount = usersPerShape;
                    int thisRatePerUser = ratePerUser;

                    // Distributing remaining users
                    if (remainingUsers[0] > 0) {
                        thisUserCount++;
                        remainingUsers[0]--;
                    }

                    // Distributing remaining spawn rate
                    if (remainingRate[0] > 0) {
                        thisRatePerUser++;
                        remainingRate[0]--;
                    }

                    String className = getClsName(userClass);
                    results.add(new ShapeTuple(className, thisUserCount, thisRatePerUser));
                }
                return results;
            }
        };
    }

    /*
    Add Hook to close the resources when the program was interrupted
     */
    public static void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook is running...");
            Runner.shutdownHook();
            closeFile();
        }));
    }

    /*
    Initial the related class
    Start the testing
     */
    public static void startTesting(EnvBuilder builder) throws IOException, InterruptedException {
        //Init the Users
        getUserClass();
        host = builder.getHost();
        htmlFilePath = builder.getHtmlFile();

        //Whether to add ratio check
        Statistics.registerFilter(new CheckRatioFilter());

        //Whether generate CSV result
        if (builder.getCsvFileName() != null) {
            setRequestCsvPath(builder.getCsvFileName());
            CsvOutput.createRequestCsvFile(RequestCsvPath);
            Statistics.registerFilter(new RequestCsvOutputFilter());
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

        //closeFile();
    }

    private static void setRequestCsvPath(String path) {
        RequestCsvPath = path + "_Request.csv";
    }

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
    private static String getClsName(Class<?> cls) {
        String name = null;
        int lastDot = cls.getName().lastIndexOf(".");
        name = cls.getName().substring(lastDot + 1);
        return name;
    }

    static String getClsName(User user) {
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
        CsvOutput.closeFile();
        //Generate html Repo
        if (HtmlCsvPath != null && htmlFilePath != null) {
            HtmlReport.generateHtml(HtmlCsvPath, htmlFilePath);
            logger.info("Generating html File");
        }
    }
}
