const UUID = require('uuid/v4');

let genFunDeleteByKey = (json, f_json) => {
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
    var key_name = json.pkey.columns[0].name;
    if(key_name == '')
        throw SQLException;
    var ph_params = 'pool, p_' + key_name.toLowerCase();
    var ph_values = 'p_' + key_name.toLowerCase();
    var code = code.replace('###PH_GEN_VALUES###', genValues);
    var code = code.replace('###PH_PARAMS###', ph_params);
    var code = code.replace('###PH_VALUES###', ph_values);
    var sql_delete = funSqlDelete(json);
    var code = code.replace('###PH_SQL_INSERT###', sql_delete);
    return code;
};


function funGenValues(f_json) {
    var params = '';
    if (f_json.generate_key) {
        params += 'var p_key =  UUID();' + '\n';
    }
    if (f_json.generate_dtc) {
        params += '\t\t' + 'var p_dtc = new Date().toLocaleString();' + '\n';
    }
    return params;
}


function funSqlDelete(json) {
    var whereBlock = funSqlWhereBlock(json)
    var sql = '\'DELETE FROM "' + json.name.toLowerCase() + '" WHERE ' + whereBlock + ' RETURNING *\'';
    return sql;
}

function funSqlWhereBlock(json) {
    var whereBlock = '';
    var key_name = json.pkey.columns[0].name;
    whereBlock = key_name + ' = $' + 1;
    index++;

    return whereBlock;
}

module.exports = {genFunDeleteByKey};