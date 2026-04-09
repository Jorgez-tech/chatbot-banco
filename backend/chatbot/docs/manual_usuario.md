# Manual de Usuario - Chatbot Banco

## 1. Objetivo
Guiar el uso de la aplicacion web para:
- autenticarse,
- revisar productos,
- contratar un producto,
- firmar digitalmente el contrato.

## 2. Requisitos
- MySQL activo en local.
- Base `chatbot` creada.
- Backend corriendo en `http://localhost:8081`.

## 3. Inicio rapido (Windows)
Desde la raiz del repositorio:

```powershell
Set-Location .\backend\chatbot
.\mvnw.cmd spring-boot:run
```

Abrir navegador en `http://localhost:8081`.

## 4. Usuario demo
Credenciales semilla:
- RUT: `11.111.111-1`
- Clave: `Password123`

Tambien funciona el RUT en formatos equivalentes (`11111111-1` o `111111111`).

## 5. Flujo recomendado en pantalla
1. Inicia sesion.
2. Pulsa `Ver productos`.
3. Escribe un ID de producto en el chat (`prod-1`, `prod-2`, `prod-3`).
4. Pulsa `Contratar producto seleccionado`.
5. Pulsa `Firmar contrato pendiente` e ingresa tu firma.
6. Verifica confirmacion con `estado: COMPLETED`.

## 6. Productos disponibles
- `prod-1` - Credito de Consumo
- `prod-2` - Cuenta Vista
- `prod-3` - Tarjeta de Credito

## 7. Botones de control
- `Ver productos`: lista productos contratables.
- `Contratar producto seleccionado`: inicia venta del producto activo.
- `Firmar contrato pendiente`: aplica firma digital al contrato pendiente.
- `Reiniciar datos`: vuelve al estado base de prueba.
- `Salir`: cierra sesion local de la interfaz.

## 8. Problemas frecuentes
### 8.1 No se habilita "Contratar producto seleccionado"
Primero escribe en el chat un ID valido (`prod-1`, `prod-2`, `prod-3`).

### 8.2 No se habilita "Firmar contrato pendiente"
Debes iniciar una venta antes de firmar.

### 8.3 Login invalido
Revisa RUT y clave. Si es necesario, usa `Reiniciar datos` y prueba el usuario demo.
