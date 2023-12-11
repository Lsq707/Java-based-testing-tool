package org.jload.output;

import org.jload.exceptions.RatioException;
import org.jload.model.groupedRequests;
import org.jload.model.PerSecondStat;
import org.jload.model.ResponseStat;
import org.jload.response.StatisticsFilter;
import org.jload.runner.Env;
import org.jload.runner.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CheckRatioFilter implements StatisticsFilter {
    private static final int DELAY_SECONDS = 2;
    private static final Logger logger = LoggerFactory.getLogger(CheckRatioFilter.class);
    private static ScheduledExecutorService scheduledCheckService;
    private static ScheduledFuture<?> checkingFuture;
    private static final ConcurrentHashMap<String, groupedRequests> dataMap = new ConcurrentHashMap<>();

    public CheckRatioFilter() {
        checkMetrics();
    }

    @Override
    public void process(ResponseStat responseStat) {
        groupedRequests groupedRequests = addOrUpdate(responseStat.label());
        groupedRequests.addSecondStat(responseStat);
    }

    public groupedRequests addOrUpdate(String key) {
        return dataMap.computeIfAbsent(key, k -> new groupedRequests(key));
    }

    private void checkMetrics() {
        scheduledCheckService = Executors.newScheduledThreadPool(1);
        checkingFuture = scheduledCheckService.scheduleWithFixedDelay(() -> {
            if (!dataMap.isEmpty()) {
                long previousSeconds = System.currentTimeMillis() / 1000 - DELAY_SECONDS;
                printHead();
                for (Map.Entry<String, groupedRequests> entry : dataMap.entrySet()) {
                    if (entry.getValue().getTimeStat(previousSeconds) != null) {
                        printInfo(previousSeconds, entry.getValue());
                    }
                }
                System.out.println();
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    private static void printHead() {
        System.out.printf("%-60s | %7s | %-16s | %-7s | %-8s | %-11s | %-7s\n",
                "Label", "#reqs", "#fails", "Avg", "req/s", "failure/s", "avg/s");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
    }

    public static ScheduledExecutorService getScheduledCheckService() {
        return scheduledCheckService;
    }

    public static ScheduledFuture<?> getCheckingFuture() {
        return checkingFuture;
    }

    public static void printInfo(long previousSeconds, groupedRequests groupedRequests) {
        PerSecondStat perSecondStat = groupedRequests.getTimeStat(previousSeconds);
        System.out.printf("%-60s | %7d | %7d(%3.2f%%) | %7.2f | %8d | %11d | %7.2f\n",
                groupedRequests.getLabel(), groupedRequests.getTotalRequestNum(), groupedRequests.getTotalFailNum(), groupedRequests.getFailRatio(),
                groupedRequests.getAvgResponseTime(), perSecondStat.getRps(), perSecondStat.getFailNum(), perSecondStat.getAvgResponseTime());
        try {
            if (Env.checkFailRatio > 0) {
                if (groupedRequests.getFailRatio() > Env.checkFailRatio) {
                    throw new RatioException("Test Failed due to failure ratio > " + Env.checkFailRatio);
                }
            }
            if (Env.checkAvgResponseTime > 0) {
                if (groupedRequests.getAvgResponseTime() > Env.checkAvgResponseTime) {
                    throw new RatioException("Test Failed due to average response time > " + Env.checkAvgResponseTime);
                }
            }
        } catch (RatioException e) {
            logger.error(e.getMessage(), e);
            //Shut down
            Runner.setTestFlag(false);
        }
        groupedRequests.removeTimeStat(previousSeconds);
    }

    public static void printAll() {
        for (int i = 0; i < DELAY_SECONDS; i++) {
            printHead();
            for (Map.Entry<String, groupedRequests> entry : dataMap.entrySet()) {
                if (!entry.getValue().getPerSecondStats().isEmpty()) {
                    printInfo(entry.getValue().getPerSecondStats().getFirst().getSecond(), entry.getValue());
                }
            }
            System.out.println();
        }
    }
}

