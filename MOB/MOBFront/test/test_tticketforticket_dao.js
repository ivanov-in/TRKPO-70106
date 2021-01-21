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
const tticketforticket_dao = require('../routes/database_dao/tticketforticket_dao');
const UUID = require('uuid/v4')

describe('test group', () => {
    it('should do something', () => {
        tticketforticket_dao.select(pool, 'D111977E-0E36-44DD-82B7-8C2228106E3E').then(
            function (res) {
                var r = res.rows.length;
            }
        ).catch(function (err) {
            var e = err;
        });

        //assert.fail();

    })
});