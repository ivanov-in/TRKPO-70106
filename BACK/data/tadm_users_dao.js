let oracledb = require('oracledb');
const logUtil = require('../util/logUtil');

let getUserData = (connection, login) => {
    return new Promise(function (resolve, reject) {
        logUtil.wi('tadm_users_dao start getUserData(connection, login)')
        connection.execute(`BEGIN MIS_ADMIN.web_auth.login_exist(:login,:po_id,:po_user_lock,:po_time_lock); END;`,
            {
                login: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: login},
                po_id: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                po_user_lock: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                po_time_lock: {type: oracledb.DATE, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            }).then(function (result) {
            logUtil.wi(`tadm_users_dao success getUserData(connection, login) result: ${JSON.stringify(result)}`)
            resolve(result);
        }).catch(err => {
            logUtil.we(`tadm_users_dao error getUserData(connection, login) err: ${err}`)
            reject(err);
        })
    });
};

async function getUsersListDao(connection) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS_ADMIN.adm_users_page.get_users(:cur);
       END;`,
            {
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        let err = "{306197F2-7C25-4BD5-9066-38496C62CC71}";
        console.log(err + " " + e.message);
        throw err;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonInsp = [];
    try {
        while ((row = await resultSet.getRow())) {
            row.LOGIN = row.LOGIN.toUpperCase();
            jsonInsp.push(row);
        }
    } catch (e) {
        let err = "{C5626E70-AE5E-440B-BEAF-905DBF10D7AF}";
        console.log(err + " " + e.message);
        throw err;
    }
    return jsonInsp;
}

let update_tel = (connection, p_id_user, p_tel, p_uc) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.adm_users_page.update_tel(:pi_id_user, :p_tel, :p_uc, :po_res); END;`,
            {
                pi_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: p_id_user},
                p_tel: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: p_tel},
                p_uc: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: p_uc},
                po_res: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            }).then(function (result) {
            if (result.outBinds.po_res === 1) {
                resolve(true);
            } else {
                reject({"error": "{C791513A-1816-4B5B-BCB8-FB6C699DE199}"});
            }
        }).catch(function (err) {
            reject(err);
        });
    });
};

let update_timelock = (connection, p_adm_users) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.update_timelock(:pi_id_user); END;`,
            {
                pi_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: p_adm_users}
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

let user_pass_set_def = (connection, id_user, id_adm) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.user_pass_set_def(:pi_id_user, :p_adm, :po_res); END;`,
            {
                pi_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_user},
                p_adm: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_adm},
                po_res: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT}
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

// OGQ5NjllZWY2ZWNhZDNjMjlhM2E2MjkyODBlNjg2Y2YwYzNmNWQ1YTg2YWZmM2NhMTIwMjBjOTIzYWRjNmM5Mg==
let user_chenge_password = (connection, id_user, old_pass, new_pass) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.user_chenge_password(:p_id_user, :p_old_pass_hash, :p_new_pass_hash, :po_error); END;`,
            {
                p_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_user},
                p_old_pass_hash: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: old_pass},
                p_new_pass_hash: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: new_pass},
                po_error: {type: oracledb.VARCHAR, dir: oracledb.BIND_OUT}
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


let user_lock = (connection, id_user, lock, id_adm) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS_ADMIN.web_auth.user_lock(:p_id_user, :p_lock, :p_adm, :po_res); END;`,
            {
                p_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_user},
                p_lock: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: lock},
                p_adm: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_adm},
                po_res: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT}
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

let user_delete = (connection, id_user, id_adm) => {
    return new Promise(function (resolve, reject) {

        connection.execute(`BEGIN MIS_ADMIN.web_auth.user_delete(:p_id_user, :uu, :po_error); END;`,
            {
                p_id_user: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_user},
                uu: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_adm},
                po_error: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
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


let insert_inspector = (connection, fio, tel, id_adm) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS.mis_web_w.insert_inspector(
                             :p_fio,
                             :p_tel,
                             :p_id_inspector_mod,
                             :po_id_inspector,
                             :status,
                             :errmsg); END;`,
            {
                p_fio: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: fio},
                p_tel: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: tel},
                p_id_inspector_mod: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_adm},
                po_id_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                errmsg: {type: oracledb.VARCHAR, dir: oracledb.BIND_OUT}
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

async function insert_devices(connection) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS_ADMIN.adm_mobdevices_page.get_devices_list(:cur);
       END;`,
            {
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        let err = "{306197F2-7C25-4BD5-9066-38496C62CC71}";
        console.log(err + " " + e.message);
        throw err;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonInsp = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonInsp.push(row);
        }
    } catch (e) {
        let err = "{C5626E70-AE5E-440B-BEAF-905DBF10D7AF}";
        console.log(err + " " + e.message);
        throw err;
    }
    return jsonInsp;
};


module.exports = {
    getUserData,
    update_timelock,
    getUsersListDao,
    update_tel,
    user_pass_set_def,
    user_lock,
    insert_inspector,
    user_chenge_password,
    user_delete,
    insert_devices
};
