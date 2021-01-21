const UUID = require('uuid/v4');

let selectCount = (pool, p_ip, p_dt) => {
	return new Promise(function (resolve, reject) {
		var values = [p_ip, p_dt];
		var sql = 'SELECT * FROM "tiperrors" WHERE "ip"=$1 AND "dtc">$2';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let insertError = (pool, p_ip, p_error) => {
	return new Promise(function (resolve, reject) {
		var p_key =  UUID();
		var p_dtc = new Date().toLocaleString();
		var values = [p_key, p_dtc, p_ip, p_error];
		var sql = 'INSERT INTO "tiperrors" ("uuid", "dtc", "ip", "error") VALUES($1, $2, $3, $4) RETURNING *';
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
		var sql = 'DELETE FROM "tiperrors" WHERE DTC < $1 RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
 module.exports = {selectCount, insertError, deleteByDTC}