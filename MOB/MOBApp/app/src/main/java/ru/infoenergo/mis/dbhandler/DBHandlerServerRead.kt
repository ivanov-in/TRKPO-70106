package ru.infoenergo.mis.dbhandler

import android.content.Context
import android.net.Uri
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import ru.infoenergo.mis.R
import ru.infoenergo.mis.helpers.*
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** ****************************************************** **/
/**   Получение информации с сервера в виде строки json    **/
/**         и запись информации в локальную БД             **/
/** ****************************************************** **/
class DBHandlerServerRead constructor(var context: Context) {


    // Поиск обновлений в задачах
    // ***********************************************************************
    fun checkUpdatesAsync(date: LocalDate, idInspector: Int): Int {
        var cnt = 0
        val dateString = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        try {
            val objs = getDataFromServer(
                "get_count_updates",
                "pdate=$dateString&pid_inspector=$idInspector"
            )
            if (objs.ok) {
                val mapper = ObjectMapper()
                val rootNode = mapper.readTree(objs.json)

                val elements = rootNode.elements()
                if (elements.hasNext()) {
                    val element = elements.next()
                    cnt = element.get("CNT")!!.asInt()
                }
                return cnt
            } else {
                if (objs.error.isNotEmpty()) {
                    println("$TAG_ERR check updates: ${objs.error}")
                }
                return cnt
            }
        } catch (e: Exception) {
            println("$TAG_ERR check updates: ${e.message}")
            return 0
        }
    }


    // Поиск объекта при добавлении новой задачи
    // *********************************************************************
    fun findObjectAsync(searchString: String): ArrayList<SearchObjectInfo> {
        try {
            val objs = getDataFromServer(
                "get_objects",
                "psearch_string=$searchString"
            )
            return if (objs.ok) {
                objsParseJsonToArray(objs.json)
            } else {
                if (objs.error.isNotEmpty()) {
                    println("$TAG_ERR get search obj: ${objs.error}")
                }
                ArrayList()
            }
        } catch (e: Exception) {
            println("$TAG_ERR get search obj: ${e.message}")
            return ArrayList()
        }
    }

    // Получение id инспектора по логину (+ значение сиквенса)
    // *******************************************************
    fun idInspectorAsync(login: String, password: String): AsyncResultJson {
        return try {
            val params =
                "p_puser=$login&p_password=$password"
            getDataFromServer("get_inspector_data", params)
        } catch (e: Exception) {
            println("$TAG_ERR idInspectorAsync: ${e.message}")
            AsyncResultJson(false, "Произошла ошибка при идентификации пользователя: ${e.message}", "")
        }
    }

    // Получение задач с сервера и запись в локальную БД
    // ***************************************************************************************
    fun tasksAsync(date: LocalDate, idInspector: Int, status: Int? = null): AsyncResultTasks {
        val dateString = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        // получаем с сервера только что созданную задачу create_task (status = 2)
        if (status != null) {
            val tasks = getDataFromServer(
                "get_tasks",
                "pdate=$dateString&pid_inspector=$idInspector&pi_task_status=$status"
            )
            DbHandlerLocalWrite(context, null).insertTaskListFromServer(tasks.json)
            return AsyncResultTasks(true, "", ArrayList())
        } else
            try {
                val tasks = getDataFromServer(
                    "get_tasks",
                    "pdate=$dateString&pid_inspector=$idInspector"
                )
                //return tasks
                return if (tasks.ok) {
                    val tasksFromServer = taskParseJsonToArray(tasks.json)
                    val idsList = tasksFromServer.joinToString { "${it.id_task}" }

                    val dbRead = DbHandlerLocalRead(context, null)
                    val tasksLocal = if (idsList.isNotEmpty()) dbRead.getTasksByIds(idsList) else ArrayList()

                    if (idsList.isEmpty() || tasksLocal.size == 0) {
                        // если в базе ничего, то всё сразу записываем
                        // (будет при первом запуске или при чистке кэша)
                        val dbWritable = DbHandlerLocalWrite(context, null)
                        dbWritable.insertTaskListFromServer(tasks.json)
                        dbWritable.close()
                        AsyncResultTasks(true, "", ArrayList())
                    } else {
                        val tasksToInsertLocal: ArrayList<Task> = ArrayList()
                        val tasksToAskUser: ArrayList<Task> = ArrayList()

                        for (taskServer in tasksFromServer) {
                            // ищем задачу с таким же id в локальной базе
                            val existIdTask = tasksLocal.indexOfFirst { it.id_task == taskServer.id_task }

                            // если нет задания на устройстве - добавляем в базу сразу, отправляем accept
                            if (existIdTask == -1) {
                                tasksToInsertLocal.add(taskServer)
                                DBHandlerServerWrite(context).acceptTask(taskServer.id_task)
                            }
                            // если полностью совпадает - пропускаем
                            else if (tasksLocal[existIdTask] == taskServer) {
                                continue
                            }
                            // если задача выполнена, то отклоняем, отправляем refuse
                            else if (tasksLocal[existIdTask].status == 12) {
                                DBHandlerServerWrite(context).refuseTask(taskServer.id_task)
                            } else {
                                // если совпадает не полностью
                                if (tasksLocal[existIdTask].diff.isNotEmpty() || taskServer.diff.isNotEmpty())
                                    tasksToAskUser.add(taskServer)
                            }
                        }

                        if (tasksToInsertLocal.size > 0) {
                            // добавляем в базу задачи, которых не было ещё
                            val dbWritable = DbHandlerLocalWrite(context, null)
                            dbWritable.insertTaskListFromServer(tasksToInsertLocal)
                            dbWritable.close()
                        }
                        AsyncResultTasks(true, "", tasksToAskUser)
                    }
                } else {
                    if (tasks.error.isNotEmpty()) {
                        println("$TAG_ERR get tasks: ${tasks.error}")
                    }
                    AsyncResultTasks(false, tasks.error, ArrayList())
                }

            } catch (e: Exception) {
                println("$TAG_ERR get tasks: ${e.message}")
                return AsyncResultTasks(false, "${e.message}", ArrayList())
            }
    }

    // Получение файлов к задачам с сервера и запись в локальную БД
    // *******************************************************************************************
    fun taskFilesAsync(date: LocalDate, idInspector: Int, status: Int? = null): AsyncResultFiles {
        val dateString = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        //val arrayFiles: ArrayList<FileInfo> = ArrayList()
        return try {
            val params = if (status == null)
                "pdate=$dateString&pid_inspector=$idInspector"
            else
                "pdate=$dateString&pid_inspector=$idInspector&pi_task_status=$status"

            val files = getDataFromServer("get_tasks_files", params)

            if (files.ok) {
                val filesServer = filesParseJsonToArray(files.json)
                AsyncResultFiles(true, "", filesServer) // отправляем пустой массив, т.к. ничего спрашивать не надо
            } else {
                if (files.error.isNotEmpty()) {
                    println("$TAG_ERR get tasks: ${files.error}")
                    AsyncResultFiles(false, files.error, ArrayList())
                } else
                    AsyncResultFiles(false, "null", ArrayList())
            }
        } catch (e: Exception) {
            println("$TAG_ERR get files from server: ${e.message}")
            AsyncResultFiles(false, "${e.message}", ArrayList())
        }
    }

    // Получение файлов к задачам с сервера и запись в локальную БД
    // *******************************************************************************************
    fun taskFilesHistoryAsync(date: LocalDate, idInspector: Int, status: Int? = null): AsyncResultFiles {
        val dateString = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        return try {
            val params = if (status == null)
                "pdate=$dateString&pid_inspector=$idInspector"
            else
                "pdate=$dateString&pid_inspector=$idInspector&pi_task_status=$status"

            val files = getDataFromServer("get_tasks_files_hist", params)

            if (files.ok) {
                val filesServer = filesParseJsonToArray(files.json)
                AsyncResultFiles(true, "", filesServer) // отправляем пустой массив, т.к. ничего спрашивать не надо
            } else {
                if (files.error.isNotEmpty()) {
                    println("$TAG_ERR get tasks: ${files.error}")
                    AsyncResultFiles(false, files.error, ArrayList())
                } else
                    AsyncResultFiles(false, "null", ArrayList())
            }
        } catch (e: Exception) {
            println("$TAG_ERR get files from server: ${e.message}")
            AsyncResultFiles(false, "${e.message}", ArrayList())
        }
    }

    // Получение blob файлов к задачам
    // **********************************
    fun taskFilesBlobAsync(idFile: Int): AsyncResultBlob {
        return try {
            var filedata: ByteArray? = null
            val params = "pid_file=$idFile"
            val blob = getDataFromServerBlob(params)
            if (blob.ok) {
                try {
                    filedata = blob.array
                } catch (e: Exception) {
                    blob.error = e.message.toString()
                    AsyncResultBlob(false, "${e.message}", null)
                }
                AsyncResultBlob(true, "", filedata)
            } else
                AsyncResultBlob(false, blob.error, null)
        } catch (e: Exception) {
            println("$TAG_ERR historyAsync: ${e.message}")
            AsyncResultBlob(false, "Произошла ошибка при загрузке фотографий: ${e.message}", null)
        }
    }

    // Парсим поля файлы из json в arrayList
    // --------------------------------------------------------------
    private fun filesParseJsonToArray(json: String): ArrayList<FileInfo> {
        val filesServer: ArrayList<FileInfo> = ArrayList()
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)

        val elements = rootNode.elements()
        while (elements.hasNext()) {
            try {
                val element = elements.next()

                val file = FileInfo(
                    id_task = element.get("ID_TASK")!!.asInt(),
                    id_file = element.get("ID_FILE")!!.asInt(),
                    filename = element.get("FILENAME")!!.asText()!!.replace("null", ""),
                    is_signed = element.get("IS_SIGNED")!!.asInt(),
                    is_send = 0,
                    paper = element.get("PAPER")!!.asInt(),
                    date_send_to_client = null,
                    email_client = "",
                    npp = 0,
                    id_act = 0
                )
                filesServer.add(file)
            } catch (e: Exception) {
                println(
                    "$TAG_ERR Не удалось спарсить файл ${elements.next().get("ID_FILE")!!.asInt()}: ${e.message}"
                )
            }
        }
        return filesServer
    }

    // Парсим объекты из поиска json в arrayList
    // --------------------------------------------------------------
    private fun objsParseJsonToArray(json: String): ArrayList<SearchObjectInfo> {
        val searchObjects: ArrayList<SearchObjectInfo> = ArrayList()
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)

        val elements = rootNode.elements()
        while (elements.hasNext()) {
            try {
                val element = elements.next()

                val obj = SearchObjectInfo(
                    adr = element.get("ADR")!!.asText()!!.replace("null", ""),
                    name = element.get("NAME")!!.asText()!!.replace("null", ""),
                    kod_obj = element.get("KOD_OBJ")!!.asInt(),
                    kod_dog = element.get("KOD_DOG")!!.asInt(),
                    kodp = element.get("KODP")!!.asInt(),
                    kod_numobj = element.get("KOD_NUMOBJ")!!.asInt(),
                    ndog = element.get("NDOG")!!.asText()!!.replace("null", ""),
                )
                searchObjects.add(obj)
            } catch (e: Exception) {
                println("$TAG_ERR Не удалось спарсить объект ${elements.next().get("KOD_OBJ")!!.asInt()}: ${e.message}")
            }
        }
        return searchObjects
    }

    // Парсим задачи из json в arrayList
    // --------------------------------------------------------------
    private fun taskParseJsonToArray(json: String): ArrayList<Task> {
        val taskServer: ArrayList<Task> = ArrayList()
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)

        val elements = rootNode.elements()
        while (elements.hasNext()) {
            try {
                val element = elements.next()

                val date = LocalDate.parse(element.get("DAT")!!.asText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                val time = LocalDateTime.parse(
                    element.get("TTIME")!!.asText(),
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
                )
                val task = Task(
                    dat = date,
                    id_task = element.get("ID_TASK")!!.asInt(),
                    address = element.get("ADR")!!.asText()!!.replace("null", ""),
                    purpose = element.get("PURPOSE")!!.asInt(),
                    purpose_name = element.get("PURPOSE_NAME")!!.asText()!!.replace("null", ""),
                    prim = element.get("PRIM")!!.asText()!!.replace("null", ""),
                    ttime = time,
                    status = element["STATUS"]!!.asInt(),
                    status_name = element.get("STATUS_NAME")!!.asText()!!.replace("null", ""),
                    id_inspector = element.get("ID_INSPECTOR")!!.asInt(),
                    fio = element.get("FIO")!!.asText()!!.replace("null", ""),
                    kod_obj = element.get("KOD_OBJ")!!.asInt(),
                    kod_dog = element.get("KOD_DOG")!!.asInt(),
                    kodp = element.get("KODP")!!.asInt(),
                    ndog = element.get("NDOG")!!.asText()!!.replace("null", ""),
                    payer_name = element.get("PAYER_NAME")!!.asText()!!.replace("null", ""),
                    fio_contact = element.get("FIO_CONTACT")!!.asText()!!.replace("null", ""),
                    email_contact = element.get("EMAIL_CONTACT")!!.asText()!!.replace("null", ""),
                    tel_contact = element.get("TEL_CONTACT")!!.asText()!!.replace("null", ""),
                    lat = element.get("LAT")!!.asText()!!.replace("null", ""),
                    lan = element.get("LAN")!!.asText()!!.replace("null", ""),
                    schema_zulu = element.get("SCHEMA_ZULU")!!.asText()!!.replace("null", ""),
                    border_zulu = element.get("BORDER_ZULU")!!.asText()!!.replace("null", ""),
                    kod_emp_podp = element.get("KOD_EMP_PODP")!!.asInt(),
                    fio_podp = element.get("FIO_PODP")!!.asText()!!.replace("null", ""),
                    tel_podp = element.get("TEL_PODP")!!.asText()!!.replace("null", ""),
                    email_podp = element.get("EMAIL_PODP")!!.asText()!!.replace("null", "")
                )

                taskServer.add(task)

            } catch (e: Exception) {
                println("$TAG_ERR Не удалось спарсить задачу ${elements.next().get("ID_TASK")!!.asInt()}: ${e.message}")
            }
        }

        return taskServer
    }

    // Получение доступных к заданиям актов с сервера и запись в локальную БД
    // **********************************************************************
    fun actsAsync(): AsyncResultJson {
        return try {
            getDataFromServer("get_purpose_acts", "")
        } catch (e: Exception) {
            println("$TAG_ERR get purpose acts: ${e.message}")
            AsyncResultJson(false, "Произошла ошибка при загрузке актов: ${e.message}", "")
        }
    }

    // Парсим поля актов из json в arrayList
    // ----------------------------------------------------------------------------
    private fun actFieldsParseJsonToArray(json: String): ArrayList<ActFieldsInfo> {
        val actFieldsServer: ArrayList<ActFieldsInfo> = ArrayList()
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)

        val elements = rootNode.elements()
        while (elements.hasNext()) {
            try {
                val element = elements.next()

                val date = LocalDate.parse(element.get("DAT")!!.asText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                val actFields = ActFieldsInfo(
                    dat = date,
                    id_task = element.get("ID_TASK")!!.asInt(),
                    id_act = element.get("ID_ACT")!!.asInt(),
                    kodp = element.get("KODP")!!.asInt(),
                    kod_dog = element.get("KOD_DOG")!!.asInt(),
                    kod_obj = element.get("KOD_OBJ")!!.asInt(),
                    num_act = element.get("NUM_ACT")!!.asText()!!.replace("null", ""),
                    dat_act = element.get("DAT_ACT")!!.asText()!!.replace("null", ""),
                    payer_name = element.get("PAYER_NAME")!!.asText()!!.replace("null", ""),
                    adr_org = element.get("ADR_ORG")!!.asText()!!.replace("null", ""),
                    fio_contact = element.get("FIO_CONTACT")!!.asText()!!.replace("null", ""),
                    tel_contact = element.get("TEL_CONTACT")!!.asText()!!.replace("null", ""),
                    filial_eso = element.get("FILIAL_ESO")!!.asText()!!.replace("null", ""),
                    fio_eso = element.get("FIO_ESO")!!.asText()!!.replace("null", ""),
                    tel_eso = element.get("TEL_ESO")!!.asText()!!.replace("null", ""),
                    list_obj = element.get("LIST_OBJ")!!.asText()!!.replace("null", ""),
                    name_obj = element.get("NAME_OBJ")!!.asText()!!.replace("null", ""),
                    num_obj = element.get("NUM_OBJ")!!.asText()!!.replace("null", ""),
                    adr_obj = element.get("ADR_OBJ")!!.asText()!!.replace("null", ""),
                    ndog = element.get("NDOG")!!.asText()!!.replace("null", ""),
                    dat_dog = element.get("DAT_DOG")!!.asText()!!.replace("null", ""),
                    otop_period = element.get("OTOP_PERIOD")!!.asText()!!.replace("null", ""),
                    sum_dolg = element.get("SUM_DOLG")!!.asText()!!.replace("null", ""),
                    remark_dog = element.get("REMARK_DOG")!!.asText()!!.replace("null", ""),
                    nal_podp_doc = element.get("NAL_PODP_DOC")!!.asText()!!.replace("null", ""),
                    opl_calcul = element.get("OPL_CALCUL")!!.asText()!!.replace("null", ""),
                    osnov = element.get("OSNOV")!!.asText()!!.replace("null", ""),
                    city = element.get("CITY")!!.asText()!!.replace("null", ""),
                    shablon = element.get("SHABLON")!!.asText()!!.replace("null", ""),
                    name_dolzhn_contact = element.get("NAME_DOLZHN_CONTACT")!!.asText()!!.replace("null", ""),
                    name_dolzhn_eso = element.get("NAME_DOLZHN_ESO")!!.asText()!!.replace("null", ""),
                    period_dolg = element.get("PERIOD_DOLG")!!.asText()!!.replace("null", ""),
                    name_st = element.get("NAME_ST")!!.asText()!!.replace("null", ""),
                    name_mag = element.get("NAME_MAG")!!.asText()!!.replace("null", ""),
                    name_tk = element.get("NAME_TK")!!.asText()!!.replace("null", ""),
                    name_aw = element.get("NAME_AW")!!.asText()!!.replace("null", ""),
                    inn_org = element.get("INN_ORG")!!.asText()!!.replace("null", ""),
                    kpp_org = element.get("KPP_ORG")!!.asText()!!.replace("null", ""),
                    nazn_name = element.get("NAZN_NAME")!!.asText()!!.replace("null", ""),
                    nal_so = element.get("NAL_SO")!!.asText()!!.replace("null", ""),
                    nal_sw = element.get("NAL_SW")!!.asText()!!.replace("null", ""),
                    nal_st = element.get("NAL_ST")!!.asText()!!.replace("null", ""),
                    nal_gv = element.get("NAL_GV")!!.asText()!!.replace("null", ""),
                    director_tatenergo = element.get("DIRECTOR_TATENERGO")!!.asText()!!.replace("null", ""),
                    volume_obj = element.get("VOLUME_OBJ")!!.asText()!!.replace("null", ""),
                    so_q = element.get("SO_Q")!!.asText()!!.replace("null", ""),
                    sw_q = element.get("SW_Q")!!.asText()!!.replace("null", ""),
                    gw_q = element.get("GW_Q")!!.asText()!!.replace("null", ""),
                    st_q = element.get("ST_Q")!!.asText()!!.replace("null", ""),
                    q_sum = element.get("Q_SUM")!!.asText()!!.replace("null", ""),
                    npp = 0,
                    nal_act_gidro = element.get("NAL_ACT_GIDRO")!!.asText()!!.replace("null", "")
                )

                actFieldsServer.add(actFields)

            } catch (e: Exception) {
                println(
                    "$TAG_ERR Не удалось спарсить поля акта ${
                        elements.next().get("ID_ACT")!!.asInt()
                    }: ${e.message}"
                )
            }
        }

        return actFieldsServer
    }

    // Получение полей актов (для таблицы шаблон) к заданию с сервера и запись в локальную БД
    // ***********************************************************************************************
    fun actFieldsAsync(date: LocalDate, idInspector: Int, status: Int? = null): AsyncResultActFields {
        val dateString = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        // получаем данные по только что созданной задаче (create_task)
        if (status != null) {
            val actsFieldsResult = getDataFromServer(
                "get_act_fields",
                "pdate=$dateString&pid_inspector=$idInspector&pi_task_status=$status"
            )
            DbHandlerLocalWrite(context, null).insertActsFieldsFromServer(actsFieldsResult.json)
            return AsyncResultActFields(true, "", ArrayList())
        } else
            try {
                val actsFieldsResult = getDataFromServer(
                    "get_act_fields",
                    "pdate=$dateString&pid_inspector=$idInspector"
                )
                return if (actsFieldsResult.ok) {

                    val actFieldsFromServer = actFieldsParseJsonToArray(actsFieldsResult.json)
                    val idsList = actFieldsFromServer.joinToString { "(${it.id_task}, ${it.id_act})" }

                    val actFieldsLocal =
                        if (idsList.isEmpty()) ArrayList()
                        else DbHandlerLocalRead(context, null).getActFieldsShablonByIds(idsList)

                    if (idsList.isEmpty() || actFieldsLocal.size == 0) {
                        // если в базе нет совпадений, то всё сразу записываем
                        // (будет при новых актах, первом запуске или при чистке кэша)
                        val dbWritable = DbHandlerLocalWrite(context, null)
                        dbWritable.insertActsFieldsFromServer(actsFieldsResult.json)
                        dbWritable.close()
                        AsyncResultActFields(
                            true,
                            "",
                            ArrayList()
                        ) // отправляем пустой массив, т.к. ничего спрашивать не надо
                    } else {

                        val actFieldsToInsertLocal: ArrayList<ActFieldsInfo> =
                            ArrayList() // те акты, что записываем сразу
                        val actFieldsToAskUser: ArrayList<ActFieldsInfo> = ArrayList() // те, что спрашиваем

                        for (actFieldsServer in actFieldsFromServer) {
                            // ищем акт с таким же id_task и id_act в локальной базе
                            val existIdAct = actFieldsLocal.indexOfFirst {
                                it.id_task == actFieldsServer.id_task && it.id_act == actFieldsServer.id_act
                            }

                            // если нет задания на устройстве - добавляем в список ожидания записи
                            if (existIdAct == -1) {
                                actFieldsToInsertLocal.add(actFieldsServer)
                            } else

                            // если полностью совпадает - пропускаем или у которого is_signed = 1
                                if (actFieldsLocal[existIdAct].is_signed == 1 || actFieldsLocal[existIdAct] == actFieldsServer) {
                                    continue
                                }
                                // если совпадает не полностью и у которого is_signed = 0
                                else {
                                    if (actFieldsLocal[existIdAct].is_signed == 0)
                                        actFieldsToAskUser.add(actFieldsServer)
                                    // если совпадает не полностью и у которого is_signed = 1
                                    // то ничего не делаем, т.к. акт подписан на устройстве и менять нельзя
                                }
                        }

                        if (actFieldsToInsertLocal.size > 0) {
                            // добавляем в базу задачи, которых не было ещё
                            val dbWritable = DbHandlerLocalWrite(context, null)
                            dbWritable.insertActsFieldsFromServer(actFieldsToInsertLocal)
                            dbWritable.close()
                        }

                        AsyncResultActFields(true, "", actFieldsToAskUser)
                    }
                } else {
                    if (actsFieldsResult.error.isNotEmpty()) {
                        println("$TAG_ERR actsFieldAsync: ${actsFieldsResult.error}")
                    }
                    AsyncResultActFields(false, actsFieldsResult.error, ArrayList())
                }
            } catch (e: Exception) {
                println("$TAG_ERR actsFieldAsync: ${e.message}")
                return AsyncResultActFields(false, e.message.toString(), ArrayList())
            }
    }

    // Парсим доп поля актов из json в arrayList
    // --------------------------------------------------------------
    private fun actFieldsDopParseJsonToArray(json: String): ArrayList<ActFieldsDopInfo> {
        val fieldsDopServer: ArrayList<ActFieldsDopInfo> = ArrayList()
        val mapper = ObjectMapper()
        val rootNode = mapper.readTree(json)

        val elements = rootNode.elements()
        while (elements.hasNext()) {
            try {
                val element = elements.next()

                val date = LocalDate.parse(element.get("DAT")!!.asText(), DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                val fieldsDop = ActFieldsDopInfo(
                    dat = date,
                    id_task = element.get("ID_TASK")!!.asInt(),
                    id_act = element.get("ID_ACT")!!.asInt(),
                    num_obj = element.get("NUM_OBJ")!!.asText()!!.replace("null", ""),
                    name_obj = element.get("NAME_OBJ")!!.asText()!!.replace("null", ""),
                    address_obj = element.get("ADDRESS_OBJ")!!.asText()!!.replace("null", ""),
                    god_zd = element.get("GOD_ZD")!!.asText()!!.replace("null", ""),
                    nazn_name = element.get("NAZN_NAME")!!.asText()!!.replace("null", ""),
                    tvr = element.get("TVR")!!.asText()!!.replace("null", ""),
                    pr_oto = element.get("PR_OTO")!!.asText()!!.replace("null", ""),
                    pr_sw = element.get("PR_SW")!!.asText()!!.replace("null", ""),
                    volume = element["VOLUME"]!!.asDouble(),
                    square_total = element["SQUARE_TOTAL"]!!.asDouble(),
                    point_name = element.get("POINT_NAME")!!.asText()!!.replace("null", ""),
                    etaz = element.get("ETAZ")!!.asText()!!.replace("null", ""),
                    so_q = element["SO_Q"]!!.asDouble(),
                    sw_q = element["SW_Q"]!!.asDouble(),
                    st_q = element["ST_Q"]!!.asDouble(),
                    gw_qmax = element["GW_QMAX"]!!.asDouble(),
                    name_vodo = element.get("NAME_VODO")!!.asText()!!.replace("null", ""),
                    nn_wpol = element.get("NN_WPOL")!!.asDouble(),
                    tt_wpol = element.get("TT_WPOL")!!.asDouble(),
                    nn_prib = element.get("NN_PRIB")!!.asDouble(),
                    pr_rec = element.get("PR_REC")!!.asText()!!.replace("null", ""),
                    pr_psusch = element.get("PR_PSUSCH")!!.asText()!!.replace("null", ""),
                    pr_iz_st = element.get("PR_IZ_ST")!!.asText()!!.replace("null", ""),
                    nom_uch = element.get("NOM_UCH")!!.asText()!!.replace("null", ""),
                    tip_name = element.get("TIP_NAME")!!.asText()!!.replace("null", ""),
                    pt_d = element.get("PT_D")!!.asDouble(),
                    pt_l = element.get("PT_L")!!.asDouble(),
                    name_pr_pt = element.get("NAME_PR_PT")!!.asText()!!.replace("null", ""),
                    ot_d = element.get("OT_D")!!.asDouble(),
                    ot_l = element.get("OT_L")!!.asDouble(),
                    name_pr_ot = element.get("NAME_PR_OT")!!.asText()!!.replace("null", ""),
                    uch_hgr = element.get("UCH_HGR")!!.asText()!!.replace("null", ""),
                    pu_num = element.get("PU_NUM")!!.asText()!!.replace("null", ""),
                    pu_name = element.get("PU_NAME")!!.asText()!!.replace("null", ""),
                    pu_mesto = element.get("PU_MESTO")!!.asText()!!.replace("null", ""),
                    pu_type = element.get("PU_TYPE")!!.asText()!!.replace("null", ""),
                    pu_diam = element.get("PU_DIAM")!!.asText()!!.replace("null", ""),
                    pu_kolvo = element.get("PU_KOLVO")!!.asText()!!.replace("null", ""),
                    pu_proba_mesto = element.get("PU_PROBA_MESTO")!!.asText()!!.replace("null", ""),
                    q_sum = element.get("Q_SUM")!!.asText()!!.replace("null", ""),
                    q_sum_max = element.get("Q_SUM_MAX")!!.asText()!!.replace("null", ""),
                    pu_srok_poverki = element.get("PU_SROK_POVERKI")!!.asText()!!.replace("null", ""),
                    pu_num_plomba = element.get("PU_NUM_PLOMBA")!!.asText()!!.replace("null", ""),
                    pu_pokaz = element.get("PU_POKAZ")!!.asText()!!.replace("null", ""),
                    schema_prisoed_name = element.get("SCHEMA_PRISOED_NAME")!!.asText()!!.replace("null", ""),
                    schema_prisoed_kod = element.get("SCHEMA_PRISOED_KOD")!!.asText()!!.replace("null", ""),
                )

                fieldsDopServer.add(fieldsDop)

            } catch (e: Exception) {
                println(
                    "$TAG_ERR Не удалось спарсить доп поля ${
                        elements.next().get("ID_ACT")!!.asInt()
                    }: ${e.message}"
                )
            }
        }

        return fieldsDopServer
    }

    // Получение доп полей для заполнения акта
    // (дополнительная информация для некоторых актов) с сервера и запись в локальную БД
    // ***********************************************************************************************
    fun actFieldsDopAsync(date: LocalDate, idInspector: Int, status: Int? = null): AsyncResultActDop {
        val dateString = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

        // получаем данные по только что созданной задаче
        if (status != null) {
            val actsFieldsResult = getDataFromServer(
                "get_act_fields_dop",
                "pdate=$dateString&pid_inspector=$idInspector&pi_task_status=$status"
            )
            DbHandlerLocalWrite(context, null).insertActsFieldsDopFromServer(actsFieldsResult.json)
            return AsyncResultActDop(true, "", ArrayList())
        } else
            try {
                val actsFields = getDataFromServer(
                    "get_act_fields_dop",
                    "pdate=$dateString&pid_inspector=$idInspector"
                )
                return if (actsFields.ok) {

                    val actDopsFromServer = actFieldsDopParseJsonToArray(actsFields.json)
                    val idsList = actDopsFromServer.joinToString { "(${it.id_task}, ${it.id_act})" }
                    // val idsList = actDopsFromServer.joinToString { "(${it.id_task}, ${it.id_act}, ${it.num_obj})" }

                    val dbRead = DbHandlerLocalRead(context, null)
                    val actDopsLocal =
                        if (idsList.isEmpty()) ArrayList() else dbRead.getActFieldsDopShablonByIds(idsList)

                    if (idsList.isEmpty() || actDopsLocal.size == 0) {
                        // если в базе нет совпадений, то всё сразу записываем
                        // (будет при новых актах, первом запуске или при чистке кэша)
                        val dbWritable = DbHandlerLocalWrite(context, null)
                        dbWritable.insertActsFieldsDopFromServer(actsFields.json)
                        dbWritable.close()
                        return AsyncResultActDop(true, "", ArrayList())
                        // отправляем пустой массив, т.к. ничего спрашивать не надо
                    } else {
                        // те акты, что записываем сразу
                        val actDopsToInsertLocal: ArrayList<ActFieldsDopInfo> = ArrayList()
                        val actDopsToAskUser: ArrayList<ActFieldsDopInfo> = ArrayList() // те, что спрашиваем

                        for (actDopsServer in actDopsFromServer) {
                            // ищем акт с таким же id_task,num_obj и id_act в локальной базе
                            val existIdAct = actDopsLocal.indexOfFirst {
                                it.id_task == actDopsServer.id_task && it.id_act == actDopsServer.id_act && it.num_obj == actDopsServer.num_obj
                            }

                            // если нет задания на устройстве - добавляем в список ожидания записи
                            if (existIdAct == -1) {
                                actDopsToInsertLocal.add(actDopsServer)
                            }
                            // если полностью совпадает - пропускаем или у которого is_signed = 1
                            else if (actDopsLocal[existIdAct].is_signed == 1 || actDopsLocal[existIdAct] == actDopsServer) {
                                continue
                            }
                            // если совпадает не полностью и у которого is_signed = 0
                            else {
                                if (actDopsLocal[existIdAct].is_signed == 0)
                                    actDopsToAskUser.add(actDopsServer)
                                // если совпадает не полностью и у которого is_signed = 1
                                // то ничего не делаем, т.к. акт подписан на устройстве и менять нельзя
                            }
                        }

                        if (actDopsToInsertLocal.size > 0) {
                            // добавляем в базу задачи, которых не было ещё
                            val dbWritable = DbHandlerLocalWrite(context, null)
                            dbWritable.insertActsFieldsDopFromServer(actDopsToInsertLocal)
                            dbWritable.close()
                        }

                        AsyncResultActDop(true, "", actDopsToAskUser)
                    }
                } else {
                    if (actsFields.error.isNotEmpty()) {
                        println("$TAG_ERR actsFieldsDopAsync: ${actsFields.error}")
                    }
                    AsyncResultActDop(false, actsFields.error, ArrayList())
                }
            } catch (e: Exception) {
                println("$TAG_ERR actsFieldsDopAsync: ${e.message}")
                return AsyncResultActDop(false, "${e.message}", ArrayList())
            }
    }

    // Получение целей с сервера и запись в локальную БД
    // *************************************************
    fun purposesAsync(): String {
        try {
            val purpose =
                getDataFromServer("get_purpose", "")
            return if (purpose.ok) {
                val dbWritable = DbHandlerLocalWrite(context, null)
                if (dbWritable.insertPurposeFromServer(purpose.json)) {
                    dbWritable.close()
                    "Загрузка целей посещения прошла успешно"
                } else {
                    dbWritable.close()
                    "Произошла ошибка при загрузке целей"
                }
            } else {
                if (purpose.error.isNotEmpty()) {
                    println("$TAG_ERR get purpose: ${purpose.error}")
                }
                "Произошла ошибка при загрузке целей: ${purpose.error}"
            }
        } catch (e: Exception) {
            println("$TAG_ERR get purpose:${e.message}")
            return "Произошла ошибка при загрузке целей"
        }
    }

    // Получение истории посещения с сервера и запись в локальную БД
    // ****************************************************************************************
    fun historyAsync(date: LocalDate, idInspector: Int, status: Int? = null): AsyncResultJson {
        val dateString = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        return try {
            val params = if (status == null)
                "pdate=$dateString&pid_inspector=$idInspector"
            else
                "pdate=$dateString&pid_inspector=$idInspector&pi_task_status=$status"
            getDataFromServer("get_history", params)
        } catch (e: Exception) {
            println("$TAG_ERR historyAsync: ${e.message}")
            AsyncResultJson(false, "Произошла ошибка при загрузке истории посещений: ${e.message}", "")
        }
    }

    // Получение данных для окна информация по договору с сервера и запись в локальную БД
    // ****************************************************************************************
    fun dogDataAsync(date: LocalDate, idInspector: Int, status: Int? = null): AsyncResultJson {
        val dateString = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        return try {
            val params = if (status == null)
                "pdate=$dateString&pid_inspector=$idInspector"
            else
                "pdate=$dateString&pid_inspector=$idInspector&pi_task_status=$status"
            getDataFromServer("get_dog_data", params)
        } catch (e: Exception) {
            println("$TAG_ERR dogDataAsync: ${e.message}")
            AsyncResultJson(false, "Произошла ошибка при загрузке данных по договорам: ${e.message}", "")
        }
    }

    // Получение данных для окна информация по договору (объекты) с сервера и запись в локальную БД
    // ********************************************************************************************
    fun dogObjAsync(date: LocalDate, idInspector: Int, status: Int? = null): AsyncResultJson {
        val dateString = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        return try {
            val params = if (status == null)
                "pdate=$dateString&pid_inspector=$idInspector"
            else
                "pdate=$dateString&pid_inspector=$idInspector&pi_task_status=$status"
            getDataFromServer("get_dog_obj_data", params)
        } catch (e: Exception) {
            println("$TAG_ERR dogObjAsync: ${e.message}")
            AsyncResultJson(false, "Произошла ошибка при загрузке объектов договоров: ${e.message}", "")
        }
    }

    // Получение данных для окна информация по договору (точки учёта) с сервера и запись в локальную БД
    // ************************************************************************************************
    fun dogTuAsync(date: LocalDate, idInspector: Int, status: Int? = null): AsyncResultJson {
        val dateString = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        return try {
            val params = if (status == null)
                "pdate=$dateString&pid_inspector=$idInspector"
            else
                "pdate=$dateString&pid_inspector=$idInspector&pi_task_status=$status"
            getDataFromServer("get_dog_tu_data", params)
        } catch (e: Exception) {
            println("$TAG_ERR dogTuAsync: ${e.message}")
            AsyncResultJson(false, "Произошла ошибка при загрузке ТУ договоров: ${e.message}", "")
        }
    }

    // Получение данных для окна информация по договору (узлы учёта) с сервера и запись в локальную БД
    // ***********************************************************************************************
    fun dogUuAsync(date: LocalDate, idInspector: Int, status: Int? = null): AsyncResultJson {
        val dateString = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        return try {
            val params = if (status == null)
                "pdate=$dateString&pid_inspector=$idInspector"
            else
                "pdate=$dateString&pid_inspector=$idInspector&pi_task_status=$status"
            getDataFromServer("get_dog_uu_data", params)
        } catch (e: Exception) {
            println("$TAG_ERR dogUuAsync: ${e.message}")
            AsyncResultJson(false, "Произошла ошибка при загрузке УУ договоров: ${e.message}", "")
        }
    }

    // Получение данных для окна информация по договору (узлы учёта СИ) с сервера и запись в локальную БД
    // **************************************************************************************************
    fun dogUuSiAsync(date: LocalDate, idInspector: Int, status: Int? = null): AsyncResultJson {
        val dateString = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        return try {
            val params = if (status == null)
                "pdate=$dateString&pid_inspector=$idInspector"
            else
                "pdate=$dateString&pid_inspector=$idInspector&pi_task_status=$status"
            getDataFromServer("get_dog_uu_si_data", params)
        } catch (e: Exception) {
            println("$TAG_ERR dogUuSiAsync: ${e.message}")
            AsyncResultJson(false, "Произошла ошибка при загрузке УУ CB договоров: ${e.message}", "")
        }
    }

    // Получение данных подписантов с сервера и запись в локальную БД
    // ************************************************************************************************
    fun podpisantAsync(date: LocalDate, idInspector: Int, status: Int? = null): AsyncResultJson {
        val dateString = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))
        return try {
            val params = if (status == null)
                "pdate=$dateString&pid_inspector=$idInspector"
            else
                "pdate=$dateString&pid_inspector=$idInspector&pi_task_status=$status"
            getDataFromServer("get_podpisant", params)
        } catch (e: Exception) {
            println("$TAG_ERR podpisantAsync: ${e.message}")
            AsyncResultJson(false, "Произошла ошибка при загрузке подписантов: ${e.message}", "")
        }
    }

    // ============================================================================================================ //


    // Получение данных с сервера и запись json в строку
    // Передаём имя функции и список параметров
    // ***********************************************************************************************
    private fun getDataFromServer(functionName: String, functionParameters: String): AsyncResultJson {
        var path =
            "${context.getString(R.string.server_uri)}:${context.getString(R.string.server_port)}/${
                context.getString(
                    R.string.server_path_get
                )
            }/${functionName}"

        if (functionParameters.isNotEmpty())
            path += "?" + functionParameters.trim().replace(" ", "%20")

        val b = Uri.parse(path).buildUpon().build()

        val response: HttpResponse
        var json = ""
        var errmsg = ""
        try {
            val request = HttpPost(b.toString())
            response = DefaultHttpClient().execute(request)

            val statusLine = response.statusLine

            val entity = response.entity
            val inputStream = entity.content
            val outputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)

            var len: Int = inputStream.read(buffer)
            while (len > 0) {
                outputStream.write(buffer, 0, len)
                len = inputStream.read(buffer)
            }

            json = outputStream.toString()

            outputStream.close()
            inputStream.close()

            // Проверим, не пришла ли ошибка с неизвестным нам статусом
            val mapper = ObjectMapper()
            val rootNode = mapper.readTree(json)

            if (rootNode.elements().hasNext()) {
                val element = rootNode.elements().next()
                if (element.has("errmsg")) {
                    errmsg = element["errmsg"]!!.asText()
                }
            }

            return when (statusLine.statusCode) {
                200 -> {
                    AsyncResultJson(
                        true,
                        if (errmsg.isEmpty()) "status code: ${statusLine.statusCode}"
                        else "status code: ${statusLine.statusCode}, errmsg: $errmsg", json
                    )
                }
                else -> {
                    AsyncResultJson(false, "status code: ${statusLine.statusCode}, errmsg: $errmsg.", json)
                }
            }
        } catch (e: Exception) {
            return AsyncResultJson(false, "Ошибка: ${e.message}", json)
        }
    }

    // Получение блоба с сервера
    // *****************************************************************
    private fun getDataFromServerBlob(params: String): AsyncResultBlob {
        var path =
            "${context.getString(R.string.server_uri)}:${context.getString(R.string.server_port)}/${
                context.getString(
                    R.string.server_path_get
                )
            }/get_file"

        if (params.isNotEmpty())
            path += "?" + params.trim().replace(" ", "%20")

        val b = Uri.parse(path).buildUpon().build()

        val response: HttpResponse
        var blob: ByteArray? = null
        try {
            val request = HttpPost(b.toString())
            response = DefaultHttpClient().execute(request)

            val statusLine = response.statusLine
            when (statusLine.statusCode) {
                200 -> {
                    val entity = response.entity
                    val inputStream = entity.content
                    val outputStream = ByteArrayOutputStream()
                    val buffer = ByteArray(1024)

                    var len: Int = inputStream.read(buffer)
                    while (len > 0) {
                        outputStream.write(buffer, 0, len)
                        len = inputStream.read(buffer)
                    }

                    blob = outputStream.toByteArray()

                    outputStream.close()
                    inputStream.close()

                    return AsyncResultBlob(true, "Status code: ${statusLine.statusCode}", blob)
                }
                204 -> {
                    return AsyncResultBlob(false, "Status code: ${statusLine.statusCode}", blob)
                }
                500 -> {
                    return AsyncResultBlob(
                        false,
                        "Status code: ${statusLine.statusCode}. Не удалось подключиться к серверу.",
                        null
                    )
                }
                else -> {
                    return AsyncResultBlob(false, "Status code: ${statusLine.statusCode}", blob)
                }
            }
        } catch (e: Exception) {
            return AsyncResultBlob(false, "Ошибка: ${e.message}", blob)
        }
    }

}