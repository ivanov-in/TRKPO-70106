package ru.infoenergo.mis

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.listview_tasks_route.*
import kotlinx.coroutines.*
import ru.infoenergo.mis.adapters.AdapterListTasks
import ru.infoenergo.mis.dbhandler.DBHandlerServerRead
import ru.infoenergo.mis.dbhandler.DBHandlerServerWrite
import ru.infoenergo.mis.dbhandler.DbHandlerLocalRead
import ru.infoenergo.mis.dbhandler.DbHandlerLocalWrite
import ru.infoenergo.mis.helpers.*
import java.io.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.schedule
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private var cnt_updates: Int = 0

/** ************************************************* **/
/**         Список заданий (маршрутный лист)          **/
/** ************************************************* **/
class ListTasksActivity : AppCompatActivity() {

    private var _calendarDate: LocalDate = LocalDate.now()
    private var _idInspector: Int = 0
    private var _taskList: ArrayList<Task> = ArrayList()
    private lateinit var lvTasksList: ListView
    private lateinit var tvMessage: TextView

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.listview_tasks_route)

        _idInspector = intent.getIntExtra("ID_INSPECTOR", 0)
        val dbLocalRead = DbHandlerLocalRead(this@ListTasksActivity, null)
        val fio = dbLocalRead.getFioInspector(_idInspector)
        dbLocalRead.close()
        if (_idInspector == 0)
            finish()

        //Разрешить кнопку назад
        if (supportActionBar != null) {
            val actionBar = supportActionBar
            actionBar!!.title =
                "Маршрутный лист на ${_calendarDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}"
            actionBar.subtitle = "Инспектор: $fio"
            actionBar.elevation = 4.0F
            actionBar.setDisplayHomeAsUpEnabled(false)
        }
        lvTasksList = findViewById(R.id.listViewRouteTasks)
        tvMessage = findViewById(R.id.tvRouteMessage)

        refreshDataLocal()
        changeDateDialog()

    }

    // Обновление данных с проверкой наличия к интернету
    // -------------------------------------------------
    private fun refreshData() {
        supportActionBar!!.title =
            "Маршрутный лист на ${_calendarDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))}"

        val networkAvailable = isNetworkAvailable(this)
        if (networkAvailable) {
            openProgressBar(log = true)
            refreshDataServer()
            cnt_updates = 0
            this@ListTasksActivity.invalidateOptionsMenu()
        } else {
            refreshDataLocal()
        }
    }

    // Обновление данных, получение с сервера
    // -------------------------------------------------
    private fun refreshDataServer(requestTaskStatus: Int? = null) {
        var requestTaskStatus = requestTaskStatus
        try {
            val date = _calendarDate

            // Если база была затёрта, то отправляем статус -1, чтобы получить данные заново
            // -----------------------------------------------------------------------------------
            val dbLocalRead = DbHandlerLocalRead(this@ListTasksActivity, null)
            val cntTasks = dbLocalRead.getTaskCount(_idInspector)
            dbLocalRead.close()
            if (cntTasks == 0)
                requestTaskStatus = -1
            // **********************************************************
            uiScope.launch {
                val dbWritableLocal = DbHandlerLocalWrite(this@ListTasksActivity, null)

                if (requestTaskStatus == null || requestTaskStatus == -1) {
                    val purpose =
                        async(Dispatchers.IO) { DBHandlerServerRead(this@ListTasksActivity).purposesAsync() }
                    val res = purpose.await()
                    if (res.toLowerCase().contains("connection") && res.contains("refused")) {
                        tvMessage.text =
                            "${tvMessage.text}. Ошибка соединения с сервером. Попробуйте обновить данные позже.\n"
                        println("$TAG_ERR: async: $res")
                        delay(5000)
                        refreshDataLocal()
                        return@launch
                    }
                    tvMessage.text = tvMessage.text as String + res + "\n"
                }
                // ****************************************************************
                tvMessage.text = tvMessage.text as String + "Загрузка данных. . . "
                val actsJson = async(Dispatchers.IO) {
                    return@async DBHandlerServerRead(this@ListTasksActivity).actsAsync()
                }.await()

                if (!actsJson.ok) {
                    println("$TAG_ERR: async acts: ${actsJson.error}")
                    if (isConnectionFailed(actsJson)) return@launch
                }
                // ****************************************************************
                tvMessage.text = tvMessage.text as String + ". "
                val history = async(Dispatchers.IO) {
                    return@async DBHandlerServerRead(this@ListTasksActivity).historyAsync(
                        date,
                        _idInspector,
                        requestTaskStatus
                    )
                }.await()
                if (!history.ok) {
                    println("$TAG_ERR: async history: ${history.error}")
                    if (isConnectionFailed(history)) return@launch
                }
                // ****************************************************************
                tvMessage.text = tvMessage.text as String + ". "
                val dogData = async(Dispatchers.IO) {
                    return@async DBHandlerServerRead(this@ListTasksActivity).dogDataAsync(
                        date,
                        _idInspector,
                        requestTaskStatus
                    )
                }.await()
                if (!dogData.ok) {
                    println("$TAG_ERR: async dogData: ${dogData.error}")
                    if (isConnectionFailed(dogData)) return@launch
                }
                // --------------------------
                tvMessage.text = tvMessage.text as String + ". "
                val dogDataObj = async(Dispatchers.IO) {
                    return@async DBHandlerServerRead(this@ListTasksActivity).dogObjAsync(
                        date,
                        _idInspector,
                        requestTaskStatus
                    )
                }.await()
                if (!dogDataObj.ok) {
                    println("$TAG_ERR: async dogDataObj: ${dogDataObj.error}")
                    if (isConnectionFailed(dogDataObj)) return@launch
                }
                // --------------------------
                tvMessage.text = tvMessage.text as String + ". "
                val dogDataTu = async(Dispatchers.IO) {
                    return@async DBHandlerServerRead(this@ListTasksActivity).dogTuAsync(
                        date,
                        _idInspector,
                        requestTaskStatus
                    )
                }.await()
                if (!dogDataTu.ok) {
                    println("$TAG_ERR: async dogDataTu: ${dogDataTu.error}")
                    if (isConnectionFailed(dogDataTu)) return@launch
                }
                // --------------------------
                tvMessage.text = tvMessage.text as String + ". "
                val podpisant = async(Dispatchers.IO) {
                    return@async DBHandlerServerRead(this@ListTasksActivity).podpisantAsync(
                        date,
                        _idInspector,
                        requestTaskStatus
                    )
                }.await()
                if (!podpisant.ok) {
                    println("$TAG_ERR: async podpisant: ${podpisant.error}")
                    if (isConnectionFailed(dogDataTu)) return@launch
                }
                // --------------------------
                tvMessage.text = tvMessage.text as String + ". "
                val dogDataUu = async(Dispatchers.IO) {
                    return@async DBHandlerServerRead(this@ListTasksActivity).dogUuAsync(
                        date,
                        _idInspector,
                        requestTaskStatus
                    )
                }.await()
                if (!dogDataUu.ok) {
                    println("$TAG_ERR: async dogDataUu: ${dogDataUu.error}")
                    if (isConnectionFailed(dogDataUu)) return@launch
                }
                // --------------------------
                tvMessage.text = tvMessage.text as String + ". "
                val dogDataUuSi = async(Dispatchers.IO) {
                    return@async DBHandlerServerRead(this@ListTasksActivity).dogUuSiAsync(
                        date,
                        _idInspector,
                        requestTaskStatus
                    )
                }.await()
                if (!dogDataUuSi.ok) {
                    println("$TAG_ERR: async dogDataUuSi: ${dogDataUuSi.error}")
                    if (isConnectionFailed(dogDataUuSi)) return@launch
                }
                // *************************************************************
                tvMessage.text = tvMessage.text as String + ". "
                withContext(Dispatchers.IO) {
                    if (actsJson.ok) actsJson.ok = dbWritableLocal.insertTaskActsFromServer(actsJson.json)
                    return@withContext
                }
                tvMessage.text = if (actsJson.ok)
                    tvMessage.text as String + " прошла успешно\n"
                else tvMessage.text as String + " прошла с ОШИБКОЙ\n"


                // TODO это всё проверить после маршрутного листа
                // *************************************************************
                tvMessage.text = tvMessage.text as String + "Загрузка истории посещений... "

                withContext(Dispatchers.IO) {
                    if (history.ok) history.ok = dbWritableLocal.insertHistoryFromServer(history.json)
                    return@withContext
                }
                tvMessage.text = if (history.ok)
                    tvMessage.text as String + " прошла успешно\n"
                else tvMessage.text as String + " прошла с ОШИБКОЙ\n"
                // **************************************************************
                tvMessage.text = tvMessage.text as String + "Загрузка данных по договорам... "
                withContext(Dispatchers.IO) {
                    if (dogData.ok) dogData.ok = dbWritableLocal.insertDogDataFromServer(dogData.json)
                }
                tvMessage.text = if (dogData.ok)
                    tvMessage.text as String + " прошла успешно\n"
                else tvMessage.text as String + " прошла с ОШИБКОЙ\n"

                // **************************************************************
                tvMessage.text = tvMessage.text as String + "Загрузка данных по объектам договоров..."
                withContext(Dispatchers.IO) {
                    if (dogDataObj.ok) dogDataObj.ok = dbWritableLocal.insertDogObjFromServer(dogDataObj.json)
                }

                tvMessage.text = if (dogDataObj.ok)
                    tvMessage.text as String + " прошла успешно\n"
                else tvMessage.text as String + " прошла с ОШИБКОЙ\n"

                // ***************************************************************
                tvMessage.text = tvMessage.text as String + "Загрузка данных по ТУ договоров... "

                withContext(Dispatchers.IO) {
                    if (dogDataTu.ok) dogDataTu.ok = dbWritableLocal.insertDogTuFromServer(dogDataTu.json)
                }

                tvMessage.text = if (dogDataTu.ok)
                    tvMessage.text as String + " прошла успешно\n"
                else tvMessage.text as String + " прошла с ОШИБКОЙ\n"

                // **************************************************************
                tvMessage.text = tvMessage.text as String + "Загрузка данных по УУ договоров... "
                withContext(Dispatchers.IO) {
                    if (dogDataUu.ok) dogDataUu.ok = dbWritableLocal.insertDogUuFromServer(dogDataUu.json)
                }

                tvMessage.text = if (dogDataUu.ok)
                    tvMessage.text as String + " прошла успешно\n"
                else tvMessage.text as String + " прошла с ОШИБКОЙ\n"

                // **************************************************************
                tvMessage.text = tvMessage.text as String + "Загрузка данных по УУ СИ договоров... "
                withContext(Dispatchers.IO) {
                    if (dogDataUuSi.ok) dogDataUuSi.ok = dbWritableLocal.insertDogUuSiFromServer(dogDataUuSi.json)
                }

                tvMessage.text = if (dogDataUuSi.ok)
                    tvMessage.text as String + " прошла успешно\n"
                else tvMessage.text as String + " прошла с ОШИБКОЙ\n"

                // **************************************************************
                tvMessage.text = tvMessage.text as String + "Загрузка подписантов... "
                withContext(Dispatchers.IO) {
                    if (podpisant.ok) podpisant.ok = dbWritableLocal.insertPodpisantFromServer(podpisant.json)
                }

                tvMessage.text = if (podpisant.ok)
                    tvMessage.text as String + " прошла успешно\n"
                else tvMessage.text as String + " прошла с ОШИБКОЙ\n"

                // *************************************************************
                tvMessage.text = tvMessage.text as String + "Загрузка файлов..."
                val files = withContext(Dispatchers.IO) {
                    return@withContext DBHandlerServerRead(this@ListTasksActivity).taskFilesAsync(
                        date, _idInspector, requestTaskStatus
                    )
                }
                withContext(Dispatchers.IO) {
                    if (files.ok) files.ok = dbWritableLocal.insertFilesFromServer(files.array, history = false)
                }

                if (!files.ok && files.error.contains("500") || files.error.toLowerCase()
                        .contains("connection") || files.error.toLowerCase().contains(
                        "refused"
                    )
                ) {
                    println("$TAG_ERR: ${files.error}")
                    tvMessage.text =
                        "${tvMessage.text}. Ошибка соединения с сервером. Попробуйте обновить данные позже.\n"
                    delay(5000)
                    refreshDataLocal()
                    return@launch
                }

                // проходимся по списку файлов, получаем blob с сервера, прикрепляем к файлу и потом записываем в локальную БД
                var cnt = files.array.size
                var i = 1
                files.array.forEach { f ->
                    tvMessage.text = (tvMessage.text as String).substringBeforeLast("файлов...")
                    tvMessage.text = tvMessage.text as String + "файлов... $i из $cnt "
                    i += 1

                    if (!files.ok) return@forEach
                    withContext(Dispatchers.IO) {
                        val blob = DBHandlerServerRead(this@ListTasksActivity)
                            .taskFilesBlobAsync(f.id_file)
                        if (blob.ok && blob.array != null) {
                            dbWritableLocal.updateFile(f, blob.array)
                        } else {
                            if (!blob.ok) {
                                println("$TAG_ERR updateFile: ${blob.error}")
                                files.ok = false
                                files.error = blob.error
                                return@withContext
                            }
                        }
                    }
                    if (!files.ok) return@forEach
                }

                // Файлы для истории посещений
                // ***********************************************************
                val filesHist = withContext(Dispatchers.IO) {
                    return@withContext DBHandlerServerRead(this@ListTasksActivity).taskFilesHistoryAsync(
                        date, _idInspector, requestTaskStatus
                    )
                }
                withContext(Dispatchers.IO) {
                    if (filesHist.ok) filesHist.ok = dbWritableLocal
                        .insertFilesFromServer(filesHist.array, history = true)
                }

                if (!filesHist.ok && filesHist.error.contains("500") || filesHist.error.toLowerCase()
                        .contains("connection") || filesHist.error.toLowerCase().contains(
                        "refused"
                    )
                ) {
                    println("$TAG_ERR: ${filesHist.error}")
                    tvMessage.text =
                        "${tvMessage.text}. Ошибка соединения с сервером. Попробуйте обновить данные позже.\n"
                    delay(5000)
                    refreshDataLocal()
                    return@launch
                }

                // проходимся по списку файлов, получаем blob с сервера, прикрепляем к файлу и потом записываем в локальную БД
                cnt = filesHist.array.size
                i = 1
                filesHist.array.forEach { f ->
                    tvMessage.text = (tvMessage.text as String).substringBeforeLast("файлов... ")
                    tvMessage.text = tvMessage.text as String + "файлов... $i из $cnt "
                    i += 1

                    if (!filesHist.ok) return@forEach
                    withContext(Dispatchers.IO) {
                        val blob = DBHandlerServerRead(this@ListTasksActivity)
                            .taskFilesBlobAsync(f.id_file)
                        if (blob.ok && blob.array != null) {
                            dbWritableLocal.updateFile(f, blob.array, history = true)
                        } else {
                            if (!blob.ok) {
                                println("$TAG_ERR updateFile: ${blob.error}")
                                filesHist.ok = false
                                filesHist.error = blob.error
                                return@withContext
                            }
                        }
                    }
                    if (!filesHist.ok) return@forEach
                }


                tvMessage.text = if (files.ok)
                    tvMessage.text as String + " прошла успешно\n"
                else tvMessage.text as String + " прошла с ОШИБКОЙ\n"

                if (!files.ok && (files.error.contains("500") || files.error.contains("404")
                            || files.error.toLowerCase().contains("connection") || files.error.toLowerCase()
                        .contains("refused"))
                ) {
                    tvMessage.text =
                        "${tvMessage.text}. Ошибка соединения с сервером. Попробуйте обновить данные позже.\n"
                    delay(5000)
                    refreshDataLocal()
                    return@launch
                }

                // *************************************************************
                // в результате получаем список полей актов, которые уже есть на устройстве, но были найдены различия
                tvMessage.text = tvMessage.text as String + "Загрузка актов... "
                val fields = withContext(Dispatchers.IO) {
                    return@withContext DBHandlerServerRead(this@ListTasksActivity).actFieldsAsync(
                        date, _idInspector, requestTaskStatus
                    )
                }
                tvMessage.text = if (fields.ok)
                    tvMessage.text as String + " прошла успешно\n"
                else tvMessage.text as String + " прошла с ОШИБКОЙ\n"

                if (!fields.ok && fields.error.contains("500") || fields.error.toLowerCase()
                        .contains("connection") || fields.error.toLowerCase().contains(
                        "refused"
                    )
                ) {
                    println("$TAG_ERR: ${fields.error}")
                    tvMessage.text =
                        "${tvMessage.text}. Ошибка соединения с сервером. Попробуйте обновить данные позже.\n"
                    delay(5000)
                    refreshDataLocal()
                    return@launch
                }

                // *************************************************************
                tvMessage.text = tvMessage.text as String + "Загрузка дополнительных данных для актов... "
                // в результате получаем список доп полей актов, которые уже есть на устройстве, но были найдены различия
                val dops = withContext(Dispatchers.IO) {
                    return@withContext DBHandlerServerRead(this@ListTasksActivity).actFieldsDopAsync(
                        date, _idInspector, requestTaskStatus
                    )
                }
                tvMessage.text = if (dops.ok)
                    tvMessage.text as String + " прошла успешно\n"
                else tvMessage.text as String + " прошла с ОШИБКОЙ\n"

                if (!dops.ok && dops.error.contains("500") || dops.error.toLowerCase()
                        .contains("connection") || dops.error.toLowerCase().contains("refused")
                ) {
                    println("$TAG_ERR: ${dops.error}")
                    tvMessage.text =
                        "${tvMessage.text}. Ошибка соединения с сервером. Попробуйте обновить данные позже.\n"
                    delay(5000)
                    refreshDataLocal()
                    return@launch
                }


                // *************************************************************
                tvMessage.text = tvMessage.text as String + "Загрузка подписантов... "

                withContext(Dispatchers.IO) {
                    if (dogDataTu.ok) dogDataTu.ok = dbWritableLocal.insertDogTuFromServer(dogDataTu.json)
                }

                tvMessage.text = if (dogDataTu.ok)
                    tvMessage.text as String + " прошла успешно\n"
                else tvMessage.text as String + " прошла с ОШИБКОЙ\n"


                // *************************************************************
                // в результате получаем список заявок, которые уже есть на устройстве, но были найдены различия
                tvMessage.text = tvMessage.text as String + "Загрузка маршрутного листа... "
                val tasks = withContext(Dispatchers.IO) {
                    return@withContext DBHandlerServerRead(this@ListTasksActivity).tasksAsync(
                        date, _idInspector, requestTaskStatus
                    )
                }

                tvMessage.text = if (tasks.ok)
                    tvMessage.text as String + " прошла успешно\n"
                else tvMessage.text as String + " прошла с ОШИБКОЙ\n"

                if (!tasks.ok && tasks.error.contains("500") || tasks.error.toLowerCase()
                        .contains("connection") && tasks.error.toLowerCase().contains(
                        "refused"
                    )
                ) {
                    println("$TAG_ERR: ${tasks.error}")
                    tvMessage.text =
                        "${tvMessage.text}. Ошибка соединения с сервером. Попробуйте обновить данные позже.\n"
                    refreshDataLocal()
                    return@launch
                }

                // *************************************************************
                if (requestTaskStatus == 2) {
                    refreshDataLocal()
                    dbWritableLocal.close()
                } else {

                    val taskAccept = ArrayList<Int>()
                    val taskRefuse = ArrayList<Int>()

                    // ************************
                    tasks.array.forEach { task ->
                        suspendCoroutine<Boolean> {
                            if (task.status == 12) {
                                fields.array.removeIf { field -> field.id_task == task.id_task }
                                dops.array.removeIf { dop -> dop.id_task == task.id_task }
                                it.resume(true)
                            } else {
                                val msg =
                                    if (task.payer_name.isNotEmpty()) "В задании ${task.id_task} (${task.address} / ${task.payer_name}) " +
                                            "есть изменения:\n\n ${task.diff}. \n\nПринять или отклонить?"
                                    else "В задании ${task.id_task} (${task.address}) " +
                                            "есть изменения:\n\n ${task.diff}. \n\nПринять или отклонить?"
                                val dialog = AlertDialog.Builder(this@ListTasksActivity)
                                    .setMessage(msg)
                                    .setIcon(R.drawable.ic_warning_triangle)
                                    .setPositiveButton("Принять") { dlg, _ ->
                                        dbWritableLocal.updateTask(task)

                                        // пытаемся найти изменения в полях актов и записываем в бд
                                        fields.array.forEach { field ->
                                            if (field.id_task == task.id_task && task.status != 12)
                                                dbWritableLocal.updateActFields(field, fromServer = true)
                                        }
                                        fields.array.removeIf { field -> field.id_task == task.id_task }

                                        // пытаемся найти изменения в доп полях актов и записываем в бд
                                        dops.array.forEach { dop ->
                                            if (dop.id_task == task.id_task && task.status != 12)
                                                dbWritableLocal.updateActFieldsDopShablon(dop)
                                        }
                                        dops.array.removeIf { dop -> dop.id_task == task.id_task }

                                        if (!taskAccept.contains(task.id_task))
                                            taskAccept.add(task.id_task)

                                        it.resume(true)
                                        dlg.dismiss()
                                    }
                                    .setNegativeButton("Отклонить") { dlg, _ ->
                                        fields.array.removeIf { field -> field.id_task == task.id_task }
                                        dops.array.removeIf { dop -> dop.id_task == task.id_task }

                                        if (!taskRefuse.contains(task.id_task))
                                            taskRefuse.add(task.id_task)

                                        it.resume(true)
                                        dlg.dismiss()
                                    }
                                    .setCancelable(false)
                                    .create()

                                dialog.setCanceledOnTouchOutside(false)
                                dialog.show()
                            }
                        }
                    }
                    var accept = true

                    val fieldsIds = fields.array.map { it.id_task }.distinct()
                    fieldsIds.forEach { id ->
                        suspendCoroutine<Boolean> {

                            val dialog = AlertDialog.Builder(this@ListTasksActivity)
                                .setTitle("Внимание")
                                .setIcon(R.drawable.ic_warning_triangle)
                                .setMessage("В АКТАХ задания $id есть изменения. Принять или отклонить?")
                                .setPositiveButton("Принять") { dlg, _ ->
                                    accept = true
                                    fields.array.forEach { f ->
                                        if (f.id_task == id)
                                            dbWritableLocal.updateActFields(f, fromServer = true)
                                    }
                                    // пытаемся найти изменения в доп полях актов и записываем в бд
                                    dops.array.forEach { dop ->
                                        if (dop.id_task == id)
                                            dbWritableLocal.updateActFieldsDopShablon(dop)
                                    }
                                    dops.array.removeIf { dop -> dop.id_task == id }
                                    it.resume(true)
                                    dlg.dismiss()
                                }
                                .setNegativeButton("Отклонить") { dlg, _ ->
                                    accept = false
                                    dops.array.removeIf { dop -> dop.id_task == id }
                                    it.resume(true)
                                    dlg.dismiss()
                                }
                                .setCancelable(false)
                                .create()

                            dialog.setCanceledOnTouchOutside(false)
                            dialog.show()
                        }
                        if (accept) {
                            if (!taskAccept.contains(id))
                                taskAccept.add(id)
                        } else {
                            if (!taskRefuse.contains(id))
                                taskRefuse.add(id)
                        }
                    }

                    val dopsIds = dops.array.map { it.id_task }.distinct()
                    dopsIds.forEach { id ->
                        suspendCoroutine<Boolean> {
                            val dialog = AlertDialog.Builder(this@ListTasksActivity)
                                .setTitle("Внимание")
                                .setIcon(R.drawable.ic_warning_triangle)
                                .setMessage("В АКТАХ задания $id есть изменения. Принять или отклонить?")
                                .setPositiveButton("Принять") { dlg, _ ->
                                    accept = true
                                    dops.array.forEach { dop ->
                                        if (dop.id_task == id)
                                            dbWritableLocal.updateActFieldsDopShablon(dop)
                                    }
                                    it.resume(true)
                                    dlg.dismiss()
                                }
                                .setNegativeButton("Отклонить") { dlg, _ ->
                                    accept = false
                                    it.resume(true)
                                    dlg.dismiss()
                                }
                                .setCancelable(false)
                                .create()

                            dialog.setCanceledOnTouchOutside(false)
                            dialog.show()
                        }

                        if (accept) {
                            if (!taskAccept.contains(id))
                                taskAccept.add(id)
                        } else {
                            if (!taskRefuse.contains(id))
                                taskRefuse.add(id)
                        }
                    }

                    dbWritableLocal.close()

                    // Отправка на сервер решения пользователя
                    // -------------------------------------------
                    val ok = withContext(Dispatchers.IO) {
                        taskAccept.forEach {
                            DBHandlerServerWrite(this@ListTasksActivity).apply { acceptTask(it) }
                        }
                        taskRefuse.forEach {
                            DBHandlerServerWrite(this@ListTasksActivity).refuseTask(it)
                        }
                        return@withContext true
                    }
                    if (ok) {
                        delay(3000)
                        refreshDataLocal()
                    }
                }
            }
        } catch (e: Exception) {
            println("$TAG_ERR: ${e.message}")
            refreshDataLocal()
        }
    }

    // Проверка не было ли сброшено соединение
    // --------------------------------------------------------------------
    private suspend fun isConnectionFailed(asyncResult: AsyncResultJson): Boolean {
        if (asyncResult.error.contains("500") || asyncResult.error.contains("connection", true)
            || asyncResult.error.contains("refused", true) || asyncResult.error.contains("timed out", true)
        ) {
            tvMessage.text =
                "${tvMessage.text}. Ошибка соединения с сервером. Попробуйте обновить данные позже.\n"
            delay(5000)
            refreshDataLocal()
            return true
        }
        return false
    }

    // Загрузка данных с локальной бд
    // ------------------------------
    private fun refreshDataLocal(): Boolean {
        return try {
            val dbLocalRead = DbHandlerLocalRead(this, null)
            _taskList = dbLocalRead.getTaskList(_calendarDate, _idInspector)
            dbLocalRead.close()

            val purposes = getListPurpose()
            // создаем адаптер
            if (_taskList.size != 0) {
                val adapter = AdapterListTasks(this@ListTasksActivity, _taskList, purposes)
                lvTasksList.adapter = adapter
                (lvTasksList.adapter as AdapterListTasks).notifyDataSetChanged()
            } else {
                if (lvTasksList.adapter != null) {
                    (lvTasksList.adapter as AdapterListTasks).clear()
                    (lvTasksList.adapter as AdapterListTasks).notifyDataSetChanged()
                }
            }

            loadAssetsToCard()
            closeProgressBar()
            true
        } catch (e: Exception) {
            println("$TAG_ERR refresh data local: ${e.message}")
            true
        }
    }

    // Отправка всех данных с устройства на сервер
    // -------------------------------------------
    private fun uploadDataToServer() {
        uiScope.launch {

            val send = async(Dispatchers.IO) {
                try {
                    DBHandlerServerWrite(this@ListTasksActivity).sendAllData(_idInspector)
                    true
                } catch (e: Exception) {
                    println("$TAG_ERR uploadDataToServer: ${e.message}")
                    false
                }
            }

            if (send.await()) {

                // Удаление файлов задач из БД
                // ---------------------------
                val dbLocalWrite = DbHandlerLocalWrite(this@ListTasksActivity, null)
                val deleted = dbLocalWrite.deleteOldData(_idInspector)
                dbLocalWrite.close()

                // Удаление файлов задач с устройства
                // ----------------------------------
                if (deleted.first || deleted.second.isNotBlank()) {
                    val external = getExternalFilesDir(null)
                    val taskIdsToDel = deleted.second.split(",").toTypedArray()
                    taskIdsToDel.forEach {
                        try {
                            val taskFolder = File("$external/tmpFiles/$it")
                            if (taskFolder.exists()) {
                                if (taskFolder.deleteRecursively())
                                    println("$TAG_OK deleted tmpFiles/$it")
                                else
                                    println("$TAG_ERR act NOT deleted tmpFiles/$it")
                            }
                        } catch (e: Exception) {
                            println("$TAG_ERR taskIdsToDel: ${e.message}")
                        }
                    }

                    // Удаляем из старой папки актов pdfActs (со времене
                    taskIdsToDel.forEach {
                        try {
                            val pdfsFolder = File("$external/pdfActs")
                            if (pdfsFolder.exists()) {
                                val oldActs = pdfsFolder.listFiles()
                                for (act in oldActs!!) {
                                    val nameSplit = act.name.split("-")
                                    if (nameSplit[2].isNotEmpty() && nameSplit[2] == it ||
                                        nameSplit[2].isEmpty() && "-${nameSplit[3]}" == it
                                    ) {
                                        if (act.delete())
                                            println("$TAG_OK deleted pdfActs/${act.name}")
                                        else
                                            println("$TAG_ERR act NOT deleted pdfActs/${act.name} ")

                                    }
                                }
                            }
                        } catch (e: Exception) {
                            println("$TAG_ERR taskIdsToDel: ${e.message}")
                        }
                    }

                }
                refreshDataLocal()
                AlertDialog.Builder(this@ListTasksActivity)
                    .setTitle("Внимание!")
                    .setIcon(R.drawable.ic_ok)
                    .setMessage("Данные успешно отправлены.")
                    .show()
            } else {
                refreshDataLocal()
            }
        }
    }

    // Загрузка шаблонов из ассетов приложения
    // ---------------------------------------
    private fun loadAssetsToCard() {
        try {
            val external = this.baseContext.getExternalFilesDir(null)
            val externalPath = File("$external")
            if (!externalPath.exists())
                externalPath.mkdirs()

            val templatesFile = File("$external/templates")
            val imgFile = File("$external/Img")
            val fontsFile = File("$external/fonts")

            if (!templatesFile.exists())
                templatesFile.mkdirs()
            if (!imgFile.exists())
                imgFile.mkdirs()
            if (!fontsFile.exists())
                fontsFile.mkdirs()

            assets.list("templates")?.forEach { it ->
                try {
                    val actTemplate = File("${externalPath.path}/templates/$it")
                    if (actTemplate.exists()) {
                        if (!actTemplate.delete()) actTemplate.deleteRecursively()
                    }

                    if (!actTemplate.exists()) {
                        if (actTemplate.createNewFile()) {
                            val outStream = FileOutputStream(actTemplate)
                            val inpStream = assets.open("templates/$it")
                            val buffer = ByteArray(1024)
                            var len = inpStream.read(buffer)
                            while (len > 0) {
                                outStream.write(buffer, 0, len)
                                len = inpStream.read(buffer)
                            }
                        }
                    }
                } catch (e: FileNotFoundException) {
                    println("$TAG_ERR templates copy: $e")
                } catch (e: Exception) {
                    println("$TAG_ERR templates copy: $e")
                }
            }

            assets.list("Img")?.forEach { it ->
                try {
                    val actTemplate = File("${externalPath.path}/Img/$it")
                    if (!actTemplate.exists()) {
                        if (actTemplate.createNewFile()) {
                            val outStream = FileOutputStream(actTemplate)
                            val inpStream = assets.open("Img/$it")
                            val buffer = ByteArray(1024)
                            var len = inpStream.read(buffer)
                            while (len > 0) {
                                outStream.write(buffer, 0, len)
                                len = inpStream.read(buffer)
                            }
                        }
                    }
                } catch (e: FileNotFoundException) {
                    println("$TAG_ERR assets Img copy: $e")
                } catch (e: Exception) {
                    println("$TAG_ERR assets Img copy: $e")
                }
            }

            assets.list("fonts")?.forEach { it ->
                try {
                    val actTemplate = File("${externalPath.path}/fonts/$it")
                    if (!actTemplate.exists()) {
                        if (actTemplate.createNewFile()) {
                            val outStream = FileOutputStream(actTemplate)
                            val inpStream = assets.open("fonts/$it")
                            val buffer = ByteArray(1024)
                            var len = inpStream.read(buffer)
                            while (len > 0) {
                                outStream.write(buffer, 0, len)
                                len = inpStream.read(buffer)
                            }
                        }
                    }
                } catch (e: FileNotFoundException) {
                    println("$TAG_ERR fonts copy: $e")
                } catch (e: Exception) {
                    println("$TAG_ERR fonts copy: $e")
                }
            }

        } catch (e: Exception) {
            println("$TAG_ERR assets copy: $e")
        }
    }

    private fun openProgressBar(log: Boolean = false, upload: Boolean = false) {
        lvTasksList.visibility = View.GONE
        if (upload) {
            progressBarLvTasksUpload?.visibility = View.VISIBLE
            progressBarLvTasks?.visibility = View.GONE
        } else {
            progressBarLvTasks?.visibility = View.VISIBLE
            progressBarLvTasksUpload?.visibility = View.GONE
        }
        if (log) {
            tvMessage.visibility = View.VISIBLE
            tvMessage.text = ""
        }
    }

    private fun closeProgressBar() {
        progressBarLvTasks?.visibility = View.GONE
        progressBarLvTasksUpload?.visibility = View.GONE
        lvTasksList.visibility = View.VISIBLE
        tvMessage.visibility = View.GONE
        tvMessage.text = ""
    }

    // Список целей для spinner
    // ------------------------------------------
    private fun getListPurpose(): Array<String> {
        val dbLocalRead = DbHandlerLocalRead(this@ListTasksActivity, null)
        val purpose = dbLocalRead.getPurposeList()
        dbLocalRead.close()
        return purpose.toTypedArray()
    }

    private var isChecked: Boolean = false

    // Подключение тулбар меню
    // --------------------------------------------------
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu_route_list, menu)
        try {
            val item: MenuItem = menu.findItem(R.id.menu_update_task_list_count)
            item.title = if (cnt_updates == 0) "$cnt_updates " else "Загрузить задания с центральной базы"
        } catch (e: Exception) {
            println("$TAG_ERR onCreateOptionsMenu ${e.message}")
        }

        try {
            val today: MenuItem = menu.findItem(R.id.menu_today)
            today.isChecked = isChecked
        } catch (e: Exception) {
            println("$TAG_ERR onCreateOptionsMenu ${e.message}")
        }
        return super.onCreateOptionsMenu(menu)
    }

    // Выбор значения из тулбар меню
    // --------------------------------------------------------
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            // Загрузить данные на сервер
            // ---------
            R.id.menu_upload_data -> {
                if (progressBarLvTasks?.visibility == View.VISIBLE)
                    return true

                if (isNetworkAvailable(this)) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Внимание")
                        .setIcon(R.drawable.ic_question)
                        .setMessage(
                            "Вы подтверждаете отправку всех данных в центральную базу?\n" +
                                    "ВНИМАНИЕ!\n " +
                                    "Данные, которые старше 7 дней, после отправки в центральную базу будут УДАЛЕНЫ с устройства."
                        )
                        .setPositiveButton("Да") { _, _ ->
                            openProgressBar(log = false, upload = true)
                            uploadDataToServer()
                        }
                        .setNegativeButton("Нет") { dialog, _ ->
                            dialog.cancel()
                        }
                    builder.show()
                } else {
                    AlertDialog.Builder(this).setTitle("Внимание")
                        .setIcon(R.drawable.ic_warning_triangle)
                        .setMessage("Отсутствует соединение с сетью, отправка невозможна.")
                        .setPositiveButton("ОК") { dialog, _ ->
                            dialog.cancel()
                        }
                        .show()
                }
                return true
            }
            // Обновить
            // ---------
            R.id.menu_update_task_list_count -> {
                if (progressBarLvTasks?.visibility == View.VISIBLE)
                    return true

                if (isNetworkAvailable(this)) {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Внимание")
                        .setIcon(R.drawable.ic_question)
                        .setMessage("Загрузить данные с центральной базы?")
                        .setPositiveButton("Да") { _, _ ->
                            openProgressBar(log = true)
                            refreshDataServer()
                            cnt_updates = 0
                            this@ListTasksActivity.invalidateOptionsMenu()
                        }
                        .setNegativeButton("Нет") { dialog, _ ->
                            dialog.cancel()
                        }
                    builder.show()

                } else {
                    refreshDataLocal()
                }

                return true
            }
            // Отобразить объекты на карте
            // ---------------------------
            R.id.menu_map_task_list -> {
                return true
            }
            // Задачи только на сегодня
            // ------------------------
            R.id.menu_today -> {
                if (progressBarLvTasks?.visibility == View.VISIBLE)
                    return true
                isChecked = !isChecked
                item.isChecked = isChecked

                if (item.isChecked) {
                    val today = _taskList.filter { it.dat == LocalDate.now() } as ArrayList<Task>
                    val adapter = AdapterListTasks(this@ListTasksActivity, today, getListPurpose())
                    lvTasksList.adapter = adapter
                    (lvTasksList.adapter as AdapterListTasks).notifyDataSetChanged()
                } else {
                    val adapter = AdapterListTasks(this@ListTasksActivity, _taskList, getListPurpose())
                    lvTasksList.adapter = adapter
                    (lvTasksList.adapter as AdapterListTasks).notifyDataSetChanged()
                }

                return true
            }
            // Календарь
            // ---------
            R.id.menu_calendar_task_list -> {
                if (progressBarLvTasks?.visibility == View.VISIBLE)
                    return true

                changeDateDialog()

                return true
            }
            // Поиск информации по абоненту
            // ----------------------------
            R.id.menu_info_abon -> {

            }
            // Новая задача
            // ------------
            R.id.menu_new_task_list -> {
                if (progressBarLvTasks?.visibility == View.VISIBLE)
                    return true

                if (isNetworkAvailable(this)) {
                    val intent = Intent(this@ListTasksActivity, NewTaskActivity::class.java)
                    intent.putExtra("TASK_ID", ID_NEW_TASK)
                    intent.putExtra("ID_INSPECTOR", _idInspector)
                    startActivityForResult(intent, CREATE_NEW_TASK)
                    return true
                } else {
                    val builder = AlertDialog.Builder(this)
                    builder.setTitle("Внимание")
                        .setIcon(R.drawable.ic_warning_triangle)
                        .setMessage("Отсутствует подключение к интернету. Создать задание вручную?")
                        .setPositiveButton("Да") { _, _ ->
                            val intent = Intent(this@ListTasksActivity, TaskActivity::class.java)
                            intent.putExtra("TASK_ID", ID_NEW_TASK)
                            intent.putExtra("ID_INSPECTOR", _idInspector)
                            startActivityForResult(intent, ADD_NEW_TASK)
                        }
                        .setNegativeButton("Нет") { dialog, _ ->
                            dialog.cancel()
                        }
                    builder.show()
                    return true
                }
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // Смена даты (получить задачи на выбранную дату)
    // ----------------------------------------------
    private fun changeDateDialog() {
        val dpd = DatePickerDialog(
            this,
            { _, year, monthOfYear, dayOfMonth ->
                val selectedDate: LocalDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                //if (!selectedDate.isEqual(_calendarDate) || _taskList.size == 0) {
                _calendarDate = selectedDate
                refreshData()
                //} else {
                //refreshDataLocal()
                // }
            },
            _calendarDate.year,
            _calendarDate.monthValue - 1,
            _calendarDate.dayOfMonth
        )

        dpd.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (lvTasksList.adapter != null)
            (lvTasksList.adapter as AdapterListTasks).isTaskOpened = false

        when (resultCode) {
            // Если вернулись из TaskActivity, созданной вручную с адресом
            RESULT_NEW_ABON -> {
                when (requestCode) {
                    // После добавления задачи вручную обновляем листвью локально
                    // После редактирования задачи обновляем листвью локально
                    ADD_NEW_TASK, TASK_UPDATE -> {
                        refreshDataLocal()
                    }
                }
            }
            RESULT_OK -> {
                when (requestCode) {
                    // После добавления задачи вручную обновляем листвью локально
                    // После редактирования задачи обновляем листвью локально
                    ADD_NEW_TASK, TASK_UPDATE -> {
                        refreshDataLocal()
                    }
                    // После создания задачи на сервере
                    // через минуту обновляем листвью с сервера
                    CREATE_NEW_TASK -> {
                        openProgressBar()

                        Timer().schedule(15000) {
                            refreshDataServer(2)
                        }
                    }
                }
            }
            RESULT_ADD_TASK -> {
                when (requestCode) {
                    // Если вернулись из NewTaskActivity, чтобы создать вручную
                    CREATE_NEW_TASK -> {
                        val intent = Intent(this@ListTasksActivity, TaskActivity::class.java)
                        intent.putExtra("TASK_ID", ID_NEW_TASK)
                        intent.putExtra("ID_INSPECTOR", _idInspector)
                        startActivityForResult(intent, ADD_NEW_TASK)
                    }
                }
            }
        }
    }

    // При нажатии кнопки ничего не делаем
    // -----------------------------------
    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Внимание!")
            .setMessage("Вы хотите перейти в окно авторизации?")
            .setIcon(R.drawable.ic_question)
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton("Да") { _, _ ->
                setResult(RESULT_OK)
                this@ListTasksActivity.finish()
            }
        builder.show()
        return
    }

    override fun onResume() {
        if (cnt_updates == 0) {
            checkUpdates()
        }
        super.onResume()
    }

    private fun checkUpdates() {
        if (isNetworkAvailable(this@ListTasksActivity)) {
            uiScope.launch {
                val check = async(Dispatchers.IO) {
                    try {
                        cnt_updates = DBHandlerServerRead(this@ListTasksActivity).checkUpdatesAsync(
                            LocalDate.now(),
                            _idInspector
                        )
                        true
                    } catch (e: Exception) {
                        println("$TAG_ERR checkUpdatesAsync: ${e.message}")
                        false
                    }
                }

                if (check.await()) {
                    if (cnt_updates > 0) {
                        this@ListTasksActivity.invalidateOptionsMenu()
                    }
                }
            }
        }
    }
}


