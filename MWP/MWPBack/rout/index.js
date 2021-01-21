const express = require('express');
const router = express.Router();


const httputil = require('../http/util');
const {v4: UUID} = require('uuid');
const admUsers = require('../controllers/admusers');
const db_pool = require('../data/db_pool');

let indexController = require('./httpControllers/indexController');
db_pool.init();

/* GET home page. */
router.get('/api/', function (req, res, next) {
    res.status(200).json(
        {
            login: true
        });
});

router.get('/api/check', function (req, res, next) {
    indexController.getConnection().then(function (result) {
        res.status(200).json(
            {
                dbcon: true
            });
    }).catch(function (err) {
        res.status(200).json(
            {
                dbcon: false,
                err: err
            });
    })

});

router.post('/api/getticketforticket', function (req, res, next) {
    //Извлеч из header: email и header: client_key
    const email = req.header("email");
    const client_key_b64 = req.header("key");
    const ip = httputil.getIp(req);
    if (!email || !client_key_b64) {
        res.status(500).json({"error": "server Error"});
        return;
    }
    // Вызываем функцию контроллера http запроса
    indexController.getticketforticket(email, client_key_b64, ip).then(function (result) {
        res.status(200).json(result);
    }).catch(function (result) {
        res.status(500).json(result);
    })

});

router.post('/api/getticketfortoken', function (req, res, next) {
    //Извлеч из header: упакованный в base 64 ticketforticket
    const ticketforticket = req.header("ticketforticket");
    const ip = httputil.getIp(req);
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString(); // convert Buffer to string
    });
    req.on('end', () => {
        console.log(body);
        let body_json = JSON.parse(body);
        //Вызвать функцию создания билета на токен
        indexController.getticketfortoken(ticketforticket, ip, body_json).then(function (result) {
            res.status(200).json(result);
        }).catch(function (result) {
            res.status(500).json(result);
        });
    });
});

router.post('/api/gettoken', function (req, res, next) {
    //Извлеч из header: упакованный в base 64 token
    var ticketfortoken = req.header("ticketfortoken");
    //Если заголовка нет
    if (ticketfortoken === undefined) {
        //Создать поддельный
        ticketfortoken = UUID().toString();
    }
    const ip = httputil.getIp(req);
    indexController.gettoken(ticketfortoken, ip).then(function (result) {
        res.status(200).json(result);
    }).catch(function (error) {
        res.status(500).json(error);
    })


});

router.post('/tadmuserlist', function (req, res, next) {

    admUsers.getUsersList().then(function (json) {

        res.status(200).json(json.rows);

    }).catch(function (err) {
        res.status(500).json(err)
    });
});


module.exports = router;
