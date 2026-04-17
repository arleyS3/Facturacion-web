package com.facturacion.api.web.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    
    @GetMapping("/api/v1/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String test(){
        return "hola ADMIN";
    }
}
