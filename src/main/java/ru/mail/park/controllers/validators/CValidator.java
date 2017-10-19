package ru.mail.park.controllers.validators;

import java.lang.annotation.*;

@Documented
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CValidator {
    String fieldName() default "";
    boolean nullable() default false;
}
