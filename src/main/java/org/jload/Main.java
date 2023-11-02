package org.jload;

import org.jload.runner.Env;
import org.jload.runner.EnvBuilder;

public class Main {

    public static void main(String[] args) throws Exception {

        Env.shutdownHook();

        try {

            //Start test
            //LOOP
            //Env.startTesting(new RunnerBuilder().setLoopTime(3).setCsvFileName("/Users/liusiqi/Desktop/test2.csv").setHtmlFile("/Users/liusiqi/Desktop/TRY2"));

            //Metrics General
            /*
            Env.startTesting(new RunnerBuilder()
                    .setCsvFileName("/Users/liusiqi/Desktop/test2.csv")
                    .setHtmlFile("/Users/liusiqi/Desktop/TRY2")
                    .setUserNum(20)
                    .setSpawnRate(1)
                    .setTestingTime(12));
             */

            Env.startTesting(new EnvBuilder()
                    .setCsvFileName("/Users/liusiqi/Desktop/test2.csv")
                    .setHtmlFile("/Users/liusiqi/Desktop/TRY2")
                    .setHost("http://0.0.0.0:9000"));


        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread was interrupted, Failed to complete operation");
        }
    }
}