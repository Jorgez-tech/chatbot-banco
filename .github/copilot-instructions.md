# Instrucciones del proyecto: Chatbot Banco

## Objetivo
Proyecto académico con un backend simple en Java/Spring Boot para simular un chatbot bancario que:
- responde preguntas frecuentes sobre productos,
- guía un flujo básico de venta,
- autentica al cliente antes de cerrar la operación,
- registra una firma digital al final como validación del contrato.

## Stack actual
- **Backend:** Java 21 + Spring Boot
- **Persistencia:** JPA + MySQL
- **Frontend:** HTML/CSS/JS estático en `src/main/resources/static`
- **Puerto local:** `8081`

## Estructura clave
- `src/main/java/com/chatbot/controller/ChatController.java`: expone la API REST
- `src/main/java/com/chatbot/service/ChatbotService.java`: lógica de negocio del chatbot
- `src/main/java/com/chatbot/model/*`: entidades `User`, `Product`, `Sale`
- `src/main/resources/schema.sql`: esquema e inicialización de datos
- `src/test/java/com/chatbot/*`: pruebas de contexto e integración
- `docs/manual_codigo.md`: documentación técnica
- `docs/manual_usuario.md`: manual de uso

## Flujo funcional básico
1. Registro de usuario
2. Login y autenticación
3. Consulta de FAQs / productos
4. Inicio de venta con `rut` + `productId`
5. Firma digital con `saleId` + `signature`
6. Consulta de venta o listado de ventas por RUT

## Endpoints principales
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

## Convenciones para cambios
- Mantener todo **lo más básico posible**.
- Evitar sobreingeniería, capas extra o dependencias innecesarias.
- No romper contratos actuales de API ni el frontend existente.
- Si se corrige un bug funcional, agregar una prueba mínima que lo cubra.
- Preferir cambios pequeños y fáciles de revisar.

## Reglas prácticas
- En Windows, ejecutar desde `backend/chatbot` con:
  - `.
  mvnw.cmd spring-boot:run`
  - `.
mvnw.cmd test`
- Mantener la semilla de datos y el esquema de `schema.sql` coherentes con los tests.
- Si se toca autenticación, normalización de RUT o ventas, revisar también las pruebas de integración.

## Estilo recomendado
- Código simple, directo y legible.
- Mensajes claros para el usuario final en español.
- Evitar cambios amplios en nombres, contratos o estructura salvo necesidad real.

## Nota de alcance
Este proyecto está pensado como demo académica. La prioridad es que el flujo completo funcione de forma estable y entendible, no construir una arquitectura compleja.

