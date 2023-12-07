package org.jload.output;

import org.jload.exceptions.RatioException;
import org.jload.model.ResponseStat;
import org.jload.model.ShapeTuple;
import org.jload.response.StatisticsFilter;
import org.jload.runner.Env;
import org.jload.runner.LoadTestShape;
import org.jload.runner.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class CheckRatioFilter implements StatisticsFilter {
    private static final Logger logger = LoggerFactory.getLogger(CheckRatioFilter.class);
    public static final AtomicLong totalResponseTime = new AtomicLong(0);
    public static final AtomicLong failNum = new AtomicLong(0);
    public static final AtomicLong totalResponseNum = new AtomicLong(0);
    public static final AtomicLong responseNum = new AtomicLong(0);
    private static final AtomicLong previousTime = new AtomicLong(0);
    private static ScheduledExecutorService scheduledCheckService;
    private static ScheduledFuture<?> checkingFuture;

    public CheckRatioFilter(){ //If needs to check ratio then do a check per seconds
        if (Env.checkFailRatio > 0 || Env.checkAvgResponseTime > 0) {
            checkMetrics();
        }
    }

    @Override
    public void process(ResponseStat responseStat) {
        long currentTime = convertToSeconds(responseStat.timeStamp());
        if(currentTime - previousTime.get() > 0 && totalResponseNum.get() != 0){ //Print each seconds
            logger.info(Runner.printInfo());
            responseNum.set(0);
        }
        responseNum.incrementAndGet();
        totalResponseNum.incrementAndGet();
        if (!responseStat.success()) {
            failNum.incrementAndGet();
        }
        totalResponseTime.addAndGet(responseStat.elapsed());
        previousTime.set(currentTime);
    }

    private void checkMetrics() {
        scheduledCheckService = Executors.newScheduledThreadPool(1);
        checkingFuture = scheduledCheckService.scheduleWithFixedDelay(() -> {
            double currentFailRatio = responseNum.get() > 0 ? (double) failNum.get() / responseNum.get() : 0;
            double currentAvgResponseTime = responseNum.get() > 0 ? (double) totalResponseTime.get() / responseNum.get() : 0;
            try {
                if (Env.checkFailRatio > 0 && currentFailRatio > Env.checkFailRatio) {
                    throw new RatioException("Test Failed due to failure ratio > " + Env.checkFailRatio);
                }

                if (Env.checkAvgResponseTime > 0 && currentAvgResponseTime > Env.checkAvgResponseTime) {
                    throw new RatioException("Test Failed due to average response time > " + Env.checkAvgResponseTime);
                }
            } catch (RatioException e) {
                logger.error(e.getMessage(), e);
                //Shut down
                Runner.setTestFlag(false);
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    private static long convertToSeconds(String timeStamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(timeStamp, formatter);
        // Assuming the system's default time zone. Adjust if necessary.
        return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public static ScheduledExecutorService getScheduledCheckService() {
        return scheduledCheckService;
    }

    public static ScheduledFuture<?> getCheckingFuture() {
        return checkingFuture;
    }
}

