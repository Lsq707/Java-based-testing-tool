package org.jload.tasks;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Task{

    String[] tag() default {};
    String subUrl() default  "";
    String name() default  "";
    int weight() default 1;

    //response msg and task info
}
