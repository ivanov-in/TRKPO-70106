const poolOra = require("./db_pool");
let oracledb = require('oracledb');
const {v4: UUID} = require('uuid');


async function select_insp(connection) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS_ADMIN.man_tasks_page.select_insp(:cur);
       END;`,
            {
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonInsp = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonInsp.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonInsp;
}

async function select_list_tasks(connection, date) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS_ADMIN.man_tasks_page.select_list_tasks(:p_date, :cur);
       END;`,
            {
                p_date: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: date},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;

}


let add_task = (adr, city, street, house, nd, purpose, prim, ttime, id_ins, puser, lat, lan, s_zulu, b_zulu, status) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection(poolOra.poolOra.hrPool).then(function (connection) {
            connection.execute(`BEGIN MIS.mis_web_w.add_task(
                     :p_adr,
                     :p_city,
                     :p_street,
                     :p_house,
                     :p_nd,
                     :p_purpose,
                     :p_prim,
                     :p_ttime,
                     :p_id_inspector,
                     :p_puser,
                     :p_lat,
                     :p_lan,
                     :p_schema_zulu,
                     :p_border_zulu,
                     :po_id_task,
                     :status,
                     :errmsg); END;`,
                {
                    p_adr: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: adr},
                    p_city: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: city.trim()},
                    p_street: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: street.trim()},
                    p_house: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: house.trim()},
                    p_nd: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nd.trim()},
                    p_purpose: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: purpose},
                    p_prim: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: prim},
                    p_ttime: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: ttime},
                    p_id_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_ins},
                    p_puser: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: puser},
                    p_lat: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: lat},
                    p_lan: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: lan},
                    p_schema_zulu: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: s_zulu},
                    p_border_zulu: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: b_zulu},
                    po_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                    status: {type: oracledb.NUMBER, dir: oracledb.BIND_INOUT, val: status},
                    errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
                },
                {
                    resultSet: true
                }).then(function (result) {
                connection.commit();
                resolve(result);
            })
        }).catch(function (err) {
            reject(err);
        });
    });
};

async function get_task(connection, id_task) {
    let jsonTask = [];

    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_task(:pid_task, :cur);
       END;`,
            {
                pid_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_task},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }

    return jsonTask;

}

async function get_list_obj(connection, id_task) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_list_obj(:pid_task, :cur);
       END;`,
            {
                pid_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_task},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;

}

async function get_objects(connection, text) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_objects(:psearch_string, :cur);
       END;`,
            {
                psearch_string: {type: oracledb.STRING, dir: oracledb.BIND_IN, val: text},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;

}

module.exports = {
    select_insp,
    select_list_tasks,
    add_task,
    get_objects,
    get_task,
    get_list_obj
};
