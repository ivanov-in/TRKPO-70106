const {v4: UUID} = require('uuid');
const {poolOra: poolOra} = require("./db_pool");
let oracledb = require('oracledb');

let get_users_role = (connection, p_id_users) => {
    return new Promise(function (resolve, reject) {
        _get_users_role(connection, p_id_users).then(function (res) {
            resolve(res)
        }).catch(function (err) {
            reject(err)
        })
    })
};

let insert_role = (connection, id_user, role, p_uc) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.insert_role(
            :p_id_user, 
   			:p_roletype, 
  			:p_uc, 
 			:po_res); END;`,
            {
                p_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_user},
                p_roletype: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: role},
                p_uc: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: p_uc},
                po_res: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            }).then(function (result) {
            resolve(result);
        }).catch(function (err) {
            reject(err);
        });
    })
};

let delete_role = (connection, id_user, role, p_ud) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.delete_role(
            :p_id_user, 
   			:p_roletype, 
  			:p_ud, 
 			:po_res); END;`,
            {
                p_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_user},
                p_roletype: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: role},
                p_ud: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: p_ud},
                po_res: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            }).then(function (result) {
            resolve(result);
        }).catch(function (err) {
            reject(err);
        });
    })
};

async function _get_users_role(connection, p_id_users) {
    oracledb.outFormat = oracledb.OBJECT;
    let result = await connection.execute(`BEGIN MIS_ADMIN.web_auth.get_users_role(
            :p_id_user, 
   			:cur); END;`,
        {
            p_id_user: {type: oracledb.NIMBER, dir: oracledb.BIND_IN, val: p_id_users},
            cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
        })
    const resultSet = await result.outBinds.cur;
    let row;
    let jsonRole = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonRole.push(row);
        }
        resultSet.close()
    } catch (e) {
        throw ("{A362635D-A1B2-40F4-830B-3B31C17C70C5}");
    }
    return (jsonRole);
}

module.exports = {get_users_role, insert_role, delete_role};
