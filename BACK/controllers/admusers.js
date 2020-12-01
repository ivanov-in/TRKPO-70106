let oracledb = require('oracledb');
const adm_users_dao = require('../data/tadm_users_dao');
const tadm_user_roles_dao = require('../data/tadm_user_roles_dao');
const tadm_token_dao = require('../data/get_token_data');
const pool = require('../data/db_pool');

async function getUsersList(connection) {
    let result = await adm_users_dao.getUsersListDao(connection);
    return result;
}


function getUrerRoles(p_adm_token) {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            tadm_token_dao.get_token_data(connection, p_adm_token).then(function (result) {
                let id_user = null;
                if (result.outBinds.po_exist === 1) {
                    id_user = result.outBinds.po_uuid_user;
                    tadm_user_roles_dao.get_users_role(connection, id_user).then(function (result) {
                        pool.connectionRelease(connection);
                        resolve(result);
                    }).catch(function (error) {
                        pool.connectionRelease(connection);
                        reject(error);
                    });
                } else {
                    pool.connectionRelease(connection);
                    reject("{7659820F-602A-4A8C-8908-04FB4294707A}")
                }
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
}

module.exports = {getUrerRoles, getUsersList};
