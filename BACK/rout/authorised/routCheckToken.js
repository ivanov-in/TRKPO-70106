const express = require('express');
const router = express.Router();
const tokenValidator = require('../httpControllers/routCheckTokenController');
const logUtil = require('../../util/logUtil');

const logError = (req, error) => {
    logUtil.we(`${req.originalUrl} status: 401 mes: ${error}`);
}
const logInfoTokenExpired = (req, mes) => {
    logUtil.wi(`${req.originalUrl} send: token_expired mes: ${mes}`);
}
const logInfoToken = (req, mes) => {
    logUtil.wi(`${req.originalUrl} send: token_ok mes: ${mes}`);
}

const token_status = 'token_status'

router.get('/authorised/*', function (req, res, next) {
    tokenValidator.validateToken(req).then(function (result) {
        if (result.valid) {
            if (result.expired) {
                logInfoTokenExpired(req, JSON.stringify(result));
                res.setHeader(token_status, 'expired')
            } else {
                logInfoToken(req, JSON.stringify(result));
            }
            next();
        } else {
            logError(req, JSON.stringify(result));
            res.status(401).json(result)
        }
    }).catch(function (err) {
        try {logError(req, err);} catch (e){}
        res.status(401).json({valid_token: false})
    })
});

router.post('/*', function (req, res, next) {
    tokenValidator.validateToken(req).then(function (result) {
        if (result.valid) {
            if (result.expired) {
                logInfoTokenExpired(req, JSON.stringify(result));
                res.setHeader(token_status, 'expired')
            } else {
                logInfoToken(req, JSON.stringify(result));
            }
            next();
        } else {
            logError(req, JSON.stringify(result));
            res.status(401).json(result)
        }
    }).catch(function (err) {
        try {logError(req, err);} catch (e){}
        res.status(401).json({valid_token: false})
    });
});

router.put('/authorised/*', function (req, res, next) {
    tokenValidator.validateToken(req).then(function (result) {
        if (result.valid) {
            next();
        } else {
            logError(req, JSON.stringify(JSON.stringify(result)));
            res.status(401).json(result)
        }
    }).catch(function (err) {
        try {logError(req, err);} catch (e){}
        res.status(401).json({valid_token: false})
    });
});

router.patch('/authorised/*', function (req, res, next) {
    tokenValidator.validateToken(req).then(function (result) {
        if (result.valid) {
            next();
        } else {
            res.status(401).json(result)
        }
    }).catch(function (err) {
        try {logError(req, err);} catch (e){}
        res.status(401).json({valid_token: false})
    });
});

router.delete('/authorised/*', function (req, res, next) {
    tokenValidator.validateToken(req).then(function (result) {
        if (result.valid) {
            next();
        } else {
            res.status(401).json(result)
        }
    }).catch(function (err) {
        try {logError(req, err);} catch (e){}
        res.status(401).json({valid_token: false})
    });
});


module.exports = router;
