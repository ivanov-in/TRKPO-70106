const {Client} = require('pg')
const pgMetadata = require('pg-metadata')
const db_ini = require('./database_ini.js');
const log = require('../log/util');
const fs = require('fs');//../node/
const createTable_exe = require('../pg_sql_executer/create_table');
const sql_info_exe = require('../pg_sql_executer/info')
const {schemaExist} = require("../pg_sql_executer/info");


let run = async (con, schema) => {
    return new Promise(function (resolve, reject) {
        console.log('Find database schema:' + db_ini.def_schema + ' ' + log.nowTime());
        schemaExist(con, db_ini.db_name, schema).then(function (result) {
            if (!result) {
                console.log('Not foud database schema:' + db_ini.def_schema + ' ' + log.nowTime());
                createSchema(con, db_ini.def_schema).then(function () {
                    checkTables(con, schema).then(function () {
                        resolve();
                    });
                }).catch(function (err) {
                    reject(err);
                })
            } else {
                console.log('Foud database schema:' + db_ini.def_schema + ' ' + log.nowTime());
                checkTables(con, schema).then(function () {
                    resolve();
                });
            }
        }).catch(function (err) {
            console.log(err);
            reject(err);
        });
    });
};

function checkTables(con, schema) {
    return new Promise(function (resolve, reject) {
        const path = require('path');
        console.log('Getting table list:' + ' ' + log.nowTime());
        jsonTableList().then(function (data) {
            console.log('Received table list: ' + data + ' ' + log.nowTime());
            const count = data.length;
            var complit = 0;
            //todo async function parseDirectory(audit) {
            //   const directory = audit.ftpUrl;
            //   let filePaths = await getPaths(directory);
            //   let promises = [];
            //   for (let filePath of filePaths) {
            //     promises.push(parse(directory, filePath, audit));
            //   }
            //   await Promise.all(promises);
            // }
            data.forEach(file =>{
                var filePath = path.join('./routes/database_dao', file);
                var json = JSON.parse(fs.readFileSync(filePath, 'utf8'));

                if (json.otype !== 'table') {
                    complit++;
                    if (complit === count) {
                        checkForenKeys(con, schema).then(function () {
                            console.log('End check FK:' + ' ' + log.nowTime());
                            resolve();
                        }).catch(function (err) {
                            reject(err + ',{0472AFCE-4614-4068-8DB7-23D8D94503DD}');
                        })
                    }
                } else {
                    sql_info_exe.tableExist(con, db_ini.db_name, schema, json.name.replace("\"", "")).then(function (exist) {
                        if (!exist) {
                            createTable_exe.createTable(con, json).then(function () {
                                complit++;
                                if (complit === count) {
                                    checkForenKeys(con, schema).then(function () {
                                        console.log('End check FK:' + ' ' + log.nowTime());
                                        resolve();
                                    }).catch(function (err) {
                                        reject(err + ',{E6373E43-08A6-46EA-81EB-767F5D54F99C}');
                                    })
                                }
                            }).catch(function (err) {
                                reject(err + ',' + '{B066A787-E1B0-4F6B-8744-E39538FA5C62}');
                            })
                        } else {
                            checkTableStructure(filePath, function () {
                                complit++;
                                if (complit === count) {
                                    checkForenKeys(con, schema).then(function () {
                                        console.log('End check FK:' + ' ' + log.nowTime());
                                        resolve();
                                    }).catch(function (err) {
                                        reject(err + ',{9CA8CF93-DE59-4206-A87C-E6C84A397D8B}');
                                    })
                                }
                            });
                        }
                    }).catch(function (err) {
                        reject(err + ',{D2A8CF19-E70C-4F2B-A154-E02391AA9C1B}');
                    });

                }
            });
        }).catch(function (err) {
            console.log(err);
            reject(err);
        });
    })
}

function checkForenKeys(con, schema) {
    console.log('Start check FK:' + ' ' + log.nowTime());
    return new Promise(function (resolve, reject) {
        var path = require('path');
        console.log('Getting table list:' + ' ' + log.nowTime());
        jsonTableList().then(function (data) {
            console.log('Received table list: ' + data + ' ' + log.nowTime());
            const count = data.length;
            var complit = 0;
            data.forEach(file => {
                var filePath = path.join('./routes/database_dao', file);
                var json = JSON.parse(fs.readFileSync(filePath, 'utf8'));
                if (json.otype !== 'table') {
                    complit++;
                    if (complit === count) {
                        resolve();
                    }
                } else {
                    if (json.fkeys === undefined) {
                        complit++;
                        if (complit === count) {
                            resolve();
                        }
                    } else if (json.fkeys.length == 0) {
                        complit++;
                        if (complit === count) {
                            resolve();
                        }
                    } else {
                        var complit_key = 0;
                        for (key in json.fkeys) {
                            sql_info_exe.fkey_exist(con, db_ini.db_name, schema, json.fkeys[key]).then(function (result) {
                                if (!result) {
                                    createTable_exe.createFK(con, json, key).then(function () {
                                            complit_key++;
                                            if (complit_key == json.fkeys.length) {
                                                complit++;
                                                if (complit === count) {
                                                    resolve();
                                                }
                                            }
                                        }
                                    ).catch(function (err) {
                                        reject(err + ',{2FF23B8E-253D-4AD9-B465-BAA67A23EF78}');
                                    });
                                }else{
                                    complit_key++;
                                    if (complit_key == json.fkeys.length) {
                                        complit++;
                                        if (complit === count) {
                                            resolve();
                                        }
                                    }
                                }
                            }).catch(function (err) {
                                reject(err + ',{30954CAC-ED90-4D3A-BEAE-9F0CBB723316}');
                            });
                        }
                    }
                }
            })
            ;
        }).catch(function (err) {
            console.log(err);
            reject(err);
        })
    })
        ;
}

function createSchema(con, name) {
    return new Promise(function (resolve, reject) {
        resolve();
    });
}

let jsonTableList = () => {
    return new Promise(function (resolve, reject) {
        const path = require('path');
        const dir = './routes/database_dao/';
        var files = [];
        fs.readdir(dir, (err, _files) => {
            if (err) {
                reject(err);
            }
            for (index in _files) {
                if (path.extname(_files[index]).toLowerCase() === '.json') {
                    files.push(_files[index]);
                }
            }
            resolve(files);
        });
    })
}


function checkTableStructure(name, callback) {
    console.log('Start check table structure: ' + name + ' ' + log.nowTime());
    console.log('End check table structure: ' + name + ' ' + log.nowTime());
    callback();
}


module.exports = {run, jsonTableList};