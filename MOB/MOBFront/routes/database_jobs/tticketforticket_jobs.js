const ticket_for_ticket_dao = require('../database_dao/tticketforticket_dao');
const pool = require('../db_pool');

function delete_old() {
    var p_dtc = new Date();
    p_dtc.setMinutes(p_dtc.getMinutes() - 2);
    ticket_for_ticket_dao.deleteByDTC(pool.con, p_dtc);
}

module.exports = {delete_old};