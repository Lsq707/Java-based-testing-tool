package org.jload.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

import jakarta.ws.rs.client.Invocation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private String host;

    public UsrClient(String host) {
        // Initialize client able to add config
        this.client = ClientBuilder.newBuilder().build();
        this.client.register(ResponseTimeFilter.class);
        this.host = host;
        clientExecutor = Executors.newVirtualThreadPerTaskExecutor();
        if(host.isEmpty()){
            System.out.println("NOTE: host is empty");
        }

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

    public Invocation.Builder Path(String path){
        long startTime = System.currentTimeMillis();
        String timeStamp = getTimeStamp();
        return client.target(host).path(path).request().property("startTime",startTime).property("timeStamp",timeStamp);
    }

    public Invocation.Builder Path(){
        long startTime = System.currentTimeMillis();
        String timeStamp = getTimeStamp();
        return client.target(host).path("").request().property("startTime",startTime).property("timeStamp",timeStamp);
    }

    private String getTimeStamp(){
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentDateTime.format(formatter);
    }

}

