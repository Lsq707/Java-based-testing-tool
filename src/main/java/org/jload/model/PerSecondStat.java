package org.jload.model;

public class PerSecondStat {
    long second;
    long requestNum;
    long requestTime; //MillionSeconds
    long failNum;
    long failTime;

    public PerSecondStat(long second) {
        this.second = second;
    }

    public void addRequest() {
        requestNum++;
    }

    public void addFailNum() {
        failNum++;
    }

    public void addResponseTime(long time) {
        requestTime += time;
    }

    public void addFailTime(long time) {
        failTime += time;
    }

    public double getAvgResponseTime() {
        if (requestNum == 0) {
            return 0;
        }
        return (double) requestTime / requestNum;
    }

    public double getFailRatio() {
        return (double) failNum / requestNum;
    }

    public long getRps() {
        return requestNum;
    }

    public long getFailNum() {
        return failNum;
    }

    public long getSecond() {
        return second;
    }
}
