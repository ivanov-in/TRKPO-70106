const express = require('express');
const router = express.Router();
const {Client} = require('pg');
let common = require('./http_public/common');
let db_ini = require('./database_init/database_ini.js');
let db_obj = require('./database_init/database_obj.js');
let tticketforticket_jobs = require('./database_jobs/tticketforticket_jobs');
let tiperrors_jobs = require('./database_jobs/tiperrors_jobs');
const log = require('./log/util');
const forge = require('node-forge');
const fs = require('fs');
const path = require('path');


async function arg_exe() {
    return new Promise(async function (resolve, reject) {
        var result = [];


        function dbreinit() {
            for (item in process.argv) {
                if (process.argv[item] == 'dbreinit') {
                    const database = require('./database_init/database_inst.js');
                    var db_name = db_ini.db_name;
                    console.log('Start Initializing database module:' + db_name + ' ' + log.nowTime());
                    database.run(function () {
                        var time = log.nowTime();
                        console.log('End Initializing database module:' + db_name + ' ' + log.nowTime());
                        console.log('Start check tables structure:' + db_name + ' ' + log.nowTime());
                        const con = new Client({
                            connectionString: db_ini.connectionString
                        });
                        con.connect();
                        db_obj.run(con, db_ini.def_schema).then(function (error) {
                            console.log('End check tables structure:' + db_name + ' ' + log.nowTime());
                            con.end();
                            daoreinit();
                        }).catch(function (err) {
                            console.log('Error check tables structure:' + error + ' ' + log.nowTime());
                            con.end();
                        });
                    });
                    result.push(process.argv[item])
                }
            }
        }

        function daoreinit() {
            return new Promise(function (resolve, reject) {
                var daoreinit = 'daoreinit';
                var contain = false;
                for (item in process.argv) {
                    if (process.argv[item] == daoreinit) {
                        contain = true;
                    }
                }
                if (contain) {
                    result.push(daoreinit)
                    var db_name = db_ini.db_name;
                    console.log('Start rebuild dao module:' + db_name + ' ' + log.nowTime());
                    const database_dao = require('./database_init/database_dao.js');
                    database_dao.run().then(function () {
                        console.log('End rebuild dao module:' + db_name + ' ' + log.nowTime());
                        resolve(daoreinit);
                    }).catch(function () {
                        console.log('Error rebuild dao module:' + db_name + ' ' + log.nowTime());
                        reject('{14F4309B-31AC-46DF-9351-DEFB47162457}');
                    });
                } else {
                    resolve('');
                }
            });
        }

        dbreinit();

        resolve(result);
    });
}

async function startup() {
    // //CRIPTO
    //Генерирует ключи
    // var keys = forge.pki.rsa.generateKeyPair({bits: 1024, e: 0x10001});
    // keys = forge.pki.rsa.generateKeyPair({bits: 1024, e: 0x10001});
    // //pub key to string
    // var frontPubKeyStr = forge.pki.publicKeyToPem(keys.publicKey, 1000);
    // //priv key to string
    // var frontPrvKeyStr = forge.pki.privateKeyToPem(keys.privateKey, 1000);
    // // pub to file
    //  fs.writeFile('pub.txt', frontPubKeyStr, (err) => {
    //     if (err) throw err;
    //     console.log('It\'s saved!');
    // });
    //  // priv to file
    // fs.writeFile('priv.txt', frontPrvKeyStr, (err) => {
    //     if (err) throw err;
    //     console.log('It\'s saved!');
    // });


    // var pubFilePath = path.join('./routes/keys/', 'pub.txt');
    // var pubFromFile = fs.readFileSync(pubFilePath, 'utf8');
    // var privFilePath = path.join('./routes/keys/', 'priv.txt');
    // var privFromFile = fs.readFileSync(privFilePath, 'utf8');
    // var servPubKey = forge.pki.publicKeyFromPem('-----BEGIN PUBLIC KEY-----MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCru6ylcwV1NVNSU9dKIH1aWi2pVfnBQRCMwbBEMb6FIoscGZfpQFEQRI4mIthOFlEaZmM9E701fQpG+4GyfGCHaqXPAFyJRv7Eft3MgZ37X4iAfq1n3YCWHsYaDGMcmwQx9DZ1NjENzyWuZqPVCXLjKl4HrZzoigV4pS/B9+9BJwIDAQAB-----END PUBLIC KEY-----');
    // //var servPubKey = forge.pki.publicKeyFromPem(pubFromFile);
    // var enText = servPubKey.encrypt(forge.util.encodeUtf8('test'));
    // var enText64 =forge.util.encode64(enText);
    // enText = forge.util.decode64(enText64);
    //
    // var frontPrvKey = forge.pki.privateKeyFromPem('-----BEGIN RSA PRIVATE KEY-----MIICXQIBAAKBgQCru6ylcwV1NVNSU9dKIH1aWi2pVfnBQRCMwbBEMb6FIoscGZfpQFEQRI4mIthOFlEaZmM9E701fQpG+4GyfGCHaqXPAFyJRv7Eft3MgZ37X4iAfq1n3YCWHsYaDGMcmwQx9DZ1NjENzyWuZqPVCXLjKl4HrZzoigV4pS/B9+9BJwIDAQABAoGAS3BM3f0R97PuHysvVqPBDRCyIEHhTdB8Kajn+nzzmV5cR4LCpbcYJRJIc145MMxC4lr52xmxsy5zdk0DY1mZuQpTF0ArBcWsJ+9l6lmafcPy1cK3X8wNYW1K1zKrOLMLmjPYG+bBdUpKdumbLpvhD7icUh0VZrTYd/gHdQ3cogkCQQDdsU0+UXIpcmpUgDgVPBgwbYFw8RqB5dPzx1SwCZzL+HhBHg2JDpS7DFZLIeSBrCGEGjFyKl9axOExbsHy1VQ7AkEAxk8mU0B9LwyFLCwIwatw7phxPlSodJupRb/sv8CzKMr/CaQj/5JkDzOXpq4NYqLhLzb2mLbJOYDpQbsjqpAUBQJACMSaaXyPJ1R/IBhyH2ThEAlEtVLT2Y9dcqbPkqbOrMKG/wxRPLp25271esM8ZmSIbtEaY4mJe+2NeocG5LPLQQJBAIDh9fWLZxCJ4Y84uyQraQUYPBkXajgG5rd3KcXh1k5/x8sbS24RVUhv7nKckgR1+UX3QcM0oIjn2DEpD+ANLYUCQQCB9i8CEORuqF8wEtWErpnXYMjfY3c+uy/2DL0fnm4RLuSJ3cx28cf1/tKHN4blwMwVL5BSPZO1I2Y/IVwp0h2M-----END RSA PRIVATE KEY-----');
    // //var frontPrvKey = forge.pki.privateKeyFromPem(privFromFile);
    // var deText = frontPrvKey.decrypt(enText);
    //
    // //frontPubKeyStr =  forge.util.decode64(frontPubKeyStrB64); //'-----BEGIN PUBLIC KEY-----\r\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCwFooQ7InSgFZr5lQ2Z1jdFpRv\r\nL816pzhtV0JK2w/RnXk/jLJ0BSPy26G0SE3RSKbyTWBGT93hkFEgdoiYOwjfXJgm\r\n7xl395/6qurGH0yvlWTjWFExPWgsb0ktnod78rQE/StugsvMAVsAx9ZCbaKardep\r\nhWzuf/Fb2hdUVRnAxQIDAQAB\r\n-----END PUBLIC KEY-----\r\n';
    // //frontPubKeyStr = '-----BEGIN PUBLIC KEY-----\r\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCwFooQ7InSgFZr5lQ2Z1jdFpRv\r\nL816pzhtV0JK2w/RnXk/jLJ0BSPy26G0SE3RSKbyTWBGT93hkFEgdoiYOwjfXJgm\r\n7xl395/6qurGH0yvlWTjWFExPWgsb0ktnod78rQE/StugsvMAVsAx9ZCbaKardep\r\nhWzuf/Fb2hdUVRnAxQIDAQAB\r\n-----END PUBLIC KEY-----`\r\n';
    //
    //
    // //var servPubKey = forge.pki.publicKeyFromPem(frontPubKeyStr);
    // var servPubKey = forge.pki.publicKeyFromPem(frontPubKeyStr);
    // //var frontPrvKeyStr = forge.pki.privateKeyToPem(keys.privateKey);
    // //var frontPrvKey = forge.pki.privateKeyFromPem(frontPrvKeyStr);
    //
    // var deText = keys.privateKey.decrypt(enText);
    //
    //
    // var mobPubKeyStr = "-----BEGIN PUBLIC KEY-----MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDBW+4CqRdFLz6Khnez+FOfI8DmvaMma9IK5OHQjuGuD2VTtpKkga4Zt3bhgU+SPLeKbaJ61A1LbxOuOMm3HHOOz+cjN8xDuTFFG65tXXNzaCIWg3axEQcrwnQbaUUUhwwxuuhlY6L3KuIHAiEjH6np8SOegKqy7zjOzy1bfqwvuQIDAQAB-----END PUBLIC KEY-----";
    // //mobPubKeyStr = forge.util.decode64(mobPubKeyStr);
    // var mobPubKey = forge.pki.publicKeyFromPem(mobPubKeyStr);
    // enText = mobPubKey.encrypt('test');



    console.log('Starting application "mobia_app_front"');
    try {
        arg_exe().then(function () {

        }).catch(function () {

        })


    } catch (err) {
        console.error(err);
        process.exit(1); // Non-zero failure code
    }

    console.log('Starting application jobs');
    tm_tticketforticket_job();
    tm_tiperrors_job();
    console.log('End starting application jobs');


}

startup();

async function tm_tticketforticket_job() {
    return new Promise(function (resolve, reject){
        setTimeout(() => {
            // перезапуск функции
            tticketforticket_jobs.delete_old();
            tm_tticketforticket_job();
            resolve("OK");
        }, 5000);
    });
}
async function tm_tiperrors_job() {
    return new Promise(function (resolve, reject){
        setTimeout(() => {
            // перезапуск функции
            tiperrors_jobs.delete_old();
            tm_tiperrors_job();
            resolve("OK");
        }, 5000);
    });
}


const newVar = router.get('*', function (req, res, next) {
    if (req.url == "/is_server_online/" || req.url == "/is_server_online" || req.url == "/get_script_frserv/") {
        next();
    } else {
        res.set('Content-Type', 'text/html');
        res.send("Error");
    }
    //res.render('index', { title: 'Express' });
});

router.post('*',function(req, resp, next){
    if (req.url == "/get_ticket_for_ticket/" || req.url == "/get_ticket_for_token/" || req.url == "/get_token/"|| req.url == "/get_data/" || req.url == "/send_answerscript_toserv/") {
        next();
    }
});

router.post('/get_ticket_for_ticket/', common.getTicketForTicket);
router.post('/get_ticket_for_token/', common.getTicketForToken);
router.post('/get_token/', common.getToken);
router.post('/get_data/', common.getData);
router.get('/is_server_online/', common.isServerOnline);
router.get('/get_script_frserv/', common.mobScriptSend);
router.post('/send_answerscript_toserv/', common.mobScriptAnswer);
//router.get('/get_ticket_for_ticket/', common.getTicketForTicket);


/* GET isServerOnline*/

module.exports = router;
