const pool = require('../../oracledb/pool');
const tman_task_dao = require('../../data/tman_task_dao');

// let selectInspectors = () => {
//     return new Promise(function (resolve, reject) {
//         pool.getConnection().then(function (connection) {
//             tman_task_dao.select_insp(connection).then(function (json) {
//                 pool.connectionRelease(connection);
//                 resolve(json);
//             }).catch(function (error) {
//                 pool.connectionRelease(connection);
//                 reject(error);
//             });
//         }).catch(function (errCC) {
//             reject(errCC);
//         });
//     });
// };

let selectWorkInspector = (dateS, datePo) => {
    return new Promise(function (resolve, reject) {
        pool.getConnection().then(function (connection) {
            tman_task_dao.select_work_inspector(connection, dateS, datePo).then(function (json) {
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
        pool.getConnection().then(function (connection) {
            tman_task_dao.select_list_tasks(connection, date).then(function (json) {
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
        pool.getConnection().then(function (connection) {
            tman_task_dao.check_marshrut(connection, date, id_insp).then(function (json) {
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
        pool.getConnection().then(function (connection) {
            tman_task_dao.get_objects(connection, text).then(function (result) {
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
        pool.getConnection().then(function (connection) {
            tman_task_dao.get_list_obj(connection, idTask).then(function (result) {
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
        pool.getConnection().then(function (connection) {
            tman_task_dao.get_contacts(connection, kodp).then(function (result) {
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




async function get_search_event(connection, date, str) {
    return new Promise(function (resolve, reject) {
        // pool.getConnection().then(function (connection) {
        tman_task_dao.select_search_event(connection, date, str).then(function (result) {
            resolve(result);
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        });
    });
};











module.exports = {
    //selectInspectors,
    //deleteFile,
    //add_blob,
    //upload_name,
    get_search_event,
    //deleteTask,
    //get_file,
    //get_task_files,
    //get_history,
    selectTasks,
    getObj,
    //get_obj_asuse,
    //sendTasks: sendTasks,
    selectWorkInspector,
    getListObj,
    getContacts,
    checkMarshrut
    //getTaskInspector,
    //get_list_dogovors_lookup
    //get_list_obj_lookup,
    //get_list_payers_lookup
};
