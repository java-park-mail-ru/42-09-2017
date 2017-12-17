package ru.mail.park.controllers.validators;

import ru.mail.park.controllers.validators.constraints.CNotBlank;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CNotBlankValidator implements ConstraintValidator<CNotBlank, CharSequence> {
    @Override
    public void initialize(CNotBlank annotation) {
    }

    @Override
    public boolean isValid(CharSequence charSequence, ConstraintValidatorContext constraintValidatorContext) {
        return charSequence == null || !charSequence.toString().trim().isEmpty();
    }
}
