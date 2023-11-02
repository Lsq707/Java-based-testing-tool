package org.example.model;

/*
Customized Response
 */
public class CustomResponse {
    private final String timeStamp;
    private final long elapsed;
    private final String label;
    private final String responseCode;
    private final String responseMessage;
    private String requestID;
    private final String dataType;
    private final boolean success;
    private final String failureMessage;
    private final long bytesSent;
    private final long bytesReceived;
    private int grpThreads;
    private int allThreads;
    private final String host;
    private final long latency;
    private final long idleTime;
    private final long connect;
    private final String statusInfo;

    public CustomResponse(String timeStamp, long elapsed, String label, String responseCode,String statusInfo, String responseMessage, String dataType, boolean success, String failureMessage, long bytesSent, long bytesReceived, String host, long connect) {
        this.timeStamp = timeStamp;
        this.elapsed = elapsed;
        this.label = label;
        this.responseCode = responseCode;
        this.statusInfo = statusInfo;
        this.responseMessage = responseMessage;
        this.dataType = dataType;
        this.success = success;
        this.failureMessage = failureMessage;
        this.bytesSent = bytesSent;
        this.bytesReceived = bytesReceived;
        this.host = host;
        this.latency = 0;
        this.idleTime = 0;
        this.connect = connect;
    }

    public CustomResponse(String timeStamp, long elapsed, String label, String responseCode, String statusInfo, String responseMessage, String requestID, String dataType, boolean success, String failureMessage, long bytesSent, long bytesReceived, int grpThreads, int allThreads, String host, long latency, long idleTime, long connect) {
        this.timeStamp = timeStamp;
        this.elapsed = elapsed;
        this.label = label;
        this.responseCode = responseCode;
        this.statusInfo = statusInfo;
        this.responseMessage = responseMessage;
        this.requestID = requestID;
        this.dataType = dataType;
        this.success = success;
        this.failureMessage = failureMessage;
        this.bytesSent = bytesSent;
        this.bytesReceived = bytesReceived;
        this.grpThreads = grpThreads;
        this.allThreads = allThreads;
        this.host = host;
        this.latency = latency;
        this.idleTime = idleTime;
        this.connect = connect;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public void setGrpThreads(int grpThreads) {
        this.grpThreads = grpThreads;
    }

    public void setAllThreads(int allThreads) {
        this.allThreads = allThreads;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public long getElapsed() {
        return elapsed;
    }

    public String getLabel() {
        return label;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public String getRequestID() {
        return requestID;
    }

    public String getDataType() {
        return dataType;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getStatusInfo() {
        return statusInfo;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public long getBytesSent() {
        return bytesSent;
    }

    public long getBytesReceived() {
        return bytesReceived;
    }

    public int getGrpThreads() {
        return grpThreads;
    }

    public int getAllThreads() {
        return allThreads;
    }

    public String getHost() {
        return host;
    }

    public long getLatency() {
        return latency;
    }

    public long getIdleTime() {
        return idleTime;
    }

    public long getConnect() {
        return connect;
    }

    @Override
    public String toString() {
        return "CustomResponse{" +
                "timeStamp=" + timeStamp +
                ", elapsed=" + elapsed +
                ", label='" + label + '\'' +
                ", responseCode='" + responseCode + '\'' +
                ", responseMessage='" + responseMessage + '\'' +
                ", requestID='" + requestID + '\'' +
                ", dataType='" + dataType + '\'' +
                ", success=" + success +
                ", failureMessage='" + failureMessage + '\'' +
                ", bytesSent=" + bytesSent +
                ", bytesReceived=" + bytesReceived +
                ", grpThreads=" + grpThreads +
                ", allThreads=" + allThreads +
                ", host='" + host + '\'' +
                ", latency=" + latency +
                ", idleTime=" + idleTime +
                ", connect=" + connect +
                '}';
    }
}

