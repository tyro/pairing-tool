const express = require("express");
const routes = require("./routes/route")

let app = express();

app.use(express.json())
app.use("/", routes)


module.exports = app;
