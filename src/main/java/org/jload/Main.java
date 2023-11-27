package org.jload;

import org.jload.runner.Env;
import org.jload.runner.EnvBuilder;

public class Main {

    public static void main(String[] args) throws Exception {

        Env.shutdownHook();

        try {

            //Start test
            //LOOP
            //Env.startTesting(new EnvBuilder().setLoopTime(3).setCsvFileName("/Users/liusiqi/Desktop/test2").setHtmlFile("/Users/liusiqi/Desktop/TRY2"));

            Env.startTesting(new EnvBuilder()
                    .setCsvFileName("/Users/liusiqi/Desktop/test")
                    .setHtmlFile("/Users/liusiqi/Desktop/TRY2")
                    .setUserNum(1)
                    .setSpawnRate(1)
                    //.setCheckAvgResponseTime(3)
                    .setTestingTime(3));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Thread was interrupted, Failed to complete operation");
        }
    }
}