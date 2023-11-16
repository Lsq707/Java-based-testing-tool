package org.jload.runner;

import org.jload.model.*;

import java.util.ArrayList;
import java.util.List;

public abstract class LoadTestShape {
    protected final long start_time;
    protected int testing_time = Integer.MAX_VALUE;
    protected LoadTestShape(){
        start_time = System.nanoTime();
    }

    /*
    Return the executeTime
     */
    protected double getRunTime(){
        long currentTime = System.nanoTime();
        return (currentTime - start_time)/ 1_000_000_000.0;
    }

    /*
    The customized testing strategy
     */
    public abstract List<ShapeTuple> tick();

}
