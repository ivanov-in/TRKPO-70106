const forge = require('node-forge');

let decriptValue = (enval_b64, front_pri_key_b64) => {
    return new Promise(function (resolve, reject) {
        let front_pri_key;
        try {
            front_pri_key = forge.pki.privateKeyFromPem(front_pri_key_b64);
        } catch (e) {
            reject("RSA {3B70EF8A-C4DC-4EEF-900B-F590918273A9}");
        }
        let deval;
        try {
            const eval = forge.util.decode64(enval_b64);
            deval = front_pri_key.decrypt(eval);
        } catch (e) {
            reject("RSA {35184BA1-01BC-4F23-8257-3AE2B5E7CD8B}");
        }
        resolve(deval);
    });
};

module.exports = {decriptValue: decriptValue};
