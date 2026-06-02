/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.paymentchain.customer.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author Duoc
 */
@Schema(description = "Objeto que retorna el error enontrado en el sistems")
@NoArgsConstructor
@Data
public class ExceptionResponse {
    
    @Schema(description = "Tipo de Error encontrado en el sistema",name ="type"
            ,requiredMode = Schema.RequiredMode.REQUIRED, example = "/error/errorlist/" )
    private String type;
    
    @Schema(description = "Tipo de Error encontrado en el sistema",name ="tittle"
            ,requiredMode = Schema.RequiredMode.REQUIRED, example = "/error/errorlist/" )
    private String tittle;
    
    @Schema(description = "Este código unico depende del problema encontrado",name ="code"
            ,requiredMode = Schema.RequiredMode.NOT_REQUIRED, example = "/error/errorlist/" )
    private String code;
    
    @Schema(description = "Tipo de Error encontrado en el sistema",name ="detail"
            ,requiredMode = Schema.RequiredMode.REQUIRED, example = "/error/errorlist/" )
    private String detail;
    
    @Schema(description = "Tipo de Error encontrado en el sistema",name ="instance"
            ,requiredMode = Schema.RequiredMode.REQUIRED, example = "/error/errorlist/" )
    private String instance;

    public ExceptionResponse(String type, String tittle, String code, String detail, String instance) {
        this.type = type;
        this.tittle = tittle;
        this.code = code;
        this.detail = detail;
        this.instance = instance;
    }
    
    
}
