// public/js/widget.js
document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("message-form");
    const input = document.getElementById("message-input");
    const chatBox = document.getElementById("chat-box");

    if (form) {
        form.addEventListener("submit", async (e) => {
            e.preventDefault();
            const message = input.value.trim();
            if (!message) return;

            await fetch("/widget/send", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify({ message })
            });

            input.value = "";
        });
    }

    // WebSocket/STOMP подписка (пример — при необходимости включим)
});
