const forge = require('node-forge');
const pool = require('../db_pool');
const tkeys_dao = require('../database_dao/tkeys_dao');
const ticket_for_ticket_dao = require('../database_dao/tticketforticket_dao');
const ticket_for_token_dao = require('../database_dao/tticketfortoken_dao');
const token_dao = require('../database_dao/ttoken_dao');
const tuser_dao = require('../database_dao/tusers_dao');
const token_validator = require('../token/validator');
const tipbun = require('../database_dao/tipbun_dao');
const tiperrors = require('../database_dao/tiperrors_dao');
const tscriptstomob = require('../database_dao/tscriptstomob_dao');
const http_util = require('../http/util');
const UUID = require('uuid/v4');

const isServerOnline = (req, rsp) => {
    rsp.send("server on");
};

function insertError(con, ip, error) {
    tiperrors.insertError(pool.con, ip, error).then(function (res) {
        var p_dtc = new Date();
        tiperrors.selectCount(con, ip, p_dtc.getDay() - 1).then(function (res) {
            if (res.rows.length > 10000) {
                tipbun.insert(con, ip).then(function (res) {

                });
            }
        });
    });
}

const p_ticket_for_token = 'ticket_for_token';
const p_result = 'result';
const p_server_error = 'server_error';

function sendError(rsp, error) {
    var respObj = {};
    console.log(error);
    respObj[p_result] = 'Error';
    respObj[p_server_error] = "Server error:" + error;
    respObj[p_ticket_for_token] = "";
    rsp.end(JSON.stringify(respObj));
}


var timout = 1000;
const getTicketForTicket = (req, rsp) => {
    var ip = http_util.getIp(req);
    tipbun.selectCount(pool.con, ip).then(function (res) {
        if (res.rowCount == 0) {
            var body = req.body;
            var mobPubKeyStr = body.mob_pub_key;
            if (mobPubKeyStr === undefined) {
                insertError(pool.con, ip, "{37D43337-F069-4C85-B94B-F558A8065FF7}");
                setTimeout(() => {
                    rsp.end(JSON.stringify(respObj));
                }, timout);
            } else {
                var mobPubKey;
                try {
                    mobPubKey = forge.pki.publicKeyFromPem(mobPubKeyStr);
                } catch (e) {
                    insertError(pool.con, ip, "{E292FD49-4D20-47BB-A700-CF2C1E1E2196}");
                    setTimeout(() => {
                        rsp.end(JSON.stringify(respObj));
                    }, timout);
                    return;
                }
                var ticketForTicket = UUID();
                var encriptedTicketForTicket;
                try {
                    encriptedTicketForTicket = mobPubKey.encrypt(forge.util.encodeUtf8(ticketForTicket));
                } catch (e) {
                    insertError(pool.con, ip, "{8C9B6DD1-80B3-41F2-935E-554C6C2ED016}");
                    setTimeout(() => {
                        rsp.end(JSON.stringify(respObj));
                    }, timout);
                    return;
                }
                var encriptedTicketForTicket64;
                try {
                    encriptedTicketForTicket64 = forge.util.encode64(encriptedTicketForTicket);
                } catch (e) {
                    sendError(rsp, "{A018C820-3FAA-4324-9C16-F7127516EC6B}");
                    return;
                }
                var keys = forge.pki.rsa.generateKeyPair({bits: 1024, e: 0x10001});
                var frontPubKeyStr = forge.pki.publicKeyToPem(keys.publicKey);
                frontPubKeyStr = frontPubKeyStr.replace('-----BEGIN PUBLIC KEY-----', '').replace('-----END PUBLIC KEY-----', '').replace('\r\n', '');
                var frontPrvKeyStr = forge.pki.privateKeyToPem(keys.privateKey);
                rsp.setHeader('Content-Type', 'application/json');
                var respObj = {};
                tkeys_dao.insert(pool.con, frontPubKeyStr, frontPrvKeyStr, mobPubKeyStr).then(function (res) {
                    ticket_for_ticket_dao.insert(pool.con, ticketForTicket, http_util.getIp(req), res.rows[0].uuid).then(function (res) {
                        var p_serv_pub_key = 'serv_pub_key';
                        var p_ticket_for_ticket = 'ticket_for_ticket';
                        respObj[p_serv_pub_key] = frontPubKeyStr;
                        respObj[p_ticket_for_ticket] = encriptedTicketForTicket64;
                        rsp.end(JSON.stringify(respObj));
                    }).catch(function (err) {
                        sendError(rsp, "{E084CF4F-864E-4073-AEC2-16C6D245DC8B}");
                    });
                }).catch(function (err) {
                    sendError(rsp, "{3F8018C9-4B5C-4E7F-BDDB-2C6CE553E312}");
                });
            }
        } else {
            setTimeout(() => {
                rsp.end(JSON.stringify(respObj));
            }, timout);
        }
    }).catch(function (err) {
        sendError(rsp, "{84C37445-7200-4007-A245-9059CEDF5227}");
    });

};


function errorReturn(con, ip, error, rsp, respObj) {
    insertError(con, ip, error);
    setTimeout(() => {
        rsp.end(JSON.stringify(respObj));
    }, timout);
}

const getTicketForToken = (req, rsp) => {
    var respObj = {};
    var ip = http_util.getIp(req);
    var body = req.body;
    var ticket_for_ticket = body.ticket_for_ticket;
    if (ticket_for_ticket === undefined) {
        errorReturn(pool.con, ip, "{FFE50459-EDD6-44B1-99E6-8948A8CDCDF0}", rsp, respObj);
        return;
    }
    var login_cr_b64 = body.login_cr_b64;
    if (login_cr_b64 === undefined) {
        errorReturn(pool.con, ip, "{D5260858-B4BE-4817-8316-F000E57C1736}", rsp, respObj);
        return;
    }
    var pass_hash_cr_b64 = body.pass_hash_cr_b64;
    if (pass_hash_cr_b64 === undefined) {
        errorReturn(pool.con, ip, "{60539697-4DA1-4D2E-BC86-DD73FA9BD6FE}", rsp, respObj);
        return;
    }
    var imei = body.imei;
    if (imei === undefined) {
        errorReturn(pool.con, ip, "{8DCC1BB0-D890-459C-92A7-739B407468B7}", rsp, respObj);
        return;
    }

    ticket_for_ticket_dao.selectByUUID(pool.con, ticket_for_ticket).then(function (res) {
        if (res.rows.length == 1) {
            var cur_date = new Date();
            var dtc = new Date(res.rows[0].dtc);
            var difference = cur_date - dtc;
            if (difference < 30 * 1000) {
                tkeys_dao.selectByKey(pool.con, res.rows[0].uuid_keys).then(function (res) {
                    if (res.rows.length == 1) {
                        var privKeyStr = res.rows[0].front_pri_key;
                        var privKey = forge.pki.privateKeyFromPem(privKeyStr);
                        //login
                        var login_cr = forge.util.decode64(login_cr_b64);
                        var login = privKey.decrypt(login_cr);
                        //pass
                        var pass_hash_cr = forge.util.decode64(pass_hash_cr_b64);
                        var pass_hash = privKey.decrypt(pass_hash_cr);
                        pass_hash = pass_hash.replace("\n", "");
                        var uuid_key = res.rows[0].uuid;
                        var mobPubKeyStr = res.rows[0].mob_pub_key;
                        var mobPubKey = forge.pki.publicKeyFromPem(mobPubKeyStr);
                        //check login_pass
                        tuser_dao.selectCount(pool.con, login, pass_hash).then(function (res) {
                            if (res.rows.length === 1) {
                                var ticket_for_token = UUID();
                                ticket_for_token_dao.insert(pool.con, ticket_for_token, http_util.getIp(req), imei, uuid_key).then(function (res) {
                                    ticket_for_ticket_dao.deleteByKey(pool.con, ticket_for_ticket).then(function (res) {
                                        respObj[p_result] = 'OK';
                                        var encriptedTicketForToken = mobPubKey.encrypt(forge.util.encodeUtf8(ticket_for_token));
                                        var encriptedTicketForToken64 = forge.util.encode64(encriptedTicketForToken);
                                        respObj[p_ticket_for_token] = encriptedTicketForToken64;
                                        rsp.end(JSON.stringify(respObj));
                                    }).catch(function (err) {
                                        sendError(rsp, "{96002FCE-2B74-43A7-BDCC-EA9F485F490B}");
                                    });
                                }).catch(function (err) {
                                    sendError(rsp, "{AACEF941-A992-43D8-8C6D-80A8F4FFCC34}");
                                });
                            } else {
                                var error = 'LoginPassError';
                                insertError(pool.con, ip, error);
                                respObj[p_result] = 'Error';
                                respObj[p_server_error] = error;
                                respObj[p_ticket_for_token] = "";
                                rsp.end(JSON.stringify(respObj));
                            }
                        }).catch(function (err) {
                            sendError(rsp, "{56524DC0-6C72-4422-A0B7-8EE457BED7D8}");
                        })
                    } else {
                        sendError(rsp, "{BDBBF7C8-1659-42B5-BA0E-975E7244C3F2}");
                    }
                }).catch(function (err) {
                    sendError(rsp, "{D73E856D-C50F-4E08-AED3-03A6E7FC9512}");
                })
            } else {
                var error = 'TicketForTicket Outdated';
                insertError(pool.con, ip, error);
                respObj[p_result] = 'Error';
                respObj[p_server_error] = error;
                respObj[p_ticket_for_token] = "";
                rsp.end(JSON.stringify(respObj));
            }
        } else {
            var error = 'TicketForTicket Error';
            insertError(pool.con, ip, error);
            respObj[p_result] = 'Error';
            respObj[p_server_error] = error;
            respObj[p_ticket_for_token] = "";
            rsp.end(JSON.stringify(respObj));
        }
    })
        .catch(function (err) {
            console.log(err);
            sendError(rsp, "{994B8382-60C3-4D20-B146-D458E1354C4E}")
        });

};

const getToken = (req, rsp) => {
    var respObj = {};
    var p_result = 'result';
    var p_token = 'token';
    var body = req.body;
    var ticket_for_token = body.ticket_for_token;
    if (ticket_for_token === undefined) {
        errorReturn(pool.con, ip, "{658BA907-9332-40A4-838C-06C5AD7E5D8F}", rsp, respObj);
        return;
    }
    var imei = body.imei;
    var ip = http_util.getIp(req);
    if (imei === undefined) {
        errorReturn(pool.con, ip, "{53211817-6FCC-4408-9096-5DF7A718FC57}", rsp, respObj);
        return;
    }
    ticket_for_token_dao.selectByUUID(pool.con, ticket_for_token, imei, ip).then(function (res) {
        if (res.rows.length == 1) {
            var cur_date = new Date();
            var dtc = new Date(res.rows[0].dtc);
            var difference = cur_date - dtc;
            if (difference < 24 * 60 * 60 * 1000) {
                var token = UUID();
                var uuid_keys = res.rows[0].uuid_keys;
                token_dao.deleteBy(pool.con, imei, ip).then(function (res) {
                    token_dao.insert(pool.con, token, ticket_for_token, imei, ip, uuid_keys, 0, 0).then(function (res) {
                        tkeys_dao.selectByKey(pool.con, uuid_keys).then(function (res) {
                            if (res.rows.length == 1) {
                                var mobPubKeyStr = res.rows[0].mob_pub_key;
                                var mobPubKey = forge.pki.publicKeyFromPem(mobPubKeyStr);
                                respObj[p_result] = 'OK';
                                var encriptedToken = mobPubKey.encrypt(forge.util.encodeUtf8(token));
                                var encriptedToken64 = forge.util.encode64(encriptedToken);
                                respObj[p_token] = encriptedToken64;
                                rsp.end(JSON.stringify(respObj));
                            } else {
                                errorReturn(pool.con, ip, "{E44D3277-3C20-4B9A-B6A6-9E68D7600B64}", rsp, respObj);
                            }
                        }).catch(function (err) {
                            sendError(rsp, "{F13A194E-84B8-4E2C-B77F-6C55C0D18991}");
                        })
                    }).catch(function (err) {
                        sendError(rsp, "{8C1442BA-3DBE-418F-92D5-5B5CC0938D75}");
                    })
                }).catch(function (err) {
                    sendError(rsp, "{2BC30B19-0775-4C39-A01D-139FB22334AA}");
                })
            } else {
                var error = 'TicketForToken Outdated';
                insertError(pool.con, ip, error);
                respObj[p_result] = 'Error';
                respObj[p_server_error] = error;
                respObj[p_token] = "";
                rsp.end(JSON.stringify(respObj));
            }
        } else {
            var error = 'TicketForToken Error';
            insertError(pool.con, ip, error);
            respObj[p_result] = 'Error';
            respObj[p_server_error] = error;
            respObj[p_token] = "";
            rsp.end(JSON.stringify(respObj));
        }
    }).catch(function (err) {
        sendError(rsp, "{67FE5B79-262C-409E-9BE0-B8E1EDCF39BF}");
    })
};

const getData = (req, rsp) => {
    token_validator.validateToken(req).then(function (res) {
        if (res) {
            var respObj = {};
            respObj["data"] = "0"
            rsp.end(JSON.stringify(respObj))
        } else {
            var respObj = {}
            respObj["error"] = "token error"
            rsp.end(JSON.stringify(respObj))
        }
    }).catch(function (err) {

    });

    // var token_cr_64 = req.header("token_cr_64")
    // var imei = req.header("imei")
    // var ip = getIp(req)
    // token_dao.selectBy(pool.con, imei, ip).then(function (res) {
    //     if (res.rows.length == 1) {
    //         var uuid_keys = res.rows[0].uuid_keys;
    //         var token_db = res.rows[0].token;
    //         var user_key_db_int = res.rows[0].use_key;
    //         var use_count_db_int = res.rows[0].use_count;
    //         tkeys_dao.selectByKey(pool.con, uuid_keys).then(function (res) {
    //             if (res.rows.length == 1) {
    //                 var privKeyStr = res.rows[0].front_pri_key;
    //                 var privKey = forge.pki.privateKeyFromPem(privKeyStr);
    //                 var token_cr = forge.util.decode64(token_cr_64);
    //                 var token = privKey.decrypt(token_cr);
    //                 if (token.includes(token_db)) {
    //                     var user_key = token.replace(token_db, '');
    //                     if (!Number.isNaN(Number(user_key))) {
    //                         var user_key_int = parseInt(user_key, 10);
    //                         if (user_key_int !== NaN) {
    //                             if (user_key_db_int < user_key_int) {
    //                                 user_key_int++;
    //                                 if (user_key_int > 10000) {
    //
    //                                 }
    //                                 use_count_db_int++;
    //                                 token_dao.updateByKey(pool.con, token_db, user_key_int, use_count_db_int).then(function (res) {
    //                                     var respObj = {};
    //                                     respObj["data"] = "0"
    //                                     rsp.end(JSON.stringify(respObj));
    //                                 })
    //                             }
    //                         }
    //                     }
    //                 }
    //             }
    //         })
    //     }
    // })
};

const mobScriptSend = (req, rsp) => {
    var respObj = {};
    // token_validator.validateToken(req).then(function (res) {
    //     if (res) {
            var login = req.header('login');
            if (login === undefined) {
                console.log(JSON.parse('{\"error\":\"{242A3A81-FFCD-4AF2-8FA7-E6633B235471}\"}'));
                respObj[p_result] = "fail"
                respObj[p_server_error] = "{242A3A81-FFCD-4AF2-8FA7-E6633B235471}\"}"
                rsp.end(JSON.stringify(respObj))
                return;
            }
            tscriptstomob.getScriptToMobNew(pool.con, login).then(function (response) {
                if (response.rowCount == 0) {
                    respObj[p_result] = "no";
                    rsp.end(JSON.stringify(respObj)); //{"result":"no"}
                    return
                } else {
                    var respObjInner = {};
                    respObjInner["id_script"] = response.rows[0].uuid
                    respObjInner["script"] = response.rows[0].script
                    respObjInner["type_script"] = response.rows[0].type_script
                    respObjInner["db_name"] = response.rows[0].db_name
                    respObjInner["t_name"] = response.rows[0].t_name
                    respObjInner["dtc"] = response.rows[0].dtc
                    respObj["data"] = respObjInner

                    tscriptstomob.updDtSendByKey(pool.con, new Date().toLocaleString(), response.rows[0].uuid).then(function (res) {
                        if (res.rowCount == 1) {
                            respObj[p_result] = "ok"
                            rsp.end(JSON.stringify(respObj)) // {"data":{"id_script":"a7104fa5-a741-4ffd-8d6e-a463d8a4938d","script":"SELECT * FROM TUSERS","type_script":"SELECT","db_name":"local.db","dtc":"2020-01-17T10:43:20.000Z"},"result":"ok"}
                            return
                        } else {
                            console.log(JSON.parse('{\"error\":\"{95E1FF45-E9FB-4298-8ECE-27D5364FEE9D} uuid = ' + response.rows[0].uuid + '\"}'));
                            respObj = {}
                            respObj[p_result] = "fail"
                            respObj[p_server_error] = "{95E1FF45-E9FB-4298-8ECE-27D5364FEE9D} to uuid = " + response.rows[0].uuid
                            rsp.end(JSON.stringify(respObj)) // TODO: нужно ли в таком случае возвращать на мобильный ошибку?
                            //{"result":"fail","server_error":"{95E1FF45-E9FB-4298-8ECE-27D5364FEE9D} to uuid = a7104fa5-a741-4ffd-8d6e-a463d8a4938d"}
                            return
                        }
                    }).catch(function (err) {
                        console.log(JSON.parse('{\"error\":\"{0EEBDC82-202B-4FF6-BF71-E573E55E631B} uuid = ' + response.rows[0].uuid + 'err = ' + err + '\"}'));
                        respObj = {}
                        respObj[p_result] = "fail"
                        respObj[p_server_error] = "{0EEBDC82-202B-4FF6-BF71-E573E55E631B} to uuid = " + response.rows[0].uuid + " err = " + err
                        rsp.end(JSON.stringify(respObj)) // TODO: нужно ли в таком случае возвращать на мобильный ошибку?
                        return
                    });
                }
            }).catch(function (err) {
                console.log(JSON.parse('{\"error\":\"{8C83058B-2874-4DF2-A392-9DBA34F63664} login = ' + login + 'err = ' + err + '\"}'));
                respObj[p_result] = "fail";
                respObj[p_server_error] = "{8C83058B-2874-4DF2-A392-9DBA34F63664} to login = " + login + " err = " + err;
                rsp.end(JSON.stringify(respObj));
                return
            });
    //     } else {
    //         respObj[p_result] = "fail";
    //         respObj[p_server_error] = "token error"
    //         rsp.end(JSON.stringify(respObj))
    //     }
    // }).catch(function (err) {
    //     respObj[p_result] = "fail";
    //     respObj[p_server_error] = "{822A5279-074B-480B-AB8C-CB16B6B30DBB} to " + login + " err = " + err
    //     rsp.end(JSON.stringify(respObj))
    // });
};

const mobScriptAnswer = (req, rsp) => {
    var respObj = {};
    // token_validator.validateToken(req).then(function (res) {
    //     if (res) {
    var body = req.body;

    // анализ результатов работы мобильного устройства
    if (body.Error != "") {
        console.log(JSON.parse('{\"error\":\"{89A74C31-6149-46FD-8EF0-2FF4D9C07E1B} на мобильном произошла ошибка: ' + body.Error + '\"}'));
        respObj[p_result] = "ok";
        rsp.end(JSON.stringify(respObj))
        return
    }

    // если все хорошо, то получаем id_script
    var answer = JSON.stringify(body.Result)
    var id_script = req.header("id_script")
    var typeAnswer = req.header("type_answer")
    // анализ результата id
    if (id_script == undefined || id_script =='-1') { // скорей всего не попадем. но если попали, то в андройде что-то неправильно
        console.log(JSON.parse('{\"error\":\"{BBE41299-CB96-492B-9E57-CCF38B4F4400} Error MobScriptAnswer: id_script = ' + id_script + '\", \"answer\" : ' + answer + '}'));
        respObj[p_result] = "fail";
        respObj[p_server_error] = "{BBE41299-CB96-492B-9E57-CCF38B4F4400}"
        rsp.end(JSON.stringify(respObj))
        return
    }

    // проверка, есть ли в базе id_script
    tscriptstomob.selectCountSendScriptByKey(pool.con, id_script).then(function (isScriptExiist) {
        if (isScriptExiist.rowCount == 1) {
            if(typeAnswer == 'result_send'){
// вставка результата отправки скрипта
                tscriptstomob.updResultSendByKey(pool.con, answer, id_script).then(function (res) {
                    if (res.rowCount == 1) {
                        respObj[p_result] = "ok" // {"result":"ok"}
                        rsp.end(JSON.stringify(respObj))
                        return
                    } else {
                        console.log(JSON.parse('{\"error\":\"{ECDF4BC0-0D4E-4A9A-9C74-0D6599D33346} id_script = ' + id_script + '\", \"answer\" : ' + answer + '}'));
                        respObj[p_result] = "fail"
                        respObj[p_server_error] = "{ECDF4BC0-0D4E-4A9A-9C74-0D6599D33346} уже получен ответ to uuid = " + id_script
                        rsp.end(JSON.stringify(respObj)) // TODO: нужно ли в таком случае возвращать на мобильный ошибку?
                        // {"result":"fail","server_error":"{ECDF4BC0-0D4E-4A9A-9C74-0D6599D33346} уже получен ответ to uuid = b001a969-6272-40de-a2f6-fdfccda1811b"}
                        return
                    }

                }).catch(function (err) {
                    console.log(JSON.parse('{\"error\":\"{F75932D8-E315-4341-8113-F44F37A64E5C} id_script = ' + id_script + ' typeAnswer =' + typeAnswer + '\", \"answer\" : ' + answer + '}'));
                    respObj[p_result] = "fail";
                    respObj[p_server_error] = "{F75932D8-E315-4341-8113-F44F37A64E5C} to id_script = " + id_script + ' typeAnswer =' + typeAnswer;
                    rsp.end(JSON.stringify(respObj));
                    return
                });
            }
            else if(typeAnswer == 'result_interim') {
                // вставка ответа
                tscriptstomob.updResultInterimByKey(pool.con, answer, id_script).then(function (res) {
                    if (res.rowCount == 1) {
                        respObj[p_result] = "ok" // {"result":"ok"}
                        rsp.end(JSON.stringify(respObj))
                        return
                    } else {
                        console.log(JSON.parse('{\"error\":\"{F8A9E5F0-4742-4009-89EC-D1A106853F45} id_script = ' + id_script + '\", \"answer\" : ' + answer + '}'));
                        respObj[p_result] = "fail"
                        respObj[p_server_error] = "{F8A9E5F0-4742-4009-89EC-D1A106853F45} уже получен ответ to uuid = " + id_script
                        rsp.end(JSON.stringify(respObj)) // TODO: нужно ли в таком случае возвращать на мобильный ошибку?
                        // {"result":"fail","server_error":"{890021C9-3CBB-41FF-A55F-8B02985A1F19} уже получен ответ to uuid = b001a969-6272-40de-a2f6-fdfccda1811b"}
                        return
                    }

                }).catch(function (err) {
                    console.log(JSON.parse('{\"error\":\"{8ED1E9A9-B826-4B29-83AA-DFFB647390BA} id_script = ' + id_script + ' typeAnswer =' + typeAnswer + '\", \"answer\" : ' + answer + '}'));
                    respObj[p_result] = "fail";
                    respObj[p_server_error] = "{8ED1E9A9-B826-4B29-83AA-DFFB647390BA} to id_script = " + id_script + ' typeAnswer =' + typeAnswer;
                    rsp.end(JSON.stringify(respObj));
                    return
                });
            }
            else if(typeAnswer == 'answer') {
                // вставка ответа
                tscriptstomob.updAnswerByKey(pool.con, answer, id_script).then(function (res) {
                    if (res.rowCount == 1) {
                        respObj[p_result] = "ok" // {"result":"ok"}
                        rsp.end(JSON.stringify(respObj))
                        return
                    } else {
                        console.log(JSON.parse('{\"error\":\"{F75932D8-E315-4341-8113-F44F37A64E5C} id_script = ' + id_script + '\", \"answer\" : ' + answer + '}'));
                        respObj[p_result] = "fail"
                        respObj[p_server_error] = "{890021C9-3CBB-41FF-A55F-8B02985A1F19} уже получен ответ to uuid = " + id_script
                        rsp.end(JSON.stringify(respObj)) // TODO: нужно ли в таком случае возвращать на мобильный ошибку?
                        // {"result":"fail","server_error":"{890021C9-3CBB-41FF-A55F-8B02985A1F19} уже получен ответ to uuid = b001a969-6272-40de-a2f6-fdfccda1811b"}
                        return
                    }

                }).catch(function (err) {
                    console.log(JSON.parse('{\"error\":\"{F75932D8-E315-4341-8113-F44F37A64E5C} id_script = ' + id_script + '; err = ' + err + '; typeAnswer =' + typeAnswer + '\", \"answer\" : ' + answer + '}'));
                    respObj[p_result] = "fail";
                    respObj[p_server_error] = "{F75932D8-E315-4341-8113-F44F37A64E5C} to id_script = " + id_script + ' typeAnswer =' + typeAnswer;
                    rsp.end(JSON.stringify(respObj));
                    return
                });
            }
            else{
                console.log(JSON.parse('{\"error\":\"{B5841FA6-C5B7-40FF-A118-B903DFB36E54} id_script = ' + id_script  + '; typeAnswer =' + typeAnswer + '\", \"answer\" : ' + answer + '}'));
                respObj[p_result] = "fail";
                respObj[p_server_error] = "{F75932D8-E315-4341-8113-F44F37A64E5C} to id_script = " + id_script + " err = " + err + '; typeAnswer =' + typeAnswer;
                rsp.end(JSON.stringify(respObj));
                return
            }
        }
        else{
            var error = "{FF126F91-841C-46E9-93A2-A912AFC34449} на сервере нет скрипта с id_script = " + id_script + '; typeAnswer =' + typeAnswer
            console.log(JSON.parse('{\"error\":\"'+ error +'\", \"answer\" : ' + answer + '}'));
            respObj[p_result] = "fail";
            respObj[p_server_error] = error;
            rsp.end(JSON.stringify(respObj));
            return
        }
    }).catch(function (err) {
        console.log(JSON.parse('{\"error\":\"{93F52CF0-13DF-44D3-99E1-04F4F07F6383} id_script = ' + id_script + ' err = ' + err + '\", \"answer\" : ' + answer + '}'));
        respObj[p_result] = "fail";
        respObj[p_server_error] = "{93F52CF0-13DF-44D3-99E1-04F4F07F6383} to id_script = " + id_script + " err = " + err;
        rsp.end(JSON.stringify(respObj));
        return
    });
    //     } else {
    //         respObj[p_result] = "fail";
    //         respObj[p_server_error] = "token error"
    //         rsp.end(JSON.stringify(respObj))
    //     }
    // }).catch(function (err) {
    //     console.log(JSON.parse('\"error\":\"{174C33B5-6FF0-4A0C-958B-C81688ACE535} err = ' + err + '\"'));
    //     respObj[p_result] = "fail";
    //     respObj[p_server_error] = "{174C33B5-6FF0-4A0C-958B-C81688ACE535} err = " + err
    //     rsp.end(JSON.stringify(respObj))
    // });
};

module.exports = {
    isServerOnline, getTicketForTicket, getTicketForToken, getToken, getData, mobScriptSend, mobScriptAnswer
};

