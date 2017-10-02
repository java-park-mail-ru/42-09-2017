package ru.mail.park.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.mail.park.controllers.messages.Message;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class ControllersExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Message<?>> validatorHandler(MethodArgumentNotValidException ex) {
        List<String> response = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            response.add(fieldError.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new Message<>(response));
    }
}
