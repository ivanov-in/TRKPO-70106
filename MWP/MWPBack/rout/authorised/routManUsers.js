const express = require('express');
const router = express.Router();
let oracledb = require('oracledb');
const {poolOra: poolOra} = require("../../data/db_pool");
const tman_task_dao = require('../../data/tman_task_dao');
const routManUsersController = require('../httpControllers/routManUsersController');
const get_token_data = require('../../data/get_token_data')

router.post('/inspectors', function (req, res, next) {
    routManUsersController.selectInspectors().then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });
});

router.post('/list_tasks', function (req, res, next) {
    let date = req.header('nowDate');
    routManUsersController.selectTasks(date).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });

});

router.post('/add_task', function (req, res, next) {
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();
        body = JSON.parse(body)// convert Buffer to string
    });
    req.on('end', (path, callback) => {
        let token = req.header("token");
        oracledb.getConnection(poolOra.hrPool).then(function (connection) {
            // get_token_data.get_token_data(connection, token).then(function (result) {
                tman_task_dao.add_task(body['address'], body['city'], body['street'], body['korpus'], body['house'], parseInt(body['purpose']), body['prim'], body['time'], parseInt(body['id_ins']), body['email'], body['lat'], body['lan'], body['s_zulu'], body['b_zulu'], parseInt(body['status'])).then(function (json) {
                    oracledb.getConnection(poolOra.hrPool).then(function (connection) {
                        // tman_task_dao.get_task(connection, result2.outBinds.po_id_task).then(function (result) {
                        // tman_task_dao.get_list_obj(connection, result2.outBinds.po_id_task).then(function (result) {
                        res.status(200).json(json);
                    }).catch(function (err) {
                        res.status(500).json(err);
                    })
                    // })
                    // })
                })
            // })
        })
    })
});

router.post('/get_obj', function (req, res, next) {
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();
        // body = JSON.parse(body)// convert Buffer to string
    });
    req.on('end', (path, callback) => {
        routManUsersController.getObj(body).then(function (result) {
            res.status(200).json(result);
        }).catch(function (error) {
            res.status(500).json(error);
        });
    })
});

module.exports = router;
