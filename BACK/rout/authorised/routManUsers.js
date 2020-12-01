const express = require('express');
const router = express.Router();
const pool = require('../../data/db_pool');
const fs = require('fs')
// const Blob = require('blob');
let oracledb = require('oracledb');
const {poolOra: poolOra} = require("../../data/db_pool");
const tman_task_dao = require('../../data/tman_task_dao');
const routManUsersController = require('../httpControllers/routManUsersController');


router.post('/inspectors', function (req, res, next) {
    routManUsersController.selectInspectors().then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });
});

router.post('/dog_payers', function (req, res, next) {
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();
    });
    req.on('end', (path, callback) => {
        body = JSON.parse(body)
        routManUsersController.get_dog_payers(body['str']).then(function (json) {
            res.status(200).json(json);
        }).catch(function (error) {
            res.status(500).json(error);
        });
    })
});
router.post('/obj_asuse', function (req, res, next) {
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();

    });
    req.on('end', (path, callback) => {
        body = JSON.parse(body)
        routManUsersController.get_obj_asuse(body['str']).then(function (json) {
            res.status(200).json(json);
        }).catch(function (error) {
            res.status(500).json(error);
        });
    })
});
router.post('/get_history', function (req, res, next) {
    let dateS = req.header('dateS')
    let datePo = req.header('datePo')
    let koddog = req.header('koddog')
    let kodobj = req.header('kodobj')
    let kodp = req.header('kodp')
    let id = req.header('idIns')
    if (koddog !== '') {
        koddog = parseInt(koddog)
    } else {
        koddog = null
    }
    if (kodobj !== '') {
        kodobj = parseInt(kodobj)
    } else {
        kodobj = null
    }
    if (kodp !== '') {
        kodp = parseInt(kodp)
    } else {
        kodp = null
    }
    if (id !== '') {
        id = parseInt(id)
    } else {
        id = null
    }

    routManUsersController.get_history(dateS, datePo, koddog, kodobj, kodp, id).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });
});

router.post('/work_inspector', function (req, res, next) {
    let dateS = req.header('dateS')
    let datePo = req.header('datePo')
    routManUsersController.selectWorkInspector(dateS, datePo).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });
});

router.post('/list_tasks', function (req, res, next) {
    let date = req.header('nowDate');
    routManUsersController.selectTasks(date).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });

});

router.post('/send_tasks', function (req, res, next) {
    let date = req.header('nowDate');
    let id_insp = req.header('id_insp');
    routManUsersController.sendTasks(date, id_insp).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });

});

router.post('/check_marshrut', function (req, res, next) {
    let date = req.header('dtc');
    let id_insp = req.header('id_ins');
    routManUsersController.checkMarshrut(date, id_insp).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });

});

router.post('/get_list_obj', function (req, res, next) {
    let idTask = req.header('id_task');
    routManUsersController.getListObj(idTask).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });

});

router.post('/add_task', function (req, res, next) {
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();
    });
    req.on('end', (path, callback) => {
        body = JSON.parse(body)
        oracledb.getConnection(poolOra.hrPool).then(function (connection) {
            tman_task_dao.add_task(connection, body['address'], body['city'], body['street'], body['korpus'], body['house'], parseInt(body['purpose']), body['prim'], body['time'], parseInt(body['id_ins']), body['email'], body['lat'], body['lan'], body['s_zulu'], body['b_zulu'], parseInt(body['status'])).then(function (json) {
                pool.connectionRelease(connection);
                res.status(200).json(json);
            }).catch(function (err) {
                pool.connectionRelease(connection);
                res.status(500).json(err);
            })
        })
    });
})

router.post('/get_obj', function (req, res, next) {
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();

    });
    req.on('end', (path, callback) => {
        routManUsersController.getObj(body).then(function (result) {
            res.status(200).json(result);
        }).catch(function (error) {
            res.status(500).json(error);
        });
    })
});

router.post('/change_task', function (req, res, next) {
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();

    });
    req.on('end', (path, callback) => {
        body = JSON.parse(body)
        tman_task_dao.change_task(body['id_task'], body['address_ya'], body['city'], body['street'], body['korpus'], body['house'], body['purpose'], body['prim'], body['time'], body['id_ins'], body['puser'], body['kod_obj'], body['kod_dog'], body['kodp'], body['kod_num_obj'], body['fio_contact'], body['email_contact'], body['tel_contact'], null, body['status'], body['lat'], body['lan'], '', '', null).then(function (json) {
            res.status(200).json(json);
        }).catch(function (error) {
            res.status(500).json({error: error})
        });
    })
});

router.post('/get_contacts', function (req, res, next) {
    let kodp = req.header('kodp');
    routManUsersController.getContacts(kodp).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });
});

router.post('/get_task', function (req, res, next) {
    let id = req.header('id_task');
    oracledb.getConnection(poolOra.hrPool).then(function (connection) {
        tman_task_dao.get_task(connection, parseInt(id)).then(function (json) {
            pool.connectionRelease(connection);
            res.status(200).json(json);
        }).catch(function (error) {
            pool.connectionRelease(connection);
            res.status(500).json(error);
        });
    })
});

router.post('/get_task_inspector', function (req, res, next) {
    let date = req.header('datePic');
    let id_insp = req.header('id_insp');
    let puser = req.header('puser');
    routManUsersController.getTaskInspector(date, id_insp, puser).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });
});

router.post('/get_list_obj_lookup', function (req, res, next) {
    let pid_task = req.header('pid_task');
    let pkodp = req.header('pkodp');
    let pkod_dog = req.header('pkod_dog');
    if (pkodp === "") {
        pkodp = null
    } else {
        pkodp = parseInt(pkodp)
    }
    if (pkod_dog === "") {
        pkod_dog = null
    } else {
        pkod_dog = parseInt(pkod_dog)
    }
    pid_task = parseInt(pid_task)
    routManUsersController.get_list_obj_lookup(pid_task, pkodp, pkod_dog).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });
});

router.post('/get_list_payers_lookup', function (req, res, next) {
    let pid_task = req.header('pid_task');
    let pkod_numobj = req.header('pkod_numobj');
    let pkod_dog = req.header('pkod_dog');
    if (pkod_numobj === "") {
        pkod_numobj = null
    } else {
        pkod_numobj = parseInt(pkod_numobj)
    }
    if (pkod_dog === "") {
        pkod_dog = null
    } else {
        pkod_dog = parseInt(pkod_dog)
    }
    pid_task = parseInt(pid_task)
    routManUsersController.get_list_payers_lookup(pid_task, pkod_numobj, pkod_dog).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });
});
router.post('/get_list_dogovors_lookup', function (req, res, next) {
    let pid_task = req.header('pid_task');
    let pkod_numobj = req.header('pkod_numobj');
    let pkodp = req.header('pkodp');
    if (pkod_numobj === "") {
        pkod_numobj = null
    } else {
        pkod_numobj = parseInt(pkod_numobj)
    }
    if (pkodp === "") {
        pkodp = null
    } else {
        pkodp = parseInt(pkodp)
    }
    pid_task = parseInt(pid_task)
    routManUsersController.get_list_dogovors_lookup(pid_task, pkod_numobj, pkodp).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });
});

router.post('/task_files', function (req, res, next) {
    let pid_task = req.header('pid_task');

    routManUsersController.get_task_files(pid_task).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });
});


router.get('/file/:id', function (req, res, next) {

    oracledb.getConnection().then(function (connection) {
        routManUsersController.get_file(connection, Number.parseInt(req.params.id)).then(blob => {
            res.setHeader('Content-Length', blob.length);
            blob.pipe(res).then(_ => {
                blob.close;
                pool.connectionRelease(connection);
            }).catch(_ => {
                pool.connectionRelease(connection);
                res.json({file: 'null'})
            })
        })
    }).catch(function (err) {
        console.log(err)
    })

});

router.post('/delete_task', function (req, res, next) {
    let id = req.header('pid_task');
    routManUsersController.deleteTask(id).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });
});
router.post('/delete_file', function (req, res, next) {
    let id_task = req.header('pid_task');
    let id_file = req.header('pid_file');
    routManUsersController.deleteFile(id_task, id_file).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        res.status(500).json(error);
    });
});

router.post('/search_events', function (req, res, next) {
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();
    });
    req.on('end', (path, callback) => {
        body = JSON.parse(body)
        oracledb.getConnection(poolOra.hrPool).then(function (connection) {
            routManUsersController.get_search_event(connection, body['datePic'], body['string']).then(function (json) {
                pool.connectionRelease(connection);
                res.status(200).json(json);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                res.status(500).json(error);
            });
        })
    })
});


router.post('/upload_file', (req, res) => {
    let id = req.header('pid_task')
    let signed = req.header('signed')
    let paper = req.header('paper')
    let file = req.file
    let str;
    try {
        const readStream = fs.createReadStream(file.path, {highWaterMark: 16})
        const data = []

        readStream.on('data', (chunk) => {
            data.push(chunk);
            // console.log('data :', chunk, chunk.length);
        })

        readStream.on('end', () => {
            str = Buffer.concat(data);
            oracledb.getConnection(poolOra.hrPool).then(function (connection) {
                routManUsersController.upload_name(connection, id, file.originalname, signed, paper).then(function (json) {
                    routManUsersController.add_blob(connection, json['outBinds'].po_id_file, str).then(function (json) {
                        // pool.connectionRelease(connection);
                        res.status(200).json(json);
                    }).catch(function (error) {
                        pool.connectionRelease(connection);
                        res.status(500).json(error);
                    });
                    res.status(200).json(json);
                }).catch(function (error) {
                    pool.connectionRelease(connection);
                    res.status(500).json(error);
                });
                //
               // res.status(200).json({result: 'OK'})
            })
        })


    } catch (e) {
        console.log(e)
    }

})
module.exports = router;
