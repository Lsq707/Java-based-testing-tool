package org.jload;

import jakarta.ws.rs.client.Entity;
import org.jload.model.ShapeTuple;

import org.jload.runner.Env;

import org.jload.runner.LoadTestShape;
import org.jload.tasks.Task;
import org.jload.user.User;
import org.jload.user.UserParam;

import java.util.List;

@UserParam(
        host = "http://0.0.0.0:9000",
        waitTime = "constant(0)"
)
class MyUser extends User {
    /*
    @Task
    void test1() throws InterruptedException {
        Thread.sleep(5000);
        System.out.println("test1");
    }

    @Task
    void test2() throws InterruptedException {
        Thread.sleep(5000);
        System.out.println("test2");
    }

    @Task
    void test3() throws InterruptedException {
        Thread.sleep(5000);
        System.out.println("test3");
    }

     */

    @Task
    void getMethod() {
        getClient("http://0.0.0.0:5000").path("/First").get();
    }

    @Task
    void getMethod2() {
        getClient().path("/First").get();
    }

    /*
    @Task
    void postMethod() {
        String payload = "{\"key1\":\"value1\", \"key2\":\"value2\"}";
        getClient().path("/First").post(Entity.json(payload));
    }

     */

}
/*
@UserParam(
        host = "http://0.0.0.0:9000",
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

 */

/*
class CustomShape extends LoadTestShape {

    @Override
    public List<ShapeTuple> tick() {

        return List.of(new ShapeTuple("MyUser", 2, 1));
    }

}

 */

