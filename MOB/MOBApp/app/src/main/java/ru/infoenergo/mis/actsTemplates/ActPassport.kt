package ru.infoenergo.mis.actsTemplates

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import kotlinx.android.synthetic.main.activity_act_passport.*
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

/** ******************************************************************** **
 **           16 id  Паспорт_готовности_(по_Приказу_103).xml    4 tip    **
 ** ******************************************************************* **/
class ActPassport : AppCompatActivity(), DialogInterface.OnCancelListener {

    private lateinit var dlgConfirmActSign: DlgConfirmationActSign
    private lateinit var _actFields: ActFieldsInfo
    private lateinit var _task: Task
    private lateinit var _act: ActInfo

    private var new: Boolean = false
    private var npp: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_act_passport)

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
            btnSignPassportAct.isEnabled = true

        try {
            if (supportActionBar != null) {
                val actionBar = supportActionBar
                actionBar!!.title = _act.name
                actionBar.subtitle = _actFields.num_act
                actionBar.elevation = 4.0F
                actionBar.setDisplayHomeAsUpEnabled(true)
            }

            // Заполнение вьюх
            //------------------------
            actTip4DatAct.setOnClickListener { changeDate(_actFields) }
            actTip4DatAct.text = if (_actFields.dat_act.isNotEmpty()) _actFields.dat_act else LocalDate.now().toString()

            actTip4OtopPeriod.text = _actFields.otop_period
            actTip4NumAct.text = _actFields.num_act
            actTip4PayerName.setText(_actFields.payer_name)
            actTip4ListObj.setText(_actFields.list_obj)
            actTip4Osnov.setText(_actFields.osnov)
            actTip4FioEso.setText(_actFields.fio_eso)
            actTip4RemarkDog.setText(_actFields.remark_dog)

            // слушатели на кнопки
            // --------------------------
            btnCreatePassportAct.setOnClickListener {
                createAct()
            }

            btnSignPassportAct.setOnClickListener {
                try {
                    dlgConfirmActSign = DlgConfirmationActSign(
                        _actFields.tel_contact,
                        _task.email_contact,
                        _actFields.id_file
                    )
                    dlgConfirmActSign.show(supportFragmentManager, "SIGN_ACT")
                } catch (e: Exception) {
                    println("$TAG_ERR ActPassport: ${e.message}")
                }
            }

            // Спиннер с фразами
            // ----------------------
            actTip4SpCopyText.apply {
                adapter = ArrayAdapter(context!!, R.layout.spitem_purpose, PhrasesCopied)
                setSelection(0)
            }

            actTip4SpCopyText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val clipboard: ClipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("CopiedText", actTip4SpCopyText.selectedItem.toString())
                    clipboard.setPrimaryClip(clip)
                }
            }
        } catch (e: Exception) {
            println("$TAG_ERR ActPassport: ${e.message}")
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
                    this@ActPassport,
                    "Не удалось получить путь к шаблону акта ${_actFields.shablon}.",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActPassport,
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
                    btnSignPassportAct.isEnabled = true

                // Открытие pdf
                // ---------------------------------------
                if (!signed) showPdf(this@ActPassport, pdfPath)
            } else {
                Toast.makeText(
                    this@ActPassport,
                    "Произошла ошибка при создании акта ${_actFields.num_act}.",
                    Toast.LENGTH_LONG
                ).show()
            }
            return true
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActPassport,
                "Произошла ошибка при создании акта ${_actFields.num_act}.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
    }

    // Установка недоступности редактирования, если акт подписан
    // ---------------------------------------------------------
    private fun setEditableFalse() {
        actTip4DatAct.isClickable = false
        actTip4OtopPeriod.isEnabled = false
        actTip4NumAct.isEnabled = false
        actTip4PayerName.isEnabled = false
        actTip4ListObj.isEnabled = false
        actTip4Osnov.isEnabled = false
        actTip4FioEso.isEnabled = false
        actTip4RemarkDog.isEnabled = false

        btnCreatePassportAct.isEnabled = false
        btnSignPassportAct.isEnabled = false
        actTip4SpCopyText.isEnabled = false
    }

    // Смена даты
    // -----------------------------------------------
    private fun changeDate(actFields: ActFieldsInfo) {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy", Locale("ru"))
            val datString = actFields.dat_act.replace(" г.", "").replace("г.", "").replace(" ", "-")
            var dat = LocalDate.parse(datString, formatter)
            val dpd = DatePickerDialog(
                this,
                { _, year, monthOfYear, dayOfMonth ->
                    val selectedDate: LocalDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                    if (!selectedDate.isEqual(dat)) {
                        dat = selectedDate
                        actFields.dat_act = dat.format(DateTimeFormatter.ofPattern("dd MMMM yyyy г."))
                        actTip4DatAct.text = actFields.dat_act
                    }
                },
                dat.year,
                dat.monthValue - 1,
                dat.dayOfMonth
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
            id_act = _act.id_act
            id_task = _task.id_task
            num_act = actTip4NumAct.text.toString()
            dat_act = actTip4DatAct.text.toString()
            otop_period = actTip4OtopPeriod.text.toString()
            payer_name = actTip4PayerName.text.toString()
            fio_eso = actTip4FioEso.text.toString()
            list_obj = actTip4ListObj.text.toString()
            osnov = actTip4Osnov.text.toString()
            remark_dog = actTip4RemarkDog.text.toString()
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
        val vals: LinkedHashMap<String, String> = LinkedHashMap(7)
        vals["%NUM_ACT"] = _actFields.num_act
        vals["%DAT_ACT"] = _actFields.dat_act
        vals["%PAYER_NAME"] = _actFields.payer_name
        vals["%OTOP_PERIOD"] = _actFields.otop_period
        vals["%LIST_OBJ"] = _actFields.list_obj
        vals["%FIO_ESO"] = _actFields.fio_eso
        vals["%OSNOV"] = _actFields.osnov
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
        if (!btnCreatePassportAct.isEnabled) {
            setResult(RESULT_OK)
            this@ActPassport.finish()
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
                    this@ActPassport.finish()
                }
            builder.show()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onCancel(dialog: DialogInterface?) {
        if (dlgConfirmActSign.confirm) {
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
            } else
                Toast.makeText(this, "Произошла непредвиденная ошибка :(", Toast.LENGTH_LONG).show()

            dbWrite.close()
            setEditableFalse()
        }
    }
}