/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.customer.common;

import lombok.Data;
import org.springframework.http.HttpStatus;

/**
 *
 * @author Duoc
 */
@Data
public class BusinessRulesException extends Exception{

    
    private long id;
    private String code;
    private HttpStatus httpStatus;
    private String mensaje;
    
    public BusinessRulesException(long id, String code, HttpStatus httpStatus, String message) {
        super(message);
        this.code =code;
        this.id =id;
        this.httpStatus =httpStatus;
    }
    
    public BusinessRulesException(long id, String code, HttpStatus httpStatus,String mensaje, String message) {
        super(message);
        this.code =code;
        this.id =id;
        this.httpStatus =httpStatus;
        this.mensaje =mensaje;
    }
    
    public BusinessRulesException(long id, String message) {
        super(message);
        this.id =id;
        
    }
    
    public BusinessRulesException(String code, HttpStatus httpStatus, String message) {
        super(message);
        this.code =code;
        this.httpStatus =httpStatus;
    }
}
