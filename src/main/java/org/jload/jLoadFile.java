package org.jload;

import jakarta.ws.rs.client.Entity;
import org.jload.model.ShapeTuple;
import org.jload.runner.LoadTestShape;
import org.jload.tasks.Task;
import org.jload.user.User;
import org.jload.user.UserParam;

import java.util.List;

@UserParam(
        host = "http://0.0.0.0:9000",
        waitTime = "between(0,5)"
)
class MyUser extends User {
    /*
    @Task
    void getMethod(){
        getClient().path("/First").get();
    }
     */

    @Task
    void postMethod(){
        String payload = "{\"key1\":\"value1\", \"key2\":\"value2\"}";
        getClient().path("/First").post(Entity.json(payload));
    }
}

@UserParam(
        host = "http://0.0.0.0:9000",
        waitTime = "constant(5)"
)
class MyUser2 extends User {
    @Task
    void getMethod(){
       // Env.putVariable("test","test");
        getClient().path("/Second").get();
    }

    /*
    @Task
    void test(){
        System.out.println("VariableTest: " + Env.getVariable("test"));
    }
    */
}
/*
class CustomShape extends LoadTestShape {

    @Override
    public List<ShapeTuple> tick() {
        if(getRunTime() < 6) {
            int MyUserNum = 3;
            int MyUserSpqwnRate = 2;
            return List.of(new ShapeTuple("MyUser2", MyUserNum, MyUserSpqwnRate));
        }
        if(getRunTime()>=6 && getRunTime() < 11){
            return List.of(new ShapeTuple("MyUser2", 0, 2));
        }

        return null;
    }
}

*/


