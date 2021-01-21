const log = require('../log/util');
const http_util = require('../http/util');
const token_dao = require('../database_dao/ttoken_dao');
const tkeys_dao = require('../database_dao/tkeys_dao');
const pool = require('../db_pool');
const forge = require('node-forge');

let validateToken = (req) => {
    return new Promise(function (resolve, reject) {
        console.log('Start token validate: ' + log.nowTime());
        if (req === undefined) {
            reject("{6CEF24DB-4182-48C5-B9DB-C51EBC60090C}");
            return;
        }
        var token_cr_64 = req.header("token_cr_64");
        if (token_cr_64 === undefined) {
            console.log('End token validate: ' + log.nowTime() + ' Result: FALSE');
            resolve(false);
            return;
        }
        var imei = req.header("imei");
        if (imei === undefined) {
            console.log('End token validate: ' + log.nowTime() + ' Result: FALSE');
            resolve(false);
            return;
        }
        try {
            var ip = http_util.getIp(req);
            token_dao.selectBy(pool.con, imei, ip).then(function (res) {
                if (res.rows.length == 1) {
                    var uuid_keys = res.rows[0].uuid_keys;
                    var token_db = res.rows[0].token;
                    var user_key_db_int = res.rows[0].use_key;
                    var use_count_db_int = res.rows[0].use_count;
                    tkeys_dao.selectByKey(pool.con, uuid_keys).then(function (res) {
                        if (res.rows.length == 1) {
                            var privKeyStr = res.rows[0].front_pri_key;
                            var privKey = forge.pki.privateKeyFromPem(privKeyStr);
                            var token_cr = forge.util.decode64(token_cr_64);
                            var token = privKey.decrypt(token_cr);
                            if (token.includes(token_db)) {
                                var user_key = token.replace(token_db, '');
                                if (!Number.isNaN(Number(user_key))) {
                                    var user_key_int = parseInt(user_key, 10);
                                    if (!isNaN(user_key_int)) {
                                        if (user_key_db_int < user_key_int) {
                                            user_key_int++;
                                            if (user_key_int > 10000) {
                                                console.log('End token validate: ' + log.nowTime() + ' Result: FALSE');
                                                resolve(false);
                                            }
                                            use_count_db_int++;
                                            token_dao.updateByKey(pool.con, token_db, user_key_int, use_count_db_int).then(function (res) {
                                                console.log('End token validate: ' + log.nowTime() + ' Result: TRUE');
                                                resolve(true);
                                            }).catch(function (err) {
                                                reject("{AFE23BFE-7B75-4FFC-BC08-3897CF9C6A1D}");
                                            })
                                        } else {
                                            console.log('End token validate: ' + log.nowTime() + ' Result: FALSE');
                                            resolve(false);
                                        }
                                    } else {
                                        console.log('End token validate: ' + log.nowTime() + ' Result: FALSE');
                                        resolve(false);
                                    }
                                } else {
                                    console.log('End token validate: ' + log.nowTime() + ' Result: FALSE');
                                    resolve(false);
                                }
                            } else {
                                console.log('End token validate: ' + log.nowTime() + ' Result: FALSE');
                                resolve(false);
                            }
                        } else {
                            reject("{D1B61515-242B-456C-83D7-770D1A2234A1}");
                        }
                    }).catch(function (err) {
                        reject("{37340403-659C-4086-98AF-58EF7046B5B5}");
                    });
                } else {
                    console.log('End token validate: ' + log.nowTime() + ' Result: FALSE');
                    resolve(false);
                }
            }).catch(function (err) {
                reject("{BAA7DA08-5411-4F48-9EF5-9348FA11C207}");
            });
        } catch (e) {
            reject("{BFF4641B-24F7-4989-8482-5498DD5E42F2}");
        }
    });
};

module.exports = {validateToken};