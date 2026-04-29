package com.chatbot.controller;

import com.chatbot.dto.LoginRequest;
import com.chatbot.dto.RegisterRequest;
import com.chatbot.model.Product;
import com.chatbot.model.Sale;
import com.chatbot.service.ChatbotService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatbotService chatbotService;

    @Autowired
    public ChatController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    @GetMapping("/faq")
    public ResponseEntity<List<Map<String, String>>> faqs() {
        return ResponseEntity.ok(chatbotService.getFaqs());
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        if (isBlank(request.getRut())) {
            return badRequest("RUT es requerido");
        }

        if (!request.isPasswordValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", request.getPasswordErrorMessage()));
        }

        String message = chatbotService.registerUser(
                request.getRut().trim(),
                request.getPassword(),
                request.getName(),
                request.getEmail(),
                request.getPhone()
        );

        if (message.contains("ya existe")) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", message));
        }

        if (message.contains("mínimo") || message.contains("número") || message.contains("mayúscula") || message.contains("inválido")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
        }

        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        String token = chatbotService.loginUser(request.getRut(), request.getPassword());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "RUT o contraseña inválidos"));
        }
        return ResponseEntity.ok(Map.of("message", "Autenticación exitosa", "token", token));
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> body) {
        if (body == null) {
            return badRequest("Solicitud inválida");
        }
        String mensaje = body.getOrDefault("mensaje", "");
        String rut = body.get("rut_cliente");
        String respuesta = chatbotService.processMessage(mensaje, rut);
        return ResponseEntity.ok(Map.of("respuesta", respuesta));
    }

    @GetMapping("/products")
    public ResponseEntity<List<Product>> products() {
        return ResponseEntity.ok(chatbotService.listProducts());
    }

    @PostMapping("/sale/start")
    public ResponseEntity<Map<String, String>> startSale(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        if (body == null) {
            return badRequest("Solicitud inválida");
        }
        String rut = body.get("rut");
        String productId = body.get("productId");
        if (isBlank(rut) || isBlank(productId)) {
            return badRequest("Debes indicar el RUT y el ID del producto");
        }

        String token = extractBearerToken(authorization);
        if (token == null || !chatbotService.isTokenValidForRut(token, rut)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sesión inválida o expirada"));
        }

        String saleId = chatbotService.startSale(rut, productId);
        if (saleId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "No se pudo iniciar la venta"));
        }
        return ResponseEntity.ok(Map.of("message", "Venta iniciada", "saleId", saleId));
    }

    @PostMapping("/sale/sign")
    public ResponseEntity<Map<String, String>> signSale(
            @RequestBody Map<String, String> body,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        if (body == null) {
            return badRequest("Solicitud inválida");
        }

        String saleId = body.get("saleId");
        String signature = body.get("signature");
        if (isBlank(saleId) || isBlank(signature)) {
            return badRequest("Debes indicar el ID de venta y la firma");
        }

        String token = extractBearerToken(authorization);
        String rutFromToken = token == null ? null : chatbotService.getRutFromToken(token);
        Sale sale = chatbotService.getSaleById(saleId);
        if (rutFromToken == null || sale == null || !rutFromToken.equals(sale.getRut())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Sesión inválida o expirada"));
        }

        boolean ok = chatbotService.signSale(saleId, signature);
        if (!ok) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Venta no encontrada o ya firmada"));
        }
        return ResponseEntity.ok(Map.of("message", "Firma digital aplicada"));
    }

    @GetMapping("/sale/{saleId}")
    public ResponseEntity<?> sale(@PathVariable String saleId, @RequestParam String rut) {
        Sale sale = chatbotService.getSaleForRut(saleId, rut);
        if (sale == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Venta no encontrada para el RUT indicado"));
        }
        return ResponseEntity.ok(sale);
    }

    @GetMapping("/sales")
    public ResponseEntity<List<Sale>> sales(@RequestParam(required = false) String rut) {
        return ResponseEntity.ok(chatbotService.listSalesByRut(rut));
    }

    @PostMapping("/reset")
    public ResponseEntity<Map<String, String>> reset() {
        chatbotService.resetData();
        return ResponseEntity.ok(Map.of("message", "Datos reiniciados"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    private ResponseEntity<Map<String, String>> badRequest(String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || authorization.isBlank()) {
            return null;
        }

        if (!authorization.startsWith("Bearer ")) {
            return null;
        }

        String token = authorization.substring("Bearer ".length()).trim();
        return token.isBlank() ? null : token;
    }
}