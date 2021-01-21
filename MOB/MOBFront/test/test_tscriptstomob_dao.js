const Pool = require('pg-pool')
const pool = new Pool({
    user: 'postgres',
    host: 'localhost',
    database: 'mobia_app',
    password: 'kl0pik',
    port: 5432,
    ssl: false,
    max: 8, // set pool max size to 20
    min: 4, // set min pool size to 4
    idleTimeoutMillis: 100000, // close idle clients after 1 second
    connectionTimeoutMillis: 100000
});


const assert = require('assert');
const daotkeys = require('../routes/database_dao/tscriptstomob_dao.js');
const UUID = require('uuid/v4')

describe('test group', () => {
    it('should do something 1: insScriptToMob', () => {
        var res = daotkeys.insScriptToMob(pool, null,"user", 'SELECT * FROM T_USER', 'SELECT', "local.db", null, null);
        // var res = daotkeys.insScriptToMob(pool, null,"user", 'UPDATE T_USER SET PASS_HASH = \'pass_2\' WHERE LOGIN = \'login_1\'', 'UPDATE', "local.db", null, null);
        // var res = daotkeys.insScriptToMob(pool, null,"user", 'INSERT INTO T_USER VALUES(\'login_1\', \'pass_1\')', 'INSERT', "local.db", null, null);
        // var res = daotkeys.insScriptToMob(pool, null,"user", 'DELETE FROM T_USER WHERE LOGIN = \'login_1\'', 'DELETE', "local.db", null, null);
        // assert.fail();

    })

    it('should do something 2: getScriptToMobNew', () => {
        var res = daotkeys.getScriptToMobNew(pool, "user_1").then( function (response ) {
            let s='';
            // response.rows[0].uuid
        }).catch(function (err) {
            sendError(rsp, "{F13A194E-84B8-4E2C-B77F-6C55C0D18991}");
        });

    })

    it('should do something 2: updDtSendByKey', () => {
        var res = daotkeys.updDtSendByKey(pool, new Date().toLocaleString(), "a7104fa5-a741-4ffd-8d6e-a463d8a4938d").then( function (response ) {
            let s='';
            // response.rows[0].uuid
        }).catch(function (err) {
            sendError(rsp, "{B22044B1-B943-48C2-B3FE-3339BBDDAAAF}");
        });

    })
});
