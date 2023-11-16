package org.jload;

import org.jload.output.CsvOutput;
import org.jload.runner.Runner;
import org.jload.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(CsvOutput.class);
    public static void main(String[] args) throws Exception {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown hook is running...");
            CsvOutput.closeFile();
        }));

        try {
            // Your main program code here
            //new Runner({locationOfOutput},{userPerSeconds},{testingTime})
            //Runner runner = new Runner("/Users/liusiqi/Desktop/test.csv",2,1);

            //new Runner({loop},{locationOfOutput})
            Runner runner = new Runner("/Users/liusiqi/Desktop/test2.csv",6,2,10);
            //Start test
            runner.run();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread was interrupted, Failed to complete operation");
        }


    }
}