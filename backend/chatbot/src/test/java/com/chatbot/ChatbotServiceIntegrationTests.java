package com.chatbot;

import com.chatbot.model.Sale;
import com.chatbot.repository.ProductRepository;
import com.chatbot.repository.SaleRepository;
import com.chatbot.repository.UserRepository;
import com.chatbot.service.ChatbotService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ChatbotServiceIntegrationTests {

    @Autowired
    private ChatbotService chatbotService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private SaleRepository saleRepository;

    private static final String SEEDED_RUT_CANONICAL = "11111111-1";
    private static final String SEEDED_PASSWORD = "Password123";
    private static final String TEST_RUT_CANONICAL = "22222222-2";

    @BeforeEach
    void ensureSeedUser() {
        if (!userRepository.existsById(SEEDED_RUT_CANONICAL)) {
            chatbotService.registerUser(SEEDED_RUT_CANONICAL, SEEDED_PASSWORD, "Usuario Seed", "seed@example.com", "+56911111111");
        }
        cleanupTestUserData();
    }

    @AfterEach
    void cleanup() {
        cleanupTestUserData();
    }

    private void cleanupTestUserData() {
        saleRepository.findByRut(TEST_RUT_CANONICAL)
                .forEach(sale -> saleRepository.deleteById(sale.getId()));

        if (userRepository.existsById(TEST_RUT_CANONICAL)) {
            userRepository.deleteById(TEST_RUT_CANONICAL);
        }
    }

    @Test
    void loginSeedAcceptsNormalizedAndFormattedRut() {
        assertNotNull(chatbotService.loginUser("11111111-1", SEEDED_PASSWORD));
        assertNotNull(chatbotService.loginUser("11.111.111-1", SEEDED_PASSWORD));
        assertNotNull(chatbotService.loginUser("111111111", SEEDED_PASSWORD));
    }

    @Test
    void registeredUserPersistsAfterServiceRestartSimulation() {
        String registerMessage = chatbotService.registerUser("22.222.222-2", "Password123", "Persist User", "persist@example.com", "+56922222222");
        assertTrue(registerMessage.contains("exitosamente") || registerMessage.contains("ya existe"));
        assertTrue(userRepository.existsById(TEST_RUT_CANONICAL));

        ChatbotService restartedService = new ChatbotService(userRepository, productRepository, saleRepository);
        restartedService.init();

        assertTrue(userRepository.existsById(TEST_RUT_CANONICAL));
        assertNotNull(restartedService.loginUser("22.222.222-2", "Password123"));
        assertNotNull(restartedService.loginUser("222222222", "Password123"));
    }

    @Test
    void chatSupportsAtLeastThreeProductsForContracting() {
        List<String> productIds = chatbotService.listProducts().stream().map(p -> p.getId()).sorted().toList();
        assertTrue(productIds.contains("prod-1"));
        assertTrue(productIds.contains("prod-2"));
        assertTrue(productIds.contains("prod-3"));
        assertTrue(productIds.size() >= 3);
    }

    @Test
    void chatFlowContractsAndSignsEachSeedProduct() {
        String[] productIds = {"prod-1", "prod-2", "prod-3"};

        long completedBefore = saleRepository.findByRut(SEEDED_RUT_CANONICAL)
                .stream()
                .filter(sale -> "COMPLETED".equals(sale.getStatus()))
                .count();

        for (String productId : productIds) {
            String selectResponse = chatbotService.processMessage(productId, SEEDED_RUT_CANONICAL);
            assertTrue(selectResponse.toLowerCase().contains(productId));

            String contractResponse = chatbotService.processMessage("contratar " + productId, SEEDED_RUT_CANONICAL);
            assertTrue(contractResponse.contains("saleId:"));
            assertTrue(contractResponse.contains("estado: PENDING"));

            String signResponse = chatbotService.processMessage("firmar Cliente QA", SEEDED_RUT_CANONICAL);
            assertTrue(signResponse.contains("estado: COMPLETED"));
        }

        long completedAfter = saleRepository.findByRut(SEEDED_RUT_CANONICAL)
                .stream()
                .filter(sale -> "COMPLETED".equals(sale.getStatus()))
                .count();

        assertEquals(completedBefore + 3, completedAfter);
    }

    @Test
    void processMessageReturnsGuidanceWhenMessageIsBlank() {
        String response = chatbotService.processMessage("   ", SEEDED_RUT_CANONICAL);
        assertTrue(response.contains("No recibi ningun mensaje"));
        assertTrue(response.contains("productos"));
    }

    @Test
    void contractIntentRecognizesContratacionKeyword() {
        String response = chatbotService.processMessage("contratacion", SEEDED_RUT_CANONICAL);
        assertTrue(response.contains("Indica que producto deseas contratar"));
    }

    @Test
    void signSaleRejectsWhitespaceOnlySignatureAndKeepsSalePending() {
        String saleId = chatbotService.startSale(SEEDED_RUT_CANONICAL, "prod-1");
        assertNotNull(saleId);

        boolean signed = chatbotService.signSale(saleId, "   ");
        assertFalse(signed);

        Sale sale = saleRepository.findById(saleId).orElseThrow();
        assertEquals("PENDING", sale.getStatus());
    }
}
