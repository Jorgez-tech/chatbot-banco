package com.chatbot;

import com.chatbot.controller.ChatController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ChatControllerIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private ChatController chatController;

    @BeforeEach
    void setupMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController).build();
    }

    @Test
    void saleStartAndSignHappyPath() throws Exception {
        // Garantiza una base conocida para el test de contrato REST.
        mockMvc.perform(post("/api/reset"))
                .andExpect(status().isOk());

        MvcResult startResult = mockMvc.perform(post("/api/sale/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rut": "11.111.111-1",
                                  "productId": "prod-1"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Venta iniciada"))
                .andExpect(jsonPath("$.saleId").isNotEmpty())
                .andReturn();

        String saleId = extractSaleId(startResult.getResponse().getContentAsString());

        mockMvc.perform(post("/api/sale/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "%s",
                                  "signature": "Cliente QA"
                                }
                                """.formatted(saleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Firma digital aplicada"));
    }

    @Test
    void saleSignRejectsAlreadySignedSale() throws Exception {
        mockMvc.perform(post("/api/reset"))
                .andExpect(status().isOk());

        MvcResult startResult = mockMvc.perform(post("/api/sale/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rut": "11.111.111-1",
                                  "productId": "prod-2"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn();

        String saleId = extractSaleId(startResult.getResponse().getContentAsString());

        mockMvc.perform(post("/api/sale/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "%s",
                                  "signature": "Cliente QA"
                                }
                                """.formatted(saleId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/sale/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "%s",
                                  "signature": "Cliente QA 2"
                                }
                                """.formatted(saleId)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Venta no encontrada o ya firmada"));
    }

    private String extractSaleId(String body) {
        String key = "\"saleId\":\"";
        int start = body.indexOf(key);
        if (start < 0) {
            return "";
        }
        int valueStart = start + key.length();
        int end = body.indexOf('"', valueStart);
        if (end < 0) {
            return "";
        }
        return body.substring(valueStart, end);
    }
}

