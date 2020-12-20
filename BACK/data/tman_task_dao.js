const pool = require("../oracledb/pool");
let oracledb = require('oracledb');

async function select_insp(connection) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS_ADMIN.man_tasks_page.select_insp(:cur);
       END;`,
            {
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonInsp = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonInsp.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonInsp;
}

async function select_work_inspector(connection, dateS, datePo) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS_ADMIN.man_tasks_page.select_work_inspectors_list(:p_date_s,:p_date_po,:cur);
       END;`,
            {
                p_date_s: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: dateS},
                p_date_po: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: datePo},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonInsp = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonInsp.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonInsp;
}

let send_marshrut = (connection, date, id_insp) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS.mis_web_w.send_marshrut(:pdat,
                          :p_id_inspector,
                          :po_cnt_sended_task,
                          :status,
                          :errmsg); END;`,
            {
                pdat: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: date},
                p_id_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: parseInt(id_insp)},
                po_cnt_sended_task: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                errmsg: {type: oracledb.VARCHAR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            }).then(function (result) {
            resolve(result);
        })
    });
};

let check_marshrut = (connection, date, id_insp) => {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS.mis_web_w.check_marshrut(:pdat,
                          :p_id_inspector,
                          :status,
                          :errmsg); END;`,
            {
                pdat: {type: oracledb.STRING, dir: oracledb.BIND_IN, val: date},
                p_id_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: parseInt(id_insp)},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                errmsg: {type: oracledb.VARCHAR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            }).then(function (result) {
            resolve(result);
        })
    });
};

async function select_list_tasks(connection, date) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS_ADMIN.man_tasks_page.select_list_tasks(:p_date, :cur);
       END;`,
            {
                p_date: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: date},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;

}

let add_task = (connection, adr, city, street, house, nd, purpose, prim, ttime, id_ins, puser, lat, lan, s_zulu, b_zulu, status) => {
    return new Promise(function (resolve, reject) {

        connection.execute(`BEGIN MIS.mis_web_w.add_task(
                     :p_adr,
                     :p_city,
                     :p_street,
                     :p_house,
                     :p_nd,
                     :p_purpose,
                     :p_prim,
                     :p_ttime,
                     :p_id_inspector,
                     :p_puser,
                     :p_lat,
                     :p_lan,
                     :p_schema_zulu,
                     :p_border_zulu,
                     :po_id_task,
                     :status,
                     :errmsg); END;`,
            {
                p_adr: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: adr},
                p_city: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: city},
                p_street: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: street},
                p_house: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: house},
                p_nd: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nd},
                p_purpose: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: purpose},
                p_prim: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: prim},
                p_ttime: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: ttime},
                p_id_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_ins},
                p_puser: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: puser},
                p_lat: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: lat},
                p_lan: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: lan},
                p_schema_zulu: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: s_zulu},
                p_border_zulu: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: b_zulu},
                po_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_INOUT, val: status},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            }).then(function (result) {
            connection.commit().then(_ => {
                resolve(result);
            })
        }).catch(function (err) {
            reject(err);
        })
    })
};


async function get_task(connection, id_task) {
    let jsonTask = [];

    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_task(:pid_task, :cur);
       END;`,
            {
                pid_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_task},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }

    return jsonTask;

}

async function get_list_obj(connection, id_task) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_list_obj(:pid_task, :cur);
       END;`,
            {
                pid_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: parseInt(id_task)},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;
}

async function get_objects(connection, text) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_objects(:psearch_string, :cur);
       END;`,
            {
                psearch_string: {type: oracledb.STRING, dir: oracledb.BIND_IN, val: text},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;

}

async function get_contacts(connection, kodp) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_contacts(:pkodp, :cur);
       END;`,
            {
                pkodp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: parseInt(kodp)},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;
}

async function get_list_task_insp(connection, date, id, puser) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_list_tasks(:pdate,
                                      :pid_inspector,
                                      :p_puser,
                                      :cur);
       END;`,
            {
                pdate: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: date},
                pid_inspector: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: id},
                p_puser: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: puser},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;
}

async function get_list_obj_look_up(connection, pid_task, pkodp, pkod_dog) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_list_obj(:pid_task,
                                      :pkodp,
                                      :pkod_dog,
                                      :cur);
       END;`,
            {
                pid_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: pid_task},
                pkodp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: pkodp},
                pkod_dog: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: pkod_dog},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;
}

async function get_list_payers_look_up(connection, pid_task, pkod_numobj, pkod_dog) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_list_payers(:pid_task,
                                      :pkod_numobj,
                                      :pkod_dog,
                                      :cur);
       END;`,
            {
                pid_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: pid_task},
                pkod_numobj: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: pkod_numobj},
                pkod_dog: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: pkod_dog},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;
}

async function get_list_dogovors_look_up(connection, pid_task, pkod_numobj, pkodp) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_list_dogovors(:pid_task,
                                      :pkod_numobj,
                                      :pkodp,
                                      :cur);
       END;`,
            {
                pid_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: pid_task},
                pkod_numobj: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: pkod_numobj},
                pkodp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: pkodp},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;
}

async function get_dog_payers_list(connection, string) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_dog_payers(:psearch_string,
                                      :cur);
       END;`,
            {
                psearch_string: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: string},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;
}

async function get_obj_asuse_list(connection, string) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_objects(:psearch_string,
                                      :cur);
       END;`,
            {
                psearch_string: {type: oracledb.STRING, dir: oracledb.BIND_IN, val: string},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;
}

async function get_history_list(connection, dateS, datePo, koddog, kodobj, kodp, insp) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_history(:pid_inspector,
                        :pkod_obj,
                        :pkodp,
                        :pkod_dog,
                        :pdate_from,
                        :pdate_to,
                        :p_sort_col,
                        :p_sort_order,
                        :cur);
       END;`,
            {
                pid_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: insp},
                pkod_obj: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kodobj},
                pkodp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kodp},
                pkod_dog: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: koddog},
                pdate_from: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: dateS},
                pdate_to: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: datePo},
                p_sort_col: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: ''},
                p_sort_order: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: ''},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;
}

async function get_task_files_name(connection, id) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS.mis_web_r.get_task_files(:pid_task,
                        :cur);
       END;`,
            {
                pid_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: parseInt(id)},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;
}

async function get_file_(connection, id) {
    return new Promise(function (resolve, reject) {
        oracledb.fetchAsBuffer = [oracledb.BLOB]
        connection.execute(`BEGIN
         MIS.mis_web_r.get_file(:pid_file,
                        :data);
       END;`,
            {
                pid_file: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: parseInt(id)},
                data: {type: oracledb.BLOB, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            }).then(result => {
            resolve(result.outBinds.data)
        }).catch(err => {
            reject(err)
        });
    });
}

async function get_podpisant(connection, p_kodp) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN MIS.mis_web_r.get_podpisant(
                        :p_kodp,
                        :cur);
                        END;`,
            {
                p_kodp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: p_kodp},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonPodpisant = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonPodpisant.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonPodpisant;
}

async function delete_task(connection, id) {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS.mis_web_w.delete_task(
                        :p_id_task,
                        :status,
                        :errmsg); END;`,
            {
                p_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: parseInt(id)},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT},
            },
            {
                resultSet: true
            }).then(function (result) {
            connection.commit().then(_ => {
                resolve(result);
            })
        }).catch(function (err) {
            reject(err);
        })
    });

}

async function delete_file(connection, id_task, id_file) {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS.mis_web_w.delete_file(
                        :p_id_task,
                        :p_id_file,
                        :status,
                        :errmsg); END;`,
            {
                p_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: parseInt(id_task)},
                p_id_file: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: parseInt(id_file)},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT},
            },
            {
                resultSet: true
            }).then(function (result) {
            connection.commit().then(_ => {
                resolve(result);
            })
        }).catch(function (err) {
            reject(err);
        })
    });

}

async function select_search_event(connection, pdate, pstr) {
    oracledb.outFormat = oracledb.OBJECT;
    let result;
    try {
        result = await connection.execute(`BEGIN
         MIS_ADMIN.adm_events_page.get_searching_events(:p_date,
         :pstring,
                        :cur);
       END;`,
            {
                p_date: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: pdate},
                pstring: {type: oracledb.VARCHAR, dir: oracledb.BIND_IN, val: pstr},
                cur: {type: oracledb.DB_TYPE_CURSOR, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });

    } catch (e) {
        throw e.message;
    }
    const resultSet = result.outBinds.cur;
    let row;
    let jsonTask = [];
    try {
        while ((row = await resultSet.getRow())) {
            jsonTask.push(row);
        }
    } catch (e) {
        throw e.message;
    }
    return jsonTask;
}

async function upload_file_name(connection, id, name, signed, paper) {
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS.mis_web_w.add_file(
                        :p_id_task,
                        :p_filename,
                        :p_is_signed,
                        :p_paper,
                        :po_id_file,
                        :status,
                        :errmsg); END;`,
            {
                p_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: parseInt(id)},
                p_filename: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: name},
                p_is_signed: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: parseInt(signed)},
                p_paper: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: parseInt(paper)},
                po_id_file: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT},
            },
            {
                resultSet: true
            }).then(function (result) {

            resolve(result);
        }).catch(function (err) {
            reject(err);
        })
    });
}

async function add_blob_data(connection, id, str) {

    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS.mis_web_w.add_file_blob(
                        :p_id_file,
                        :p_filedata,
                        :status,
                        :errmsg); END;`,
            {
                p_id_file: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id},
                p_filedata: {type: oracledb.BLOB, dir: oracledb.BIND_IN, val: str},

                status: {type: oracledb.NUMBER, dir: oracledb.BIND_OUT},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT},
            },
            {
                resultSet: true
            }).then(function (result) {
            connection.commit().then(_ => {
                resolve(result);
            }).catch(function (err) {
                reject(err);
            })
        }).catch(function (err) {
            reject(err);
        })
    })
}

let change_task = (
    connection,
    id_task,
    adr_ya,
    city,
    street,
    house,
    nd,
    purpose,
    prim,
    time,
    id_insp,
    puser,
    kod_obj,
    kod_dog,
    kodp,
    kod_numobj,
    fio_contact,
    email_contact,
    tel_contact,
    dol,
    status,
    lat,
    lan,
    s_zulu,
    b_zulu,
    statusOut,
    kod_emp
) => {
    if (kod_emp === '') {
        kod_emp = null
    }
    if (lat === null) {
        lat = ''
    }
    if (lan === null) {
        lan = ''
    }
    return new Promise(function (resolve, reject) {
        connection.execute(`BEGIN MIS.mis_web_w.update_task(
                        :p_id_task,
                        :p_adr_ya,
                        :p_city,
                        :p_street,
                        :p_house,
                        :p_nd,
                        :p_purpose,
                        :p_prim,
                        :p_ttime,
                        :p_id_inspector,
                        :p_puser,
                        :p_kod_obj,
                        :p_kod_dog,
                        :p_kodp,
                        :p_kod_numobj,
                        :p_fio_contact,
                        :p_email_contact,
                        :p_tel_contact,
                        :p_namedol,
                        :p_status,
                        :p_lat,
                        :p_lan,
                        :p_schema_zulu,
                        :p_border_zulu,
                        :p_kod_emp,
                        :status,
                        :errmsg); END;`,
            {
                p_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_task},
                p_adr_ya: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: adr_ya},
                p_city: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: city},
                p_street: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: street},
                p_house: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: ''},
                p_nd: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nd},
                p_purpose: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: purpose},
                p_prim: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: prim},
                p_ttime: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: time},
                p_id_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_insp},
                p_puser: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: puser},
                p_kod_obj: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_obj},
                p_kod_dog: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_dog},
                p_kodp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kodp},
                p_kod_numobj: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_numobj},
                p_fio_contact: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: fio_contact},
                p_email_contact: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: email_contact},
                p_tel_contact: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: tel_contact},
                p_namedol: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: ''},
                p_status: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: 0},
                p_lat: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: lat.toString()},
                p_lan: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: lan.toString()},
                p_schema_zulu: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: s_zulu},
                p_border_zulu: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: b_zulu},
                p_kod_emp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_emp},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_INOUT, val: statusOut},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT},
            },
            {
                resultSet: true
            }).then(function (result) {
            connection.commit().then(_ => {
                resolve(result);
            }).catch(err => {
                reject(err)
            })
        }).catch(error => {
            reject(error)
        })
    });
};




module.exports = {
    select_insp,
    add_blob_data: add_blob_data,
    upload_file_name,
    select_search_event,
    delete_task,
    delete_file,
    get_file_,
    get_task_files_name,
    get_history_list,
    get_obj_asuse_list,
    select_list_tasks,
    add_task,
    get_objects,
    get_task,
    get_dog_payers_list,
    get_list_obj,
    send_marshrut: send_marshrut,
    select_work_inspector,
    change_task,
    get_contacts,
    check_marshrut,
    get_list_task_insp,
    get_list_dogovors_look_up,
    get_list_obj_look_up,
    get_list_payers_look_up,
    get_podpisant
};
