package org.jload.tasks;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import org.jload.client.ResponseTimeFilter;
import org.jload.exceptions.TaskException;
import org.jload.model.ResponseStat;
import org.jload.response.Statistics;
import org.jload.runner.Env;
import org.jload.runner.Runner;
import org.jload.user.User;
import org.jload.user.WaitTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class TaskSet {
    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    private final List<Method> userTasks;
    private int sumWeight = 0;
    private final User user;
    private int loopTime;

    public TaskSet(User user, List<Method> userTasks) {
        this.user = user;
        this.userTasks = userTasks;
        loopTime = 1;
        initSumWeight();
    }

    /*
    Get the sumWeight
     */
    private void initSumWeight() {
        List<Method> toRemove = new ArrayList<>();
        for (Method task : userTasks) {
            Annotation[] annotations = task.getAnnotations();
            for (Annotation myAnnotation : annotations) {
                if (myAnnotation instanceof Task theTask) {
                    if (Env.taskTag != null) {
                        //Filter
                        Set<String> tmpTag = new HashSet<>(Arrays.asList(theTask.tag().split("\\s+")));
                        if (hasOverlap(tmpTag, Env.taskTag)) {
                            sumWeight += theTask.weight();
                        } else {
                            toRemove.add(task);
                        }
                    } else {
                        sumWeight += theTask.weight();
                    }
                    continue;
                }
                //Methods have no @task
                toRemove.add(task);
            }
        }
        userTasks.removeAll(toRemove);
    }

    /*
    The strategy to choose next task is to add weight
    until the task that has the sum bigger than the randomNum
     */
    public Method getNextMethod() {
        Random random = new Random();
        double randomNum;
        do {
            randomNum = sumWeight * random.nextDouble();
        } while (randomNum == 0);

        int currentValue = 0;
        for (Method task : userTasks) {
            currentValue += getTaskWeight(task);
            if (currentValue >= randomNum) {
                return task;
            }
        }
        return null;
    }

    /*
    Return the weight defined for each task
    */
    private int getTaskWeight(Method task) {
        int taskWeigth = 0;
        Annotation[] annotations = task.getAnnotations();
        for (Annotation myAnotation : annotations) {
            if (myAnotation instanceof Task theTask) {
                taskWeigth = theTask.weight();
            }
        }
        return taskWeigth;
    }

    public void startTesting() throws InterruptedException, TaskException {
        WaitTime waitTime = user.getWaitTimeStrategy();
        while (user.getTaskFlag()) {
            if (userTasks.isEmpty()) { //No matching task
                Runner.disposeUser(user);
                logger.warn("No matching tasks in: " + user.getClass().getName());
                break;
            }
            Method task = getNextMethod();
            if (!task.isAccessible()) {
                task.setAccessible(true);
            }
            try {
                task.invoke(user);
            } catch (InvocationTargetException ite) {
                //System.out.println(ite.getMessage() + ite);
                Throwable cause = ite.getCause();  // get the connection errors
                if (cause instanceof ProcessingException || cause instanceof WebApplicationException) {
                    trackFailInfo(cause);
                }
                logger.debug("Error in user- {} : task- {} : error: {}", user.getClass().getName(), task.getName(), ite.getMessage(), ite);
            } catch (Exception e) {
                logger.debug("Error in user- {} : task- {} : error: {}", user.getClass().getName(), task.getName(), e.getMessage(), e);
                throw new TaskException("Error when executing the " + user.getClass().getName() + "-" + task.getName());
            }
            Thread.sleep(waitTime.getWaitTime());
            //If defined loop times the tasks will only do once and then dispose the user
            loopTime += 1;
            if (Runner.getLoop() != 0 && loopTime > Runner.getLoop()) {
                Runner.disposeUser(user);
            }
        }
    }

    private void trackFailInfo(Throwable cause) {
        long endTime = System.currentTimeMillis();
        String timeStamp = ResponseTimeFilter.timeStamp.get();
        long responseTime = endTime - ResponseTimeFilter.startTime.get();
        String label = ResponseTimeFilter.label.get();
        String host = ResponseTimeFilter.host.get();
        String rewrittenPath = ResponseTimeFilter.rewrittenPath.get();
        String dataType = ResponseTimeFilter.dataType.get();
        //logger.info("Request failed - URL: {}, Method: {}, Error: {}", url, method, cause.getMessage());
        ResponseStat responseStat = new ResponseStat(timeStamp, responseTime, label, cause.getMessage(), "NA", cause.getMessage(), dataType, false, cause.getMessage(), 0, 0, host, rewrittenPath);
        Statistics.addStatistic(responseStat);
        //ScreenMetrics.addResponse(responseStat);
    }

    /*
    Check the tags
     */
    public static boolean hasOverlap(Set<String> set1, Set<String> set2) {
        Set<String> copyOfSet1 = new HashSet<>(set1);
        copyOfSet1.retainAll(set2);
        return !copyOfSet1.isEmpty();
    }
}
