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
    try {
        await oracledb.createPool(poolOra.hrPool);
        console.log('Initializing database module');
        // let con = oracledb.getConnection();
        // console.log('Test oracle connection');
    } catch (err) {
        console.error(err);
        init();
        //process.exit(1);
    }
}

function connectionRelease(connection) {
    setTimeout(_connectionRelease, 1500, connection);
}

function _connectionRelease(connection) {

    connection.release().then(function () {
        console.log('DB Connection Released');
    }).catch(function (error) {
        process.exit(1521)
    });
}


module.exports = {
    poolOra, init, connectionRelease
};

