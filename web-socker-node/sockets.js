
let interval;

module.exports = {
    websocket: (socket) => {
        console.log("connected...")

        socket.on("disconnect", () => {
            console.log("Client disconnected");
        });
        socket.on("JoinSession", (sessionId) => {
            socket.join(sessionId);
        });
        socket.on("Event", (sessionId, event) => {
            socket.to(sessionId).emit("FromServer", event);
        });
    }
}
