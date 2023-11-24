package org.jload.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RequestOutput {
    private List<RequestOutput> collections = new ArrayList<>();
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

    // Percentile fields
    private long percentile50;
    private long percentile66;
    private long percentile75;
    private long percentile80;
    private long percentile90;
    private long percentile95;
    private long percentile98;
    private long percentile99;
    private long percentile999;
    private long percentile9999;
    private long percentile100;
    private String timeStamp;

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(int requestCount) {
        this.requestCount = requestCount;
    }

    public int getFailureCount() {
        return failureCount;
    }

    public void setFailureCount(int failureCount) {
        this.failureCount = failureCount;
    }

    public long getMedianResponseTime() {
        return medianResponseTime;
    }

    public void setMedianResponseTime(long medianResponseTime) {
        this.medianResponseTime = medianResponseTime;
    }

    public double getAverageResponseTime() {
        return averageResponseTime;
    }

    public void setAverageResponseTime(double averageResponseTime) {
        this.averageResponseTime = averageResponseTime;
    }

    public long getMinResponseTime() {
        return minResponseTime;
    }

    public void setMinResponseTime(long minResponseTime) {
        this.minResponseTime = minResponseTime;
    }

    public long getMaxResponseTime() {
        return maxResponseTime;
    }

    public void setMaxResponseTime(long maxResponseTime) {
        this.maxResponseTime = maxResponseTime;
    }

    public double getAverageContentSize() {
        return averageContentSize;
    }

    public void setAverageContentSize(double averageContentSize) {
        this.averageContentSize = averageContentSize;
    }

    public double getRequestsPerSecond() {
        return requestsPerSecond;
    }

    public void setRequestsPerSecond(double requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
    }

    public double getFailuresPerSecond() {
        return failuresPerSecond;
    }

    public void setFailuresPerSecond(double failuresPerSecond) {
        this.failuresPerSecond = failuresPerSecond;
    }

    public long getPercentile50() {
        return percentile50;
    }

    public void setPercentile50(long percentile50) {
        this.percentile50 = percentile50;
    }

    public long getPercentile66() {
        return percentile66;
    }

    public void setPercentile66(long percentile66) {
        this.percentile66 = percentile66;
    }

    public long getPercentile75() {
        return percentile75;
    }

    public void setPercentile75(long percentile75) {
        this.percentile75 = percentile75;
    }

    public long getPercentile80() {
        return percentile80;
    }

    public void setPercentile80(long percentile80) {
        this.percentile80 = percentile80;
    }

    public long getPercentile90() {
        return percentile90;
    }

    public void setPercentile90(long percentile90) {
        this.percentile90 = percentile90;
    }

    public long getPercentile95() {
        return percentile95;
    }

    public void setPercentile95(long percentile95) {
        this.percentile95 = percentile95;
    }

    public long getPercentile98() {
        return percentile98;
    }

    public void setPercentile98(long percentile98) {
        this.percentile98 = percentile98;
    }

    public long getPercentile99() {
        return percentile99;
    }

    public void setPercentile99(long percentile99) {
        this.percentile99 = percentile99;
    }

    public long getPercentile999() {
        return percentile999;
    }

    public void setPercentile999(long percentile999) {
        this.percentile999 = percentile999;
    }

    public long getPercentile9999() {
        return percentile9999;
    }

    public void setPercentile9999(long percentile9999) {
        this.percentile9999 = percentile9999;
    }

    public long getPercentile100() {
        return percentile100;
    }

    public void setPercentile100(long percentile100) {
        this.percentile100 = percentile100;
    }

    @Override
    public String toString() {
        return type + "," +
                name + "," +
                requestCount + "," +
                failureCount + "," +
                medianResponseTime + "," +
                averageResponseTime + "," +
                minResponseTime + "," +
                maxResponseTime + "," +
                averageContentSize + "," +
                requestsPerSecond + "," +
                failuresPerSecond + "," +
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
    }

    private void addRequestCount(){
        requestCount += 1;
    }

    private void addFailCount(){
        failureCount += 1;
        addRequestCount();
    }

    private void updateResponseTime(long newTime){
        int beforeSize = requestCount - failureCount;
        int currentSize = beforeSize + 1;
        if(newTime > maxResponseTime)
            maxResponseTime = newTime;
        if(newTime < minResponseTime)
            minResponseTime = newTime;
        averageResponseTime = (averageResponseTime*beforeSize + newTime) / currentSize;

        sortCollectionsByAverageResponseTime();
        if (currentSize % 2 == 0) {
            medianResponseTime = (long) ((collections.get(currentSize / 2 - 1).getAverageResponseTime() + collections.get(currentSize / 2).getAverageResponseTime()) / 2);
        } else {
            medianResponseTime = (long) collections.get(currentSize / 2).getAverageResponseTime();
        }
    }

    private void sortCollectionsByAverageResponseTime() {
        Collections.sort(collections, new Comparator<RequestOutput>() {
            @Override
            public int compare(RequestOutput o1, RequestOutput o2) {
                return Double.compare(o1.getAverageResponseTime(), o2.getAverageResponseTime());
            }
        });
    }

    private void updateAvgContentSize(long newSize){
        averageContentSize = (averageContentSize*(requestCount - failureCount) + newSize) / (requestCount - failureCount + 1);
    }
    private void updatePerSeconds(){
        LocalDateTime minTimeStamp = LocalDateTime.MAX;
        LocalDateTime maxTimeStamp = LocalDateTime.MIN;

        for (RequestOutput output : collections) {
            LocalDateTime current = parseTimeStamp(output.getTimeStamp());
            if (current.isAfter(maxTimeStamp)) {
                maxTimeStamp = current;
            }
            if (current.isBefore(minTimeStamp)) {
                minTimeStamp = current;
            }
        }

        long durationSeconds = Duration.between(minTimeStamp, maxTimeStamp).getSeconds();
        requestsPerSecond = (double) requestCount / durationSeconds;
        failuresPerSecond = (double) failureCount / durationSeconds;
    }
    private LocalDateTime parseTimeStamp(String timeStamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(timeStamp, formatter);
    }
    public synchronized void update(RequestOutput requestOutput){
        collections.add(requestOutput);
        if(requestOutput.getFailureCount() == 0){
            updateResponseTime((long) requestOutput.getAverageResponseTime());
            updateAvgContentSize((long) requestOutput.getAverageContentSize());
            addRequestCount();
            updatePerSeconds();
        }
        else
            addFailCount();
    }
}
