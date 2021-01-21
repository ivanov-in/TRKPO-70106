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
const daotkeys = require('../routes/database_dao/tusers_dao.js');
const UUID = require('uuid/v4')

describe('test group', () => {
    it('should do something 1: selectCount', () => {
        var res = daotkeys.selectCount(pool, "e375c3ec-8ad9-483a-97b7-4cc3ec174f07");
        // assert.fail();

    })

    it('should do something 2: updateChangePassByLogin', () => {
        var res = daotkeys.updateChangePassByLogin(pool, "111", "qwe");

    })

    it('should do something 3: updateChangePassByKey', () => {
        var res = daotkeys.updateChangePassByKey(pool, "e375c3ec-8ad9-483a-97b7-4cc3ec174f07", "123456");

    })

    it('should do something 4: deleteByKey', () => {
        var res = daotkeys.deleteByKey(pool, "e375c3ec-8ad9-483a-97b7-4cc3ec174f07");
    })

    it('should do something 5: insertUser', () => {
        var res = daotkeys.insertUser(pool, 'user_1', '111111');
    })

    it('should do something 6: selectList', () => {
        var res = daotkeys.selectList(pool, new Date()).then( function (response ) {
            // if (response.rows.length == 1) {
            //
            // }
            let s='';
        }).catch(function (err) {
            sendError(rsp, "{F13A194E-84B8-4E2C-B77F-6C55C0D18991}");
        });
    })
});