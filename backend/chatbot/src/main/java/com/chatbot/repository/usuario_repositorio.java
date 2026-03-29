package com.chatbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.chatbot.model.usuario;

public interface usuario_repositorio extends JpaRepository<usuario, Long> {
    usuario findByUsernameAndPassword(String username, String password);
}

