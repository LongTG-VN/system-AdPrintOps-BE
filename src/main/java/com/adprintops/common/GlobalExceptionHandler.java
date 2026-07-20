package com.adprintops.common;

import com.adprintops.auth.EmailAlreadyRegisteredException;
import com.adprintops.auth.InvalidCredentialsException;
import com.adprintops.pricing.PricingConfigurationException;
import com.adprintops.user.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Dữ liệu gửi lên không hợp lệ.", fieldErrors);
    }

    @ExceptionHandler(PricingConfigurationException.class)
    public ResponseEntity<ApiError> handlePricingConfiguration(PricingConfigurationException exception) {
        return build(HttpStatus.SERVICE_UNAVAILABLE, "PRICING_CONFIGURATION_UNAVAILABLE", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ResponseEntity<ApiError> handleDuplicateEmail(EmailAlreadyRegisteredException exception) {
        return build(HttpStatus.CONFLICT, "EMAIL_ALREADY_REGISTERED", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ApiError> handleInvalidCredentials(InvalidCredentialsException exception) {
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", exception.getMessage(), Map.of());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiError> handleMissingUser(UserNotFoundException exception) {
        return build(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", exception.getMessage(), Map.of());
    }

    private ResponseEntity<ApiError> build(
            HttpStatus status,
            String code,
            String message,
            Map<String, String> fieldErrors
    ) {
        return ResponseEntity.status(status).body(new ApiError(Instant.now(), status.value(), code, message, fieldErrors));
    }
}
