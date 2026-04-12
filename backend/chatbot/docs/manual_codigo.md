# Manual Tecnico - Chatbot Banco

## 1. Alcance
Este backend academico en Spring Boot implementa un flujo bancario basico:
- autenticacion,
- consulta de productos,
- inicio de venta,
- firma digital de contrato,
- consulta de ventas.

La prioridad del proyecto es mantener una base simple, estable y facil de revisar.

## 2. Estructura del backend
Directorio base: `backend/chatbot`

- `src/main/java/com/chatbot/controller/ChatController.java`
  - API REST principal.
- `src/main/java/com/chatbot/controller/ApiExceptionHandler.java`
  - Manejo centralizado de errores de validacion y body invalido.
- `src/main/java/com/chatbot/service/ChatbotService.java`
  - Logica de negocio del chatbot y ventas.
- `src/main/java/com/chatbot/model/*`
  - Entidades JPA: `User`, `Product`, `Sale`.
- `src/main/java/com/chatbot/repository/*`
  - Repositorios JPA.
- `src/main/resources/schema.sql`
  - Esquema SQL.
- `src/main/resources/static/*`
  - Frontend estatico (HTML/CSS/JS).

## 3. Stack
- Java 21
- Spring Boot 4.0.4
- Spring Data JPA
- Spring Validation
- MySQL
- Frontend estatico (sin frameworks)

## 4. Configuracion local
Archivo: `src/main/resources/application.properties`

- Puerto: `8081`
- Base: `chatbot`
- Usuario local por defecto: `root`
- Password local por defecto: `mysql`

Notas:
- `spring.sql.init.mode=always` inicializa esquema.
- `spring.jpa.hibernate.ddl-auto=none` evita generar esquema automatico.

## 5. Modelo de datos
Definido en `schema.sql`:

- `users(rut, name, email, phone, password)`
- `products(id, name, description)`
- `sales(id, product_id, rut, status, signature, created_at)`

Relacion clave:
- `sales.product_id -> products.id`
- `sales.rut -> users.rut`

## 6. Endpoints principales
- `GET /api/faq`
- `POST /api/register`
- `POST /api/login`
- `POST /api/chat`
- `GET /api/products`
- `POST /api/sale/start`
- `POST /api/sale/sign`
- `GET /api/sale/{saleId}`
- `GET /api/sales`
- `POST /api/reset`
- `GET /api/health`

Notas de contrato:
- Se mantiene compatibilidad con el frontend actual.
- En errores de validacion se responde `400` con mensaje claro para el usuario.

## 7. Flujo de venta y firma
1. Cliente autenticado.
2. Selecciona producto (`prod-1`, `prod-2`, `prod-3`).
3. Inicia venta con `rut + productId`.
4. Sistema genera `saleId` con estado `PENDING`.
5. Cliente firma digitalmente (`saleId + signature`).
6. Estado final: `COMPLETED`.

Atajos de uso en chat:
- Contratacion: `contratar prod-1` (o `prod-2`, `prod-3`).
- Firma: `firmar <tu nombre>`.

## 8. Productos semilla minimos
- `prod-1`: Credito de Consumo
- `prod-2`: Cuenta Vista
- `prod-3`: Tarjeta de Credito

El servicio asegura que existan para cumplir el requisito academico de minimo 3 productos contratables.

## 9. Pruebas
Ubicacion: `src/test/java/com/chatbot`

- `ChatbotApplicationTests`: carga de contexto.
- `ChatbotServiceIntegrationTests`: flujo de negocio, normalizacion de RUT y contratacion/firma por 3 productos.
- `ChatControllerIntegrationTests`: contrato REST para inicio/firma de venta y validaciones de entrada.

Ejecucion en Windows:

```powershell
.\mvnw.cmd test
```

## 10. Criterios para cambios
- Cambios pequenos y enfocados.
- No romper contratos API ni frontend.
- Toda correccion funcional debe incluir prueba minima.
- Preferir mensajes claros, formales y amigables para el usuario final.
