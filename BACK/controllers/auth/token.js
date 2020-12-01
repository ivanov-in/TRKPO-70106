const pool = require('../../data/db_pool');
//const UUID = require('uuid/v4');
const {v4: UUID} = require('uuid');
const tadm_token_dao = require('../../data/get_token_data');
const tadm_ticktoken_dao = require('../../data/tadm_ticktoken_dao');
const tadm_users_dao = require('../../data/tadm_users_dao');
const encript = require('./encript');
const tadm_keys_dao = require('../../data/tadm_keys_dao');

let getNewToken = (connection, ticketfortoken, ip) => {
    //Создаем поддельный токен
    // Зашифровываем поддельный токен и переводим в base64
    //Проверка наличия заголовка с билетом на токен в незашифрованном виде
    //  Если нет
    //      Возвращаем в теле ответа поддельный токен
    //  Если есть
    //      Ищем билет на токен в базе данных
    //          Если не найден
    //              Возвращаем в теле ответа поддельный токен
    //          Если найден
    //              Находим строку пользователя в таблице пользователей
    //                  Проверяем действительность билета
    //                      Если не действителен
    //                          В билет записываем дату использования
    //                          Сообщаем о том что билет недействителен
    //                      Если действителен
    //                          Обявляем все действительные билеты на токен пользователя недействительными
    //                          Создаем токен
    //                          Записываем токен в таблицу токенов
    //                          В билет записываем дату создания токена и дату использования
    //                          Зашифровываем токен
    //                          Отправляем зашифрованный токен через тело запроса

    return new Promise(function (resolve, reject) {
        //Создаем поддельный токен
        var token = UUID().toString();
        // Зашифровываем поддельный токен и переводим в base64
        encript.encriptValue(token, undefined).then(function (tokenF_en_b64) {
            //Ищем билет на токен в базе данных
            tadm_ticktoken_dao.get_ticktoken_data(connection, ticketfortoken).then(function (ticktoken_rows) {
                //Если нет. Возвращаем зашифрованный поддельный токен.
                if (ticktoken_rows.outBinds.po_exist == 0) {
                    resolve({token: tokenF_en_b64});
                } else {
                    //Проверяем действительность билета
                    if (ticktoken_rows.outBinds.po_dt_exp.getTime() > (new Date()).getTime()) {
                        //Если дейсвие билета не истекло
                        //проверяем что билет не использовался
                        if (ticktoken_rows.outBinds.po_dt_use === null) {

                            if (ticktoken_rows.outBinds.po_exist == 1) {
                                //Проверяем пользователя на lock
                                if (ticktoken_rows.outBinds.po_user_lock) {
                                    encript.encriptValue(UUID().toString(), undefined).then(function (ticket_for_token_en_b64) {
                                        //  Если залочен
                                        resolve(
                                            {
                                                token: tokenF_en_b64,
                                                ticket_for_token: ticket_for_token_en_b64
                                            })
                                    }).catch(function (err) {
                                        reject("{E863F1D0-9254-4F54-B5CB-D72E5B5EF778};" + err)
                                    })
                                } else {
                                    //Проверяем временную блокировку
                                    if (ticktoken_rows.outBinds.po_user_time_lock !== undefined && ticktoken_rows.outBinds.po_user_time_lock !== null) {
                                        if (ticktoken_rows.outBinds.po_user_time_lock.getTime() > (new Date()).getTime()) {
                                            // Если таймлок
                                            resolve(
                                                {
                                                    time_lock: ticktoken_rows.outBinds.po_user_time_lock
                                                })
                                        }
                                    }
                                    //Находим клиентский открытый ключ
                                    const key_uuid = ticktoken_rows.outBinds.po_uuid_keys;
                                    tadm_keys_dao.select_keys(connection, key_uuid).then(function (key_result) {
                                        if (key_result.outBinds.po_exist == 0) {
                                            reject("{DAC36A32-A7C5-49DE-AA83-B60B6640EC1C}");
                                        }
                                        //Сохраняем token
                                        var dt_exp = new Date();
                                        //dt_exp.setTime(dt_exp.getTime() + 1000 * 60 * 60 * 2);// 2 H
                                        dt_exp.setTime(dt_exp.getTime() + 1000 * 60 * 2);// 2 M
                                        var p_token = UUID().toString();
                                        tadm_token_dao.insert_token(connection, p_token, ticketfortoken, ticktoken_rows.outBinds.po_id_user, ip, key_uuid, ticktoken_rows.outBinds.po_login).then(function (ins_token) {
                                            token = p_token;
                                            //Зашифровать token
                                            encript.encriptValue(token, key_result.outBinds.po_client_pub_key).then(function (token_en_b64) {
                                                //отмечаем билет как использованный
                                                tadm_ticktoken_dao.ticktoken_use(connection, ticketfortoken).then(function (upd_settoken) {
                                                    // Создаем новый билет на token
                                                    const ticketForToken = UUID().toString();
                                                    // Шифруем билет на token
                                                    encript.encriptValue(ticketForToken, key_result.outBinds.po_client_pub_key).then(function (ticket_for_token_en_b64) {
                                                        //Сохраняем билет в базу данных
                                                        var dt_exp = new Date();
                                                        dt_exp.setTime(dt_exp.getTime() + 1000 * 60 * 60 * 2);
                                                        tadm_ticktoken_dao.insert_ticktoken(
                                                            connection,
                                                            ticketForToken,
                                                            dt_exp,
                                                            ticktoken_rows.outBinds.po_id_user,
                                                            ip,
                                                            key_uuid
                                                        ).then(function (insert_result) {
                                                            // Возвращаем созданный token
                                                            resolve(
                                                                {
                                                                    token: token_en_b64,
                                                                    ticket_for_token: ticket_for_token_en_b64
                                                                }
                                                            )
                                                        }).catch(function (err) {
                                                            reject("{51CB33EF-E4E6-4A1F-A4AA-7A11CDF21278};" + err);
                                                        })
                                                    }).catch(function (err) {
                                                        reject("{23E62566-A511-4830-AD0D-DA8564F32562};" + err);
                                                    })
                                                }).catch(function (err) {
                                                    reject("{407B915D-057D-4361-9A06-5C818A3F6207};" + err);
                                                })
                                            }).catch(function (err) {
                                                reject("{E5C99451-D9AE-40C9-A5CC-7D15ED2CB634};" + err);
                                            })

                                        }).catch(function (err) {
                                            reject("{950D0E33-722A-4269-A96E-E93F64F3A461};" + err)
                                        });


                                    }).catch(function (err) {
                                        reject("{02743383-67E3-4965-B32C-F591D0FFF6AD};" + err);
                                    })
                                }
                            } else {
                                reject("{9A1C0449-9178-4CA0-9F10-2ED8CDEF8DA9}");
                            }
                        } else {
                            //  Если использован
                            //  Возвращаем сообщение о том что время действия билета на токен истекло
                            resolve(
                                {
                                    expired: true
                                })
                        }
                    } else {
                        //    Возвращаем сообщение о том что время действия билета на токен истекло
                        resolve(
                            {
                                expired: true
                            })
                    }
                }
            }).catch(function (err) {
                reject("{2EBF171D-94B0-4359-8BBA-C86D68F279DD};" + err);
            })
        }).catch(function (err) {
            reject("{9E21D020-19F0-4300-9B4A-B4121C6349F8};" + err);
        })

    });
};

module.exports = {getNewToken};
