package org.jload.tasks;

import org.jload.runner.Runner;
import org.jload.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;

public class TaskSet {
    private static final Logger logger = LoggerFactory.getLogger(Runner.class);
    private final List<Method> userTasks;
    private int sumWeight = 0;

    public TaskSet(List<Method> userTasks) {
        this.userTasks = userTasks;
        initSumWeight();
    }

    /*
    Get the sumWeight
     */
    private void initSumWeight(){
        for(Method task: userTasks){
            Annotation[] annotations =  task.getAnnotations();
            for (Annotation myAnotation : annotations) {
                if (myAnotation instanceof Task theTask) {
                    sumWeight += theTask.weight();
                }
            }
        }
    }

    /*
    The strategy to choose next task is to add weight
    until the task that has the sum bigger than the randomNum
     */
    public Method getNextMethod(){
        Random random = new Random();
        double randomNum;
        do {
            randomNum = sumWeight * random.nextDouble();
        } while (randomNum == 0);

        int currentValue = 0;
        for(Method task: userTasks){
            currentValue += getTaskWeight(task);
            if(currentValue >= randomNum)
                return task;
        }
        return null;
    }

    /*
    Return the weight defined for each task
    */
    private int getTaskWeight(Method task){
        int taskWeigth = 0;
        Annotation[] annotations =  task.getAnnotations();
        for (Annotation myAnotation : annotations) {
            if (myAnotation instanceof Task theTask) {
                taskWeigth = theTask.weight();
            }
        }
        return taskWeigth;
    }


}
