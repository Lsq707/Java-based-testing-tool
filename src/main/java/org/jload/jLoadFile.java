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
        host = "http://0.0.0.0:5000",
        waitTime = "constant(0)"
)
class MyUser extends User {

    @Task(tag = "V1")
    void getMethod() {
        getClient("http://0.0.0.0:5000").path("/First").get();
    }

    @Task(tag = "V1")
    void getMethod2() {
        getClient().path("/First").get();
    }

    /*
    @Task(tag = "V2")
    void testTag() {
        System.out.println("V2");
    }


    /*
    @Task
    void postMethod() {
        String payload = "{\"key1\":\"value1\", \"key2\":\"value2\"}";
        getClient().path("/First").post(Entity.json(payload));
    }

     */
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



/*

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


class CustomShape2 extends LoadTestShape {

    @Override
    public List<ShapeTuple> tick() {
        if (getRunTime() < 3) {
            return List.of(new ShapeTuple("MyUser", 2, 1));
        }
        return  null;
    }

}

 */





