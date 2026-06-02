package com.valledelsol.recursos.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionResponse {

    private String type;
    private String tittle;
    private String code;
    private String detail;
    private String instance;
}
