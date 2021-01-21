const {conOra: poolOra} = require("./db_pool");
let oracledb = require('oracledb');


// данные по логину (id инспектора, значение сиквенса)
async function getInspectorDataDal(connection, puser, password) {
    let result;
    let jsonObj = [];
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_inspector_data(:p_puser,:p_password,:cur, :errmsg);
       END;`,
            {
                p_puser: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: puser},
                p_password: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: password},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT},
                errmsg: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true

            });
    } catch (e) {
        console.log(e.message)
        jsonObj.push({"errmsg" : e.message});
        return jsonObj;
    }
    let msg = result.outBinds.errmsg;
    if (msg != null) {
        jsonObj.push({"errmsg" : msg})

        return jsonObj
    }
    const resultSet = result.outBinds.cur;
    let row;

    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

// список объектов  (с полями номер договора и наименование объекта),
// найденных по переданной строке в адресе объекта или наименовании абонента
//  для случая ручного поиска объекта в поисковике
async function getObjectsDal(connection, searchString) {

    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_objects(:psearch_string,:cur);
       END;`,
            {
                psearch_string: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: searchString},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonObj = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getTasksDal(connection, date, id_inspector, task_status) {
    let result;
    let jsonObj = [];
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_tasks(:pdate, :pid_inspector, :pi_task_status, :cur);
       END;`,
            {
                pdate: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: date},
                pid_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_inspector},
                pi_task_status: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: task_status? task_status : null},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
        jsonObj.push(e.message);

    }
    const resultSet = result.outBinds.cur;
    let row;

    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getTasksFilesDal(connection, date, id_inspector, task_status) {
    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_tasks_files(:pdate, :pid_inspector,:pi_task_status, :cur);
       END;`,
            {
                pdate: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: date},
                pid_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_inspector},
                pi_task_status: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: task_status? task_status : null},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonObj = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getTasksFilesHistDal(connection, date, id_inspector, task_status) {
    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_tasks_files_hist(:pdate, :pid_inspector,:pi_task_status, :cur);
       END;`,
            {
                pdate: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: date},
                pid_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_inspector},
                pi_task_status: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: task_status? task_status : null},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonObj = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getFileDal(connection, id_file) {
    return new Promise(function (resolve, reject) {
        oracledb.fetchAsBuffer = [oracledb.BLOB]
        connection.execute(`BEGIN
         MIS_MOBILE_R.get_file(:pid_file, :data);
       END;`,
            {
                pid_file: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: parseInt(id_file)},
                data: {type: oracledb.BLOB, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            }).then(result => {
            resolve(result.outBinds.data)
        }).catch(err => {
            reject(err)
        });
    })
}


async function getPurposeActsDal(connection) {

    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_purpose_acts(:cur);
       END;`,
            {
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonObj = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getActsFieldsDal(connection, date, id_inspector, task_status) {

    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_act_fields(:pdate, :pid_inspector,:pi_task_status, :cur);
       END;`,
            {
                pdate: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: date},
                pid_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_inspector},
                pi_task_status: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: task_status? task_status : null},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonObj = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getActsFieldsDopDal(connection, date, id_inspector, task_status) {

    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_act_fields_dop(:pdate, :pid_inspector,:pi_task_status, :cur);
       END;`,
            {
                pdate: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: date},
                pid_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_inspector},
                pi_task_status: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: task_status? task_status : null},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonObj = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getContactsDal(connection, kodp) {

    let result;
    try {oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_contacts(:pkodp, :cur);
       END;`,
            {
                pkodp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kodp},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonObj = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getPurposeDal(connection) {

    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_purpose(:cur);
       END;`,
            {
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonObj = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getDogPayersDal(connection, searchString) {

    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_dog_payers(:psearch_string,:cur);
       END;`,
            {
                psearch_string: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: searchString},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonObj = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getHistoryDal(connection, date, id_inspector, task_status) {

    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_history(:pdate, :pid_inspector, :pi_task_status,:cur);
       END;`,
            {
                pdate: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: date},
                pid_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_inspector},
                pi_task_status: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: task_status? task_status : null},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonObj = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getDogDataDal(connection, date, id_inspector, task_status) {
    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_dog_data(:pdate, :pid_inspector, :pi_task_status, :cur);
       END;`,
            {
                pdate: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: date},
                pid_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_inspector},
                pi_task_status: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: task_status? task_status : null},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonObj = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getDogObjDataDal(connection, date, id_inspector, task_status) {
    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_dog_obj_data(:pdate, :pid_inspector, :pi_task_status,:cur);
       END;`,
            {
                pdate: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: date},
                pid_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_inspector},
                pi_task_status: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: task_status? task_status : null},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonObj = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getDogTuDataDal(connection, date, id_inspector, task_status) {

    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_dog_tu_data(:pdate, :pid_inspector,:pi_task_status, :cur);
       END;`,
            {
                pdate: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: date},
                pid_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_inspector},
                pi_task_status: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: task_status? task_status : null},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonObj = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getDogUuDataDal(connection, date, id_inspector, task_status) {

    let result;
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_dog_uu_data(:pdate, :pid_inspector, :pi_task_status,:cur);
       END;`,
            {
                pdate: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: date},
                pid_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_inspector},
                pi_task_status: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: task_status? task_status : null},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonObj = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}


async function getDogUuSiDataDal(connection, date, id_inspector, task_status) {

    let result;
    let jsonObj = [];
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_dog_uu_si_data(:pdate, :pid_inspector, :pi_task_status,:cur);
       END;`,
            {
                pdate: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: date},
                pid_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_inspector},
                pi_task_status: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: task_status? task_status : null},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
        jsonObj.push({"errmsg" : e.message});
        return jsonObj;
    }
    const resultSet = result.outBinds.cur;
    let row;

    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
        console.log(e.message)
        jsonObj.push({"errmsg" : e.message});
        return jsonObj;
    }
    return jsonObj;
}

async function getDogDataOnlineDal(connection, kod_dog) {

    let result;
    let jsonObj = [];
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_dog_data_online(:pi_kod_dog,:cur);
       END;`,
            {
                pi_kod_dog: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_dog},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
        jsonObj.push({"errmsg" : e.message});
        return jsonObj;
    }
    const resultSet = result.outBinds.cur;
    let row;

    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
        console.log(e.message)
        jsonObj.push({"errmsg" : e.message});
        return jsonObj;
    }
    return jsonObj;
}

async function getDogObjDataOnlineDal(connection, kod_dog) {

    let result;
    let jsonObj = [];
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_dog_obj_data_online(:pi_kod_dog,:cur);
       END;`,
            {
                pi_kod_dog: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_dog},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
        jsonObj.push({"errmsg" : e.message});
        return jsonObj;
    }
    const resultSet = result.outBinds.cur;
    let row;

    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
        console.log(e.message)
        jsonObj.push({"errmsg" : e.message});
        return jsonObj;
    }
    return jsonObj;
}

async function getDogTuDataOnlineDal(connection, kod_dog) {

    let result;
    let jsonObj = [];
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_dog_tu_data_online(:pi_kod_dog,:cur);
       END;`,
            {
                pi_kod_dog: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_dog},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
        jsonObj.push({"errmsg" : e.message});
        return jsonObj;
    }
    const resultSet = result.outBinds.cur;
    let row;

    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
        console.log(e.message)
        jsonObj.push({"errmsg" : e.message});
        return jsonObj;
    }
    return jsonObj;
}

async function getDogUuDataOnlineDal(connection, kod_dog) {

    let result;
    let jsonObj = [];
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_dog_uu_data_online(:pi_kod_dog,:cur);
       END;`,
            {
                pi_kod_dog: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_dog},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
        jsonObj.push({"errmsg" : e.message});
        return jsonObj;
    }
    const resultSet = result.outBinds.cur;
    let row;

    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
        console.log(e.message)
        jsonObj.push({"errmsg" : e.message});
        return jsonObj;
    }
    return jsonObj;
}

async function getDogUuSiDataOnlineDal(connection, kod_dog) {

    let result;
    let jsonObj = [];
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_dog_uu_si_data_online(:pi_kod_dog,:cur);
       END;`,
            {
                pi_kod_dog: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_dog},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
        jsonObj.push({"errmsg" : e.message});
        return jsonObj;
    }
    const resultSet = result.outBinds.cur;
    let row;

    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
        console.log(e.message)
        jsonObj.push({"errmsg" : e.message});
        return jsonObj;
    }
    return jsonObj;
}

async function getPodpisantDal(connection, date, id_inspector, task_status) {

    let result;
    let jsonObj = [];
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_podpisant(:pdate, :pid_inspector,:pi_task_status, :cur);
       END;`,
            {
                pdate: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: date},
                pid_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_inspector},
                pi_task_status: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: task_status? task_status : null},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
        jsonObj.push(e.message);
        return jsonObj;
    }
    const resultSet = result.outBinds.cur;
    let row;

    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getCountUpdatesDal(connection, date, id_inspector) {

    let result;
    let jsonObj = [];
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_count_updates(:pdate, :pid_inspector, :cur);
       END;`,
            {
                pdate: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: date},
                pid_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_inspector},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
        jsonObj.push(e.message);
        return jsonObj;
    }
    const resultSet = result.outBinds.cur;
    let row;

    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
    }
    return jsonObj;
}

async function getSignedActDal(connection, id_task, id_act, npp, filename) {
    let result;
    let jsonObj = [];
    try {
        oracledb.outFormat = oracledb.OBJECT;
        result = await connection.execute(`BEGIN
         MIS_MOBILE_R.get_signed_act(:pi_id_task, :pi_id_act, :pi_npp, :pi_filename, :cur);
       END;`,
            {
                pi_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_task},
                pi_id_act: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_act},
                pi_npp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: npp},
                pi_filename: {type: oracledb.DB_TYPE_VARCHAR, dir: oracledb.BIND_IN, val: filename},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
        jsonObj.push({"errmsg" : e.message});
        return jsonObj;

    }
    const resultSet = result.outBinds.cur;
    let row;

    try {
        while ((row = await resultSet.getRow())) {
            jsonObj.push(row);
        }
    } catch (e) {
        jsonObj.push({"errmsg" : e.message});
    }
    return jsonObj;
}

module.exports = {
    getObjectsDal,
    getTasksDal,
    getTasksFilesDal,
    getTasksFilesHistDal,
    getPurposeActsDal,
    getActsFieldsDal,
    getActsFieldsDopDal,
    getContactsDal,
    getPurposeDal,
    getDogPayersDal,
    getHistoryDal,
    getDogDataDal,
    getDogObjDataDal,
    getDogTuDataDal,
    getDogUuDataDal,
    getDogUuSiDataDal,
    getFileDal,
    getInspectorDataDal,
    getPodpisantDal,
    getCountUpdatesDal,
    getSignedActDal,
    getDogDataOnlineDal,
    getDogObjDataOnlineDal,
    getDogTuDataOnlineDal,
    getDogUuDataOnlineDal,
    getDogUuSiDataOnlineDal,
};
