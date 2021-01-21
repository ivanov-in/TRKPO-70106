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
const daotkeys = require('../routes/database_dao/tkeys_dao.js');
const UUID = require('uuid/v4')

describe('test group', () => {
    it('should do something', () => {
        daotkeys.insert(pool, "ggg", "hghj", "kxxk");
        //assert.fail();

    })
});