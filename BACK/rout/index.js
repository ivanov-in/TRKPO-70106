const express = require('express');
const router = express.Router();


const httputil = require('../http/util');
const {v4: UUID} = require('uuid');

const logUtil = require('../util/logUtil');
const index_controller = require('../controllers/index_controller');

const responseRouterError = (req, res, error) => {
    logUtil.we(`${req.originalUrl} mes: ${error}`);
    res.status(500).json({error});
}
/* GET home page. */
router.get('/api/', function (req, res, next) {
    res.status(200).json(
        {
            login: true
        });
});

router.get('/api/check', function (req, res, next) {
    index_controller.execute(index_controller.checkConnection, null).then(_ => {
        res.status(200).json(
            {
                dbcon: true
            });
    }).catch(e => {
        res.status(200).json(
            {
                dbcon: false,
                err: err
            });
    })
});

router.get('/api/back_ver', function (req, res, next) {
    res.status(200).json(
        {
            version: global.back_ver
        });
});




router.post('/api/getticketforticket', function (req, res, next) {
    //Извлеч из header: email и header: client_key
    const email = req.header("email");
    const client_key_b64 = req.header("key");
    logUtil.wi(`http get getticketforticket email: ${email}, client_key_b64: ${client_key_b64}`);
    const ip = httputil.getIp(req);
    if (!email || !client_key_b64) {
        responseRouterError(req, res, {"error": "server Error"})
        return;
    }
    // Вызываем функцию контроллера http запроса
    index_controller.execute(index_controller.getticketforticket, {email, client_key_b64, ip}).then(function (result) {
        logUtil.wi(`http get getticketforticket httpresult: 200`);
        res.status(200).json(result);
    }).catch(function (result) {
        responseRouterError(req, res, result)
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
        index_controller.execute(index_controller.getticketfortoken, {
            ticketforticket,
            ip,
            body_json
        }).then(function (result) {

            res.status(200).json(result);
        }).catch(function (error) {
            res.status(500).json(error);
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
    index_controller.execute(index_controller.gettoken, {ticketfortoken, ip}).then(function (result) {
        res.status(200).json(result);
    }).catch(function (error) {
        res.status(500).json(error);
    })
});

// router.post('/tadmuserlist', function (req, res, next) {
//
//     adm_controller.execute(adm_controller.admuserslist, null).then(function (json) {
//         res.status(200).json(json.rows);
//     }).catch(function (err) {
//         res.status(500).json(err)
//     });
// });


module.exports = router;
