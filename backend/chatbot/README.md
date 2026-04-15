# Chatbot Banco - Backend

Backend academico en Spring Boot para una demo de chatbot bancario con:

- registro e inicio de sesion,
- catalogo de productos,
- contratacion guiada,
- firma digital de contratos,
- consultas de ventas.

## Requisitos

- Java 21
- MySQL local
- Base de datos `chatbot` creada

## Ejecucion

Desde esta carpeta (`backend/chatbot`):

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicacion queda disponible en `http://localhost:8081`.

## Pruebas

```powershell
.\mvnw.cmd test
```

## Flujo funcional recomendado

1. Registro (`/api/register`) o login (`/api/login`).
2. Consulta de productos (`/api/products`) o chat (`/api/chat`).
3. Inicio de venta (`/api/sale/start`) con `rut` y `productId`.
4. Firma digital (`/api/sale/sign`) con `saleId` y `signature`.
5. Consulta de venta (`/api/sale/{saleId}`) o listado (`/api/sales`).

## Comandos utiles en el chat

- Ver opciones: `productos`
- Guiar contratacion: `contratar ...` (el chat orienta; la accion se ejecuta con el boton de contratacion)
- Guiar firma: `firmar ...` (el chat orienta; la firma se ejecuta con el boton de firma)
- Ayuda general: `FAQ` o `ayuda`

## Nota de responsabilidades

- `/api/chat` se usa para conversacion y guia.
- `/api/sale/start` y `/api/sale/sign` son la via de negocio para iniciar/finalizar ventas.

## Documentacion

- Tecnica: `docs/manual_codigo.md`
- Usuario: `docs/manual_usuario.md`
