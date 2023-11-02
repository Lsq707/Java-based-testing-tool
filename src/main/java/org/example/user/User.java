package org.example.user;

import org.example.client.UsrClient;
import org.example.runner.Runner;
import org.example.tasks.UsrTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import jakarta.ws.rs.core.Response;

/*
This Class meant to simulate a real user's operation
*/
public class User implements Runnable{

    private static final List<User> USERS = new ArrayList<>();
    //One client for each user
    private final UsrClient usrClient;
    private final String usrID;
    private String host;
    private List<UsrTask> tasks;

    public User(String host){
        this.host = host;
        this.usrClient = new UsrClient();
        this.tasks = new ArrayList<>();
        USERS.add(this);
        usrID = String.valueOf(USERS.size());
    }

    public void addTask(UsrTask usrTask) {
        tasks.add(usrTask);
    }
    public UsrClient getClient(){
        return usrClient;
    }
    public String getHost(){
        return host;
    }
    public List<UsrTask> getTasks(){
        return tasks;
    }
    public static List<User> getUsers(){return USERS;}
    public String getUserID(){
        return usrID;
    }

    //execute tasks
    @Override
    public void run() {
        try {
            Runner.RunUsers(this);
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        //Release the resources after requests
            //ussrClient.closeClient();
           // System.out.println("Release Resources");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return Objects.equals(usrClient, user.usrClient) && Objects.equals(usrID, user.usrID) && Objects.equals(host, user.host) && Objects.equals(tasks, user.tasks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usrClient, usrID, host, tasks);
    }

    //Able to do presettings to UsrClient

    //List of taks can be categories by tags

    //Wait time between each tasks

    //Execute tasks and if any thing wrong will report by other class



}
