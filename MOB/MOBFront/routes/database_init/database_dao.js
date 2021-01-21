const database_obj = require('./database_obj.js');
const path = require('path');
const fs = require('fs');
const log = require('../log/util');
const {genFunInsert} = require("../pg_dao_generator/gen_fun_insert");
const {genFunSelectRow} = require("../pg_dao_generator/gen_fun_select_row");
const {genFunUpdateByKey} = require("../pg_dao_generator/gen_fun_updatebykey");
const {genFunUpdateBy} = require("../pg_dao_generator/gen_fun_updateby");
const {genFunDeleteByKey} = require("../pg_dao_generator/gen_fun_deletebykey");
const {genFunDeleteBy} = require("../pg_dao_generator/gen_fun_deleteby");
const {genFunSelect} = require("../pg_dao_generator/gen_fun_select");

let run = () => {
    return new Promise(function (resolve, reject) {
        database_obj.jsonTableList().then(async function (files) {
            for (key in files) {
                var file = files[key];
                var filePath = path.join('./routes/database_dao', file);
                var json = JSON.parse(fs.readFileSync(filePath, 'utf8'));
                await writeDaoFile(json);
            }
            resolve();
        });
    });
};

async function writeDaoFile(json) {
    if (json.otype !== undefined) {
        if (json.name !== undefined) {
            if (json.dao_config !== undefined) {
                if (json.dao_config.functions !== undefined) {
                    if (json.dao_config.functions.length != 0) {
                        if (json.otype.toLocaleString() === 'table') {
                            const dir = './routes/database_dao/';
                            var filePath = dir + json.name.toLocaleString() + '_dao.js';
                            filePath = filePath.toLowerCase();
                            //if (!fs.existsSync(filePath)) {
                            var code = genDaoCod(json);
                            fs.writeFileSync(filePath, code);
                            //}
                        }
                    }
                }
            }
        }
    }
}

let exportFun = '';

function genDaoCod(json) {
    try {
        var cod = 'const UUID = require(\'uuid/v4\');' + '\n';
        for (key in json.dao_config.functions) {
            var f_json = json.dao_config.functions[key];
            var code_f = genFun(json, f_json);
            cod += '\n' + code_f;
            if (exportFun == '') {
                exportFun += f_json.name;
            } else {
                exportFun += ', ' + f_json.name;
            }
        }
        return cod + '\n module.exports = {' + exportFun + '}';
    } catch (e) {
        System.print(e);
    } finally {
        cod = '';
        exportFun = ''
    }
}

function genFun(json, f_json) {
    if (f_json.type.toLowerCase() === 'insert') {
        return genFunInsert(json, f_json);
    } else if (f_json.type.toLowerCase() === "selectrow") {
        return genFunSelectRow(json, f_json);
    } else if (f_json.type.toLowerCase() === "updateby") {
        return genFunUpdateBy(json, f_json);
    } else if (f_json.type.toLowerCase() === "updatebykey") {
        return genFunUpdateByKey(json, f_json);
    } else if (f_json.type.toLowerCase() === "deletebykey") {
        return genFunDeleteByKey(json, f_json);
    } else if (f_json.type.toLowerCase() === "deleteby") {
        return genFunDeleteBy(json, f_json);
    } else if (f_json.type.toLowerCase() === "select") {
        return genFunSelect(json, f_json);
    }
    return 'error';
}


module.exports = {run};