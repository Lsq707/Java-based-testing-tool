package org.jload.tasks;

import org.jload.runner.Env;
import org.jload.exceptions.TaskException;
import org.jload.runner.Runner;
import org.jload.user.User;
import org.jload.user.WaitTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
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
    private User user;
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
            for (Annotation myAnotation : annotations) {
                if (myAnotation instanceof Task theTask) {
                    if(Env.taskTag != null){
                        //Filter
                        Set<String> tmpTag = new HashSet<>(Arrays.asList(theTask.tag().split("\\s+")));
                        if (hasOverlap(tmpTag, Env.taskTag)) {
                            sumWeight += theTask.weight();
                        } else {
                            toRemove.add(task);
                        }
                    }
                    else
                        sumWeight += theTask.weight();
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
            if(userTasks.isEmpty()) { //No matching task
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
            }catch (Exception e){
                logger.error("Task execute error: {}", e.getMessage(), e);
                System.out.println("Error in user-" + user.getClass().getName() + " : task-" + task.getName());
                throw new TaskException("Error in executing task: if the target service accessible ?");
            }
            Thread.sleep(waitTime.getWaitTime());
            //If defined loop times the tasks will only do once and then dispose the user
            loopTime += 1;
            if (Runner.loop != 0 && loopTime > Runner.loop) {
                Runner.disposeUser(user);
            }
        }
        user.getClient().closeClient();
    }

    public static boolean hasOverlap(Set<String> set1, Set<String> set2) {
        Set<String> copyOfSet1 = new HashSet<>(set1);
        copyOfSet1.retainAll(set2);
        return !copyOfSet1.isEmpty();
    }
}
