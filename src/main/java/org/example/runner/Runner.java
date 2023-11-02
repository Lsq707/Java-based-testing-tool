package org.example.runner;

import org.example.model.CustomResponse;
import org.example.tasks.UsrTask;
import org.example.user.User;
import org.example.output.OutputTheCsv;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

/*
Assign the Virtualthreads
 */
public class Runner {
    //loop to set the task running times To be finished
    private static int loop;
    private static OutputTheCsv outputCsv;
    private String fileName;
    private static List<Future<?>> futuresUser;
    private static List<Future<?>> futuresTasks;

    /*
    LoopTime is to be finished
     */
    public Runner(){
        loop = 1;
        futuresUser = new ArrayList<>();
        futuresTasks = new ArrayList<>();
    }

    public Runner(String fileName) throws IOException {
        loop = 1;
        this.fileName = fileName;
        outputCsv = new OutputTheCsv(fileName);
        futuresUser = new ArrayList<>();
        futuresTasks = new ArrayList<>();
    }

    public Runner(int loopTime){
        loop = loopTime;
    }

    public Runner(int loopTime,String fileName) throws IOException {
        loop = loopTime;
        this.fileName = fileName;
        outputCsv = new OutputTheCsv(fileName);
        futuresUser = new ArrayList<>();
        futuresTasks = new ArrayList<>();
    }

    //Set users run
    public void Run(){
            //Assign virtual thread for each userClient
            try{// This can be a random user
                ExecutorService poolExecutor = Executors.newVirtualThreadPerTaskExecutor();
                for (User user : User.getUsers()) {
                    var futureUser = poolExecutor.submit(user);
                    futuresUser.add(futureUser);
                }
                //wait for the users to finish and close the resources
                for (Future<?> echoUses : futuresUser) {
                    echoUses.get();
                }
                //close the file
                outputCsv.closeFile();
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
    }
    //Set tasks to run
    public static void RunUsers(User user) throws ExecutionException, InterruptedException {
            for (UsrTask usrTask : user.getTasks()) {
                for (int i=0;i<loop;i++) {
                    var futureTask = usrTask.run(user.getClient().getClientExecutor(), outputCsv);
                    futuresTasks.add(futureTask);
                }
            }
            //Wait for the tasks to finish
            for (Future<?> echoTask : futuresTasks) {
                echoTask.get();
            }
    }

    /*
    public static void RunUsers(User user) {
        List<Future<?>> futuresTasks = new ArrayList<>();
            //Assign virtual thread for each userClient
            try {
               //for(int i=0;i<1000;i++) {
                    // This can be a random user
                    for (UsrTask usrTask : user.getTasks()) {
                        var futureTask = user.getClient().getClientExecutor().submit(usrTask::run);
                        futuresTasks.add(futureTask);
                    }
               // }
                //wait for the requests
                for (Future<?> echoTask : futuresTasks) {
                    System.out.println(echoTask.get());
                    outputCsv.writeToCsv((CustomResponse) echoTask.get());
                }
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
    }

     */
}
