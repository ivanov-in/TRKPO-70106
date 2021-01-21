const admusers = require('../controllers/admusers');


admusers.getUrerRoles('adm5@tatenergo.ru').then(
    function (json) {
        const res = json;
    }
).catch(function (err) {
    const res = err;
});
