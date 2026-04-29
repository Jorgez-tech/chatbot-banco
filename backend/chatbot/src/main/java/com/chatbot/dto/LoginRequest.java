package com.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    @NotBlank(message = "RUT es requerido")
    @Size(max = 20, message = "RUT inválido")
    private String rut;

    @NotBlank(message = "Contraseña es requerida")
    @Size(max = 120, message = "Contraseña inválida")
    private String password;

    public String getRut() {
        return rut;
    }

    public void setRut(String rut) {
        this.rut = rut;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
