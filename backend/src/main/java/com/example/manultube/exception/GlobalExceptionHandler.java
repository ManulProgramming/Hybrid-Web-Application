package com.example.manultube.exception;

import com.example.manultube.model.APIError;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIError> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage,
                        (existing, replacement) -> existing
                ));

        APIError response = new APIError(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                errors
        );

        return ResponseEntity
                .status(response.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<APIError> handleConstraintViolation(
            ConstraintViolationException ex) {

        Map<String, String> errors = ex.getConstraintViolations()
                .stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        violation -> violation.getMessage()
                ));

        APIError response = new APIError(
                HttpStatus.BAD_REQUEST,
                "Constraint violation",
                errors
        );
        return ResponseEntity
                .status(response.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<APIError> handleDataIntegrityViolation(
            DataIntegrityViolationException ex){
        APIError response = new APIError(
                HttpStatus.BAD_REQUEST,
                "Data integrity violation",
                Map.of("error", "Data already exists")
        );
        return ResponseEntity
                .status(response.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @ExceptionHandler(EmptyResultDataAccessException.class)
    public ResponseEntity<APIError> handleEmptyResultDataAccess(
            EmptyResultDataAccessException ex){
        APIError response = new APIError(
                HttpStatus.BAD_REQUEST,
                "Empty result",
                Map.of("error", "Data not found")
        );
        return ResponseEntity
                .status(response.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @ExceptionHandler(DataAccessResourceFailureException.class)
    public ResponseEntity<APIError> handleDataAccessResourceFailureException(
            DataAccessResourceFailureException ex){
        APIError response = new APIError(
                HttpStatus.BAD_REQUEST,
                "Data Access Resource Failure",
                Map.of("error",ex.getMessage())
        );
        return ResponseEntity
                .status(response.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIError> handleGeneralException(Exception ex) {

        APIError response = new APIError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                Map.of("error", ex.getMessage())
        );
        return ResponseEntity
                .status(response.getStatus())
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}