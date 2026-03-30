# Manual de Codigo - Chatbot Banco

## 1. Objetivo
Este documento describe la estructura tecnica del proyecto, flujo de datos y puntos clave para mantener o extender el sistema.

## 2. Estructura del Proyecto
- Backend Spring Boot: `backend/chatbot`
- Codigo fuente Java: `src/main/java/com/chatbot`
- Recursos y estaticos: `src/main/resources`
- Pruebas: `src/test/java/com/chatbot`

Paquetes principales:
- `controller`: expone endpoints REST.
- `service`: contiene logica de negocio.
- `model`: entidades JPA (`User`, `Product`, `Sale`).
- `repository`: acceso a datos con Spring Data JPA.
- `dto`: contratos de entrada para login y registro.

## 3. Stack Tecnologico
- Java 21
- Spring Boot 4.0.4
- Spring Data JPA
- MySQL
- Maven Wrapper (`mvnw` / `mvnw.cmd`)
- Frontend estatico (HTML, CSS, JavaScript)

## 4. Configuracion
Archivo: `src/main/resources/application.properties`

Valores actuales de desarrollo local:
- Puerto app: `8081`
- BD: `jdbc:mysql://localhost:3306/chatbot`
- Usuario: `root`
- Password: `mysql`

Notas:
- `spring.sql.init.mode=always` ejecuta `schema.sql` al iniciar.
- `spring.jpa.hibernate.ddl-auto=none` evita autogeneracion de esquema por Hibernate.

## 5. Modelo de Datos
Definido en `src/main/resources/schema.sql`:
- `users(rut PK, name, email, phone, password)`
- `products(id PK, name, description)`
- `sales(id PK, product_id FK, rut FK, status, signature, created_at)`

## 6. Flujo Principal
1. Registro: `POST /api/register`
2. Login: `POST /api/login`
3. Chat: `POST /api/chat`
4. Productos: `GET /api/products`
5. Ventas:
- `POST /api/sale/start`
- `POST /api/sale/sign`
- `GET /api/sale/{saleId}`
- `GET /api/sales`

## 7. Normalizacion de RUT
La logica en servicio normaliza RUT al formato canonico `cuerpo-dv`:
- Quita puntos, espacios y guion.
- Conserva digitos y `K`.
- Reconstruye como `digits-dv`.

Esto permite autenticar con variantes como:
- `11111111-1`
- `11.111.111-1`
- `111111111`

## 8. Seed de Datos
Al iniciar:
- Si no hay usuarios, se ejecuta reinicializacion completa (`resetData`).
- Si ya existe data, se asegura presencia de usuarios y productos seed sin borrar registros existentes.

Credenciales seed:
- RUT: `11111111-1`
- Password: `Password123`

## 9. Pruebas
Pruebas en `src/test/java/com/chatbot`:
- `ChatbotApplicationTests`: carga de contexto Spring.
- `ChatbotServiceIntegrationTests`:
  - Login con formato de RUT alternativo.
  - Persistencia de usuario registrado tras reinicio simulado del servicio.

Ejecutar pruebas:
```bash
./mvnw test
```
En Windows:
```powershell
.\mvnw.cmd test
```

## 10. Convenciones para Cambios
- Mantener cambios minimos y enfocados.
- No romper contratos actuales de endpoints.
- Agregar prueba automatica por cada bug corregido en negocio.
- Validar con `mvnw test` antes de entregar.

## 11. Deuda Tecnica Recomendada
- Implementar autenticacion/autorizacion real para endpoints de negocio.
- Restringir CORS en ambientes no locales.
- Mover credenciales de BD a variables de entorno.
- Evitar exponer endpoint destructivo de reset en produccion.
