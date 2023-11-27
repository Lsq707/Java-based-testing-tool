package org.jload.output;

import org.jload.model.ResponseStat;
import org.jload.response.StatisticsFilter;
import org.jload.runner.Env;
import org.jload.runner.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

public class CheckRatioFilter implements StatisticsFilter {
    private static final Logger logger = LoggerFactory.getLogger(CheckRatioFilter.class);
    public static final AtomicLong failNum = new AtomicLong(0);
    public static final AtomicLong totalResponseTime = new AtomicLong(0);
    public static final AtomicLong responseNum = new AtomicLong(0);

    @Override
    public synchronized void process(ResponseStat responseStat) {
        Env.responseStats.add(responseStat);
        responseNum.incrementAndGet();
        if (!responseStat.success()) {
            failNum.incrementAndGet();
        }
        totalResponseTime.addAndGet(responseStat.elapsed());

        if (Env.checkFailRatio > 0 || Env.checkAvgResponseTime > 0) {
            checkMetrics();
        }
    }

    private void checkMetrics() {
        double currentFailRatio = responseNum.get() > 0 ? (double) failNum.get() / responseNum.get() : 0;
        double currentAvgResponseTime = responseNum.get() > 0 ? (double) totalResponseTime.get() / responseNum.get() : 0;

        try {
            if (Env.checkFailRatio > 0 && currentFailRatio > Env.checkFailRatio) {
                throw new RuntimeException("Test Failed due to failure ratio > " + Env.checkFailRatio);
            }

            if (Env.checkAvgResponseTime > 0 && currentAvgResponseTime > Env.checkAvgResponseTime) {
                throw new RuntimeException("Test Failed due to average response time > " + Env.checkAvgResponseTime);
            }
        } catch (RuntimeException e) {
            logger.error(e.getMessage(), e);
            //Shut down
            System.exit(1);
        }
    }
}

