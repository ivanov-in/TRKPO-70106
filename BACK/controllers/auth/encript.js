const forge = require('node-forge');

let encriptValue = (ticket, client_key_b64) => {
    return new Promise(function (resolve, reject) {
        if (client_key_b64 === undefined) {
            var keys = forge.pki.rsa.generateKeyPair({bits: 1024, e: 0x10001});
            client_key_b64 = forge.pki.publicKeyToPem(keys.publicKey);
        }
        let client_key;
        try {
            client_key = forge.pki.publicKeyFromPem(client_key_b64);
        } catch (e) {
            reject("RSA {07C7FAC9-719D-48C6-84A8-AA50B129ADB4}");
        }
        let ticket_en;
        try {
            ticket_en = client_key.encrypt(forge.util.encodeUtf8(ticket));
        } catch (e) {
            reject("RSA {7E7235F5-6382-4C54-9CA8-AB58BD64B9A9}");
        }
        resolve(forge.util.encode64(ticket_en));
    });
};

module.exports = {encriptValue: encriptValue};
