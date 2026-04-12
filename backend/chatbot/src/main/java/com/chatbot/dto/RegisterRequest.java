package com.chatbot.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank(message = "RUT es requerido")
    @Size(max = 20, message = "RUT inválido")
    private String rut;

    @NotBlank(message = "La contraseña es requerida")
    @Size(max = 120, message = "La contraseña es inválida")
    private String password;

    @NotBlank(message = "El nombre es requerido")
    @Size(max = 255, message = "El nombre es inválido")
    private String name;

    @Email(message = "Email inválido")
    @Size(max = 255, message = "Email inválido")
    private String email;

    @Size(max = 50, message = "Teléfono inválido")
    private String phone;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isPasswordValid() {
        if (password == null || password.length() < 8) {
            return false;
        }
        boolean hasNumber = password.matches(".*\\d.*");
        boolean hasUpperCase = password.matches(".*[A-Z].*");
        return hasNumber && hasUpperCase;
    }

    public String getPasswordErrorMessage() {
        if (password == null || password.isEmpty()) {
            return "La contraseña es requerida";
        }
        if (password.length() < 8) {
            return "La contraseña debe tener mínimo 8 caracteres";
        }
        if (!password.matches(".*\\d.*")) {
            return "La contraseña debe contener al menos 1 número";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "La contraseña debe contener al menos 1 mayúscula";
        }
        return null;
    }
}
