package com.valledelsol.incidentes.common;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BusinessRulesException extends Exception {

    private final String code;
    private final HttpStatus httpStatus;
    private final String mensaje;

    public BusinessRulesException(String code, HttpStatus httpStatus, String mensaje) {
        super(mensaje);
        this.code = code;
        this.httpStatus = httpStatus;
        this.mensaje = mensaje;
    }
}
