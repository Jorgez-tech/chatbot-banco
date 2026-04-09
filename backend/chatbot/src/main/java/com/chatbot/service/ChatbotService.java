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

    private static final String PRODUCT_CONSUMO_ID = "prod-1";
    private static final String PRODUCT_CUENTA_ID = "prod-2";
    private static final String PRODUCT_TARJETA_ID = "prod-3";

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final SaleRepository saleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    private final Map<String, String> sessions = new ConcurrentHashMap<>();
    private final Map<String, ChatState> chatStates = new ConcurrentHashMap<>();

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
            return;
        }

        ensureSeedData();
    }

    private void ensureSeedData() {
        upsertSeedUser("11111111-1", "Juan Perez", "juan@example.com", "+56912345678", "Password123");
        upsertSeedUser("11222333-4", "Maria Gomez", "maria@example.com", "+56987654321", "Password123");

        upsertSeedProduct("prod-1", "Crédito de Consumo", "Préstamo personal a 12 meses a tasa fija");
        upsertSeedProduct("prod-2", "Cuenta Vista", "Cuenta corriente para manejo diario");
        upsertSeedProduct("prod-3", "Tarjeta de Crédito", "Tarjeta internacional con cupo preaprobado");
    }

    private void upsertSeedUser(String rut, String name, String email, String phone, String rawPassword) {
        User user = userRepository.findById(rut).orElse(new User());
        user.setRut(rut);
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(rawPassword));
        userRepository.save(user);
    }

    private void upsertSeedProduct(String id, String name, String description) {
        Product product = productRepository.findById(id).orElse(new Product());
        product.setId(id);
        product.setName(name);
        product.setDescription(description);
        productRepository.save(product);
    }

    public void resetData() {
        saleRepository.deleteAll();
        productRepository.deleteAll();
        userRepository.deleteAll();
        sessions.clear();
        chatStates.clear();

        User u1 = new User("11111111-1", "Juan Perez", "juan@example.com", "+56912345678");
        u1.setPassword(passwordEncoder.encode("Password123"));
        User u2 = new User("11222333-4", "Maria Gomez", "maria@example.com", "+56987654321");
        u2.setPassword(passwordEncoder.encode("Password123"));
        userRepository.saveAll(Arrays.asList(u1, u2));

        Product p1 = new Product(PRODUCT_CONSUMO_ID, "Crédito de Consumo", "Préstamo personal a 12 meses a tasa fija");
        Product p2 = new Product(PRODUCT_CUENTA_ID, "Cuenta Vista", "Cuenta corriente para manejo diario");
        Product p3 = new Product(PRODUCT_TARJETA_ID, "Tarjeta de Crédito", "Tarjeta internacional con cupo preaprobado");
        productRepository.saveAll(Arrays.asList(p1, p2, p3));
    }

    public List<Map<String, String>> getFaqs() {
        List<Map<String, String>> faqs = new ArrayList<>();
        faqs.add(Map.of("pregunta", "¿Qué productos ofrecen?", "respuesta", "Crédito, cuenta vista y tarjeta de crédito."));
        faqs.add(Map.of("pregunta", "¿Cómo inicio una venta?", "respuesta", "Solicita contratar y luego usa /api/sale/start."));
        faqs.add(Map.of("pregunta", "¿Cómo firmo contrato?", "respuesta", "Usa /api/sale/sign al finalizar."));
        return faqs;
    }
    private String normalizeRut(String rut) {
        if (rut == null || rut.isBlank()) {
            return null;
        }
        String compact = rut.trim()
                .replace(".", "")
                .replace(" ", "")
                .replace("-", "")
                .toUpperCase()
                .replaceAll("[^0-9K]", "");

        if (compact.length() < 2) {
            return null;
        }

        String body = compact.substring(0, compact.length() - 1);
        String verifier = compact.substring(compact.length() - 1);
        return body + "-" + verifier;
    }

    public String registerUser(String rut, String password, String name, String email, String phone) {
        String normalizedRut = normalizeRut(rut);
        if (normalizedRut == null) {
            return "RUT inválido.";
        }

        if (userRepository.existsById(normalizedRut)) {
            return "El usuario con RUT " + normalizedRut + " ya existe.";
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

        User user = new User(normalizedRut, name == null || name.isBlank() ? "Cliente" : name, email, phone);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return "Usuario registrado exitosamente.";
    }

    public String loginUser(String rut, String password) {
        if (rut == null || password == null) {
            return null;
        }
        String normalizedRut = normalizeRut(rut);
        if (normalizedRut == null) {
            return null;
        }

        Optional<User> userOpt = userRepository.findById(normalizedRut);
        if (userOpt.isEmpty()) {
            return null;
        }
        if (passwordEncoder.matches(password, userOpt.get().getPassword())) {
            String token = UUID.randomUUID().toString();
            sessions.put(token, normalizedRut);
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
        String normalizedRut = normalizeRut(rut);
        if (normalizedRut == null) {
            return null;
        }

        if (!userRepository.existsById(normalizedRut) || !productRepository.existsById(productId)) {
            return null;
        }
        String saleId = UUID.randomUUID().toString();
        Sale sale = new Sale(saleId, productId, normalizedRut, "PENDING", null);
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
        String normalizedRut = normalizeRut(rut);
        if (normalizedRut == null) {
            return List.of();
        }
        return saleRepository.findByRut(normalizedRut);
    }

    public Sale getSaleForRut(String saleId, String rut) {
        if (saleId == null || rut == null || rut.isBlank()) {
            return null;
        }
        String normalizedRut = normalizeRut(rut);
        if (normalizedRut == null) {
            return null;
        }
        Sale sale = saleRepository.findById(saleId).orElse(null);
        if (sale == null || !normalizedRut.equals(sale.getRut())) {
            return null;
        }
        return sale;
    }

    public String processMessage(String mensaje, String rut) {
        if (mensaje == null || mensaje.isBlank()) {
            return "No recibi ningun mensaje. Escribe 'productos' para ver opciones.";
        }

        String normalizedRut = normalizeRut(rut);
        String m = mensaje.trim().toLowerCase();

        if (m.contains("hola") || m.contains("buenas") || m.contains("inicio")) {
            return "Hola, puedo ayudarte con FAQ, productos y contratacion. Escribe 'productos' para ver los 3 productos disponibles.";
        }

        if (m.contains("faq") || m.contains("preguntas") || m.contains("ayuda")) {
            return "FAQ rapida:\n"
                    + "- Productos: credito de consumo, cuenta vista y tarjeta de credito.\n"
                    + "- Contratacion: escribe 'contratar prod-1', 'contratar prod-2' o 'contratar prod-3'.\n"
                    + "- Firma digital: escribe 'firmar <tu nombre>' para cerrar el contrato.";
        }

        if (m.contains("producto") || m.contains("catalogo") || m.contains("ofrecen")) {
            return formatProductsForChat();
        }

        Product matchedProduct = findProductByQuery(m);
        if (matchedProduct != null) {
            rememberSelectedProduct(normalizedRut, matchedProduct.getId());
            return "Producto seleccionado: " + matchedProduct.getId() + " - " + matchedProduct.getName() + "\n"
                    + matchedProduct.getDescription() + "\n"
                    + "Si deseas continuar, escribe 'contratar " + matchedProduct.getId() + "'.";
        }

        if (m.contains("contratar") || m.contains("venta")) {
            return handleContractIntent(m, normalizedRut, rut);
        }

        if (m.contains("firma") || m.startsWith("firmar")) {
            return handleSignIntent(m, normalizedRut, rut);
        }

        if (m.contains("saldo")) {
            if (normalizedRut == null) {
                return "Para consultar saldo primero autenticate y envia tu rut_cliente.";
            }
            return "Validacion completada para " + normalizedRut + ". Saldo referencial: $1.500.000.";
        }

        return "No entendi tu consulta. Escribe 'productos', un id como 'prod-1' o 'contratar prod-1'.";
    }

    private String handleContractIntent(String message, String normalizedRut, String rawRut) {
        if (normalizedRut == null || !userRepository.existsById(normalizedRut)) {
            return "Para contratar un producto primero inicia sesion con un RUT valido.";
        }

        Product product = extractProductFromMessage(message);
        if (product == null) {
            String selectedProductId = getOrCreateChatState(normalizedRut).selectedProductId;
            if (selectedProductId != null) {
                product = productRepository.findById(selectedProductId).orElse(null);
            }
        }

        if (product == null) {
            return "Indica el producto a contratar. Ejemplos: 'contratar prod-1', 'contratar prod-2', 'contratar prod-3'.";
        }

        String saleId = startSale(normalizedRut, product.getId());
        if (saleId == null) {
            return "No se pudo iniciar la venta. Verifica que el producto exista e intentalo nuevamente.";
        }

        ChatState state = getOrCreateChatState(normalizedRut);
        state.selectedProductId = product.getId();
        state.pendingSaleId = saleId;

        return "Venta iniciada para " + product.getName() + " (" + product.getId() + ").\n"
                + "Resumen de contrato:\n"
                + "- saleId: " + saleId + "\n"
                + "- rut: " + normalizedRut + "\n"
                + "- estado: PENDING\n"
                + "Para firmar digitalmente, escribe: firmar " + (rawRut == null ? "Cliente" : rawRut.trim());
    }

    private String handleSignIntent(String message, String normalizedRut, String rawRut) {
        if (normalizedRut == null || !userRepository.existsById(normalizedRut)) {
            return "Para firmar un contrato primero inicia sesion con un RUT valido.";
        }

        ChatState state = getOrCreateChatState(normalizedRut);
        if (state.pendingSaleId == null) {
            return "No tienes una venta pendiente por firmar. Primero escribe 'contratar prod-1', 'contratar prod-2' o 'contratar prod-3'.";
        }

        String signature = extractSignature(message, rawRut, normalizedRut);
        if (signature == null || signature.isBlank()) {
            return "Debes indicar una firma. Ejemplo: firmar Juan Perez";
        }

        boolean signed = signSale(state.pendingSaleId, signature);
        if (!signed) {
            return "No se pudo aplicar la firma digital. La venta puede no existir o ya estar firmada.";
        }

        Sale signedSale = saleRepository.findById(state.pendingSaleId).orElse(null);
        Product product = signedSale == null ? null : productRepository.findById(signedSale.getProductId()).orElse(null);
        String saleId = state.pendingSaleId;

        state.pendingSaleId = null;

        return "Firma digital aplicada correctamente.\n"
                + "Contrato validado para " + (product == null ? "producto" : product.getName()) + ".\n"
                + "saleId: " + saleId + "\n"
                + "estado: COMPLETED";
    }

    private Product extractProductFromMessage(String message) {
        Product byQuery = findProductByQuery(message);
        if (byQuery != null) {
            return byQuery;
        }

        String lower = message.toLowerCase();
        if (lower.contains(PRODUCT_CONSUMO_ID)) {
            return productRepository.findById(PRODUCT_CONSUMO_ID).orElse(null);
        }
        if (lower.contains(PRODUCT_CUENTA_ID)) {
            return productRepository.findById(PRODUCT_CUENTA_ID).orElse(null);
        }
        if (lower.contains(PRODUCT_TARJETA_ID)) {
            return productRepository.findById(PRODUCT_TARJETA_ID).orElse(null);
        }
        return null;
    }

    private Product findProductByQuery(String query) {
        if (query == null || query.isBlank()) {
            return null;
        }

        String compact = query.trim().toLowerCase();
        List<Product> products = productRepository.findAll();

        for (Product product : products) {
            if (product.getId() != null && product.getId().equalsIgnoreCase(compact)) {
                return product;
            }
        }

        for (Product product : products) {
            String name = product.getName() == null ? "" : product.getName().toLowerCase();
            String description = product.getDescription() == null ? "" : product.getDescription().toLowerCase();
            if (compact.contains(name) || name.contains(compact) || compact.contains(description) || description.contains(compact)) {
                return product;
            }
            if (compact.contains("credito") && (name.contains("credito") || description.contains("prestamo"))) {
                return product;
            }
            if (compact.contains("cuenta") && name.contains("cuenta")) {
                return product;
            }
            if (compact.contains("tarjeta") && name.contains("tarjeta")) {
                return product;
            }
        }

        return null;
    }

    private String formatProductsForChat() {
        List<Product> products = productRepository.findAll();
        if (products.isEmpty()) {
            return "No hay productos disponibles.";
        }

        StringBuilder out = new StringBuilder("Productos disponibles para contratar:\n");
        for (Product p : products) {
            out.append("- ").append(p.getId()).append(" - ").append(p.getName()).append(": ").append(p.getDescription()).append("\n");
        }
        out.append("Selecciona uno escribiendo su id (ejemplo: prod-1).\n");
        out.append("Luego escribe 'contratar <id>' para iniciar la venta y 'firmar <tu nombre>' para cerrar el contrato.");
        return out.toString();
    }

    private String extractSignature(String message, String rawRut, String normalizedRut) {
        String trimmed = message == null ? "" : message.trim();
        String lower = trimmed.toLowerCase();
        if (!lower.startsWith("firmar")) {
            return rawRut != null && !rawRut.isBlank() ? rawRut.trim() : normalizedRut;
        }

        String possible = trimmed.substring("firmar".length()).trim();
        if (!possible.isBlank()) {
            return possible;
        }
        return rawRut != null && !rawRut.isBlank() ? rawRut.trim() : normalizedRut;
    }

    private void rememberSelectedProduct(String normalizedRut, String productId) {
        if (normalizedRut == null) {
            return;
        }
        ChatState state = getOrCreateChatState(normalizedRut);
        state.selectedProductId = productId;
    }

    private ChatState getOrCreateChatState(String normalizedRut) {
        if (normalizedRut == null) {
            return new ChatState();
        }
        return chatStates.computeIfAbsent(normalizedRut, key -> new ChatState());
    }

    private static class ChatState {
        private String selectedProductId;
        private String pendingSaleId;
    }
}


