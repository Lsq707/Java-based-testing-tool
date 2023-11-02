package org.jload.model;

/*
The output in csv
*/
public class JMeterOutput {
    private String timeStamp;
    private long elapsed;
    private String label;
    private String responseCode;
    private String responseMessage;
    private String threadName;
    private String dataType;
    private boolean success;
    private String failureMessage;
    private long bytesSent;
    private long bytesReceived;
    private int grpThreads;
    private int allThreads;
    private String URL;
    private long latency;
    private long idleTime;
    private long connect;

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }

    public void setBytesSent(long bytesSent) {
        this.bytesSent = bytesSent;
    }

    public void setBytesReceived(long bytesReceived) {
        this.bytesReceived = bytesReceived;
    }

    public void setGrpThreads(int grpThreads) {
        this.grpThreads = grpThreads;
    }

    public void setAllThreads(int allThreads) {
        this.allThreads = allThreads;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public void setLatency(long latency) {
        this.latency = latency;
    }

    public void setIdleTime(long idleTime) {
        this.idleTime = idleTime;
    }

    public void setConnect(long connect) {
        this.connect = connect;
    }

    // Constructor

    public JMeterOutput(String timeStamp, long elapsed, String label, String responseCode, String responseMessage, String threadName, String dataType, boolean success, String failureMessage, long bytesSent, long bytesReceived, int grpThreads, int allThreads, String URL, long latency, long idleTime, long connect) {
        this.timeStamp = timeStamp;
        this.elapsed = elapsed;
        this.label = label;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.threadName = threadName;
        this.dataType = dataType;
        this.success = success;
        this.failureMessage = failureMessage;
        this.bytesSent = bytesSent;
        this.bytesReceived = bytesReceived;
        this.grpThreads = grpThreads;
        this.allThreads = allThreads;
        this.URL = URL;
        this.latency = latency;
        this.idleTime = idleTime;
        this.connect = connect;
    }

    @Override
    public String toString() {
        return  timeStamp + "," +
                elapsed + "," +
                label + "," +
                responseCode + "," +
                responseMessage + "," +
                threadName  + "," +
                dataType  + "," +
                success + "," +
                failureMessage + "," +
                bytesSent + "," +
                bytesReceived + "," +
                grpThreads + "," +
                allThreads + "," +
                //URL + "," +
                latency + "," +
                idleTime + "," +
                connect + "\n";
    }
}

