const  nowTime = () => {
    var cd = new Date();
    var time = cd.getHours() + ":" + cd.getMinutes() + ":" + cd.getSeconds();
    return time;
}

module.exports = {nowTime};