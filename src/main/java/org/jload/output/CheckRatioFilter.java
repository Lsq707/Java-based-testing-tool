package org.jload.output;

import org.jload.exceptions.RatioException;
import org.jload.model.ResponseStat;
import org.jload.model.ScreenOutputs;
import org.jload.response.StatisticsFilter;
import org.jload.runner.Env;
import org.jload.runner.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CheckRatioFilter implements StatisticsFilter {
    private static final Logger logger = LoggerFactory.getLogger(CheckRatioFilter.class);
    private static ScheduledExecutorService scheduledCheckService;
    private static ScheduledFuture<?> checkingFuture;

    public CheckRatioFilter() {
        checkMetrics();
    }

    @Override
    public void process(ResponseStat responseStat) {
    }

    private void checkMetrics() {
        scheduledCheckService = Executors.newScheduledThreadPool(1);
        checkingFuture = scheduledCheckService.scheduleWithFixedDelay(() -> {
            try {
                List<ScreenOutputs> screenOutputs = ScreenMetricsFilter.getScreenOutputs();
                if (Env.checkFailRatio > 0 || Env.checkAvgResponseTime > 0) {
                    if (screenOutputs.isEmpty()) {
                        logger.debug("screenOutputs is empty.");
                    }
                    for (ScreenOutputs data : screenOutputs) {
                        if (data.getFailRatio() > Env.checkFailRatio) {
                            throw new RatioException("Test Failed due to failure ratio > " + Env.checkFailRatio);
                        }
                        if ((data.getTotalResponseTime() / data.getTotalRequestNum()) > Env.checkAvgResponseTime) {
                            throw new RatioException("Test Failed due to average response time > " + Env.checkAvgResponseTime);
                        }
                    }
                }
            } catch (RatioException e) {
                logger.error(e.getMessage(), e);
                //Shut down
                Runner.setTestFlag(false);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static ScheduledExecutorService getScheduledCheckService() {
        return scheduledCheckService;
    }

    public static ScheduledFuture<?> getCheckingFuture() {
        return checkingFuture;
    }
}

