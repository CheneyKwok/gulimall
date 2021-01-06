package com.guo.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.Set;

public class ListValueValidator implements ConstraintValidator<ListValue,Integer> {
    private final Set<Integer> set = new HashSet<>();
    @Override
    public void initialize(ListValue constraintAnnotation) {
        for (int value : constraintAnnotation.values()) {
            set.add(value);
        }

    }

    /**
     * @param value 需要校验的值
     * @param context 上下文环境
     * @return 校验结果
     */
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return set.contains(value);
    }
}
