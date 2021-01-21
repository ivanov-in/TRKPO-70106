const express = require('express');
const router = express.Router();
const GET_DATA_CONTROLLER = require('../controllers/getDataController');
const SET_DATA_CONTROLLER = require('../controllers/setDataController');
const SET_DATA_DAL = require('../data/setDataDal');
const GET_DATA_DAL = require('../data/getDataDal');
const pool = require('../data/db_pool');
let oracledb = require('oracledb');
const fs = require('fs')
var multer = require('multer')
const forge = require('node-forge');
const nodemailer = require('nodemailer')


storage = multer.diskStorage({
    destination: './upload_dir',
    filename: function(req, file, cb) {
       return cb(null, file.originalname);
    }
});


pool.init();


/* GET home page. */
router.get('/api/', function (req, res, next) {
    res.status(200).json(
        {
            login: true
        });
});

// Данные по инспектору
router.post('/mobback/get/get_inspector_data', function (req, res, next) {

    let puser = req.query['p_puser'];
    let password = req.query['p_password'];
   /* const md = forge.md.sha256.create();
    md.update(password);
    let result = forge.util.encode64(md.digest().toHex())*/


    oracledb.getConnection().then(function (connection) {
        GET_DATA_DAL.getInspectorDataDal(connection, puser, password).then(function (json) {
            pool.connectionRelease(connection);
            let s = JSON.stringify(json[0]);
            const obj = JSON.parse(s);
            if (obj.errmsg != undefined)
            { res.status(201).json(json);}
            else
            {res.status(200).json(json);}
        }).catch(function (err) {
            pool.connectionRelease(connection);
            res.status(500).json({error:err});

        });

    }).catch(function (err) {
        pool.connectionRelease(connection);
        res.status(500).json({error: err.message})

    });
});

// Поиск объектов при  добавлении нового задания
// Возвращает список объектов,
// найденных по переданной строке в адресе объекта или наименовании абонента
router.post('/mobback/get/get_objects', function (req, res, next) {

    let searchString = req.query['psearch_string'];
    GET_DATA_CONTROLLER.getObjects(searchString).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

// Список задач инспектора на дату
router.post('/mobback/get/get_tasks', function (req, res, next) {

    let pdate = req.query['pdate'];
    let pid_inspector = req.query['pid_inspector'];
    let pi_task_status = req.query['pi_task_status'];
    GET_DATA_CONTROLLER.getTasks(pdate, parseInt(pid_inspector), parseInt(pi_task_status)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

//Получение файлов к заданиям
router.post('/mobback/get/get_tasks_files', function (req, res, next) {

    let pdate = req.query['pdate'];
    let pid_inspector = req.query['pid_inspector'];
    let pi_task_status = req.query['pi_task_status'];
    GET_DATA_CONTROLLER.getTasksFiles(pdate, parseInt(pid_inspector), parseInt(pi_task_status)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

//Получение файлов к истории посещений к заданиям
router.post('/mobback/get/get_tasks_files_hist', function (req, res, next) {

    let pdate = req.query['pdate'];
    let pid_inspector = req.query['pid_inspector'];
    let pi_task_status = req.query['pi_task_status'];
    GET_DATA_CONTROLLER.getTasksFilesHist(pdate, parseInt(pid_inspector), parseInt(pi_task_status)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

//Получение файла
router.post('/mobback/get/get_file', function (req, res, next) {
    let pid_file = req.query['pid_file'];
    oracledb.getConnection().then(function (connection) {
        GET_DATA_CONTROLLER.getFile(parseInt(pid_file), connection).then(blob => {
            res.setHeader('Content-Length', blob.length);
            blob.pipe(res)
            blob.close;
            pool.connectionRelease(connection);
            res.status(200)
        }).catch(function (err) {
           pool.connectionRelease(connection);
           res.status(500).json({error: err.message})
        });
    }).catch(function (error) {
        pool.connectionRelease(connection);
        res.status(500).json({error: error.message})
    })
});

//Список доступных к посещению актов
router.post('/mobback/get/get_purpose_acts', function (req, res, next) {

    GET_DATA_CONTROLLER.getPurposeActs()
        .then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

//Акты к заданию со всеми полями
router.post('/mobback/get/get_act_fields', function (req, res, next) {

    let pdate = req.query['pdate'];
    let pid_inspector = req.query['pid_inspector'];
    let pi_task_status = req.query['pi_task_status'];
    GET_DATA_CONTROLLER.getActsFields(pdate, parseInt(pid_inspector), parseInt(pi_task_status)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

//Поля для заполнения акта (дополнительная информация для некоторых актов)
router.post('/mobback/get/get_act_fields_dop', function (req, res, next) {

    let pdate = req.query['pdate'];
    let pid_inspector = req.query['pid_inspector'];
    let pi_task_status = req.query['pi_task_status'];
    GET_DATA_CONTROLLER.getActsFieldsDop(pdate, parseInt(pid_inspector), parseInt(pi_task_status)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

// контакты выбранного абонента (при создании нового задания)
router.post('/mobback/get/get_contacts', function (req, res, next) {

    let kodp = req.query['pkodp'];
    GET_DATA_CONTROLLER.getContacts(parseInt(kodp)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

// справочник целей посещения
router.post('/mobback/get/get_purpose', function (req, res, next) {

    GET_DATA_CONTROLLER.getPurpose().then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

// Поиск абонента, договора по введённой строке в окне истории посещений
// Возвращает список договоров,
// найденных по переданной строке в номере договора или наименовании абонента
router.post('/mobback/get/get_dog_payers', function (req, res, next) {

    let searchString = req.query['psearch_string'];
    GET_DATA_CONTROLLER.getDogPayers(searchString).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

//история посещений
router.post('/mobback/get/get_history', function (req, res, next) {

    let pdate = req.query['pdate'];
    let pid_inspector = req.query['pid_inspector'];
    let pi_task_status = req.query['pi_task_status'];
    GET_DATA_CONTROLLER.getHistory(pdate, parseInt(pid_inspector), parseInt(pi_task_status)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

// данные для окна информация по договору
router.post('/mobback/get/get_dog_data', function (req, res, next) {

    let pdate = req.query['pdate'];
    let pid_inspector = req.query['pid_inspector'];
    let pi_task_status = req.query['pi_task_status'];
    GET_DATA_CONTROLLER.getDogData(pdate, parseInt(pid_inspector), parseInt(pi_task_status)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

// данные для окна информация по договору (объекты)
router.post('/mobback/get/get_dog_obj_data', function (req, res, next) {

    let pdate = req.query['pdate'];
    let pid_inspector = req.query['pid_inspector'];
    let pi_task_status = req.query['pi_task_status'];
    GET_DATA_CONTROLLER.getDogObjData(pdate, parseInt(pid_inspector), parseInt(pi_task_status)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

// данные для окна информация по договору (точкм учёта)
router.post('/mobback/get/get_dog_tu_data', function (req, res, next) {

    let pdate = req.query['pdate'];
    let pid_inspector = req.query['pid_inspector'];
    let pi_task_status = req.query['pi_task_status'];
    GET_DATA_CONTROLLER.getDogTuData(pdate, parseInt(pid_inspector), parseInt(pi_task_status)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

// данные для окна информация по договору (узлы учёта)
router.post('/mobback/get/get_dog_uu_data', function (req, res, next) {

    let pdate = req.query['pdate'];
    let pid_inspector = req.query['pid_inspector'];
    let pi_task_status = req.query['pi_task_status'];
    GET_DATA_CONTROLLER.getDogUuData(pdate, parseInt(pid_inspector), parseInt(pi_task_status)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

// данные для окна информация по договору (узлы учёта - СИ)
router.post('/mobback/get/get_dog_uu_si_data', function (req, res, next) {

    let pdate = req.query['pdate'];
    let pid_inspector = req.query['pid_inspector'];
    let pi_task_status = req.query['pi_task_status'];
    oracledb.getConnection().then(function (connection) {
        GET_DATA_DAL.getDogUuSiDataDal(connection, pdate, parseInt(pid_inspector), parseInt(pi_task_status)).then(function (json) {
            pool.connectionRelease(connection);
            res.status(200).json(json);

        }).catch(function (err) {
            pool.connectionRelease(connection);
            res.status(500).json({error:err});

        });

    }).catch(function (err) {
        pool.connectionRelease(connection);
        res.status(500).json({error: err.message})

    });
});

// данные для окна информация по договору (договор) в режиме On-line
router.post('/mobback/get/get_dog_data_online', function (req, res, next) {

    let kod_dog = req.query['pi_kod_dog'];
    oracledb.getConnection().then(function (connection) {
        GET_DATA_DAL.getDogDataOnlineDal(connection, parseInt(kod_dog)).then(function (json) {
            pool.connectionRelease(connection);
            res.status(200).json(json);

        }).catch(function (err) {
            pool.connectionRelease(connection);
            res.status(500).json({errmsg:err});

        });

    }).catch(function (err) {
        pool.connectionRelease(connection);
        res.status(500).json({error: errmsg.message})

    });
});

// данные для окна информация по договору (объекты) в режиме On-line
router.post('/mobback/get/get_dog_obj_data_online', function (req, res, next) {

    let kod_dog = req.query['pi_kod_dog'];
    oracledb.getConnection().then(function (connection) {
        GET_DATA_DAL.getDogObjDataOnlineDal(connection, parseInt(kod_dog)).then(function (json) {
            pool.connectionRelease(connection);
            res.status(200).json(json);

        }).catch(function (err) {
            pool.connectionRelease(connection);
            res.status(500).json({errmsg:err});

        });

    }).catch(function (err) {
        pool.connectionRelease(connection);
        res.status(500).json({error: errmsg.message})

    });
});

// данные для окна информация по договору (ТУ) в режиме On-line
router.post('/mobback/get/get_dog_tu_data_online', function (req, res, next) {

    let kod_dog = req.query['pi_kod_dog'];
    oracledb.getConnection().then(function (connection) {
        GET_DATA_DAL.getDogTuDataOnlineDal(connection, parseInt(kod_dog)).then(function (json) {
            pool.connectionRelease(connection);
            res.status(200).json(json);

        }).catch(function (err) {
            pool.connectionRelease(connection);
            res.status(500).json({errmsg:err});

        });

    }).catch(function (err) {
        pool.connectionRelease(connection);
        res.status(500).json({error: errmsg.message})

    });
});

// данные для окна информация по договору (УУ) в режиме On-line
router.post('/mobback/get/get_dog_uu_data_online', function (req, res, next) {

    let kod_dog = req.query['pi_kod_dog'];
    oracledb.getConnection().then(function (connection) {
        GET_DATA_DAL.getDogUuDataOnlineDal(connection, parseInt(kod_dog)).then(function (json) {
            pool.connectionRelease(connection);
            res.status(200).json(json);

        }).catch(function (err) {
            pool.connectionRelease(connection);
            res.status(500).json({errmsg:err});

        });

    }).catch(function (err) {
        pool.connectionRelease(connection);
        res.status(500).json({error: errmsg.message})

    });
});

// данные для окна информация по договору (СИ УУ) в режиме On-line
router.post('/mobback/get/get_dog_uu_si_data_online', function (req, res, next) {

    let kod_dog = req.query['pi_kod_dog'];
    oracledb.getConnection().then(function (connection) {
        GET_DATA_DAL.getDogUuSiDataOnlineDal(connection, parseInt(kod_dog)).then(function (json) {
            pool.connectionRelease(connection);
            res.status(200).json(json);

        }).catch(function (err) {
            pool.connectionRelease(connection);
            res.status(500).json({errmsg:err});

        });

    }).catch(function (err) {
        pool.connectionRelease(connection);
        res.status(500).json({error: errmsg.message})

    });
});

// Создание запроса на автоматическое формирование задания
router.post('/mobback/set/update_min_task_id', function (req, res, next) {
    let id_ins = req.query['p_id_inspector'];
    let min_task_id = req.query['p_min_task_id'];

    SET_DATA_CONTROLLER.updateMinTaskId(parseInt(id_ins),parseInt(min_task_id)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json(err)
    });
});

// Создание запроса на автоматическое формирование задания
router.post('/mobback/set/create_task', function (req, res, next) {
    let id_ins = req.query['p_id_inspector'];
    let id_task = req.query['p_id_task'];
    let kod_dog = req.query["p_kod_dog"];
    let purpose = req.query["p_purpose"];
    let kod_obj = req.query["p_kod_obj"];
    let kod_numobj = req.query["p_kod_numobj"];
    let ttime = req.query["p_ttime"];

    SET_DATA_CONTROLLER.createTask(parseInt(id_ins), parseInt(kod_dog), parseInt(purpose), parseInt(kod_obj), parseInt(kod_numobj), ttime, parseInt(id_task)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json(err)
    });
});

//Добавление задания, созданного вручную
router.post('/mobback/set/add_task', function (req, res, next) {
    let adr = req.query["p_adr"];
    let city = req.query["p_city"];
    let street = req.query["p_street"];
    let house = req.query["p_house"];
    let nd = req.query["p_nd"];
    let purpose = req.query["p_purpose"];
    let prim = req.query["p_prim"];
    let ttime = req.query["p_ttime"];
    let id_ins = req.query['p_id_inspector'];
    let id_task = req.query['p_id_task'];
    let kod_dog = req.query['p_kod_dog'];
    let kodp = req.query['p_kodp'];
    let kod_obj = req.query['p_kod_obj'];
    let kod_numobj = req.query['p_kod_numobj'];

    SET_DATA_CONTROLLER.addTask(adr, city, street, house, nd, parseInt(purpose), prim, ttime, parseInt(id_ins), parseInt(id_task),parseInt(kod_dog), parseInt(kodp), parseInt(kod_obj), parseInt(kod_numobj)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

//Обновление полей задания
router.post('/mobback/set/update_task', function (req, res, next) {
    let id_task = req.query["p_id_task"];
    let purpose = req.query["p_purpose"];
    let prim = req.query["p_prim"];
    let status = req.query["p_status"];
    let kod_emp_podp = req.query["p_kod_emp_podp"];
    let kod_dog = req.query['p_kod_dog'];
    let kodp = req.query['p_kodp'];
    let kod_obj = req.query['p_kod_obj'];
    let kod_numobj = req.query['p_kod_numobj'];

    SET_DATA_CONTROLLER.updateTask(parseInt(id_task), parseInt(purpose), prim, parseInt(kod_emp_podp), parseInt(status), parseInt(kod_dog), parseInt(kodp), parseInt(kod_obj), parseInt(kod_numobj)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

//Мобильное приняло задание
router.post('/mobback/set/accept_task', function (req, res, next) {
    let id_task = req.query["p_id_task"];
    SET_DATA_CONTROLLER.acceptTask(parseInt(id_task)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

//Мобильное отклонило задание
router.post('/mobback/set/refuse_task', function (req, res, next) {
    let id_task = req.query["p_id_task"];
    SET_DATA_CONTROLLER.refuseTask(parseInt(id_task)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

//Обновление полей актов
router.post('/mobback/set/insert_acts_fields', function (req, res, next) {
    let id_task = req.query["p_id_task"];
    let id_act = req.query["p_id_act"];
    let purpose_text = req.query["p_purpose_text"];
    let fact_text = req.query["p_fact_text"];
    let num_act = req.query["p_num_act"];
    let dat_act = req.query["p_dat_act"];
    let payer_name = req.query["p_payer_name"];
    let adr_org = req.query["p_adr_org"];
    let fio_contact = req.query["p_fio_contact"];
    let tel_contact = req.query["p_tel_contact"];
    let filial_eso = req.query["p_filial_eso"];
    let fio_eso = req.query["p_fio_eso"];
    let tel_eso = req.query["p_tel_eso"];
    let list_obj = req.query["p_list_obj"];
    let name_obj = req.query["p_name_obj"];
    let num_obj = req.query["p_num_obj"];
    let adr_obj = req.query["p_adr_obj"];
    let ndog = req.query["p_ndog"];
    let dat_dog = req.query["p_dat_dog"];
    let otop_period = req.query["p_otop_period"];
    let id_manometr = req.query["p_id_manometr"];
    let id_aupr_so = req.query["p_id_aupr_so"];
    let id_aupr_sw = req.query["p_id_aupr_sw"];
    let id_aupr_gvs = req.query["p_id_aupr_gvs"];
    let id_aupr_sv = req.query["p_id_aupr_sv"];
    let sost_kip_str = req.query["p_sost_kip_str"];
    let nal_act_gidro = req.query["p_nal_act_gidro"];
    let id_sost_tube = req.query["p_id_sost_tube"];
    let id_sost_armatur = req.query["p_id_sost_armatur"];
    let id_sost_izol = req.query["p_id_sost_izol"];
    let sost_tube_str = req.query["p_sost_tube_str"];
    let id_sost_net = req.query["p_id_sost_net"];
    let sost_net_str = req.query["p_sost_net_str"];
    let id_sost_utepl = req.query["p_id_sost_utepl"];
    let sost_utepl_str = req.query["p_sost_utepl_str"];
    let id_nal_pasport = req.query["p_id_nal_pasport"];
    let id_nal_schema = req.query["p_id_nal_schema"];
    let id_nal_instr = req.query["p_id_nal_instr"];
    let nal_pasp_str = req.query["p_nal_pasp_str"];
    let id_nal_direct_connect = req.query["p_id_nal_direct_connect"];
    let nal_direct_connect = req.query["p_nal_direct_connect"];
    let sum_dolg = req.query["p_sum_dolg"];
    let remark_dog = req.query["p_remark_dog"];
    let nal_podp_doc = req.query["p_nal_podp_doc"];
    let opl_calcul = req.query["p_opl_calcul"];
    let osnov = req.query["p_osnov"];
    let city = req.query["p_city"];
    let comiss_post_gotov = req.query["p_comiss_post_gotov"];
    let shablon = req.query["p_shablon"];
    let name_dolzhn_contact = req.query["p_name_dolzhn_contact"];
    let name_dolzhn_eso = req.query["p_name_dolzhn_eso"];
    let period_dolg = req.query["p_period_dolg"];
    let name_st = req.query["p_name_st"];
    let name_mag = req.query["p_name_mag"];
    let name_tk = req.query["p_name_tk"];
    let name_aw = req.query["p_name_aw"];
    let inn_org = req.query["p_inn_org"];
    let kpp_org = req.query["p_kpp_org"];
    let nazn_name = req.query["p_nazn_name"];
    let nal_so = req.query["p_nal_so"];
    let nal_sw = req.query["p_nal_sw"];
    let nal_st = req.query["p_nal_st"];
    let nal_gv = req.query["p_nal_gv"];
    let pred_comiss = req.query["p_pred_comiss"];
    let zam_pred_gkh = req.query["p_zam_pred_gkh"];
    let podpisi = req.query["p_podpisi"];
    let director_tatenergo = req.query["p_director_tatenergo"];
    let director_t_dover_num = req.query["p_director_t_dover_num"];
    let director_t_dover_date = req.query["p_director_t_dover_date"];
    let zayavitel = req.query["p_zayavitel"];
    let zayavitel_dover = req.query["p_zayavitel_dover"];
    let podgotovka = req.query["p_podgotovka"];
    let podgotovka_proj_num = req.query["p_podgotovka_proj_num"];
    let podgotovka_proj_ispoln = req.query["p_podgotovka_proj_ispoln"];
    let podgotovka_proj_utvergden = req.query["p_podgotovka_proj_utvergden"];
    let net_inner_teplonositel = req.query["p_net_inner_teplonositel"];
    let net_inner_dp = req.query["p_net_inner_dp"];
    let net_inner_do = req.query["p_net_inner_do"];
    let net_inner_tip_kanal = req.query["p_net_inner_tip_kanal"];
    let net_inner_tube_type_p = req.query["p_net_inner_tube_type_p"];
    let net_inner_tube_type_o = req.query["p_net_inner_tube_type_o"];
    let net_inner_l = req.query["p_net_inner_l"];
    let net_inner_l_undeground = req.query["p_net_inner_l_undeground"];
    let net_inner__otstuplenie = req.query["p_net_inner__otstuplenie"];
    let energo_effect_object = req.query["p_energo_effect_object"];
    let nal_rezerv_istochnik = req.query["p_nal_rezerv_istochnik"];
    let nal_svyazi = req.query["p_nal_svyazi"];
    let vid_connect_system = req.query["p_vid_connect_system"];
    let elevator_num = req.query["p_elevator_num"];
    let elevator_diam = req.query["p_elevator_diam"];
    let podogrev_otop_num = req.query["p_podogrev_otop_num"];
    let podogrev_otop_kolvo_sekc = req.query["p_podogrev_otop_kolvo_sekc"];
    let podogrev_otop_l_sekc = req.query["p_podogrev_otop_l_sekc"];
    let podogrev_otop_nazn = req.query["p_podogrev_otop_nazn"];
    let podogrev_otop_marka = req.query["p_podogrev_otop_marka"];
    let d_napor_patrubok = req.query["p_d_napor_patrubok"];
    let power_electro_engine = req.query["p_power_electro_engine"];
    let chastota_vr_engine = req.query["p_chastota_vr_engine"];
    let drossel_diafragma_d = req.query["p_drossel_diafragma_d"];
    let drossel_diafragma_mesto = req.query["p_drossel_diafragma_mesto"];
    let drossel_diafragma_tip_otop = req.query["p_drossel_diafragma_tip_otop"];
    let drossel_diafragma_cnt_stoyak = req.query["p_drossel_diafragma_cnt_stoyak"];
    let pu_data = req.query["p_pu_data"];
    let pu_pover_lico = req.query["p_pu_pover_lico"];
    let pu_pover_pokaz = req.query["p_pu_pover_pokaz"];
    let pu_pover_rez = req.query["p_pu_pover_rez"];
    let balans_prinadl_obj = req.query["p_balans_prinadl_obj"];
    let balans_prin_dop = req.query["p_balans_prin_dop"];
    let gr_ekspl_otvetst = req.query["p_gr_ekspl_otvetst"];
    let gr_ekspl_otvetst_dop = req.query["p_gr_ekspl_otvetst_dop"];
    let st_podkl_rub = req.query["p_st_podkl_rub"];
    let st_podkl_rub_nds = req.query["p_st_podkl_rub_nds"];
    let podkl_dop_sved = req.query["p_podkl_dop_sved"];
    let pred_pogash_dolg_num = req.query["p_pred_pogash_dolg_num"];
    let pred_pogash_dolg_date = req.query["p_pred_pogash_dolg_date"];
    let otkl_proizv = req.query["p_otkl_proizv"];
    let otkl_pu_pokaz_do = req.query["p_otkl_pu_pokaz_do"];
    let otkl_pu_pokaz_posle = req.query["p_otkl_pu_pokaz_posle"];
    let filial_name = req.query["p_filial_name"];
    let filial_address = req.query["p_filial_address"];
    let filial_tel = req.query["p_filial_tel"];
    let act_poluchil = req.query["p_act_poluchil"];
    let nal_document = req.query["p_nal_document"];
    let dop_info = req.query["p_dop_info"];
    let ispolnitel = req.query["p_ispolnitel"];
    let dog_podkl_num = req.query["p_dog_podkl_num"];
    let dog_podkl_date = req.query["p_dog_podkl_date"];
    let mesto_karta = req.query["p_mesto_karta"];
    let uvedoml_otkl_num = req.query["p_uvedoml_otkl_num"];
    let uvedoml_otkl_date = req.query["p_uvedoml_otkl_date"];
    let prichina_otkaza = req.query["p_prichina_otkaza"];
    let otkaz_svidet_1 = req.query["p_otkaz_svidet_1"];
    let otkaz_svidet_2 = req.query["p_otkaz_svidet_2"];
    let pravo_sobstv = req.query["p_pravo_sobstv"];
    let uvedom_aktirov_num = req.query["p_uvedom_aktirov_num"];
    let uvedom_aktirov_date = req.query["p_uvedom_aktirov_date"];
    let predst_potrebit = req.query["p_predst_potrebit"];
    let predst_potrebit_dover = req.query["p_predst_potrebit_dover"];
    let volume_obj = req.query["p_volume_obj"];
    let square_obj = req.query["p_square_obj"];
    let q_sum = req.query["p_q_sum"];
    let so_q = req.query["p_so_q"];
    let sw_q = req.query["p_sw_q"];
    let st_q = req.query["p_st_q"];
    let gw_q = req.query["p_gw_q"];
    let nal_pu = req.query["p_nal_pu"];
    let nal_aupr = req.query["p_nal_aupr"];
    let bezdog_sposob_num = req.query["p_bezdog_sposob_num"];
    let bezdog_ustanovleno = req.query["p_bezdog_ustanovleno"];
    let bezdog_narushenie = req.query["p_bezdog_narushenie"];
    let bezdog_pereraschet_s = req.query["p_bezdog_pereraschet_s"];
    let bezdog_pereraschet_po = req.query["p_bezdog_pereraschet_po"];
    let bezdog_predpis = req.query["p_bezdog_predpis"];
    let bezdog_obyasn = req.query["p_bezdog_obyasn"];
    let bezdog_pretenz = req.query["p_bezdog_pretenz"];
    let uslovie_podkl_num = req.query["p_uslovie_podkl_num"];
    let soglasop_proekte_num = req.query["p_soglasop_proekte_num"];
    let dopusk_s = req.query["p_dopusk_s"];
    let dopusk_po = req.query["p_dopusk_po"];
    let tel_spravki = req.query["p_tel_spravki"];
    let tel_dispetch = req.query["p_tel_dispetch"];
    let org_ustanov_pu = req.query["p_org_ustanov_pu"];
    let type_oto_prib = req.query["p_type_oto_prib"];
    let schema_vkl_gvs = req.query["p_schema_vkl_gvs"];
    let schema_vkl_podogrev = req.query["p_schema_vkl_podogrev"];
    let kolvo_sekc_1 = req.query["p_kolvo_sekc_1"];
    let kolvo_sekc_1_l = req.query["p_kolvo_sekc_1_l"];
    let kolvo_sekc_2 = req.query["p_kolvo_sekc_2"];
    let kolvo_sekc_2_l = req.query["p_kolvo_sekc_2_l"];
    let kolvo_kalorifer = req.query["p_kolvo_kalorifer"];
    let poverhnost_nagreva = req.query["p_poverhnost_nagreva"];
    let podkl_num = req.query["p_podkl_num"];
    let q_max = req.query["p_q_max"];
    let itog_text = req.query["p_itog_text"];
    let is_signed = req.query["is_signed"];
    let npp = req.query["p_npp"];

    SET_DATA_CONTROLLER.insertActsFields(parseInt(id_task),
        parseInt(id_act),
        parseInt(npp),
        purpose_text,
        fact_text,
        num_act,
        dat_act,
        payer_name,
        adr_org,
        fio_contact,
        tel_contact,
        filial_eso,
        fio_eso,
        tel_eso,
        list_obj,
        name_obj,
        num_obj,
        adr_obj,
        ndog,
        dat_dog,
        otop_period,
        id_manometr,
        id_aupr_so,
        id_aupr_sw,
        id_aupr_gvs,
        id_aupr_sv,
        sost_kip_str,
        nal_act_gidro,
        id_sost_tube,
        id_sost_armatur,
        id_sost_izol,
        sost_tube_str,
        id_sost_net,
        sost_net_str,
        id_sost_utepl,
        sost_utepl_str,
        id_nal_pasport,
        id_nal_schema,
        id_nal_instr,
        nal_pasp_str,
        id_nal_direct_connect,
        nal_direct_connect,
        sum_dolg,
        remark_dog,
        nal_podp_doc,
        opl_calcul,
        osnov,
        city,
        comiss_post_gotov,
        shablon,
        name_dolzhn_contact,
        name_dolzhn_eso,
        period_dolg,
        name_st,
        name_mag,
        name_tk,
        name_aw,
        inn_org,
        kpp_org,
        nazn_name,
        nal_so,
        nal_sw,
        nal_st,
        nal_gv,
        pred_comiss,
        zam_pred_gkh,
        podpisi,
        director_tatenergo,
        director_t_dover_num,
        director_t_dover_date,
        zayavitel,
        zayavitel_dover,
        podgotovka,
        podgotovka_proj_num,
        podgotovka_proj_ispoln,
        podgotovka_proj_utvergden,
        net_inner_teplonositel,
        net_inner_dp,
        net_inner_do,
        net_inner_tip_kanal,
        net_inner_tube_type_p,
        net_inner_tube_type_o,
        net_inner_l,
        net_inner_l_undeground,
        net_inner__otstuplenie,
        energo_effect_object,
        nal_rezerv_istochnik,
        nal_svyazi,
        vid_connect_system,
        elevator_num,
        elevator_diam,
        podogrev_otop_num,
        podogrev_otop_kolvo_sekc,
        podogrev_otop_l_sekc,
        podogrev_otop_nazn,
        podogrev_otop_marka,
        d_napor_patrubok,
        power_electro_engine,
        chastota_vr_engine,
        drossel_diafragma_d,
        drossel_diafragma_mesto,
        drossel_diafragma_tip_otop,
        drossel_diafragma_cnt_stoyak,
        pu_data,
        pu_pover_lico,
        pu_pover_pokaz,
        pu_pover_rez,
        balans_prinadl_obj,
        balans_prin_dop,
        gr_ekspl_otvetst,
        gr_ekspl_otvetst_dop,
        st_podkl_rub,
        st_podkl_rub_nds,
        podkl_dop_sved,
        pred_pogash_dolg_num,
        pred_pogash_dolg_date,
        otkl_proizv,
        otkl_pu_pokaz_do,
        otkl_pu_pokaz_posle,
        filial_name,
        filial_address,
        filial_tel,
        act_poluchil,
        nal_document,
        dop_info,
        ispolnitel,
        dog_podkl_num,
        dog_podkl_date,
        mesto_karta,
        uvedoml_otkl_num,
        uvedoml_otkl_date,
        prichina_otkaza,
        otkaz_svidet_1,
        otkaz_svidet_2,
        pravo_sobstv,
        uvedom_aktirov_num,
        uvedom_aktirov_date,
        predst_potrebit,
        predst_potrebit_dover,
        volume_obj,
        square_obj,
        q_sum,
        so_q,
        sw_q,
        st_q,
        gw_q,
        nal_pu,
        nal_aupr,
        bezdog_sposob_num,
        bezdog_ustanovleno,
        bezdog_narushenie,
        bezdog_pereraschet_s,
        bezdog_pereraschet_po,
        bezdog_predpis,
        bezdog_obyasn,
        bezdog_pretenz,
        uslovie_podkl_num,
        soglasop_proekte_num,
        dopusk_s,
        dopusk_po,
        tel_spravki,
        tel_dispetch,
        org_ustanov_pu,
        type_oto_prib,
        schema_vkl_gvs,
        schema_vkl_podogrev,
        kolvo_sekc_1,
        kolvo_sekc_1_l,
        kolvo_sekc_2,
        kolvo_sekc_2_l,
        kolvo_kalorifer,
        poverhnost_nagreva,
        podkl_num,
        q_max,
        itog_text,
        is_signed).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

//Удаление доп информации по актам перед записью
router.post('/mobback/set/delete_acts', function (req, res, next) {
    let id_task = req.query["p_id_task"];
    SET_DATA_CONTROLLER.deleteActs(parseInt(id_task)).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});

//Запись доп информации по актам
router.post('/mobback/set/insert_dop_act_fields', function (req, res, next) {
    let id_task = req.query["p_id_task"];
    let id_act = req.query["p_id_act"];
    let num_obj = req.query["p_num_obj"];
    let name_obj = req.query["p_name_obj"];
    let address_obj = req.query["p_address_obj"];
    let god_zd = req.query["p_god_zd"];
    let nazn_name = req.query["p_nazn_name"];
    let tvr = req.query["p_tvr"];
    let pr_oto = req.query["p_pr_oto"];
    let pr_sw = req.query["p_pr_sw"];
    let volume = req.query["p_volume"];
    let square_total = req.query["p_square_total"];
    let point_name = req.query["p_point_name"];
    let etaz = req.query["p_etaz"];
    let so_q = req.query["p_so_q"];
    let sw_q = req.query["p_sw_q"];
    let st_q = req.query["p_st_q"];
    let gw_qmax = req.query["p_gw_qmax"];
    let name_vodo = req.query["p_name_vodo"];
    let nn_wpol = req.query["p_nn_wpol"];
    let tt_wpol = req.query["p_tt_wpol"];
    let nn_prib = req.query["p_nn_prib"];
    let pr_rec = req.query["p_pr_rec"];
    let pr_psusch = req.query["p_pr_psusch"];
    let pr_iz_st = req.query["p_pr_iz_st"];
    let nom_uch = req.query["p_nom_uch"];
    let tip_name = req.query["p_tip_name"];
    let pt_d = req.query["p_pt_d"];
    let pt_l = req.query["p_pt_l"];
    let name_pr_pt = req.query["p_name_pr_pt"];
    let ot_d = req.query["p_ot_d"];
    let ot_l = req.query["p_ot_l"];
    let name_pr_ot = req.query["p_name_pr_ot"];
    let uch_hgr = req.query["p_uch_hgr"];
    let pu_num = req.query["p_pu_num"];
    let pu_name = req.query["p_pu_name"];
    let pu_mesto = req.query["p_pu_mesto"];
    let pu_type = req.query["p_pu_type"];
    let pu_diam = req.query["p_pu_diam"];
    let pu_kolvo = req.query["p_pu_kolvo"];
    let pu_proba_mesto = req.query["p_pu_proba_mesto"];
    let q_sum = req.query["p_q_sum"];
    let q_sum_max = req.query["p_q_sum_max"];
    let pu_srok_poverki = req.query["p_pu_srok_poverki"];
    let pu_num_plomba = req.query["p_pu_num_plomba"];
    let pu_pokaz = req.query["p_pu_pokaz"];
    let schema_prisoed_name = req.query["p_schema_prisoed_name"];
    let schema_prisoed_kod = req.query["p_schema_prisoed_kod"];
    let npp  = req.query["p_npp"];

    SET_DATA_CONTROLLER.insertDopActFields(parseInt(id_task), parseInt(id_act),parseInt(npp), num_obj,
        name_obj,
        address_obj,
        god_zd,
        nazn_name,
        tvr,
        pr_oto,
        pr_sw,
        volume,
        square_total,
        point_name,
        etaz,
        so_q,
        sw_q,
        st_q,
        gw_qmax,
        name_vodo,
        nn_wpol,
        tt_wpol,
        nn_prib,
        pr_rec,
        pr_psusch,
        pr_iz_st,
        nom_uch,
        tip_name,
        pt_d,
        pt_l,
        name_pr_pt,
        ot_d,
        ot_l,
        name_pr_ot,
        uch_hgr,
        pu_num,
        pu_name,
        pu_mesto,
        pu_type,
        pu_diam,
        pu_kolvo,
        pu_proba_mesto,
        q_sum,
        q_sum_max,
        pu_srok_poverki,
        pu_num_plomba,
        pu_pokaz,
        schema_prisoed_name,
        schema_prisoed_kod).then(function (json) {

        res.status(200).json(json);

    }).catch(function (err) {
        res.status(500).json({error: err.message})
    });
});


router.post('/mobback/set/add_file', multer({
    storage: storage
}).single('upload'), function(req, res) {
    let id_task = req.query["p_id_task"];
    let filename = req.file.filename// req.query["p_filename"];
    let is_signed = req.query["p_is_signed"];
    let paper = req.query["p_paper"];
    let date_send_to_client  = req.query["p_date_send_to_client"];
    let email_client = req.query["p_email_client"];
    let npp = req.query["p_npp"];
    let id_act = req.query["p_id_act"];
    let str;
    try {

        const readStream = fs.createReadStream('./upload_dir/'+filename, {highWaterMark: 16})
        const data = []

        readStream.on('data', (chunk) => {
            data.push(chunk);
           // console.log('data :', chunk, chunk.length);
        })

        readStream.on('end', () => {
            str = Buffer.concat(data);
            oracledb.getConnection().then(function (connection) {
                SET_DATA_DAL.addFileDal(connection, parseInt(id_task), filename, str, parseInt(is_signed),parseInt(paper), date_send_to_client, email_client,parseInt(npp), parseInt(id_act)).then(function (json) {
                    pool.connectionRelease(connection);
                    res.status(200).json({result: 'OK'})

                    }).catch(function (err) {
                    pool.connectionRelease(connection);
                        res.status(500).json({error:err});

                    });

            }).catch(function (err) {
                pool.connectionRelease(connection);
                res.status(500).json({error: err.message})

            });
        })


    } catch (e) {
        console.log(e)
    }

})


// просто тест сервера
router.post('/mobback/get/say_ok', function (req, res, next) {
    console.log("server answers OOOOkey!")
    res.statusCode = 200;
    res.setHeader('Content-Type', 'text/plain');
    res.end('OK');
});

router.get('/mobback/mis', function(req, res){
    const file = `d:\\WORK\\Web\\МИС\\apk\\mis.apk`;
    res.download(file); // Set disposition and send it.
});


// Список подписантов для задач инспектора на дату
router.post('/mobback/get/get_podpisant', function (req, res, next) {

    let pdate = req.query['pdate'];
    let pid_inspector = req.query['pid_inspector'];
    let pi_task_status = req.query['pi_task_status'];
    oracledb.getConnection().then(function (connection) {
        GET_DATA_DAL.getPodpisantDal(connection, pdate, parseInt(pid_inspector), parseInt(pi_task_status)).then(function (json) {
            pool.connectionRelease(connection);
            res.status(200).json(json);

        }).catch(function (err) {
            pool.connectionRelease(connection);
            res.status(500).json({error:err});

        });

    }).catch(function (err) {
        pool.connectionRelease(connection);
        res.status(500).json({error: err.message})

    });
});


// Кол-во задач,  у которых есть обновления на  инспектора на дату
router.post('/mobback/get/get_count_updates', function (req, res, next) {

    let pdate = req.query['pdate'];
    let pid_inspector = req.query['pid_inspector'];

    oracledb.getConnection().then(function (connection) {
        GET_DATA_DAL.getCountUpdatesDal(connection, pdate, parseInt(pid_inspector)).then(function (json) {
            pool.connectionRelease(connection);
            res.status(200).json(json);

        }).catch(function (err) {
            pool.connectionRelease(connection);
            res.status(500).json({error:err});

        });

    }).catch(function (err) {
        pool.connectionRelease(connection);
        res.status(500).json({error: err.message})

    });
});


let transporter = nodemailer.createTransport({
    host: 'mx1.infoenergo.ru',
    port: 25,
    secure: false,
    auth: {
        user: 'nyurchenko@infoenergo.ru',
        pass: 'dBUU5VVa',
    },
})

let transporterTat = nodemailer.createTransport({
    host: '10.1.1.9',
    port: 25,
    secure: false,
    auth: {
        user: '',
        pass: '',
    },
})
// Отправка почты
router.post('/mobback/set/send_email', function (req, res, next)
{
    let id_task = req.query['pi_id_task'];
    let id_act  = req.query['pi_id_act'];
    let npp = req.query['pi_npp'];
    let filename = req.query['pi_filename'];
    let emailTo = req.query['email_to'];
    let subject = req.query['subject'];

    att  = []
     oracledb.getConnection().then(function (connection) {
        GET_DATA_DAL.getSignedActDal(connection, parseInt(id_task),parseInt(id_act),parseInt(npp), filename).then(function (json) {
            let s = JSON.stringify(json[0]);
            if (s === undefined)
            {
                pool.connectionRelease(connection);
                res.status(201).json({errmsg: "Не найдено ни одного файла для отправки."});
            }
            else {
                const obj = JSON.parse(s);
                if (obj.errmsg != undefined) {
                   pool.connectionRelease(connection);
                    res.status(201).json(json);
                } else {
                    for (var i = 0; i < json.length; i++) {
                        let s = JSON.stringify(json[i]);
                        const obj = JSON.parse(s);
                        if (obj.FILENAME != undefined) {
                            att.push({
                                filename: obj.FILENAME,
                                path: './upload_dir/' + obj.FILENAME
                            })
                        }
                    }

                    pool.connectionRelease(connection);


                    let result = transporter.sendMail({
                        from: transporter.options.auth.user,
                        to: emailTo,
                        subject: subject,
                        text: 'Автоматически сгенерированное сообщение по результатм обхода инспектора',
                        html:
                            'Отправка с вложением2',
                        attachments: att
                    })
                    res.status(200).json(result.response);
                }
            }


        }).catch(function (err) {
            pool.connectionRelease(connection);
            res.status(201).json({errmsg: err });
        });
    }).catch(function (err) {
        res.status(201).json({errmsg: err});
    })

});

// Отправка почты
router.post('/mobback/set/send_email_kod', function (req, res, next)
{
    let kod = req.query['kod'];
    let emailTo = req.query['email_to'];
    let result = transporter.sendMail({
        from: transporter.options.auth.user,
        to: emailTo,
        subject: 'Электронное подписание акта теплоснабжения',
        text: 'Автоматически сгенерированное сообщение с кодом подтверждения для подписания акта',
        html:
            '<b>Автоматически сгенерированное сообщение с кодом подтверждения для подписания акта</b><br/> Код для подтверждения: ' + kod

    })
    res.status(200).json(result.response);

});



module.exports = router;

