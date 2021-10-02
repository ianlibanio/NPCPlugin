package com.ianlibanio.voidcommand.annotation;

import com.ianlibanio.voidcommand.data.Executor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Sub {

    String name();
    String permission() default "";
    Executor executor() default Executor.BOTH;

}
