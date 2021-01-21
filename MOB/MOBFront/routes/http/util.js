
const getIp = (req) => {
    return req.connection.remoteAddress.toString().replace("::ffff:", "")
}

module.exports = {getIp};