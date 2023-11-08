package org.jload;

import jakarta.ws.rs.client.Entity;
import org.jload.tasks.Task;
import org.jload.user.User;
import org.jload.user.UserParam;


public class jLoadFile {
    @UserParam(
            host = "http://0.0.0.0:9000",
            waitTime = "between(0,5)"
    )
    static class MyUser extends User {
        @Task
        void getMethod(){
           // System.out.println("get");
            getClient().Path("/First").get();
        }
        @Task
        void postMethod(){
            String payload = "{\"key1\":\"value1\", \"key2\":\"value2\"}";
            getClient().Path("/First").post(Entity.json(payload));;
        }
    }

    @UserParam(
            host = "http://0.0.0.0:9000",
            waitTime = "constant(0)"
    )
    static class MyUser2 extends User {
        @Task
        void getMethod(){
            getClient().Path("/Second").get();
        }
    }
}
