package org.jload;

import jakarta.ws.rs.client.Entity;
import org.jload.tasks.Task;
import org.jload.user.User;
import org.jload.user.UserParam;

@UserParam(
        host = "http://0.0.0.0:9000",
        waitTime = "between(0,5)"
)
class MyUser extends User {
    @Task
    void getMethod(){
       // System.out.println("get");
        getClient().path("/First").get();
    }
    @Task
    void postMethod(){
        String payload = "{\"key1\":\"value1\", \"key2\":\"value2\"}";
        getClient().path("/First").post(Entity.json(payload));
    }
}

@UserParam(
        host = "http://0.0.0.0:9000",
        waitTime = "constant(0)"
)
class MyUser2 extends User {
    @Task
    void getMethod(){
        getClient().path("/Second").get();
    }
}
