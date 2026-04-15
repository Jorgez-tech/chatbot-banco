# Checklist de Entrega - Chatbot Banco

## 1. Chatbot funcional

- [ ] Login funciona con usuario semilla (`11.111.111-1` / `Password123`).
- [ ] Chat responde FAQ y mensajes de ayuda (`FAQ`, `ayuda`, `productos`).
- [ ] El chat orienta el flujo de contratacion y firma con mensajes claros.
- [ ] Los 3 productos existen y son contratables (`prod-1`, `prod-2`, `prod-3`).
- [ ] La venta se inicia por `/api/sale/start` y devuelve `saleId`.
- [ ] La firma se aplica por `/api/sale/sign` y deja estado `COMPLETED`.

## 2. Seguridad basica (alcance academico)

- [ ] Contraseñas almacenadas con hash (BCrypt).
- [ ] Login retorna token de sesion.
- [ ] `/api/sale/start` valida `Authorization: Bearer <token>`.
- [ ] `/api/sale/sign` valida token y propiedad de la venta.

## 3. Pruebas

- [ ] Ejecutar `./mvnw.cmd test` en `backend/chatbot` sin fallos.
- [ ] Verificar prueba E2E REST (login -> sale/start -> sale/sign -> sale/{id}).

## 4. Documentacion

- [ ] Manual tecnico actualizado: `docs/manual_codigo.md`.
- [ ] Manual de usuario actualizado: `docs/manual_usuario.md`.
- [ ] README actualizado: `README.md`.

## 5. Video demostrativo (breve)

- [ ] Mostrar inicio de backend (`./mvnw.cmd spring-boot:run`).
- [ ] Mostrar login exitoso.
- [ ] Mostrar consulta de productos.
- [ ] Mostrar seleccion de producto + contratacion.
- [ ] Mostrar firma digital y estado final `COMPLETED`.
- [ ] Mostrar brevemente que el chat orienta y los botones ejecutan negocio.
