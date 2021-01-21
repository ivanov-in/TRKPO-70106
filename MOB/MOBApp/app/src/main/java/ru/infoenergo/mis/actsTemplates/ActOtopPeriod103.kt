package ru.infoenergo.mis.actsTemplates

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_act_otop_period_103.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.infoenergo.mis.BuildConfig
import ru.infoenergo.mis.CreatePdf
import ru.infoenergo.mis.DlgConfirmationActSign
import ru.infoenergo.mis.R
import ru.infoenergo.mis.dbhandler.DbHandlerLocalRead
import ru.infoenergo.mis.dbhandler.DbHandlerLocalWrite
import ru.infoenergo.mis.helpers.*
import java.io.File
import java.io.FileFilter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.LinkedHashMap
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter

/** ***************************************************************** **/
/**      15 id Акт проверки готовности (по Приказу 103)  3 tip        **/
/** ***************************************************************** **/
class ActOtopPeriod103 : AppCompatActivity(), DialogInterface.OnCancelListener {
    private lateinit var dlgConfirmActSign: DlgConfirmationActSign

    private lateinit var _task: Task
    private lateinit var _act: ActInfo
    private lateinit var _actFields: ActFieldsInfo

    private var new: Boolean = false
    private var npp: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_act_otop_period_103)

        _act = intent.getSerializableExtra("ACT") as ActInfo
        _task = intent.getSerializableExtra("TASK") as Task
        _actFields = intent.getSerializableExtra("ACT_FIELDS") as ActFieldsInfo

        if (_act.id_act == 0 || _task.id_task == 0)
            finish()

        // Если пришли поля ACT_FIELDS, то это редактирование
        // Если не пришли, то это новый акт, получаем поля из act_fields_shablon
        if (_actFields.id_task == 0 && _actFields.id_act == 0 && _actFields.npp == 0) {
            val dbRead = DbHandlerLocalRead(this, null)
            new = true
            _actFields = dbRead.getActFieldsShablon(_task.id_task, _act.id_act)

            // Формирование npp
            // ----------------
            if (new) {
                npp = dbRead.getLastActNpp(_actFields.id_task, _actFields.id_act)
                npp = if (npp == 0) 1 else npp + 1
                _actFields.npp = npp
                _actFields.num_act += "\\${npp}"
            }
            dbRead.close()
        }
        npp = _actFields.npp

        if (!new && _task.kod_emp_podp != 0)
            btnSignActOtopPeriod103.isEnabled = true

        try {
            if (supportActionBar != null) {
                val actionBar = supportActionBar
                actionBar!!.title = _act.name
                actionBar.subtitle = _actFields.num_act
                actionBar.elevation = 4.0F
                actionBar.setDisplayHomeAsUpEnabled(true)
            }

            // Заполнение вьюх
            //--------------------------------
            actTip3NumAct.text = _actFields.num_act
            actTip3DatAct.setOnClickListener { changeDate() }
            actTip3DatAct.text =
                if (_actFields.dat_act.isNotEmpty()) _actFields.dat_act else LocalDate.now().toString()
            actTip3OtopPeriod.setText(_actFields.otop_period)
            actTip3City.setText(_actFields.city)

            actTip3PayerName.setText(_actFields.payer_name)
            actTip3ListObj.setText(_actFields.list_obj)

            actTip3DopInfo.setText(_actFields.dop_info)
            actTip3ComissPostGotov.setText(_actFields.comiss_post_gotov)

            actTip3PredComiss.setText(_actFields.pred_comiss)
            actTip3ZamPredGkh.setText(_actFields.zam_pred_gkh)
            actTip3FilialEso.text = _actFields.filial_eso
            actTip3DirectorTatenergo.setText(_actFields.director_tatenergo)
            actTip3Podpisi.setText(_actFields.podpisi)
            actTip3FioPodp.text = _task.fio_podp
            actTip3RemarkDog.setText(_actFields.remark_dog)

            // слушатели на кнопки
            // --------------------------
            btnCreateActOtopPeriod103.setOnClickListener {
                createAct()
            }

            btnSignActOtopPeriod103.setOnClickListener {
                try {
                    dlgConfirmActSign = DlgConfirmationActSign(
                        _task.tel_podp,
                        _task.email_podp,
                        _actFields.id_file
                    )
                    dlgConfirmActSign.show(supportFragmentManager, "SIGN_ACT")
                } catch (e: Exception) {
                    println("$TAG_ERR ActProverkiGotovnosti: ${e.message}")
                }
            }

            // Спиннер с фразами
            // ----------------------
            actTip3SpCopyText.apply {
                adapter = ArrayAdapter(context!!, R.layout.spitem_purpose, PhrasesCopied)
                setSelection(0)
            }

            actTip3SpCopyText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val clipboard: ClipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("CopiedText", actTip3SpCopyText.selectedItem.toString())
                    clipboard.setPrimaryClip(clip)
                }
            }
        } catch (e: Exception) {
            println("$TAG_ERR ActProverkiGotovnosti: ${e.message}")
        }
    }

    // На кнопку Создать акт
    // ----------------------
    private fun createAct(signed: Boolean = false): Boolean {
        val pdfPath: String
        val htmlPath: String
        val pdfsFolder: File?

        try {
            val external = this.baseContext.getExternalFilesDir(null)
            val template = File("$external/templates")
            if (!template.exists()) template.mkdirs()

            pdfsFolder = File("$external/tmpFiles/${_task.id_task}")
            if (!pdfsFolder.exists()) pdfsFolder.mkdirs()

            // Формирование путей html и pdf
            // ----------------------------------------------------------------------------------------
            htmlPath = "$template/${_actFields.shablon.replace(".pdf", ".html")}"
            pdfPath =
                "${pdfsFolder.path}/${_actFields.num_act.replace("\\", "-")}_${_actFields.shablon.replace(" ", "_")}"

            if (pdfPath.isEmpty() || htmlPath.isEmpty()) {
                Toast.makeText(
                    this@ActOtopPeriod103,
                    "Не удалось получить путь к шаблону акта ${_actFields.shablon}.",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActOtopPeriod103,
                "Произошла ошибка при создании папки: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            return false
        }

        try {
            // Удаляем акт(ы) на устройстве, которому создаём взамен новый
            // -----------------------------------------------------------
            if (pdfsFolder.exists()) {
                val oldActs = pdfsFolder.listFiles(FileFilter {
                    it.path.contains(pdfPath.substringAfterLast("/"))
                })
                for (act in oldActs!!) {
                    if (!act.delete())
                        act.deleteRecursively()
                }
            }

            // Если вызов метода был не после подписания акта,
            // то insert полей act fields в локальную базу
            // -------------------------------------------
            if (!signed) {
                if (saveDataToLocalDb()) {
                    Toast.makeText(
                        this,
                        "Данные акта ${_actFields.num_act} сохранены в базу данных.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this, "Не удалось сохранить данные акта ${_actFields.num_act} в базу данных.", Toast.LENGTH_LONG
                    ).show()
                }
            }
            // Создание нового файла
            // -------------------------------------------------------
            val vals: LinkedHashMap<String, String> = getHashMapVals(signed)
            if (CreatePdf.Create(htmlPath, pdfPath, vals, this)) {
                val dbWrite = DbHandlerLocalWrite(this, null)
                // Сохранение blob в локальной базе
                // --------------------------------
                val filedata = FileProvider.getUriForFile(
                    Objects.requireNonNull(applicationContext),
                    BuildConfig.APPLICATION_ID + ".provider", File(pdfPath)
                )
                val file = FileInfo(
                    id_task = _actFields.id_task,
                    filename = pdfPath.substringAfterLast("/"),
                    filedata = blobFromUri(this, filedata),
                    uri = Uri.parse(pdfPath),
                    id_file = _actFields.id_file,
                    is_signed = _actFields.is_signed,
                    paper = 0,
                    is_send = 0,
                    date_send_to_client = if (signed) LocalDateTime.now() else null,
                    email_client = _task.email_podp,
                    npp = _actFields.npp,
                    id_act = _actFields.id_act
                )

                // Если после подписания, то просто update blob и даты отправки клиенту
                // ---------------------------------------------------------------------
                if (signed) {
                    dbWrite.updateFile(file, file.filedata!!)
                }
                // Если новый акт - inset blob
                // ---------------------------
                else {
                    val idFile = dbWrite.insertFileWithBlob(file)
                    _actFields.id_file = idFile
                    dbWrite.updateActFieldsIdFile(
                        _actFields.id_task, _actFields.id_act, _actFields.npp, _actFields.id_file
                    )
                }

                dbWrite.close()
                setEditableFalse()

                if (_task.kod_emp_podp != 0 && !signed)
                    btnSignActOtopPeriod103.isEnabled = true

                // Открытие pdf
                // ---------------------------------------
                if (!signed) showPdf(this@ActOtopPeriod103, pdfPath)
            } else {
                Toast.makeText(
                    this@ActOtopPeriod103,
                    "Произошла ошибка при создании акта ${_actFields.num_act}.",
                    Toast.LENGTH_LONG
                ).show()
            }
            return true
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActOtopPeriod103,
                "Произошла ошибка при создании акта ${_actFields.num_act}.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
    }

    // Установка недоступности редактирования, если акт подписан
    // ---------------------------------------------------------
    private fun setEditableFalse() {
        actTip3City.isEnabled = false
        actTip3DatAct.isClickable = false
        actTip3OtopPeriod.isEnabled = false
        actTip3NumAct.isEnabled = false
        actTip3PayerName.isEnabled = false
        actTip3ListObj.isEnabled = false

        actTip3DopInfo.isEnabled = false
        actTip3ComissPostGotov.isEnabled = false
        actTip3PredComiss.isEnabled = false
        actTip3ZamPredGkh.isEnabled = false
        actTip3DirectorTatenergo.isEnabled = false

        actTip3Podpisi.isEnabled = false
        actTip3FioPodp.isEnabled = false
        actTip3RemarkDog.isEnabled = false

        btnCreateActOtopPeriod103.isEnabled = false
        btnSignActOtopPeriod103.isEnabled = false
        actTip3SpCopyText.isEnabled = false
    }

    // Смена даты
    // -----------------------------------------------
    private fun changeDate() {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy", Locale("ru"))
            val datString = _actFields.dat_act.replace(" г.", "").replace("г.", "").replace(" ", "-")
            var datLocalDate = LocalDate.parse(datString, formatter)
            val dpd = DatePickerDialog(
                this,
                { _, year, monthOfYear, dayOfMonth ->
                    val selectedDate: LocalDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                    if (!selectedDate.isEqual(datLocalDate)) {
                        datLocalDate = selectedDate
                        _actFields.dat_act = datLocalDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy г."))
                        actTip3DatAct.text = _actFields.dat_act
                    }
                },
                datLocalDate.year,
                datLocalDate.monthValue - 1,
                datLocalDate.dayOfMonth
            )

            dpd.show()
        } catch (e: Exception) {
            println("$TAG_ERR: ${e.message}")
        }
    }

    // Сохраняем поля акта в локальную БД
    // ----------------------------------
    private fun saveDataToLocalDb(): Boolean {
        _actFields.apply {
            city = actTip3City.text.toString()
            id_act = _act.id_act
            id_task = _task.id_task
            num_act = actTip3NumAct.text.toString()
            dat_act = actTip3DatAct.text.toString()
            otop_period = actTip3OtopPeriod.text.toString()
            payer_name = actTip3PayerName.text.toString()
            list_obj = actTip3ListObj.text.toString()

            dop_info = actTip3DopInfo.text.toString()
            comiss_post_gotov = actTip3ComissPostGotov.text.toString()
            pred_comiss = actTip3PredComiss.text.toString()
            zam_pred_gkh = actTip3ZamPredGkh.text.toString()
            director_tatenergo = actTip3DirectorTatenergo.text.toString()
            remark_dog = actTip3RemarkDog.text.toString()

            fio_contact = actTip3FioPodp.text.toString()
            podpisi = actTip3Podpisi.text.toString()
        }

        val dbWrite = DbHandlerLocalWrite(this, null)
        return if (new) {
            val res = dbWrite.insertActFields(_actFields)
            dbWrite.close()
            res
        } else {
            val res = dbWrite.updateActFields(_actFields, fromServer = false)
            dbWrite.close()
            res
        }
    }

    // Создаём LinkedHashMap из введённых данных
    // --------------------------------------------------------------------
    private fun getHashMapVals(signed: Boolean): java.util.LinkedHashMap<String, String> {
        val vals: LinkedHashMap<String, String> = LinkedHashMap(13)
        vals["%NUM_ACT"] = _actFields.num_act
        vals["%DAT_ACT"] = _actFields.dat_act
        vals["%CITY"] = _actFields.city
        vals["%OTOP_PERIOD"] = _actFields.otop_period
        vals["%PAYER_NAME"] = _actFields.payer_name
        vals["%LIST_OBJ"] = _actFields.list_obj

        vals["%DOP_INFO"] = _actFields.dop_info
        vals["%COMISS_POST_GOTOV"] = _actFields.comiss_post_gotov
        vals["%PRED_COMISS"] = _actFields.pred_comiss
        vals["%ZAM_PRED_GKH"] = _actFields.zam_pred_gkh
        vals["%DIRECTOR_TATENERGO"] = _actFields.director_tatenergo
        vals["%FILIAL_ESO"] = _actFields.filial_eso
        vals["%PODPISI"] = _actFields.podpisi

        vals["%FIO_PODP"] = _task.fio_podp
        vals["%REMARK_DOG"] =
            if (_actFields.remark_dog.isNotEmpty()) "Замечания абонента: " + _actFields.remark_dog else ""

        if (signed) {
            vals["%ACT_SIGNED"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        }
        return vals
    }

    // ---------------------
    // Кнопка "назад" в меню
    // ---------------------
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    // При нажатии кнопки назад спрашиваем подтверждение
    // -------------------------------------------------
    override fun onBackPressed() {
        if (!btnCreateActOtopPeriod103.isEnabled) {
            setResult(RESULT_OK)
            this@ActOtopPeriod103.finish()
        } else {
            val msg = "Покинуть создание '${_act.name}' (все введённые данные будут утеряны)?"
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Внимание!")
                .setMessage(msg)
                .setIcon(R.drawable.ic_question)
                .setNegativeButton("Нет") { dialog, _ ->
                    dialog.cancel()
                }
                .setPositiveButton("Да") { _, _ ->
                    setResult(RESULT_CANCELED)
                    this@ActOtopPeriod103.finish()
                }
            builder.show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCancel(dialog: DialogInterface?) {
        val dbWrite = DbHandlerLocalWrite(this, null)
        if (dbWrite.updateActSigned(
                _actFields.id_task, _actFields.id_act, _actFields.npp, _actFields.id_file, _task.email_podp
            )
        ) {
            if (createAct(signed = true)) {
                val viewModelJob = SupervisorJob()
                val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
                uiScope.launch {
                    dlgConfirmActSign.sendSignedAct()
                }
            }
            Toast.makeText(this, "Акт ${_actFields.num_act} успешно подписан.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Произошла непредвиденная ошибка :(", Toast.LENGTH_LONG).show()
        }

        dbWrite.close()
        setEditableFalse()
    }
}