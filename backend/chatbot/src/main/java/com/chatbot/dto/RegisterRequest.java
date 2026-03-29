package com.chatbot.dto;

public class RegisterRequest {
    private String rut;
    private String password;
    private String name;
    private String email;
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
