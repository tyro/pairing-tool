const express = require("express");
const router = express.Router();

router.get("/alive", (req, res, next) => {
    res.send('{"alive":"yes"}')
});

router.get("/ready", (req, res, next) => {
    res.send('{"ready":"ok"}')
});

router.get("/", (req, res, next) => {
    res.send('{"ready":"ok"}')
});

module.exports = router
