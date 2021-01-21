let oracledb = require('oracledb');
const pool = require('../../data/db_pool');
const admUsers = require('../../controllers/admusers');
const tadm_token_dao = require('../../data/get_token_data');
const tadm_users_dao = require('../../data/tadm_users_dao');
const tadm_user_roles_dao = require('../../data/tadm_user_roles_dao');
const tadm_audit_dao = require('../../data/tadm_audit_dao')

let admuserslist = () => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            admUsers.getUsersList(connection).then(function (json) {
                pool.connectionRelease(connection);
                resolve(json);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                resolve(error);
            });
        }).catch(function (error) {
            resolve(error);
        });
    });
}

let admuserSetTel = (id_user, tel, token) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            tadm_token_dao.get_token_data(connection, token).then(function (token_data) {
                tadm_users_dao.update_tel(connection, id_user, tel, parseInt(token_data.outBinds.po_uuid_user)).then(function (result) {
                    connection.commit().then(function (result) {
                        pool.connectionRelease(connection);
                        if (result) {
                            resolve({"result": true});
                        } else {
                            resolve(result);
                        }
                    });

                }).catch(function (error) {
                    pool.connectionRelease(connection);
                    reject(error);
                });
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
}

let admuserRolesAdd = (id_user, roles, token) => {
    let udRoles = 'ins,adm,man,';
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            tadm_token_dao.get_token_data(connection, token).then(function (token_data) {
                let rolesArr = roles.split(',');
                if (rolesArr.length === 1) {
                    if (rolesArr[0] === '') {
                        rolesArr = []
                    }
                }
                let toInsArrProm = [];
                for (let role of rolesArr) {
                    if (role.length > 0) {
                        udRoles = udRoles.replace(role + ',', '');
                    }
                    toInsArrProm.push(tadm_user_roles_dao.insert_role(connection, id_user, role, parseInt(token_data.outBinds.po_uuid_user)));
                }
                Promise.all(toInsArrProm).then(function (result) {
                    let arrDelRoles = udRoles.slice(0, udRoles.length - 1).split(',');
                    if (arrDelRoles.length === 1) {
                        if (arrDelRoles[0] === '') {
                            arrDelRoles = []
                        }
                    }
                    let toDelArrProm = [];
                    for (let role of arrDelRoles) {
                        toDelArrProm.push(tadm_user_roles_dao.delete_role(connection, id_user, role, parseInt(token_data.outBinds.po_uuid_user)));
                    }
                    Promise.all(toDelArrProm).then(function (result) {
                        connection.commit();
                        pool.connectionRelease(connection);
                        resolve({"result": true});
                    }).catch(function (error) {
                        pool.connectionRelease(connection);
                        reject(error);
                    })
                }).catch(function (error) {
                    pool.connectionRelease(connection);
                    reject(error);
                })
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
}

let admuserPasSetDef = (token, id_user) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            tadm_token_dao.get_token_data(connection, token).then(function (res) {
                tadm_users_dao.user_pass_set_def(connection, id_user, parseInt(res.outBinds.po_uuid_user)).then(function (result) {
                    connection.commit();
                    pool.connectionRelease(connection);
                    resolve(result);
                }).catch(function (error) {
                    pool.connectionRelease(connection);
                    reject(error);
                })
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
}

let userDelete = (token, id_user) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            tadm_token_dao.get_token_data(connection, token).then(function (res) {
                tadm_users_dao.user_delete(connection, id_user, parseInt(res.outBinds.po_uuid_user)).then(function (result) {
                    connection.commit();
                    pool.connectionRelease(connection);
                    resolve(result)
                }).catch(function (error) {
                    pool.connectionRelease(connection);
                    reject(error)
                })
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error)
            })
        }).catch(function (error) {
            reject(error);
        })
    });
}

let userChengePassword = (token, old_pass, new_pass) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            tadm_token_dao.get_token_data(connection, token).then(function (result) {
                tadm_users_dao.user_chenge_password(connection, parseInt(result.outBinds.po_uuid_user), old_pass, new_pass).then(function (result) {
                    connection.commit();
                    pool.connectionRelease(connection);
                    resolve(result);
                }).catch(function (error) {
                    pool.connectionRelease(connection);
                    reject(error);
                });
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
}

let admuserUserLock = (token, lock, id_insp) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            tadm_token_dao.get_token_data(connection, token).then(function (result) {
                tadm_users_dao.user_lock(connection, id_insp, lock, parseInt(result.outBinds.po_uuid_user)).then(function (result) {
                    connection.commit();
                    pool.connectionRelease(connection);
                    resolve(result);
                }).catch(function (err) {
                    pool.connectionRelease(connection);
                    reject(err)
                })
            }).catch(function (err) {
                pool.connectionRelease(connection);
                reject(err)
            })
        }).catch(function (error) {
            reject(err)
        });
    });
}

let admuserCreateUser = (token, fio, tel, rolesArr) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            tadm_token_dao.get_token_data(connection, token).then(function (res) {
                tadm_users_dao.insert_inspector(connection, fio, tel, parseInt(res.outBinds.po_uuid_user)).then(function (result) {
                    let arrProm = [];
                    for (let role of rolesArr) {
                        arrProm.push(tadm_user_roles_dao.insert_role(connection, result.outBinds.po_id_inspector, role, parseInt(res.outBinds.po_uuid_user)));
                    }
                    Promise.all(arrProm).then(function (result) {
                        connection.commit();
                        pool.connectionRelease(connection);
                        resolve(result);
                    }).catch(function (error) {
                        pool.connectionRelease(connection);
                        reject(error);
                    })
                }).catch(function (error) {
                    pool.connectionRelease(connection);
                    reject(error);
                })
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error)
            });
        }).catch(function (error) {
            reject(error);
        })
    });
}

let evenList = () => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            tadm_audit_dao.get_events(connection).then(function (json) {
                resolve(json);
            }).catch(function (error) {
                reject(error)
            });
        });
    });
};

let selectevenList = (date) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            tadm_audit_dao.select_list_events(connection, date).then(function (json) {
                resolve(json);
            }).catch(function (error) {
                reject(error)
            });
        });
    });
};

module.exports = {
    admuserslist,
    admuserSetTel,
    admuserRolesAdd,
    admuserPasSetDef,
    userDelete,
    userChengePassword,
    admuserUserLock,
    admuserCreateUser,
    evenList,
    selectevenList
}
