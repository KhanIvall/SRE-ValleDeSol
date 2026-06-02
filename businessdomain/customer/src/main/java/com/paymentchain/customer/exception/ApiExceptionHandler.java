/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.customer.exception;

import com.paymentchain.customer.common.BusinessRulesException;
import com.paymentchain.customer.common.ExceptionResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 *
 * @author Duoc
 */
@RestControllerAdvice
public class ApiExceptionHandler {
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handlerUnknowHostException(Exception ex){
        ExceptionResponse respuesta = new ExceptionResponse("Técnico", "Input output exception", "459", ex.getMessage(), "");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(respuesta);
    }
    
    @ExceptionHandler(BusinessRulesException.class)
    public ResponseEntity<?> handlerBusinessErrorException(BusinessRulesException ex){
        ExceptionResponse respuesta = new ExceptionResponse("Business",ex.getMensaje(), ex.getCode(), ex.getMessage(), "");
        return ResponseEntity.status(ex.getHttpStatus()).body(respuesta);
    }
}
