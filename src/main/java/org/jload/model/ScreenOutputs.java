package org.jload.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
JLOAD screen output which is the grouped requests
 */
public class ScreenOutputs {
    private String label;
    private long totalRequestNum = 0;
    private long totalFailNum = 0;
    private double failRatio = 0;
    private double totalResponseTime = 0;
    private final List<SecondPrint> secondPrints;

    public ScreenOutputs() {
        secondPrints = new ArrayList<>();
    }

    public void addRequest(String timeStamp) {
        totalRequestNum++;
        SecondPrint secondPrint = getSecondPrint(timeStamp);
        secondPrint.requestNum++;
        secondPrint.rps++;
    }

    private SecondPrint getSecondPrint(String timeStamp) {
        for (SecondPrint secondPrint : secondPrints) {
            if (secondPrint.timeStamp.equals(timeStamp)) {
                return secondPrint;
            }
        }
        SecondPrint secondPrint = new SecondPrint();
        secondPrint.timeStamp = timeStamp;
        secondPrints.add(secondPrint);
        return secondPrint;
    }

    public void addResponse(ResponseStat responseStat) {
        SecondPrint secondPrint = getSecondPrint(responseStat.timeStamp());
        if (!responseStat.success()) {
            totalFailNum++;
            secondPrint.failNum++;
        }
        failRatio = ((double) totalFailNum / totalRequestNum) * 100;
        totalResponseTime += responseStat.elapsed();
        secondPrint.totalResponseTimePerSecond += responseStat.elapsed();
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    //Output to screen
    public String toString(String timeStamp) {
        SecondPrint secondPrint = getSecondPrint(timeStamp);
        return String.format("%-60s | %7d | %7d(%6.2f%%) | %7.2f | %8d | %11d | %7.2f",
                label, totalRequestNum, totalFailNum, failRatio,
                totalResponseTime / totalRequestNum, secondPrint.rps, secondPrint.failNum, secondPrint.totalResponseTimePerSecond / secondPrint.requestNum);
    }

    public void add(ScreenOutputs screenOutputs1, String timeStamp) {
        this.totalRequestNum += screenOutputs1.totalRequestNum;
        this.totalFailNum += screenOutputs1.totalFailNum;
        this.totalResponseTime += screenOutputs1.totalResponseTime;
        this.failRatio = ((double) totalFailNum / totalRequestNum) * 100;
        SecondPrint secondPrint1 = screenOutputs1.getSecondPrint(timeStamp);
        SecondPrint secondPrint = this.getSecondPrint(timeStamp);
        secondPrint.requestNum += secondPrint1.requestNum;
        secondPrint.rps += secondPrint1.rps;
        secondPrint.failNum += secondPrint1.failNum;
        secondPrint.totalResponseTimePerSecond += secondPrint1.totalResponseTimePerSecond;
    }

    public long getTotalRequestNum() {
        return totalRequestNum;
    }

    public long getTotalFailNum() {
        return totalFailNum;
    }

    public double getFailRatio() {
        return failRatio;
    }

    public double getTotalResponseTime() {
        return totalResponseTime;
    }

    public void removeSecond(String timeStamp) {
        secondPrints.removeIf(sp -> Objects.equals(sp.timeStamp, timeStamp));
    }

    public class SecondPrint {
        String timeStamp;
        long rps = 0;
        long failNum = 0;
        double totalResponseTimePerSecond = 0;
        long requestNum = 0;
    }
}
