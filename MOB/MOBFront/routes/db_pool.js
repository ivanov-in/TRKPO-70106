const Pool = require('pg-pool')
const pool = new Pool({
    user: 'postgres',
    host: '192.168.1.9',
    database: 'mobia_app',
    password: 'ZAQ12ws',
    port: 5432,
    ssl: false,
    max: 8, // set pool max size to 20
    min: 4, // set min pool size to 4
    idleTimeoutMillis: 100000, // close idle clients after 1 second
    connectionTimeoutMillis: 100000
});

module.exports = {
    con: pool
}