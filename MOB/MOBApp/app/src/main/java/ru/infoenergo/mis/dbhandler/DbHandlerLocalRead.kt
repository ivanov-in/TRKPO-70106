package ru.infoenergo.mis.dbhandler

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.core.net.toUri
import ru.infoenergo.mis.helpers.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** ************************************ **/
/**     Чтение данных с локальной БД     **/
/** ************************************ **/

class DbHandlerLocalRead(
    var context: Context, factory: SQLiteDatabase.CursorFactory?
) : DbHandlerLocal(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    private val patternTime = "yyyy-MM-dd HH:mm"

    // Получение маршрутного листа
    // ----------------------------------------
    fun getTaskCount(idInspector: Int): Int {
        val db = this.readableDatabase
        val cur =
            db.rawQuery("select count(id_task) as cnt_tasks from MI_MOB_TASK  where id_inspector = $idInspector", null)
        var res = 0

        if (cur.count != 0) {
            cur.moveToFirst()
            res = cur.getInt(0)
        }
        if (!cur.isClosed) cur.close()

        return res
    }

    // Получение маршрутного листа
    // ----------------------------------------
    fun getTaskList(date: LocalDate?, idInspector: Int): ArrayList<Task> {
        val query = " select * from MI_MOB_TASK " +
                " where (dat = '${date}' or (dat < '${date}' and (status < 8 or status = 15))) " +
                "  and id_inspector = $idInspector  " +
                " order by datetime(ttime), status "

        return readCursorTasks(query)
    }

    // Проверяем есть ли информация об инспекторе на устройстве
    // Если есть, то отправим минимальное значение id task и обновляем запись (+1)
    // notInc - не обновлять запись, только считать
    // ---------------------------------------------------------------------------
    fun getMinIdTask(idInspector: Int, notInc: Boolean = false): Int {
        val query = "select * from MI_INSPECTOR_DATA where id_inspector = $idInspector"
        val db = this.readableDatabase
        val cur = db.rawQuery(query, null)
        var minIdTask = 0
        try {
            if (cur.count != 0) {
                cur.moveToFirst()
                minIdTask = cur.getInt(cur.getColumnIndexOrThrow("MIN_TASK_ID"))
            }
            if (!cur.isClosed) cur.close()

            if (!notInc) {
                val minIdTaskNew = "-$idInspector${
                    (minIdTask.toString().replaceFirst("-$idInspector", "")).toInt() + 1
                }".toInt()
                DbHandlerLocalWrite(context, null).updateIdInspector(idInspector, minIdTaskNew)
            }

        } catch (e: Exception) {
            println("$TAG_ERR getMinIdTask: ${e.message}")
        }
        return minIdTask
    }

    // Поиск id_inspector в локальной базе, на случай, если надо авторизоваться, а сети нет
    // ------------------------------------------------------------------------------------
    fun getInspectorDataByPuser(puser: String, pswd: String): Int {
        val query = "select * from MI_INSPECTOR_DATA where puser = '$puser' and pswd = '$pswd'"
        val db = this.readableDatabase
        var idInspector = 0
        try {
            val cur = db.rawQuery(query, null)

            if (cur.count != 0) {
                cur.moveToFirst()
                idInspector = cur.getInt(cur.getColumnIndexOrThrow("id_inspector"))
            }
            if (!cur.isClosed) cur.close()
        } catch (e: Exception) {
            println("$TAG_ERR: ${e.message}")
            return idInspector
        }
        return idInspector
    }

    // Получаем ФИО инспектора
    // ---------------------------------------------
    fun getFioInspector(idInspector: Int): String {
        val query = "select * from MI_INSPECTOR_DATA where id_inspector = $idInspector"
        val db = this.readableDatabase
        val cur = db.rawQuery(query, null)
        var fio = ""
        try {
            if (cur.count != 0) {
                cur.moveToFirst()
                fio = cur.getString(cur.getColumnIndexOrThrow("fio"))
            }
            if (!cur.isClosed) cur.close()
        } catch (e: Exception) {
            println("$TAG_ERR getFioInspector: ${e.message}")
        }
        return fio
    }

    // Получение всех задач (для отправки на сервер)
    // *************************************************
    fun getTaskList(idInspector: Int): ArrayList<Task> {
        val query = "select * from MI_MOB_TASK where id_inspector = $idInspector"
        // and is_send = 0" // когда новый файл, то is_send может остаться 1
        val arrTasks = ArrayList<Task>()
        val db = this.readableDatabase
        val cur = db.rawQuery(query, null)

        while (cur.moveToNext()) {
            try {
                var time: String = cur.getString(cur.getColumnIndexOrThrow("ttime")).replace('T', ' ')
                if (time.length > 16) time = time.substring(0, 16)
                val task = Task(
                    dat = LocalDate.parse(cur.getString(cur.getColumnIndexOrThrow("dat"))),
                    id_task = cur.getInt(cur.getColumnIndexOrThrow("id_task")),
                    address = cur.getString(cur.getColumnIndexOrThrow("adr")) ?: "",
                    purpose = cur.getInt(cur.getColumnIndexOrThrow("purpose")),
                    prim = cur.getString(cur.getColumnIndexOrThrow("prim")) ?: "",
                    ttime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern(patternTime)),
                    status = cur.getInt(cur.getColumnIndexOrThrow("status")),
                    id_inspector = cur.getInt(cur.getColumnIndexOrThrow("id_inspector")),
                    kod_obj = cur.getInt(cur.getColumnIndexOrThrow("kod_obj")),
                    kod_numobj = if (cur.getColumnIndexOrThrow("kod_numobj") != -1)
                        cur.getInt(cur.getColumnIndexOrThrow("kod_numobj")) else 0,
                    kod_dog = cur.getInt(cur.getColumnIndexOrThrow("kod_dog")),
                    kodp = cur.getInt(cur.getColumnIndexOrThrow("kodp")),
                    ndog = cur.getString(cur.getColumnIndexOrThrow("ndog")) ?: "",
                    payer_name = cur.getString(cur.getColumnIndexOrThrow("payer_name")) ?: "",
                    fio_contact = cur.getString(cur.getColumnIndexOrThrow("fio_contact")) ?: "",
                    email_contact = cur.getString(cur.getColumnIndexOrThrow("email_contact")) ?: "",
                    tel_contact = cur.getString(cur.getColumnIndexOrThrow("tel_contact")) ?: "",
                    city = cur.getString(cur.getColumnIndexOrThrow("city")) ?: "",
                    street = cur.getString(cur.getColumnIndexOrThrow("street")) ?: "",
                    nd = cur.getString(cur.getColumnIndexOrThrow("nd")) ?: "",
                    house = cur.getString(cur.getColumnIndexOrThrow("houses")) ?: "",
                    fio_podp = cur.getString(cur.getColumnIndexOrThrow("fio_podp")) ?: "",
                    email_podp = cur.getString(cur.getColumnIndexOrThrow("email_podp")) ?: "",
                    tel_podp = cur.getString(cur.getColumnIndexOrThrow("tel_podp")) ?: "",
                    name_dolzhn_podp = cur.getString(cur.getColumnIndexOrThrow("name_dolzhn_podp")) ?: "",
                    kod_emp_podp = cur.getInt(cur.getColumnIndexOrThrow("kod_emp_podp"))
                )

                arrTasks.add(task)
            } catch (e: java.lang.Exception) {
                println("$TAG_ERR getRouteList (для отправки на сервер): ${e.message}")
            } finally {
                continue
            }
        }
        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()
        return arrTasks
    }

    // Получаем доп поля актов для сравнения с сервера
    // (только те id_task+id_act что пришли с сервера)
    // ------------------------------------------------------------------------
    fun getActFieldsDopShablonByIds(idsList: String, sendAllToServer: Boolean = false)
            : ArrayList<ActFieldsDopInfo> {
        val query =
            "select * from MI_ACT_FIELDS_DOP_SHABLON where (id_task, id_act) in (VALUES  ${idsList})"

        return readCursorActFieldsDop(query)
    }

    // Получаем доп поля акта из шаблонной таблицы
    // ------------------------------------------------------------------------
    fun getActFieldsDop(idTask: Int, idAct: Int, npp: Int): ArrayList<ActFieldsDopInfo> {
        val query =
            "select * from MI_ACT_FIELDS_DOP where id_task = $idTask and id_act = $idAct and npp = $npp"
        return if (readCursorActFieldsDop(query).size > 0)
            readCursorActFieldsDop(query)
        else ArrayList()
    }

    // Получаем доп поля акта из шаблонной таблицы (только для типов 9 и 10)
    // ------------------------------------------------------------------------
    fun getActFieldsDopShablon(idTask: Int, idAct: Int): ArrayList<ActFieldsDopInfo> {
        val query =
            "select * from MI_ACT_FIELDS_DOP_SHABLON where id_task = $idTask and id_act = $idAct " +
                    if (idAct in arrayOf(24, 25)) " and pu_name is not null" else ""
        return if (readCursorActFieldsDop(query).size > 0)
            readCursorActFieldsDop(query)
        else ArrayList()
    }

    // Чтение из курсора доп поля акта
    // ------------------------------------------------------------------------------
    private fun readCursorActFieldsDop(query: String): ArrayList<ActFieldsDopInfo> {
        val db = this.readableDatabase
        val cur = db.rawQuery(query, null)

        val arrActDops: ArrayList<ActFieldsDopInfo> = ArrayList()

        while (cur.moveToNext()) {
            try {
                val fieldsDop = ActFieldsDopInfo(
                    dat = if (cur.getString(cur.getColumnIndexOrThrow("dat")) == "null")
                        LocalDate.now() else LocalDate.parse(cur.getString(cur.getColumnIndexOrThrow("dat"))),
                    id_task = cur.getInt(cur.getColumnIndexOrThrow("id_task")),
                    id_act = cur.getInt(cur.getColumnIndexOrThrow("id_act")),
                    id_file = if (cur.getColumnIndex("id_file") == -1)
                        0 else cur.getInt(cur.getColumnIndexOrThrow("id_file")),
                    npp = if (cur.getColumnIndex("npp") == -1)
                        0 else cur.getInt(cur.getColumnIndexOrThrow("npp")),
                    num_obj = cur.getString(cur.getColumnIndexOrThrow("num_obj")) ?: "",
                    name_obj = cur.getString(cur.getColumnIndexOrThrow("name_obj")) ?: "",
                    address_obj = cur.getString(cur.getColumnIndexOrThrow("address_obj")) ?: "",
                    god_zd = cur.getString(cur.getColumnIndexOrThrow("god_zd")) ?: "",
                    nazn_name = cur.getString(cur.getColumnIndexOrThrow("nazn_name")) ?: "",
                    tvr = cur.getString(cur.getColumnIndexOrThrow("tvr")) ?: "",
                    pr_oto = cur.getString(cur.getColumnIndexOrThrow("pr_oto")) ?: "",
                    pr_sw = cur.getString(cur.getColumnIndexOrThrow("pr_sw")) ?: "",
                    volume = cur.getDouble(cur.getColumnIndexOrThrow("volume")),
                    square_total = cur.getDouble(cur.getColumnIndexOrThrow("square_total")),
                    point_name = cur.getString(cur.getColumnIndexOrThrow("point_name")) ?: "",
                    etaz = cur.getString(cur.getColumnIndexOrThrow("etaz")) ?: "",
                    so_q = cur.getDouble(cur.getColumnIndexOrThrow("so_q")),
                    sw_q = cur.getDouble(cur.getColumnIndexOrThrow("sw_q")),
                    st_q = cur.getDouble(cur.getColumnIndexOrThrow("st_q")),
                    gw_qmax = cur.getDouble(cur.getColumnIndexOrThrow("gw_qmax")),
                    name_vodo = cur.getString(cur.getColumnIndexOrThrow("name_vodo")) ?: "",
                    nn_wpol = cur.getDouble(cur.getColumnIndexOrThrow("nn_wpol")),
                    tt_wpol = cur.getDouble(cur.getColumnIndexOrThrow("tt_wpol")),
                    nn_prib = cur.getDouble(cur.getColumnIndexOrThrow("nn_prib")),
                    pr_rec = cur.getString(cur.getColumnIndexOrThrow("pr_rec")) ?: "",
                    pr_psusch = cur.getString(cur.getColumnIndexOrThrow("pr_psusch")) ?: "",
                    pr_iz_st = cur.getString(cur.getColumnIndexOrThrow("pr_iz_st")) ?: "",
                    nom_uch = cur.getString(cur.getColumnIndexOrThrow("nom_uch")) ?: "",
                    tip_name = cur.getString(cur.getColumnIndexOrThrow("tip_name")) ?: "",
                    pt_d = cur.getDouble(cur.getColumnIndexOrThrow("pt_d")),
                    pt_l = cur.getDouble(cur.getColumnIndexOrThrow("pt_l")),
                    name_pr_pt = cur.getString(cur.getColumnIndexOrThrow("name_pr_pt")) ?: "",
                    ot_d = cur.getDouble(cur.getColumnIndexOrThrow("ot_d")),
                    ot_l = cur.getDouble(cur.getColumnIndexOrThrow("ot_l")),
                    name_pr_ot = cur.getString(cur.getColumnIndexOrThrow("name_pr_ot")) ?: "",
                    uch_hgr = cur.getString(cur.getColumnIndexOrThrow("uch_hgr")) ?: "",
                    pu_num = cur.getString(cur.getColumnIndexOrThrow("pu_num")) ?: "",
                    pu_name = cur.getString(cur.getColumnIndexOrThrow("pu_name")) ?: "",
                    pu_mesto = cur.getString(cur.getColumnIndexOrThrow("pu_mesto")) ?: "",
                    pu_type = cur.getString(cur.getColumnIndexOrThrow("pu_type")) ?: "",
                    pu_diam = cur.getString(cur.getColumnIndexOrThrow("pu_diam")) ?: "",
                    pu_kolvo = cur.getString(cur.getColumnIndexOrThrow("pu_kolvo")) ?: "",
                    pu_proba_mesto = cur.getString(cur.getColumnIndexOrThrow("pu_proba_mesto")) ?: "",
                    q_sum = cur.getString(cur.getColumnIndexOrThrow("q_sum")) ?: "",
                    q_sum_max = cur.getString(cur.getColumnIndexOrThrow("q_sum_max")) ?: "",
                    pu_srok_poverki = cur.getString(cur.getColumnIndexOrThrow("pu_srok_poverki")) ?: "",
                    pu_num_plomba = cur.getString(cur.getColumnIndexOrThrow("pu_num_plomba")) ?: "",
                    pu_pokaz = cur.getString(cur.getColumnIndexOrThrow("pu_pokaz")) ?: "",
                    schema_prisoed_name = cur.getString(cur.getColumnIndexOrThrow("schema_prisoed_name")) ?: "",
                    schema_prisoed_kod = cur.getString(cur.getColumnIndexOrThrow("schema_prisoed_kod")) ?: ""
                )
                arrActDops.add(fieldsDop)
            } catch (e: java.lang.Exception) {
                println("$TAG_ERR getActFieldsDop: ${e.message}")
            }
        }

        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()

        return arrActDops
    }

    // Получаем поля актов для сравнения с сервера
    // (только те id_task+id_act что пришли с сервера)
    // ---------------------------------------------------------------
    fun getActFieldsShablonByIds(idsList: String): ArrayList<ActFieldsInfo> {
        val query = "select * from MI_ACT_FIELDS_SHABLON where (id_task, id_act) in (VALUES  ${idsList})"
        return readCursorActFields(query, readShablon = true)
    }

    // Проверяем наличие неподписанных актов задачи idTask типа idAct
    // ------------------------------------------------------------------------
    fun existedActsByIdAct(idTask: Int, idAct: Int): ArrayList<ActFieldsInfo> {
        val query = "select * from MI_ACT_FIELDS where id_task = $idTask and id_act = $idAct and is_signed = 0"
        return readCursorActFields(query)
    }

    // Проверяем наличие актов задачи idTask
    // -------------------------------------
    fun existedActsByIdTask(idTask: Int): ArrayList<ActFieldsInfo> {
        val query = "select * from MI_ACT_FIELDS where id_task = $idTask and id_act != 27"
        return readCursorActFields(query)
    }

    // Получаем поля акта для заполнения акта MI_ACT_FIELDS_SHABLON
    // --------------------------------------------------------------
    fun getActFieldsShablon(idTask: Int, idAct: Int): ActFieldsInfo {
        val query = "select * from MI_ACT_FIELDS_SHABLON where id_task = $idTask and id_act = $idAct"
        val actFields = readCursorActFields(query, readShablon = true)
        return if (actFields.size > 0)
            actFields.first()
        else ActFieldsInfo()
    }

    // Получаем последний npp по id_task + id_act
    // ----------------------------------------------
    fun getLastActNpp(idTask: Int, idAct: Int): Int {
        val db = this.readableDatabase
        val query = "select ifnull(max(npp), 0) as npp from MI_ACT_FIELDS where id_task = $idTask and id_act = $idAct"
        val cur = db.rawQuery(query, null)
        var npp = 0
        try {
            if (cur.count != 0) {
                cur.moveToFirst()
                npp = cur.getInt(cur.getColumnIndexOrThrow("npp"))
            }
        } catch (e: Exception) {
            println("$TAG_ERR: ${e.message}")
        } finally {
            if (!cur.isClosed) cur.close()
            if (db != null && db.isOpen) db.close()
        }
        return npp
    }

    // Получаем все файлы, по id_task, у которых is_send = 0 и id_file < 0  для отправки на сервер
    // *******************************************************************************************
    fun getAllFiles(idsTasks: String): ArrayList<FileInfo> {
        val query = "select * from MI_MOB_TASK_FILES where id_task in $idsTasks and is_send = 0 and id_file < 0"
        //+ " and (filedata like '%.pdf' and is_signed = 1 or filedata not like '%.pdf') "

        return readCursorFiles(query)
    }

    // Получаем всю информацию по актам для отправки на сервер
    // idsTasks в формате (1, 2, 3 [...])
    // *******************************************************************************
    fun getAllActFields(idsTasks: String): ArrayList<ActFieldsInfo> {
        val query = "select * from MI_ACT_FIELDS where id_task in $idsTasks"

        return readCursorActFields(query)
    }

    // Чтение из курсора поля акта
    // readShablon - признак считывания только тех полей, что есть в таблице MI_ACT_FIELDS_SHABLON
    // --------------------------------------------------------------------------------------------------------
    private fun readCursorActFields(query: String, readShablon: Boolean = false): ArrayList<ActFieldsInfo> {
        val db = this.readableDatabase
        val cur = db.rawQuery(query, null)

        val arrActFields: ArrayList<ActFieldsInfo> = ArrayList()

        while (cur.moveToNext()) {
            try {
                val actFields = ActFieldsInfo(
                    id_task = cur.getInt(cur.getColumnIndexOrThrow("id_task")),
                    id_act = cur.getInt(cur.getColumnIndexOrThrow("id_act")),
                    npp = if (cur.getColumnIndex("npp") == -1)
                        0 else cur.getInt(cur.getColumnIndexOrThrow("npp")),
                    id_file = if (cur.getColumnIndex("id_file") == -1)
                        0 else cur.getInt(cur.getColumnIndexOrThrow("id_file")),
                    kodp = cur.getInt(cur.getColumnIndexOrThrow("kodp")),
                    kod_dog = cur.getInt(cur.getColumnIndexOrThrow("kod_dog")),
                    kod_obj = cur.getInt(cur.getColumnIndexOrThrow("kod_obj")),
                    dat = if (cur.getString(cur.getColumnIndexOrThrow("dat")) == "null")
                        null else LocalDate.parse(cur.getString(cur.getColumnIndexOrThrow("dat"))),
                    num_act = cur.getString(cur.getColumnIndexOrThrow("num_act")) ?: "",
                    dat_act = cur.getString(cur.getColumnIndexOrThrow("dat_act")) ?: "",
                    payer_name = cur.getString(cur.getColumnIndexOrThrow("payer_name")) ?: "",
                    adr_org = cur.getString(cur.getColumnIndexOrThrow("adr_org")) ?: "",
                    fio_contact = cur.getString(cur.getColumnIndexOrThrow("fio_contact")) ?: "",
                    tel_contact = cur.getString(cur.getColumnIndexOrThrow("tel_contact")) ?: "",
                    filial_eso = cur.getString(cur.getColumnIndexOrThrow("filial_eso")) ?: "",
                    fio_eso = cur.getString(cur.getColumnIndexOrThrow("fio_eso")) ?: "",
                    tel_eso = cur.getString(cur.getColumnIndexOrThrow("tel_eso")) ?: "",
                    list_obj = cur.getString(cur.getColumnIndexOrThrow("list_obj")) ?: "",
                    name_obj = cur.getString(cur.getColumnIndexOrThrow("name_obj")) ?: "",
                    num_obj = cur.getString(cur.getColumnIndexOrThrow("num_obj")) ?: "",
                    adr_obj = cur.getString(cur.getColumnIndexOrThrow("adr_obj")) ?: "",
                    ndog = cur.getString(cur.getColumnIndexOrThrow("ndog")) ?: "",
                    dat_dog = cur.getString(cur.getColumnIndexOrThrow("dat_dog")) ?: "",
                    otop_period = cur.getString(cur.getColumnIndexOrThrow("otop_period")) ?: "",
                    sum_dolg = cur.getString(cur.getColumnIndexOrThrow("sum_dolg")) ?: "",
                    remark_dog = cur.getString(cur.getColumnIndexOrThrow("remark_dog")) ?: "",
                    nal_podp_doc = cur.getString(cur.getColumnIndexOrThrow("nal_podp_doc")) ?: "",
                    opl_calcul = cur.getString(cur.getColumnIndexOrThrow("opl_calcul")) ?: "",
                    osnov = cur.getString(cur.getColumnIndexOrThrow("osnov")) ?: "",
                    city = cur.getString(cur.getColumnIndexOrThrow("city")) ?: "",
                    shablon = cur.getString(cur.getColumnIndexOrThrow("shablon")) ?: "",
                    name_dolzhn_contact = cur.getString(cur.getColumnIndexOrThrow("name_dolzhn_contact")) ?: "",
                    name_dolzhn_eso = cur.getString(cur.getColumnIndexOrThrow("name_dolzhn_eso")) ?: "",
                    period_dolg = cur.getString(cur.getColumnIndexOrThrow("period_dolg")) ?: "",
                    name_st = cur.getString(cur.getColumnIndexOrThrow("name_st")) ?: "",
                    name_mag = cur.getString(cur.getColumnIndexOrThrow("name_mag")) ?: "",
                    name_tk = cur.getString(cur.getColumnIndexOrThrow("name_tk")) ?: "",
                    name_aw = cur.getString(cur.getColumnIndexOrThrow("name_aw")) ?: "",
                    inn_org = cur.getString(cur.getColumnIndexOrThrow("inn_org")) ?: "",
                    kpp_org = cur.getString(cur.getColumnIndexOrThrow("kpp_org")) ?: "",
                    nazn_name = cur.getString(cur.getColumnIndexOrThrow("nazn_name")) ?: "",
                    nal_so = cur.getString(cur.getColumnIndexOrThrow("nal_so")) ?: "",
                    nal_sw = cur.getString(cur.getColumnIndexOrThrow("nal_sw")) ?: "",
                    nal_st = cur.getString(cur.getColumnIndexOrThrow("nal_st")) ?: "",
                    nal_gv = cur.getString(cur.getColumnIndexOrThrow("nal_gv")) ?: "",
                    volume_obj = cur.getString(cur.getColumnIndexOrThrow("volume_obj")) ?: "",
                    so_q = cur.getString(cur.getColumnIndexOrThrow("so_q")) ?: "",
                    st_q = cur.getString(cur.getColumnIndexOrThrow("st_q")) ?: "",
                    sw_q = cur.getString(cur.getColumnIndexOrThrow("sw_q")) ?: "",
                    gw_q = cur.getString(cur.getColumnIndexOrThrow("gw_q")) ?: "",
                    q_sum = cur.getString(cur.getColumnIndexOrThrow("q_sum")) ?: "",
                    director_tatenergo = cur.getString(cur.getColumnIndexOrThrow("director_tatenergo")) ?: "",
                    director_t_dover_num = cur.getString(cur.getColumnIndexOrThrow("director_t_dover_num")) ?: "",
                    filial_address = cur.getString(cur.getColumnIndexOrThrow("filial_address")) ?: "",
                    filial_tel = cur.getString(cur.getColumnIndexOrThrow("filial_tel")) ?: "",
                )

                if (readShablon) {
                    arrActFields.add(actFields)
                    continue
                } else {
                    actFields.itog_text = cur.getString(cur.getColumnIndexOrThrow("itog_text")) ?: ""
                    actFields.purpose_text = cur.getString(cur.getColumnIndexOrThrow("purpose_text")) ?: ""
                    actFields.podgotovka = cur.getString(cur.getColumnIndexOrThrow("podgotovka")) ?: ""

                    // Акт проверки филиалом готовности абонента к отопительному периоду ActOtopPeriod
                    if (actFields.id_act == 14) {
                        actFields.apply {
                            id_manometr = cur.getString(cur.getColumnIndexOrThrow("id_manometr")) ?: "0"
                            id_aupr_so = cur.getString(cur.getColumnIndexOrThrow("id_aupr_so")) ?: "0"
                            id_aupr_sw = cur.getString(cur.getColumnIndexOrThrow("id_aupr_sw")) ?: "0"
                            id_aupr_gvs = cur.getString(cur.getColumnIndexOrThrow("id_aupr_gvs")) ?: "0"
                            id_aupr_sv = cur.getString(cur.getColumnIndexOrThrow("id_aupr_sv")) ?: "0"
                            sost_kip_str = cur.getString(cur.getColumnIndexOrThrow("sost_kip_str")) ?: ""
                            nal_act_gidro = cur.getString(cur.getColumnIndexOrThrow("nal_act_gidro")) ?: ""
                            id_sost_tube = cur.getString(cur.getColumnIndexOrThrow("id_sost_tube")) ?: "0"
                            id_sost_armatur = cur.getString(cur.getColumnIndexOrThrow("id_sost_armatur")) ?: "0"
                            id_sost_izol = cur.getString(cur.getColumnIndexOrThrow("id_sost_izol")) ?: "0"
                            sost_tube_str = cur.getString(cur.getColumnIndexOrThrow("sost_tube_str")) ?: ""
                            id_sost_net = cur.getString(cur.getColumnIndexOrThrow("id_sost_net")) ?: "0"
                            sost_net_str = cur.getString(cur.getColumnIndexOrThrow("sost_net_str")) ?: ""
                            id_sost_utepl = cur.getString(cur.getColumnIndexOrThrow("id_sost_utepl")) ?: "0"
                            sost_utepl_str = cur.getString(cur.getColumnIndexOrThrow("sost_utepl_str")) ?: ""
                            id_nal_pasport = cur.getString(cur.getColumnIndexOrThrow("id_nal_pasport")) ?: "0"
                            id_nal_schema = cur.getString(cur.getColumnIndexOrThrow("id_nal_schema")) ?: "0"
                            id_nal_instr = cur.getString(cur.getColumnIndexOrThrow("id_nal_instr")) ?: "0"
                            nal_pasp_str = cur.getString(cur.getColumnIndexOrThrow("nal_pasp_str")) ?: ""
                            id_nal_direct_connect =
                                cur.getString(cur.getColumnIndexOrThrow("id_nal_direct_connect")) ?: "0"
                            nal_direct_connect = cur.getString(cur.getColumnIndexOrThrow("nal_direct_connect")) ?: ""
                            dop_info = cur.getString(cur.getColumnIndexOrThrow("dop_info")) ?: ""
                        }
                    }

                    // Акт проверки готовности (по Приказу 103) ActOtopPeriod103
                    if (actFields.id_act == 15) {
                        actFields.apply {
                            comiss_post_gotov = cur.getString(cur.getColumnIndexOrThrow("comiss_post_gotov")) ?: ""
                            pred_comiss = cur.getString(cur.getColumnIndexOrThrow("pred_comiss")) ?: ""
                            zam_pred_gkh = cur.getString(cur.getColumnIndexOrThrow("zam_pred_gkh")) ?: ""
                            podpisi = cur.getString(cur.getColumnIndexOrThrow("podpisi")) ?: ""
                            dop_info = cur.getString(cur.getColumnIndexOrThrow("dop_info")) ?: ""
                        }
                    }

                    // Акт о готовности внутриплощадочных и внутридомовых сетей
                    if (actFields.id_act == 17) {
                        actFields.apply {
                            square_obj = cur.getString(cur.getColumnIndexOrThrow("square_obj")) ?: ""
                            nal_document = cur.getString(cur.getColumnIndexOrThrow("nal_document")) ?: ""

                            podgotovka = cur.getString(cur.getColumnIndexOrThrow("podgotovka")) ?: ""
                            director_t_dover_num =
                                cur.getString(cur.getColumnIndexOrThrow("director_t_dover_num")) ?: ""
                            zayavitel = cur.getString(cur.getColumnIndexOrThrow("zayavitel")) ?: ""
                            zayavitel_dover = cur.getString(cur.getColumnIndexOrThrow("zayavitel_dover")) ?: ""
                            podgotovka_proj_num = cur.getString(cur.getColumnIndexOrThrow("podgotovka_proj_num")) ?: ""
                            podgotovka_proj_ispoln =
                                cur.getString(cur.getColumnIndexOrThrow("podgotovka_proj_ispoln")) ?: ""
                            podgotovka_proj_utvergden =
                                cur.getString(cur.getColumnIndexOrThrow("podgotovka_proj_utvergden")) ?: ""
                            net_inner_teplonositel =
                                cur.getString(cur.getColumnIndexOrThrow("net_inner_teplonositel")) ?: ""
                            net_inner_dp = cur.getString(cur.getColumnIndexOrThrow("net_inner_dp")) ?: ""
                            net_inner_do = cur.getString(cur.getColumnIndexOrThrow("net_inner_do")) ?: ""
                            net_inner_tip_kanal = cur.getString(cur.getColumnIndexOrThrow("net_inner_tip_kanal")) ?: ""
                            net_inner_tube_type_p =
                                cur.getString(cur.getColumnIndexOrThrow("net_inner_tube_type_p")) ?: ""
                            net_inner_tube_type_o =
                                cur.getString(cur.getColumnIndexOrThrow("net_inner_tube_type_o")) ?: ""
                            net_inner_l = cur.getString(cur.getColumnIndexOrThrow("net_inner_l")) ?: ""
                            net_inner_l_undeground =
                                cur.getString(cur.getColumnIndexOrThrow("net_inner_l_undeground")) ?: ""
                            net_inner_otstuplenie =
                                cur.getString(cur.getColumnIndexOrThrow("net_inner_otstuplenie")) ?: ""
                            energo_effect_object =
                                cur.getString(cur.getColumnIndexOrThrow("energo_effect_object")) ?: ""
                            nal_rezerv_istochnik =
                                cur.getString(cur.getColumnIndexOrThrow("nal_rezerv_istochnik")) ?: ""
                            nal_svyazi = cur.getString(cur.getColumnIndexOrThrow("nal_svyazi")) ?: ""
                            vid_connect_system = cur.getString(cur.getColumnIndexOrThrow("vid_connect_system")) ?: ""
                            elevator_num = cur.getString(cur.getColumnIndexOrThrow("elevator_num")) ?: ""
                            elevator_diam = cur.getString(cur.getColumnIndexOrThrow("elevator_diam")) ?: ""
                            podogrev_otop_num = cur.getString(cur.getColumnIndexOrThrow("podogrev_otop_num")) ?: ""
                            podogrev_otop_kolvo_sekc =
                                cur.getString(cur.getColumnIndexOrThrow("podogrev_otop_kolvo_sekc")) ?: ""
                            podogrev_otop_l_sekc =
                                cur.getString(cur.getColumnIndexOrThrow("podogrev_otop_l_sekc")) ?: ""
                            podogrev_otop_nazn = cur.getString(cur.getColumnIndexOrThrow("podogrev_otop_nazn")) ?: ""
                            podogrev_otop_marka = cur.getString(cur.getColumnIndexOrThrow("podogrev_otop_marka")) ?: ""
                            d_napor_patrubok = cur.getString(cur.getColumnIndexOrThrow("d_napor_patrubok")) ?: ""
                            power_electro_engine =
                                cur.getString(cur.getColumnIndexOrThrow("power_electro_engine")) ?: ""
                            chastota_vr_engine = cur.getString(cur.getColumnIndexOrThrow("chastota_vr_engine")) ?: ""
                            drossel_diafragma_d = cur.getString(cur.getColumnIndexOrThrow("drossel_diafragma_d")) ?: ""
                            drossel_diafragma_mesto =
                                cur.getString(cur.getColumnIndexOrThrow("drossel_diafragma_mesto")) ?: ""
                            drossel_diafragma_tip_otop =
                                cur.getString(cur.getColumnIndexOrThrow("drossel_diafragma_tip_otop")) ?: ""
                            drossel_diafragma_cnt_stoyak =
                                cur.getString(cur.getColumnIndexOrThrow("drossel_diafragma_cnt_stoyak")) ?: ""
                            type_oto_prib = cur.getString(cur.getColumnIndexOrThrow("type_oto_prib")) ?: ""
                            schema_vkl_gvs = cur.getString(cur.getColumnIndexOrThrow("schema_vkl_gvs")) ?: ""
                            schema_vkl_podogrev = cur.getString(cur.getColumnIndexOrThrow("schema_vkl_podogrev")) ?: ""
                            kolvo_sekc_1 = cur.getString(cur.getColumnIndexOrThrow("kolvo_sekc_1")) ?: ""
                            kolvo_sekc_1_l = cur.getString(cur.getColumnIndexOrThrow("kolvo_sekc_1_l")) ?: ""
                            kolvo_sekc_2 = cur.getString(cur.getColumnIndexOrThrow("kolvo_sekc_2")) ?: ""
                            kolvo_sekc_2_l = cur.getString(cur.getColumnIndexOrThrow("kolvo_sekc_2_l")) ?: ""
                            kolvo_kalorifer = cur.getString(cur.getColumnIndexOrThrow("kolvo_kalorifer")) ?: ""
                            poverhnost_nagreva = cur.getString(cur.getColumnIndexOrThrow("poverhnost_nagreva")) ?: ""
                            mesto_karta = cur.getString(cur.getColumnIndexOrThrow("mesto_karta")) ?: ""
                            dop_info = cur.getString(cur.getColumnIndexOrThrow("dop_info")) ?: ""
                        }
                    }

                    // Акт о подключении к системе теплоснабжения
                    if (actFields.id_act == 18) {
                        actFields.apply {
                            podgotovka = cur.getString(cur.getColumnIndexOrThrow("podgotovka")) ?: ""
                            director_t_dover_num =
                                cur.getString(cur.getColumnIndexOrThrow("director_t_dover_num")) ?: ""
                            zayavitel = cur.getString(cur.getColumnIndexOrThrow("zayavitel")) ?: ""
                            zayavitel_dover = cur.getString(cur.getColumnIndexOrThrow("zayavitel_dover")) ?: ""

                            mesto_karta = cur.getString(cur.getColumnIndexOrThrow("mesto_karta")) ?: ""
                            pu_data = cur.getString(cur.getColumnIndexOrThrow("pu_data")) ?: ""
                            pu_pover_lico = cur.getString(cur.getColumnIndexOrThrow("pu_pover_lico")) ?: ""
                            pu_pover_pokaz = cur.getString(cur.getColumnIndexOrThrow("pu_pover_pokaz")) ?: ""
                            pu_pover_rez = cur.getString(cur.getColumnIndexOrThrow("pu_pover_rez")) ?: ""
                            balans_prinadl_obj = cur.getString(cur.getColumnIndexOrThrow("balans_prinadl_obj")) ?: ""
                            balans_prin_dop = cur.getString(cur.getColumnIndexOrThrow("balans_prin_dop")) ?: ""
                            gr_ekspl_otvetst = cur.getString(cur.getColumnIndexOrThrow("gr_ekspl_otvetst")) ?: ""
                            gr_ekspl_otvetst_dop =
                                cur.getString(cur.getColumnIndexOrThrow("gr_ekspl_otvetst_dop")) ?: ""
                            st_podkl_rub = cur.getString(cur.getColumnIndexOrThrow("st_podkl_rub")) ?: ""
                            st_podkl_rub_nds = cur.getString(cur.getColumnIndexOrThrow("st_podkl_rub_nds")) ?: ""
                            podkl_dop_sved = cur.getString(cur.getColumnIndexOrThrow("podkl_dop_sved")) ?: ""
                        }
                    }

                    // Акт после ограничения, отключения
                    if (actFields.id_act in arrayOf(19)) {
                        actFields.apply {
                            act_poluchil = cur.getString(cur.getColumnIndexOrThrow("act_poluchil")) ?: ""
                            dop_info = cur.getString(cur.getColumnIndexOrThrow("dop_info")) ?: ""
                            uvedoml_otkl_num = cur.getString(cur.getColumnIndexOrThrow("uvedoml_otkl_num")) ?: ""
                            uvedoml_otkl_date = cur.getString(cur.getColumnIndexOrThrow("uvedoml_otkl_date")) ?: ""

                            pred_pogash_dolg_num =
                                cur.getString(cur.getColumnIndexOrThrow("pred_pogash_dolg_num")) ?: ""
                            pred_pogash_dolg_date =
                                cur.getString(cur.getColumnIndexOrThrow("pred_pogash_dolg_date")) ?: ""
                            otkl_proizv = cur.getString(cur.getColumnIndexOrThrow("otkl_proizv")) ?: ""
                            otkl_pu_pokaz_do = cur.getString(cur.getColumnIndexOrThrow("otkl_pu_pokaz_do")) ?: ""
                            otkl_pu_pokaz_posle = cur.getString(cur.getColumnIndexOrThrow("otkl_pu_pokaz_posle")) ?: ""
                        }
                    }

                    // Акт об отказе в доступе к теплоустановкам
                    if (actFields.id_act == 20) {
                        actFields.apply {
                            act_poluchil = cur.getString(cur.getColumnIndexOrThrow("act_poluchil")) ?: ""
                            uvedoml_otkl_num = cur.getString(cur.getColumnIndexOrThrow("uvedoml_otkl_num")) ?: ""
                            uvedoml_otkl_date = cur.getString(cur.getColumnIndexOrThrow("uvedoml_otkl_date")) ?: ""
                            prichina_otkaza = cur.getString(cur.getColumnIndexOrThrow("prichina_otkaza")) ?: ""
                            otkaz_svidet_1 = cur.getString(cur.getColumnIndexOrThrow("otkaz_svidet_1")) ?: ""
                            otkaz_svidet_2 = cur.getString(cur.getColumnIndexOrThrow("otkaz_svidet_2")) ?: ""
                        }
                    }

                    // Акт бездоговорного потребления
                    if (actFields.id_act == 23) {
                        actFields.apply {
                            square_obj = cur.getString(cur.getColumnIndexOrThrow("square_obj")) ?: ""
                            pravo_sobstv = cur.getString(cur.getColumnIndexOrThrow("pravo_sobstv")) ?: ""
                            uvedom_aktirov_num = cur.getString(cur.getColumnIndexOrThrow("uvedom_aktirov_num")) ?: ""
                            uvedom_aktirov_date = cur.getString(cur.getColumnIndexOrThrow("uvedom_aktirov_date")) ?: ""
                            nal_pu = cur.getString(cur.getColumnIndexOrThrow("nal_pu")) ?: ""
                            nal_aupr = cur.getString(cur.getColumnIndexOrThrow("nal_aupr")) ?: ""
                            bezdog_sposob_num = cur.getString(cur.getColumnIndexOrThrow("bezdog_sposob_num")) ?: ""
                            bezdog_ustanovleno = cur.getString(cur.getColumnIndexOrThrow("bezdog_ustanovleno")) ?: ""
                            bezdog_narushenie = cur.getString(cur.getColumnIndexOrThrow("bezdog_narushenie")) ?: ""
                            bezdog_pereraschet_s =
                                cur.getString(cur.getColumnIndexOrThrow("bezdog_pereraschet_s")) ?: ""
                            bezdog_pereraschet_po =
                                cur.getString(cur.getColumnIndexOrThrow("bezdog_pereraschet_po")) ?: ""
                            bezdog_predpis = cur.getString(cur.getColumnIndexOrThrow("bezdog_predpis")) ?: ""
                            bezdog_obyasn = cur.getString(cur.getColumnIndexOrThrow("bezdog_obyasn")) ?: ""
                            bezdog_pretenz = cur.getString(cur.getColumnIndexOrThrow("bezdog_pretenz")) ?: ""
                            otkaz_svidet_1 = cur.getString(cur.getColumnIndexOrThrow("otkaz_svidet_1")) ?: ""
                            otkaz_svidet_2 = cur.getString(cur.getColumnIndexOrThrow("otkaz_svidet_2")) ?: ""
                            dop_info = cur.getString(cur.getColumnIndexOrThrow("dop_info")) ?: ""
                        }
                    }

                    if (actFields.id_act in arrayOf(21, 22))
                        actFields.apply {

                            filial_name = cur.getString(cur.getColumnIndexOrThrow("filial_name")) ?: ""
                            ispolnitel = cur.getString(cur.getColumnIndexOrThrow("ispolnitel")) ?: ""
                            dog_podkl_num = cur.getString(cur.getColumnIndexOrThrow("dog_podkl_num")) ?: ""
                            dog_podkl_date = cur.getString(cur.getColumnIndexOrThrow("dog_podkl_date")) ?: ""

                            predst_potrebit = cur.getString(cur.getColumnIndexOrThrow("predst_potrebit")) ?: ""
                            predst_potrebit_dover =
                                cur.getString(cur.getColumnIndexOrThrow("predst_potrebit_dover")) ?: ""

                            uslovie_podkl_num = cur.getString(cur.getColumnIndexOrThrow("uslovie_podkl_num")) ?: ""
                            soglasov_proekte_num =
                                cur.getString(cur.getColumnIndexOrThrow("soglasov_proekte_num")) ?: ""
                            dopusk_s = cur.getString(cur.getColumnIndexOrThrow("dopusk_s")) ?: ""
                            dopusk_po = cur.getString(cur.getColumnIndexOrThrow("dopusk_po")) ?: ""
                            tel_spravki = cur.getString(cur.getColumnIndexOrThrow("tel_spravki")) ?: ""
                            tel_dispetch = cur.getString(cur.getColumnIndexOrThrow("tel_dispetch")) ?: ""
                            org_ustanov_pu = cur.getString(cur.getColumnIndexOrThrow("org_ustanov_pu")) ?: ""

                            podkl_num = cur.getString(cur.getColumnIndexOrThrow("podkl_num")) ?: ""
                            q_max = cur.getString(cur.getColumnIndexOrThrow("q_max")) ?: ""
                        }

                    if (actFields.id_act in arrayOf(24, 25))
                        actFields.apply {
                            pu_data = cur.getString(cur.getColumnIndexOrThrow("pu_data")) ?: ""
                            dopusk_s = cur.getString(cur.getColumnIndexOrThrow("dopusk_s")) ?: ""
                            dopusk_po = cur.getString(cur.getColumnIndexOrThrow("dopusk_po")) ?: ""
                            tel_spravki = cur.getString(cur.getColumnIndexOrThrow("tel_spravki")) ?: ""
                            tel_dispetch = cur.getString(cur.getColumnIndexOrThrow("tel_dispetch")) ?: ""
                            org_ustanov_pu = cur.getString(cur.getColumnIndexOrThrow("org_ustanov_pu")) ?: ""
                            dop_info = cur.getString(cur.getColumnIndexOrThrow("dop_info")) ?: ""
                        }
                    arrActFields.add(actFields)
                }
            } catch (e: java.lang.Exception) {
                println("$TAG_ERR getActFields: ${e.message}")
            }
        }
        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()

        return arrActFields
    }

    // Получение определённых задач для сравнения данных с сервера и локальных
    // -----------------------------------------------------------------------
    fun getTasksByIds(idsList: String): ArrayList<Task> {
        val query = " select * from MI_MOB_TASK where id_task in ($idsList)"
        return readCursorTasks(query)
    }

    // Получение задачи
    // -----------------------------------------------------------------------
    fun getTaskById(idTask: Int): Task {
        val query = " select * from MI_MOB_TASK where id_task = $idTask"
        val tasks = readCursorTasks(query)
        return if (tasks.size == 0) Task() else tasks.first()
    }

    // Чтение заданий (task) из курсора (передаём строку запрос)
    // ----------------------------------------------------------
    private fun readCursorTasks(query: String): ArrayList<Task> {
        val arrTasks = ArrayList<Task>()
        val db = this.readableDatabase
        val cur = db.rawQuery(query, null)

        while (cur.moveToNext()) {
            try {
                var time: String = cur.getString(cur.getColumnIndexOrThrow("ttime")).replace('T', ' ')
                if (time.length > 16) time = time.substring(0, 16)

                val task = Task(
                    id_task = cur.getInt(cur.getColumnIndexOrThrow("id_task")),
                    kod_obj = cur.getInt(cur.getColumnIndexOrThrow("kod_obj")),
                    kod_numobj = cur.getInt(cur.getColumnIndexOrThrow("kod_numobj")),
                    dat = LocalDate.parse(cur.getString(cur.getColumnIndexOrThrow("dat"))),
                    address = cur.getString(cur.getColumnIndexOrThrow("adr")) ?: "",
                    purpose = cur.getInt(cur.getColumnIndexOrThrow("purpose")),
                    purpose_name = cur.getString(cur.getColumnIndexOrThrow("purpose_name")),
                    prim = cur.getString(cur.getColumnIndexOrThrow("prim")) ?: "",
                    status = cur.getInt(cur.getColumnIndexOrThrow("status")),
                    status_name = cur.getString(cur.getColumnIndexOrThrow("status_name")) ?: "",
                    fio_contact = cur.getString(cur.getColumnIndexOrThrow("fio_contact")) ?: "",
                    email_contact = cur.getString(cur.getColumnIndexOrThrow("email_contact")) ?: "",
                    tel_contact = cur.getString(cur.getColumnIndexOrThrow("tel_contact")) ?: "",
                    payer_name = cur.getString(cur.getColumnIndexOrThrow("payer_name")) ?: "",
                    kod_dog = cur.getInt(cur.getColumnIndexOrThrow("kod_dog")),
                    kodp = cur.getInt(cur.getColumnIndexOrThrow("kodp")),
                    lan = cur.getString(cur.getColumnIndexOrThrow("lan")) ?: "",
                    lat = cur.getString(cur.getColumnIndexOrThrow("lat")) ?: "",
                    schema_zulu = cur.getString(cur.getColumnIndexOrThrow("schema_zulu")) ?: "",
                    border_zulu = cur.getString(cur.getColumnIndexOrThrow("border_zulu")) ?: "",
                    ndog = cur.getString(cur.getColumnIndexOrThrow("ndog")) ?: "",
                    id_inspector = cur.getInt(cur.getColumnIndexOrThrow("id_inspector")),
                    fio = cur.getString(cur.getColumnIndexOrThrow("fio_inspector")) ?: "",
                    ttime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern(patternTime)),
                    fio_podp = cur.getString(cur.getColumnIndexOrThrow("fio_podp")) ?: "",
                    email_podp = cur.getString(cur.getColumnIndexOrThrow("email_podp")) ?: "",
                    tel_podp = cur.getString(cur.getColumnIndexOrThrow("tel_podp")) ?: "",
                    name_dolzhn_podp = cur.getString(cur.getColumnIndexOrThrow("name_dolzhn_podp")) ?: "",
                    kod_emp_podp = cur.getInt(cur.getColumnIndexOrThrow("kod_emp_podp"))
                )

                if (task.id_task < 0) {
                    task.apply {
                        house = cur.getString(cur.getColumnIndexOrThrow("houses")) ?: ""
                        nd = cur.getString(cur.getColumnIndexOrThrow("nd")) ?: ""
                        street = cur.getString(cur.getColumnIndexOrThrow("street")) ?: ""
                        city = cur.getString(cur.getColumnIndexOrThrow("city")) ?: ""
                    }
                }

                arrTasks.add(task)
            } catch (e: java.lang.Exception) {
                println("$TAG_ERR getRouteList: ${e.message}")
            } finally {
                continue
            }
        }
        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()
        return arrTasks
    }

    // Список файлов, прикрепленных к задаче
    // ----------------------------------------------
    fun getFiles(idTask: Int): ArrayList<FileInfo> {
        val query = "select * from MI_MOB_TASK_FILES where id_task = $idTask"
        return readCursorFiles(query)
    }

    // Список файлов, прикрепленных к задаче для отправки
    // --------------------------------------------------
    fun getFilesToSend(idTask: Int, idAct: Int, npp: Int, idFile: Int): ArrayList<FileInfo> {
        val query =
            "select * from MI_MOB_TASK_FILES where id_task = $idTask and is_send = 0 and id_act = $idAct and npp = $npp or id_file = $idFile"
        return readCursorFiles(query)
    }

    // Список фоток, прикрепленных к акту
    // ----------------------------------------------
    fun getFilesAttaches(idTask: Int, idAct: Int, npp: Int): ArrayList<FileInfo> {
        val query = "select * from MI_MOB_TASK_FILES where id_task = $idTask and id_act = $idAct and npp = $npp " +
                " and lower(filename) not like '%.pdf%'"
        return readCursorFiles(query)
    }

    // Файл по Id (для отправки по почте)
    // ----------------------------------------------
    fun getFile(idFile: Int): FileInfo {
        val query = "select * from MI_MOB_TASK_FILES where id_file = $idFile"
        return readCursorFiles(query).first()
    }

    // Чтение файлов из курсора (передаём строку запрос)
    // --------------------------------------------------------------
    private fun readCursorFiles(query: String): ArrayList<FileInfo> {
        val filesArray = ArrayList<FileInfo>()
        val db = this.readableDatabase
        val cur = db.rawQuery(query, null)
        try {
            while (cur.moveToNext()) {
                try {
                    val uriString = cur.getString(cur.getColumnIndexOrThrow("uri"))
                    var time = try {
                        cur.getString(cur.getColumnIndexOrThrow("date_send_to_client"))
                            .replace('T', ' ')
                    } catch (e: Exception) {
                        ""
                    }
                    if (time.length > 16) time = time.substring(0, 16)

                    val photo = FileInfo(
                        id_task = cur.getInt(cur.getColumnIndexOrThrow("id_task")),
                        id_act = cur.getInt(cur.getColumnIndex("id_act")),
                        npp = cur.getInt(cur.getColumnIndexOrThrow("npp")),
                        filename = cur.getString(cur.getColumnIndexOrThrow("filename")) ?: "",
                        filedata = cur.getBlob(cur.getColumnIndexOrThrow("filedata")) ?: null,
                        id_file = cur.getInt(cur.getColumnIndexOrThrow("id_file")),
                        uri = if (uriString.isNullOrEmpty()) null else uriString.toUri(),
                        is_signed = cur.getInt(cur.getColumnIndexOrThrow("is_signed")),
                        is_send = cur.getInt(cur.getColumnIndexOrThrow("is_send")),
                        paper = cur.getInt(cur.getColumnIndexOrThrow("paper")),
                        date_send_to_client = try {
                            if (time.isNotEmpty()) LocalDateTime.parse(
                                time,
                                DateTimeFormatter.ofPattern(patternTime)
                            ) else null
                        } catch (e: Exception) {
                            try {
                                if (time.isNotEmpty()) LocalDateTime.parse(
                                    time,
                                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                                ) else null
                            } catch (e: Exception) {
                                null
                            }
                        },
                        email_client = cur.getString(cur.getColumnIndexOrThrow("email_client")) ?: ""
                    )
                    filesArray.add(photo)
                } catch (e: Exception) {
                    println("$TAG_ERR readCursorFiles ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("$TAG_ERR readCursorFiles ${e.message}")
        }
        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()

        return filesArray
    }

    // Список файлов для истории посещений
    // (id_task - задание из истории, не из маршрутного листа)
    // -------------------------------------------------------
    fun getFilesHistory(idTask: Int): ArrayList<FileInfo> {
        val query = "select  * from MI_MOB_TASK_FILES_HISTORY where id_task = $idTask"
        return readCursorFiles(query)
    }

    // Список целей для spinner
    // ----------------------------------------
    fun getPurposeList(): ArrayList<String> {
        val arrPurpose = ArrayList<String>()
        val db = this.readableDatabase

        val cur = db.rawQuery("select purpose, name from mi_purpose", null)

        while (cur.moveToNext()) {
            try {
                arrPurpose.add(cur.getString(cur.getColumnIndexOrThrow("name")))
            } catch (e: Exception) {
                println("$TAG_ERR getPurposeList ${e.message}")
            }
        }

        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()

        // Заглушка, если данных совсем нет
        if (arrPurpose.size == 0) {
            arrPurpose.add("технический аудит")
            arrPurpose.add("присоединение")
            arrPurpose.add("контроль ПУ")
        }

        return arrPurpose
    }

    // Список подписантов для spinner
    // ----------------------------------------
    fun getPodpisantList(idTask: Int): ArrayList<Podpisant> {
        val arrPodpisant = ArrayList<Podpisant>()
        val db = this.readableDatabase

        val cur = db.rawQuery(
            "select id_task, fio, e_mail, tel, name_dolzhn, kod_emp " +
                    " from MI_PODPISANT where id_task = $idTask", null
        )
        arrPodpisant.add(Podpisant())

        while (cur.moveToNext()) {
            try {
                val podp = Podpisant(
                    id_task = cur.getInt(cur.getColumnIndexOrThrow("id_task")),
                    fio = cur.getString(cur.getColumnIndexOrThrow("fio")),
                    email = cur.getString(cur.getColumnIndexOrThrow("e_mail")),
                    tel = cur.getString(cur.getColumnIndexOrThrow("tel")),
                    name_dolzhn = cur.getString(cur.getColumnIndexOrThrow("name_dolzhn")),
                    kod_emp = cur.getInt(cur.getColumnIndexOrThrow("kod_emp"))
                )
                arrPodpisant.add(podp)
            } catch (e: Exception) {
                println("$TAG_ERR getPodpisantList ${e.message}")
            }
        }

        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()

        return arrPodpisant
    }

    // Список актов, доступных для формирования
    // ----------------------------------------
    fun getActNamesList(idPurpose: Int): ArrayList<ActInfo> {
        val arrDocs = ArrayList<ActInfo>()
        val db = this.readableDatabase

        val cur =
            db.rawQuery(
                /* if (idPurpose == 0) "select distinct id_act, name, tip from MI_ACTS where id_act = 26"
                 else*/ "select id_purpose, id_act, name, tip from MI_ACTS where id_purpose = $idPurpose",
                null
            )
        while (cur.moveToNext()) {
            try {
                val doc = ActInfo(
                    id_purpose = if (idPurpose == 0) 0
                    else cur.getInt(cur.getColumnIndexOrThrow("id_purpose")),
                    id_act = cur.getInt(cur.getColumnIndexOrThrow("id_act")),
                    tip = cur.getInt(cur.getColumnIndexOrThrow("tip")),
                    name = cur.getString(cur.getColumnIndexOrThrow("name")) ?: ""
                )
                arrDocs.add(doc)

            } catch (e: Exception) {
                println("$TAG_ERR getActNamesList: ${e.message}")
                return arrDocs
            }
        }

        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()
        return arrDocs
    }

    // Список истории посещения по объекту
    // ------------------------------------------------
    fun getHistoryList(kod_obj: Int): ArrayList<HistoryItemInfo>? {
        val query = "select * from MI_HISTORY where kod_obj = $kod_obj order by ttime, dat desc"

        val db = this.readableDatabase
        val historyList = ArrayList<HistoryItemInfo>()
        val cur = db.rawQuery(query, null)
        if (cur.count == 0) {
            if (!cur.isClosed) cur.close()
            return null
        }

        while (cur.moveToNext()) {
            try {
                var time: String = cur.getString(cur.getColumnIndexOrThrow("ttime")).replace('T', ' ')
                if (time.length > 16) time = time.substring(0, 16)
                val obj = HistoryItemInfo(
                    dat = LocalDate.parse(cur.getString(cur.getColumnIndexOrThrow("dat"))),
                    id_task = cur.getInt(cur.getColumnIndexOrThrow("id_task")),
                    adr = cur.getString(cur.getColumnIndexOrThrow("adr")) ?: "",
                    purpose = cur.getInt(cur.getColumnIndexOrThrow("purpose")),
                    purpose_name = cur.getString(cur.getColumnIndexOrThrow("purpose_name")) ?: "",
                    prim = cur.getString(cur.getColumnIndexOrThrow("prim")) ?: "",
                    ttime = LocalDateTime.parse(time, DateTimeFormatter.ofPattern(patternTime)),
                    status = cur.getInt(cur.getColumnIndexOrThrow("status")),
                    status_name = cur.getString(cur.getColumnIndexOrThrow("status_name")) ?: "",
                    id_inspector = cur.getInt(cur.getColumnIndexOrThrow("id_inspector")),
                    fio = cur.getString(cur.getColumnIndexOrThrow("fio")) ?: "",
                    cnt_files = cur.getInt(cur.getColumnIndexOrThrow("cnt_files")),
                    kod_dog = cur.getInt(cur.getColumnIndexOrThrow("kod_dog")),
                    kod_obj = cur.getInt(cur.getColumnIndexOrThrow("kod_obj")),
                    kodp = cur.getInt(cur.getColumnIndexOrThrow("kodp")),
                    ndog = cur.getString(cur.getColumnIndexOrThrow("ndog")) ?: "",
                    payer_name = cur.getString(cur.getColumnIndexOrThrow("payer_name")) ?: "",
                    fio_contact = cur.getString(cur.getColumnIndexOrThrow("fio_contact")) ?: "",
                    email_contact = cur.getString(cur.getColumnIndexOrThrow("email_contact")) ?: "",
                    tel_contact = cur.getString(cur.getColumnIndexOrThrow("tel_contact")) ?: "",
                    fio_podp = cur.getString(cur.getColumnIndexOrThrow("fio_podp")) ?: "",
                    email_podp = cur.getString(cur.getColumnIndexOrThrow("email_podp")) ?: "",
                    tel_podp = cur.getString(cur.getColumnIndexOrThrow("tel_podp")) ?: "",
                    name_dolzhn_podp = cur.getString(cur.getColumnIndexOrThrow("name_dolzhn_podp")) ?: "",
                    kod_emp_podp = cur.getInt(cur.getColumnIndexOrThrow("kod_emp_podp"))
                )

                //  Проверяем наличие файлов для задачи на устройстве
                //  Это только если человек был на адресе в последние три дня
                /* if (obj.cnt_files == 0) {
                     val cntFiles = db.rawQuery(
                         "select count(id_file) as cnt_files from MI_MOB_TASK_FILES where id_task = ${obj.id_task}",
                         null
                     )
                     if (cntFiles.count == 0) {
                         if (!cntFiles.isClosed) cntFiles.close()
                     } else {
                         cntFiles.moveToFirst()
                         obj.cnt_files += cntFiles.getInt(cntFiles.getColumnIndexOrThrow("cnt_files"))
                         if (!cntFiles.isClosed) cntFiles.close()
                     }
                 }*/
                historyList.add(obj)
            } catch (e: java.lang.Exception) {
                println("$TAG_ERR getHistoryList: ${e.message}")
            }
        }

        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()
        return historyList
    }

    // Получение списка договоров по коду объекта
    // ------------------------------------------
    fun getNDogsByKodObj(kod_obj: Int): String {
        val db = this.readableDatabase
        val query = "select ifnull('', group_concat(distinct mmb.ndog)) as ndogs " +
                " from mi_history mmb " +
                " where  mmb.kod_obj = $kod_obj " +
                " order by mmb.kod_obj"
        var res = ""
        val cur = db.rawQuery(query, null)

        if (cur.moveToFirst()) {
            cur.moveToFirst()
            res = cur.getString(cur.getColumnIndexOrThrow("ndogs"))
        }

        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()

        return res
    }

    // Получить id file из сиквенса
    // ----------------------------------------
    fun getIdFileFromSequence(notInc: Boolean = false): Int {
        val db = this.readableDatabase
        val query = "select min(seq) as seq from sequences where name = 'SEQ_TASK_FILES'"
        val cur = db.rawQuery(query, null)
        var res = -1
        if (cur.moveToFirst()) {
            cur.moveToFirst()
            res = cur.getInt(cur.getColumnIndexOrThrow("seq"))
        }

        val dbWrite = DbHandlerLocalWrite(context, null)
        dbWrite.writableDatabase.execSQL(
            "UPDATE sequences SET seq = (SELECT (MIN(seq) - 1) FROM sequences WHERE name='SEQ_TASK_FILES') WHERE name='SEQ_TASK_FILES'"
        )
        dbWrite.close()

        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()
        return res
    }

    // Информация по договору теплоснабжения
    // ------------------------------------------------------
    fun getAbonentInfo(kod_dog: Int, id_task: Int): DogData {
        val db = this.readableDatabase
        val query = "select id_task,kodp, dat, kod_dog, name, inn, contact_string, ndog, dat_dog, " +
                "dog_har, nal_pu, last_nachisl, last_opl, sum_dolg_total, remark_dog, remark_rasch, " +
                " remark_ur, remark_kontrol, remark_tu,pusk_tu,otkl_tu  from MI_DOG_DATA " +
                " where id_task = $id_task and kod_dog = $kod_dog"
        val cur = db.rawQuery(query, null)
        var dogData = DogData()
        if (cur.count == 0)
            return dogData
        try {
            cur.moveToFirst()
            var datDog: String = cur.getString(cur.getColumnIndexOrThrow("dat_dog"))
            datDog = (datDog.replace('T', ' ')).replace('T', ' ')
            if (datDog.length > 16) datDog = datDog.substring(0, 16)

            if (cur.moveToFirst()) {
                cur.moveToFirst()
                dogData = DogData(
                    id_task = cur.getInt(cur.getColumnIndexOrThrow("id_task")),
                    kod_dog = cur.getInt(cur.getColumnIndexOrThrow("kod_dog")),
                    dat = LocalDate.parse(cur.getString(cur.getColumnIndexOrThrow("dat"))),
                    name = cur.getString(cur.getColumnIndexOrThrow("name")) ?: "",
                    inn = cur.getString(cur.getColumnIndexOrThrow("inn")) ?: "",
                    contact = cur.getString(cur.getColumnIndexOrThrow("contact_string")) ?: "", // контактные данные
                    ndog = cur.getString(cur.getColumnIndexOrThrow("ndog")) ?: "", // номер договора
                    dat_dog = LocalDateTime.parse(datDog, DateTimeFormatter.ofPattern(patternTime)), // дата договора
                    dog_har = cur.getString(cur.getColumnIndexOrThrow("dog_har"))
                        ?: "", // Договорные нагрузки строкой
                    nal_pu = cur.getInt(cur.getColumnIndexOrThrow("nal_pu")), // наличие ПУ
                    last_nachisl = cur.getString(cur.getColumnIndexOrThrow("last_nachisl"))
                        ?: "", //последние начисления
                    last_opl = cur.getString(cur.getColumnIndexOrThrow("last_opl")) ?: "",// последние оплаты
                    sum_dolg_total = cur.getString(cur.getColumnIndexOrThrow("sum_dolg_total")), //-- лицевая карта, сумма долга
                    remark_dog = cur.getString(cur.getColumnIndexOrThrow("remark_dog")) ?: "",
                    remark_rasch = cur.getString(cur.getColumnIndexOrThrow("remark_rasch")) ?: "",
                    remark_ur = cur.getString(cur.getColumnIndexOrThrow("remark_ur")) ?: "",
                    remark_kontrol = cur.getString(cur.getColumnIndexOrThrow("remark_kontrol")) ?: "",
                    remark_tu = cur.getString(cur.getColumnIndexOrThrow("remark_tu")) ?: "",
                    pusk_tu = cur.getString(cur.getColumnIndexOrThrow("pusk_tu")) ?: "",
                    otkl_tu = cur.getString(cur.getColumnIndexOrThrow("otkl_tu")) ?: "",
                    kodp = cur.getInt(cur.getColumnIndexOrThrow("kodp"))
                )
                try {
                    dogData.listDogObjects = getDogObjectList(id_task, kod_dog)
                } catch (e: Exception) {
                    println("$TAG_ERR getDogObjectList ${e.message}")
                }

                try {
                    dogData.listDogTu = getDogTuList(id_task, kod_dog)
                } catch (e: Exception) {
                    println("$TAG_ERR getDogTuList ${e.message}")
                }

                try {
                    dogData.listDogUu = getDogUuList(id_task, kod_dog)
                } catch (e: Exception) {
                    println("$TAG_ERR getDogUuList ${e.message}")
                }
                try {
                    if (dogData.listDogUu!!.size > 0) {
                        val uu = dogData.listDogUu!!.map { it.kod_uu }.distinct()
                        uu.forEach {
                            val si = getDogUuSiList(id_task, it)
                            if (si.size > 0) {
                                si.add(
                                    0, DogSiUu(id_task, "",
                                        "№ п/п", it.toString(),
                                        "Вид СИ",
                                        "Место фикс.",
                                        "Обозначение СИ",
                                        "Тип. марка",
                                        "Заводской номер",
                                        "Диаметр (мм)",
                                        "Принцип измерения",
                                        "Дата поверки",
                                        "Межпов. интер.",
                                        "Дата окончания поверки",
                                        "Период час.архив. (сут.)",
                                        "Период сут.архив. (сут.)",
                                        "№ в Гос. реестре",
                                        "Завод изготовитель",
                                        "Гидравл. под давл. (м в ст)",
                                        "Дата прек. действ. приб.",
                                        "Примечание"
                                    )
                                )
                                dogData.listDogUuSi!!.add(si)
                            }
                        }
                    }
                } catch (e: Exception) {
                    println("$TAG_ERR getDogUuList ${e.message}")
                }
            }

        } catch (e: java.lang.Exception) {
            println("$TAG_ERR getAbonentInfo ${e.message}")
        } finally {
            if (!cur.isClosed) cur.close()
            if (db != null && db.isOpen) db.close()
            return dogData
        }
    }

    // Список объектов по договору
    // ---------------------------------------------------------------------------
    private fun getDogObjectList(idTask: Int, kodDog: Int): ArrayList<DogObject> {
        val list = ArrayList<DogObject>()
        val query = "SELECT kod_dog, name, adr  FROM MI_DOG_OBJ  where kod_dog = $kodDog and id_task = $idTask"

        val db = this.readableDatabase
        val cur = db.rawQuery(query, null)

        while (cur.moveToNext()) {
            try {
                val obj = DogObject(
                    adr = cur.getString(cur.getColumnIndexOrThrow("adr")) ?: "",
                    name = cur.getString(cur.getColumnIndexOrThrow("name")) ?: "",
                    kod_dog = cur.getInt(cur.getColumnIndexOrThrow("kod_dog")),
                )
                list.add(obj)
            } catch (e: java.lang.Exception) {
                println("$TAG_ERR getDogObjectList ${e.message}")

            }
        }
        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()
        return list
    }

    // Список ТУ по договору
    // -------------------------------------------------------------------
    private fun getDogTuList(idTask: Int, kodDog: Int): ArrayList<DogTu> {
        val list = ArrayList<DogTu>()

        val query = "select kod_dog, kod_obj, nomer, name, so_q, so_g, sw_q, sw_g, st_q, st_g, gw_qmax, " +
                "gw_qsr, name_tarif from MI_DOG_TU where kod_dog = $kodDog and id_task = $idTask order by kod_obj"

        val db = this.readableDatabase
        val cur = db.rawQuery(query, null)

        while (cur.moveToNext()) {
            try {
                val tu = DogTu(
                    nomer = cur.getInt(cur.getColumnIndexOrThrow("nomer")),
                    name = cur.getString(cur.getColumnIndexOrThrow("name")) ?: "",
                    kod_dog = cur.getInt(cur.getColumnIndexOrThrow("kod_dog")),
                    so_g = cur.getDouble(cur.getColumnIndexOrThrow("so_g")),
                    so_q = cur.getDouble(cur.getColumnIndexOrThrow("so_q")),
                    sw_q = cur.getDouble(cur.getColumnIndexOrThrow("sw_q")),
                    sw_g = cur.getDouble(cur.getColumnIndexOrThrow("sw_g")),
                    st_q = cur.getDouble(cur.getColumnIndexOrThrow("st_q")),
                    st_g = cur.getDouble(cur.getColumnIndexOrThrow("st_g")),
                    gw_qmax = cur.getDouble(cur.getColumnIndexOrThrow("gw_qmax")),
                    gw_qsr = cur.getDouble(cur.getColumnIndexOrThrow("gw_qsr")),
                    name_tarif = cur.getString(cur.getColumnIndexOrThrow("name_tarif")) ?: "",
                )
                list.add(tu)
            } catch (e: java.lang.Exception) {
                println("$TAG_ERR getDogTuList ${e.message}")

            }
        }
        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()
        return list
    }

    // Список УУ по договору
    // -------------------------------------------------------------------
    private fun getDogUuList(idTask: Int, kodDog: Int): ArrayList<DogUu> {
        val list = ArrayList<DogUu>()
        val query = "SELECT kod_dog, kod_uu, name, mesto_uu, time_uu  FROM MI_DOG_UU " +
                " where kod_dog = $kodDog and id_task = $idTask order by kod_uu"

        val db = this.readableDatabase
        val cur = db.rawQuery(query, null)

        while (cur.moveToNext()) {
            try {
                val uu = DogUu(
                    kod_dog = cur.getInt(cur.getColumnIndexOrThrow("kod_dog")),
                    kod_uu = cur.getInt(cur.getColumnIndexOrThrow("kod_uu")),
                    name = cur.getString(cur.getColumnIndexOrThrow("name")) ?: "",
                    mesto_uu = cur.getString(cur.getColumnIndexOrThrow("mesto_uu")) ?: "",
                    time_uu = cur.getString(cur.getColumnIndexOrThrow("time_uu")) ?: "",
                )
                list.add(uu)
            } catch (e: java.lang.Exception) {
                println("$TAG_ERR getDogUuList ${e.message}")
            }
        }
        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()

        return list
    }

    // Список УУ СИ по договору
    // ---------------------------------------------------------------------
    private fun getDogUuSiList(idTask: Int, kodUu: Int): ArrayList<DogSiUu> {
        val list = ArrayList<DogSiUu>()
        val query = "SELECT * FROM MI_DOG_UU_SI where kod_uu = $kodUu and id_task =$idTask"

        val db = this.readableDatabase
        val cur = db.rawQuery(query, null)

        while (cur.moveToNext()) {
            try {
                val uu = DogSiUu(
                    id_task = cur.getInt(cur.getColumnIndexOrThrow("id_task")),
                    dat = cur.getString(cur.getColumnIndexOrThrow("dat")) ?: "",
                    npp = cur.getString(cur.getColumnIndexOrThrow("npp")) ?: "",
                    kod_uu = cur.getString(cur.getColumnIndexOrThrow("kod_uu")) ?: "",
                    name_si = cur.getString(cur.getColumnIndexOrThrow("name_si")) ?: "",
                    mesto = cur.getString(cur.getColumnIndexOrThrow("mesto")) ?: "",
                    obozn_t = cur.getString(cur.getColumnIndexOrThrow("obozn_t")) ?: "",
                    name_tip = cur.getString(cur.getColumnIndexOrThrow("name_tip")) ?: "",
                    nomer = cur.getString(cur.getColumnIndexOrThrow("nomer")) ?: "",
                    dim = cur.getString(cur.getColumnIndexOrThrow("dim")) ?: "",
                    izm = cur.getString(cur.getColumnIndexOrThrow("izm")) ?: "",
                    data_pov = cur.getString(cur.getColumnIndexOrThrow("data_pov")) ?: "",
                    int = cur.getString(cur.getColumnIndexOrThrow("int")) ?: "",
                    data_pov_end = cur.getString(cur.getColumnIndexOrThrow("data_pov_end")) ?: "",
                    per_chas_arx = cur.getString(cur.getColumnIndexOrThrow("per_chas_arx")) ?: "",
                    per_sut_arx = cur.getString(cur.getColumnIndexOrThrow("per_sut_arx")) ?: "",
                    n_greest = cur.getString(cur.getColumnIndexOrThrow("n_greest")) ?: "",
                    work = cur.getString(cur.getColumnIndexOrThrow("work")) ?: "",
                    loss_press = cur.getString(cur.getColumnIndexOrThrow("loss_press")) ?: "",
                    data_out = cur.getString(cur.getColumnIndexOrThrow("data_out")) ?: "",
                    prim = cur.getString(cur.getColumnIndexOrThrow("prim")) ?: ""
                )
                list.add(uu)
            } catch (e: java.lang.Exception) {
                println("$TAG_ERR getDogUuSiList ${e.message}")
            }
        }
        if (!cur.isClosed) cur.close()
        if (db != null && db.isOpen) db.close()

        return list
    }

    // Получение префикса для актов инспектора
    // ----------------------------------------
    fun getPrefAct(idInspector: Int): String {
        val query = "select NUM_ACT_PREF from MI_INSPECTOR_DATA where id_inspector = $idInspector"
        val db = this.readableDatabase
        val cur = db.rawQuery(query, null)
        var pref = ""
        try {
            if (cur.count != 0) {
                cur.moveToFirst()
                pref = cur.getString(cur.getColumnIndexOrThrow("NUM_ACT_PREF"))
            }
            if (!cur.isClosed) cur.close()

        } catch (e: Exception) {
            println("$TAG_ERR: ${e.message}")
        }
        return pref
    }
}