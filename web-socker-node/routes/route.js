const express = require("express");
const router = express.Router();

router.get("/alive", (req, res, next) => {
    res.send('{"alive":"yes"}')
});

router.get("/healthy", (req, res, next) => {
    res.send('{"health":"ok"}')
});

module.exports = router
