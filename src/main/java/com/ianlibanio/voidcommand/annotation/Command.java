package com.ianlibanio.voidcommand.annotation;

import com.ianlibanio.voidcommand.data.Executor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    String name();
    String permission() default "";
    String[] aliases() default {};

    Executor executor() default Executor.BOTH;

}
