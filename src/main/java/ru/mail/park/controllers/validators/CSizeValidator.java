package ru.mail.park.controllers.validators;

import org.hibernate.validator.internal.util.logging.Log;
import org.hibernate.validator.internal.util.logging.LoggerFactory;
import ru.mail.park.controllers.validators.constraints.CSize;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CSizeValidator implements ConstraintValidator<CSize, CharSequence> {
    private static final Log LOG = LoggerFactory.make();

    private int min;
    private int max;

    @Override
    public void initialize(CSize parameters) {
        min = parameters.min();
        max = parameters.max();
        validateParameters();
    }

    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
        if (charSequence == null || charSequence.length() == 0) {
            return true;
        }
        int length = charSequence.length();
        return length >= min && length <= max;
    }

    private void validateParameters() {
        if (min < 0) {
            throw LOG.getMinCannotBeNegativeException();
        }
        if (max < 0) {
            throw LOG.getMaxCannotBeNegativeException();
        }
        if (max < min) {
            throw LOG.getLengthCannotBeNegativeException();
        }
    }
}
