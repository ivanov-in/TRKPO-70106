let sql_create_table = require('../pg_sql_generator/create_table.js');
const log = require('../log/util');

let createTable = (con, json) => {
    return new Promise(function (resolve, reject) {
        console.log('Start create table: ' + json.name + ' ' + log.nowTime());
        if (json.name === undefined) {
            reject('{9AED08EE-C6AD-4407-A2C3-BB59B5B210E7}');
        } else {
            sql_create_table.getScriptTable(json).then(function (sql) {
                con.query(sql, (err, results) => {
                    if (err) {
                        console.log('Error create table: ' + json.name + ' {803BB419-61DF-422B-83B9-22715F8D321C} ' + log.nowTime());
                        reject('{803BB419-61DF-422B-83B9-22715F8D321C}');
                    } else {
                        createPK(con, json).then(function () {
                            sql_create_table.getScriptIndex(json).then(function (sql_a) {
                                var counter = 0;
                                for (key in sql_a) {
                                    createIndex(con, sql_a[key], json, key).then(function () {
                                        counter++;
                                        if (counter == sql_a.length) {
                                            console.log('End create table: ' + json.name + ' ' + log.nowTime());
                                            resolve();
                                        }
                                    }).catch(function (err) {
                                        reject(err + ',{8D57A44B-FE01-4F59-A868-E8DF3694B059}');
                                    });
                                }
                            }).catch(function (err) {
                                reject(err + ',{A41B117A-CD34-463F-8DEF-A61C95D120B9}');
                            })
                        });
                    }
                });
            });
        }
    });
};

function createPK(con, json) {
    return new Promise(function (resolve, reject) {
        console.log('Start create PK: ' + json.pkey.name + ' ' + log.nowTime());
        sql_create_table.getScriptPK(json).then(function (sql) {
            con.query(sql, (err, results) => {
                if (err) {
                    console.log('Error create PK: ' + json.pkey.name + log.nowTime());
                    reject('{B8431AF3-5CFE-42FE-A00E-67C3552986B3}');
                } else {
                    console.log('End create PK: ' + json.pkey.name + ' ' + log.nowTime());
                    resolve();
                }
            })
        });
    });
}

function createIndex(con, sql, json, arr_index) {
    return new Promise(function (resolve, reject) {
        console.log('Start create Indexes: ' + json.indexes[arr_index].name + ' ' + log.nowTime());
        con.query(sql, (err, results) => {
            if (err) {
                console.log('Error create Index: ' + json.indexes[arr_index].name + ' ' + err + ' ' + log.nowTime());
                reject('{8B6E8492-3D25-41EE-96C4-C92A4C047E8E}');
            } else {
                console.log('End create Index: ' + json.indexes[arr_index].name + ' ' + log.nowTime());
                resolve();
            }
        });
    });
}

let createFK = (con, json, arr_index) => {
    return new Promise(function (resolve, reject) {
        console.log('Start create FK: ' + json.fkeys[arr_index].name + ' ' + log.nowTime());
        sql_create_table.getScriptFK(json, arr_index).then(function (sql) {
            con.query(sql, (err, results) => {
                if (err) {
                    console.log('Error create FK: ' + json.fkeys[arr_index].name + log.nowTime());
                    reject('{8B10F688-DC86-47E4-A4BA-67C17792963A}');
                } else {
                    console.log('End create FK: ' + json.fkeys[arr_index].name + ' ' + log.nowTime());
                    resolve();
                }
            });
        });
    });
};

module.exports = {createTable, createFK};