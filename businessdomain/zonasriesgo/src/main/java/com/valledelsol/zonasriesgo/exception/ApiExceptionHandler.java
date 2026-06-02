package com.valledelsol.zonasriesgo.exception;

import com.valledelsol.zonasriesgo.common.BusinessRulesException;
import com.valledelsol.zonasriesgo.common.ExceptionResponse;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponse> handleGeneral(Exception ex) {
        ExceptionResponse respuesta = new ExceptionResponse("Tecnico", "Error interno", "ZON-500", ex.getMessage(), "");
        return ResponseEntity.internalServerError().body(respuesta);
    }
}
