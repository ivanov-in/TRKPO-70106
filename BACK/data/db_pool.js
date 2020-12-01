let oracledb = require('oracledb');

const poolOra = {
    hrPool: {
        user: "mis",
        password: "mis",
        connectString: "  (DESCRIPTION =\n" +
            "   (ADDRESS_LIST =\n" +
            "     (ADDRESS = (PROTOCOL = TCP)(HOST = ol72-db04)(PORT = 1521))\n" +
            "   )\n" +
            "   (CONNECT_DATA =\n" +
            "     (SERVICE_NAME = tepnet2)\n" +
            "   )\n" +
            " )",
        poolMin: 5,
        poolMax: 8,
        poolIncrement: 1
    }
};

async function init() {
    let pool = null;
    console.log('Start initializing database module ...');
    try {
        pool = await oracledb.createPool(poolOra.hrPool);
        global.pool = pool;
        console.log('Initializing database module success');
    } catch (err) {
        console.error('Initializing database module error');
        console.error(err);
    }
}

 // function _connectionRelease(connection) {
 //    console.log('timeoutRealise')
 //     setTimeout(connectionRelease, 1500, connection);
 // }

async function connectionRelease(connection) {
    connection.release().then(function () {
        console.log('DB Connection Released');
    }).catch(function () {
        process.exit(1521)
        // _connectionRelease(connection)
    });
}


module.exports = {
    poolOra, init, connectionRelease
};

