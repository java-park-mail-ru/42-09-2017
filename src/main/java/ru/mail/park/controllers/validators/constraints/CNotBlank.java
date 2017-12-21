package ru.mail.park.controllers.validators.constraints;

import ru.mail.park.controllers.validators.CNotBlankValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * The difference to {@link org.hibernate.validator.constraints.NotBlank}
 * is that {@code null} values are getting ignored
 *
 * @author Artur Ahadov
 */
@Documented
@Constraint(validatedBy = { CNotBlankValidator.class })
@Target({ METHOD, FIELD, ANNOTATION_TYPE, CONSTRUCTOR, PARAMETER })
@Retention(RUNTIME)
public @interface CNotBlank {
    String message() default "";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}