const UUID = require('uuid/v4');
const {funGetSetWhere} = require("../pg_dao_generator/gen_fun_common");

let genFunSelectRow = (json, f_json) => {
    var code = 'let ' + f_json.name + ' = (###PH_PARAMS###) => {' + '\n' +
        '\t' + 'return new Promise(function (resolve, reject) {' + '\n' +
        '\t\t' + 'var values = [###PH_PARAM###];' + '\n' +
        '\t\t' + 'var sql = ###PH_SQL_SELECT_ROW###;' + '\n' +
        '\t\t' + 'pool.query(sql, values)' + '\n' +
        '\t\t\t' + '.then(res => {' + '\n' +
        '\t\t\t\t' + 'resolve(res);' + '\n' +
        '\t\t\t' + '})' + '\n' +
        '\t\t' + '.catch(err => {reject(err)})' + '\n' +
        '\t' + '});' + '\n' +
        '};';

    var ph_params = funParams(f_json);
    var code = code.replace('###PH_PARAMS###', 'pool, ' + ph_params);
    var code = code.replace('###PH_PARAM###', ph_params);
    var sql_select_row = funSqlSelectRow(json, f_json);
    var code = code.replace('###PH_SQL_SELECT_ROW###', sql_select_row);
    return code;
};

function funParams(json) {
    var params = '';
    for (key in json.params) {
        if (key != 0)
            params += ', ';
        params += 'p_' + json.params[key].name.toLowerCase();
    }
    return params;
}

function funSqlSelectRow(json, f_json) {
    var where_body = funGetSetWhere(f_json, f_json.where, " ");
    var sql = '\'SELECT * FROM "' + json.name.toLowerCase() + '" WHERE ' + where_body + '\'';
    return sql;
}

// function funSqlWhereColumnsWithValue(f_json) {
//     var columns = '';
//     for (key in f_json.params) {
//         if (key != 0) {
//             columns += ', ';
//         }
//         var index = parseInt(key) + 1;
//         columns += '"' + f_json.params[key].name.toLowerCase() + '"' + ' = $' + index;
//     }
//     return columns;
// }

module.exports = {genFunSelectRow};