package ru.infoenergo.mis.actsTemplates

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import kotlinx.android.synthetic.main.activity_act_otkaz_dost_teplo.*
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

/** ******************************************************************** **
 **        20 id Акт об отказе в доступе к теплоустановкам tip 8         **
 ** ******************************************************************** **/
class ActOtkazDostTeplo : AppCompatActivity(), DialogInterface.OnCancelListener {
    private var new: Boolean = false
    private var npp: Int = 0

    private lateinit var _actFields: ActFieldsInfo
    private lateinit var _task: Task
    private lateinit var _act: ActInfo
    private lateinit var dlgConfirmActSign: DlgConfirmationActSign


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_act_otkaz_dost_teplo)

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
            btnSignActOtkazDostTeplo.isEnabled = true

        try {
            if (supportActionBar != null) {
                val actionBar = supportActionBar
                actionBar!!.title = _act.name
                actionBar.subtitle = _actFields.num_act
                actionBar.elevation = 4.0F
                actionBar.setDisplayHomeAsUpEnabled(true)
            }

            actTip8FilialEso.setText(_actFields.filial_eso)

            actTip8DatAct.text = _actFields.dat_act
            actTip8DatAct.setOnClickListener { changeDate() }
            actTip8DatAct.text = if (_actFields.dat_act.isNotEmpty()) _actFields.dat_act
            else LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy г."))

            actTip8NumAct.text = _actFields.num_act

            actTip8PayerName.setText(_actFields.payer_name)
            actTip8AdrObj.setText(_actFields.adr_obj)
            actTip8NameObj.setText(_actFields.name_obj)
            actTip8NDog.text = _actFields.ndog
            actTip8DatDog.text = _actFields.dat_dog
            actTip8UvedomlOtklNum.setText(_actFields.uvedoml_otkl_num)
            actTip8UvedomlOtklDate.setText(_actFields.uvedoml_otkl_date)
            actTip8FioEso.setText(_actFields.fio_eso)
            actTip8NameDolzhnEso.setText(_actFields.name_dolzhn_eso)
            actTip8TelEso.setText(_actFields.tel_eso)
            actTip8FioPodp.text = _task.fio_podp
            actTip8NameDolzhnPodp.text = _task.name_dolzhn_podp
            actTip8TelPodp.text = _task.tel_podp
            actTip8Eso1.text = "${_actFields.fio_eso}, ${_actFields.name_dolzhn_eso} (${_actFields.tel_eso})"
            actTip8Contact1.text = "${_task.fio_podp}, ${_task.name_dolzhn_podp} (${_task.tel_podp})"
            actTip8Eso2.text = "${_actFields.fio_eso}, ${_actFields.name_dolzhn_eso} (${_actFields.tel_eso})"
            actTip8Contact2.text = "${_task.fio_podp}, ${_task.name_dolzhn_podp} (${_task.tel_podp})"
            actTip8ActPoluchil.setText(_actFields.act_poluchil)

            actTip8FioEso.doOnTextChanged { text, _, _, _ ->
                actTip8Eso1.text = "$text, ${actTip8NameDolzhnEso.text} (${actTip8TelEso.text})"
                actTip8Eso2.text = "$text, ${actTip8NameDolzhnEso.text} (${actTip8TelEso.text})"
            }
            actTip8NameDolzhnEso.doOnTextChanged { text, _, _, _ ->
                actTip8Eso1.text = "${actTip8FioEso.text}, $text (${actTip8TelEso.text})"
                actTip8Eso2.text = "${actTip8FioEso.text}, $text (${actTip8TelEso.text})"
            }
            actTip8TelEso.doOnTextChanged { text, _, _, _ ->
                actTip8Eso1.text = "${actTip8FioEso.text}, ${actTip8NameDolzhnEso.text} ($text)"
                actTip8Eso2.text = "${actTip8FioEso.text}, ${actTip8NameDolzhnEso.text} ($text)"
            }

            actTip8PrichinaOtkaza.setText(_actFields.prichina_otkaza)
            actTip8OtkazSvidet1.setText(_actFields.otkaz_svidet_1)
            actTip8OtkazSvidet2.setText(_actFields.otkaz_svidet_2)
            actTip8RemarkDog.setText(_actFields.remark_dog)

        } catch (e: Exception) {
            println("$TAG_ERR ActOtkazDostTeplo: ${e.message}")
        }

        try {
            // слушатель на кнопку создания акта
            // ---------------------------------------
            btnCreateActOtkazDostTeplo.setOnClickListener {
                createAct()
            }

            // слушатель на кнопку подписания акта
            // -------------------------------------
            btnSignActOtkazDostTeplo.setOnClickListener {
                dlgConfirmActSign = DlgConfirmationActSign(
                    _task.tel_podp,
                    _task.email_podp,
                    _actFields.id_file
                )
                dlgConfirmActSign.show(supportFragmentManager, "SIGN_ACT")
            }

            // Спиннер с фразами
            // ----------------------
            actTip8SpCopyText.apply {
                adapter = ArrayAdapter(context!!, R.layout.spitem_purpose, PhrasesCopied)
                setSelection(0)
            }

            actTip8SpCopyText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val clipboard: ClipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("CopiedText", actTip8SpCopyText.selectedItem.toString())
                    clipboard.setPrimaryClip(clip)
                }
            }
        } catch (e: Exception) {
            println("$TAG_ERR ActOtkazDostTeplo: ${e.message}")
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
                    this@ActOtkazDostTeplo,
                    "Не удалось получить путь к шаблонам.",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActOtkazDostTeplo,
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
                    btnSignActOtkazDostTeplo.isEnabled = true

                // Открытие pdf
                // ---------------------------------------
                if (!signed) showPdf(this@ActOtkazDostTeplo, pdfPath)
            } else {
                Toast.makeText(
                    this@ActOtkazDostTeplo,
                    "Произошла ошибка при создании акта ${_actFields.num_act}.",
                    Toast.LENGTH_LONG
                ).show()
            }
            return true
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActOtkazDostTeplo,
                "Произошла ошибка при создании акта ${_actFields.num_act}.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
    }

    // Установка недоступности редактирования, если акт подписан
    // ---------------------------------------------------------
    private fun setEditableFalse() {
        actTip8FilialEso.isEnabled = false
        actTip8NumAct.isEnabled = false
        actTip8DatAct.isEnabled = false
        actTip8PayerName.isEnabled = false
        actTip8NameObj.isEnabled = false
        actTip8AdrObj.isEnabled = false
        actTip8NDog.isEnabled = false
        actTip8DatDog.isEnabled = false
        actTip8UvedomlOtklNum.isEnabled = false
        actTip8UvedomlOtklDate.isEnabled = false
        actTip8FioEso.isEnabled = false
        actTip8NameDolzhnEso.isEnabled = false
        actTip8TelEso.isEnabled = false
        actTip8FioPodp.isEnabled = false
        actTip8NameDolzhnPodp.isEnabled = false
        actTip8TelPodp.isEnabled = false
        actTip8PrichinaOtkaza.isEnabled = false
        actTip8Eso1.isEnabled = false
        actTip8Contact1.isEnabled = false
        actTip8OtkazSvidet1.isEnabled = false
        actTip8OtkazSvidet2.isEnabled = false
        actTip8Eso2.isEnabled = false
        actTip8Contact2.isEnabled = false
        actTip8ActPoluchil.isEnabled = false
        actTip8RemarkDog.isEnabled = false

        btnCreateActOtkazDostTeplo.isEnabled = false
        btnSignActOtkazDostTeplo.isEnabled = false
        actTip8SpCopyText.isEnabled = false
    }

    // Смена даты
    // -----------------------------------------------
    private fun changeDate() {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd-MMMM-yyyy", Locale("ru"))
            val datString = _actFields.dat_act.replace(" г.", "").replace("г.", "").replace(" ", "-")
            var dat = LocalDate.parse(datString, formatter)
            val dpd = DatePickerDialog(
                this,
                { _, year, monthOfYear, dayOfMonth ->
                    val selectedDate: LocalDate = LocalDate.of(year, monthOfYear + 1, dayOfMonth)
                    if (!selectedDate.isEqual(dat)) {
                        dat = selectedDate
                        _actFields.dat_act = dat.format(DateTimeFormatter.ofPattern("dd MMMM yyyy г."))
                        actTip8DatAct.text = _actFields.dat_act
                    }
                },
                dat.year,
                dat.monthValue - 1,
                dat.dayOfMonth
            )

            dpd.show()
        } catch (e: Exception) {
            println("LOG ERR: ${e.message}")
        }
    }

    // Сохраняем поля акта в локальную БД
    // ----------------------------------
    private fun saveDataToLocalDb(): Boolean {
        _actFields.apply {
            id_act = _act.id_act
            id_task = _task.id_task
            filial_eso = actTip8FilialEso.text.toString()
            dat_act = actTip8DatAct.text.toString()
            num_act = actTip8NumAct.text.toString()
            payer_name = actTip8PayerName.text.toString()
            name_obj = actTip8NameObj.text.toString()
            adr_obj = actTip8AdrObj.text.toString()
            ndog = actTip8NDog.text.toString()
            dat_dog = actTip8DatDog.text.toString()
            uvedoml_otkl_num = actTip8UvedomlOtklNum.text.toString()
            uvedoml_otkl_date = actTip8UvedomlOtklDate.text.toString()
            fio_eso = actTip8FioEso.text.toString()
            name_dolzhn_eso = actTip8NameDolzhnEso.text.toString()
            tel_eso = actTip8TelEso.text.toString()
            fio_contact = actTip8FioPodp.text.toString()
            name_dolzhn_contact = actTip8NameDolzhnPodp.text.toString()
            tel_contact = actTip8TelPodp.text.toString()
            prichina_otkaza = actTip8PrichinaOtkaza.text.toString()
            otkaz_svidet_1 = actTip8OtkazSvidet1.text.toString()
            otkaz_svidet_2 = actTip8OtkazSvidet2.text.toString()
            act_poluchil = actTip8ActPoluchil.text.toString()
            remark_dog = actTip8RemarkDog.text.toString()
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
        val vals: LinkedHashMap<String, String> = LinkedHashMap(20)
        vals["%FILIAL_ESO"] = _actFields.filial_eso
        vals["%NUM_ACT"] = _actFields.num_act
        vals["%DAT_ACT"] = _actFields.dat_act
        vals["%PAYER_NAME"] = _actFields.payer_name
        vals["%NAME_OBJ"] = _actFields.name_obj
        vals["%ADR_OBJ"] = _actFields.adr_obj
        vals["%NDOG"] = _actFields.ndog
        vals["%DAT_DOG"] = _actFields.dat_dog
        vals["%UVEDOML_OTKL_NUM"] = _actFields.uvedoml_otkl_num
        vals["%UVEDOML_OTKL_DATE"] = _actFields.uvedoml_otkl_date
        vals["%FIO_ESO"] = _actFields.fio_eso
        vals["%NAME_DOLZHN_ESO"] = _actFields.name_dolzhn_eso
        vals["%TEL_ESO"] = _actFields.tel_eso
        vals["%FIO_PODP"] = _task.fio_podp
        vals["%NAME_DOLZHN_PODP"] = _task.name_dolzhn_podp
        vals["%TEL_PODP"] = _task.tel_podp
        vals["%PRICHINA_OTKAZA"] = _actFields.prichina_otkaza
        vals["%OTKAZ_SVIDET_1"] = _actFields.otkaz_svidet_1
        vals["%OTKAZ_SVIDET_2"] = _actFields.otkaz_svidet_2
        vals["%ACT_POLUCHIL"] = _actFields.act_poluchil

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
        if (!btnCreateActOtkazDostTeplo.isEnabled) {
            setResult(RESULT_OK)
            this@ActOtkazDostTeplo.finish()
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
                    this@ActOtkazDostTeplo.finish()
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
            } else {
                Toast.makeText(this, "Произошла непредвиденная ошибка :(", Toast.LENGTH_LONG).show()
            }

            dbWrite.close()
            setEditableFalse()
        }
    }
}