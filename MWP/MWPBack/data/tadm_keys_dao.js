const {v4: UUID} = require('uuid');
const {poolOra: poolOra} = require("./db_pool");
let oracledb = require('oracledb');

let insert_keys = (connection, p_front_pub_key, p_front_pri_key, p_client_pub_key) => {
    return new Promise(function (resolve, reject) {
        let uuid = UUID().toString();
        connection.execute(`BEGIN MIS_ADMIN.web_auth.insert_keys(:p_uuid, :p_front_pub_key, :p_front_pri_key, :p_client_pub_key); END;`,
            {
                p_uuid: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: uuid},
                p_front_pub_key: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_front_pub_key},
                p_front_pri_key: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_front_pri_key},
                p_client_pub_key: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_client_pub_key}
            },
            {
                resultSet: true
            }).then(function (result) {
            resolve(uuid);
        })
    });
};
let select_keys = (connection, p_uuid) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.select_keys(:p_uuid_keys, :po_exist, :po_front_pri_key, :po_client_pub_key); END;`,
            {
                p_uuid_keys: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_uuid},
                po_exist: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                po_front_pri_key: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT, maxSize: 4000},
                po_client_pub_key: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT, maxSize: 4000}
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
module.exports = {insert_keys, select_keys};
