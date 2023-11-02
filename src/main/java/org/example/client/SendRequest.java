package org.example.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.model.CustomResponse;
import org.example.response.ByteProcess;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
This class meant to send requests
And timing the connectTime
*/
public class SendRequest {
    private long allStartTime;
    private long allEndTime;
    private long bytesSent;
    private long requestStartTime;
    private long requestEndTime;
    private long connectStartTime ;
    private long connectEndTime = 0;
    private boolean success = false;
    private long responseTime = 0;
    private long elapsed = 0;
    private long connectTime = 0;
    private long bytesReceived = 0;
    private Client client;
    private String getTimeStamp(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        return currentDateTime.format(formatter);
    }

    //Set the client to send request
    public SendRequest(Client usrClient){
        this.client = usrClient;
    }

    //Get Method
    public CustomResponse getResponse(String baseUrl, String subUrl) {
        String label = "Https Request GET";
        String failureMsg ="";
        ByteProcess byteProcess = new ByteProcess();
        String timeStamp = getTimeStamp();
        allStartTime = System.currentTimeMillis();
        WebTarget target = client.target(baseUrl).path(subUrl);
        //Send get request
        try {
            // Prepare the request
            requestStartTime = System.currentTimeMillis();
            Invocation.Builder builder = target.request();
            // Process the response
            connectStartTime = System.currentTimeMillis();
            Response response = builder.get();
            requestEndTime = System.currentTimeMillis();

            String responseCode = String.valueOf(response.getStatus());
            String statusinfo = String.valueOf(response.getStatusInfo());
            success = (response.getStatus() >= 200 && response.getStatus() < 300);
            String responseMessage = response.readEntity(String.class);
            if(!success){
                failureMsg = responseMessage;
            }

            //String responseStatus = response.getStatus();

            String dataType = response.getHeaderString("Content-Type");
            long bytesReceived = responseMessage.getBytes().length;

            // Get the number of bytes sent
            bytesSent = byteProcess.getByteCount();

            allEndTime = System.currentTimeMillis();
            //release resources
            response.close();
            responseTime = requestEndTime - requestStartTime;
            elapsed = allEndTime - allStartTime;
            connectTime = requestEndTime - connectStartTime;

            return new CustomResponse(timeStamp,elapsed,label,responseCode,statusinfo,responseMessage,dataType,success,failureMsg,bytesSent,bytesReceived,baseUrl+subUrl,connectTime);
        } catch (Exception e) {
            System.out.println("UsrClient-" + "getRequest(): " + e.getMessage());
        }
        return null;
    }
    //GetMethod withBody

    //Post Method
    public CustomResponse postRequest(String baseUrl, String subUrl, String requestBody) {
        String label = "Https Request POST";
        String failureMsg ="";
        ByteProcess byteProcess = new ByteProcess();
        allStartTime = System.currentTimeMillis();
        String timeStamp = getTimeStamp();
        WebTarget target = client.target(baseUrl).path(subUrl);
        try {
            // Prepare the request
            requestStartTime = System.currentTimeMillis();
            Invocation.Builder builder = client.target(baseUrl + subUrl).request();
            connectStartTime = System.currentTimeMillis();
            Response response = builder.post(Entity.entity(requestBody, MediaType.APPLICATION_JSON));
            requestEndTime = System.currentTimeMillis();

            String responseCode = String.valueOf(response.getStatus());
            String statusinfo = String.valueOf(response.getStatusInfo());
            success = (response.getStatus() >= 200 && response.getStatus() < 300);
            String responseMessage = response.readEntity(String.class);
            if (!success) {
                failureMsg = responseMessage;
            }

            String dataType = response.getHeaderString("Content-Type");
            bytesReceived = responseMessage.getBytes().length;

            bytesSent = byteProcess.getByteCount();

            allEndTime = System.currentTimeMillis();
            response.close();
            long responseTime = requestEndTime - requestStartTime;
            elapsed = allEndTime - allStartTime;
            connectTime = requestEndTime - connectStartTime;

            return new CustomResponse(timeStamp, elapsed, label, responseCode, statusinfo,responseMessage, dataType, success, failureMsg, bytesSent, bytesReceived, baseUrl + subUrl, connectTime);
        } catch (Exception e) {
            System.out.println("UsrClient-" + "postRequest(): " + e.getMessage());
        }
        return null;
    }
    //Delete Method
    //Patch Method
}
