#!/usr/bin/env node

const process = require('process');
const {websocket} = require("./sockets");
const app = require("./app");
const http = require("http");

process.on('SIGINT', () => {
    console.info("")
    console.info("Stopping...")
    process.exit(0)
});

let port = (process.env.PORT) || '4001';
console.log("Starting on port: " + port)
app.set(port);

const server = http.createServer(app);
const io = require("socket.io")(server, {
    origins: ["*"],
    methods: ["GET", "POST"],
});

io.on("connection", websocket)

server.listen(port);
