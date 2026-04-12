# Manual de Usuario - Chatbot Banco

## 1. Objetivo
Este manual explica, de forma simple, como utilizar la aplicacion web para:
- iniciar sesion,
- revisar productos,
- contratar un producto,
- firmar digitalmente un contrato.

## 2. Requisitos
- MySQL activo en tu equipo.
- Base de datos `chatbot` creada.
- Backend ejecutandose en `http://localhost:8081`.

## 3. Inicio rapido (Windows)
Desde la raiz del repositorio:

```powershell
Set-Location .\backend\chatbot
.\mvnw.cmd spring-boot:run
```

Luego abre el navegador en `http://localhost:8081`.

## 4. Usuario de prueba
Credenciales semilla:
- RUT: `11.111.111-1`
- Clave: `Password123`

Tambien puedes ingresar el RUT en formatos equivalentes: `11111111-1` o `111111111`.

## 5. Flujo recomendado de uso
1. Inicia sesion.
2. Pulsa `Ver productos`.
3. Escribe en el chat un ID de producto (`prod-1`, `prod-2`, `prod-3`).
4. Pulsa `Contratar producto seleccionado`.
5. Pulsa `Firmar contrato pendiente` e ingresa tu firma.
6. Confirma el cierre con el mensaje `estado: COMPLETED`.

## 6. Productos disponibles
- `prod-1` - Credito de Consumo
- `prod-2` - Cuenta Vista
- `prod-3` - Tarjeta de Credito

## 7. Botones principales
- `Ver productos`: muestra la lista de productos contratables.
- `Contratar producto seleccionado`: inicia la venta del producto activo.
- `Firmar contrato pendiente`: aplica la firma digital al contrato en curso.
- `Reiniciar datos`: restaura los datos de prueba.
- `Salir`: cierra la sesion local en la interfaz.

## 8. Recomendaciones de uso del chat
- Para contratar, utiliza comandos como `contratar prod-1`.
- Para firmar, utiliza `firmar <tu nombre>`.
- Si no recuerdas los pasos, escribe `FAQ` o `ayuda`.

## 9. Problemas frecuentes
### 9.1 No se habilita "Contratar producto seleccionado"
Primero debes seleccionar un producto en el chat con un ID valido (`prod-1`, `prod-2`, `prod-3`).

### 9.2 No se habilita "Firmar contrato pendiente"
Debes iniciar una venta antes de firmar.

### 9.3 Login invalido
Verifica RUT y contraseña. Si lo necesitas, usa `Reiniciar datos` y prueba nuevamente con el usuario demo.
