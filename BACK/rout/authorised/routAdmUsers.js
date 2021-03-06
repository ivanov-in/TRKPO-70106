const express = require('express');
const router = express.Router();
const adm_controller = require('../../controllers/adm_controller')


router.post('/admuser_user_lock', function (req, res, next) {
    let token = req.header("token");
    let lock;
    let id_insp;
    try {
        lock = req.header("lock_user");
        id_insp = req.header("id_insp");
        lock = lock = parseInt(lock);
        id_insp = parseInt(id_insp);
    } catch (e) {
        res.status(500).json({error: err});
        return;
    }
    //  token, lock, id_insp
    adm_controller.execute(adm_controller.admuserUserLock, {token, lock, id_insp}).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json({error: error})
    });
});

router.post('/admuserslist', function (req, res, next) {
    adm_controller.execute(adm_controller.admuserslist, null).then(function (json) {
        res.status(200).json(json);
    }).catch(function (err) {
        res.status(500).json({"error": "server Error"});
    });
});

router.post('/admuser_set_tel', function (req, res, next) {
    const token = req.header("token");
    let id_user;
    let tel;
    try {
        id_user = req.header("id_user");
        tel = req.header("tel_user");
        id_user = parseInt(id_user);
        // id_user, tel, token
        adm_controller.execute(adm_controller.admuserSetTel, {id_user, tel, token}).then(function (result) {
            res.status(200).json(result);
        }).catch(err => {
            res.status(500).json({"error": "server Error"});
        })
    } catch (e) {
        res.status(500).json({"error": "server Error"});
    }
});

router.post('/admuser_roles_add', function (req, res, next) {
    let token = req.header("token");
    let roles;
    let id_user;
    try {
        roles = req.header("roles");
        id_user = req.header("id_user");
        id_user = parseInt(id_user);
    } catch (e) {
        res.status(500).json({"error": "server Error"});
        return;
    }
    // id_user, roles, token
    adm_controller.execute(adm_controller.admuserRolesAdd, {id_user, roles, token}).then(function (result) {
        res.status(200).json(result);
    }).catch(function (error) {
        res.status(500).json({"error": "server Error"});
    });
});

router.post('/admuser_roleslist', function (req, res, next) {
    const token = req.header("token");
    // p_adm_token
    adm_controller.execute(adm_controller.getUserRoles, {p_adm_token: token}).then(function (json) {
        res.status(200).json(json);
    }).catch(function (err) {
        res.status(500).json({error: err})
    })
});

router.post('/admuser_pass_set', function (req, res, next) {
    let token = req.header("token");
    let id_user;
    try {
        id_user = req.header("id_insp");
        id_user = parseInt(id_user)
    } catch (e) {
        res.status(500).json({error: err});
        return;
    }
    adm_controller.execute(adm_controller.admuserPasSetDef, {token, id_user}).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json({error: error})
    });
});

router.post('/user_chenge_password', function (req, res, next) {
    let old_pass = req.header("old_password");
    let new_pass = req.header("new_password");
    let token = req.header("token");
    // token, old_pass, new_pass
    adm_controller.execute(adm_controller.userChengePassword, {token, old_pass, new_pass}).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json({error: error})
    })
});

router.post('/admuser_new_user', function (req, res, next) {
    let roles = req.header("roles");
    let rolesArr = roles.split(',');
    let tel = req.header("tel_insp");
    let token = req.header("token");
    let fio = '';
    req.on('data', chunk => {
        fio += chunk.toString();
    });
    // token, fio, tel, rolesArr
    req.on('end', (path, callback) => {
        adm_controller.execute(adm_controller.admuserCreateUser, {token, fio, tel, rolesArr}).then(function (json) {
            res.status(200).json(json);
        }).catch(function (error) {
            res.status(500).json({error: error})
        });
    });
});

router.post('/select_events_list', function (req, res, next) {
    let date = req.header("nowDate");
    // date
    adm_controller.execute(adm_controller.selectevenList, {date}).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json({error: error})
    });
});






// router.post('/user_delete', function (req, res, next) {
//     let token = req.header("token");
//     let id_user;
//     try {
//         id_user = req.header("id_user");
//         id_user = parseInt(id_user);
//     } catch (e) {
//         res.status(500).json({error: err});
//         return;
//     }
//     routAdmUsersController.userDelete(token, id_user).then(function (json) {
//         res.status(200).json(json);
//     }).catch(function (error) {
//         res.status(500).json({error: error})
//     })
// });



// router.post('/events_list', function (req, res, next) {
//     routAdmUsersController.evenList().then(function (json) {
//         res.status(200).json(json);
//     }).catch(function (error) {
//         res.status(500).json({error: error})
//     });
// });

// router.post('/devises_list', function (req, res, next) {
//     routAdmUsersController.devicesList().then(function (json) {
//         res.status(200).json(json);
//     }).catch(function (error) {
//         res.status(500).json({error: error})
//     });
// });



module.exports = router;
