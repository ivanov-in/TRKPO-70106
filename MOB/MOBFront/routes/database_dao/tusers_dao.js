const UUID = require('uuid/v4');

let selectCount = (pool, p_login, p_pass_hash) => {
	return new Promise(function (resolve, reject) {
		var values = [p_login, p_pass_hash];
		var sql = 'SELECT * FROM "tusers" WHERE "pass_hash"=$2 AND "login"=$1';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let updateChangePassByLogin = (pool, p_pass_hash, p_login) => {
	return new Promise(function (resolve, reject) {
		var values = [p_pass_hash, p_login];
		var sql = 'UPDATE "tusers" SET "pass_hash" = $1 WHERE "login"=$2 RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let updateChangePassByKey = (pool, p_uuid, p_pass_hash) => {
	return new Promise(function (resolve, reject) {
				var p_dtc = new Date().toLocaleString();
		var values = [p_uuid, p_pass_hash, p_dtc];
		var sql = 'UPDATE "tusers" SET "pass_hash" = $2, "dtc" = $2 WHERE "uuid" = $3 RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let deleteByKey = (pool, p_uuid) => {
	return new Promise(function (resolve, reject) {
				var values = [p_uuid];
		var sql = 'DELETE FROM "tusers" WHERE UUID = $1 RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let insertUser = (pool, p_login, p_pass_hash) => {
	return new Promise(function (resolve, reject) {
		var p_key =  UUID();
		var p_dtc = new Date().toLocaleString();
		var values = [p_key, p_dtc, p_login, p_pass_hash];
		var sql = 'INSERT INTO "tusers" ("uuid", "dtc", "login", "pass_hash") VALUES($1, $2, $3, $4) RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let selectList = (pool, p_dtc) => {
	return new Promise(function (resolve, reject) {
		var values = [p_dtc];
		var sql = 'SELECT "uuid", "dtc", "login", "pass_hash" FROM "tusers" WHERE "dtc"<$1 ORDER BY "dtc"  ';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
 module.exports = {selectCount, updateChangePassByLogin, updateChangePassByKey, deleteByKey, insertUser, selectList}