let oracledb = require('oracledb');

async function get_token_data(connection, p_token) {
    try {

        let result = await connection.execute(`BEGIN MIS_ADMIN.web_auth.get_token_data(
            :p_uuid_token, 
   			:po_exist, 
  			:po_dt_exp, 
 			:po_uuid_user); END;`,
            {
                p_uuid_token: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_token},
                po_exist: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                po_dt_exp: {type: oracledb.DATE, dir: oracledb.BIND_OUT},
                po_uuid_user: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
            });
        return (result);
    } catch (err) {
        return (err);
    }
}

let insert_token = (connection, p_token, p_tiketfortoken, p_id_user, p_ip, p_uuid_keys, p_login) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.insert_token(
            :p_uuid_token, 
   			:p_uuid_tiketfortoken, 
  			:p_id_user, 
 			:p_ip, 
  			:p_uuid_keys,
  			:p_login); END;`,
            {
                p_uuid_token: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_token},
                p_uuid_tiketfortoken: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_tiketfortoken},
                p_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: p_id_user},
                p_ip: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_ip},
                p_uuid_keys: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_uuid_keys},
                p_login: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_login},
            },
            {
                resultSet: true
            }).then(function (result) {
            connection.commit().then(_=>{
                resolve(result);
            });
        }).catch(function (err) {
            reject(err);
        });
    });
};

module.exports = {get_token_data, insert_token};
