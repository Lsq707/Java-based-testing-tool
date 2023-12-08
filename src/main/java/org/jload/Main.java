package org.jload;

import org.jload.runner.Env;
import org.jload.runner.EnvBuilder;
import org.apache.commons.cli.*;
import org.jload.runner.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {


        Env.shutdownHook();

        // Create the command line parser
        CommandLineParser parser = new DefaultParser();
        Options options = getOptions();

        try {
            EnvBuilder envBuilder = new EnvBuilder();

            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("host")) {
                envBuilder.setHost(cmd.getOptionValue("host"));
            }
            if (cmd.hasOption("user")) {
                envBuilder.setChosenUser(cmd.getOptionValue("user"));
            }
            if (cmd.hasOption("userCount")) {
                String[] userCounts = cmd.getOptionValues("uc");
                for(String uc:userCounts){
                    envBuilder.setUserCount(uc);
                }
            }
            if (cmd.hasOption("Loop")) {
                envBuilder.setLoopTime(Integer.parseInt(cmd.getOptionValue("Loop")));
            }
            if (cmd.hasOption("Duration")) {
                envBuilder.setTestingTime(Integer.parseInt(cmd.getOptionValue("Duration")));
            }
            if (cmd.hasOption("csvLocation")) {
                envBuilder.setCsvFileName(cmd.getOptionValue("csvLocation"));
            }
            if (cmd.hasOption("htmlLocation")) {
                envBuilder.setHtmlFile(cmd.getOptionValue("htmlLocation"));
            }
            if (cmd.hasOption("spawnRate")) {
                envBuilder.setSpawnRate(Integer.parseInt(cmd.getOptionValue("spawnRate")));
            }
            if (cmd.hasOption("totalUserCount")) {
                envBuilder.setUserNum(Integer.parseInt(cmd.getOptionValue("totalUserCount")));
            }
            if (cmd.hasOption("failRatioCheck")) {
                envBuilder.setCheckFailRatio(Double.parseDouble(cmd.getOptionValue("failRatioCheck")));
            }
            if (cmd.hasOption("responseTimeCheck")) {
                envBuilder.setCheckAvgResponseTime(Double.parseDouble(cmd.getOptionValue("responseTimeCheck")));
            }
            if (cmd.hasOption("customizedShape")) {
                envBuilder.setChosenShape(cmd.getOptionValue("customizedShape"));
            }
            if (cmd.hasOption("tag")) {
                envBuilder.setTaskTags(cmd.getOptionValue("tag"));
            }


            /*
        try {

            //Start test
            //LOOP
            //Env.startTesting(new EnvBuilder().setLoopTime(3).setCsvFileName("/Users/liusiqi/Desktop/test2").setHtmlFile("/Users/liusiqi/Desktop/TRY2"));
            Env.startTesting(new EnvBuilder()
                    //.setChosenUser("MyUser")
                    .setUserCount("MyUser 1")
                    //.setUserCount("MyUser2 2")
                    .setCsvFileName("/Users/liusiqi/Desktop/test_res")
                    .setHtmlFile("/Users/liusiqi/Desktop/TRY4") // can comment it out if no need
                    .setUserNum(3)
                    .setSpawnRate(2)
                    .setTaskTags("V1")
                    //.setCheckAvgResponseTime(3) // milliseconds
                    .setTestingTime(10)
                    );
             */
            Env.startTesting(envBuilder);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.info("Thread was interrupted, Failed to complete operation");
            logger.debug("Error in main: {}",e.getMessage(), e);
            System.exit(1);
        } catch (ParseException e) {
            logger.info(e.getMessage());
            // Automatically generate the help statement
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("commandline", options);
            System.exit(1);
        }catch (Exception e){
            logger.info(e.getMessage());
            System.exit(1);
        }

    }

    private static Options getOptions() {
        Options options = new Options();

        options.addOption("h", "host", true, "Host name");
        options.addOption("i", "Loop", true, "Indicates the number of iterations to run each user task");
        options.addOption("d", "Duration", true, "Sets the duration of the testing period in seconds");
        options.addOption("r", "spawnRate", true, "Rate to spawn users at (users per second)");
        options.addOption("u", "user", true, "Select specific user class for testing");
        options.addOption("uc", "userCount", true, "Specify the number of users to simulate for each chosen user type");
        options.addOption("ut", "totalUserCount", true, "Total number of users involved in the testing process");
        options.addOption("t", "Tag", true, "Specify task tags");
        options.addOption("csv", "csvLocation", true, "Directory path for storing CSV files");
        options.addOption("html", "htmlLocation", true, "Directory path for storing HTML report files.");
        options.addOption("check_failRatio", "failRatioCheck", true, "Check fail Ratio per seconds");
        options.addOption("check_avgResponseTime", "responseTimeCheck", true,"Check avg response time per seconds");
        options.addOption("shape", "customizedShape", true,"Specify the customShape when there are more than 1 customized shape defined");

        return options;

    }
}