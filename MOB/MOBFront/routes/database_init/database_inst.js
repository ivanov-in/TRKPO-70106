const db_ini = require('./database_ini.js');
const log = require('../log/util');

const db_name = db_ini.db_name;
const {Client} = require('pg');

const run = async (callback) => {
    var time = log.nowTime();
    console.log('Find database:' + db_name + ' ' + time);
    databaseExist(function (result) {

        if (!result) {
            var time = log.nowTime();
            console.log('Not found database:' + db_name + ' ' + time);
            console.log('Start create database:' + db_name + ' ' + time);
            databaseCreate(function (result) {
                console.log('End create database:' + db_name + ' result:' + result);
                callback();
            });

        } else {
            var time = log.nowTime();
            console.log('Found database:' + db_name + ' ' + time);
            callback();
        }
    });
}

function databaseExist(callback) {
    const con = new Client({
        connectionString: db_ini.connectionStringRoot
    });
    con.connect();
    var db_exist = false;
    con.query('SELECT datname FROM pg_database', (error, results) => {
        if (error) {
            con.end();
            throw error;
        }
        for (let i = 0; i < results.rows.length; i++) {
            if (results.rows[i].datname == db_name) {
                db_exist = true;
            }
        }
        con.end();
        callback(db_exist);
    });
}

function databaseCreate(callback) {
    const con = new Client({
        connectionString: db_ini.connectionStringRoot
    });
    con.connect();
    var sql_db_create = 'CREATE DATABASE ' + db_name + '\n' +
        '  WITH OWNER = postgres\n' +
        '       ENCODING = \'UTF8\'\n' +
        '       TABLESPACE = pg_default\n' +
        '       LC_COLLATE = \'Russian_Russia.1251\'\n' +
        '       LC_CTYPE = \'Russian_Russia.1251\'\n' +
        '       CONNECTION LIMIT = -1;\n';
    con.query(sql_db_create, (error, results) => {
        if (error) {
            con.end();
            console.log('Error create database:' + db_name);
            callback(true);
        } else {
            var sql_schema_create = 'CREATE SCHEMA public\n' +
                '  AUTHORIZATION postgres;';
            con.query(sql_schema_create, (error, results) => {

                //con.query('COMMIT');
                con.end();
                callback(true);
                return;
            });
        }
    });


}


// let initialize = new Promise((resolve, reject) => {
//     run(() => resolve(true));
// });


module.exports = {run};