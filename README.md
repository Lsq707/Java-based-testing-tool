# Java-based-testing-tool

### Introduce to classes

* UsrClient:  This class is to create a client and executorService for each user to do assigned tasks, and also call sendRequest with client parameter to sent requests to server. (Plan to add check-fail-ratio and avg-response-time here)
* SendRequest:  This class is to sent requests to the server / timing the response time / get the response from server 
* User: This class is to define user operations. Including a client, a list of tasks, userID ,host(optional) and a static method to get all the user entities created. (Plan to add wait_time as a parameter)
* UsrTask: This task will be initialized by a User entity to get the client and set the information for requests, also will create a feature assigned to the user’ executorService when the task is running. (Plan to add tag here as parameter to implement the classification)
* Runner: start the test / assign the threads to users 

