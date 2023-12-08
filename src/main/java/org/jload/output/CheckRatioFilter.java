package org.jload.output;

import org.jload.exceptions.RatioException;
import org.jload.model.CollectRequests;
import org.jload.model.PerSecondStat;
import org.jload.model.ResponseStat;
import org.jload.response.StatisticsFilter;
import org.jload.runner.Env;
import org.jload.runner.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
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
    private static final ConcurrentHashMap<String, CollectRequests> dataMap = new ConcurrentHashMap<>();

    public CheckRatioFilter() {
        checkMetrics();
    }

    @Override
    public void process(ResponseStat responseStat) {
        CollectRequests collectRequests = addOrUpdate(responseStat.label());
        collectRequests.addSecondStat(responseStat);
    }

    public CollectRequests addOrUpdate(String key) {
        return dataMap.computeIfAbsent(key, k -> new CollectRequests(key));
    }

    private void checkMetrics() {
        scheduledCheckService = Executors.newScheduledThreadPool(1);
        checkingFuture = scheduledCheckService.scheduleWithFixedDelay(() -> {
            long previousSeconds = System.currentTimeMillis() / 1000 - 1;
            if (!dataMap.isEmpty()) {
                printHead();
            }
            for (Map.Entry<String, CollectRequests> entry : dataMap.entrySet()) {
                if (entry.getValue().getTimeStat(previousSeconds) != null) {
                    printInfo(previousSeconds, entry.getValue());
                }
            }
            System.out.println();
        }, 0, 1, TimeUnit.SECONDS);
    }

    private static void printHead() {
        System.out.printf("%-60s | %7s | %-14s | %-7s | %-8s | %-11s | %-7s\n",
                "Label", "#reqs", "#fails", "Avg", "req/s", "failure/s", "avg/s");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
    }

    public static ScheduledExecutorService getScheduledCheckService() {
        return scheduledCheckService;
    }

    public static ScheduledFuture<?> getCheckingFuture() {
        return checkingFuture;
    }

    public static void printInfo(long previousSeconds, CollectRequests collectRequests) {
        PerSecondStat perSecondStat = collectRequests.getTimeStat(previousSeconds);
        System.out.printf("%-60s | %7d | %7d(%.2f%%) | %7.2f | %8d | %11d | %7.2f\n",
                collectRequests.getLable(), collectRequests.getTotalRequestNum(), collectRequests.getTotalFailNum(), collectRequests.getFailRatio(),
                collectRequests.getAvgResponseTime(), perSecondStat.getRps(), perSecondStat.getFailNum(), perSecondStat.getAvgResponseTime());
        try {
            if (Env.checkFailRatio > 0) {
                if (collectRequests.getFailRatio() > Env.checkFailRatio) {
                    throw new RatioException("Test Failed due to failure ratio > " + Env.checkFailRatio);
                }
            }
            if (Env.checkAvgResponseTime > 0) {
                if (collectRequests.getAvgResponseTime() > Env.checkAvgResponseTime) {
                    throw new RatioException("Test Failed due to average response time > " + Env.checkAvgResponseTime);
                }
            }
        } catch (RatioException e) {
            logger.error(e.getMessage(), e);
            //Shut down
            Runner.setTestFlag(false);
        }
        collectRequests.removeTimeStat(previousSeconds);
    }

    public static void cleanDataMap() {
        long unPrintedSeconds = getUnPrintedSeconds();
        long currentSeconds = System.currentTimeMillis() / 1000;
        for (; unPrintedSeconds <= currentSeconds; unPrintedSeconds++) {
            printHead();
            Iterator<Map.Entry<String, CollectRequests>> iterator = dataMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, CollectRequests> entry = iterator.next();
                if (entry.getValue().getTimeStat(unPrintedSeconds) != null) {
                    printInfo(unPrintedSeconds, entry.getValue());
                }
                if (entry.getValue().allPrint()) {
                    iterator.remove(); // Safely remove the current element
                }
            }
            if (dataMap.isEmpty()) {
                break;
            }
            System.out.println();
        }
    }

    private static long getUnPrintedSeconds() {
        long unPrintedTime = System.currentTimeMillis() / 1000;
        for (Map.Entry<String, CollectRequests> entry : dataMap.entrySet()) {
            if (entry.getValue().getTheEarliestRecordExist() < unPrintedTime) {
                unPrintedTime = entry.getValue().getTheEarliestRecordExist();
            }
        }
        return unPrintedTime;
    }
}

