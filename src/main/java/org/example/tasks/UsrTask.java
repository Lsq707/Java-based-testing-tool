package org.example.tasks;

/*
User tasks
*/
import org.example.client.UsrClient;
import org.example.model.CustomResponse;
import org.example.output.OutputTheCsv;
import org.example.user.User;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class UsrTask{
    //get the client user created
    private User user;
    //base url to create webTarget
    private String host;
    //UserClien for eachc user to send request
    private final UsrClient usrClient;
    //subUrl
    private String subUrl;
    //response msg and task info
    private CustomResponse response;
    private String fullUrl; //host + subUrl
    private String method;
    private final String taskID;
    private Future<?> future;
    //can be clasified by tags
    private String tag;
    private enum HttpMethod { GET, POST, PUT, DELETE }
    private String requestBody;

    public UsrTask(User user) {
        this.user = user;
        this.host = user.getHost();
        this.usrClient = user.getClient();
        taskID = String.valueOf(user.getUserID() + " - " + user.getTasks().size());
        user.addTask(this);
        Future<?> future = null;
        requestBody = null;
    }
    private String getFullUrl(){
        return fullUrl;
    }

    //Verify the methods
    public void setRequestMethod(String method) {
        try {
            HttpMethod.valueOf(method.toUpperCase());
            this.method = method.toUpperCase();
        } catch (IllegalArgumentException e) {
            System.out.println("Error in method setting: Invalid HTTP method");
        }
    }
    public void setSubUrl(String subUrl){
        this.subUrl = subUrl;
        fullUrl = host + subUrl;
    }

    public void setRequestBody(String requestBody){
        this.requestBody = requestBody;
    }

    //Assign a thread for each task
    public Future<?> run(ExecutorService myExecutor, OutputTheCsv outputCsv) {
            future = myExecutor.submit(() -> {
            System.out.println("Task is being executed...");
            long startTime = System.currentTimeMillis();
            if(requestBody == null)
                response = usrClient.getResponse(method,host,subUrl);
            else
                response = usrClient.getResponse(method,host,subUrl,requestBody);
            response.setRequestID(this.taskID);
            response.setGrpThreads(user.getTasks().size());
            response.setAllThreads(User.getUsers().size());
            outputCsv.writeToCsv(response);
        });
        return future ;
    }

    /*
    public CustomResponse run() {
        System.out.println("Task is being executed...");
        if(method.equals("GET")){
            long startTime = System.currentTimeMillis();
             response = usrClient.getResponse(host,subUrl);
             response.setRequestID(this.taskID);
             response.setGrpThreads(user.getTasks().size());
             response.setAllThreads(User.getUsers().size());
        }
        return this.response ;
    }
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsrTask usrTask = (UsrTask) o;
        return Objects.equals(user, usrTask.user) && Objects.equals(host, usrTask.host) && Objects.equals(usrClient, usrTask.usrClient) && Objects.equals(subUrl, usrTask.subUrl) && Objects.equals(response, usrTask.response) && Objects.equals(fullUrl, usrTask.fullUrl) && Objects.equals(method, usrTask.method) && Objects.equals(taskID, usrTask.taskID) && Objects.equals(future, usrTask.future) && Objects.equals(tag, usrTask.tag) && Objects.equals(requestBody, usrTask.requestBody);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, host, usrClient, subUrl, response, fullUrl, method, taskID, future, tag, requestBody);
    }
}

