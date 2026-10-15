package com.kloudspot.ems.exception;

import com.kloudspot.ems.controller.EmployeeWebController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice(assignableTypes = EmployeeWebController.class)
public class WebExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(WebExceptionHandler.class);

    @ExceptionHandler(EmployeeNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(EmployeeNotFoundException ex, Model model) {
        return errorPage(model, HttpStatus.NOT_FOUND, "Employee not found",
                ex.getMessage() + ". It may have been deleted.");
    }

    @ExceptionHandler({InvalidRequestException.class, MethodArgumentTypeMismatchException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(Exception ex, Model model) {
        String message = ex instanceof MethodArgumentTypeMismatchException mismatch
                ? "Invalid value '" + mismatch.getValue() + "' for '" + mismatch.getName() + "'."
                : ex.getMessage();
        return errorPage(model, HttpStatus.BAD_REQUEST, "Invalid request", message);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String handleConflict(DataIntegrityViolationException ex, Model model) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return errorPage(model, HttpStatus.CONFLICT, "Could not save changes",
                "The changes conflict with existing data (for example, a duplicate email). Please try again.");
    }

    private String errorPage(Model model, HttpStatus status, String title, String message) {
        model.addAttribute("status", status.value());
        model.addAttribute("error", title);
        model.addAttribute("message", message);
        return "error";
    }
}
