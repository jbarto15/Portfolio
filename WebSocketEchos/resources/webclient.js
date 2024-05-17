function mainFunction() {
    // Create a web socket
    let ws = new WebSocket("ws://localhost:8080");
    ws.onopen = handleConnect;
    let wsOpen = false;
    ws.onclose = handleClose;
    ws.onerror = handleError;
    ws.onmessage = handleMessage;
    // ws.close(); // When we are completely done.

    // Variables to store the username, room, and message
    let usernameInput = document.getElementById("usernameValue");
    let roomInput = document.getElementById("roomValue");
    let messageInput = document.getElementById("inputMessage");
    usernameInput.addEventListener("keypress", handleUserNameAndRoom);
    roomInput.addEventListener("keypress", handleUserNameAndRoom);
    messageInput.addEventListener("keypress", handleSendMessage);

    // Get the div section for "people in room"
    let peopleInRoom = document.getElementById("peopleInRoom");
    // Get the div section for "message center"
    let messageCenter = document.getElementById("messageCenter");


    // Get the join button
    let joinButton = document.getElementById("join");
    joinButton.onclick = function (event) {
        let roomNameValid = true;
        let room = roomInput.value;
        for (let i = 0; i < room.length; i++) {
            if (room[i] < 'a' || room[i] > 'z') {
                roomNameValid = false;
            }
        }

        if (!roomNameValid)
            alert("Room must be all lowercase letter.");


        if (roomNameValid) {
            let username = usernameInput.value;
            let message = messageInput.value;
            // If the websocket is open, send the username and room to the server
            if (wsOpen) {
                // Create JSON object for the join
                let jsonJoin = {
                    "type": "join",
                    "room": roomInput.value,
                    "user": usernameInput.value,
                }
                // Send the JSON join
                ws.send(JSON.stringify(jsonJoin));

            } else {
                message.value = "Could not open the websocket!";
            }
        }
    }

    // Get the send button
    let sendButton = document.getElementById("sendButton");
    sendButton.onclick = function (event) {
        let message = messageInput.value;
        // If the websocket is open, send the message to the server
        if (wsOpen) {
            let jsonMessage = {
                "type": "message",
                "user": usernameInput.value,
                "room": roomInput.value,
                "message": message
            }
            ws.send(JSON.stringify(jsonMessage));

        } else {
            message.value = "Could not open the websocket!";
        }
    }

    // Get the leave button
    let leaveButton = document.getElementById("leaveButton");
    leaveButton.onclick = function (event) {
        let message = messageInput.value;
        // If the websocket is open, send the message to the server
        if (wsOpen) {
            // Create a leave JSON object
            let jsonLeave = {
                "type": "leave",
                "room": roomInput.value,
                "user": usernameInput.value,
            }
            // Send the JSON join
            ws.send(JSON.stringify(jsonLeave));

        } else {
            message.value = "Could not open the websocket!";
        }
    }


    // Function that gets the username and room and stores them in variables, then sends the information to the server
    function handleUserNameAndRoom(event) {
        if (event.code === "Enter") {
            let roomNameValid = true;
            let room = roomInput.value;

            for (let i = 0; i < room.length; i++) {
                console.log(room[i]);
                if (room[i] < 'a' || room[i] > 'z') {
                    roomNameValid = false;
                }
            }

            if (!roomNameValid) {
                alert("Room must be all lowercase letter.");
            }


            if (roomNameValid) {
                let username = usernameInput.value;
                let message = messageInput.value;
                // If the websocket is open, send the username and room to the server
                if (wsOpen) {
                    // Create JSON object for the join
                    let jsonJoin = {
                        "type": "join",
                        "room": roomInput.value,
                        "user": usernameInput.value,
                    }
                    // Send the JSON join
                    ws.send(JSON.stringify(jsonJoin));

                } else {
                    message.value = "Could not open the websocket!";
                }
            }
        }
    }


    // Function that gets the message and stores them in variables, then sends the information to the server
    function handleSendMessage(event) {
        if (event.code === "Enter") {
            let message = messageInput.value;
            let currentDate = new Date();
            let time = currentDate.toLocaleTimeString();

            // If the websocket is open, send the username and room to the server
            if (wsOpen) {
                let jsonMessage = {
                    "type": "message",
                    "user": usernameInput.value,
                    "room": roomInput.value,
                    "message": message,
                    "time": time
                }
                ws.send(JSON.stringify(jsonMessage));

                // After message is sent, clear the text box
                let messageInput = document.getElementById("inputMessage");
                messageInput.value = "";

            } else {
                message.value = "Could not open the websocket!";
            }
        }
    }


    // Function that will handle the connection of the client to the server
    function handleConnect(event) {
        wsOpen = true;
    }

    // Function that will handle closing the connection
    function handleClose(event) {
        alert("Connection is closed...");
    }

    // Function that will handle any errors
    function handleError(event) {

    }

    // Function that will handle messages sent
    function handleMessage(event) {
        let receivedMessage = event.data;
        console.log("received message: " + receivedMessage);

        // Parse the JSON object
        let object = JSON.parse(receivedMessage);

        // Get the room and the message
        let user = object.user;
        let room = object.room;
        let message = object.message;
        let time = object.time;

        console.log(user);
        console.log(room);

        // Create elements that will add text to the room and message center
        let addToRoom = document.createElement("p");
        let addToMessageCenter = document.createElement("div");
        let messageParagraph = document.createElement("p");
        let timeParagraph = document.createElement("p");


        if (object.type === "join") {
            // Add the user to the division "People in Room"
            addToRoom.innerHTML = user + " has joined the room: " + room;
            // Add the paragraph to the "People in Room division"
            peopleInRoom.appendChild(addToRoom);
        }

        if (object.type === "message" && object.user !== "null") {
            // Add the message to the division "message center"
            messageParagraph.innerHTML = user + ": " + message;
            timeParagraph.innerHTML = time;

            // Styling for the message center
            addToMessageCenter.style.display = "flex";
            addToMessageCenter.style.justifyContent = "space-between";
            addToMessageCenter.style.alignItems = "center";
            timeParagraph.style.marginLeft = "20px";
            timeParagraph.style.fontSize = "10px";

            // Add paragraph and time stamp to the message center
            addToMessageCenter.appendChild(messageParagraph);
            addToMessageCenter.appendChild(timeParagraph);

            // Append to message center
            messageCenter.appendChild(addToMessageCenter);
        }

        if (object.type === "leave") {
            let leaveMessage = document.createElement("p");
            leaveMessage.innerHTML = user + " left the room."
            peopleInRoom.appendChild(leaveMessage);
        }
    }
}

window.onload = mainFunction();
