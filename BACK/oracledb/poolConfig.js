module.exports = {

    get hrPool() {
        return {
            user: process.env.ORA_USER,
            password: process.env.ORA_USER_PASSWORD,
            connectString: process.env.CONNECTION_STRING,
            poolMin: +process.env.ORA_POOL_MIN,
            poolMax: +process.env.ORA_POOL_MAX,
            poolIncrement: +process.env.ORA_POOL_INCREMENT
        }
    }
};
