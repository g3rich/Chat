// Единый обработчик для всей страницы
document.addEventListener('DOMContentLoaded', function() {
    checkAuthStatus();
    setupEventListeners();
});

function checkAuthStatus() {
    const token = localStorage.getItem('token');

    // Если пользователь уже авторизован и на странице входа/регистрации - перенаправляем
    if (token && (window.location.pathname === '/login' || window.location.pathname === '/register')) {
        window.location.href = '/chat';
    }
    // Если пользователь не авторизован и на странице чата - перенаправляем
    else if (!token && window.location.pathname === '/chat') {
        window.location.href = '/login';
    }
}

function setupEventListeners() {
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', handleLogin);
    }

    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        registerForm.addEventListener('submit', handleRegister);
    }
}

async function handleLogin(e) {
    e.preventDefault();
    const username = document.getElementById('username').value.trim();
    const password = document.getElementById('password').value.trim();

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Login failed');
        }

        // Сохраняем токен и перенаправляем с ним в URL
        const token = data.token;
        localStorage.setItem('token', token);
        window.location.href = `/home`;
    } catch (error) {
        showError(error.message);
        console.error('Login error:', error);
    }
}

async function handleRegister(e) {
    e.preventDefault();
    const username = document.getElementById('username').value.trim();
    const name = document.getElementById('name').value.trim();
    const password = document.getElementById('password').value.trim();

    if (!username || !name || !password) {
        showError('Please fill in all fields');
        return;
    }

    try {
        const response = await fetch('/api/auth/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ username, name, password })
        });

        const data = await response.json();

        if (!response.ok) {
            throw new Error(data.message || 'Регистрация провалена!');
        }

        alert('Регистрация прошла успешно. Пожалуйста, теперь авторизуйтесь.');
        window.location.href = '/login';
    } catch (error) {
        showError(error.message);
    }
}

function showError(message) {
    const errorElement = document.getElementById('error-message');
    if (errorElement) {
        errorElement.textContent = message;
        setTimeout(() => {
            errorElement.textContent = '';
        }, 5000);
    } else {
        alert(message);
    }
}
