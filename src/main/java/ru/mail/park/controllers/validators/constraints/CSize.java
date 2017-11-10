package ru.mail.park.controllers.validators.constraints;

import ru.mail.park.controllers.validators.CSizeValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The difference to {@link javax.validation.constraints.Size}
 * is that zero length values are getting ignored
 *
 * @author Artur Ahadov
 */
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = { CSizeValidator.class })
public @interface CSize {
    String message() default "";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };

    int min() default 0;

    int max() default Integer.MAX_VALUE;
}
