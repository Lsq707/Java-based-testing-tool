package org.jload.runner;

import org.jload.model.ShapeTuple;
import org.jload.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/*
Process global parameters
*/
public class Env {
    private static final Logger logger = LoggerFactory.getLogger(Env.class);
    private static HashMap<String, Object> userVariables = new HashMap<>();
    private static final Class<?> shapeClass = getShapeClass();
    private static List<Class<?>> definedUsers;

    /*
    Get the customized shape class in jLoadFile
    */
    private static Class<?> getShapeClass(){
        List<Class<?>> scanShapeClass = ClassScanner.getClasses("LoadTestShape");
        if(scanShapeClass.size() > 1) {
            logger.error("Only one customized shape allowed");
            return null;
        }
        if(scanShapeClass.isEmpty()) {
            logger.info("Using default shape");
            return null;
        }
        return scanShapeClass.get(0);
    }

    /*
    Initial the shape class declared or default
     */
    public static LoadTestShape initShape(){
        LoadTestShape loadTestShape = null;
        if(shapeClass == null){
            loadTestShape = defaultShape();
        }
        else if(LoadTestShape.class.isAssignableFrom(shapeClass) && !shapeClass.isInterface() && !Modifier.isAbstract(shapeClass.getModifiers())){
            try {
                Constructor<?> constructor = shapeClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                loadTestShape = (LoadTestShape) constructor.newInstance();
                logger.info("Instance of {} created: {}", shapeClass.getName(), loadTestShape);
            } catch (Exception e) {
                logger.error("Error creating an instance of class {}: {}", shapeClass.getName(), e.getMessage(), e);
            }
        }
        return loadTestShape;
    }

    /*
     Get the user class in jLoadFile
    */
    public static List<Class<?>> getUserClass(){
        definedUsers = new ArrayList<>();
        List<Class<?>> Users = ClassScanner.getClasses("User");
        for (Class<?> cls : Users) {
            try {
                if (User.class.isAssignableFrom(cls) && !cls.isInterface() && !Modifier.isAbstract(cls.getModifiers())) {
                    definedUsers.add(cls);
                    logger.info("User class {} defined", cls.getName());
                }
            } catch (Exception e) {
                logger.error("Error getting the definition of class {}: {}", cls.getName(), e.getMessage(), e);
            }
        }
        return definedUsers;
    }

    private static LoadTestShape defaultShape(){
        int testingTime = Runner.getTestingTime();
        int spawnRate = Runner.getSpawnRate();
        int userNumber = Runner.getUserNum();
        int perUserNumber = userNumber/definedUsers.size();
        int perUserRate = spawnRate/definedUsers.size();

        return new LoadTestShape() {
            @Override
            public List<ShapeTuple> tick() {
                if(getRunTime() > testingTime)
                    return null;
                List<ShapeTuple> res = new ArrayList<>();
                for(Class<?> cls : definedUsers){
                    res.add(new ShapeTuple(getClsName(cls),perUserNumber,perUserRate));
                }
                return res;
            }

        };
    }


    /*
    Get the user Variable defined before
     */
    public static Object getVariable(String name){
        return userVariables.get(name);
    }

    /*
    Define the user Variable that can be used by other users or tasks
     */
    public static void putVariable(String name, Object variable){
        userVariables.put(name,variable);
    }

    /*
    Get the class name without pkg name
    */
    private static String getClsName(Class<?> cls){
        String name = null;
        int lastDot = cls.getName().lastIndexOf(".");
        name = cls.getName().substring(lastDot+1);
        return name;
    }

}
