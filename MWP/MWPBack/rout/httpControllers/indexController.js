let oracledb = require('oracledb');
const ticketForTicket = require('../../controllers/auth/ticketForTicket');
const ticketForToken = require('../../controllers/auth/ticketForToken');
const token = require('../../controllers/auth/token');
const pool = require('../../data/db_pool');


let getConnection = () => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            pool.connectionRelease(connection);
            resolve('ok')
        }).catch(function (err) {
            reject(err)
        });
    })
}



let getticketforticket = (email, client_key_b64, ip) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            //Вызвать функцию создания билета на билет
            ticketForTicket.getNewTicketForTicket(connection, email, client_key_b64, ip).then(function (result) {
                pool.connectionRelease(connection);
                let ticket_for_ticket_64;
                try {
                    ticket_for_ticket_64 = result.ticket_for_ticket;
                } catch (e) {
                    //Обработка серверных ошибок
                    reject({error: "server error"});
                }
                if (result.time_lock !== undefined) {
                    //Вернуть информацию о времени до которого пользователь
                    resolve({time_lock: result.time_lock});

                } else {
                    //Вернуть билет полученный из функции создания билета на билет
                    resolve(
                        {
                            data:
                                {
                                    ticket_for_ticket: ticket_for_ticket_64,
                                    server_key: result.frontPubKeyStr
                                }
                        }
                    );
                }
            }).catch(function (err) {
                //Обработка серверных ошибок
                pool.connectionRelease(connection);
                reject({error: "server error"});
            });
        }).catch(function (error) {
            //Обработка серверных ошибок
            reject({error: "server error"});
        });
    });
}

let getticketfortoken = (ticketforticket, ip, body_json) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            ticketForToken.getNewTicketForToken(connection, ticketforticket, ip, body_json.pass).then(function (result) {
                pool.connectionRelease(connection);
                //Вернуть билет полученный из функции создания билета на билет
                if (result.expired !== undefined) {
                    resolve({ticket_for_ticket: "date expired"});
                } else if (result.time_lock !== undefined) {
                    resolve({time_lock: result.time_lock});
                } else if (result.pass_error !== undefined) {
                    resolve({pass_error: result.pass_error});
                } else {
                    resolve({ticket_for_token: result.ticket_for_token});
                }
            }).catch(function (err) {
                pool.connectionRelease(connection);
                //Обработка серверных ошибок
                reject({error: "server error"});
            })
        }).catch(function (error) {
            //Обработка серверных ошибок
            reject({error: "server error"});
        });
    });
}

let gettoken = (ticketfortoken, ip) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            //Вызвать функцию создания токена и нового билета на токен
            token.getNewToken(connection, ticketfortoken, ip).then(function (result) {
                pool.connectionRelease(connection);
                //Вернуть билет полученный из функции создания билета на билет
                if (result.expired !== undefined) {
                    resolve({ticket_for_ticket: "date expired"});
                } else if (result.time_lock !== undefined) {
                    resolve({time_lock: result.time_lock});
                } else {
                    resolve({token: result.token, ticket_for_token: result.ticket_for_token});
                }
            }).catch(function (err) {
                pool.connectionRelease(connection);
                //Обработка серверных ошибок
                reject({error: "server error"});
            });
        }).catch(function (error) {
            //Обработка серверных ошибок
            reject({error: "server error"});
        });
    });
}



module.exports = {getticketforticket, getticketfortoken, gettoken,getConnection};
