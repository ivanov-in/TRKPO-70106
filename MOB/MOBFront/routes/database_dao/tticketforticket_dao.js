const UUID = require('uuid/v4');

let insert = (pool, p_tiketfortiket, p_ip, p_uuid_keys) => {
	return new Promise(function (resolve, reject) {
				var p_dtc = new Date().toLocaleString();
		var values = [p_tiketfortiket, p_dtc, p_ip, p_uuid_keys];
		var sql = 'INSERT INTO "tticketforticket" ("tiketfortiket", "dtc", "ip", "uuid_keys") VALUES($1, $2, $3, $4) RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let selectByUUID = (pool, p_tiketfortiket) => {
	return new Promise(function (resolve, reject) {
		var values = [p_tiketfortiket];
		var sql = 'SELECT "tiketfortiket", "dtc", "ip", "uuid_keys" FROM "tticketforticket" WHERE TIKETFORTIKET = $1   ';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let deleteByKey = (pool, p_tiketfortiket) => {
	return new Promise(function (resolve, reject) {
				var values = [p_tiketfortiket];
		var sql = 'DELETE FROM "tticketforticket" WHERE TIKETFORTIKET = $1 RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let deleteByDTC = (pool, p_dtc) => {
	return new Promise(function (resolve, reject) {
		var values = [p_dtc];
		var sql = 'DELETE FROM "tticketforticket" WHERE DTC < $1 RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
 module.exports = {insert, selectByUUID, deleteByKey, deleteByDTC}