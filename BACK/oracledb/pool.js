const oracledb = require("oracledb");

const poolConfig = require('./poolConfig');
const logUtil = require('../util/logUtil');

let pool = global.pool;

async function init() {
    logUtil.wi('Start initializing database module ...');
    try {
        global.pool = pool = await oracledb.createPool(poolConfig.hrPool);
        logUtil.wi('Initializing database module success');
    } catch (err) {
        logUtil.we('Initializing database module error');
        logUtil.we(err);
        throw(err)
    }
}

const getConnection = function () {
    return new Promise((resolve, reject) => {
        //logUtil.wi("begin connection open...");
        if (pool.connectionsInUse < pool.poolMax) {
            _getConnection().then(connection => {
                resolve(connection)
            }).then(errCC => {
                reject(errCC)
            })
        } else {
            setTimeout(_ => {
                _getConnection().then(connection => {
                    resolve(connection)
                }).then(errCC => {
                    reject(errCC)
                })
            }, 1000)
        }
    })
}

const _getConnection = function () {
    return new Promise(((resolve, reject) => {
        pool.getConnection(function (err, connection) {
            // UNABLE TO GET CONNECTION - CALLBACK WITH ERROR
            if (err) {
                logUtil.we("Cannot get a connection: ", err);
                reject(err);
            }
            // If pool is defined - show connectionsOpen and connectionsInUse
            if (typeof pool !== "undefined") {
                logUtil.wi(`Pool info: {Connections_open: ${pool.connectionsOpen}, Connections_in_use: ${pool.connectionsInUse}}`);
            }
            process.env.OPENED_CONNECTION = (+process.env.OPENED_CONNECTION + 1).toString()
            resolve(connection);
        });
    }))

}

const connectionRelease = async function (connection) {
    connection.release(function (err) {
        if (err) {
            logUtil.we('Unable to RELEASE the connection: ', err);
            return;
        }
        process.env.CLOSED_CONNECTION = (+process.env.CLOSED_CONNECTION + 1).toString()
        //logUtil.wi(`RELEASE the connection: success; OPENED_CONNECTION: ${process.env.OPENED_CONNECTION}; CLOSED_CONNECTION: ${process.env.CLOSED_CONNECTION}`);
        if (typeof pool !== "undefined") {
            logUtil.wi(`Pool info: {Connections_open: ${pool.connectionsOpen}, Connections_in_use: ${pool.connectionsInUse}}`);
        }
        return;
    });

}

module.exports = {
    init,
    getConnection: getConnection,
    connectionRelease
}
