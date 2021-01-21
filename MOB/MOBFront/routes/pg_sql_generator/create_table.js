let getScriptTable = (json) => {
    return new Promise(function (resolve, reject) {
        if (json.name === undefined) {
            reject('{5F2716CD-35D3-4E59-A2FC-334E56CAB578}')
        }
        const ph = 'B16A66B3-6467-4A5E-9E8F-330B89FBDD53';
        var sql = 'CREATE TABLE ' + json.name + ' (' + ph + ')';
        sqlTableColumns(json).then(function (sql_columns) {

            resolve(sql.replace(ph, sql_columns))
        }).catch(function (err) {
            reject(err + ',' + '{802D9EC8-B9DA-4B37-84BB-CAD0A237E19E}');
        })
    });
}

function sqlTableColumns(json) {
    return new Promise(function (resolve, reject) {
        if (json.columns === undefined) {
            reject('{96C9DB4C-94B4-4A03-AA03-95FF5143B172}');
        }
        var sql_columns = "";
        if (json.columns.length == 0) {
            reject('{496C0DF6-8D09-4939-9FD0-A738152FC996}');
        }
        for (var key in json.columns) {
            if (key != 0) sql_columns += ', ';
            sql_columns += json.columns[key].name + ' ' + json.columns[key].type;
            if (json.columns[key].not_null !== undefined) {
                if (json.columns[key].not_null.toLowerCase() == 'true') {
                    sql_columns += ' NOT NULL'
                }
            }
        }
        resolve(sql_columns);
    });
}

let getScriptPK = (json) => {
    return new Promise(function (resolve, reject) {
        var sql_columns = "";
        if (json.pkey === undefined) {
            resolve(sql_columns);
        }
        if (json.pkey.name === undefined) {
            reject('{0C7FEE72-3234-44D1-83F1-8329D2DEF13B}');
        }
        if (json.pkey.columns === undefined) {
            reject('{B5791193-D66F-4B66-BA82-28E6C566B601}');
        }
        if (json.pkey.columns.length != 1) {
            reject('{E34A3D09-062E-4A3A-A6DE-91DDB4194409}');
        }
        if (json.pkey.columns[0].name === undefined) {
            reject('{7A18CD84-D9C7-4166-ACF2-F0C43ED54137}');
        }
        sql_columns = 'ALTER TABLE ' + json.name + '  ADD CONSTRAINT "' + json.pkey.name + '" PRIMARY KEY(' + json.pkey.columns[0].name + ')';
        resolve(sql_columns);
    });
}

let getScriptIndex = (json) => {
    return new Promise(function (resolve, reject) {

        var sql_a = [];
        if (json.indexes === undefined) {
            resolve(sql_a);
        }
        if (json.indexes.length == 0) {
            resolve(sql_a);
        }
        for (key in json.indexes) {

            if (json.indexes[key].name === undefined) {
                reject('{3A3C0DBD-29FC-482D-9FB0-D98EA277D7AA}');
            }
            if (json.indexes[key].columns === undefined) {
                reject('{8FD43183-7868-4BCC-8D48-07441529050E}');
            }
            if (json.indexes[key].columns.length == 0) {
                reject('{BBDC310F-09B0-4926-8867-9BB84740D518}');
            }

            var columns = '';
            for (col_key in json.indexes[key].columns) {
                if (json.indexes[key].columns[col_key].name === undefined) {
                    reject('col:' + key + ' {26F2933B-A588-4073-A845-B909565E44AD}');
                }
                if (col_key > 0) {
                    columns += ', ';
                }
                columns += json.indexes[key].columns[col_key].name.toLowerCase();
            }
            sql = 'CREATE INDEX "' + json.indexes[key].name + '" ON "' + json.name.toLowerCase() + '" USING btree ("' + columns + '");';
            sql_a.push(sql);
        }
        resolve(sql_a);
    });
}

let getScriptFK = (json, arr_index) => {
    return new Promise(function (resolve, reject) {
        if (json.fkeys === undefined) {
            reject('{AC2919A5-6223-4EB0-9A20-9470FF427BA2}');
        }
        if (json.fkeys.length < arr_index - 1) {
            reject('{C13DAFFC-9638-4E0B-9D54-F4C06A186176}');
        }
        if (json.fkeys[arr_index].name === undefined) {
            reject('{E8E5B68A-2CBA-466D-857B-AEF8B2D30450}');
        }
        if (json.fkeys[arr_index].column === undefined) {
            reject('{31FA77FF-AC33-4E7E-A64B-C1EBF27F80C8}');
        }
        if (json.fkeys[arr_index].table === undefined) {
            reject('{0331C4AF-798C-4FBA-9140-7C4BC791C9D2}');
        }
        if (json.fkeys[arr_index].tcolumn === undefined) {
            reject('{C1AB4457-DAB3-4938-8F48-67555B552FFA}');
        }
        var sql = 'ALTER TABLE "' + json.name.toLowerCase() + '" ADD CONSTRAINT "' + json.fkeys[arr_index].name.toLowerCase() +
            '"  FOREIGN KEY ("' + json.fkeys[arr_index].column.toLowerCase() + '") REFERENCES "' + json.fkeys[arr_index].table.toLowerCase() +
            '" ("' + json.fkeys[arr_index].tcolumn.toLowerCase() + '") MATCH SIMPLE ON UPDATE NO ACTION ON DELETE NO ACTION;';
        resolve(sql);
    });
}

module.exports = {getScriptTable: getScriptTable, getScriptPK, getScriptIndex, getScriptFK};