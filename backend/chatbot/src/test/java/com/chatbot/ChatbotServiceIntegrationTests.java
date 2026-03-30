package com.chatbot;

import com.chatbot.repository.ProductRepository;
import com.chatbot.repository.SaleRepository;
import com.chatbot.repository.UserRepository;
import com.chatbot.service.ChatbotService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
        if (userRepository.existsById(TEST_RUT_CANONICAL)) {
            userRepository.deleteById(TEST_RUT_CANONICAL);
        }
    }

    @AfterEach
    void cleanup() {
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
}
