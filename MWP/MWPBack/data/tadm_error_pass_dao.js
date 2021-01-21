const {v4: UUID} = require('uuid');
const {poolOra: poolOra} = require("./db_pool");
let oracledb = require('oracledb');

let select_error_pass = (connection, p_adm_users) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.select_error_pass(:p_id_user, :po_attempt_num); END;`,
            {
                p_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: p_adm_users},
                po_attempt_num: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            }).then(function (result) {
            resolve(result);
        }).catch(function (error) {
            reject(error);
        });
    });
};

let set_error_pass = (connection, p_adm_users, p_attempt_num) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.set_error_pass(:pi_id_user, :pi_attempt_num); END;`,
            {
                pi_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: p_adm_users},
                pi_attempt_num: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: p_attempt_num}
            },
            {
                resultSet: true
            }).then(function (result) {
            connection.commit();
            resolve(result);
        }).catch(function (err) {
            reject(err);
        });
    });
};
module.exports = {select_error_pass, set_error_pass};
