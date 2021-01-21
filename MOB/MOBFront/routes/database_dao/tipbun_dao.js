const UUID = require('uuid/v4');

let selectCount = (pool, p_ip) => {
	return new Promise(function (resolve, reject) {
		var values = [p_ip];
		var sql = 'SELECT * FROM "tipbun" WHERE "ip"=$1';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let insert = (pool, p_ip) => {
	return new Promise(function (resolve, reject) {
				var p_dtc = new Date().toLocaleString();
		var values = [p_ip, p_dtc];
		var sql = 'INSERT INTO "tipbun" ("ip", "dtc") VALUES($1, $2) RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
 module.exports = {selectCount, insert}