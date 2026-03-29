package com.chatbot.service;

import com.chatbot.model.Product;
import com.chatbot.model.Sale;
import com.chatbot.model.User;
import com.chatbot.repository.ProductRepository;
import com.chatbot.repository.SaleRepository;
import com.chatbot.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class ChatbotService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    public ChatbotService(UserRepository userRepository, ProductRepository productRepository, SaleRepository saleRepository) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.saleRepository = saleRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostConstruct
    public void init() {
        if (userRepository.count() == 0) {
            resetData();
        }
    }

    public void resetData() {
        saleRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        sessions.clear();

        User u1 = new User("11111111-1", "Juan Perez", "juan@example.com", "+56912345678");
        u1.setPassword(passwordEncoder.encode("Password123"));
        User u2 = new User("11222333-4", "Maria Gomez", "maria@example.com", "+56987654321");
        u2.setPassword(passwordEncoder.encode("Password123"));
        userRepository.saveAll(Arrays.asList(u1, u2));

        Product p1 = new Product("prod-1", "Crédito de Consumo", "Préstamo personal a 12 meses a tasa fija");
        Product p2 = new Product("prod-2", "Cuenta Vista", "Cuenta corriente para manejo diario");
        Product p3 = new Product("prod-3", "Tarjeta de Crédito", "Tarjeta internacional con cupo preaprobado");
        productRepository.saveAll(Arrays.asList(p1, p2, p3));
    }

    public List<Map<String, String>> getFaqs() {
        List<Map<String, String>> faqs = new ArrayList<>();
        faqs.add(Map.of("pregunta", "¿Qué productos ofrecen?", "respuesta", "Crédito, cuenta vista y tarjeta de crédito."));
        faqs.add(Map.of("pregunta", "¿Cómo inicio una venta?", "respuesta", "Solicita contratar y luego usa /api/sale/start."));
        faqs.add(Map.of("pregunta", "¿Cómo firmo contrato?", "respuesta", "Usa /api/sale/sign al finalizar."));
        return faqs;
    }

    public String registerUser(String rut, String password, String name, String email, String phone) {
        if (userRepository.existsById(rut)) {
            return "El usuario con RUT " + rut + " ya existe.";
        }
        if (password == null || password.length() < 8) {
            return "La contraseña debe tener mínimo 8 caracteres.";
        }
        if (!password.matches(".*\\d.*")) {
            return "La contraseña debe contener al menos 1 número.";
        }
        if (!password.matches(".*[A-Z].*")) {
            return "La contraseña debe contener al menos 1 mayúscula.";
        }

        User user = new User(rut, name == null || name.isBlank() ? "Cliente" : name, email, phone);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return "Usuario registrado exitosamente.";
    }

    public String loginUser(String rut, String password) {
        if (rut == null || password == null) {
            return null;
        }
        Optional<User> userOpt = userRepository.findById(rut);
        if (userOpt.isEmpty()) {
            return null;
        }
        if (passwordEncoder.matches(password, userOpt.get().getPassword())) {
            String token = UUID.randomUUID().toString();
            sessions.put(token, rut);
            return token;
        }
        return null;
    }

    public List<Product> listProducts() {
        return productRepository.findAll();
    }

    public String startSale(String rut, String productId) {
        if (rut == null || productId == null) {
            return null;
        }
        if (!userRepository.existsById(rut) || !productRepository.existsById(productId)) {
            return null;
        }
        String saleId = UUID.randomUUID().toString();
        Sale sale = new Sale(saleId, productId, rut, "PENDING", null);
        saleRepository.save(sale);
        return saleId;
    }

    public boolean signSale(String saleId, String signature) {
        if (saleId == null || signature == null || signature.isBlank()) {
            return false;
        }
        Optional<Sale> saleOpt = saleRepository.findById(saleId);
        if (saleOpt.isEmpty()) {
            return false;
        }
        Sale sale = saleOpt.get();
        if ("COMPLETED".equals(sale.getStatus())) {
            return false;
        }
        sale.setSignature(signature);
        sale.setStatus("COMPLETED");
        saleRepository.save(sale);
        return true;
    }

    public List<Sale> listSalesByRut(String rut) {
        if (rut == null || rut.isBlank()) {
            return saleRepository.findAll();
        }
        return saleRepository.findByRut(rut);
    }

    public Sale getSaleForRut(String saleId, String rut) {
        if (saleId == null || rut == null || rut.isBlank()) {
            return null;
        }
        Sale sale = saleRepository.findById(saleId).orElse(null);
        if (sale == null || !rut.equals(sale.getRut())) {
            return null;
        }
        return sale;
    }

    public String processMessage(String mensaje, String rut) {
        if (mensaje == null) {
            return "No recibí ningún mensaje.";
        }
        String m = mensaje.toLowerCase();

        if (m.contains("hola") || m.contains("buenas")) {
            return "Hola, puedo ayudarte con FAQ, productos y proceso de venta.";
        }
        if (m.contains("faq")) {
            return "Puedes consultar FAQ en /api/faq.";
        }
        if (m.contains("producto") || m.contains("productos")) {
            return "Consulta productos en /api/products.";
        }
        if (m.contains("contratar") || m.contains("venta")) {
            return "Para iniciar una venta usa /api/sale/start con rut y productId.";
        }
        if (m.contains("firma") || m.contains("firmar")) {
            return "Para firmar contrato usa /api/sale/sign con saleId y signature.";
        }
        if (m.contains("saldo")) {
            if (rut == null || rut.isBlank()) {
                return "Para consultar saldo primero autentícate y envía tu rut_cliente.";
            }
            return "Validación completada para " + rut + ". Saldo referencial: $1.500.000.";
        }
        return "No entendí tu consulta. Escribe 'faq', 'productos' o 'contratar'.";
    }
}