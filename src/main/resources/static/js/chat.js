let currentChatId = null;
let stompClient = null;
let currentUserId = null;

const token = localStorage.getItem('token');
if (!token) {
    window.location.href = '/login';
}

document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('logoutBtn').addEventListener('click', logout);

    document.getElementById('participantsCount').addEventListener('click', async () => {
        const responseChat = await fetch(`/api/chats/${currentChatId}`, {
            credentials: 'include'
        });
        if (!responseChat.ok) throw new Error('Failed to fetch chat info');
        const chat = await responseChat.json();
        console.log('Loaded chat:', chat);

        if (chat && chat.group) {
            showParticipantsModal(chat.participants);
        }
    });

    document.querySelector('.close-participants').addEventListener('click', () => {
        document.getElementById('viewParticipantsModal').style.display = 'none';
    });


    connectWebSocket();
    loadChats();

    document.getElementById("fileInput").addEventListener("change", async function (event) {
        const file = event.target.files[0];
        if (!file) return;

        const formData = new FormData();
        formData.append("file", file);
        formData.append("chatId", currentChatId);

        try {
            const response = await fetch("/api/files/upload", {
                method: "POST",
                body: formData
            });

            const result = await response.json();

            if (currentChatId && stompClient) {
                const message = {
                    chatId: currentChatId,
                    content: result.fileUrl,
                    type: 'FILE'
                };

                stompClient.send(`/app/chat/${currentChatId}/send`, {}, JSON.stringify(message));
            }

        } catch (error) {
            console.error("Ошибка загрузки файла:", error);
        }
        loadChats();
    });

    document.getElementById('sendButton').addEventListener('click', sendMessage);
    document.getElementById('messageInput').addEventListener('keypress', (e) => {
        if (e.key === 'Enter') sendMessage();
    });

    const addUserBtn = document.getElementById('addUserBtn');
    const addUserModal = document.getElementById('addUserModal');
    const closeAddModal = document.querySelector('.close-add');
    const addParticipantSearch = document.getElementById('addParticipantSearch');
    const addSearchResults = document.getElementById('addSearchResults');
    const addSelectedParticipants = document.getElementById('addSelectedParticipants');
    const addUsersToChatBtn = document.getElementById('addUsersToChatBtn');

    let selectedUserIdsToAdd = [];

    // Открытие/закрытие модального окна
    addUserBtn.addEventListener('click', () => {
        addUserModal.style.display = 'block';
    });
    closeAddModal.addEventListener('click', () => {
        addUserModal.style.display = 'none';
        selectedUserIdsToAdd = [];
        addSelectedParticipants.innerHTML = '';
        addParticipantSearch.value = '';
        addSearchResults.innerHTML = '';
    });

    // Поиск пользователей
    addParticipantSearch.addEventListener('input', async () => {
        const query = addParticipantSearch.value.trim();
        users = null;
        if (query.length > 2) {
            users = await searchUsers(query);
            displaySearchResults(users);
        } else {
            addSearchResults.innerHTML = '';
        }


        //addSearchResults.innerHTML = '';
        if (users !== null){
            users.forEach(user => {
                if (!selectedUserIdsToAdd.includes(user.id)) {
                    const div = document.createElement('div');
                    div.className = 'search-result-item';
                    div.textContent = user.name;
                    div.addEventListener('click', () => {
                        selectedUserIdsToAdd.push(user.id);
                        const span = document.createElement('span');
                        span.textContent = user.name;
                        addSelectedParticipants.appendChild(span);
                        addSearchResults.innerHTML = '';
                        addParticipantSearch.value = '';
                    });
                    addSearchResults.appendChild(div);
                }
            });
        }
    });

    // Кнопка "Добавить"
    addUsersToChatBtn.addEventListener('click', async () => {
        if (selectedUserIdsToAdd.length === 0 || !currentChatId) {
            alert('Выберите хотя бы одного пользователя.');
            return;
        }

        const response = await fetch(`/api/chats/${currentChatId}/add-users`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ userIds: selectedUserIdsToAdd })
        });

        if (response.ok) {
            alert('Участники добавлены!');
            addUserModal.style.display = 'none';
            selectedUserIdsToAdd = [];
            addSelectedParticipants.innerHTML = '';
        } else {
            alert('Ошибка при добавлении участников');
        }
    });


    const modal = document.getElementById('newChatModal');
    const closeBtn = document.querySelector('.close');
    const createChatBtn = document.getElementById('createChatBtn');
    const participantSearch = document.getElementById('participantSearch');
    const searchResults = document.getElementById('searchResults');
    let selectedUsers = [];
    const isGroupCheckbox = document.getElementById('isGroupChat');
    const selectedParticipants = document.getElementById('selectedParticipants');
    const chatNameInput = document.getElementById('chatName');

    isGroupCheckbox.addEventListener('change', () => {
        const isGroup = isGroupCheckbox.checked;
        document.querySelectorAll('.group-chat-only').forEach(el => {
            el.style.display = isGroup ? 'block' : 'none';
        });
        selectedUsers = [];
        selectedParticipants.innerHTML = '';
    });

    document.getElementById('newChatBtn').addEventListener('click', () => {
        modal.style.display = 'block';
        chatNameInput.value = '';
        participantSearch.value = '';
        searchResults.innerHTML = '';
    });

    closeBtn.addEventListener('click', () => {
        modal.style.display = 'none';
    });

    window.addEventListener('click', (e) => {
        if (e.target === modal) {
            modal.style.display = 'none';
        }
    });

    participantSearch.addEventListener('input', async (e) => {
        const query = e.target.value.trim();
        if (query.length > 2) {
            const users = await searchUsers(query);
            displaySearchResults(users);
        } else {
            searchResults.innerHTML = '';
        }
    });
    function displaySearchResults(users) {
        searchResults.innerHTML = '';
        addSearchResults.innerHTML = '';
        users.forEach(user => {
            const userElement = document.createElement('div');
            userElement.textContent = `${user.name} (${user.username})`;
            userElement.addEventListener('click', () => {
                if (!isGroupCheckbox.checked) {
                    participantSearch.value = `${user.name} (${user.username})`;
                    participantSearch.dataset.userId = user.id;
                    searchResults.innerHTML = '';
                } else {
                    if (!selectedUsers.find(u => u.id === user.id)) {
                        selectedUsers.push(user);
                        updateSelectedParticipants();
                    }
                    participantSearch.value = '';
                    searchResults.innerHTML = '';
                }
            });
            searchResults.appendChild(userElement);
        });
    }

    function updateSelectedParticipants() {
        selectedParticipants.innerHTML = '';
        selectedUsers.forEach(user => {
            const item = document.createElement('div');
            item.textContent = `${user.name} (${user.username})`;
            item.classList.add('participant-tag');
            const removeBtn = document.createElement('span');
            removeBtn.textContent = '×';
            removeBtn.style.marginLeft = '10px';
            removeBtn.style.cursor = 'pointer';
            removeBtn.addEventListener('click', () => {
                selectedUsers = selectedUsers.filter(u => u.id !== user.id);
                updateSelectedParticipants();
            });
            item.appendChild(removeBtn);
            selectedParticipants.appendChild(item);
        });
    }

    createChatBtn.addEventListener('click', async () => {
        const isGroup = isGroupCheckbox.checked;

        if (isGroup) {
            const chatName = chatNameInput.value.trim();
            const userIds = selectedUsers.map(u => u.id);

            if (!chatName || userIds.length === 0) {
                alert('Введите имя чата и выберите хотя бы одного участника.');
                return;
            }

            try {
                const response = await fetch('/api/chats/group', {
                    method: 'POST',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({ chatName: chatName, participantIds: userIds })

                });

                if (!response.ok) throw new Error('Не удалось создать групповой чат');

                const chat = await response.json();
                loadChats();
                loadChat(chat.id, chat.name);
                modal.style.display = 'none';
            } catch (error) {
                alert(error.message);
            }
        } else {
            const userId = participantSearch.dataset.userId;
            const chatName = chatNameInput.value.trim();
            if (!userId) {
                alert('Выберите пользователя для приватного чата');
                return;
            }

            try {
                const response = await fetch(`/api/chats/private/${userId}`, {
                    method: 'POST',
                    credentials: 'include',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ chatName })
                });

                if (!response.ok) throw new Error('Не удалось создать приватный чат');

                const chat = await response.json();
                if (chatName !== chat.name) {
                    await fetch(`/api/chats/${chat.id}/rename`, {
                        method: 'POST',
                        credentials: 'include',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ name: chatName })
                    });
                }

                loadChats();
                loadChat(chat.id, chat.name);
                modal.style.display = 'none';
            } catch (error) {
                alert(error.message);
            }
        }
    });
});


async function loadChats() {
    // Получаем текущего пользователя, если ещё не получен
    if (!currentUserId) {
        const userResponse = await fetch('/api/users/me', { credentials: 'include' });
        if (!userResponse.ok) throw new Error('Failed to get current user');
        const userData = await userResponse.json();
        currentUserId = userData.id;
    }

    const response = await fetch('/api/chats', { credentials: 'include' });
    if (!response.ok) throw new Error('Failed to load chats');
    const chats = await response.json();

    for (const chat of chats) {
        if (!chat.group) {
            const otherParticipant = chat.participants.find(p => p.id !== currentUserId);
            if (otherParticipant) {
                chat.name = otherParticipant.name;
            }
        }
    }

    renderChatList(chats);
}

async function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, async (frame) => {
        console.log('Connected: ' + frame);

        // Получаем список чатов и подписываемся на все
        const response = await fetch('/api/chats', { credentials: 'include' });
        if (!response.ok) throw new Error('Failed to load chats');
        const chats = await response.json();

        chats.forEach(chat => {
            stompClient.subscribe(`/topic/chat/${chat.id}`, (message) => {
                const wsMessage = JSON.parse(message.body);
                handleWebSocketMessage(wsMessage);
            });
        });
    }, (error) => {
        console.error('WebSocket error:', error);
        setTimeout(connectWebSocket, 5000);
    });
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


function renderChatList(chats) {
    const chatList = document.getElementById('chatList');
    chatList.innerHTML = '';

    chats.forEach(chat => {
        const chatElement = document.createElement('div');
        chatElement.className = `chat-item ${chat.hasUnreadMessages ? 'unread' : ''}`;
        //chatElement.dataset.chatId = chat.id;

        // Создаем аватар
        const avatar = document.createElement('div');
        avatar.className = 'avatar';
        const initials = chat.name.split(' ').map(word => word[0]).join('').toUpperCase().substring(0, 2);
        avatar.textContent = initials;

        // Создаем контент чата
        const content = document.createElement('div');
        content.className = 'chat-item-content';

        let lastMessageHtml = '';
        if (chat.lastMessage) {
            const isCurrentUser = chat.lastMessage.sender?.id === currentUserId;
            let messagePrefix = '';

            if (!chat.group) {
                messagePrefix = isCurrentUser ? 'Вы: ' : '';
            } else {
                const senderName = isCurrentUser ? 'Вы' : chat.lastMessage.sender?.name;
                messagePrefix = `${senderName}: `;
            }

            const maxLength = 32;
            let content = chat.lastMessage.content;
            if (chat.lastMessage.type === 'FILE') {
                content = 'Файл';
            } else {
                if ((content.length + messagePrefix.length) > maxLength) {
                    content = content.slice(0, maxLength - 3 - messagePrefix.length) + '...';
                }
            }

            lastMessageHtml = `<p class="message-preview">${messagePrefix}${content}</p>`;
        }

        // Добавляем индикатор с количеством непрочитанных
        const unreadCount = chat.unreadCount > 0 ?
            `<span class="unread-indicator">${chat.unreadCount}</span>` :
            '';

        content.innerHTML = `
            <h3>${chat.name}${unreadCount}</h3>
            ${lastMessageHtml}
        `;

        // Добавляем аватар и контент
        chatElement.appendChild(avatar);
        chatElement.appendChild(content);

        // Обработчик клика
        chatElement.addEventListener('click', () => {
            chatElement.classList.remove('unread');
            const indicator = chatElement.querySelector('.unread-indicator');
            if (indicator) {
                indicator.remove();
            }
            loadChat(chat.id, chat.name);
        });

        chatList.appendChild(chatElement);
    });
}


async function loadChat(chatId, chatName) {
    currentChatId = chatId;
    //let chat;

    try {
        // Если имя не указано, получаем чат и определяем его имя
        if (!chatName) {
            const response = await fetch(`/api/chats/${chatId}`, {
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Failed to fetch chat info');
            const chat = await response.json();
            chatName = chat.name;
        }

        const responseChat = await fetch(`/api/chats/${chatId}`, {
            credentials: 'include'
        });
        if (!responseChat.ok) throw new Error('Failed to fetch chat info');
        const chat = await responseChat.json();
        console.log('Loaded chat:', chat);
        updateAddUserButtonVisibility(chat);
        updateParticipantsCount(chat);

        document.getElementById('chatTitle').textContent = chatName;

        const response = await fetch(`/api/chats/${chatId}/messages`, {
            credentials: 'include'
        });

        if (!response.ok) throw new Error('Failed to load messages');

        const messages = await response.json();
        renderMessages(messages);

        await fetch(`/api/chats/${chatId}/read`, {
            method: 'POST',
            credentials: 'include'
        });

        const chatItems = document.querySelectorAll('.chat-item');
        chatItems.forEach(item => {
            if (item.dataset.chatId === chatId ||
                item.querySelector('h3')?.textContent.includes(chatName)) {
                item.classList.remove('unread');
                const indicator = item.querySelector('.unread-indicator');
                if (indicator) {
                    indicator.remove();
                }
            }
        });
    } catch (error) {
        console.error(error);
    }
}


function renderMessages(messages) {
    const messagesContainer = document.getElementById('messages');
    messagesContainer.innerHTML = '';

    messages.forEach(message => {
        const messageElement = document.createElement('div');
        const isCurrentUser = message.sender.id === currentUserId;
        messageElement.className = `message ${isCurrentUser ? 'message-right' : 'message-left'}`;

        if (message.type === 'FILE') {
            const fileMessage = document.createElement('div');
            fileMessage.className = 'message file-message';

            // Имя отправителя
            const senderElement = document.createElement('div');
            senderElement.className = 'message-sender';
            senderElement.textContent = message.sender.name;

            // Ссылка на файл
            const fileLink = document.createElement('a');
            fileLink.href = message.content;
            fileLink.download = ''; // Можно задать имя файла, если нужно
            fileLink.textContent = '📄 Скачать файл';
            fileLink.className = 'file-download-link';

            // Время отправки
            const timeElement = document.createElement('div');
            timeElement.className = 'message-time';
            timeElement.textContent = new Date(message.sentAt).toLocaleTimeString();

            //fileMessage.appendChild(senderElement);
            fileMessage.appendChild(fileLink);
            //fileMessage.appendChild(timeElement);

            messageElement.appendChild(senderElement);
            messageElement.appendChild(fileMessage);
            messageElement.appendChild(timeElement);
        } else {
            messageElement.innerHTML = `
                <div class="message-sender">${message.sender.name}</div>
                <div class="message-content">${message.content}</div>
                <div class="message-time">${new Date(message.sentAt).toLocaleTimeString()}</div>
            `;
        }

        messagesContainer.appendChild(messageElement);
    });

    messagesContainer.scrollTop = messagesContainer.scrollHeight;
}


function sendMessage() {
    const input = document.getElementById('messageInput');
    const content = input.value.trim();

    if (content && currentChatId && stompClient) {
        const message = {
            message: {
                content: content,
                type: 'TEXT',
                chatId: currentChatId
            },
            action: "SEND"
        };

        //stompClient.send(`/app/chat/${currentChatId}/send`, {}, JSON.stringify(message));

        stompClient.send(`/app/chat/${currentChatId}/send`, {}, JSON.stringify({
            chatId: currentChatId,
            content: content,
            type: 'TEXT'
        }));

        input.value = '';
    }
}

function updateChatPreview(message) {
    const chatItems = document.querySelectorAll('.chat-item');
    let chatElement = null;

    chatItems.forEach(item => {
        if (item.dataset.chatId == message.chatId) {
            chatElement = item;
        }
    });

    const senderName = message.sender.id === currentUserId ? 'Вы' : message.sender.name;
    const previewHTML = `<p><strong>${senderName}:</strong> ${message.content}</p>`;

    if (chatElement) {
        // Обновить содержимое
        chatElement.querySelector('p')?.remove();
        chatElement.insertAdjacentHTML('beforeend', previewHTML);

        // Переместить в начало списка
        const chatList = document.getElementById('chatList');
        chatList.prepend(chatElement);
    } else {
        // Новый чат — перезагрузим список
        loadChats();
    }
}


function handleWebSocketMessage(message) {
    if (message.action === 'SEND' && message.chatId === currentChatId) {
        const messagesContainer = document.getElementById('messages');
        const messageElement = document.createElement('div');
        const isCurrentUser = message.sender.id === currentUserId;
        messageElement.className = `message ${isCurrentUser ? 'message-right' : 'message-left'}`;

        if (message.type === 'FILE') {
            /*const fileLink = document.createElement('a');
            fileLink.href = message.content;

            const isImage = /\.(jpg|jpeg|png|gif|bmp|webp)$/i.test(message.content);
            if (isImage) {
                const img = document.createElement('img');
                img.src = message.content;
                img.alt = 'Файл';
                img.classList.add('chat-image-preview');
                fileLink.appendChild(img);
                messageElement.appendChild(fileLink);
            } else {*/
            const fileMessage = document.createElement('div');
            fileMessage.className = 'message file-message';

                // Ссылка на файл
            const fileLink = document.createElement('a');
            fileLink.href = message.content;
            fileLink.download = ''; // Можно задать имя файла, если нужно
            fileLink.textContent = '📄 Скачать файл';
            fileLink.className = 'file-download-link';
            fileMessage.appendChild(fileLink);
                // Имя отправителя
            const senderElement = document.createElement('div');
            senderElement.className = 'message-sender';
            senderElement.textContent = message.sender.name;


            // Время отправки
            const timeElement = document.createElement('div');
            timeElement.className = 'message-time';
            timeElement.textContent = new Date(message.sentAt).toLocaleTimeString();
            messageElement.appendChild(senderElement);
            messageElement.appendChild(fileMessage);
            messageElement.appendChild(timeElement);
        } else {
            messageElement.innerHTML = `
            <div class="message-sender">${message.sender.name}</div>
            <div class="message-content">${message.content}</div>
            <div class="message-time">${new Date(message.sentAt).toLocaleTimeString()}</div>
        `;
        }
        messagesContainer.appendChild(messageElement);
        messagesContainer.scrollTop = messagesContainer.scrollHeight;
    }
    updateChatPreview(message);
}

// Показываем/скрываем кнопку в зависимости от выбранного чата
function updateAddUserButtonVisibility(chat) {
    const addUserBtn = document.getElementById('addUserBtn');
    if (chat && chat.group) {
        addUserBtn.style.display = 'inline-block';
    } else {
        addUserBtn.style.display = 'none';
    }
}

function updateParticipantsCount(chat) {
    const participantsCountEl = document.getElementById('participantsCount');
    const participantsNumberEl = document.getElementById('participantsNumber');

    if (chat.group) {
        participantsNumberEl.textContent = chat.participants.length;
        participantsCountEl.style.display = 'inline';
    } else {
        participantsCountEl.style.display = 'none';
    }
}

function showParticipantsModal(participants) {
    const modal = document.getElementById('viewParticipantsModal');
    const list = document.getElementById('participantsList');
    list.innerHTML = '';

    participants.forEach(user => {
        const li = document.createElement('li');
        li.textContent = `${user.name} (${user.username})`;
        list.appendChild(li);
    });

    modal.style.display = 'block';
}



async function searchUsers(query) {
    try {
        const response = await fetch(`/api/chats/search?query=${encodeURIComponent(query)}`, {
            credentials: 'include',
        });
        return await response.json();
    } catch (error) {
        console.error('Search error:', error);
        return [];
    }
}