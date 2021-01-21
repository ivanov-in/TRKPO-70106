let oracledb = require('oracledb');
const {v4: UUID} = require('uuid');
const uuidParse = require('uuid-parse');
const pool = require('../../data/db_pool');
const tadm_token_dao = require("../../data/get_token_data")

let validateToken = (req) => {
    return new Promise(function (resolve, reject) {

        if (req === undefined) {
            reject("{6CEF24DB-4182-48C5-B9DB-C51EBC60090C}");
            return;
        }
        const token = req.header("token");
        if (token === undefined) {
            //нет хедера токена
            resolve({
                valid: false,
                token_count: null
            });
        }
        var v4options = null;
        try {
            v4options = {
                random: uuidParse.parse(token)
            }
        } catch (e) {
            //не правельный формат токена
            resolve({
                valid: false,
                token_count: null
            });
        }
        const uuid = UUID(v4options);
        oracledb.getConnection().then(function (connection) {
            tadm_token_dao.get_token_data(connection, uuid).then(function (result) {
                pool.connectionRelease(connection);
                try {
                    if (result.outBinds.po_exist === 1) {
                        const dt_exp = Date.parse(result.outBinds.po_dt_exp);
                        const dt_cur = Date.now();
                        if (dt_exp < dt_cur) {
                            //токен устарел
                            resolve({
                                valid: false,
                                token_count: result.outBinds.po_exist,
                                expired: true
                            })
                        } else {
                            //токен действителен
                            resolve({
                                valid: true,
                                token_count: result.outBinds.po_exist
                            })
                        }
                    } else {
                        //токен не был зарегистрирован или удален
                        resolve({
                            valid: false,
                            token_count: result.outBinds.po_exist
                        })
                    }
                } catch (err) {
                    reject("{4394297D-BF82-4383-98AD-AF7CE146618E}");
                }
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject("{A41BAB25-94D1-4F19-BA21-918BB2E3D57E}");
            })
        }).catch(function (error) {
            reject("{DF11CFA5-0AB1-463E-BCE3-CD4DD224FB1B}")
        });
    });
};


module.exports = {validateToken};
