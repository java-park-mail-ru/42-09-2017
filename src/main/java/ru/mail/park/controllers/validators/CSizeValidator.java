package ru.mail.park.controllers.validators;

import ru.mail.park.controllers.validators.constraints.CSize;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CSizeValidator implements ConstraintValidator<CSize, CharSequence> {
    private int min;
    private int max;

    @Override
    public void initialize(CSize parameters) {
        min = parameters.min();
        max = parameters.max();
    }

    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
        if (charSequence == null || charSequence.length() == 0) {
            return true;
        }
        final int length = charSequence.length();
        return length >= min && length <= max;
    }
}
