package ru.infoenergo.mis.dbhandler

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.fasterxml.jackson.databind.ObjectMapper
import ru.infoenergo.mis.helpers.*
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList

/** ************************************** **/
/**      Запись данных в локальную БД      **/
/**  (с сервера и созданные пользователем) **/
/** ************************************** **/

class DbHandlerLocalWrite(
    var context: Context, factory: SQLiteDatabase.CursorFactory?
) : DbHandlerLocal(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // Update Акт подписан, меняем статус на is_signed = 1
    // ---------------------------------------------------
    fun updateActSigned(idTask: Int, idAct: Int, npp: Int, idFile: Int, email: String): Boolean {
        var res = true
        val dbWrite = this.writableDatabase
        val cv = ContentValues()
        cv.put("IS_SIGNED", 1)
        try {
            val s = dbWrite.update("MI_ACT_FIELDS", cv, "ID_TASK = $idTask AND ID_ACT = $idAct and npp = $npp", null)
            res = res && s != 0
        } catch (e: Exception) {
            println("$TAG_ERR Не удалось UPDATE MI_ACT_FIELDS IS_SIGNED: ${e.message}")
        }
        try {
            if (idAct in arrayOf(17, 24, 25)) {
                val s1 =
                    dbWrite.update(
                        "MI_ACT_FIELDS_DOP",
                        cv,
                        "ID_TASK = $idTask AND ID_ACT = $idAct and npp = $npp",
                        null
                    )
                res = res && s1 != 0
            }
        } catch (e: Exception) {
            println("$TAG_ERR Не удалось UPDATE MI_ACT_FIELDS_DOP IS_SIGNED: ${e.message}")
        }
        try {
            cv.put("email_client", email)
            cv.put("date_send_to_client", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            val s2 = dbWrite.update("MI_MOB_TASK_FILES", cv, "ID_TASK = $idTask AND ID_FILE = $idFile", null)
            res = res && s2 != 0
        } catch (e: Exception) {
            println("$TAG_ERR Не удалось UPDATE MI_MOB_TASK_FILES IS_SIGNED: ${e.message}")
        }
        dbWrite.close()
        // пока что пусть всегда true будет
        res = true
        return res
    }

    // Insert Список задач с сервера (json string)
    // *******************************************************
    fun insertTaskListFromServer(json: String) {
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)
        // rootNode.count()
        //val node = mapper.valueToTree<JsonNode>(json)

        val dbWrite = this.writableDatabase
        var cnt = 0

        val elements = rootNode.elements()
        println("$TAG_OK запись задачи начата")
        while (elements.hasNext()) {
            val element = elements.next()

            try {
                val cv = ContentValues()
                val date = LocalDate.parse(element.get("DAT")!!.asText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                val time = LocalDateTime.parse(
                    element.get("TTIME").asText(),
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                )

                cv.put("DAT", date!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                cv.put("ID_TASK", element.get("ID_TASK").asInt())
                cv.put("ADR", element.get("ADR")!!.asText()!!.replace("null", ""))
                cv.put("PURPOSE", element.get("PURPOSE")!!.asInt())
                cv.put("PURPOSE_NAME", element.get("PURPOSE_NAME")!!.asText()!!.replace("null", ""))
                cv.put("PRIM", element.get("PRIM")!!.asText()!!.replace("null", ""))
                cv.put("TTIME", time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                cv.put("STATUS", element.get("STATUS")!!.asInt())
                cv.put("STATUS_NAME", element.get("STATUS_NAME")!!.asText()!!.replace("null", ""))
                cv.put("ID_INSPECTOR", element.get("ID_INSPECTOR").asInt())
                cv.put("FIO_INSPECTOR", element.get("FIO")!!.asText()!!.replace("null", ""))
                cv.put("KOD_OBJ", element.get("KOD_OBJ")!!.asInt())
                cv.put("KOD_DOG", element.get("KOD_DOG")!!.asInt())
                cv.put("KODP", element.get("KODP")!!.asInt())
                cv.put("NDOG", element.get("NDOG")!!.asText()!!.replace("null", ""))
                cv.put("PAYER_NAME", element.get("PAYER_NAME")!!.asText()!!.replace("null", ""))
                cv.put("FIO_CONTACT", element.get("FIO_CONTACT")!!.asText()!!.replace("null", ""))
                cv.put("EMAIL_CONTACT", element.get("EMAIL_CONTACT")!!.asText()!!.replace("null", ""))
                cv.put("TEL_CONTACT", element.get("TEL_CONTACT")!!.asText()!!.replace("null", ""))
                cv.put("LAT", element.get("LAT")!!.asText()!!.replace("null", ""))
                cv.put("LAN", element.get("LAN")!!.asText()!!.replace("null", ""))
                cv.put("SCHEMA_ZULU", element.get("SCHEMA_ZULU")!!.asText()!!.replace("null", ""))
                cv.put("BORDER_ZULU", element.get("BORDER_ZULU")!!.asText()!!.replace("null", ""))
                cv.put("KOD_EMP_PODP", element.get("KOD_EMP_PODP")!!.asInt())
                cv.put("FIO_PODP", element.get("FIO_PODP")!!.asText()!!.replace("null", ""))
                cv.put("EMAIL_PODP", element.get("EMAIL_PODP")!!.asText()!!.replace("null", ""))
                cv.put("TEL_PODP", element.get("TEL_PODP")!!.asText()!!.replace("null", ""))
                cv.put("NAME_DOLZHN_PODP", element.get("NAME_DOLZHN_PODP")!!.asText()!!.replace("null", ""))

                val res = dbWrite.insertOrThrow("MI_MOB_TASK", null, cv).toInt()
                if (res != -1) {
                    DBHandlerServerWrite(context).acceptTask(element.get("ID_TASK").asInt())
                    cnt += 1
                } else {
                    println("$TAG_OK Не удалось записать задачу ${element.get("ID_TASK").asInt()}")
                    DBHandlerServerWrite(context).refuseTask(element.get("ID_TASK").asInt())
                }
            } catch (e: Exception) {
                println("CATCH Не удалось записать задачу ${element.get("ID_TASK").asInt()}: ${e.message}")
                DBHandlerServerWrite(context).refuseTask(element.get("ID_TASK").asInt())
            }
        }
        println("$TAG_OK запись задачи окончена $cnt")

        if (dbWrite != null && dbWrite.isOpen) dbWrite.close()
    }

    // Insert Список задач с сервера (ArrayList)
    // *******************************************************
    fun insertTaskListFromServer(taskList: ArrayList<Task>) {
        println("$TAG_OK запись задачи начата")
        for (task in taskList) {
            insertTask(task)
        }
        println("$TAG_OK запись задачи окончена ${taskList.size}")
    }

    // Insert 1 (одной) задачи в локальную базу с сервера
    // *******************************************************
    private fun insertTask(task: Task): Boolean {
        var res = true
        val dbWrite = this.writableDatabase
        try {
            val cv = ContentValues()

            cv.put("DAT", task.dat.toString())
            cv.put("ID_TASK", task.id_task)
            cv.put("ADR", task.address)
            cv.put("PURPOSE", task.purpose)
            cv.put("PURPOSE_NAME", task.purpose_name)
            cv.put("PRIM", task.prim)
            cv.put("TTIME", task.ttime.toString())
            cv.put("STATUS", task.status)
            cv.put("STATUS_NAME", task.status_name)
            cv.put("ID_INSPECTOR", task.id_inspector)
            cv.put("FIO_INSPECTOR", task.fio)
            cv.put("KOD_OBJ", task.kod_obj)
            cv.put("KOD_NUMOBJ", task.kod_numobj)
            cv.put("KOD_DOG", task.kod_dog)
            cv.put("KODP", task.kodp)
            cv.put("NDOG", task.ndog)
            cv.put("PAYER_NAME", task.payer_name)
            cv.put("FIO_CONTACT", task.fio_contact)
            cv.put("EMAIL_CONTACT", task.email_contact)
            cv.put("TEL_CONTACT", task.tel_contact)
            cv.put("LAT", task.lat)
            cv.put("LAN", task.lan)
            cv.put("SCHEMA_ZULU", task.schema_zulu)
            cv.put("BORDER_ZULU", task.border_zulu)
            cv.put("KOD_EMP_PODP", task.kod_emp_podp)
            cv.put("FIO_PODP", task.fio_podp)
            cv.put("EMAIL_PODP", task.email_podp)
            cv.put("TEL_PODP", task.tel_podp)
            cv.put("NAME_DOLZHN_PODP", task.name_dolzhn_podp)

            res = dbWrite.insertOrThrow("MI_MOB_TASK", null, cv) != (-1).toLong()
            if (res) DBHandlerServerWrite(context).acceptTask(task.id_task)
        } catch (e: Exception) {
            res = false
            println("$TAG_ERR Не удалось записать задачу ${task.id_task}: ${e.message}")
            DBHandlerServerWrite(context).refuseTask(task.id_task)
        } finally {
            dbWrite.close()
            return res
        }
    }

    // Insert Файлы к задачам с сервера (ArrayList) (без blob вложения)
    // ****************************************************************
    fun insertFilesFromServer(files: ArrayList<FileInfo>, history: Boolean = false): Boolean {
        var result = true
        for (file in files) {
            result = insertFileWithoutBlob(file, history)
            if (!result)
                //println("$TAG_OK blob ${file.id_file} ${if (history) "history" else ""} задачи ${file.id_task} записан")
            //else
                println("$TAG_OK blob ${file.id_file} ${if (history) "history" else ""} задачи ${file.id_task} НЕ записан")

        }
        return result
    }

    // Insert 1 (одного) файла к задаче с сервера (без blob вложения)
    // **************************************************************
    private fun insertFileWithoutBlob(file: FileInfo, history: Boolean = false): Boolean {
        var result: Boolean
        val dbWrite = this.writableDatabase
        try {
            val cv = ContentValues()

            cv.put("ID_TASK", file.id_task)
            cv.put("ID_FILE", file.id_file)
            cv.put("FILENAME", file.filename)
            cv.put("IS_SIGNED", file.is_signed)
            cv.put("PAPER", file.paper)

            if (file.uri != null)
                cv.put("URI", file.uri.toString())

            result = if (history)
                dbWrite.insertOrThrow("MI_MOB_TASK_FILES_HISTORY", null, cv) != (-1).toLong()
            else
                dbWrite.insertOrThrow("MI_MOB_TASK_FILES", null, cv) != (-1).toLong()


        } catch (e: Exception) {
            result = false
            println("$TAG_ERR insertTaskFilesFromServer: ${e.message}")
        }
        dbWrite.close()

        return result
    }

    // Insert Id инспектора по логину и паролю
    // *****************************************************
    fun insertIdInspectorFromServer(json: String, pswd: String): Boolean {
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)

        var result = true
        val dbWrite = this.writableDatabase

        val elements = rootNode.elements()
        if (elements.hasNext()) {
            val element = elements.next()
            try {

                val cv = ContentValues()

                cv.put("ID_INSPECTOR", element["ID_INSPECTOR"]!!.asInt())
                cv.put("FIO", element["FIO"]!!.asText().replace("null", ""))
                cv.put("PUSER", element["PUSER"]!!.asText().replace("null", ""))
                cv.put("MIN_TASK_ID", element["MIN_TASK_ID"]!!.asInt())
                cv.put("NUM_ACT_PREF", element["NUM_ACT_PREF"]!!.asText().replace("null", ""))
                cv.put("VERS", element["VERS"]!!.asText().replace("null", ""))
                cv.put("PSWD", pswd)

                dbWrite.insertOrThrow("MI_INSPECTOR_DATA", null, cv)
            } catch (e: Exception) {
                result = false
                println("$TAG_ERR insert MI_INSPECTOR_DATA: ${e.message}")
            }
        }

        dbWrite.close()
        println("$TAG_OK информация об инспекторе записана")
        return result
    }

    // Update ФИО инспектора и min_id_task по id
    // Если ФИО пусто, то не update
    // ************************************************************************
    fun updateIdInspector(
        idInspector: Int, minTaskId: Int, pswd: String = "",
        pref: String = "", fio: String = "", version: String = ""
    ): Boolean {
        var result = true
        val dbWrite = this.writableDatabase

        try {
            val cv = ContentValues()

            cv.put("MIN_TASK_ID", minTaskId)
            if (version.isNotEmpty())
                cv.put("VERS", fio.replace("null", ""))
            if (fio.isNotEmpty())
                cv.put("FIO", fio.replace("null", ""))
            if (pswd.isNotEmpty())
                cv.put("PSWD", pswd)
            if (pref.isNotEmpty())
                cv.put("NUM_ACT_PREF", pref)

            dbWrite.update("MI_INSPECTOR_DATA", cv, "ID_INSPECTOR = $idInspector", null)
        } catch (e: Exception) {
            result = false
            println("$TAG_ERR insert MI_INSPECTOR_DATA: ${e.message}")
        }

        dbWrite.close()
        println("$TAG_OK информация об инспекторе обновлена")
        return result
    }

    // Insert Справочник целей, полученный с сервера
    // *************************************************
    fun insertPurposeFromServer(json: String): Boolean {
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)

        var result = true
        val dbWrite = this.writableDatabase

        var cnt = 0
        val elements = rootNode.elements()
        while (elements.hasNext()) {
            val element = elements.next()
            try {

                val cv = ContentValues()

                cv.put("PURPOSE", element["PURPOSE"]!!.asInt())
                cv.put("NAME", element["NAME"].asText()!!.replace("null", ""))

                val ins = dbWrite.insertOrThrow("MI_PURPOSE", null, cv).toInt()
                if (ins != -1)
                    cnt += 1

            } catch (e: Exception) {
                result = false
                println("$TAG_ERR insertPurpose (${element.get("PURPOSE")!!.asInt()}): ${e.message}")
            }
        }
        /*if (rootNode.size() == 0)
        {
            dbWrite.execSQL("insert into MI_PURPOSE (PURPOSE, NAME) values (1, 'технический аудит')")
            dbWrite.execSQL("insert into MI_PURPOSE (PURPOSE, NAME) values (2, 'присоединение')")
            dbWrite.execSQL("insert into MI_PURPOSE (PURPOSE, NAME) values (3, 'контроль ПУ')")
        }*/
        dbWrite.close()
        println("$TAG_OK запись цели окончена $cnt")
        return result
    }

    // Insert Список доступных к заданиям актов
    // **************************************************
    fun insertTaskActsFromServer(json: String): Boolean {
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)
        var result = true

        val dbWrite = this.writableDatabase
        var cnt = 0

        val elements = rootNode.elements()
        while (elements.hasNext()) {
            val element = elements.next()
            try {
                val cv = ContentValues()
                cv.put("ID_PURPOSE", element.get("PURPOSE").asInt())
                cv.put("ID_ACT", element.get("ID_ACT")!!.asInt())
                cv.put("TIP", element.get("TIP")!!.asInt())
                cv.put("NAME", element.get("NAME")!!.asText()!!.replace("null", ""))

                val r = dbWrite.insertOrThrow("MI_ACTS", null, cv)
                result = r != (-1).toLong()
                if (result) cnt += 1
            } catch (e: Exception) {
                result = false
                println(
                    "$TAG_ERR insertAct (PURPOSE: ${element.get("PURPOSE")!!.asInt()}, " +
                            "ID_ACT: ${element.get("ID_ACT")!!.asInt()}): ${e.message}"
                )
            }
        }
        dbWrite.close()
        println("$TAG_OK запись акты окончена $cnt")
        return result
    }

    // Insert Список актов к заданию со всеми полями  (input: json string)
    // (запись, если акта не было в базе, т.е. здесь пустые значения допустимы)
    // ************************************************************************
    fun insertActsFieldsFromServer(json: String) {
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)

        val dbWrite = this.writableDatabase
        var cnt = 0

        val elements = rootNode.elements()
        while (elements.hasNext()) {
            val element = elements.next()
            try {
                val cv = ContentValues()
                val date = LocalDate.parse(element.get("DAT")!!.asText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                cv.put("DAT", date!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                cv.put("ID_TASK", element["ID_TASK"].asInt())
                cv.put("ID_ACT", element.get("ID_ACT")!!.asInt())
                cv.put("KODP", element.get("KODP")!!.asInt())
                cv.put("KOD_DOG", element.get("KOD_DOG")!!.asInt())
                cv.put("KOD_OBJ", element.get("KOD_OBJ")!!.asInt())

                cv.put("NUM_ACT", element.get("NUM_ACT")!!.asText()!!.replace("null", ""))
                cv.put("DAT_ACT", element.get("DAT_ACT")!!.asText()!!.replace("null", ""))
                cv.put("PAYER_NAME", element.get("PAYER_NAME")!!.asText()!!.replace("null", ""))
                cv.put("ADR_ORG", element.get("ADR_ORG")!!.asText()!!.replace("null", ""))
                cv.put("FIO_CONTACT", element.get("FIO_CONTACT")!!.asText()!!.replace("null", ""))
                cv.put("TEL_CONTACT", element.get("TEL_CONTACT")!!.asText()!!.replace("null", ""))
                cv.put("FILIAL_ESO", element.get("FILIAL_ESO")!!.asText()!!.replace("null", ""))
                cv.put("FIO_ESO", element.get("FIO_ESO")!!.asText()!!.replace("null", ""))
                cv.put("TEL_ESO", element.get("TEL_ESO")!!.asText()!!.replace("null", ""))
                cv.put("LIST_OBJ", element.get("LIST_OBJ")!!.asText()!!.replace("null", ""))
                cv.put("NAME_OBJ", element.get("NAME_OBJ")!!.asText()!!.replace("null", ""))
                cv.put("NUM_OBJ", element.get("NUM_OBJ")!!.asText()!!.replace("null", ""))
                cv.put("ADR_OBJ", element.get("ADR_OBJ")!!.asText()!!.replace("null", ""))
                cv.put("NDOG", element.get("NDOG")!!.asText()!!.replace("null", ""))
                cv.put("DAT_DOG", element.get("DAT_DOG")!!.asText()!!.replace("null", ""))
                cv.put("OTOP_PERIOD", element.get("OTOP_PERIOD")!!.asText()!!.replace("null", ""))
                cv.put("SUM_DOLG", element.get("SUM_DOLG")!!.asText()!!.replace("null", ""))
                cv.put("REMARK_DOG", element.get("REMARK_DOG")!!.asText()!!.replace("null", ""))
                cv.put("NAL_PODP_DOC", element.get("NAL_PODP_DOC")!!.asText()!!.replace("null", ""))
                cv.put("OPL_CALCUL", element.get("OPL_CALCUL")!!.asText()!!.replace("null", ""))
                cv.put("OSNOV", element.get("OSNOV")!!.asText()!!.replace("null", ""))
                cv.put("CITY", element.get("CITY")!!.asText()!!.replace("null", ""))
                cv.put("SHABLON", element.get("SHABLON")!!.asText()!!.replace("null", ""))
                cv.put("NAME_DOLZHN_CONTACT", element.get("NAME_DOLZHN_CONTACT")!!.asText()!!.replace("null", ""))
                cv.put("NAME_DOLZHN_ESO", element.get("NAME_DOLZHN_ESO")!!.asText()!!.replace("null", ""))
                cv.put("PERIOD_DOLG", element.get("PERIOD_DOLG")!!.asText()!!.replace("null", ""))
                cv.put("NAME_ST", element.get("NAME_ST")!!.asText()!!.replace("null", ""))
                cv.put("NAME_MAG", element.get("NAME_MAG")!!.asText()!!.replace("null", ""))
                cv.put("NAME_TK", element.get("NAME_TK")!!.asText()!!.replace("null", ""))
                cv.put("NAME_AW", element.get("NAME_AW")!!.asText()!!.replace("null", ""))
                cv.put("INN_ORG", element.get("INN_ORG")!!.asText()!!.replace("null", ""))
                cv.put("KPP_ORG", element.get("KPP_ORG")!!.asText()!!.replace("null", ""))
                cv.put("NAZN_NAME", element.get("NAZN_NAME")!!.asText()!!.replace("null", ""))
                cv.put("NAL_SO", element.get("NAL_SO")!!.asText()!!.replace("null", ""))
                cv.put("NAL_SW", element.get("NAL_SW")!!.asText()!!.replace("null", ""))
                cv.put("NAL_ST", element.get("NAL_ST")!!.asText()!!.replace("null", ""))
                cv.put("NAL_GV", element.get("NAL_GV")!!.asText()!!.replace("null", ""))
                cv.put("DIRECTOR_TATENERGO", element.get("DIRECTOR_TATENERGO")!!.asText()!!.replace("null", ""))
                cv.put("VOLUME_OBJ", element.get("VOLUME_OBJ")!!.asText()!!.replace("null", ""))
                cv.put("SO_Q", element.get("SO_Q")!!.asText()!!.replace("null", ""))
                cv.put("SW_Q", element.get("SW_Q")!!.asText()!!.replace("null", ""))
                cv.put("GW_Q", element.get("GW_Q")!!.asText()!!.replace("null", ""))
                cv.put("ST_Q", element.get("ST_Q")!!.asText()!!.replace("null", ""))
                cv.put("Q_SUM", element.get("Q_SUM")!!.asText()!!.replace("null", ""))
                cv.put("NAL_ACT_GIDRO", element.get("NAL_ACT_GIDRO")!!.asText()!!.replace("null", ""))
                cv.put("FILIAL_ADDRESS", ((element.get("FILIAL_ADDRESS"))!!.asText())!!.replace("null", ""))
                cv.put("FILIAL_TEL", ((element.get("FILIAL_TEL"))!!.asText())!!.replace("null", ""))
                cv.put("DIRECTOR_T_DOVER_NUM", ((element.get("DIRECTOR_T_DOVER_NUM"))!!.asText())!!.replace("null", ""))

                val ins = dbWrite.insertOrThrow("MI_ACT_FIELDS_SHABLON", null, cv).toInt()
                if (ins != -1)
                    cnt += 1
            } catch (e: Exception) {
                dbWrite.close()
                println(
                    "$TAG_ERR insertActFields (ID_TASK: ${
                        element.get("ID_TASK").asInt()
                    }, ID_ACT: ${element.get("ID_ACT")!!.asInt()}): ${e.message}"
                )
            }
        }
        dbWrite.close()
        println("$TAG_OK запись поля акты окончена $cnt")
    }

    // Insert Данные по актам для шаблонной таблицы  (input: ArrayList)
    // *******************************************************************************
    fun insertActsFieldsFromServer(actsFieldsList: java.util.ArrayList<ActFieldsInfo>) {
        val dbWrite = this.writableDatabase
        for (actFields in actsFieldsList) {
            if (actFields.is_signed == 1) {
                println("$TAG_OK Акт уже подписан ${actFields.id_act} (задача ${actFields.id_task})")
            }
            try {
                val cv = putCvActFields(actFields, fromServer = true, update = false)
                val res = dbWrite.insert("MI_ACT_FIELDS_SHABLON", null, cv) != (-1).toLong()
                if (!res) {
                    println("$TAG_OK Не удалось записать акт ${actFields.id_act} (задача ${actFields.id_task})")
                }
            } catch (e: Exception) {
                println("$TAG_ERR Не удалось записать акт ${actFields.id_act} (задача ${actFields.id_task}): ${e.message}")
            }
        }
        dbWrite.close()
    }

    // Insert Данные по актам для шаблонной таблицы  (input: ArrayList)
    // ----------------------------------------------------------------
    fun insertActsFieldsShablon(actFields: ActFieldsInfo) {
        val dbWrite = this.writableDatabase
        try {
            val cv = putCvActFields(actFields, fromServer = true, update = false)
            val res = dbWrite.insert("MI_ACT_FIELDS_SHABLON", null, cv).toInt() != -1
            if (!res) {
                println("$TAG_OK Не удалось записать акт ${actFields.id_act} (задача ${actFields.id_task})")
            }
        } catch (e: Exception) {
            println("$TAG_ERR Не удалось записать акт ${actFields.id_act} (задача ${actFields.id_task}): ${e.message}")
        }
        dbWrite.close()
    }

    // Insert полей 1 акта к заданию
    // запись не подписанных актов, пустые поля не перезаписываются
    // ------------------------------------------------------------
    fun insertActFields(actFields: ActFieldsInfo): Boolean {
        val dbWrite = this.writableDatabase
        var res = false
        /* if (actFields.is_signed == 1) {
             println("$TAG_OK Не удалось записать акт ${actFields.id_act}/${actFields.npp} (задача ${actFields.id_task}): акт уже подписан")
         }*/

        if (actFields.npp == 0) {
            actFields.npp =
                if (DbHandlerLocalRead(context, null).getLastActNpp(actFields.id_task, actFields.id_act) == 0)
                    1 else 0
        }

        return try {
            val cv = putCvActFields(actFields, fromServer = false, update = false)
            res = dbWrite.insert("MI_ACT_FIELDS", null, cv) != (-1).toLong()
            if (!res) {
                println("$TAG_OK Не удалось записать акт ${actFields.id_act}/${actFields.npp} (задача ${actFields.id_task})")
            }
            dbWrite.close()
            res
        } catch (e: Exception) {
            println("$TAG_ERR Не удалось записать акт ${actFields.id_act}/${actFields.npp} (задача ${actFields.id_task}): ${e.message}")
            dbWrite.close()
            res
        }
    }

    // Update полей для 1 (одного) акта  (input: ActFieldsInfo)
    // **************************************************************************
    fun updateActFields(actFields: ActFieldsInfo, fromServer: Boolean): Boolean {
        if (actFields.is_signed == 1) {
            println("$TAG_OK Не удалось обновить акт ${actFields.id_act} (задача ${actFields.id_task}): акт уже подписан")
            return false
        }
        val dbWrite = this.writableDatabase
        return try {
            val cv = putCvActFields(actFields, fromServer, update = true)

            val res = if (fromServer) {
                dbWrite.update(
                    "MI_ACT_FIELDS_SHABLON", cv,
                    "id_act = ${actFields.id_act} and id_task = ${actFields.id_task}",
                    null
                )
            } else {
                dbWrite.update(
                    "MI_ACT_FIELDS", cv,
                    "id_act = ${actFields.id_act} and id_task = ${actFields.id_task} and npp = ${actFields.npp}",
                    null
                )
            }

            if (res == -1)
                println("$TAG_OK Не удалось обновить акт ${actFields.id_act} (задача ${actFields.id_task})")

            dbWrite.close()
            res != -1
        } catch (e: Exception) {
            dbWrite.close()
            println("$TAG_ERR Не удалось обновить акт ${actFields.id_act} (задача ${actFields.id_task}): ${e.message}")
            false
        }
    }

    // Update поля idFile для 1 (одного) акта  (input: ActFieldsInfo)
    // **************************************************************************
    fun updateActFieldsIdFile(idTask: Int, idAct: Int, npp: Int, idFile: Int): Boolean {
        val dbWrite = this.writableDatabase
        return try {
            val cv = ContentValues()
            cv.put("ID_FILE", idFile)

            val res =
                dbWrite.update(
                    "MI_ACT_FIELDS", cv,
                    "id_act = $idAct and id_task = $idTask and npp = $npp",
                    null
                )

            if (res == -1)
                println("$TAG_OK Не удалось обновить акт $idAct (задача $idTask)")

            dbWrite.close()
            res != -1
        } catch (e: Exception) {
            dbWrite.close()
            println("$TAG_ERR Не удалось обновить акт $idAct (задача $idTask): ${e.message}")
            false
        }
    }

    // Формирование contentValues для полей акта
    // ----------------------------------------------------------------------------------------------------------------
    private fun putCvActFields(actFields: ActFieldsInfo, fromServer: Boolean, update: Boolean = false): ContentValues {
        val cv = ContentValues()

        cv.put("DAT", actFields.dat.toString())

        // при апдейте не будем перезаписывать эти поля
        if (!update) {
            cv.put("ID_TASK", actFields.id_task)
            cv.put("ID_ACT", actFields.id_act)
        }

        cv.put("KODP", actFields.kodp)
        cv.put("KOD_DOG", actFields.kod_dog)
        cv.put("KOD_OBJ", actFields.kod_obj)
        cv.put("NUM_ACT", actFields.num_act)
        cv.put("DAT_ACT", actFields.dat_act)
        cv.put("PAYER_NAME", actFields.payer_name)
        cv.put("ADR_ORG", actFields.adr_org)
        cv.put("FIO_CONTACT", actFields.fio_contact)
        cv.put("TEL_CONTACT", actFields.tel_contact)
        cv.put("FILIAL_ESO", actFields.filial_eso)
        cv.put("FIO_ESO", actFields.fio_eso)
        cv.put("TEL_ESO", actFields.tel_eso)
        cv.put("LIST_OBJ", actFields.list_obj)
        cv.put("NAME_OBJ", actFields.name_obj)
        cv.put("NUM_OBJ", actFields.num_obj)
        cv.put("ADR_OBJ", actFields.adr_obj)
        cv.put("NDOG", actFields.ndog)
        cv.put("DAT_DOG", actFields.dat_dog)
        cv.put("OTOP_PERIOD", actFields.otop_period)
        cv.put("SUM_DOLG", actFields.sum_dolg)
        cv.put("REMARK_DOG", actFields.remark_dog)
        cv.put("NAL_PODP_DOC", actFields.nal_podp_doc)
        cv.put("OPL_CALCUL", actFields.opl_calcul)
        cv.put("OSNOV", actFields.osnov)
        cv.put("CITY", actFields.city)
        cv.put("SHABLON", actFields.shablon)
        cv.put("NAME_DOLZHN_CONTACT", actFields.name_dolzhn_contact)
        cv.put("NAME_DOLZHN_ESO", actFields.name_dolzhn_eso)
        cv.put("PERIOD_DOLG", actFields.period_dolg)
        cv.put("NAME_ST", actFields.name_st)
        cv.put("NAME_MAG", actFields.name_mag)
        cv.put("NAME_TK", actFields.name_tk)
        cv.put("NAME_AW", actFields.name_aw)
        cv.put("INN_ORG", actFields.inn_org)
        cv.put("KPP_ORG", actFields.kpp_org)
        cv.put("NAZN_NAME", actFields.nazn_name)
        cv.put("NAL_SO", actFields.nal_so)
        cv.put("NAL_SW", actFields.nal_sw)
        cv.put("NAL_ST", actFields.nal_st)
        cv.put("NAL_GV", actFields.nal_gv)
        cv.put("director_tatenergo", actFields.director_tatenergo)
        cv.put("volume_obj", actFields.volume_obj)
        cv.put("so_q", actFields.so_q)
        cv.put("sw_q", actFields.sw_q)
        cv.put("st_q", actFields.st_q)
        cv.put("gw_q", actFields.gw_q)
        cv.put("q_sum", actFields.q_sum)
        cv.put("nal_act_gidro", actFields.nal_act_gidro)
        cv.put("filial_address", actFields.filial_address)
        cv.put("filial_tel", actFields.filial_tel)
        cv.put("director_t_dover_num", actFields.director_t_dover_num)

        if (fromServer) return cv

        cv.put("NPP", actFields.npp)
        cv.put("ID_FILE", actFields.id_file)
        cv.put("square_obj", actFields.square_obj)

        cv.put("itog_text", actFields.itog_text)
        cv.put("PURPOSE_TEXT", actFields.purpose_text)
        cv.put("podgotovka", actFields.podgotovka)

        if (actFields.id_act in arrayOf(16, 26, 27) || actFields.id_act in 1..13) return cv

        // Акт проверки филиалом готовности абонента к отопительному периоду ActOtopPeriod
        if (actFields.id_act == 14) {
            cv.put("id_manometr", actFields.id_manometr)
            cv.put("id_aupr_so", actFields.id_aupr_so)
            cv.put("id_aupr_sw", actFields.id_aupr_sw)
            cv.put("id_aupr_gvs", actFields.id_aupr_gvs)
            cv.put("id_aupr_sv", actFields.id_aupr_sv)
            cv.put("sost_kip_str", actFields.sost_kip_str)
            cv.put("id_sost_tube", actFields.id_sost_tube)
            cv.put("id_sost_armatur", actFields.id_sost_armatur)
            cv.put("id_sost_izol", actFields.id_sost_izol)
            cv.put("sost_tube_str", actFields.sost_tube_str)
            cv.put("id_sost_net", actFields.id_sost_net)
            cv.put("sost_net_str", actFields.sost_net_str)
            cv.put("id_sost_utepl", actFields.id_sost_utepl)
            cv.put("sost_utepl_str", actFields.sost_utepl_str)
            cv.put("id_nal_pasport", actFields.id_nal_pasport)
            cv.put("id_nal_schema", actFields.id_nal_schema)
            cv.put("id_nal_instr", actFields.id_nal_instr)
            cv.put("nal_pasp_str", actFields.nal_pasp_str)
            cv.put("id_nal_direct_connect", actFields.id_nal_direct_connect)
            cv.put("nal_direct_connect", actFields.nal_direct_connect)
            cv.put("dop_info", actFields.dop_info)
            return cv
        }

        // Акт проверки готовности (по Приказу 103) ActOtopPeriod103
        if (actFields.id_act == 15) {
            cv.put("comiss_post_gotov", actFields.comiss_post_gotov)
            cv.put("pred_comiss", actFields.pred_comiss)
            cv.put("zam_pred_gkh", actFields.zam_pred_gkh)
            cv.put("podpisi", actFields.podpisi)
            cv.put("dop_info", actFields.dop_info)
            return cv
        }

        // Акт о готовности внутриплощадочных и внутридомовых сетей
        if (actFields.id_act == 17) {
            cv.put("nal_document", actFields.nal_document)
            cv.put("zayavitel", actFields.zayavitel)
            cv.put("zayavitel_dover", actFields.zayavitel_dover)
            cv.put("podgotovka", actFields.podgotovka)
            cv.put("podgotovka_proj_num", actFields.podgotovka_proj_num)
            cv.put("podgotovka_proj_ispoln", actFields.podgotovka_proj_ispoln)
            cv.put("podgotovka_proj_utvergden", actFields.podgotovka_proj_utvergden)

            cv.put("net_inner_teplonositel", actFields.net_inner_teplonositel)
            cv.put("net_inner_dp", actFields.net_inner_dp)
            cv.put("net_inner_do", actFields.net_inner_do)
            cv.put("net_inner_tip_kanal", actFields.net_inner_tip_kanal)
            cv.put("net_inner_tube_type_p", actFields.net_inner_tube_type_p)
            cv.put("net_inner_tube_type_o", actFields.net_inner_tube_type_o)
            cv.put("net_inner_l", actFields.net_inner_l)
            cv.put("net_inner_l_undeground", actFields.net_inner_l_undeground)
            cv.put("net_inner_otstuplenie", actFields.net_inner_otstuplenie)
            cv.put("energo_effect_object", actFields.energo_effect_object)
            cv.put("nal_rezerv_istochnik", actFields.nal_rezerv_istochnik)
            cv.put("nal_svyazi", actFields.nal_svyazi)

            cv.put("vid_connect_system", actFields.vid_connect_system)
            cv.put("elevator_num", actFields.elevator_num)
            cv.put("elevator_diam", actFields.elevator_diam)
            cv.put("podogrev_otop_num", actFields.podogrev_otop_num)
            cv.put("podogrev_otop_kolvo_sekc", actFields.podogrev_otop_kolvo_sekc)
            cv.put("podogrev_otop_l_sekc", actFields.podogrev_otop_l_sekc)
            cv.put("podogrev_otop_nazn", actFields.podogrev_otop_nazn)
            cv.put("podogrev_otop_marka", actFields.podogrev_otop_marka)
            cv.put("d_napor_patrubok", actFields.d_napor_patrubok)
            cv.put("power_electro_engine", actFields.power_electro_engine)
            cv.put("chastota_vr_engine", actFields.chastota_vr_engine)
            cv.put("drossel_diafragma_d", actFields.drossel_diafragma_d)
            cv.put("drossel_diafragma_mesto", actFields.drossel_diafragma_mesto)
            cv.put("drossel_diafragma_tip_otop", actFields.drossel_diafragma_tip_otop)
            cv.put("drossel_diafragma_cnt_stoyak", actFields.drossel_diafragma_cnt_stoyak)
            cv.put("type_oto_prib", actFields.type_oto_prib)
            cv.put("schema_vkl_gvs", actFields.schema_vkl_gvs)
            cv.put("schema_vkl_podogrev", actFields.schema_vkl_podogrev)
            cv.put("kolvo_sekc_1", actFields.kolvo_sekc_1)
            cv.put("kolvo_sekc_1_l", actFields.kolvo_sekc_1_l)
            cv.put("kolvo_sekc_2", actFields.kolvo_sekc_2)
            cv.put("kolvo_sekc_2_l", actFields.kolvo_sekc_2_l)
            cv.put("kolvo_kalorifer", actFields.kolvo_kalorifer)
            cv.put("poverhnost_nagreva", actFields.poverhnost_nagreva)
            cv.put("mesto_karta", actFields.mesto_karta)
            cv.put("dop_info", actFields.dop_info)

            return cv
        }

        // Акт о подключении к системе теплоснабжения
        if (actFields.id_act == 18) {
            cv.put("zayavitel", actFields.zayavitel)
            cv.put("zayavitel_dover", actFields.zayavitel_dover)
            cv.put("podgotovka", actFields.podgotovka)
            cv.put("q_max", actFields.q_max)

            cv.put("mesto_karta", actFields.mesto_karta)
            cv.put("pu_data", actFields.pu_data)
            cv.put("pu_pover_lico", actFields.pu_pover_lico)
            cv.put("pu_pover_pokaz", actFields.pu_pover_pokaz)
            cv.put("pu_pover_rez", actFields.pu_pover_rez)
            cv.put("balans_prinadl_obj", actFields.balans_prinadl_obj)
            cv.put("balans_prin_dop", actFields.balans_prin_dop)
            cv.put("gr_ekspl_otvetst", actFields.gr_ekspl_otvetst)
            cv.put("gr_ekspl_otvetst_dop", actFields.gr_ekspl_otvetst_dop)
            cv.put("st_podkl_rub", actFields.st_podkl_rub)
            cv.put("st_podkl_rub_nds", actFields.st_podkl_rub_nds)
            cv.put("podkl_dop_sved", actFields.podkl_dop_sved)

            return cv
        }

        // Акт после ограничения, отключения
        if (actFields.id_act == 19) {
            cv.put("dop_info", actFields.dop_info)
            cv.put("act_poluchil", actFields.act_poluchil)
            cv.put("uvedoml_otkl_num", actFields.uvedoml_otkl_num)
            cv.put("uvedoml_otkl_date", actFields.uvedoml_otkl_date)
            cv.put("pred_pogash_dolg_num", actFields.pred_pogash_dolg_num)
            cv.put("pred_pogash_dolg_date", actFields.pred_pogash_dolg_date)
            cv.put("otkl_proizv", actFields.otkl_proizv)
            cv.put("otkl_pu_pokaz_do", actFields.otkl_pu_pokaz_do)
            cv.put("otkl_pu_pokaz_posle", actFields.otkl_pu_pokaz_posle)
            return cv
        }

        // Акт об отказе в доступе к теплоустановкам
        if (actFields.id_act == 20) {
            cv.put("act_poluchil", actFields.act_poluchil)
            cv.put("uvedoml_otkl_num", actFields.uvedoml_otkl_num)
            cv.put("uvedoml_otkl_date", actFields.uvedoml_otkl_date)
            cv.put("prichina_otkaza", actFields.prichina_otkaza)
            cv.put("otkaz_svidet_1", actFields.otkaz_svidet_1)
            cv.put("otkaz_svidet_2", actFields.otkaz_svidet_2)
            return cv
        }

        // Акт бездоговорного потребления
        if (actFields.id_act == 23) {
            cv.put("pravo_sobstv", actFields.pravo_sobstv)
            cv.put("uvedom_aktirov_num", actFields.uvedom_aktirov_num)
            cv.put("uvedom_aktirov_date", actFields.uvedom_aktirov_date)
            cv.put("nal_pu", actFields.nal_pu)
            cv.put("nal_aupr", actFields.nal_aupr)
            cv.put("bezdog_sposob_num", actFields.bezdog_sposob_num)
            cv.put("bezdog_ustanovleno", actFields.bezdog_ustanovleno)
            cv.put("bezdog_narushenie", actFields.bezdog_narushenie)
            cv.put("bezdog_pereraschet_s", actFields.bezdog_pereraschet_s)
            cv.put("bezdog_pereraschet_po", actFields.bezdog_pereraschet_po)
            cv.put("bezdog_predpis", actFields.bezdog_predpis)
            cv.put("bezdog_obyasn", actFields.bezdog_obyasn)
            cv.put("bezdog_pretenz", actFields.bezdog_pretenz)
            cv.put("otkaz_svidet_1", actFields.otkaz_svidet_1)
            cv.put("otkaz_svidet_2", actFields.otkaz_svidet_2)
            cv.put("predst_potrebit_dover", actFields.predst_potrebit_dover)
            cv.put("dop_info", actFields.dop_info)
            return cv
        }

        // Акт допуска узла учета
        if (actFields.id_act in arrayOf(24, 25)) {
            cv.put("pu_data", actFields.pu_data)
            cv.put("dopusk_s", actFields.dopusk_s)
            cv.put("dopusk_po", actFields.dopusk_po)
            cv.put("tel_spravki", actFields.tel_spravki)
            cv.put("tel_dispetch", actFields.tel_dispetch)
            cv.put("org_ustanov_pu", actFields.org_ustanov_pu)
            cv.put("dop_info", actFields.dop_info)
            return cv
        }

        if (actFields.id_act > 27) {
            cv.put("uslovie_podkl_num", actFields.uslovie_podkl_num)
            cv.put("soglasop_proekte_num", actFields.soglasov_proekte_num)
            cv.put("filial_name", actFields.filial_name)
            cv.put("ispolnitel", actFields.ispolnitel)
            cv.put("dog_podkl_num", actFields.dog_podkl_num)
            cv.put("dog_podkl_date", actFields.dog_podkl_date)
            cv.put("predst_potrebit", actFields.predst_potrebit)
            cv.put("podkl_num", actFields.podkl_num)
            cv.put("fact_text", actFields.fact_text)
        }
        return cv
    }

    // Insert Доп поля для заполнения акта (input: json string)
    // (дополнительная информация для некоторых актов)
    // **********************************************
    fun insertActsFieldsDopFromServer(json: String) {
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)


        val dbWrite = this.writableDatabase

        val elements = rootNode.elements()
        while (elements.hasNext()) {
            val element = elements.next()
            try {
                val cv = ContentValues()
                val date = LocalDate.parse(element.get("DAT")!!.asText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))

                cv.put("DAT", date!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                cv.put("ID_TASK", element.get("ID_TASK").asInt())
                cv.put("ID_ACT", element.get("ID_ACT")!!.asInt())
                cv.put("NUM_OBJ", element.get("NUM_OBJ")!!.asInt())
                cv.put("NAME_OBJ", ((element.get("NAME_OBJ"))!!.asText())!!.replace("null", ""))
                cv.put("ADDRESS_OBJ", ((element.get("ADDRESS_OBJ"))!!.asText())!!.replace("null", ""))
                cv.put("GOD_ZD", ((element.get("GOD_ZD"))!!.asText())!!.replace("null", ""))
                cv.put("NAZN_NAME", ((element.get("NAZN_NAME"))!!.asText())!!.replace("null", ""))
                cv.put("TVR", ((element.get("TVR"))!!.asText())!!.replace("null", ""))
                cv.put("PR_OTO", ((element.get("PR_OTO"))!!.asText())!!.replace("null", ""))
                cv.put("PR_SW", ((element.get("PR_SW"))!!.asText())!!.replace("null", ""))
                cv.put("VOLUME", element.get("VOLUME")!!.asInt())
                cv.put("SQUARE_TOTAL", element.get("SQUARE_TOTAL")!!.asInt())
                cv.put("POINT_NAME", ((element.get("POINT_NAME"))!!.asText())!!.replace("null", ""))
                cv.put("ETAZ", ((element.get("ETAZ"))!!.asText())!!.replace("null", ""))
                cv.put("SO_Q", element.get("SO_Q")!!.asInt())
                cv.put("SW_Q", element.get("SW_Q")!!.asInt())
                cv.put("ST_Q", element.get("ST_Q")!!.asInt())
                cv.put("GW_QMAX", element.get("GW_QMAX")!!.asInt())
                cv.put("NAME_VODO", ((element.get("NAME_VODO"))!!.asText())!!.replace("null", ""))
                cv.put("NN_WPOL", element.get("NN_WPOL")!!.asInt())
                cv.put("TT_WPOL", element.get("TT_WPOL")!!.asInt())
                cv.put("NN_PRIB", element.get("NN_PRIB")!!.asInt())
                cv.put("PR_REC", ((element.get("PR_REC"))!!.asText())!!.replace("null", ""))
                cv.put("PR_PSUSCH", ((element.get("PR_PSUSCH"))!!.asText())!!.replace("null", ""))
                cv.put("PR_IZ_ST", ((element.get("PR_IZ_ST"))!!.asText())!!.replace("null", ""))
                cv.put("NOM_UCH", ((element.get("NOM_UCH"))!!.asText())!!.replace("null", ""))
                cv.put("TIP_NAME", ((element.get("TIP_NAME"))!!.asText())!!.replace("null", ""))
                cv.put("PT_D", element.get("PT_D")!!.asInt())
                cv.put("PT_L", element.get("PT_L")!!.asInt())
                cv.put("NAME_PR_PT", ((element.get("NAME_PR_PT"))!!.asText())!!.replace("null", ""))
                cv.put("OT_D", element.get("OT_D")!!.asInt())
                cv.put("OT_L", element.get("OT_L")!!.asInt())
                cv.put("NAME_PR_OT", ((element.get("NAME_PR_OT"))!!.asText())!!.replace("null", ""))
                cv.put("UCH_HGR", ((element.get("UCH_HGR"))!!.asText())!!.replace("null", ""))
                cv.put("PU_NUM", ((element.get("PU_NUM"))!!.asText())!!.replace("null", ""))
                cv.put("PU_NAME", ((element.get("PU_NAME"))!!.asText())!!.replace("null", ""))
                cv.put("PU_MESTO", ((element.get("PU_MESTO"))!!.asText())!!.replace("null", ""))
                cv.put("PU_TYPE", ((element.get("PU_TYPE"))!!.asText())!!.replace("null", ""))
                cv.put("PU_DIAM", ((element.get("PU_DIAM"))!!.asText())!!.replace("null", ""))
                cv.put("PU_KOLVO", ((element.get("PU_KOLVO"))!!.asText())!!.replace("null", ""))
                cv.put("PU_PROBA_MESTO", ((element.get("PU_PROBA_MESTO"))!!.asText())!!.replace("null", ""))
                cv.put("Q_SUM", ((element.get("Q_SUM"))!!.asText())!!.replace("null", ""))
                cv.put("Q_SUM_MAX", ((element.get("Q_SUM_MAX"))!!.asText())!!.replace("null", ""))
                cv.put("PU_SROK_POVERKI", ((element.get("PU_SROK_POVERKI"))!!.asText())!!.replace("null", ""))
                cv.put("PU_NUM_PLOMBA", ((element.get("PU_NUM_PLOMBA"))!!.asText())!!.replace("null", ""))
                cv.put("PU_POKAZ", ((element.get("PU_POKAZ"))!!.asText())!!.replace("null", ""))
                cv.put("SCHEMA_PRISOED_NAME", ((element.get("SCHEMA_PRISOED_NAME"))!!.asText())!!.replace("null", ""))
                cv.put("SCHEMA_PRISOED_KOD", ((element.get("SCHEMA_PRISOED_KOD"))!!.asText())!!.replace("null", ""))

                dbWrite.insertOrThrow("MI_ACT_FIELDS_DOP_SHABLON", null, cv)

            } catch (e: Exception) {
                println(
                    "$TAG_ERR insertActFieldsDop (ID_TASK: ${
                        element.get("ID_TASK").asInt()
                    }, ID_ACT: ${element.get("ID_ACT")!!.asInt()}): ${e.message}"
                )
            }
        }
        dbWrite.close()
    }

    // Insert Список актов к заданию со всеми полями  (input:  ArrayList)
    // запись не подписанных актов, пустые поля не перезаписываются
    // **********************************************************************************
    fun insertActsFieldsDopFromServer(fieldsDop: java.util.ArrayList<ActFieldsDopInfo>) {
        for (dop in fieldsDop) {
            insertActFieldsDopShablon(dop)
        }
        println("$TAG_OK запись доп поля акты окончена")
    }

    // Insert Доп полей для 1 (одного) акта  (input: ActFieldsDopInfo)
    // ***************************************************************
    private fun insertActFieldsDopShablon(dop: ActFieldsDopInfo) {
        val dbWrite = this.writableDatabase
        try {
            val cv = putCvActFieldsDop(dop, shablon = true)

            dbWrite.insertOrThrow("MI_ACT_FIELDS_DOP_SHABLON", null, cv)
        } catch (e: Exception) {
            println("$TAG_ERR Не удалось записать доп поля акт ${dop.id_act} (задача ${dop.id_task}): ${e.message}")
        }
        dbWrite.close()
    }

    // Update Доп полей для 1 (одного) акта  (input: ActFieldsDopInfo)
    // ***************************************************************
    fun updateActFieldsDopShablon(dop: ActFieldsDopInfo) {
        val dbWrite = this.writableDatabase
        try {
            val cv = putCvActFieldsDop(dop, shablon = true)

            val res = dbWrite.update(
                "MI_ACT_FIELDS_DOP_SHABLON",
                cv,
                "id_act = ${dop.id_act} and id_task = ${dop.id_task}",
                null
            )
            res
        } catch (e: Exception) {
            println("$TAG_ERR Не удалось записать доп поля акт ${dop.id_act} (задача ${dop.id_task}): ${e.message}")
        }
        dbWrite.close()
    }

    // Delete Доп полей для 1 (одного) акта  (input: ActFieldsDopInfo)
    // ***************************************************************
    fun deleteActFieldsDop(idTask: Int, idAct: Int, npp: Int) {
        val dbWrite = this.writableDatabase
        try {
            dbWrite.delete(
                "MI_ACT_FIELDS_DOP",
                "id_task = $idTask and id_act = $idAct and npp = $npp", null
            )
        } catch (e: Exception) {
            println("$TAG_ERR Не удалось удалить доп поля акт $idAct/$npp (задача $idTask): ${e.message}")
        }
        dbWrite.close()
    }

    // Insert Доп полей для 1 (одного) акта  (input: ActFieldsDopInfo)
    // ***************************************************************
    fun insertActFieldsDop(dop: ActFieldsDopInfo): Int {
        val dbWrite = this.writableDatabase
        return try {
            val cv = putCvActFieldsDop(dop, shablon = false)
            val res = dbWrite.insert("MI_ACT_FIELDS_DOP", null, cv).toInt()
            dbWrite.close()
            res
        } catch (e: Exception) {
            println("$TAG_ERR Не удалось записать доп поля акт ${dop.id_act} (задача ${dop.id_task}): ${e.message}")
            dbWrite.close()
            -1
        }
    }

    // Формирование contentValues для доп полей акта
    // ------------------------------------------------------------------------------------
    private fun putCvActFieldsDop(dop: ActFieldsDopInfo, shablon: Boolean): ContentValues {
        val cv = ContentValues()

        cv.put("ID_TASK", dop.id_task)
        cv.put("ID_ACT", dop.id_act)
        cv.put("NUM_OBJ", dop.num_obj)

        if (!shablon) {
            cv.put("NPP", dop.npp)
            cv.put("ID_FILE", dop.id_file)
        }
        cv.put("DAT", dop.dat.toString())
        cv.put("NAME_OBJ", dop.name_obj)
        cv.put("ADDRESS_OBJ", dop.address_obj)
        cv.put("GOD_ZD", dop.god_zd)
        cv.put("NAZN_NAME", dop.nazn_name)
        cv.put("TVR", dop.tvr)
        cv.put("PR_OTO", dop.pr_oto)
        cv.put("PR_SW", dop.pr_sw)
        cv.put("VOLUME", dop.volume)
        cv.put("SQUARE_TOTAL", dop.square_total)
        cv.put("POINT_NAME", dop.point_name)
        cv.put("ETAZ", dop.etaz)
        cv.put("SO_Q", dop.so_q)
        cv.put("SW_Q", dop.sw_q)
        cv.put("ST_Q", dop.st_q)
        cv.put("GW_QMAX", dop.gw_qmax)
        cv.put("NAME_VODO", dop.name_vodo)
        cv.put("NN_WPOL", dop.nn_wpol)
        cv.put("TT_WPOL", dop.tt_wpol)
        cv.put("NN_PRIB", dop.nn_prib)
        cv.put("PR_REC", dop.pr_rec)
        cv.put("PR_PSUSCH", dop.pr_psusch)
        cv.put("PR_IZ_ST", dop.pr_iz_st)
        cv.put("NOM_UCH", dop.nom_uch)
        cv.put("TIP_NAME", dop.tip_name)
        cv.put("PT_D", dop.pt_d)
        cv.put("PT_L", dop.pt_l)
        cv.put("NAME_PR_PT", dop.name_pr_pt)
        cv.put("OT_D", dop.ot_d)
        cv.put("OT_L", dop.ot_l)
        cv.put("NAME_PR_OT", dop.name_pr_ot)
        cv.put("UCH_HGR", dop.uch_hgr)
        cv.put("PU_NUM", dop.pu_num)
        cv.put("PU_NAME", dop.pu_name)
        cv.put("PU_MESTO", dop.pu_mesto)
        cv.put("PU_TYPE", dop.pu_type)
        cv.put("PU_DIAM", dop.pu_diam)
        cv.put("PU_KOLVO", dop.pu_kolvo)
        cv.put("PU_PROBA_MESTO", dop.pu_proba_mesto)
        cv.put("Q_SUM", dop.q_sum)
        cv.put("Q_SUM_MAX", dop.q_sum_max)
        cv.put("PU_SROK_POVERKI", dop.pu_srok_poverki)
        cv.put("PU_NUM_PLOMBA", dop.pu_num_plomba)
        cv.put("PU_POKAZ", dop.pu_pokaz)
        cv.put("SCHEMA_PRISOED_NAME", dop.schema_prisoed_name)
        cv.put("SCHEMA_PRISOED_KOD", dop.schema_prisoed_kod)
        return cv
    }

    // Insert История посещений
    // *************************************************
    fun insertHistoryFromServer(json: String): Boolean {
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)
        var result = true
        val dbWrite = this.writableDatabase
        var cnt = 0

        val elements = rootNode.elements()
        while (elements.hasNext()) {
            val element = elements.next()
            try {
                val cv = ContentValues()
                val date = LocalDate.parse(element.get("DAT")!!.asText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                val time = LocalDateTime.parse(
                    element.get("TTIME").asText(),
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                )
                cv.put("DAT", date!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                cv.put("ID_TASK", element.get("ID_TASK").asInt())
                cv.put("ADR", element.get("ADR")!!.asText()!!.replace("null", ""))
                cv.put("PURPOSE", element.get("PURPOSE")!!.asInt())
                cv.put("PURPOSE_NAME", element.get("PURPOSE_NAME")!!.asText()!!.replace("null", ""))
                cv.put("PRIM", element.get("PRIM")!!.asText()!!.replace("null", ""))
                cv.put("TTIME", time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                cv.put("STATUS", element.get("STATUS")!!.asInt())
                cv.put("STATUS_NAME", element.get("STATUS_NAME")!!.asText()!!.replace("null", ""))
                cv.put("ID_INSPECTOR", element.get("ID_INSPECTOR").asInt())
                cv.put("FIO", element.get("FIO")!!.asText()!!.replace("null", ""))
                cv.put("CNT_FILES", element.get("CNT_FILES")!!.asInt())
                cv.put("KOD_OBJ", element.get("KOD_OBJ")!!.asInt())
                cv.put("KOD_DOG", element.get("KOD_DOG")!!.asInt())
                cv.put("KODP", element.get("KODP")!!.asInt())
                cv.put("NDOG", element.get("NDOG")!!.asText()!!.replace("null", ""))
                cv.put("PAYER_NAME", element.get("PAYER_NAME")!!.asText()!!.replace("null", ""))
                cv.put("FIO_CONTACT", element.get("FIO_CONTACT")!!.asText()!!.replace("null", ""))
                cv.put("EMAIL_CONTACT", element.get("EMAIL_CONTACT")!!.asText()!!.replace("null", ""))
                cv.put("TEL_CONTACT", element.get("TEL_CONTACT")!!.asText()!!.replace("null", ""))
                cv.put("KOD_EMP_PODP", element.get("KOD_EMP_PODP")!!.asInt())
                cv.put("FIO_PODP", element.get("FIO_PODP")!!.asText()!!.replace("null", ""))
                cv.put("EMAIL_PODP", element.get("EMAIL_PODP")!!.asText()!!.replace("null", ""))
                cv.put("TEL_PODP", element.get("TEL_PODP")!!.asText()!!.replace("null", ""))
                cv.put("NAME_DOLZHN_PODP", element.get("NAME_DOLZHN_PODP")!!.asText()!!.replace("null", ""))

                val ins = dbWrite.insertOrThrow("MI_HISTORY", null, cv).toInt()
                if (ins != -1)
                    cnt += 1

            } catch (e: Exception) {
                println(
                    "$TAG_ERR insertHistory(ID_TASK ${
                        element.get("ID_TASK").asInt()
                    }, KOD_DOG ${element.get("KOD_DOG")!!.asInt()}): ${e.message}"
                )
                result = false
            }
        }
        dbWrite.close()
        println("$TAG_OK запись истории окончена $cnt")
        return result
    }

    // Insert Данные для окна информация по договору
    // *************************************************
    fun insertDogDataFromServer(json: String): Boolean {
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)
        var result = true

        val dbWrite = this.writableDatabase
        var cnt = 0

        val elements = rootNode.elements()
        while (elements.hasNext()) {
            try {
                val element = elements.next()

                val cv = ContentValues()

                val date = LocalDate.parse(element.get("DAT")!!.asText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))

                cv.put("DAT", date!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                cv.put("ID_TASK", element.get("ID_TASK").asInt())
                cv.put("KOD_DOG", element.get("KOD_DOG")!!.asInt())
                cv.put("DAT_DOG", element.get("DAT_DOG").asText())
                cv.put("KODP", element.get("KODP")!!.asInt())
                cv.put("NAME", element.get("NAME")!!.asText()!!.replace("null", ""))
                cv.put("INN", element.get("INN")!!.asText()!!.replace("null", ""))
                cv.put("CONTACT_STRING", element.get("CONTACT_STRING")!!.asText()!!.replace("null", ""))
                cv.put("NDOG", element.get("NDOG")!!.asText()!!.replace("null", ""))
                cv.put("DOG_HAR", element.get("DOG_HAR")!!.asText()!!.replace("null", ""))
                cv.put("NAL_PU", element.get("NAL_PU")!!.asInt())
                cv.put("LAST_NACHISL", element.get("LAST_NACHISL")!!.asText()!!.replace("null", ""))
                cv.put("LAST_OPL", element.get("LAST_OPL")!!.asText()!!.replace("null", ""))
                cv.put("SUM_DOLG_TOTAL", element.get("SUM_DOLG_TOTAL")!!.asInt())
                cv.put("REMARK_DOG", element.get("REMARK_DOG")!!.asText()!!.replace("null", ""))
                cv.put("REMARK_RASCH", element.get("REMARK_RASCH")!!.asText()!!.replace("null", ""))
                cv.put("REMARK_UR", element.get("REMARK_UR")!!.asText()!!.replace("null", ""))
                cv.put("REMARK_KONTROL", element.get("REMARK_KONTROL")!!.asText()!!.replace("null", ""))
                cv.put("REMARK_TU", element.get("REMARK_TU")!!.asText()!!.replace("null", ""))
                cv.put("PUSK_TU", element.get("PUSK_TU")!!.asText()!!.replace("null", ""))
                cv.put("OTKL_TU", element.get("OTKL_TU")!!.asText()!!.replace("null", ""))

                if (dbWrite.insertOrThrow("MI_DOG_DATA", null, cv).toInt() != -1)
                    cnt += 1

            } catch (e: Exception) {
                println("$TAG_ERR insertDogDataFromServer: ${e.message}")
                result = false
            }
        }
        dbWrite.close()
        println("$TAG_OK запись договоры окончена $cnt")
        return result
    }

    // Insert Данные для окна информация по договору (объекты)
    // *******************************************************
    fun insertDogObjFromServer(json: String): Boolean {
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)
        var result = true

        val dbWrite = this.writableDatabase
        var cnt = 0

        val elements = rootNode.elements()
        while (elements.hasNext()) {
            try {
                val element = elements.next()

                val cv = ContentValues()
                val date = LocalDate.parse(element.get("DAT")!!.asText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))

                cv.put("ID_TASK", element.get("ID_TASK").asInt())
                cv.put("KOD_DOG", element.get("KOD_DOG")!!.asInt())
                cv.put("KOD_OBJ", element.get("KOD_OBJ")!!.asInt())
                cv.put("DAT", date!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                cv.put("NAME", element.get("NAME")!!.asText()!!.replace("null", ""))
                cv.put("ADR", element.get("ADR")!!.asText()!!.replace("null", ""))

                val ins = dbWrite.insertOrThrow("MI_DOG_OBJ", null, cv).toInt()
                if (ins != -1)
                    cnt = +1

            } catch (e: Exception) {
                result = false
                println("$TAG_ERR insertDogObjFromServer: ${e.message}")
            }
        }
        dbWrite.close()
        println("$TAG_OK запись объектов договоров окончена $cnt")
        return result
    }

    // Insert Данные для окна информация по договору (точки учёта)
    // ***********************************************************
    fun insertDogTuFromServer(json: String): Boolean {
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)
        var result = true

        val dbWrite = this.writableDatabase
        var cnt = 0

        val elements = rootNode.elements()
        while (elements.hasNext()) {
            val element = elements.next()
            try {
                val cv = ContentValues()

                cv.put("ID_TASK", element.get("ID_TASK").asInt())
                cv.put("KOD_DOG", element.get("KOD_DOG")!!.asInt())
                cv.put("KOD_OBJ", element.get("KOD_OBJ")!!.asInt())
                cv.put("NOMER", element.get("NOMER")!!.asInt())
                cv.put("NAME", element.get("NAME")!!.asText()!!.replace("null", ""))
                cv.put("SO_Q", element.get("SO_Q").asDouble())
                cv.put("SO_G", element.get("SO_G").asDouble())
                cv.put("SW_Q", element.get("SW_Q").asDouble())
                cv.put("SW_G", element.get("SW_G").asDouble())
                cv.put("ST_Q", element.get("ST_Q").asDouble())
                cv.put("ST_G", element.get("ST_G").asDouble())
                cv.put("GW_QMAX", element.get("GW_QMAX").asDouble())
                cv.put("GW_QSR", element.get("GW_QSR").asDouble())
                cv.put("NAME_TARIF", element.get("NAME_TARIF")!!.asText()!!.replace("null", ""))

                val ins = dbWrite.insertOrThrow("MI_DOG_TU", null, cv).toInt()
                if (ins != -1)
                    cnt = +1

            } catch (e: Exception) {
                println(
                    "$TAG_ERR insertDogTuFromServer (KOD_DOG ${element.get("KOD_DOG")!!.asInt()}," +
                            " KOD_OBJ ${element.get("KOD_OBJ")!!.asInt()}): ${e.message}"
                )
                result = false
            }
        }
        dbWrite.close()
        println("$TAG_OK запись ТУ договоры окончена $cnt")
        return result
    }

    // Insert Данные для окна информация по договору (узлы учёта)
    // **********************************************************
    fun insertDogUuFromServer(json: String): Boolean {
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)
        var result = true

        val dbWrite = this.writableDatabase
        var cnt = 0

        val elements = rootNode.elements()
        while (elements.hasNext()) {
            try {
                val element = elements.next()
                val cv = ContentValues()
                val date = LocalDate.parse(element.get("DAT")!!.asText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))

                cv.put("ID_TASK", element.get("ID_TASK")!!.asInt())
                cv.put("DAT", date!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                cv.put("KOD_UU", element.get("KOD_UU")!!.asInt())
                cv.put("NAME", element.get("NAME")!!.asText()!!.replace("null", ""))
                cv.put("KOD_DOG", element.get("KOD_DOG")!!.asInt())
                cv.put("MESTO_UU", element.get("MESTO_UU")!!.asText()!!.replace("null", ""))
                cv.put("TIME_UU", element.get("TIME_UU")!!.asText())

                val ins = dbWrite.insertOrThrow("MI_DOG_UU", null, cv).toInt()
                if (ins != -1)
                    cnt += 1

            } catch (e: Exception) {
                println("$TAG_ERR insertDogUuFromServer: ${e.message}")
                result = false
            }
        }
        dbWrite.close()
        println("$TAG_OK запись УУ договоры окончена $cnt")
        return result
    }

    // Insert Данные для окна информация по договору (узлы учёта, средства измерения)
    // ******************************************************************************
    fun insertDogUuSiFromServer(json: String): Boolean {
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)
        var result = true

        val dbWrite = this.writableDatabase
        var cnt = 0

        val elements = rootNode.elements()
        while (elements.hasNext()) {
            try {
                val element = elements.next()
                val cv = ContentValues()

                val date = LocalDate.parse(element.get("DAT")!!.asText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                cv.put("DAT", date!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))

                var d = element.get("DATA_POV")!!.asText().replace("null", "")
                val dataPov = if (d.isNotEmpty()) d.take(10) else ""
                d = element.get("DATA_POV_END")!!.asText().replace("null", "")
                val dataPovEnd = if (d.isNotEmpty()) d.take(10) else ""
                d = element.get("DATA_OUT")!!.asText().replace("null", "")
                val dataOut = if (d.isNotEmpty()) d.take(10) else ""

                cv.put("ID_TASK", element.get("ID_TASK")!!.asInt())
                cv.put("NPP", element.get("NPP")!!.asInt())
                cv.put("KOD_UU", element.get("KOD_UU")!!.asInt())
                cv.put("NAME_SI", element.get("NAME_SI")!!.asText()!!.replace("null", ""))
                cv.put("MESTO", element.get("MESTO")!!.asText()!!.replace("null", ""))
                cv.put("OBOZN_T", element.get("OBOZN_T")!!.asText()!!.replace("null", ""))
                cv.put("NAME_TIP", element.get("NAME_TIP")!!.asText()!!.replace("null", ""))
                cv.put("NOMER", element.get("NOMER")!!.asText()!!.replace("null", ""))
                cv.put("DIM", element.get("DIM")!!.asInt())
                cv.put("IZM", element.get("IZM")!!.asText()!!.replace("null", ""))
                if (dataPov.isNotEmpty()) cv.put("DATA_POV", dataPov)
                cv.put("INT", element.get("INT")!!.asInt())
                if (dataPovEnd.isNotEmpty()) cv.put("DATA_POV_END", dataPovEnd)
                cv.put("PER_CHAS_ARX", element.get("PER_CHAS_ARX")!!.asDouble())
                cv.put("PER_SUT_ARX", element.get("PER_SUT_ARX")!!.asDouble())
                cv.put("N_GREEST", element.get("N_GREEST")!!.asText()!!.replace("null", ""))
                cv.put("WORK", element.get("WORK")!!.asText()!!.replace("null", ""))
                cv.put("LOSS_PRESS", element.get("LOSS_PRESS")!!.asDouble())
                if (dataOut.isNotEmpty()) cv.put("DATA_OUT", dataOut)
                cv.put("PRIM", element.get("PRIM")!!.asText().replace("null", ""))

                val ins = dbWrite.insertOrThrow("MI_DOG_UU_SI", null, cv).toInt()
                if (ins != -1)
                    cnt += 1

            } catch (e: Exception) {
                println("$TAG_ERR insertDogUuSiFromServer: ${e.message}")
                result = false
            }
        }
        dbWrite.close()
        println("$TAG_OK запись УУ СИ договоры окончена $cnt")
        return result
    }

    // Insert Подписанты
    // *************************************************
    fun insertPodpisantFromServer(json: String): Boolean {
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)
        var result = true

        val dbWrite = this.writableDatabase
        var cnt = 0

        var idTask = 0
        val elements = rootNode.elements()
        while (elements.hasNext()) {
            try {
                val element = elements.next()

                val cv = ContentValues()

                cv.put("ID_TASK", element.get("ID_TASK").asInt())
                cv.put("FIO", element.get("FIO")!!.asText()!!.replace("null", ""))
                cv.put("E_MAIL", element.get("E_MAIL")!!.asText()!!.replace("null", ""))
                cv.put("TEL", element.get("TEL")!!.asText()!!.replace("null", ""))
                cv.put("NAME_DOLZHN", element.get("NAME_DOLZHN")!!.asText()!!.replace("null", ""))
                cv.put("KOD_EMP", element.get("KOD_EMP")!!.asInt())

                if (idTask != element.get("ID_TASK").asInt()) {
                    idTask = element.get("ID_TASK").asInt()
                    dbWrite.delete("MI_PODPISANT", "id_task = ${element.get("ID_TASK")}", null)
                }
                if (dbWrite.insertOrThrow("MI_PODPISANT", null, cv).toInt() != -1)
                    cnt += 1

            } catch (e: Exception) {
                println("$TAG_ERR insertPodpisantFromServer: ${e.message} ")
                result = false
            }
        }
        dbWrite.close()
        println("$TAG_OK запись подписантов окончена $cnt")
        return result
    }

    // Insert Запись новой задачи
    // -------------------------------------------------------
    fun insertNewTask(task: Task, idInspector: Int): Boolean {
        val dbWrite = this.writableDatabase

        try {

            val cv = ContentValues()

            cv.put("DAT", "${task.dat}")
            cv.put("ID_TASK", task.id_task)
            cv.put("ADR", task.address)
            cv.put("PURPOSE", task.purpose)
            cv.put("PURPOSE_NAME", task.purpose_name)
            cv.put("STATUS", task.status)
            cv.put("STATUS_NAME", task.status_name)
            cv.put("PRIM", task.prim)
            cv.put("TTIME", "${task.ttime}")
            cv.put("ID_INSPECTOR", idInspector)
            cv.put("FIO_INSPECTOR", task.fio)
            cv.put("FIO_CONTACT", task.fio_contact)
            cv.put("TEL_CONTACT", task.tel_contact)
            cv.put("CITY", task.city)
            cv.put("STREET", task.street)
            cv.put("HOUSES", task.house)
            cv.put("ND", task.nd)
            cv.put("KOD_OBJ", task.kod_obj)
            cv.put("KOD_NUMOBJ", task.kod_numobj)
            cv.put("KOD_DOG", task.kod_dog)
            cv.put("KODP", task.kodp)
            cv.put("NDOG", task.ndog)
            cv.put("PAYER_NAME", task.payer_name)
            cv.put("KOD_EMP_PODP", task.kod_emp_podp)

            val result = dbWrite.insertOrThrow("MI_MOB_TASK", null, cv)

            return result != (-1).toLong()
        } catch (e: Exception) {
            dbWrite.close()
            println("$TAG_ERR insert new task: ${e.message}")
            return false
        }
    }

    // Update Обновление данных в задаче
    // ----------------------------------
    fun updateTask(task: Task): Boolean {
        val dbWrite = this.writableDatabase
        try {
            val cv = ContentValues()

            cv.put("DAT", task.dat.toString())
            cv.put("ADR", task.address)
            cv.put("PURPOSE", task.purpose)
            cv.put("PURPOSE_NAME", task.purpose_name)
            cv.put("PRIM", task.prim)
            cv.put("TTIME", task.ttime.toString())
            cv.put("STATUS", task.status)
            cv.put("STATUS_NAME", task.status_name)
            cv.put("ID_INSPECTOR", task.id_inspector)
            cv.put("FIO_INSPECTOR", task.fio)
            cv.put("KOD_OBJ", task.kod_obj)
            cv.put("KOD_NUMOBJ", task.kod_numobj)
            cv.put("KOD_DOG", task.kod_dog)
            cv.put("KODP", task.kodp)
            cv.put("NDOG", task.ndog)
            cv.put("PAYER_NAME", task.payer_name)
            cv.put("FIO_CONTACT", task.fio_contact)
            cv.put("EMAIL_CONTACT", task.email_contact)
            cv.put("TEL_CONTACT", task.tel_contact)
            cv.put("LAT", task.lat)
            cv.put("LAN", task.lan)
            cv.put("SCHEMA_ZULU", task.schema_zulu)
            cv.put("BORDER_ZULU", task.border_zulu)

            cv.put("KOD_EMP_PODP", task.kod_emp_podp)
            cv.put("FIO_PODP", task.fio_podp)
            cv.put("EMAIL_PODP", task.email_podp)
            cv.put("TEL_PODP", task.tel_podp)
            cv.put("NAME_DOLZHN_PODP", task.name_dolzhn_podp)
            cv.put("IS_SEND", task.is_send)

            if (task.id_task < 0 && task.status in arrayOf(0, -12)) {
                cv.put("CITY", task.city)
                cv.put("STREET", task.street)
                cv.put("HOUSES", task.house)
                cv.put("ND", task.nd)
            }

            val result = dbWrite.update("MI_MOB_TASK", cv, "id_task = ${task.id_task}", null)

            return result != (-1)

        } catch (e: Exception) {
            dbWrite.close()
            println("$TAG_ERR update task: ${e.message}")
            return false
        }
    }

    // Update Статуса (12 выполнен, 15 отправлен инспектором)
    // ------------------------------------------------------------
    fun updateTaskStatus(idTask: Int, idStatus: Int): Boolean {
        val dbWrite = this.writableDatabase
        return try {
            val cv = ContentValues()
            cv.put("STATUS", idStatus)
            if (idStatus in arrayOf(12, -12))
                cv.put("STATUS_NAME", "Выполнен")
            if (idStatus == 15)
                cv.put("STATUS_NAME", "Отправлена инспектором")
            val result = dbWrite.update("MI_MOB_TASK", cv, "id_task = $idTask", null)

            result != (-1)
        } catch (e: Exception) {
            dbWrite.close()
            println("$TAG_ERR updateTaskStatus: ${e.message}")
            false
        }
    }

    // Update признака отправки задачи
    // (чтобы удалять только те, которые были отправлены)
    // (сброс в 0 после изменений чего-либо)
    // --------------------------------------------------
    fun updateTaskSend(idTask: Int, isSend: Int = 1): Boolean {
        val dbWrite = this.writableDatabase
        return try {
            val cv = ContentValues()
            cv.put("IS_SEND", isSend)
            val result = dbWrite.update("MI_MOB_TASK", cv, "id_task = $idTask", null)

            result != (-1)
        } catch (e: Exception) {
            dbWrite.close()
            println("$TAG_ERR updateTaskSend: ${e.message}")
            false
        }
    }

    // Insert Blob с устройства
    // возвращает idFile или -1, в случае не успеха
    // --------------------------------------------------------------------
    fun insertFileWithBlob(file: FileInfo, isImage: Boolean = false): Int {
        if (file.filename.isEmpty()) {
            file.filename = "FILE_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))}"
            file.filename += if (isImage) ".jpg" else ".pdf"
        }

        // Если картинка, но в названии не найдено расширение
        if (isImage && !isImageExtension(file.filename)) {
            file.filename += ".jpg"
        }

        val db = this.writableDatabase
        try {
            if (db.isReadOnly) return -1

            val dbRead = DbHandlerLocalRead(context, null)
            if (file.id_file == 0) {
                file.id_file = dbRead.getIdFileFromSequence()
            }
            dbRead.close()

            val cv = ContentValues()
            cv.put("id_task", file.id_task)
            if (file.filedata != null && isImageExtension(file.filename)) {
                var data = file.filedata
                while (data!!.size > 2097152) {
                    data = resizeImage(data)
                }
                cv.put("filedata", data)
            } else
                cv.put("filedata", file.filedata)

            cv.put("filename", file.filename)
            cv.put("id_file", file.id_file)
            cv.put("uri", file.uri.toString())
            cv.put("paper", file.paper)
            cv.put("is_signed", file.is_signed)
            cv.put("date_send_to_client", file.date_send_to_client.toString())
            cv.put("email_client", file.email_client)
            cv.put("id_act", file.id_act)
            cv.put("npp", file.npp)

            val result = db.insertOrThrow("MI_MOB_TASK_FILES", null, cv).toInt()
            return if (result != -1) file.id_file else result
        } catch (e: Exception) {
            println("$TAG_ERR : ${e.message}")
            return -1
        }
    }

    // Проверка на наличие расширения
    // --------------------------------------------------
    private fun isImageExtension(name: String): Boolean {
        return name.length > 4 &&
                name.substringAfterLast(".").toLowerCase(Locale.ROOT) in arrayOf("jpg", "jpeg", "bmp", "png", "gif")
    }

    // Уменьшение размера фотографий (compress)
    // -------------------------------------------------------
    private fun resizeImage(filedata: ByteArray): ByteArray? {
        return try {
            val bitmap = BitmapFactory.decodeByteArray(filedata, 0, filedata.size)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            stream.toByteArray()
        } catch (e: Exception) {
            println("$TAG_ERR resize image: ${e.message}")
            null
        }
    }

    // update Blob
    // ----------------------------------------------------------------
    fun updateFile(fileInfo: FileInfo, filedata: ByteArray, history: Boolean = false): Boolean {
        val db = this.writableDatabase
        try {
            if (db.isReadOnly) return false

            val cv = ContentValues()
            if (isImageExtension(fileInfo.filename)) {
                var data = filedata
                while (data.size > 2097152) {
                    data = resizeImage(data)!!
                }
                cv.put("filedata", data)
            } else
                cv.put("filedata", filedata)
            cv.put("email_client", fileInfo.email_client)
            if (fileInfo.date_send_to_client != null)
                cv.put("date_send_to_client", fileInfo.date_send_to_client.toString())

            return db.update(
                if (history) "MI_MOB_TASK_FILES_HISTORY" else "MI_MOB_TASK_FILES", cv,
                "id_task = ${fileInfo.id_task} and id_file = ${fileInfo.id_file}",
                null
            ) != -1

        } catch (e: Exception) {
            println("$TAG_ERR update file ${if (history) "history" else ""}: ${e.message}")
            return false
        }
    }

    // update FileUri
    // ---------------------------------------------
    fun updateFileUri(fileInfo: FileInfo): Boolean {
        val db = this.writableDatabase
        try {
            if (db.isReadOnly) return false

            val cv = ContentValues()
            if (fileInfo.uri != null)
                cv.put("uri", fileInfo.uri.toString())
            return db.update(
                "MI_MOB_TASK_FILES", cv,
                "id_task = ${fileInfo.id_task} and id_file = ${fileInfo.id_file}",
                null
            ) != -1

        } catch (e: Exception) {
            println("$TAG_ERR update file uri: ${e.message}")
            return false
        }
    }

    // update File send
    // ---------------------------------------------
    fun updateFileSend(idFile: Int): Boolean {
        val db = this.writableDatabase
        try {
            if (db.isReadOnly) return false

            val cv = ContentValues()
            cv.put("is_send", 1)
            return db.update(
                "MI_MOB_TASK_FILES", cv,
                "id_file = $idFile",
                null
            ) != -1

        } catch (e: Exception) {
            println("$TAG_ERR updateFileSend: ${e.message}")
            return false
        }
    }


    // Delete Открепление фотографий от задачи
    // -----------------------------------------------------------
    fun deleteFilesFromTask(id_task: Int, id_file: Int): Boolean {
        val db = this.writableDatabase
        return try {
            val result = db.delete("MI_MOB_TASK_FILES", "id_task = $id_task and id_file = $id_file", null)
            if (result != 0)
                db.delete("MI_ACT_FIELDS", "id_task = $id_task and id_file = $id_file", null)
            true
        } catch (e: Exception) {
            println("$TAG_ERR delete files: ${e.message}")
            false
        }
    }

    // Delete всех данных, что старше трёх дней и были отправлены
    // ----------------------------------------------------------
    fun deleteOldData(_idInspector: Int): Pair<Boolean, String> {
        val db = this.writableDatabase
        return try {
            var tasksIds = ""
            var historyIds = ""
            var kodobjs = ""
            try {
                val query = "select ifnull(group_concat(distinct id_task), '') as tasks, " +
                        " ifnull(group_concat(distinct kod_obj), '') as kodobjs" +
                        " from MI_MOB_TASK where id_inspector = $_idInspector " +
                        " and dat < datetime('${LocalDate.now()}','-7 day') and is_send = 1"
                var cur = (DbHandlerLocalRead(context, null).readableDatabase)
                    .rawQuery(query, null)

                if (cur.count != 0) {
                    cur.moveToNext()
                    tasksIds = cur.getString(cur.getColumnIndexOrThrow("tasks"))
                    kodobjs = cur.getString(cur.getColumnIndexOrThrow("kodobjs"))
                }
                if (!cur.isClosed) cur.close()

                cur = (DbHandlerLocalRead(context, null).readableDatabase)
                    .rawQuery(
                        "select ifnull(group_concat(distinct id_task), '') as tasks " +
                                " from MI_HISTORY where id_inspector = $_idInspector " +
                                "and kod_obj in ($kodobjs)", null
                    )

                if (cur.count != 0) {
                    cur.moveToNext()
                    historyIds = cur.getString(cur.getColumnIndexOrThrow("tasks"))
                }
                if (!cur.isClosed) cur.close()

            } catch (e: Exception) {
                println("$TAG_ERR delete old data: ${e.message}")
            }

            val result1 = db.delete("MI_MOB_TASK", "id_task in (${tasksIds})", null)
            println("$TAG_OK delete $result1 rows from MI_MOB_TASK")
            val result2 = db.delete("MI_ACT_FIELDS", "id_task in (${tasksIds})", null)
            println("$TAG_OK delete $result2 rows from MI_ACT_FIELDS")
            val result3 = db.delete("MI_ACT_FIELDS_DOP", "id_task in (${tasksIds})", null)
            println("$TAG_OK delete $result3 rows from MI_ACT_FIELDS_DOP")
            val result4 = db.delete("MI_ACT_FIELDS_SHABLON", "id_task in (${tasksIds})", null)
            println("$TAG_OK delete $result4 rows from MI_ACT_FIELDS_SHABLON")
            val result5 = db.delete("MI_DOG_DATA", "id_task in (${tasksIds})", null)
            println("$TAG_OK delete $result5 rows from MI_DOG_DATA")
            val result6 = db.delete("MI_DOG_OBJ", "id_task in (${tasksIds})", null)
            println("$TAG_OK delete $result6 rows from MI_DOG_OBJ")
            val result7 = db.delete("MI_DOG_TU", "id_task in (${tasksIds})", null)
            println("$TAG_OK delete $result7 rows from MI_DOG_TU")
            val result8 = db.delete("MI_DOG_UU", "id_task in (${tasksIds})", null)
            println("$TAG_OK delete $result8 rows from MI_DOG_UU")
            val result9 = db.delete("MI_HISTORY", "kod_obj in (${kodobjs})", null)
            println("$TAG_OK delete $result9 rows from MI_HISTORY")
            val result10 = db.delete("MI_MOB_TASK_FILES", "id_task in (${tasksIds})", null)
            println("$TAG_OK delete $result10 rows from MI_MOB_TASK_FILES")
            val result11 = db.delete("MI_MOB_TASK_FILES_HISTORY", "id_task in (${historyIds})", null)
            println("$TAG_OK delete $result11 rows from MI_MOB_TASK_FILES_HISTORY")

            Pair(true, tasksIds)
        } catch (e: Exception) {
            println("$TAG_ERR delete old data: ${e.message}")
            Pair(false, "")
        }
    }

    // Delete ручной задачи
    // -------------------------------------------------------
    fun deleteTask(_idInspector: Int, _idTask: Int): Boolean {
        val db = this.writableDatabase
        return try {
            val result1 = db.delete("MI_MOB_TASK", "id_task = $_idTask and id_inspector = $_idInspector", null)
            println("$TAG_OK delete rows from MI_MOB_TASK: $result1")
            val result2 = db.delete("MI_ACT_FIELDS", "id_task = $_idTask and id_inspector = $_idInspector", null)
            println("$TAG_OK delete rows from MI_ACT_FIELDS: $result2")
            val result4 =
                db.delete("MI_ACT_FIELDS_SHABLON", "id_task = $_idTask and id_inspector = $_idInspector", null)
            println("$TAG_OK delete rows from MI_ACT_FIELDS_SHABLON: $result4")
            val result10 = db.delete("MI_MOB_TASK_FILES", "id_task = $_idTask and id_inspector = $_idInspector", null)
            println("$TAG_OK delete rows from MI_MOB_TASK_FILES: $result10")

            true
        } catch (e: Exception) {
            println("$TAG_ERR delete задачи ручной: ${e.message}")
            false
        }
    }
}