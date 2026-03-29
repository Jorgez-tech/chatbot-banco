package com.chatbot.controller;

import com.chatbot.dto.LoginRequest;
import com.chatbot.dto.RegisterRequest;
import com.chatbot.model.Product;
import com.chatbot.model.Sale;
import com.chatbot.service.ChatbotService;
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

    @PostMapping("/auth")
    public ResponseEntity<Map<String, String>> auth(@RequestBody Map<String, String> body) {
        String token = chatbotService.authenticate(body.get("rut"), body.get("code"));
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Credenciales inválidas"));
        }
        return ResponseEntity.ok(Map.of("message", "Autenticación exitosa", "token", token));
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        if (request.getRut() == null || request.getRut().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "RUT es requerido"));
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

        if (message.contains("mínimo") || message.contains("número") || message.contains("mayúscula")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", message));
        }

        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = chatbotService.loginUser(request.getRut(), request.getPassword());
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "RUT o contraseña inválidos"));
        }
        return ResponseEntity.ok(Map.of("message", "Autenticación exitosa", "token", token));
    }

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> body) {
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
    public ResponseEntity<Map<String, String>> startSale(@RequestBody Map<String, String> body) {
        String rut = body.get("rut");
        String productId = body.get("productId");
        String saleId = chatbotService.startSale(rut, productId);
        if (saleId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "No se pudo iniciar la venta"));
        }
        return ResponseEntity.ok(Map.of("message", "Venta iniciada", "saleId", saleId));
    }

    @PostMapping("/sale/sign")
    public ResponseEntity<Map<String, String>> signSale(@RequestBody Map<String, String> body) {
        boolean ok = chatbotService.signSale(body.get("saleId"), body.get("signature"));
        if (!ok) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Venta no encontrada o ya firmada"));
        }
        return ResponseEntity.ok(Map.of("message", "Firma digital aplicada"));
    }

    @GetMapping("/sale/{saleId}")
    public ResponseEntity<?> sale(@PathVariable String saleId) {
        Sale sale = chatbotService.getSale(saleId);
        if (sale == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Venta no encontrada"));
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

    @GetMapping("/debug/users")
    public ResponseEntity<?> debugUsers() {
        return ResponseEntity.ok(Map.of(
                "total_usuarios", chatbotService.getAllUsers().size(),
                "usuarios", chatbotService.getAllUsers().stream().map(u -> Map.of(
                        "rut", u.getRut(),
                        "name", u.getName(),
                        "email", u.getEmail() != null ? u.getEmail() : "N/A",
                        "phone", u.getPhone() != null ? u.getPhone() : "N/A"
                )).toList()
        ));
    }

    @DeleteMapping("/debug/users/{rut}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable String rut) {
        boolean deleted = chatbotService.deleteUserByRut(rut);
        if (!deleted) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Usuario con RUT " + rut + " no encontrado"));
        }
        return ResponseEntity.ok(Map.of("message", "Usuario con RUT " + rut + " eliminado exitosamente"));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}