package vn.cineshow.utils.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = EmailValidator.class)
@Target( { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)

/// This annotation to validate the email.
/// The email valid: user.name@example.com, user_name@sub.domain.co.uk, abc123@domain.vn.
/// The mail invalid: @domain.com, user@.com, user@domain
public @interface Email {

    String message() default "Invalid email format! Email must be like: 'phongtmhe182382@fpt.edu.vn.'";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
