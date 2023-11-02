package org.jload.model;

/*
The output in csv
*/
public record JMeterOutput(
        String timeStamp,
        long elapsed,
        String label,
        String responseCode,
        String responseMessage,
        String threadName,
        String dataType,
        boolean success,
        String failureMessage,
        long bytesSent,
        long bytesReceived,
        int grpThreads,
        int allThreads,
        String URL,
        long latency,
        long idleTime,
        long connect
) {

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
                // URL + "," +
                latency + "," +
                idleTime + "," +
                connect + "\n";
    }
}

