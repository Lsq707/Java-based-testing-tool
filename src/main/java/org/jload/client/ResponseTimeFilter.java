package org.jload.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import org.jload.model.ResponseStat;
import org.jload.response.Statistics;
import org.jload.response.ByteProcess;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


/*
Client filter to compute the return type
*/
public class ResponseTimeFilter implements ClientResponseFilter {
    private static final Logger logger = LoggerFactory.getLogger(ResponseTimeFilter.class);
    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        long startTime = (Long) requestContext.getProperty("startTime");
        String timeStamp = (String) requestContext.getProperty("timeStamp");

        long endTime = System.currentTimeMillis();
        long responseTime = calculateResponseTime(startTime, endTime);
        //logger.info("Response time: " + responseTime + " milliseconds");

        String label = "Http Request " + requestContext.getMethod();

        String host = requestContext.getUri().toString();

        int status = responseContext.getStatus();
        boolean success = isSuccessful(status);

        String responseMsg = retrieveResponseMessage(responseContext);

        ByteProcess byteProcess = new ByteProcess();
        long bytesSent = byteProcess.getByteCount();
        int bytesReceived = (responseMsg != null) ? responseMsg.getBytes().length : 0;

        String failureMsg = (!success) ? getFailureMsg(responseMsg) : null;

        int responseCode = responseContext.getStatus();
        String statusInfo = responseContext.getStatusInfo().toString();
        String dataType = requestContext.getHeaderString("Content-Type");

        //logger.info(responseContext.toString());

        Statistics.addStatistic(new ResponseStat(timeStamp, responseTime, label, String.valueOf(responseCode), statusInfo, responseMsg, dataType, success, failureMsg, bytesSent, bytesReceived, host));
        //writeToCSV(timeStamp, responseTime, label, String.valueOf(responseCode), statusInfo, responseMsg, dataType, success, failureMsg, bytesSent, bytesReceived, host);
    }

    private long calculateResponseTime(long startTime, long endTime) {
        return endTime - startTime;
    }

    private boolean isSuccessful(int status) {
        return status >= 200 && status < 300;
    }

    private String retrieveResponseMessage(ClientResponseContext responseContext) {
        if (responseContext.hasEntity()) {
            InputStream entityStream = responseContext.getEntityStream();
            if (entityStream != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(entityStream))) {
                    StringBuilder stringBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    return stringBuilder.toString();
                } catch (IOException e) {
                    logger.error("Error getClass {}: {}", this.getClass().getName(), e.getMessage(), e);
                }
            }
        }
        return null;
    }

    /*
    Get the error msg from server
     */
    private String getFailureMsg(String responseMsg){
        String fMsg = null;
        if(responseMsg != null){
            JSONObject jsonResponse = new JSONObject(responseMsg); // Parsing the JSON
            fMsg = jsonResponse.optString("detail");
        }
        return fMsg;
    }
}


