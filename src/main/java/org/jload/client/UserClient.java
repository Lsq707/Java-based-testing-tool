package org.jload.client;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.jnh.connector.JavaNetHttpConnectorProvider;
import org.glassfish.jersey.apache5.connector.Apache5ConnectorProvider;
import org.glassfish.jersey.apache5.connector.Apache5ClientProperties;
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
    private static String host;
    //Save resources for URI compute
    private static final ConcurrentHashMap<String, WebTarget> uriCache = new ConcurrentHashMap<>();

    public UserClient() {
        // Initialize client able to add config
        ClientConfig clientConfig = new ClientConfig().connectorProvider(new JavaNetHttpConnectorProvider()); //will create error after 8000
        /*
        final PoolingHttpClientConnectionManager poolConnectionManager = new PoolingHttpClientConnectionManager();
        poolConnectionManager.setMaxTotal(10000);
        poolConnectionManager.setDefaultMaxPerRoute(5000);
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.property(Apache5ClientProperties.CONNECTION_MANAGER, poolConnectionManager);
        clientConfig.connectorProvider(new Apache5ConnectorProvider());
         */
        this.client = ClientBuilder.newBuilder().withConfig(clientConfig).build();
        this.client.register(ResponseTimeFilter.class);
    }

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

