package org.jload.model;

import org.jload.runner.Env;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class RequestOutput {
    private PriorityQueue<ResponseStat> requestCollections = new PriorityQueue<>(Comparator.comparingDouble(ResponseStat::elapsed));
    private String type;
    private String name;
    private int requestCount;
    private int failureCount;
    private long medianResponseTime;
    private double averageResponseTime;
    private long minResponseTime;
    private long maxResponseTime;
    private double averageContentSize;
    private double requestsPerSecond;
    private double failuresPerSecond;
    private LocalDateTime minTimeStamp;
    private LocalDateTime maxTimeStamp;

    private void updateResponseTime(long newTime) {
        if (newTime > maxResponseTime) {
            maxResponseTime = newTime;
        }
        if (newTime < minResponseTime) {
            minResponseTime = newTime;
        }
        averageResponseTime = ((averageResponseTime * requestCount) + newTime) / (requestCount + 1);
    }

    private void updateAvgContentSize(long newSize) {
        averageContentSize = ((averageContentSize * requestCount) + newSize) / (requestCount + 1);
    }

    private void updatePerSeconds() {
        long durationSeconds = Duration.between(minTimeStamp, maxTimeStamp).getSeconds();
        requestsPerSecond = (double) requestCount / durationSeconds;
        failuresPerSecond = (double) failureCount / durationSeconds;
    }

    private LocalDateTime parseTimeStamp(String timeStamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(timeStamp, formatter);
    }

    public synchronized void update(ResponseStat responseStat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime current = LocalDateTime.parse(responseStat.timeStamp(), formatter);
        ;
        if (current.isAfter(maxTimeStamp)) {
            maxTimeStamp = current;
        }
        if (current.isBefore(minTimeStamp)) {
            minTimeStamp = current;
        }

        requestCollections.add(responseStat);
        if (responseStat.success()) {
            updateResponseTime((long) responseStat.elapsed());
            updateAvgContentSize((long) responseStat.bytesReceived());
            requestCount++;
            updatePerSeconds();
        } else {
            failureCount++;
            requestCount++;
        }

        // Update media
        if (requestCount % 2 == 0) {
            ResponseStat middle1 = requestCollections.stream().skip((requestCount / 2) - 1).findFirst().orElse(null);
            ResponseStat middle2 = requestCollections.stream().skip(requestCount / 2).findFirst().orElse(null);
            if (middle1 != null && middle2 != null) {
                medianResponseTime = (long) ((middle1.elapsed() + middle2.elapsed()) / 2);
            }
        } else {
            ResponseStat middle = requestCollections.stream().skip(requestCount / 2).findFirst().orElse(null);
            if (middle != null) {
                medianResponseTime = (long) middle.elapsed();
            }
        }
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public long getMedianResponseTime() {
        return medianResponseTime;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public long getMinResponseTime() {
        return minResponseTime;
    }

    public long getMaxResponseTime() {
        return maxResponseTime;
    }

    public double getAverageContentSize() {
        return averageContentSize;
    }

    public double getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public double getFailuresPerSecond() {
        return failuresPerSecond;
    }

    public LocalDateTime getMinTimeStamp() {
        return minTimeStamp;
    }

    public LocalDateTime getMaxTimeStamp() {
        return maxTimeStamp;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public void setMedianResponseTime(long medianResponseTime) {
        this.medianResponseTime = medianResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public void setMinResponseTime(long minResponseTime) {
        this.minResponseTime = minResponseTime;
    }

    public void setMaxResponseTime(long maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
    }

    public void setAverageContentSize(double averageContentSize) {
        this.averageContentSize = averageContentSize;
    }

    public void setRequestsPerSecond(double requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
    }

    public void setFailuresPerSecond(double failuresPerSecond) {
        this.failuresPerSecond = failuresPerSecond;
    }

    public void setMinTimeStamp(LocalDateTime minTimeStamp) {
        this.minTimeStamp = minTimeStamp;
    }

    public void setMaxTimeStamp(LocalDateTime maxTimeStamp) {
        this.maxTimeStamp = maxTimeStamp;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return type + "," +
                name + "," +
                requestCount + "," +
                failureCount + "," +
                medianResponseTime + "," +
                Env.df.format(averageResponseTime) + "," +
                minResponseTime + "," +
                maxResponseTime + "," +
                Env.df.format(averageContentSize) + "," +
                Env.df.format(requestsPerSecond) + "," +
                Env.df.format(failuresPerSecond) + "," +
                0 + "," +
                0 + "," +
                0 + "," +
                0 + "," +
                0 + "," +
                0 + "," +
                0 + "," +
                0 + "," +
                0 + "," +
                0 + "," +
                0 + "\n";
                /*
                percentile50 + "," +
                percentile66 + "," +
                percentile75 + "," +
                percentile80 + "," +
                percentile90 + "," +
                percentile95 + "," +
                percentile98 + "," +
                percentile99 + "," +
                percentile999 + "," +
                percentile9999 + "," +
                percentile100 + "\n";
                 */
    }
}
