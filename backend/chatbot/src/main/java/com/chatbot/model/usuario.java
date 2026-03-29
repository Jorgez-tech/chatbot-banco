package com.chatbot.model;

import jakarta.persistence.*;

@Entity
public class usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private double saldo;
    private double deuda;
    private String estadoPago;

    // getters y setters
    public Long getId() {
        return id;
    }
    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
    public double getSaldo() {
        return saldo;
    }
    public double getDeuda() {
        return deuda;
    }
    public String getEstadoPago() {
        return estadoPago;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }
    public void setDeuda(double deuda) {
        this.deuda = deuda;
    }
    public void setEstadoPago(String estadoPago) {
        this.estadoPago = estadoPago;
    } 
}