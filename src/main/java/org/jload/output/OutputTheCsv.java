package org.jload.output;

import org.jload.model.CustomResponse;
import org.jload.model.JMeterOutput;
import org.jload.user.User;

import java.io.FileWriter;
import java.io.IOException;

/*
Write result to csv file
 */
public class OutputTheCsv {
    // Head Row
    private static final String HEADERS = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,Latency,IdleTime,Connect\n";
    private static FileWriter writer;

    public static FileWriter getWriter(){
        return writer;
    }

    public static void createFile(String filePath) throws IOException {
        writer = new FileWriter(filePath);
        try {
            writer.append(HEADERS);
            System.out.println("CSV file created successfully with headers.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void writeToCsv(CustomResponse customResponse) {
        JMeterOutput jMeterOutput = new JMeterOutput(customResponse.getTimeStamp(), customResponse.getElapsed(),
                customResponse.getLabel(), customResponse.getResponseCode(), customResponse.getStatusInfo(),
                customResponse.getRequestID(), customResponse.getDataType(), customResponse.isSuccess(),
                customResponse.getFailureMessage(), customResponse.getBytesSent(), customResponse.getBytesReceived(),
                0, 0, customResponse.getHost(), customResponse.getLatency(),
                customResponse.getIdleTime(), customResponse.getConnect());

        try {
            if (writer != null) {
                writer.append(jMeterOutput.toString());
            } else {
                System.err.println("CSV writer not initialized. Please create the file first.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void closeFile() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

