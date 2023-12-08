package org.jload.output;

import org.jload.exceptions.RatioException;
import org.jload.model.ResponseStat;
import org.jload.response.StatisticsFilter;
import org.jload.runner.Env;
import org.jload.runner.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
    private static ScheduledExecutorService scheduledCheckService;
    private static ScheduledFuture<?> checkingFuture;
    private static final ConcurrentHashMap<Long, Long> dataMap = new ConcurrentHashMap<>();

    public CheckRatioFilter(){
        checkMetrics();
    }

    @Override
    public void process(ResponseStat responseStat) {
        long currentSeconds = convertToSeconds(responseStat.timeStamp());
        addOrUpdate(currentSeconds);
        totalResponseNum.incrementAndGet();
        if (!responseStat.success()) {
            failNum.incrementAndGet();
        }
        totalResponseTime.addAndGet(responseStat.elapsed());
    }

    public void addOrUpdate(Long key) {
        dataMap.compute(key, (k, v) -> (v == null) ? 1 : v + 1);
    }

    private void checkMetrics() {
        scheduledCheckService = Executors.newScheduledThreadPool(1);
        checkingFuture = scheduledCheckService.scheduleWithFixedDelay(() -> {
            long previousSeconds = System.currentTimeMillis() / 1000 - 1;
            if(dataMap.get(previousSeconds) != null){
                printInfo(previousSeconds);
                try {
                    if (Env.checkFailRatio > 0) {
                        double currentFailRatio = totalResponseNum.get() > 0 ? (double) failNum.get() / totalResponseNum.get() : 0;
                        if(currentFailRatio > Env.checkFailRatio)
                            throw new RatioException("Test Failed due to failure ratio > " + Env.checkFailRatio);
                    }
                    if (Env.checkAvgResponseTime > 0) {
                        double currentAvgResponseTime = totalResponseNum.get() > 0 ? (double) totalResponseTime.get() / totalResponseNum.get() : 0;
                        if(currentAvgResponseTime > Env.checkAvgResponseTime)
                            throw new RatioException("Test Failed due to average response time > " + Env.checkAvgResponseTime);
                    }
                } catch (RatioException e) {
                    logger.error(e.getMessage(), e);
                    //Shut down
                    Runner.setTestFlag(false);
                }
                dataMap.remove(previousSeconds);
            }
        }, 0, 1, TimeUnit.SECONDS);
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

    public static void printInfo(long previousSeconds){
        long rps = dataMap.get(previousSeconds);
        double currentFailRatio = totalResponseNum.get() > 0 ? (double) failNum.get() / totalResponseNum.get() : 0;
        double currentAvgResponseTime = totalResponseNum.get() > 0 ? (double) totalResponseTime.get() / totalResponseNum.get() : 0;

        String message = String.format("Requests: %d Fails: %d RPS: %s AvgResponseTime: %s FailRatio: %s",
                totalResponseNum.get(), failNum.get(), rps, Env.df.format(currentAvgResponseTime), Env.df.format(currentFailRatio));
        logger.info(message);
    }

    public static void cleanDataMap(){
        for (Map.Entry<Long, Long> entry : dataMap.entrySet()){
            printInfo(entry.getKey());
        }
    }
}

