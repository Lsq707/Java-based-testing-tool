package org.jload;

import jakarta.ws.rs.client.Entity;
import org.jload.model.ShapeTuple;

import org.jload.runner.Env;

import org.jload.runner.LoadTestShape;
import org.jload.runner.Runner;
import org.jload.tasks.Task;
import org.jload.user.User;
import org.jload.user.UserParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@UserParam(
        waitTime = "constant(0)"
)
class MyUser extends User {

    @Task
    void getMethod() {
        getClient("http://0.0.0.0:5000").path("/v1", "http://0.0.0.0:5000").get();
    }

    @Task
    void getMethod2() {
        getClient("http://0.0.0.0:5000").path("/v2", "http://0.0.0.0:5000").get();
    }
}

@UserParam(
        host = "http://0.0.0.0:5000",
        waitTime = "constant(0)"

)
class MyUser2 extends User {
    @Task
    void getMethod() {
        //Env.putVariable("test","test");
        getClient().path("/Second").get();
    }

    @Task(weight = 0)
        //Will not execute
    void test() {
        System.out.println("VariableTest: " + Env.getVariable("test"));
    }
}

class CustomShape extends LoadTestShape {

    @Override
    public List<ShapeTuple> tick() {
        if (getRunTime() < 4) {
            return List.of(new ShapeTuple("MyUser", 2, 1));
        }
        if (getRunTime() < 7) {
            return List.of(new ShapeTuple("MyUser", 0, 1));
        }
        return null;
    }
}
/*
class CustomShape2 extends LoadTestShape {

    @Override
    public List<ShapeTuple> tick() {
        if (getRunTime() < 3) {
            return List.of(new ShapeTuple("MyUser", 2, 1));
        }
        return  null;
    }

}*/
