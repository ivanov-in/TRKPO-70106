const {funGetSetWhere, funParams} = require("../pg_dao_generator/gen_fun_common");

let genFunDeleteBy = (json, f_json) => {
    var code = 'let ' + f_json.name + ' = (###PH_PARAMS###) => {' + '\n' +
        '\t' + 'return new Promise(function (resolve, reject) {' + '\n' +
        '\t\t' + 'var values = [###PH_VALUES###];' + '\n' +
        '\t\t' + 'var sql = ###PH_SQL_DELETE###;' + '\n' +
        '\t\t' + 'pool.query(sql, values)' + '\n' +
        '\t\t\t' + '.then(res => {' + '\n' +
        '\t\t\t\t' + 'resolve(res);' + '\n' +
        '\t\t\t' + '})' + '\n' +
        '\t\t' + '.catch(err => {reject(err)})' + '\n' +
        '\t' + '});' + '\n' +
        '};';



    var f_params = 'pool, ' + funParams(f_json);
    var code = code.replace('###PH_PARAMS###', f_params);
    var code = code.replace('###PH_VALUES###', funParams(f_json));
    var sql_delete = funSqlDelete(json, f_json);
    var code = code.replace('###PH_SQL_DELETE###', sql_delete);
    return code;
};

function funSqlDelete(json, f_json) {
    var whereBlock = funGetSetWhere(f_json, f_json.where, " ")
    var sql = '\'DELETE FROM "' + json.name.toLowerCase() + '" WHERE ' + whereBlock + ' RETURNING *\'';
    return sql;
}
module.exports = {genFunDeleteBy};