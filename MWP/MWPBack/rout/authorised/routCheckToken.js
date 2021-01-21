const express = require('express');
const router = express.Router();
const tokenValidator = require('../httpControllers/routCheckTokenController');


router.get('/authorised/*', function (req, res, next) {
    tokenValidator.validateToken(req).then(function (result) {
        if (result.valid){
            next();
        } else {
            res.status(401).json(result)
        }
    }).catch(function (err) {
        res.status(401).json({valid_token: false})
    });
});

router.post('/*', function (req, res, next) {
    tokenValidator.validateToken(req).then(function (result) {
        if (result.valid){
            next();
        } else {
            res.status(401).json(result)
        }
    }).catch(function (err) {
        res.status(401).json({valid_token: false})
    });
});

router.put('/authorised/*', function (req, res, next) {
    tokenValidator.validateToken(req).then(function (result) {
        if (result.valid){
            next();
        } else {
            res.status(401).json(result)
        }
    }).catch(function (err) {
        res.status(401).json({valid_token: false})
    });
});

router.patch('/authorised/*', function (req, res, next) {
    tokenValidator.validateToken(req).then(function (result) {
        if (result.valid){
            next();
        } else {
            res.status(401).json(result)
        }
    }).catch(function (err) {
        res.status(401).json({valid_token: false})
    });
});

router.delete('/authorised/*', function (req, res, next) {
    tokenValidator.validateToken(req).then(function (result) {
        if (result.valid){
            next();
        } else {
            res.status(401).json(result)
        }
    }).catch(function (err) {
        res.status(401).json({valid_token: false})
    });
});



module.exports = router;
