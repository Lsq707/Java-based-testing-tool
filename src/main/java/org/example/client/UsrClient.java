package org.example.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

import org.example.model.CustomResponse;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
This class meant to create client based on the Jersey Client
And able to get customized response
*/
public class UsrClient {
    private final Client client;
    //Assign a thread to user
    private ExecutorService clientExecutor;
    private CustomResponse customResponse = null;
    public UsrClient() {
        // Initialize client able to add config
        this.client = ClientBuilder.newBuilder().build();
        clientExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    //Requests without body
    public CustomResponse getResponse(String method,String baseUrl, String subUrl){
        SendRequest sendRequest = new SendRequest(this.client);
        if (method.equals("GET"))
            customResponse = sendRequest.getResponse(baseUrl,subUrl);
        return customResponse;
    }

    //Requests with body
    public CustomResponse getResponse(String method,String baseUrl, String subUrl,String requestBody){
        SendRequest sendRequest = new SendRequest(this.client);
        if(method.equals("POST"))
            customResponse =  sendRequest.postRequest(baseUrl,subUrl,requestBody);
        return customResponse;
    }
    public ExecutorService getClientExecutor(){
        return this.clientExecutor;
    }
    public Client getClient() {
        return client;
    }
    public void closeClient() {
        client.close();
    }
}

