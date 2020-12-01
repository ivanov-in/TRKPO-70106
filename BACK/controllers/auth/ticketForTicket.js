const pool = require('../../data/db_pool');
const {v4: UUID} = require('uuid');
const tadm_users_dao = require('../../data/tadm_users_dao');
const tadm_tickticket_dao = require('../../data/tadm_tickticket_dao');
const tadm_keys_dao = require('../../data/tadm_keys_dao');
const forge = require('node-forge');
const encript = require('./encript');

let getNewTicketForTicket = (connection, login, client_key_b64, ip) => {
    //1. Создаем билет на билет
    //2. Проверяем существование в базе email:
    //  2.1 Если да:
    //      2.1.1 Все ранее выданные билеты на билет удаляем
    //      2.1.2 Если ползователь залочен
    //          2.1.2.1 Если Да:
    //              2.1.2.1.1 Ничего не делаем
    //          2.1.2.2  Если Нет:
    //              2.1.2 Проверяем что клиент передал свой открытый ключ
    //                  2.1.2.1 Если Да:
    //                      2.1.2.1.1 создаем серверные ключи
    //                      2.1.2.1.2 записываем открытый ключ и серверные ключи в таблицу ключей
    //                      2.1.2.1.3 записываем билет на билет в таблицу
    //          2.1.2.3 Если Нет:
    //              2.1.2.3.1 Ничего не делаем
    //  2.2 Если нет:
    //      2.2.1 Ничего не делаем
    //3. Возвращаем созданный билет
    return new Promise(function (resolve, reject) {
        //1. Создаем билет на билет и шифруем его
        const ticket_for_ticket = UUID().toString();
        const client_key_str = forge.util.decode64(client_key_b64);
        encript.encriptValue(ticket_for_ticket, client_key_str).then(function (ticketForTicketF) {
            //2. Проверяем существование в базе email:
            if (login !== undefined && client_key_b64 !== undefined) {
                tadm_users_dao.getUserData(connection, login).then(function (result) {
                    if (result.outBinds.po_id !== null) {
                        //  2.1 Если да:
                        //      2.1.1 Все ранее выданные, действительные, билеты на билет - удаляем
                        tadm_tickticket_dao.deleteLost(connection, result.outBinds.po_id).then(function (del_result) {
                            connection.commit().then(_=>{
                                //      2.1.2 Если ползователь залочен
                                if (result.outBinds.po_user_lock == 1) {
                                    //          2.1.2.1 Если Да:
                                    resolve(
                                        {
                                            ticket_for_ticket: ticketForTicketF
                                        }
                                    )
                                } else {
                                    // Проверка на Time Lock
                                    if (result.outBinds.po_time_lock > new Date()) {
                                        // Если таймлок
                                        resolve(
                                            {
                                                time_lock: result.outBinds.po_time_lock
                                            }
                                        )
                                    } else {
                                        //          2.1.2.2  Если Нет:
                                        //                      2.1.2.1.1 создаем серверные ключи
                                        var keys = forge.pki.rsa.generateKeyPair({bits: 1024, e: 0x10001});
                                        var serverPubKeyStr = forge.pki.publicKeyToPem(keys.publicKey);
                                        var serverPrvKeyStr = forge.pki.privateKeyToPem(keys.privateKey);
                                        //                      2.1.2.1.2 записываем открытый ключ и серверные ключи в таблицу ключей
                                        tadm_keys_dao.insert_keys(connection, serverPubKeyStr, serverPrvKeyStr, client_key_str).then(function (ins_result) {
                                            //                      2.1.2.1.2 записываем билет на билет в таблицу
                                            var dt_exp = new Date();
                                            dt_exp.setTime(dt_exp.getTime() + 1000 * 60 * 60 * 2);
                                            tadm_tickticket_dao.insert_tickticket(connection, ticket_for_ticket, dt_exp, result.outBinds.po_id, ip, ins_result).then(function (ins_tickticket_res) {
                                                connection.commit().then(_=>{
                                                    //Зашифровываем билет
                                                    encript.encriptValue(ticket_for_ticket, client_key_str).then(function (ticket_for_ticket_en_b64) {
                                                        //3. Возвращаем созданный билет
                                                        resolve(
                                                            {
                                                                ticket_for_ticket: ticket_for_ticket_en_b64,
                                                                frontPubKeyStr: serverPubKeyStr
                                                            }
                                                        );
                                                    }).catch(function (err) {
                                                        reject("{2F850A2D-D07E-4126-8C27-67B687B5F208}" + err)
                                                    });
                                                }).catch(err=>{
                                                    reject("{CB4315C8-999E-45B8-B2EF-BD30F0EE099A}" + err)
                                                });
                                            }).catch(function (err) {
                                                reject(err);
                                            });
                                        }).catch(function (err) {
                                            reject(err);
                                        })
                                    }
                                }
                            });

                        }).catch(function (err) {
                            reject(err);
                        })
                    } else {
                        resolve(
                            {
                                ticket_for_ticket: ticketForTicketF
                            }
                        )
                    }
                }).catch(function (err) {
                    reject(err);
                });
            } else {
                resolve(
                    {
                        ticket_for_ticket: ticketForTicketF
                    }
                )
            }
        }).catch(function (err) {
            reject("{A7EFE86A-F660-4900-9DAA-400AA5512590};" + err);
        });
    });
};

module.exports = {getNewTicketForTicket};
