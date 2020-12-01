const pool = require('../../data/db_pool');
//const UUID = require('uuid/v4');
const {v4: UUID} = require('uuid');
const encript = require('./encript');
const decript = require('./decript');
const tadm_tickticket_dao = require('../../data/tadm_tickticket_dao');
const tadm_ticktoken_dao = require('../../data/tadm_ticktoken_dao');
const tadm_users_dao = require('../../data/tadm_users_dao');
const tadm_keys_dao = require('../../data/tadm_keys_dao');
const tadm_error_pass_dao = require('../../data/tadm_error_pass_dao');

let getNewTicketForToken = (connection, ticketforticket, ip, pass_hash_en_b64) => {
    //Получаем в заголовке билет на билет в незашефрованом виде (отправляли мы его клиенту в зашифрованном виде)
    // Создаем незарегистрированный, поддельный билет на токен
    //  Проверка заголовков:
    //      Если результат проверки отрицательный:
    //          Возвращаем незарегистрированный, поддельный билет на токен
    //      Если все заголовки есть
    //          Проверяем существование в базе билета на билет
    //              Если билета на билет нет
    //                  Возвращаем незарегистрированный, поддельный билет на токен
    //              Если билет есть
    //                  Проверяем его действительность
    //                      Если не действителен
    //                          Возвращаем сообщение о том что время действия билета на билет истекло
    //                      Если действителен
    //                          Проверяем пользователя на lock
    //                              Если залочен
    //                                  Возвращаем незарегистрированный, поддельный токен токен
    //                              Если не залочен
    //                                  Проверяем временную блокировку
    //                                      Если есть временная блокировка
    //                                          Возвращаем сообщение о том до какого времени вход запрещен
    //                                      Если нет временной блокировки
    //                                          Расшифровываем хэш пароль переданный в теле запроса
    //                                          Сравниваем с хэшем из базы
    //                                              Если хэш не правельный
    //                                                  Записываем номер повторной попытки в таблице TPASS_ERR увеличивая существующий на 1
    //                                                      Если номер повторной попытки больше 4
    //                                                          Устанавливаем временную блокировку
    //                                                      Отправляем сообщение о неправельном пароле
    //                                              Если хэш правельный
    //                                                  Обнуляем количество повторных попыток
    //                                                  Отмечаем билет на билет как использованый
    //                                                  Создаем билет на токен
    //                                                  Объявляем все ранее выданные билеты на токен, данному пользователю недействительными
    //                                                  Записываем билет на токен в базу данных
    //                                                  Шифруем открытым ключем клиента билет на токен и переводим в base64
    //                                                  Добавляем зашифрованный билет на токен в тело ответа и отправляем

    return new Promise(function (resolve, reject) {
        // Создаем незарегистрированный, поддельный билет на токен
        const ticketForToken = UUID().toString();
        encript.encriptValue(ticketForToken, undefined).then(function (ticketForTokenF) {
            //  Проверка заголовков:
            if (ticketforticket === undefined) {
                //      Если результат проверки отрицательный:
                //          Возвращаем незарегистрированный, поддельный билет на токен
                resolve(
                    {
                        ticket_for_token: ticketForTokenF
                    }
                )
            } else {
                //      Если все заголовки есть
                //          Проверяем существование в базе билета на билет
                tadm_tickticket_dao.get_tickticket_data(connection, ticketforticket).then(function (result) {
                    if (result.outBinds.po_exist === 0) {
                        resolve(
                            {
                                ticket_for_token: ticketForTokenF
                            })
                    } else {
                        //              Если билет есть
                        //                  Проверяем что не использовался
                        if (result.outBinds.po_dt_use !== null) {
                            resolve(
                                {
                                    expired: true
                                })
                        } else {
                            //                  Проверяем его действительность
                            if (result.outBinds.po_dt_exp.getTime() > (new Date()).getTime()) {
                                //                  Ищем строку пользователя по билету
                                //Проверяем пользователя на lock
                                if (result.outBinds.po_user_lock) {
                                    //  Если залочен
                                    resolve(
                                        {
                                            ticket_for_token: ticketForTokenF
                                        })
                                } else {
                                    //Проверяем временную блокировку
                                    if (result.outBinds.po_user_time_lock !== undefined && result.outBinds.po_user_time_lock !== null) {
                                        if (result.outBinds.po_user_time_lock.getTime() > (new Date()).getTime()) {
                                            // Если таймлок
                                            resolve(
                                                {
                                                    time_lock: result.outBinds.po_user_time_lock
                                                });
                                        }
                                    }
                                    //Находим клиентский открытый ключ
                                    const key_uuid = result.outBinds.po_uuid_keys;
                                    tadm_keys_dao.select_keys(connection, key_uuid).then(function (key_result) {
                                            if (key_result.outBinds.exist == 0) {
                                                reject("{FA774649-0B2C-40E2-916E-5506837FBA0C}");
                                            }
                                            //Расшифровываем пароль
                                            decript.decriptValue(pass_hash_en_b64, key_result.outBinds.po_front_pri_key).then(function (decr_result) {
                                                    //Проверка пароля
                                                    if (result.outBinds.po_pass == decr_result) {
                                                        //Пароль подходит
                                                        //Зашифровать билет
                                                        encript.encriptValue(ticketForToken, key_result.outBinds.po_client_pub_key).then(function (ticket_for_token_en_b64) {
                                                            tadm_ticktoken_dao.update_use(connection, result.outBinds.po_id_user).then(function (updateuse_result) {
                                                                //Сохраняем билет в базу данных
                                                                var dt_exp = new Date();
                                                                dt_exp.setTime(dt_exp.getTime() + 1000 * 60 * 60 * 2);
                                                                tadm_ticktoken_dao.insert_ticktoken(
                                                                    connection,
                                                                    ticketForToken,
                                                                    dt_exp,
                                                                    result.outBinds.po_id_user,
                                                                    ip,
                                                                    key_uuid
                                                                ).then(function (insert_result) {

                                                                    //3. Возвращаем созданный билет
                                                                    resolve(
                                                                        {
                                                                            ticket_for_token: ticket_for_token_en_b64
                                                                        }
                                                                    )

                                                                }).catch(function (err) {
                                                                    reject("{A9DCFE30-8732-490A-9002-4776A2AF2B97}");
                                                                })
                                                            }).catch(function (err) {
                                                                reject("{4423E198-20F6-4995-9EEC-CE5ACEEE9F96}");
                                                            })
                                                        }).catch(function (err) {
                                                            reject("{B07DE36F-D25F-4844-BE01-1C376A003F79}" + err);
                                                        })
                                                    } else {
                                                        //Увеличить количество повторных попыток на 1
                                                        tadm_error_pass_dao.select_error_pass(connection, result.outBinds.po_id_user).then(function (errpass_result) {
                                                                //Проверяем количество попыток
                                                                let attempt_num = errpass_result.outBinds.po_attempt_num + 1;
                                                                if (attempt_num > 4) {
                                                                    tadm_users_dao.update_timelock(connection, result.outBinds.po_id_user).then(function () {
                                                                        tadm_error_pass_dao.set_error_pass(connection, result.outBinds.po_id_user, 0).then(function () {
                                                                            resolve({pass_error: attempt_num});
                                                                        }).catch(function (err) {
                                                                            reject("{91656317-6DB4-45DA-A738-5AC4C283175F;}" + err);
                                                                        })
                                                                    }).catch(function (err) {
                                                                        reject("{2C87E652-A651-4F5A-B679-4CD4FF2E9A64};" + err);
                                                                    });
                                                                } else {
                                                                    tadm_error_pass_dao.set_error_pass(connection, result.outBinds.po_id_user, attempt_num).then(function () {
                                                                        resolve(
                                                                            {
                                                                                pass_error: attempt_num
                                                                            }
                                                                        )
                                                                    }).catch(function (err) {
                                                                        reject("{408FAC91-8F43-47DA-AB14-8FE95AE43F53}");
                                                                    })
                                                                }
                                                            }
                                                        ).catch(function (err) {
                                                            reject("{E4C242CD-4C6B-401D-B976-C46D304E64B8};" + err);
                                                        })
                                                    }

                                                }
                                            ).catch(function (err) {
                                                reject("{AC048A7B-BEB2-49A1-BB79-3D3366C304A0}");
                                            });


                                        }
                                    ).catch(function (err) {
                                        reject("{98125C7E-E955-4A04-B9C6-7F56CBBBABBE};" + err);
                                    });
                                }

                            } else {
                                //    Возвращаем сообщение о том что время действия билета на билет истекло
                                resolve(
                                    {
                                        expired: true
                                    })
                            }
                        }
                    }
                }).catch(function (err) {
                    reject("{877BD7CC-38FE-4BA3-8200-F030296D0054};" + err);
                });
            }
        }).catch(function (err) {
            reject("{F155A5C9-05D9-40BC-82E2-0B4FC2B81287};" + err)
        });
    });
};


module.exports = {getNewTicketForToken};
