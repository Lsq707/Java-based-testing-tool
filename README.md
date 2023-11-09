# Java-based-testing-tool

## Requirements
To compile and run the code, you must have Java 21.

## How to start
* Create your user class with related parameters in jLoadFile like below
``` 
@UserParam(
        host = "http://0.0.0.0:9000",
        waitTime = "between(0,5)"
)
class MyUser extends User {
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
class MyUser2 extends User {
    @Task
    void getMethod(){
        getClient().Path("/Second").get();
    }
}
```
* Run the main method(you can set the testing info here eg. loop time/ testing time...)
