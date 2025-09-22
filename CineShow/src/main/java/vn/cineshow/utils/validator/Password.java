package vn.cineshow.utils.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordValidator.class)
@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {
    String message() default "Password must be between 8 and 20 characters!";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
