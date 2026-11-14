package com.express.addressbook.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Unicode code point 长度校验器。null 视为通过，由 @NotBlank 等注解负责非空。
 */
public class CodePointLengthValidator implements ConstraintValidator<CodePointLength, CharSequence> {

    private int min;
    private int max;

    @Override
    public void initialize(CodePointLength annotation) {
        this.min = annotation.min();
        this.max = annotation.max();
    }

    @Override
    public boolean isValid(CharSequence value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        int length = Character.codePointCount(value, 0, value.length());
        return length >= min && length <= max;
    }
}
