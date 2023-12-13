package org.jload.model;

import org.jload.runner.Env;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class groupedRequests {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final String label;
    private long totalRequestNum;
    private long totalFailNum;
    private long totalRequestTime;
    private long totalFailTime;

    //Data seconds
    private final List<PerSecondStat> perSecondStats;

    public groupedRequests(String label) {
        this.label = label;
        perSecondStats = new CopyOnWriteArrayList<>();
    }

    public void addRequestNum() {
        totalRequestNum++;
    }

    public void addFailNum() {
        totalFailNum++;
    }

    public void addRequestTime(long time) {
        totalRequestTime += time;
    }

    public void addFailTIme(long time) {
        totalFailTime += time;
    }

    public void addSecondStat(ResponseStat responseStat) {
        long time = responseStat.elapsed();
        addRequestTime(time);
        addRequestNum();
        long currentSeconds = convertToSeconds(responseStat.timeStamp());
        PerSecondStat perSecondStat = getOrCreateStatWithLabel(currentSeconds);
        perSecondStat.addRequest();
        perSecondStat.addResponseTime(time);
        if (!responseStat.success()) {
            addFailNum();
            addFailTIme(time);
            perSecondStat.addFailTime(time);
            perSecondStat.addFailNum();
        }
    }

    public PerSecondStat getOrCreateStatWithLabel(long second) {
        for (PerSecondStat stat : perSecondStats) {
            if (stat.getSecond() == second) {
                return stat;
            }
        }
        PerSecondStat newStat = new PerSecondStat(second);
        perSecondStats.add(newStat);
        return newStat;
    }

    private long convertToSeconds(String timeStamp) {
        LocalDateTime localDateTime = LocalDateTime.parse(timeStamp, FORMATTER);
        // Assuming the system's default time zone.
        return localDateTime.atZone(ZoneId.systemDefault()).toEpochSecond();
    }

    public PerSecondStat getTimeStat(long time) {
        for (PerSecondStat stat : perSecondStats) {
            if (stat.getSecond() == time) {
                return stat;
            }
        }
        return null;
    }

    public String getLabel() {
        return label;
    }

    public long getTotalRequestNum() {
        return totalRequestNum;
    }

    public long getTotalFailNum() {
        return totalFailNum;
    }

    public double getAvgResponseTime() {
        if (totalRequestNum == 0) {
            return 0;
        }
        return (double) totalRequestTime / totalRequestNum;
    }

    public double getFailRatio() {
        return ((double) totalFailNum / totalRequestNum) * 100;
    }

    public void removeTimeStat(long time) {
        perSecondStats.removeIf(stat -> stat.getSecond() == time);
    }

    public List<PerSecondStat> getPerSecondStats() {
        return perSecondStats;
    }

    public long getEarliestTime(){
        return perSecondStats.get(0).getSecond();
    }
}
