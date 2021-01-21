const DATA = require('../data/getDataDal');
let oracledb = require('oracledb');
const pool = require('../data/db_pool');

async function getObjects(searchString) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getObjectsDal(connection, searchString).then(function (result) {
            pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })

}

async function getTasks(pdate, pid_inspector, pi_task_status) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getTasksDal(connection, pdate, pid_inspector, pi_task_status).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })
}

async function getTasksFiles(pdate, pid_inspector, pi_task_status) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getTasksFilesDal(connection, pdate, pid_inspector, pi_task_status).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })

}

async function getTasksFilesHist(pdate, pid_inspector, pi_task_status) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getTasksFilesHistDal(connection, pdate, pid_inspector, pi_task_status).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })

}

async function getFile(id, connection) {
    return new Promise( function (resolve, reject) {
            DATA.getFileDal(connection, id).then(function (result) {
                resolve(result);
            }).catch(function (error) {
                reject(error);
            });


    })
}

async function getPurposeActs() {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getPurposeActsDal(connection).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })

}

async function getActsFields(pdate, pid_inspector, pi_task_status) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getActsFieldsDal(connection, pdate, pid_inspector, pi_task_status).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })

}

async function getActsFieldsDop(date, id_inspector, pi_task_status) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getActsFieldsDopDal(connection, date, id_inspector, pi_task_status).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })

}

async function getContacts(kodp) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getContactsDal(connection, kodp).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })

}

async function getPurpose() {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getPurposeDal(connection).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })

}

async function getDogPayers(searchString) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getDogPayersDal(connection,searchString).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })

}

async function getHistory(pdate, pid_inspector, pi_task_status) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getHistoryDal(connection,pdate, pid_inspector, pi_task_status).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })

}

async function getDogData(date, id_inspector, pi_task_status) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getDogDataDal(connection, date, id_inspector, pi_task_status).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })

}

async function getDogObjData(date, id_inspector, pi_task_status) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getDogObjDataDal(connection, date, id_inspector, pi_task_status).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })

}

async function getDogTuData(date, id_inspector, pi_task_status) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getDogTuDataDal(connection, date, id_inspector, pi_task_status).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })

}

async function getDogUuData(date, id_inspector, pi_task_status) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.getDogUuDataDal(connection, date, id_inspector, pi_task_status).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })

}


module.exports = {
    getObjects,
    getTasks,
    getTasksFiles,
    getTasksFilesHist,
    getPurposeActs,
    getActsFields,
    getActsFieldsDop,
    getContacts,
    getPurpose,
    getDogPayers,
    getHistory,
    getDogData,
    getDogObjData,
    getDogTuData,
    getDogUuData,
    getFile
};
