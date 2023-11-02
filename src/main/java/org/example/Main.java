package org.example;

import org.example.runner.Runner;
import org.example.tasks.UsrTask;
import org.example.user.User;


public class Main {

    public static void main(String[] args) throws Exception {

        /*
        A user was created with host url(can be null then add url in task)
        set user presettings(to be finished)
        create a useTask with user(must be assigned to a user)
        set the task parameters

        Create a runner to execute the test
        set runner parameters
        start run
         */

        //Create User
        User User1 = new User("http://0.0.0.0:5000");
        //Create Task assign to User1

        UsrTask task1 = new UsrTask(User1);
        //Set Task parameters
        /*
        Only support simple get without body and post now
         */
        task1.setRequestMethod("GET");
        task1.setSubUrl("");

        UsrTask task2 = new UsrTask(User1);
        task2.setRequestMethod("POST");
        task2.setSubUrl("");
        task2.setRequestBody("{\"name\": \"morpheus\", \"job\": \"leader \"}");

        /*
        User User2 = new User("http://localhost:5000");
        UsrTask task2 = new UsrTask(User2);
        task2.setRequestMethod("GET");
        task2.setSubUrl("");
         */

        //Can set presettings eg. loop\spwan... in runner
        Runner runner = new Runner(1000,"/Users/liusiqi/Desktop/test2.csv");
        //Start test
        runner.Run();

    }
}