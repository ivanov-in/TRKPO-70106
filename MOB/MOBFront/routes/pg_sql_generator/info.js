let tableExist = (database, schema, table) => {
    table = table.replace("\"", "");
    var sql = 'select count(*) as count from information_schema.tables where table_catalog = \'' + database + '\' and table_schema = \'' + schema + '\' and table_name = \'' + table.toLowerCase() + '\'';
    return sql;
};
let schemaExist = (database, schema) => {
    var sql = 'select count(*) as count from information_schema.schemata t where catalog_name = \'' + database + '\' and t.schema_name = \'' + schema + '\'';
    return sql;
};
let fkey_exist = (database, schema, key) => {
    var sql = 'select count(*) as count from information_schema.referential_constraints t where t.constraint_catalog = \'' + database.toLowerCase() + '\' and t.constraint_schema = \''
        + schema.toLowerCase() + '\' and t.constraint_name = \'' + key.name.toLowerCase() + '\'';
    return sql;
};
module.exports = {tableExist, schemaExist, fkey_exist};