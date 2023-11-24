package org.jload.model;

/*
Response statistic
 */
public record ResponseStat(
        String timeStamp,
        long elapsed,
        String label,
        String responseCode,
        String statusInfo,
        String responseMessage,
        String requestID,
        String dataType,
        boolean success,
        String failureMessage,
        long bytesSent,
        long bytesReceived,
        int grpThreads,
        int allThreads,
        String host,
        long latency,
        long idleTime,
        long connect,
        String rewrittenPath
) {
    //Customized constructor must back to origin generated one in record
    public ResponseStat(String timeStamp, long elapsed, String label, String responseCode, String statusInfo, String responseMessage, String dataType, boolean success, String failureMessage, long bytesSent, long bytesReceived, String host, String rewrittenPath) {
        this(timeStamp, elapsed, label, responseCode, statusInfo, responseMessage, null, dataType, success, failureMessage, bytesSent, bytesReceived, 0, 0, host, 0, 0, 0,rewrittenPath);
    }
}

