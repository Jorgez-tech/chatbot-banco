# Chatbot Banco

Proyecto academico de integracion para simular un chatbot bancario con:
- FAQ de productos,
- flujo de contratacion guiado,
- autenticacion por RUT y clave,
- firma digital de contrato al cierre.

## Estructura
- Backend principal: `backend/chatbot`
- Frontend estatico: `backend/chatbot/src/main/resources/static`
- Manual tecnico: `backend/chatbot/docs/manual_codigo.md`
- Manual de usuario: `backend/chatbot/docs/manual_usuario.md`

## Requisitos
- Java 21
- MySQL local
- Base `chatbot` creada en MySQL

## Ejecutar en local (Windows)
Desde la raiz del repositorio:

```powershell
Set-Location .\backend\chatbot
.\mvnw.cmd spring-boot:run
```

Aplicacion disponible en `http://localhost:8081`.

## Pruebas
Desde `backend/chatbot`:

```powershell
.\mvnw.cmd test
```

## Flujo funcional esperado
1. Registro (`/api/register`) o login (`/api/login`).
2. Consulta de productos (`/api/products`) o chat (`/api/chat`).
3. Inicio de venta (`/api/sale/start`) con `rut` y `productId`.
4. Firma digital (`/api/sale/sign`) con `saleId` y `signature`.
5. Consulta de venta (`/api/sale/{saleId}`) o listado (`/api/sales`).

## Productos minimos de contratacion
El sistema mantiene al menos 3 productos semilla:
- `prod-1`: Credito de Consumo
- `prod-2`: Cuenta Vista
- `prod-3`: Tarjeta de Credito
