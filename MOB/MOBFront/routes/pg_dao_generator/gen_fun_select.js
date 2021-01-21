const UUID = require('uuid/v4');
const {funParams, funGetSetWhere, funSqlColumns} = require("../pg_dao_generator/gen_fun_common");

// Значения пподставляются из набора параметров (params) в нужном порядке
let genFunSelect = (json, f_json) => {
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
    var sql_select = funSqlSelect(json, f_json);
    var code = code.replace('###PH_SQL_INSERT###', sql_select);
    return code;
};

function funSqlSelect(json, f_json) {
    // var setBlock = funGetSetWhere(f_json,  f_json.SET, ",");
    var listColumns = funSqlColumns(json, f_json);
    var whereBlock = funGetSetWhere(f_json, f_json.where, " ");
    var listColOrder = funSqlColumnsOrder(f_json);
    var limitValue = funSelectLimit(f_json);

    var sql = '\'SELECT ' + listColumns + ' FROM "' + json.name.toLowerCase() + '" WHERE ' + whereBlock + ' ' + '####ORDER_BY#### ' + '####LIMIT#### '+ '\'';
    if (listColOrder===''){
        sql = sql.replace('####ORDER_BY####', '')
    }else{
        sql = sql.replace('####ORDER_BY####', 'ORDER BY ' + listColOrder)
    }

    if (limitValue===''){
        sql = sql.replace('####LIMIT####', '')
    }else{
        sql = sql.replace('####LIMIT####', limitValue)
    }

    return sql;
}

function funSqlColumnsOrder(f_json) {
    var columns = '';
    if (f_json.order_by != undefined) {
        if (f_json.order_by.columns != undefined) {
            for (key in f_json.order_by.columns) {
                if (key != 0) {
                    columns += ', ';
                }
                columns += '"' + f_json.order_by.columns[key].name.toLowerCase() + '"';

            }
        }
    }
    return columns;
}

function funSelectLimit(f_json){
    var res = '';
    if(f_json.limit != undefined) {
        res = 'LIMIT ' + f_json.limit
    }
    return res;
}

module.exports = {genFunSelect};