package ru.mail.park.exceptions.handlers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.mail.park.controllers.messages.Message;
import ru.mail.park.exceptions.ControllerValidationException;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class ControllersExceptionHandler {
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Message<?>> validatorHandler(MethodArgumentNotValidException ex) {
        List<String> response = new ArrayList<>();
        for (ObjectError objectError : ex.getBindingResult().getAllErrors()) {
            response.add(objectError.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new Message<>(response));
    }

    @ExceptionHandler(ControllerValidationException.class)
    public ResponseEntity<Message<?>> validHandler(ControllerValidationException ex) {
        return ResponseEntity
                .badRequest()
                .body(new Message<>(ex.getResponseList()));
    }
}
