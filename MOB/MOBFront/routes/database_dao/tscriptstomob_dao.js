const UUID = require('uuid/v4');

let selectCountNewScriptByKey = (pool, p_uuid) => {
	return new Promise(function (resolve, reject) {
		var values = [p_uuid];
		var sql = 'SELECT * FROM "tscriptstomob" WHERE "uuid"=$1 and "dt_send" is null and "dtd" is null';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let selectCountSendScriptByKey = (pool, p_uuid) => {
	return new Promise(function (resolve, reject) {
		var values = [p_uuid];
		var sql = 'SELECT * FROM "tscriptstomob" WHERE "uuid"=$1 and "dt_send" is not null and "dtd" is null';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let getScriptToMobNew = (pool, p_login) => {
	return new Promise(function (resolve, reject) {
		var values = [p_login];
		var sql = 'SELECT "uuid", "dtc", "login", "script", "type_script", "db_name" FROM "tscriptstomob" WHERE "login"=$1 and "dtd" is null and "dt_send" is null ORDER BY "dtc" LIMIT 1 ';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let getScriptToMobByKey = (pool, p_uuid) => {
	return new Promise(function (resolve, reject) {
		var values = [p_uuid];
		var sql = 'SELECT "uuid", "dtc", "login", "script", "type_script", "db_name", "dt_send", "result_send", "result_interim", "answer" FROM "tscriptstomob" WHERE "uuid"=$1 and "dtd" is null and "dt_send" is null   ';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let updDtSendByKey = (pool, p_dt_send, p_uuid) => {
	return new Promise(function (resolve, reject) {
		var values = [p_dt_send, p_uuid];
		var sql = 'UPDATE "tscriptstomob" SET "dt_send" = $1 WHERE "uuid"=$2 and "dt_send" is null and "dtd" is null RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let updAnswerByKey = (pool, p_answer, p_uuid) => {
	return new Promise(function (resolve, reject) {
		var values = [p_answer, p_uuid];
		var sql = 'UPDATE "tscriptstomob" SET "answer" = $1 WHERE "uuid"=$2 and "dt_send" is not null and "dtd" is null and "answer" is null RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let updResultSendByKey = (pool, p_result_send, p_uuid) => {
	return new Promise(function (resolve, reject) {
		var values = [p_result_send, p_uuid];
		var sql = 'UPDATE "tscriptstomob" SET "result_send" = $1 WHERE "uuid"=$2 and "dt_send" is not null and "dtd" is null and "result_send" is null RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let updResultInterimByKey = (pool, p_result_interim, p_uuid) => {
	return new Promise(function (resolve, reject) {
		var values = [p_result_interim, p_uuid];
		var sql = 'UPDATE "tscriptstomob" SET "result_interim" = $1 WHERE "uuid"=$2 and "dt_send" is not null and "dtd" is null RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
let insScriptToMob = (pool, p_dtd, p_login, p_script, p_type_script, p_db_name, p_dt_send, p_result_send, p_result_interim, p_answer) => {
	return new Promise(function (resolve, reject) {
		var p_key =  UUID();
		var p_dtc = new Date().toLocaleString();
		var values = [p_key, p_dtc, p_dtd, p_login, p_script, p_type_script, p_db_name, p_dt_send, p_result_send, p_result_interim, p_answer];
		var sql = 'INSERT INTO "tscriptstomob" ("uuid", "dtc", "dtd", "login", "script", "type_script", "db_name", "dt_send", "result_send", "result_interim", "answer") VALUES($1, $2, $3, $4, $5, $6, $7, $8, $9, $10, $11) RETURNING *';
		pool.query(sql, values)
			.then(res => {
				resolve(res);
			})
		.catch(err => {reject(err)})
	});
};
 module.exports = {selectCountNewScriptByKey, selectCountSendScriptByKey, getScriptToMobNew, getScriptToMobByKey, updDtSendByKey, updAnswerByKey, updResultSendByKey, updResultInterimByKey, insScriptToMob}