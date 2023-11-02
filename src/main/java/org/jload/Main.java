package org.jload;

import org.jload.runner.Runner;


public class Main {

    public static void main(String[] args) throws Exception {
        //new Runner({locationOfOutput},{userPerSeconds},{testingTime})
        //Runner runner = new Runner("/Users/liusiqi/Desktop/test.csv",2,1);

        //new Runner({loop},{locationOfOutput})
        Runner runner = new Runner(1,"/Users/liusiqi/Desktop/test2.csv");
        //Start test
        runner.run();

    }
}