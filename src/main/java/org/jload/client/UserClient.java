package org.jload.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;

/*
This class meant to create client based on the Jersey Client for each user
*/
public class UserClient {
    private static final Logger logger = LoggerFactory.getLogger(UserClient.class);
    private final Client client;
    //Assign a thread to user
    //private final ExecutorService clientExecutor;
    private static String host;
    //Save resources for URI compute
    private static final ConcurrentHashMap<String, WebTarget> uriCache = new ConcurrentHashMap<>();

    public UserClient() {
        // Initialize client able to add config
        this.client = ClientBuilder.newBuilder().build();
        this.client.register(ResponseTimeFilter.class);
        //clientExecutor = Executors.newVirtualThreadPerTaskExecutor();
    }

    /*
    Get the Executor for each user to execute their tasks
     */
    /*
    public ExecutorService getClientExecutor() {
        return this.clientExecutor;
    }
     */

    public Client getClient() {
        return client;
    }

    public void closeClient() {
        client.close();
    }

    public Invocation.Builder path(String path) {
        WebTarget webTarget = uriCache.computeIfAbsent(path, this::buildWebTarget);
        return createRequestBuilder(webTarget);
    }

    public Invocation.Builder path() {
        return path("");
    }

    public Invocation.Builder path(String path, String rewrittenPath) {
        WebTarget webTarget = uriCache.computeIfAbsent(path, this::buildWebTarget);
        Invocation.Builder builder = createRequestBuilder(webTarget);
        return builder.property("rewritten", rewrittenPath);
    }

    private WebTarget buildWebTarget(String path) {
        // When there has no cache
        return client.target(host).path(path);
    }

    private Invocation.Builder createRequestBuilder(WebTarget webTarget) {
        long startTime = System.currentTimeMillis();
        String timeStamp = getTimeStamp();
        return webTarget.request().property("startTime", startTime).property("timeStamp", timeStamp);
    }

    private String getTimeStamp() {
        LocalDateTime currentDateTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return currentDateTime.format(formatter);
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        UserClient.host = host;
    }
}

