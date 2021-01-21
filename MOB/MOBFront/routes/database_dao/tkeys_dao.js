const UUID = require('uuid/v4');

let insert = (pool, p_front_pub_key, p_front_pri_key, p_mob_pub_key) => {
	return new Promise(function (resolve, reject) {
		var p_key =  UUID();
		var p_dtc = new Date().toLocaleString();
		var values = [p_key, p_dtc, p_front_pub_key, p_front_pri_key, p_mob_pub_key];
		var sql = 'INSERT INTO "tkeys" ("uuid", "dtc", "front_pub_key", "front_pri_key", "mob_pub_key") VALUES($1, $2, $3, $4, $5) RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let selectByKey = (pool, p_uuid) => {
	return new Promise(function (resolve, reject) {
		var values = [p_uuid];
		var sql = 'SELECT * FROM "tkeys" WHERE UUID = $1';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
 module.exports = {insert, selectByKey}