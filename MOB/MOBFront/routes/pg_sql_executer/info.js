let info = require('../pg_sql_generator/info.js');

let tableExist = (con, database, schema, table) => {
    return new Promise(function (resolve, reject) {
        var sql = info.tableExist(database, schema, table);
        con.query(sql, (error, results) => {
            if (error) {
                reject('{8D69DD51-CBBC-4625-9807-73DB2D4C3F5A}')
            }
            if (results.rows.length != 1) {
                reject('{F8CF237A-FA00-482D-974E-ECF3C48140CD}');
            }
            if (results.rows[0].count == 1) {
                resolve(true);
            } else {
                resolve(false);
            }
        });
    })
};
let schemaExist = (con, database, schema) => {
    return new Promise(function (resolve, reject) {
        var sql = info.schemaExist(database, schema);
        con.query(sql, (err, results) => {
            if (err) {
                reject('{CCDB3CB8-D2CC-4982-8DBD-B3F186B24FA4}');
            }
            if (results.rows.length != 1) {
                reject('{8CD8F663-26BB-4844-AB48-A6A929C0D388}');
            }
            if (results.rows[0].count == 1) {
                resolve(true);
            } else {
                resolve(false);
            }
        });
    })
};
let fkey_exist = (con, database, schema, key) => {
    return new Promise(function (resolve, reject) {
        var sql = info.fkey_exist(database, schema, key);
        con.query(sql, (err, results) => {
            if (err) {
                reject('{500DF1FA-C0AE-4507-B424-4E34BB2BDD4C}');
            }
            if (results.rows.length != 1) {
                reject('{84C92CDC-A839-4227-B412-CA5DD87CD180}');
            }
            if (results.rows[0].count == 1) {
                resolve(true);
            } else {
                resolve(false);
            }
        });
    });
};

module.exports = {tableExist, schemaExist, fkey_exist};