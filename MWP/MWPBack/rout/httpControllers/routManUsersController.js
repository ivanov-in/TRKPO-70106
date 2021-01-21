let oracledb = require('oracledb');
const pool = require('../../data/db_pool');
const taman_task_dao = require('../../data/tman_task_dao');

let selectInspectors = () => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.select_insp(connection).then(function (json) {
                pool.connectionRelease(connection);
                resolve(json);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};

let selectTasks = (date) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.select_list_tasks(connection, date).then(function (json) {
                pool.connectionRelease(connection);
                resolve(json);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });

        }).catch(function (error) {
            reject(error);
        });
    });
};

let getObj = (text) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.get_objects(connection, text).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};

module.exports = {selectInspectors, selectTasks, getObj};
