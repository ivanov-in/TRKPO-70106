const UUID = require('uuid/v4');
const {funGenValues} = require("../pg_dao_generator/gen_fun_common");

let genFunUpdateByKey = (json, f_json) => {
    var code = 'let ' + f_json.name + ' = (###PH_PARAMS###) => {' + '\n' +
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

    var genValues = funGenValues(f_json);
    var ph_params = funParams(f_json, json, f_json.generate_key, f_json.generate_dtc);
    var ph_values = insParams(f_json, ph_params);
    var code = code.replace('###PH_GEN_VALUES###', genValues);
    var code = code.replace('###PH_PARAMS###', 'pool, ' + ph_params);
    var code = code.replace('###PH_VALUES###', ph_values);
    var sql_insert = funSqlUpdateByKey(f_json, json);
    var code = code.replace('###PH_SQL_INSERT###', sql_insert);
    return code;
};

function funParams(f_json, json, generate_key, generate_dtc) {
    var params = '';
    var params_temp = '';
    var key_name = json.pkey.columns[0].name;
    if (f_json.key != undefined) {
        if (json.pkey.name != f_json.key)
            key_name = '';
        else {
            // params += ', ';
            params += 'p_' + key_name.toLowerCase();
        }
    }

    if (f_json.columns != undefined) {
        for (key in f_json.columns) {
            // в даной функции UpdateByKey generate_key = undefined всегда, т.к. ключ используется в блоке where, т.е. не меняется.
            // но здесь оставляю проверку на generate_key, чтобы в будущем сделать эту функцию общей
            if ((f_json.columns[key].toLowerCase() != key_name.toLowerCase() && generate_key) || !generate_key) {
                if ((f_json.columns[key].toLowerCase() != "dtc" && generate_dtc) || !generate_dtc) {
                    params += ', ';
                    params += 'p_' + f_json.columns[key].toLowerCase();
                }
            }
        }
    } else {
        for (key in json.columns) {
            // в даной функции UpdateByKey generate_key = undefined всегда, т.к. ключ используется в блоке where, т.е. не меняется.
            // но здесь оставляю проверку на generate_key, чтобы в будущем сделать эту функцию общей
            if ((json.columns[key].name.toLowerCase() != key_name.toLowerCase() && generate_key) || !generate_key) {
                if ((json.columns[key].name.toLowerCase() != "dtc" && generate_dtc) || !generate_dtc) {
                    params += ', ';
                    params += 'p_' + json.columns[key].name.toLowerCase();
                }
            }
        }
    }


    return params;
}

function insParams(f_json, first_param) {
    var params = first_param;
    var index = 0;

    if (f_json.generate_key) {
        if (params != '')
            params += ', ';
        params += 'p_key';
        if (f_json.generate_dtc) {
            params += ', p_dtc'
        }
    } else if (f_json.generate_dtc) {
        if (params != '')
            params += ', ';
        params += 'p_dtc'
    }
    return params;
}

var index = 1;

function funSqlUpdateByKey(f_json, json) {
    var whereBlock = funSqlWhereBlock(f_json, json)
    var setBlock = funSqlSetBlockColumn(f_json, json, f_json.generate_key, f_json.generate_dtc);
    var sql = '\'UPDATE "' + json.name.toLowerCase() + '" SET ' + setBlock + ' WHERE ' + whereBlock + ' RETURNING *\'';
    return sql;
}

function funSqlWhereBlock(f_json, json) {
    var whereBlock = '';
    var key_name = json.pkey.columns[0].name;
    if (f_json.key != undefined) {
        if (json.pkey.name != f_json.key)
            key_name = '';
        else {
            whereBlock = '"' + key_name.toLowerCase() + '" = $' + index;
            index++;
        }
    }
    index = 1;
    return whereBlock;
}

function funSqlSetBlockColumn(f_json, json, generate_key, generate_dtc) {
    var columns = '';
    var columns_temp = '';
    var index_temp = 0;

    var key_name = json.pkey.columns[0].name;
    if (f_json.key != undefined) {
        if (json.pkey.name != f_json.key)
            key_name = '';
    }

    if (f_json.columns != undefined) {
        for (key in f_json.columns) {
            // в даной функции UpdateByKey generate_key = undefined всегда, т.к. ключ используется в блоке where, т.е. не меняется.
            // но здесь оставляю проверку на generate_key, чтобы в будущем сделать эту функцию общей
            if ((f_json.columns[key].toLowerCase() != key_name.toLowerCase() && generate_key) || !generate_key) {
                if ((f_json.columns[key].toLowerCase() != "dtc" && generate_dtc) || !generate_dtc) {
                    if (key != 0) {
                        columns += ', ';
                    }
                    columns += '"' + f_json.columns[key].toLowerCase() + '" = $' + (parseInt(index)+1);
                    index++;
                } else {
                    if (index_temp != 0) {
                        columns_temp += ', ';
                    }
                    columns_temp += '"dtc" = $temp_' + index_temp;
                    index_temp++;
                }
            } else {
                if (generate_key) {
                    if (index_temp != 0) {
                        columns_temp += ', ';
                    }
                    columns_temp += '"' + key_name + '" = $temp_' + index_temp;
                    index_temp++;
                }
            }
        }
    } else {
        for (key in json.columns) {
            // в даной функции UpdateByKey generate_key = undefined всегда, т.к. ключ используется в блоке where, т.е. не меняется.
            // но здесь оставляю проверку на generate_key, чтобы в будущем сделать эту функцию общей
            if ((json.columns[key].name.toLowerCase() != key_name.toLowerCase() && generate_key) || !generate_key) {
                if ((f_json.columns[key].toLowerCase() != "dtc" && generate_dtc) || !generate_dtc) {
                    if (key != 0) {
                        columns += ', ';
                    }
                    columns += '"' + json.columns[key].name.toLowerCase() + '" = $' + index;
                    index++;
                } else {
                    if (index_temp != 0) {
                        columns_temp += ', ';
                    }
                    columns_temp += '"dtc" = $temp_' + index_temp;
                    index_temp++;
                }
            } else {
                if (generate_key) {
                    if (index_temp != 0) {
                        columns_temp += ', ';
                    }
                    columns_temp += '"' + key_name + '" = $temp_' + index_temp;
                    index_temp++;
                }
            }
        }
    }
    if (columns_temp != '') {
        // предположим, что index_temp это однозначное чесло
        var columns_temp_2 = columns_temp.split(", ");
        for (var i in columns_temp_2) {
            var temp = columns_temp_2[i].replace(columns_temp_2[i].substring(columns_temp_2[i].indexOf("temp_"), columns_temp_2[i].indexOf("temp_") + 6), parseInt(columns_temp_2[i].substring(columns_temp_2[i].indexOf("temp_") + 5, columns_temp_2[i].indexOf("temp_") + 6)) + index);
            columns += ', ' + temp;
            index++;
        }
    } else {
        if (generate_key) {
            columns += ', "' + key_name + '" = $' + index;
            index++;
        }
        if (generate_dtc) {
            columns += ', "dtc" = $' + index;
            index++;
        }
    }


    return columns;
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

module.exports = {genFunUpdateByKey};