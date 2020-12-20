const express = require('express');
const router = express.Router();
const pool = require('../../oracledb/pool');
const fs = require('fs')
// const Blob = require('blob');
const tman_task_dao = require('../../data/tman_task_dao');
const routManUsersController = require('../httpControllers/routManUsersController');
const controller = require('../../controllers/man_controller');
const logUtil = require("../../util/logUtil");


const responseRouterError = (req, res, error) => {
    logUtil.we(`${req.originalUrl} mes: ${error}`);
    res.status(500).json({error});
}

router.post('/inspectors', function (req, res, next) {
    controller.execute(controller.inspectors, null).then(json => {
        res.status(200).json(json);
    }).catch(error => {
        responseRouterError(req, res, error);
    });
});

router.post('/podpisant_list', function (req,res, next){
    try {
        let p_kodp = req.header('p_kodp');
        if (p_kodp === ''){
            p_kodp = null
        } else {
            p_kodp = parseInt(p_kodp)
        }
        controller.execute(controller.get_podpisant_list, {p_kodp}).then(json => {
            res.status(200).json(json);
        }).catch(error => {
            responseRouterError(req, res, error);
        })
    } catch (e) {
        responseRouterError(req, res, e);
    }
})

router.post('/get_task_inspector', function (req, res, next) {
    try {
        let date = req.header('datePic');
        let id_insp = req.header('id_insp');
        let puser = req.header('puser');
        controller.execute(controller.getTaskInspector, {date, id_insp, puser}).then(function (json) {
            res.status(200).json(json);
        }).catch(function (error) {
            responseRouterError(req, res, error);
        });
    } catch (e) {
        responseRouterError(req, res, e);
    }
});

router.post('/get_task', function (req, res, next) {
    try {
        let id = req.header('id_task');
        controller.execute(controller.get_task, {id: parseInt(id)}).then(json => {
            res.status(200).json(json);
        }).catch(error => {
            responseRouterError(req, res, error);
        });
    } catch (e) {
        responseRouterError(req, res, e);
    }
});

router.post('/get_list_obj_lookup', function (req, res, next) {
    try {
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
        controller.execute(controller.get_list_obj_lookup, {pid_task, pkodp, pkod_dog}).then(function (json) {
            res.status(200).json(json);
        }).catch(function (error) {
            responseRouterError(req, res, error);
        });
    } catch (e) {
        responseRouterError(req, res, e);
    }
});

router.post('/get_list_payers_lookup', function (req, res, next) {
    try {
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
        controller.execute(controller.get_list_payers_lookup, {pid_task, pkod_numobj, pkod_dog}).then(function (json) {
            res.status(200).json(json);
        }).catch(function (error) {
            responseRouterError(req, res, error);
        });
    } catch (e) {
        responseRouterError(req, res, e);
    }
});

router.post('/get_list_dogovors_lookup', function (req, res, next) {
    try {
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
        controller.execute(controller.get_list_dogovors_lookup, {pid_task, pkod_numobj, pkodp}).then(function (json) {
            res.status(200).json(json);
        }).catch(function (error) {
            responseRouterError(req, res, error);
        });
    } catch (e) {
        responseRouterError(req, res, e);
    }
});

router.post('/change_task', function (req, res, next) {
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();

    });
    req.on('end', (path, callback) => {
        try {
            body = JSON.parse(body)
            controller.execute(
                controller.change_task,
                {
                    id_task: body['id_task'],
                    adr_ya: body['address_ya'],
                    city: body['city'],
                    street: body['street'],
                    house: body['korpus'],
                    nd: body['house'],
                    purpose: body['purpose'],
                    prim: body['prim'],
                    time: body['time'],
                    id_insp: body['id_ins'],
                    puser: body['puser'],
                    kod_obj: body['kod_obj'],
                    kod_dog: body['kod_dog'],
                    kodp: body['kodp'],
                    kod_numobj: body['kod_num_obj'],
                    fio_contact: body['fio_contact'],
                    email_contact: body['email_contact'],
                    tel_contact: body['tel_contact'],
                    dol: null,
                    status: body['status'],
                    lat: body['lat'],
                    lan: body['lan'],
                    s_zulu: '',
                    b_zulu: '',
                    statusOut: null,
                    kod_emp: body['kod_emp']
                }).then(function (json) {
                res.status(200).json(json);
            }).catch(function (error) {
                responseRouterError(req, res, error);
            });
        } catch (e) {
            responseRouterError(req, res, e);
        }
    })
});


router.post('/task_files', function (req, res, next) {
    let pid_task = req.header('pid_task');
    controller.execute(controller.get_task_files, {id: pid_task}).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        responseRouterError(req, res, error);
    });
});

router.post('/delete_task', function (req, res, next) {
    let id = req.header('pid_task');
    controller.execute(controller.deleteTask, {id}).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        responseRouterError(req, res, error);
    });
});

router.post('/upload_file', (req, res) => {
    try {
        let id = req.header('pid_task')
        let signed = req.header('signed')
        let paper = req.header('paper')
        let file = req.file
        let str;
        const readStream = fs.createReadStream(file.path, {highWaterMark: 16})
        const data = []

        readStream.on('data', (chunk) => {
            data.push(chunk);
            // console.log('data :', chunk, chunk.length);
        })

        readStream.on('end', () => {
            str = Buffer.concat(data);
            controller.execute(controller.upload_file, {id, name: file.filename, signed, paper, str}).then(json => {
                res.status(200).json(json);
            }).catch(error => {
                responseRouterError(req, res, error);
            })
        })
    } catch (e) {
        responseRouterError(req, res, e);
    }
})

router.post('/get_history', function (req, res, next) {
    try {
        let dateS = req.header('dateS')
        let datePo = req.header('datePo')
        let koddog = req.header('koddog')
        let kodobj = req.header('kodobj')
        let kodp = req.header('kodp')
        let insp = req.header('idIns')
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
        if (insp !== '') {
            insp = parseInt(insp)
        } else {
            insp = null
        }

        controller.execute(controller.get_history, {dateS, datePo, koddog, kodobj, kodp, insp}).then(function (json) {
            res.status(200).json(json);
        }).catch(function (error) {
            responseRouterError(req, res, error);
        })
    } catch (e) {
        responseRouterError(req, res, e);
    }
});

router.post('/obj_asuse', function (req, res, next) {
    try {
        let body = '';
        req.on('data', chunk => {
            body += chunk.toString();
        });
        req.on('end', (path, callback) => {
            body = JSON.parse(body)
            controller.execute(controller.get_obj_asuse, {string: body['str']}).then(function (json) {
                res.status(200).json(json);
            }).catch(function (error) {
                responseRouterError(req, res, error);
            });
        });
    } catch (e) {
        responseRouterError(req, res, e);
    }
});

router.post('/send_tasks', function (req, res, next) {
    let date = req.header('nowDate');
    let id_insp = req.header('id_insp');
    controller.execute(controller.sendTasks, {date, id_insp}).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        responseRouterError(req, res, error);
    });
});

router.post('/add_task', function (req, res, next) {
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();
    });
    req.on('end', (path, callback) => {
        body = JSON.parse(body);
        controller.execute(controller.add_task, {
                adr: body['address'],
                city: body['city'],
                street: body['street'],
                house: body['korpus'],
                nd: body['house'],
                purpose: parseInt(body['purpose']),
                prim: body['prim'],
                ttime: body['time'],
                id_ins: parseInt(body['id_ins']),
                puser: body['email'],
                lat: body['lat'],
                lan: body['lan'],
                s_zulu: body['s_zulu'],
                b_zulu: body['b_zulu'],
                status: parseInt(body['status'])
            }
        ).then(function (json) {
            //adr, city, street, house, nd, purpose, prim, ttime, id_ins, puser, lat, lan, s_zulu, b_zulu, status
            res.status(200).json(json);
        }).catch(error => {
            responseRouterError(req, res, error);
        })
    });
})

router.post('/dog_payers', function (req, res, next) {

    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();
    });
    req.on('end', (path, callback) => {
        try {
            body = JSON.parse(body);
            controller.execute(controller.get_dog_payers,{string: body['str']}).then(function (json) {
                res.status(200).json(json);
            }).catch(function (error) {
                responseRouterError(req, res, error);
            });
        } catch (e) {
            responseRouterError(req, res, e);
        }
    });
});

router.get('/file/:id', function (req, res, next) {
    controller.execute(controller.get_file, {id: Number.parseInt(req.params.id)}, false).then(result => {
        let connection = result.connection;
        let blob = result.data;
        res.setHeader('Content-Length', blob.length);
        let cur_len = 0;
        blob.on('data', (x) => {
            // console.log(`on data ${cur_len} += ${x.length}`);
            cur_len += x.length
            if (cur_len >= blob.length) {
                blob.end()
            }
        })
        blob.on('finish', () => {
            console.log('on finish');
        })
        blob.on('error', (error) => {
            responseRouterError(req, res, error);
        })
        blob.on('close', () => {
            pool.connectionRelease(connection).then(_ => {
                res.end()
            });
        })
        blob.resume().pipe(res, {end: false});
    }).catch(error => {
        responseRouterError(req, res, error);
    })
});

router.post('/delete_file', function (req, res, next) {
    let id_task = req.header('pid_task');
    let id_file = req.header('pid_file');
    controller.execute(controller.deleteFile,{id_task, id_file}).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        logUtil.we(error);
        res.status(500).json({error});
    });
});





router.post('/work_inspector', function (req, res, next) {
    let dateS = req.header('dateS')
    let datePo = req.header('datePo')
    routManUsersController.selectWorkInspector(dateS, datePo).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        logUtil.we(error);
        res.status(500).json({error});
    });
});

router.post('/list_tasks', function (req, res, next) {
    let date = req.header('nowDate');
    routManUsersController.selectTasks(date).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        logUtil.we(error);
        res.status(500).json({error});
    });
});

router.post('/check_marshrut', function (req, res, next) {
    let date = req.header('dtc');
    let id_insp = req.header('id_ins');
    routManUsersController.checkMarshrut(date, id_insp).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        logUtil.we(error);
        res.status(500).json({error});
    });
});

router.post('/get_list_obj', function (req, res, next) {
    let idTask = req.header('id_task');
    routManUsersController.getListObj(idTask).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        logUtil.we(error);
        res.status(500).json({error});
    });
});

router.post('/get_obj', function (req, res, next) {
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();

    });
    req.on('end', (path, callback) => {
        routManUsersController.getObj(body).then(function (result) {
            res.status(200).json(result);
        }).catch(function (error) {
            logUtil.we(error);
            res.status(500).json({error});
        });
    })
});

router.post('/get_contacts', function (req, res, next) {
    let kodp = req.header('kodp');
    routManUsersController.getContacts(kodp).then(function (json) {
        res.status(200).json(json);
    }).catch(function (error) {
        logUtil.we(error);
        res.status(500).json({error});
    });
});




router.post('/search_events', function (req, res, next) {
    let body = '';
    req.on('data', chunk => {
        body += chunk.toString();
    });
    req.on('end', (path, callback) => {
        body = JSON.parse(body)
        pool.getConnection().then(function (connection) {
            routManUsersController.get_search_event(connection, body['datePic'], body['string']).then(function (json) {
                pool.connectionRelease(connection).then(_ => {
                    res.status(200).json(json);
                }).catch(errCR => {
                    logUtil.we(error);
                    res.status(500).json({errCR});
                });
            }).catch(function (error) {
                pool.connectionRelease(connection).then(_ => {
                    logUtil.we(error);
                    res.status(500).json({error});
                }).catch(errCR => {
                    logUtil.we(error);
                    res.status(500).json({error, errCR});
                });
            });
        }).catch(errCC => {
            logUtil.we(errCC)
            logUtil.we(error);
            res.status(500).json({errCC})
        })
    })
});


module.exports = router;
