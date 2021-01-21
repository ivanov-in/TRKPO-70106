const tiperrors_dao = require('../database_dao/tiperrors_dao');
const pool = require('../db_pool');

function delete_old() {
    var p_dtc = new Date();
    p_dtc.setMinutes(p_dtc.getMinutes() - 60*24);
    tiperrors_dao.deleteByDTC(pool.con, p_dtc);
}

module.exports = {delete_old};