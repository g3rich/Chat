function loadWebSocketDependencies() {
    return new Promise((resolve, reject) => {
        if (window.SockJS && window.Stomp) {
            resolve();
            return;
        }

        const sockjsScript = document.createElement('script');
        sockjsScript.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js';

        const stompScript = document.createElement('script');
        stompScript.src = 'https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js';

        let loaded = 0;
        const checkLoaded = () => {
            if (++loaded === 2 && window.SockJS && window.Stomp) {
                resolve();
            }
        };

        sockjsScript.onload = checkLoaded;
        sockjsScript.onerror = () => reject(new Error('Failed to load SockJS'));

        stompScript.onload = checkLoaded;
        stompScript.onerror = () => reject(new Error('Failed to load Stomp'));

        document.head.appendChild(sockjsScript);
        document.head.appendChild(stompScript);
    });
}
let currentUserId = null

document.addEventListener('DOMContentLoaded', async () => {
    try {
        await loadWebSocketDependencies();
    } catch (error) {
        console.error('Failed to load WebSocket dependencies:', error);
        alert('Не удалось загрузить компоненты чата. Пожалуйста, обновите страницу.');
        return;
    }

    const button = document.createElement('button');
    button.id = 'clientChatButton';
    button.textContent = '💬';
    document.body.appendChild(button);

    const chatWindow = document.createElement('div');
    chatWindow.style = "display:none";
    chatWindow.id = 'clientChatWindow';
    chatWindow.innerHTML = `
        <header>Онлайн-консультант</header>
        <div id="clientMessages"></div>
        <div id="clientNameInput">
            <input type="text" id="clientName" placeholder="Ваше имя..." />
            <button id="startChatBtn">Начать</button>
        </div>
        <form id="clientInputForm" style="display:none;">
            <input type="text" id="clientMsgInput" placeholder="Сообщение..." />
            <button>▶</button>
        </form>
    `;
    document.body.appendChild(chatWindow);

    let clientData = null;
    let stompClient = null;

    button.addEventListener('click', () => {
        chatWindow.style.display = chatWindow.style.display === 'none' ? 'flex' : 'none';
    });

    document.getElementById('startChatBtn').addEventListener('click', async () => {
        const name = document.getElementById('clientName').value.trim();
        if (!name) return;

        try {
            const response = await fetch(`/api/widget/start?name=${encodeURIComponent(name)}`, {
                method: 'POST',
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            clientData = await response.json();
            console.log('Получены данные:', clientData);
            currentUserId = clientData.client.id;

            document.getElementById('clientNameInput').style.display = 'none';
            document.getElementById('clientInputForm').style.display = 'flex';

            // Инициализация WebSocket
            initWebSocket();
        } catch (error) {
            console.error('Error starting chat:', error);
            alert('Ошибка при запуске чата');
        }
    });

    function initWebSocket() {
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, async (frame) => {
            console.log('WebSocket подключен');

            stompClient.subscribe(`/topic/chat/${clientData.chat.id}`, (message) => {
                const msg = JSON.parse(message.body);
                displayMessage(msg, false);
            });
        }, function(error) {
            console.error('Ошибка подключения WebSocket:', error);
            alert('Ошибка подключения к чату');
        });
    }

    function displayMessage(message, isOwn) {
        const senderName = message.sender.id === currentUserId ? 'Вы' : 'Консультант';

        const messages = document.getElementById('clientMessages');
        const msgElem = document.createElement('div');
        msgElem.textContent = `${senderName}: ${message.content}`;
        msgElem.className = `message ${message.sender.id === currentUserId ? 'message-right' : 'message-left'}`;
        messages.appendChild(msgElem);
        messages.scrollTop = messages.scrollHeight;
    }

    document.getElementById('clientInputForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        const input = document.getElementById('clientMsgInput');
        const content = input.value.trim();
        if (!content || !stompClient) return;

        try {
            // Отправка через WebSocket
            const message = {
                message: {
                    content: content,
                    type: 'TEXT',
                    chatId: clientData.chat.id
                },
                action: "SEND"
            };

            stompClient.send(`/app/chat/${clientData.chat.id}/send`, {}, JSON.stringify({
                chatId: clientData.chat.id,
                content: content,
                sender: clientData.client,
                type: 'TEXT'
            }));

            /*displayMessage({
                sender: clientData.client,
                content: content
            }, true);*/

            input.value = '';
        } catch (error) {
            console.error('Error sending message:', error);
        }
    });
});