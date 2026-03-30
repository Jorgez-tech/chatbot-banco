# Manual de Usuario - Chatbot Banco

## 1. Objetivo
Este manual explica como usar la aplicacion web del Chatbot Banco para registrarse, iniciar sesion, conversar con el bot y ejecutar flujo basico de ventas.

## 2. Requisitos
- MySQL ejecutandose localmente.
- Base de datos `chatbot` creada.
- Backend iniciado en `http://localhost:8081`.

## 3. Inicio Rapido
1. Ir a la carpeta raiz del backend: `backend/chatbot`.
2. Ejecutar en Windows:
```powershell
.\mvnw.cmd spring-boot:run
```
3. Abrir navegador en:
`http://localhost:8081`

## 4. Acceso con Usuario Seed
Si la base esta limpia, puedes entrar con:
- RUT: `11.111.111-1` (tambien funciona `11111111-1` o `111111111`)
- Contrasena: `Password123`

## 5. Registro de Nuevo Usuario
1. En la pantalla principal, elegir pestana `Registrarse`.
2. Completar campos obligatorios:
- RUT
- Nombre
- Contrasena
3. Regla de contrasena:
- Minimo 8 caracteres
- Al menos 1 numero
- Al menos 1 mayuscula
4. Presionar `Crear cuenta`.
5. Luego iniciar sesion desde la pestana `Iniciar sesion`.

## 6. Uso del Chat
Una vez logueado:
1. Escribir mensaje en el cuadro inferior.
2. Presionar `Enviar`.
3. Consultas sugeridas:
- `faq`
- `productos`
- `contratar`
- `firmar`

## 7. Botones de Control
- `Ver productos`: muestra catalogo disponible.
- `Reiniciar datos`: restaura datos base del servidor (uso de prueba).
- `Salir`: cierra sesion de la interfaz actual.

## 8. Flujo Basico de Venta (API)
Para pruebas tecnicas con herramientas como Postman:
1. Iniciar venta: `POST /api/sale/start`
2. Firmar venta: `POST /api/sale/sign`
3. Consultar venta: `GET /api/sale/{saleId}?rut=...`

## 9. Problemas Frecuentes
### 9.1 RUT o contrasena invalidos
- Verificar formato de RUT correcto.
- Probar con seed: `11.111.111-1` y `Password123`.
- Confirmar que la app apunta a la BD correcta (`chatbot`).

### 9.2 Usuario desaparece
- Revisar que no se haya usado `Reiniciar datos`.
- Evitar conectarse a una base antigua de otra version.
- Validar que MySQL este usando el schema `chatbot`.

### 9.3 No carga la aplicacion
- Confirmar backend arriba en puerto `8081`.
- Revisar consola del backend para errores de conexion a MySQL.

## 10. Recomendaciones de Uso para Entrega
- Mantener una base limpia al preparar demo.
- Probar login seed antes de exponer flujo completo.
- Registrar 1 usuario nuevo y validar login para mostrar persistencia.
