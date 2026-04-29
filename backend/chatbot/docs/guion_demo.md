# Guion Breve de Demo (3-5 minutos)

## 0. Intro (20s)

"Este proyecto implementa un chatbot bancario academico con FAQ, flujo de venta de 3 productos, autenticacion basica y firma digital de contrato."

## 1. Levantar sistema (30s)

1. Abrir terminal en `backend/chatbot`.
2. Ejecutar `./mvnw.cmd spring-boot:run`.
3. Abrir `http://localhost:8081`.

## 2. Login (30s)

1. Ingresar RUT `11.111.111-1`.
2. Ingresar clave `Password123`.
3. Confirmar acceso al chat.

## 3. Conversacion y guia (60s)

1. Escribir `FAQ`.
2. Escribir `productos`.
3. Escribir `prod-1` para seleccionar.
4. Mencionar que el chat guia, pero la accion de negocio se ejecuta por botones.

## 4. Contratacion y firma (90s)

1. Pulsar `Contratar producto seleccionado`.
2. Mostrar confirmacion de venta iniciada y el ID de venta generado.
3. Pulsar `Firmar contrato pendiente`.
4. Ingresar firma, por ejemplo `Cliente Demo`.
5. Mostrar mensaje de firma aplicada y estado final `COMPLETED`.

## 5. Cierre (20s)

"Se valida el flujo completo con separacion de responsabilidades: chat para guia y endpoints dedicados para negocio (`/api/sale/start`, `/api/sale/sign`)."
