let oracledb = require('oracledb');
const pool = require('../../data/db_pool');
const taman_task_dao = require('../../data/tman_task_dao');

let selectInspectors = () => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.select_insp(connection).then(function (json) {
                pool.connectionRelease(connection);
                resolve(json);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};

let selectWorkInspector = (dateS, datePo) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.select_work_inspector(connection, dateS, datePo).then(function (json) {
                pool.connectionRelease(connection);
                resolve(json);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};

let selectTasks = (date) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.select_list_tasks(connection, date).then(function (json) {
                pool.connectionRelease(connection);
                resolve(json);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });

        }).catch(function (error) {
            reject(error);
        });
    });
};

let sendTasks = (date, id_insp) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.send_marshrut(connection, date, id_insp).then(function (json) {
                pool.connectionRelease(connection);
                resolve(json);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};

let checkMarshrut = (date, id_insp) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.check_marshrut(connection, date, id_insp).then(function (json) {
                pool.connectionRelease(connection);
                resolve(json);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};

let getObj = (text) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.get_objects(connection, text).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};

let getListObj = (idTask) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.get_list_obj(connection, idTask).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};

let getContacts = (kodp) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.get_contacts(connection, kodp).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};

let getTaskInspector = (date, id_insp, puser) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.get_list_task_insp(connection, date, id_insp, puser).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};

let get_list_obj_lookup = (pid_task, pkodp, pkod_dog) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.get_list_obj_look_up(connection, pid_task, pkodp, pkod_dog).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};
let get_list_payers_lookup = (pid_task, pkod_numobj, pkod_dog) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.get_list_payers_look_up(connection, pid_task, pkod_numobj, pkod_dog).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};
let get_list_dogovors_lookup = (pid_task, pkod_numobj, pkodp) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.get_list_dogovors_look_up(connection, pid_task, pkod_numobj, pkodp).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};

let get_dog_payers = (string) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.get_dog_payers_list(connection, string).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};


let get_obj_asuse = (string) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.get_obj_asuse_list(connection, string).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};
let get_history = (dateS, datePo, koddog, kodobj, kodp, insp) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.get_history_list(connection, dateS, datePo, koddog, kodobj, kodp, insp).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};

let get_task_files = (id) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.get_task_files_name(connection, id).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};

async function get_file(connection, id) {
    return new Promise(function (resolve, reject) {
        taman_task_dao.get_file_(connection, id).then(function (result) {
            resolve(result);
        }).catch(function (error) {

            reject(error);
        });

    });
};

async function get_search_event(connection, date, str) {
    return new Promise(function (resolve, reject) {
        // oracledb.getConnection().then(function (connection) {
        taman_task_dao.select_search_event(connection, date, str).then(function (result) {
            resolve(result);
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        });
    });
};

let deleteTask = (id) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.delete_task(connection, id).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};

let deleteFile = (id_task, id_file) => {
    return new Promise(function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            taman_task_dao.delete_file(connection, id_task, id_file).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            reject(error);
        });
    });
};


async function upload_name(connection, id, name, signed, paper) {
    return new Promise(function (resolve, reject) {
        taman_task_dao.upload_file_name(connection, id, name, signed, paper).then(function (result) {
            resolve(result);
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        });
    });
};

async function add_blob(connection, id, str) {
    return new Promise(function (resolve, reject) {
        taman_task_dao.add_blob_data(connection, id, str).then(function (result) {
            resolve(result);
        }).catch(function (error) {
            reject(error);
        });
    });
};
module.exports = {
    selectInspectors,
    deleteFile,
    add_blob,
    upload_name,
    get_search_event,
    deleteTask,
    get_file,
    get_task_files,
    get_history,
    selectTasks,
    getObj,
    get_obj_asuse,
    sendTasks,
    selectWorkInspector,
    get_dog_payers,
    getListObj,
    getContacts,
    checkMarshrut,
    getTaskInspector,
    get_list_dogovors_lookup,
    get_list_obj_lookup,
    get_list_payers_lookup
};
