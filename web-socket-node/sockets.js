
module.exports = {
    websocket: (socket) => {
        console.log("connected...")

        socket.on("disconnect", () => {
            console.log(`${Date()} | Client disconnected`);
        });
        socket.on("JoinSession", (sessionId) => {
            socket.join(sessionId);
            console.log(`${Date()} | Client joined ${sessionId}`)
        });
        socket.on("Event", (sessionId, event) => {
            console.log(`${Date()} | Event: ${sessionId} ${event}`)
            socket.to(sessionId).emit("FromServer", event);
        });
    }
}
