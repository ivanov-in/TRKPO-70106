let oracledb = require('oracledb');


async function get_events(connection) {
    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_ADMIN.adm_events_page.get_events(:cur);
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
    let jsonEvents = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonEvents.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonEvents;
}
async function select_list_events(connection, date) {
    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_ADMIN.adm_events_page.select_list_events(:p_date, :cur);
       END;`,
            {
                p_date: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: date},
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
    let jsonEvents = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonEvents.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonEvents;
}
module.exports = {
    get_events,
    select_list_events
};
