package org.jload.output;

import org.jload.model.ResponseStat;
import org.jload.model.ScreenOutputs;
import org.jload.response.StatisticsFilter;
import org.jload.runner.Runner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScreenMetricsFilter implements StatisticsFilter {
    private static final Logger logger = LoggerFactory.getLogger(ScreenMetricsFilter.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<ScreenOutputs> screenOutputs = new ArrayList<>();
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
    private static final int DELAY_PRINT = 1;
    private static String previousPrintTime;

    public ScreenMetricsFilter() {
        scheduledExecutorService.scheduleWithFixedDelay(() -> {
            try {
                previousPrintTime = getDelayTimeStamp(DELAY_PRINT);
                printMetrics(previousPrintTime);
            } catch (Exception e) {
                logger.error("Error occurred in scheduled task", e);
            }
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void printHead() {
        System.out.printf("%-60s | %7s | %-16s | %-7s | %-8s | %-11s | %-7s\n",
                "Label", "#reqs", "#fails", "Avg", "req/s", "failure/s", "avg/s");
        System.out.println("-------------------------------------------------------------------------------------------------------------------------------------");
    }

    public static void printMetrics(String timeStamp) {
        System.out.println();
        System.out.println(timeStamp);
        printHead();
        ScreenOutputs total = new ScreenOutputs();
        total.setLabel("--------TOTAL--------");
        for (ScreenOutputs screenOutputs1 : screenOutputs) {
            total.add(screenOutputs1, timeStamp);
            System.out.println(screenOutputs1.toString(timeStamp));
            screenOutputs1.removeSecond(timeStamp);
        }
        System.out.println(total.toString(timeStamp));
        Runner.printOutActiveUsr();
    }

    public static void printMetrics() {
        List<String> timestamps = getTimestampsSince();
        for (String time : timestamps) {
            printMetrics(time);
        }
    }

    public static void addRequest(ResponseStat responseStat) {
        ScreenOutputs screenOutputsTmp = getScreenMetrics(responseStat.label());
        screenOutputsTmp.addRequest(responseStat.timeStamp());
    }

    public static void addResponse(ResponseStat responseStat) {
        ScreenOutputs screenOutputsTmp = getScreenMetrics(responseStat.label());
        screenOutputsTmp.addResponse(responseStat);
    }

    private static ScreenOutputs getScreenMetrics(String label) {

        for (ScreenOutputs screenOutputs1 : screenOutputs) {
            if (screenOutputs1.getLabel().equals(label)) {
                return screenOutputs1;
            }
        }

        ScreenOutputs screenOutputs1 = new ScreenOutputs();
        screenOutputs1.setLabel(label);
        screenOutputs.add(screenOutputs1);
        return screenOutputs1;
    }

    @Override
    public void process(ResponseStat responseStat) {
        //queue.offer(responseStat);
        if (responseStat.responseCode().equals("NA")) {
            addRequest(responseStat);
        } else {
            addResponse(responseStat);
        }
    }

    private static String getTimeStamp() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentDateTime.format(formatter);
    }

    private static String getDelayTimeStamp(int delayTime) {
        LocalDateTime currentDateTime = LocalDateTime.now().minusSeconds(delayTime);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentDateTime.format(formatter);
    }

    public static List<ScreenOutputs> getScreenOutputs() {
        return screenOutputs;
    }

    private static List<String> getTimestampsSince() {
        List<String> timestamps = new ArrayList<>();
        LocalDateTime startTime = LocalDateTime.parse(previousPrintTime, FORMATTER).plusSeconds(1);
        ;
        LocalDateTime currentTime = LocalDateTime.now();

        while (!startTime.isAfter(currentTime)) {
            timestamps.add(startTime.format(FORMATTER));
            startTime = startTime.plusSeconds(1);
        }
        return timestamps;
    }

    public static ScheduledExecutorService getScheduledExecutor() {
        return scheduledExecutorService;
    }

    public static void shutdown() {
        scheduledExecutorService.shutdownNow();
    }
}
