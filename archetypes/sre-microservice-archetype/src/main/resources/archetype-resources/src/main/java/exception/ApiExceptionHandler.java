package ${package}.exception;

import ${package}.common.BusinessRulesException;
import ${package}.common.ExceptionResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(BusinessRulesException.class)
    public ResponseEntity<ExceptionResponse> handleBusiness(BusinessRulesException ex) {
        ExceptionResponse respuesta =
                new ExceptionResponse("Business", ex.getMensaje(), ex.getCode(), ex.getMessage(), "");
        return ResponseEntity.status(ex.getHttpStatus()).body(respuesta);
    }
}
