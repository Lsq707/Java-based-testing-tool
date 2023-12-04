package org.jload.runner;

import org.jload.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class LoadTestShape {
    private static final Logger logger = LoggerFactory.getLogger(LoadTestShape.class);
    protected final long start_time;
    protected int testing_time = Integer.MAX_VALUE;

    protected LoadTestShape() {
        start_time = System.nanoTime();
    }

    /*
    Return the executeTime
     */
    protected long getRunTime() {
        long duration = Runner.getTestDuration();
        logger.debug("Get the duration in LoadTestShape: {}", duration);
        return duration;
    }

    /*
    The customized testing strategy
     */
    public abstract List<ShapeTuple> tick();

    public long getStartTime() {
        return start_time;
    }
}
