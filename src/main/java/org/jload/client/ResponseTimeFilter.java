package org.jload.client;

import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.client.ClientResponseContext;
import jakarta.ws.rs.client.ClientResponseFilter;
import org.jload.model.ResponseStat;
import org.jload.response.Statistics;
import org.jload.response.ByteProcess;
import org.jload.runner.Runner;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
Client filter to compute the return type
*/
public class ResponseTimeFilter implements ClientResponseFilter, ClientRequestFilter {
    /*
    To store and get the error requests
     */
    private static final Logger logger = LoggerFactory.getLogger(ResponseTimeFilter.class);
    public static ThreadLocal<String> url = new ThreadLocal<>();
    public static ThreadLocal<String> method = new ThreadLocal<>();
    public static ThreadLocal<Long> startTime = new ThreadLocal<>();
    public static ThreadLocal<Long> bytesSent = new ThreadLocal<>();
    public static ThreadLocal<String> timeStamp = new ThreadLocal<>();
    public static ThreadLocal<String> host = new ThreadLocal<>();
    public static ThreadLocal<String> label = new ThreadLocal<>();
    public static ThreadLocal<String> dataType = new ThreadLocal<>();
    public static ThreadLocal<String> rewrittenPath = new ThreadLocal<>();

    @Override
    public void filter(ClientRequestContext requestContext) {
        timeStamp.set((String) requestContext.getProperty("timeStamp"));
        startTime.set(System.currentTimeMillis());
        ByteProcess byteProcess = new ByteProcess();
        bytesSent.set(byteProcess.getByteCount());
        rewrittenPath.set(null);
        if (requestContext.getProperty("rewritten") != null) {
            rewrittenPath.set((String) requestContext.getProperty("rewritten"));
        }
        host.set(requestContext.getUri().toString());
        dataType.set(requestContext.getHeaderString("Content-Type"));
        label.set((rewrittenPath.get() == null ? host.get() : rewrittenPath.get()) + " " + requestContext.getMethod());
        url.set(requestContext.getUri().toString());
        method.set(requestContext.getMethod());
        ResponseStat responseStat = new ResponseStat(timeStamp.get(), 0, label.get(), "NA", "NA", "NA", "NA", false, "NA", 0, 0, host.get(), rewrittenPath.get());
        Statistics.addStatistic(responseStat);
        //ScreenMetrics.addRequest(requestContext);
    }

    @Override
    public void filter(ClientRequestContext requestContext, ClientResponseContext responseContext) {
        String rewrittenPath = null;
        if (requestContext.getProperty("rewritten") != null) {
            rewrittenPath = (String) requestContext.getProperty("rewritten");
        }
        long startTime = (Long) requestContext.getProperty("startTime");
        String timeStamp = (String) requestContext.getProperty("timeStamp");

        long endTime = System.currentTimeMillis();
        long responseTime = calculateResponseTime(startTime, endTime);

        String host = requestContext.getUri().toString();
        String label = (rewrittenPath == null ? host : rewrittenPath) + " " + requestContext.getMethod();

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
        String threadInfo = Thread.currentThread().toString();
        String workerName = threadInfo.split("@")[1];
        //String workerName = "test";
        Runner.addPlatformThread(workerName);
        ResponseStat responseStat = new ResponseStat(timeStamp, responseTime, label, String.valueOf(responseCode), statusInfo, responseMsg, dataType, success, failureMsg, bytesSent, bytesReceived, host, rewrittenPath);
        //ScreenMetrics.addResponse(responseStat);
        Statistics.addStatistic(responseStat);
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
    private String getFailureMsg(String responseMsg) {
        String fMsg = null;
        if (responseMsg != null) {
            JSONObject jsonResponse = new JSONObject(responseMsg); // Parsing the JSON
            fMsg = jsonResponse.optString("detail");
        }
        return fMsg;
    }
}


