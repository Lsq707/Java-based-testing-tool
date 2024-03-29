package org.jload.user;

import org.jload.client.UserClient;
import org.jload.exceptions.TaskException;
import org.jload.runner.Runner;
import org.jload.tasks.TaskSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
This Class meant to simulate a real user's operation
*/
@UserParam(waitTime = "constant(0)", host = "")
public class User implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(User.class);
    //One client for each user
    private final UserClient userClient;
    private UserParam userParam;
    //UserStatus as user Lifecycle
    private Boolean taskFlag = true;
    //Schedule the tasks
    private TaskSet taskSet;
    private String threadInfo;

    //Default constructor
    public User() {
        fetchAnnotationParameters();
        this.userClient = new UserClient();
        setTaskSet();
    }

    //Accept parameter
    public User(UserParam userParam) {
        this.userParam = userParam;
        this.userClient = new UserClient();
        setTaskSet();
    }

    public UserClient getClient() {
        userClient.setHost(getUserParamHost());
        return userClient;
    }

    public UserClient getClient(String host) {
        userClient.setHost(host);
        return userClient;
    }

    public String getThreadInfo() {
        return threadInfo;
    }

    //Check the super and child annotation and overwrite it
    private void fetchAnnotationParameters() {
        Annotation[] annotations = this.getClass().getAnnotations();
        for (Annotation myAnotation : annotations) {
            if (myAnotation instanceof org.jload.user.UserParam myParam) {
                this.userParam = new UserParam(myParam.waitTime(), myParam.host());
            }
        }
    }

    public Boolean getTaskFlag() {
        return taskFlag;
    }

    public void setTaskFlag(Boolean taskFlag) {
        this.taskFlag = taskFlag;
    }

    public String getUserParamHost() {
        return userParam != null ? userParam.getHost() : null;
    }

    public TaskSet getTaskSet() {
        return taskSet;
    }

    public <T extends WaitTime> T getWaitTimeStrategy() {
        return (T) this.userParam.getWaitTimeStrategy();
    }

    //Execute tasks
    @Override
    public void run() {
        //Assign virtual threads to each tasks in the user
        //logger.debug("User Running: {}", this.getClass().getName());
        threadInfo = Thread.currentThread().toString();
        //Runner.runUsers(this);
        try {
            //Mark the fist start
            Runner.getIsFirstRequestSent().compareAndSet(false, true);
            taskSet.startTesting();
        } catch (InterruptedException | TaskException e) {
            logger.error("User {} got error {}", this.getClass().getName(), e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            getClient().closeClient();
        }
        logger.debug("User Ended: {}", this.getClass().getName());
    }

    private void setTaskSet() {
        Method[] declaredMethods = this.getClass().getDeclaredMethods();
        List<Method> userTasks = new ArrayList<>(List.of(declaredMethods));
        taskSet = new TaskSet(this, userTasks);
    }

    public void setUserParamHost(String host) {
        userParam.setHost(host);
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

        return Objects.equals(userClient, user.userClient) && Objects.equals(userParam, user.userParam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userClient, userParam);
    }

    //Process the annotation
    public class UserParam {
        private String host;
        private final String waitTime;
        private Between between = null;
        private Constant constant = null;
        private ConstantThroughput constantThroughput = null;

        public UserParam(String waitTime, String host) {
            this.waitTime = waitTime.toLowerCase();
            this.host = host;
            getWaitTime();
        }

        public String getWaitTimeString() {
            return waitTime;
        }

        public String getHost() {
            return host;
        }

        //Get the waitTime strategy defined
        private void getWaitTime() {
            if (waitTime.isEmpty() || waitTime.isBlank()) {
                return;
            }
            if (waitTime.startsWith("between")) {
                String[] values = waitTime.substring(waitTime.indexOf('(') + 1, waitTime.indexOf(')')).split(",");
                long min = Long.parseLong(values[0]);
                long max = Long.parseLong(values[1]);
                between = new Between(min, max);
            } else if (waitTime.startsWith("constant")) {
                String[] values = waitTime.substring(waitTime.indexOf('(') + 1, waitTime.indexOf(')')).split(",");
                long waitTime = Long.parseLong(values[0]);
                constant = new Constant(waitTime);
            } else if (waitTime.startsWith("constantThroughput")) {
                // Extract the min and max values from the string
                String[] values = waitTime.substring(waitTime.indexOf('(') + 1, waitTime.indexOf(')')).split(",");
                long taskRunsPerSecond = Long.parseLong(values[0]);
                constantThroughput = new ConstantThroughput(taskRunsPerSecond);
            } else { //Default
                constant = new Constant(0);
            }
        }

        public WaitTime getWaitTimeStrategy() {
            if (between != null) {
                return between;
            }
            if (constant != null) {
                return constant;
            }
            if (constantThroughput != null) {
                return constantThroughput;
            }
            return null; // None of the strategies are set
        }

        private void setHost(String host) {
            this.host = host;
        }
    }
}
