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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ScreenMetricsFilter implements StatisticsFilter {
    private static final Logger logger = LoggerFactory.getLogger(ScreenMetricsFilter.class);
    private static final ConcurrentLinkedQueue<ResponseStat> queue = new ConcurrentLinkedQueue<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<ScreenOutputs> screenOutputs = new ArrayList<>();
    private static final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();
    private static final int DEPLAY_PRINT = 2;

    public ScreenMetricsFilter() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                processQueue();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                printMetrics(getDelayTimeStamp());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private static void processQueue() {
        while (!queue.isEmpty()) {
            ResponseStat responseStat = queue.poll();
            if (responseStat.responseCode().equals("NA")) {
                addRequest(responseStat);
            } else {
                addResponse(responseStat);
            }
            if (responseStat.timeStamp().equals(getTimeStamp())) {
                break;
            }
        }
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
        processQueue();
    }

    public static void addRequest(ResponseStat responseStat) {
        ScreenOutputs screenOutputsTmp = getScreenMetrics(responseStat.timeStamp(), responseStat.label());
        screenOutputsTmp.addRequest(responseStat.timeStamp());
    }

    public static void addResponse(ResponseStat responseStat) {
        ScreenOutputs screenOutputsTmp = getScreenMetrics(responseStat.timeStamp(), responseStat.label());
        screenOutputsTmp.addResponse(responseStat);
    }

    private static ScreenOutputs getScreenMetrics(String timeStamp, String label) {

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
        queue.offer(responseStat);
    }

    private static String getTimeStamp() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentDateTime.format(formatter);
    }

    private static String getDelayTimeStamp() {
        LocalDateTime currentDateTime = LocalDateTime.now().minusSeconds(DEPLAY_PRINT);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentDateTime.format(formatter);
    }

    public static List<ScreenOutputs> getScreenOutputs() {
        return screenOutputs;
    }

    public static void shutdown() {
        executorService.shutdownNow();
    }
}
