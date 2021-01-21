const UUID = require('uuid/v4');

let insert = (pool, p_token, p_tiketfortoken, p_imei, p_ip, p_uuid_keys, p_use_count, p_use_key) => {
	return new Promise(function (resolve, reject) {
				var p_dtc = new Date().toLocaleString();
		var values = [p_token, p_tiketfortoken, p_dtc, p_imei, p_ip, p_uuid_keys, p_use_count, p_use_key];
		var sql = 'INSERT INTO "ttoken" ("token", "tiketfortoken", "dtc", "imei", "ip", "uuid_keys", "use_count", "use_key") VALUES($1, $2, $3, $4, $5, $6, $7, $8) RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let deleteBy = (pool, p_imei, p_ip) => {
	return new Promise(function (resolve, reject) {
		var values = [p_imei, p_ip];
		var sql = 'DELETE FROM "ttoken" WHERE IMEI = $1 AND IP = $2 RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let selectBy = (pool, p_imei, p_ip) => {
	return new Promise(function (resolve, reject) {
		var values = [p_imei, p_ip];
		var sql = 'SELECT * FROM "ttoken" WHERE IMEI = $1 AND IP = $2';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let updateByKey = (pool, p_token, p_use_key, p_use_count) => {
	return new Promise(function (resolve, reject) {
				var values = [p_token, p_use_key, p_use_count];
		var sql = 'UPDATE "ttoken" SET "use_key" = $2, "use_count" = $3 WHERE "token" = $1 RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
 module.exports = {insert, deleteBy, selectBy, updateByKey}