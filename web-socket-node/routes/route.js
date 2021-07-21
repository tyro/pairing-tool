const express = require("express");
const createError = require('http-errors');
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

router.use(function(req, res, next) {
    next(createError(404));
});

module.exports = router
