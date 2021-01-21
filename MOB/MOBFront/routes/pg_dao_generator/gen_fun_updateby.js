const UUID = require('uuid/v4');
const {funGetSetWhere, funParams} = require("../pg_dao_generator/gen_fun_common");

// Значения пподставляются из набора параметров (params) в нужном порядке
let genFunUpdateBy = (json, f_json) => {
    var code = 'let ' + f_json.name + ' = (###PH_PARAMS###) => {' + '\n' +
        '\t' + 'return new Promise(function (resolve, reject) {' + '\n' +
        '\t\t' + 'var values = [###PH_VALUES###];' + '\n' +
        '\t\t' + 'var sql = ###PH_SQL_INSERT###;' + '\n' +
        '\t\t' + 'pool.query(sql, values)' + '\n' +
        '\t\t\t' + '.then(res => {' + '\n' +
        '\t\t\t\t' + 'resolve(res);' + '\n' +
        '\t\t\t' + '})' + '\n' +
        '\t\t' + '.catch(err => {reject(err)})' + '\n' +
        '\t' + '});' + '\n' +
        '};';

    var ph_params = funParams(f_json);
    // var ph_values = insParams(json, f_json);
    var code = code.replace('###PH_PARAMS###', 'pool, ' + ph_params);
    var code = code.replace('###PH_VALUES###', ph_params);
    var sql_insert = funSqlUpdateBy(json, f_json);
    var code = code.replace('###PH_SQL_INSERT###', sql_insert);
    return code;
};

function funSqlUpdateBy(json, f_json) {
    var setBlock = funGetSetWhere(f_json,  f_json.SET, ",");
    var whereBlock = funGetSetWhere(f_json, f_json.where, " ");

    var sql = '\'UPDATE "' + json.name.toLowerCase() + '" SET ' + setBlock + ' WHERE ' + whereBlock + ' RETURNING *\'';
    return sql;
}

module.exports = {genFunUpdateBy};