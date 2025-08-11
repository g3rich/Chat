const token = localStorage.getItem('token');
if (!token) {
    window.location.href = '/login';
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById('logoutBtn').addEventListener('click', logout);
    /*fetchCurrentUser()
        .then(user => {
            if (!user.roles.some(role => role.name === "ROLE_ADMIN")) {
                window.location.href = "/login";
                return;
            }

            document.getElementById("welcome").innerText = `Добро пожаловать, ${user.username}`;
            loadUsers();
        })
        .catch(() => {
            window.location.href = "/login";
        });*/
});

// Получение текущего пользователя
async function fetchCurrentUser() {
    const response = await fetch("/api/users/me", {
        method: "GET",
        credentials: "include"
    });

    if (!response.ok) {
        throw new Error("Unauthorized");
    }

    return await response.json();
}

// Загрузка всех пользователей
async function loadUsers() {
    const userList = document.getElementById("userList");
    userList.innerHTML = "";

    const response = await fetch("/api/users", {
        method: "GET",
        credentials: "include"
    });

    if (!response.ok) {
        userList.innerHTML = "<p>Не удалось загрузить пользователей.</p>";
        return;
    }

    const users = await response.json();

    if (users.length === 0) {
        userList.innerHTML = "<p>Пользователи не найдены.</p>";
        return;
    }

    users.forEach(user => {
        const div = document.createElement("div");
        div.className = "user-entry";
        div.innerText = "id:" + user.userId + "   " + "name:" + user.username;
        div.style.cursor = "pointer";
        div.style.padding = "8px";
        div.style.borderBottom = "1px solid #ccc";

        div.addEventListener("click", () => openChatWithUser(user.id));

        userList.appendChild(div);
    });
}

// Создание приватного чата с выбранным пользователем
async function openChatWithUser(userId) {
    const response = await fetch("/api/chats/private", {
        method: "POST",
        credentials: "include",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({ userId: userId })
    });

    if (!response.ok) {
        alert("Не удалось создать чат");
        return;
    }

    const chat = await response.json();
    window.location.href = `/chat?chatId=${chat.id}`;
}
function logout() {
    fetch('/api/auth/logout', {
        method: 'POST',
        credentials: 'include'
    }).then(() => {
        localStorage.removeItem('token');
        window.location.href = '/login';
    });
}