const {v4: UUID} = require('uuid');
const {poolOra: poolOra} = require("./db_pool");
let oracledb = require('oracledb');

let get_ticktoken_data = (connection, p_adm_ticktoken) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.get_ticktoken_data(
			:p_web_ticktoken, 
            :po_exist, 
            :po_id_user,
            :po_dt_exp, 
            :po_dt_use, 
            :po_user_lock, 
            :po_user_time_lock,
            :po_uuid_keys,
            :po_login); END;`,
            {
                p_web_ticktoken: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_adm_ticktoken},
                po_exist: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                po_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                po_dt_exp: {type: oracledb.DATE, dir: oracledb.BIND_OUT},
                po_dt_use: {type: oracledb.DATE, dir: oracledb.BIND_OUT},
                po_user_lock: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                po_user_time_lock: {type: oracledb.DATE, dir: oracledb.BIND_OUT},
                po_uuid_keys: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT},
                po_login: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            }).then(function (result) {
            resolve(result);
        }).catch(function (err) {
            reject(err);
        });
    });
};
let insert_ticktoken = (connection, p_adm_ticktoken, p_dt_exp, p_uuid_user, p_ip, p_uuid_keys) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.insert_ticktoken(:p_uuid_web_ticktoken, :p_dt_exp, :p_id_user, :p_ip, :p_uuid_keys); END;`,
            {
                p_uuid_web_ticktoken: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_adm_ticktoken},
                p_dt_exp: {type: oracledb.DATE, dir: oracledb.BIND_IN, val: p_dt_exp},
                p_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: p_uuid_user},
                p_ip: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_ip},
                p_uuid_keys: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_uuid_keys}
            },
            {
                resultSet: true
            }).then(function (result) {
            connection.commit().then(_ => {
                resolve(result);
            });
        }).catch(function (error) {
            reject(error);
        })
    });
};
let update_use = (connection, p_uuid_user) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.update_use(:p_id_user); END;`,
            {
                p_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: p_uuid_user},
            },
            {
                resultSet: true
            }).then(function (result) {
            connection.commit().then(_ => {
                resolve(result);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};
let ticktoken_use = (connection, p_ticktoken) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.ticktoken_use(:p_ticktoken); END;`,
            {
                p_ticktoken: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_ticktoken},
            },
            {
                resultSet: true
            }).then(function (result) {
            connection.commit().then(_ => {
                resolve(result);
            });
        }).catch(function (err) {
            reject(err);
        });
    });
};
module.exports = {get_ticktoken_data, insert_ticktoken, update_use, ticktoken_use};
