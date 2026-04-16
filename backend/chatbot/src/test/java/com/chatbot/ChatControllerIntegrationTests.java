package com.chatbot;

import com.chatbot.controller.ChatController;
import com.chatbot.controller.ApiExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class ChatControllerIntegrationTests {

    private MockMvc mockMvc;

    @Autowired
    private ChatController chatController;

    @Autowired
    private ApiExceptionHandler apiExceptionHandler;

    @BeforeEach
    void setupMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(chatController)
                .setControllerAdvice(apiExceptionHandler)
                .build();
    }

    @Test
    void loginRejectsBlankRutWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rut": "",
                                  "password": "Password123"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Solicitud inválida"));
    }

    @Test
    void saleStartRejectsMissingProductId() throws Exception {
        mockMvc.perform(post("/api/sale/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rut": "11.111.111-1"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Debes indicar el RUT y el ID del producto"));
    }

    @Test
    void saleSignRejectsMissingSignature() throws Exception {
        mockMvc.perform(post("/api/sale/sign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "abc-123",
                                  "signature": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Debes indicar el ID de venta y la firma"));
    }

    @Test
    void chatRejectsMissingBodyWithBadRequest() throws Exception {
        mockMvc.perform(post("/api/chat")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Solicitud inválida"));
    }

    @Test
    void saleStartAndSignHappyPath() throws Exception {
        // Garantiza una base conocida para el test de contrato REST.
        mockMvc.perform(post("/api/reset"))
                .andExpect(status().isOk());

        String token = loginAndGetToken();

        MvcResult startResult = mockMvc.perform(post("/api/sale/start")
                        .header("Authorization", "Bearer " + token)
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
                                                                                                .header("Authorization", "Bearer " + token)
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

        String token = loginAndGetToken();

        MvcResult startResult = mockMvc.perform(post("/api/sale/start")
                        .header("Authorization", "Bearer " + token)
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
                                                                                                .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "%s",
                                  "signature": "Cliente QA"
                                }
                                """.formatted(saleId)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/sale/sign")
                                                                                                .header("Authorization", "Bearer " + token)
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

    @Test
    void saleStartRejectsMissingSession() throws Exception {
        mockMvc.perform(post("/api/sale/start")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rut": "11.111.111-1",
                                  "productId": "prod-1"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Sesión inválida o expirada"));
    }

    @Test
    void saleFlowE2EAllowsFetchingCompletedSale() throws Exception {
        mockMvc.perform(post("/api/reset"))
                .andExpect(status().isOk());

        String token = loginAndGetToken();

        MvcResult startResult = mockMvc.perform(post("/api/sale/start")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rut": "11.111.111-1",
                                  "productId": "prod-3"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saleId").isNotEmpty())
                .andReturn();

        String saleId = extractSaleId(startResult.getResponse().getContentAsString());

        mockMvc.perform(post("/api/sale/sign")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "saleId": "%s",
                                  "signature": "Cliente E2E"
                                }
                                """.formatted(saleId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Firma digital aplicada"));

        mockMvc.perform(get("/api/sale/{saleId}", saleId)
                        .param("rut", "11.111.111-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.saleId").value(saleId))
                .andExpect(jsonPath("$.productId").value("prod-3"))
                .andExpect(jsonPath("$.rut").value("11111111-1"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    private String loginAndGetToken() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "rut": "11.111.111-1",
                                  "password": "Password123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        return extractToken(loginResult.getResponse().getContentAsString());
    }

    private String extractToken(String body) {
        String key = "\"token\":\"";
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

