package org.example.output;

import org.example.model.CustomResponse;
import org.example.model.JMeterOutput;
import org.example.user.User;

import java.io.FileWriter;
import java.io.IOException;

/*
Write result to csv file
 */
public class OutputTheCsv {
    FileWriter writer;
    //Head Row
    //String headers = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytesSent,bytesReceived,grpThreads,allThreads,URL,Latency,IdleTime,Connect\n";
    String headers = "timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,Latency,IdleTime,Connect\n";

    //Initial the csv file
    public OutputTheCsv(String filePath) throws IOException {
        writer = new FileWriter(filePath);
        try{
            writer.append(headers);
            System.out.println("CSV file created successfully with headers.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void writeToCsv(CustomResponse customResponse){
        JMeterOutput jMeterOutput1 = new JMeterOutput(customResponse.getTimeStamp(),customResponse.getElapsed(),customResponse.getLabel(),customResponse.getResponseCode(),customResponse.getStatusInfo(),
        customResponse.getRequestID(),customResponse.getDataType(),customResponse.isSuccess(),customResponse.getFailureMessage(),customResponse.getBytesSent(),customResponse.getBytesReceived(),
                User.getUsers().size(),User.getUsers().size(),customResponse.getHost(),customResponse.getLatency(),customResponse.getIdleTime(),customResponse.getConnect());

        try{
            writer.append(jMeterOutput1.toString());
            //System.out.println("Added to CSV");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeFile() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
