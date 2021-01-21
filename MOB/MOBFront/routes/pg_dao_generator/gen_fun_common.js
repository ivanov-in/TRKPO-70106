const UUID = require('uuid/v4');

function funGetSetWhere(f_json, baseBlock, split) {
    baseBlock += split;
    var columns = baseBlock;
    var temp = baseBlock.match(/@/g);
    if(!(temp != null)){
        return baseBlock.substring(0,columns.length-1);
    }
    var count = temp.length;
    for(var i = 0; i < count; i++){
        var setValues = baseBlock.substring(baseBlock.indexOf("@") + 1)
        setValues = setValues.substring(0, setValues.indexOf(split));
        for (key in f_json.params) {
            if (f_json.params[key].name.toLowerCase() == (setValues.toLowerCase())) {
                columns = columns.replace('@' + setValues, '$' + (parseInt(key)+1));
                columns = columns.replace(setValues, setValues.toLowerCase());
                baseBlock = baseBlock.substring(baseBlock.indexOf('@' + setValues) + setValues.length + 3);
            }
            if (baseBlock.length == 0)
                break;
        }
    }
    return columns.substring(0,columns.length-1);
}

function funGenValues(f_json) {
    var params = '';
    if (f_json.generate_key) {
        params += 'var p_key =  UUID();' + '\n';
    }
    if(f_json.generate_dtc){
        params += '\t\t' + 'var p_dtc = new Date().toLocaleString();' + '\n';
    }
    return params;
}

function funParams(json) {
    var params = '';
    for (key in json.params) {
        if (key != 0)
            params += ', ';
        params += 'p_' + json.params[key].name.toLowerCase();
    }
    return params;
}

function funSqlColumns(json, f_json) {
    var columns = '';
    if(f_json.columns != undefined){
        for (key in f_json.columns) {
            if (key != 0) {
                columns += ', ';
            }
            columns += '"' + f_json.columns[key].toLowerCase() + '"';

        }
    }
    else {
        for (key in json.columns) {
            if (key != 0) {
                columns += ', ';
            }
            columns += '"' + json.columns[key].name.toLowerCase() + '"';
        }
    }
    return columns;
}

module.exports = {funGetSetWhere, funGenValues, funParams, funSqlColumns};