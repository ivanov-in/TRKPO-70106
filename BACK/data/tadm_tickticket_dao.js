let oracledb = require('oracledb');
logUtil = require('../util/logUtil');

let deleteLost = (connection, id_user) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.delete_lost(:pi_id_user); END;`,
            {
                pi_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_user}
            },
            {
                resultSet: true
            }).then(function (result) {
            resolve(result);
        })
    });
};

let get_tickticket_data = (connection, p_adm_tickticket) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.get_tickticket_data(
			:p_tickticket, 
            :po_exist, 
            :po_id_user, 
            :po_login, 
            :po_dt_exp, 
            :po_dt_use, 
            :po_user_lock, 
            :po_user_time_lock, 
            :po_pass,
            :po_uuid_keys); END;`,
            {
                p_tickticket: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_adm_tickticket},
                po_exist: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                po_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                po_login: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT},
                po_dt_exp: {type: oracledb.DATE, dir: oracledb.BIND_OUT},
                po_dt_use: {type: oracledb.DATE, dir: oracledb.BIND_OUT},
                po_user_lock: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                po_user_time_lock: {type: oracledb.DATE, dir: oracledb.BIND_OUT},
                po_pass: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT},
                po_uuid_keys: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            }).then(function (result) {
            resolve(result);
        }).catch(function (error) {
            reject(error);
        })
    });
};

let insert_tickticket = (connection, p_adm_tickticket, p_dt_exp, p_id_user, p_ip, p_uuid_keys) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.insert_tickticket(:p_uuid_web_tickticket, :p_dt_exp, :p_id_user, :p_ip, :p_uuid_keys); END;`,
            {
                p_uuid_web_tickticket: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_adm_tickticket},
                p_dt_exp: {type: oracledb.DATE, dir: oracledb.BIND_IN, val: p_dt_exp},
                p_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: p_id_user},
                p_ip: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_ip},
                p_uuid_keys: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_uuid_keys}
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
module.exports = {get_tickticket_data, deleteLost, insert_tickticket};
