package com.chatbot.repository;

import com.chatbot.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, String> {
    List<Sale> findByRut(String rut);
}
