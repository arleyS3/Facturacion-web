package com.facturacion.api.web.dto;

import lombok.*;

@Getter
@Setter
public class RegisterRequest {
    
    private String email;
    private String password;
}
