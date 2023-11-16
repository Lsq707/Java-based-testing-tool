package org.jload.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

import jakarta.ws.rs.client.Invocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/*
This class meant to create client based on the Jersey Client for each user
*/
public class UserClient {
    private static final Logger logger = LoggerFactory.getLogger(UserClient.class);
    private final Client client;
    //Assign a thread to user
    private final ExecutorService clientExecutor;
    private final String host;

    public UserClient(String host) {
        // Initialize client able to add config
        this.client = ClientBuilder.newBuilder().build();
        this.client.register(ResponseTimeFilter.class);
        this.host = (host != null) ? host : "";;
        clientExecutor = Executors.newVirtualThreadPerTaskExecutor();
        if(host == null || host.isEmpty()){
            logger.warn("NOTE: Host is empty");
        }

    }

    /*
    Get the Executor for each user to execute their tasks
     */
    public ExecutorService getClientExecutor(){
        return this.clientExecutor;
    }

    public Client getClient() {
        return client;
    }

    public void closeClient() {
        client.close();
    }

    public Invocation.Builder path(String path){
        long startTime = System.currentTimeMillis();
        String timeStamp = getTimeStamp();
        return client.target(host).path(path).request().property("startTime",startTime).property("timeStamp",timeStamp);
    }

    public Invocation.Builder path(){
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

