const adm_users_dao = require('../data/tadm_users_dao');
const tadm_user_roles_dao = require('../data/tadm_user_roles_dao');
const pool = require('../data/db_pool');

// adm_users_dao.selectList(pool.con).then(function (result) {
//     const r = result;
// }).catch(function (err) {
//     const e = err;
// });


tadm_user_roles_dao.selectUserRoles(pool.con, '80d5abdd-3425-46f1-9a82-d015481fc54e').then(function (result) {
    const r = result;
}).catch(function (err) {
    const e = err;
});

