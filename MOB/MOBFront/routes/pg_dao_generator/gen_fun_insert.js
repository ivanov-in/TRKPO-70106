const UUID = require('uuid/v4');
const {funSqlColumns} = require("../pg_dao_generator/gen_fun_common");

let genFunInsert = (json, f_json) => {
    var code ='let ' + f_json.name + ' = (###PH_PARAMS###) => {' + '\n' +
        '\t' + 'return new Promise(function (resolve, reject) {' + '\n' +
        '\t\t' + '###PH_GEN_VALUES###' +
        '\t\t' + 'var values = [###PH_VALUES###];' + '\n' +
        '\t\t' + 'var sql = ###PH_SQL_INSERT###;' + '\n' +
        '\t\t' + 'pool.query(sql, values)' + '\n' +
        '\t\t\t' + '.then(res => {' + '\n' +
        '\t\t\t\t' + 'resolve(res);' + '\n' +
        '\t\t\t' + '})' + '\n' +
        '\t\t' + '.catch(err => {reject(err)})' + '\n' +
        '\t' + '});' + '\n' +
        '};';

    var genValues = funGenValues(json, f_json.generate_key, json.pkey.columns[0].name, f_json.generate_dtc);
    var ph_params = funParams(json, f_json.generate_key, json.pkey.columns[0].name, f_json.generate_dtc);
    var ph_values = insParams(json, f_json.generate_key, json.pkey.columns[0].name, f_json.generate_dtc);
    var code = code.replace('###PH_GEN_VALUES###', genValues);
    var code = code.replace('###PH_PARAMS###', ph_params);
    var code = code.replace('###PH_VALUES###', ph_values);
    var sql_insert = funSqlInsert(json, f_json);
    var code = code.replace('###PH_SQL_INSERT###', sql_insert);
    return code;
};


function funGenValues(json, generate_key, key_name, generate_dtc) {
    var params = '';
    if (generate_key) {
        params += 'var p_key =  UUID();' + '\n';
    }
    if (generate_dtc) {
        params += '\t\t' + 'var p_dtc = new Date().toLocaleString();' + '\n';
    }
    return params;
}

function funParams(json, generate_key, key_name, generate_dtc) {
    var params = 'pool';
    for (key in json.columns) {
        if ((json.columns[key].name.toLowerCase() != key_name.toLowerCase() && generate_key) || !generate_key) {
            if ((json.columns[key].name.toLowerCase() != "dtc" && generate_dtc) || !generate_dtc) {
                params += ', ';
                params += 'p_' + json.columns[key].name.toLowerCase();
            }
        }
    }
    return params;
}

function insParams(json, generate_key, key_name, generate_dtc) {
    var params = '';
    var index = 0;
    for (key in json.columns) {
        if ((json.columns[key].name.toLowerCase() != key_name.toLowerCase() && generate_key) || !generate_key) {
            if ((json.columns[key].name.toLowerCase() != "dtc" && generate_dtc) || !generate_dtc) {
                if (index != 0) {
                    params += ', ';
                }
                params += 'p_' + json.columns[key].name.toLowerCase();
                index++;
            } else {
                if (index != 0) {
                    params += ', ';
                }
                params += 'p_dtc'
                index++;
            }
        } else {
            if (index != 0) {
                params += ', ';
            }
            params += 'p_key'
            index++;
        }
    }
    return params;
}

function funSqlInsert(json, f_json) {
    var columns = funSqlColumns(json, f_json);
    var phs = funSqlInsertColumnsPh(json);
    var sql = '\'INSERT INTO "' + json.name.toLowerCase() + '" (' + columns + ') VALUES(' + phs + ') RETURNING *\'';
    return sql;
}

function funSqlInsertColumnsPh(json) {
    var columns = '';
    for (key in json.columns) {
        if (key != 0) {
            columns += ', ';
        }
        var ind = parseInt(key) + parseInt(1);
        columns += '$' + ind;
    }
    return columns;
}

module.exports = {genFunInsert};