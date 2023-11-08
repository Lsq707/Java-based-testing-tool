package org.jload.user;

import org.jload.client.UsrClient;
import org.jload.runner.Runner;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
This Class meant to simulate a real user's operation
*/
@UserParam
public class User implements Runnable{

    //One client for each user
    private final UsrClient usrClient;
    private String waitTimeString;
    private Between between = null;
    private Constant constant = null;
    private ConstantThroughput constantThroughput = null;


    public User(){
        this.usrClient = new UsrClient(getUserParamHost());
        getWaitTime();
        getWaitTime();
    }

    public UsrClient getClient(){return usrClient;}

    public WaitTime getwaitTimeStrategy(){
        if (between != null) {
            return between;
        } else if (constant != null) {
            return constant;
        } else if (constantThroughput != null) {
            return constantThroughput;
        } else {
            // None of the strategies are set
            return null;
        }
    }

    private String getUserParamHost() {
        Annotation[] annotations = this.getClass().getAnnotations();
        for (Annotation annotation : annotations) {
            if (annotation instanceof UserParam) {
                UserParam userParam = (UserParam) annotation;
                waitTimeString = userParam.waitTime().toLowerCase();
                return userParam.host();
            }
        }
        return null; // Or handle the case where UserParam is not found
    }

    //Get the waitTime strategy defined
    private void getWaitTime(){
        if(waitTimeString.isEmpty()|| waitTimeString.isBlank())
            return;
        if (waitTimeString.startsWith("between")) {
            // Extract the min and max values from the string
            String[] values = waitTimeString.substring(waitTimeString.indexOf('(') + 1, waitTimeString.indexOf(')')).split(",");
            long min = Long.parseLong(values[0]);
            long max = Long.parseLong(values[1]);
            between = new Between(min,max);
        } else if(waitTimeString.startsWith("constant")) {
            // Extract the min and max values from the string
            String[] values = waitTimeString.substring(waitTimeString.indexOf('(') + 1, waitTimeString.indexOf(')')).split(",");
            long waitTime = Long.parseLong(values[0]);
            constant = new Constant(waitTime);
        } else if(waitTimeString.startsWith("constantThroughput")) {
            // Extract the min and max values from the string
            String[] values = waitTimeString.substring(waitTimeString.indexOf('(') + 1, waitTimeString.indexOf(')')).split(",");
            long taskRunsPerSecond = Long.parseLong(values[0]);
            constantThroughput = new ConstantThroughput(taskRunsPerSecond);
        }else { //Default
            constant = new Constant(0);
        }

    }

    //Execute tasks
    @Override
    public void run() {
        try {
            //Assign virtual threads to each tasks in the user
            Runner.RunUsers(this);
        } catch (InterruptedException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
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
        return Objects.equals(usrClient, user.usrClient);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usrClient);
    }
}
