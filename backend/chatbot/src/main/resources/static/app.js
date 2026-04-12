// ===== ELEMENTOS DEL DOM =====
const authContainer = document.getElementById('auth-container');
const chatContainer = document.getElementById('chat-container');

// Login
const loginRut = document.getElementById('login-rut');
const loginPassword = document.getElementById('login-password');
const btnLogin = document.getElementById('btn-login');
const loginError = document.getElementById('login-error');

// Register
const registerRut = document.getElementById('register-rut');
const registerName = document.getElementById('register-name');
const registerEmail = document.getElementById('register-email');
const registerPhone = document.getElementById('register-phone');
const registerPassword = document.getElementById('register-password');
const btnRegister = document.getElementById('btn-register');
const registerError = document.getElementById('register-error');

// Chat
const chatMessages = document.getElementById('chat-messages');
const userInput = document.getElementById('user-input');
const sendBtn = document.getElementById('send-btn');
const productsBtn = document.getElementById('btn-products');
const buyBtn = document.getElementById('btn-buy');
const signBtn = document.getElementById('btn-sign');
const resetBtn = document.getElementById('btn-reset');
const logoutBtn = document.getElementById('btn-logout');
const actionsHint = document.getElementById('actions-hint');

// ===== ESTADO GLOBAL =====
let currentUser = null;
let currentToken = null;
let selectedProductId = null;
let pendingSaleId = null;

function apiErrorMessage(data, fallback) {
  if (!data) return fallback;
  if (data.detail) return `${data.message || fallback} (${data.detail})`;
  return data.message || fallback;
}

async function requestJson(url, options = {}) {
  const response = await fetch(url, options);
  let data = null;
  try {
    data = await response.json();
  } catch (_) {
    data = null;
  }
  return { response, data };
}

// ===== FORMATEO DE RUT =====
function formatRUT(value) {
  // Remove all non-digit and 'k' characters
  value = value.replace(/[^\d kK]/g, '').toUpperCase();
  
  // Keep only first 8 digits and 1 letter
  if (value.length > 9) {
    value = value.substring(0, 9);
  }
  
  let formatted = '';
  let cleanValue = value.replace(/\D/g, '').slice(0, 8);
  
  if (cleanValue.length === 0) {
    formatted = '';
  } else if (cleanValue.length <= 1) {
    formatted = cleanValue;
  } else if (cleanValue.length <= 4) {
    formatted = cleanValue.slice(0, 1) + '.' + cleanValue.slice(1);
  } else if (cleanValue.length <= 7) {
    formatted = cleanValue.slice(0, 1) + '.' + 
                cleanValue.slice(1, 4) + '.' + 
                cleanValue.slice(4);
  } else {
    formatted = cleanValue.slice(0, 2) + '.' + 
                cleanValue.slice(2, 5) + '.' + 
                cleanValue.slice(5, 8);
  }
  
  // Add letter if present
  if (value.length > 8) {
    formatted += '-' + value[8];
  }
  
  return formatted;
}

// Event listeners for RUT input formatting
loginRut.addEventListener('input', (e) => {
  e.target.value = formatRUT(e.target.value);
});

registerRut.addEventListener('input', (e) => {
  e.target.value = formatRUT(e.target.value);
});

// ===== VALIDACIÓN DE CONTRASEÑA EN CLIENTE =====
function validatePassword(password) {
  return {
    length: password.length >= 8,
    number: /\d/.test(password),
    uppercase: /[A-Z]/.test(password)
  };
}

registerPassword.addEventListener('input', (e) => {
  const pwd = e.target.value;
  const validation = validatePassword(pwd);
  
  document.getElementById('req-length').className = validation.length ? 'requirement valid' : 'requirement invalid';
  document.getElementById('req-number').className = validation.number ? 'requirement valid' : 'requirement invalid';
  document.getElementById('req-uppercase').className = validation.uppercase ? 'requirement valid' : 'requirement invalid';
});

// ===== CAMBIO DE TAB =====
function switchTab(tab) {
  const loginForm = document.getElementById('login-form');
  const registerForm = document.getElementById('register-form');
  const tabLogin = document.getElementById('tab-login');
  const tabRegister = document.getElementById('tab-register');
  
  if (tab === 'login') {
    loginForm.classList.add('active');
    registerForm.classList.remove('active');
    tabLogin.classList.add('active');
    tabRegister.classList.remove('active');
    loginError.textContent = '';
  } else {
    registerForm.classList.add('active');
    loginForm.classList.remove('active');
    tabRegister.classList.add('active');
    tabLogin.classList.remove('active');
    registerError.textContent = '';
  }
}

// ===== LOGIN =====
btnLogin.addEventListener('click', async () => {
  const rut = loginRut.value.trim();
  const password = loginPassword.value;
  
  if (!rut || !password) {
    loginError.textContent = 'Por favor completa todos los campos';
    return;
  }
  
  try {
    const { response, data } = await requestJson('/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ rut, password })
    });

    if (response.ok && data.token) {
      currentToken = data.token;
      currentUser = rut;
      loginError.textContent = '';
      showChatUI();
      appendMessage('bot', `¡Bienvenido ${rut}! ¿En qué puedo ayudarte?`);
    } else {
      loginError.textContent = apiErrorMessage(data, 'Error en la autenticación');
    }
  } catch (error) {
    loginError.textContent = 'Error de conexión con el servidor';
  }
});

// ===== REGISTRO =====
btnRegister.addEventListener('click', async () => {
  const rut = registerRut.value.trim();
  const password = registerPassword.value;
  const name = registerName.value.trim();
  const email = registerEmail.value.trim();
  const phone = registerPhone.value.trim();
  
  if (!rut || !password || !name) {
    registerError.textContent = 'Por favor completa RUT, nombre y contraseña';
    return;
  }
  
  const validation = validatePassword(password);
  if (!validation.length || !validation.number || !validation.uppercase) {
    registerError.textContent = 'La contraseña no cumple los requisitos';
    return;
  }
  
  try {
    const { response, data } = await requestJson('/api/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ rut, password, name, email, phone })
    });

    if (response.ok) {
      registerError.textContent = '';
      // Limpia formulario y cambia a login
      registerRut.value = '';
      registerName.value = '';
      registerEmail.value = '';
      registerPhone.value = '';
      registerPassword.value = '';
      loginRut.value = rut;
      loginPassword.value = '';
      switchTab('login');
      loginError.textContent = '✓ Registro exitoso. Ahora inicia sesión.';
    } else {
      registerError.textContent = apiErrorMessage(data, 'Error en el registro');
    }
  } catch (error) {
    registerError.textContent = 'Error de conexión con el servidor';
  }
});

// ===== LOGOUT =====
logoutBtn.addEventListener('click', () => {
  currentUser = null;
  currentToken = null;
  selectedProductId = null;
  pendingSaleId = null;
  chatContainer.style.display = 'none';
  authContainer.style.display = 'block';
  loginRut.value = '';
  loginPassword.value = '';
  chatMessages.innerHTML = '';
  loginError.textContent = '';
});

// ===== UI CHAT =====
function showChatUI() {
  authContainer.style.display = 'none';
  chatContainer.style.display = 'flex';
  updateActionButtons();
}

function appendMessage(sender, text) {
  const div = document.createElement('div');
  div.className = `message ${sender}-message`;
  div.innerText = text;
  chatMessages.appendChild(div);
  chatMessages.scrollTop = chatMessages.scrollHeight;
}

async function sendMessageText(text) {
  appendMessage('user', text);
  try {
    const { response, data } = await requestJson('/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ mensaje: text, rut_cliente: currentUser || null })
    });

    if (!response.ok) {
      appendMessage('bot', apiErrorMessage(data, 'No se pudo procesar tu mensaje.'));
      return;
    }
    appendMessage('bot', data.respuesta);
    syncChatStateFromResponse(data.respuesta);
  } catch (error) {
    appendMessage('bot', 'Error de conexión con el servidor.');
  }
}

function syncChatStateFromResponse(responseText) {
  if (!responseText) {
    return;
  }

  const productMatch = responseText.match(/\bprod-[1-9]\b/i);
  if (productMatch) {
    selectedProductId = productMatch[0].toLowerCase();
  }

  const saleMatch = responseText.match(/saleId:\s*([a-zA-Z0-9\-]+)/i);
  if (saleMatch) {
    pendingSaleId = saleMatch[1];
  }

  if (responseText.includes('estado: COMPLETED')) {
    pendingSaleId = null;
  }

  updateActionButtons();
}

function updateActionButtons() {
  buyBtn.disabled = !selectedProductId;
  signBtn.disabled = !pendingSaleId;

  if (!selectedProductId) {
    buyBtn.title = 'Primero selecciona un producto escribiendo su ID (prod-1, prod-2 o prod-3).';
  } else {
    buyBtn.title = `Listo para contratar ${selectedProductId}.`;
  }

  if (!pendingSaleId) {
    signBtn.title = 'Primero inicia una venta para habilitar la firma digital.';
  } else {
    signBtn.title = `Contrato pendiente detectado (${pendingSaleId}).`;
  }

  if (pendingSaleId) {
    actionsHint.textContent = `Paso 3: firma el contrato pendiente (${pendingSaleId}) con el boton "Firmar contrato pendiente".`;
    return;
  }

  if (selectedProductId) {
    actionsHint.textContent = `Paso 2: producto ${selectedProductId} seleccionado. Ahora presiona "Contratar producto seleccionado".`;
    return;
  }

  actionsHint.textContent = 'Paso 1: revisa los productos y selecciona uno (ej: prod-1).';
}

sendBtn.addEventListener('click', () => {
  const text = userInput.value.trim();
  if (!text) return;
  userInput.value = '';
  sendMessageText(text);
});

userInput.addEventListener('keypress', function(e) {
  if (e.key === 'Enter') sendBtn.click();
});

productsBtn.addEventListener('click', async () => {
  appendMessage('user', 'Ver productos');
  try {
    const { response, data } = await requestJson('/api/products');
    if (!response.ok) {
      appendMessage('bot', apiErrorMessage(data, 'No se pudo obtener la lista de productos.'));
      return;
    }

    const products = data || [];
    if (!products.length) appendMessage('bot', 'No hay productos disponibles.');
    else {
      let out = 'Productos disponibles:\n';
      products.forEach(p => out += `${p.id} - ${p.name}: ${p.description}\n`);
      appendMessage('bot', out);
      appendMessage('bot', 'Escribe el ID del producto (ej: prod-1) para seleccionarlo y luego usa el botón "Contratar producto seleccionado".');
    }
  } catch (err) {
    appendMessage('bot', 'No se pudo obtener la lista de productos.');
  }
});

buyBtn.addEventListener('click', () => {
  if (!selectedProductId) {
    appendMessage('bot', 'Primero selecciona un producto escribiendo su ID (prod-1, prod-2, prod-3).');
    return;
  }
  sendMessageText(`contratar ${selectedProductId}`);
});

signBtn.addEventListener('click', () => {
  if (!pendingSaleId) {
    appendMessage('bot', 'No hay contrato pendiente por firmar. Primero inicia una contratación.');
    return;
  }
  const signature = window.prompt('Ingresa tu firma digital (ejemplo: Nombre Apellido)');
  if (!signature || !signature.trim()) {
    appendMessage('bot', 'Firma cancelada.');
    return;
  }
  sendMessageText(`firmar ${signature.trim()}`);
});

resetBtn.addEventListener('click', async () => {
  try {
    const { response, data } = await requestJson('/api/reset', { method: 'POST' });
    if (!response.ok) {
      appendMessage('bot', apiErrorMessage(data, 'No fue posible reiniciar los datos.'));
      return;
    }
    selectedProductId = null;
    pendingSaleId = null;
    updateActionButtons();
    appendMessage('bot', data.message || 'Datos reiniciados en el servidor.');
  } catch (err) {
    appendMessage('bot', 'No fue posible reiniciar los datos.');
  }
});

// ===== INICIO =====
window.onload = () => {
  // La pantalla de login ya está visible
  updateActionButtons();
};

