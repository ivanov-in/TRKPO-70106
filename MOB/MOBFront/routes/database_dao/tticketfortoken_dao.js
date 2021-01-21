const UUID = require('uuid/v4');

let insert = (pool, p_ticketfortoken, p_ip, p_imei, p_uuid_keys) => {
	return new Promise(function (resolve, reject) {
				var p_dtc = new Date().toLocaleString();
		var values = [p_ticketfortoken, p_dtc, p_ip, p_imei, p_uuid_keys];
		var sql = 'INSERT INTO "tticketfortoken" ("ticketfortoken", "dtc", "ip", "imei", "uuid_keys") VALUES($1, $2, $3, $4, $5) RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let selectByUUID = (pool, p_ticketfortoken, p_imei, p_ip) => {
	return new Promise(function (resolve, reject) {
		var values = [p_ticketfortoken, p_imei, p_ip];
		var sql = 'SELECT "ticketfortoken", "dtc", "ip", "imei", "uuid_keys" FROM "tticketfortoken" WHERE TICKETFORTOKEN = $1 AND IMEI = $2 AND IP = $3   ';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
 module.exports = {insert, selectByUUID}