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
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import kotlinx.android.synthetic.main.activity_act_ogr_otkl.*
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

/** ***************************************************************** **
 **           19 id     Акт после огр, отключения.pdf       7 tip     **
 ** ***************************************************************** **/
class ActOgrOtkl : AppCompatActivity(), DialogInterface.OnCancelListener {
    private var new: Boolean = false
    private var npp: Int = 0

    private lateinit var _actFields: ActFieldsInfo
    private lateinit var _task: Task
    private lateinit var _act: ActInfo
    private lateinit var dlgConfirmActSign: DlgConfirmationActSign

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_act_ogr_otkl)

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
            btnSignActOgrOtkl.isEnabled = true

        try {
            if (supportActionBar != null) {
                val actionBar = supportActionBar
                actionBar!!.title = _act.name
                actionBar.subtitle = _actFields.num_act
                actionBar.elevation = 4.0F
                actionBar.setDisplayHomeAsUpEnabled(true)
            }

            // Заполнение вьюх
            //----------------------------------------
            actTip7FilialEso.setText(_actFields.filial_eso)

            actTip7DatAct.text = _actFields.dat_act
            actTip7DatAct.setOnClickListener { changeDate() }
            actTip7DatAct.text = if (_actFields.dat_act.isNotEmpty()) _actFields.dat_act
            else LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy г."))

            actTip7NumAct.text = _actFields.num_act
            actTip7PayerName.setText(_actFields.payer_name)
            actTip7NameObj.setText(_actFields.name_obj)
            actTip7NDog.text = _actFields.ndog
            actTip7DatDog.text = _actFields.dat_dog
            actTip7DatAct1.text = _actFields.dat_act
            actTip7SumDolg.setText(_actFields.sum_dolg)
            actTip7PredPogashDolgNum.setText(_actFields.pred_pogash_dolg_num)
            actTip7PredPogashDolgDate.setText(_actFields.pred_pogash_dolg_date)
            actTip7UvedomlOtklNum.setText(_actFields.uvedoml_otkl_num)
            actTip7UvedomlOtklDate.setText(_actFields.uvedoml_otkl_date)
            actTip7FioEso.setText(_actFields.fio_eso)
            actTip7NameDolzhnEso.setText(_actFields.name_dolzhn_eso)
            actTip7TelEso.setText(_actFields.tel_eso)
            actTip7FioPodp.text = _task.fio_podp
            actTip7NameDolzhnPodp.text = _task.name_dolzhn_podp
            actTip7TelPodp.text = _task.tel_podp
            actTip7OtklProizv.setText(_actFields.otkl_proizv)
            actTip7OtklPuPokazDo.setText(_actFields.otkl_pu_pokaz_do)
            actTip7OtklPuPokazPosle.setText(_actFields.otkl_pu_pokaz_posle)
            actTip7DopInfo.setText(_actFields.dop_info)
            actTip7FilialEso1.text = _actFields.filial_eso
            actTip7FilialAddress.setText(_actFields.filial_address)
            actTip7FilialTel.setText(_actFields.filial_tel)
            actTip7Eso.text = "${_actFields.fio_eso}, ${_actFields.name_dolzhn_eso} (${_actFields.tel_eso})"
            actTip7Contact.text = "${_task.fio_podp}, ${_task.name_dolzhn_podp} (${_task.tel_podp})"
            actTip7ActPoluchil.setText(_actFields.act_poluchil)

            actTip7DatAct.doAfterTextChanged { actTip7DatAct1.text = actTip7DatAct.text }
            actTip7FioEso.doOnTextChanged { text, _, _, _ ->
                actTip7Eso.text = "$text, ${actTip7NameDolzhnEso.text} (${actTip7TelEso.text})"
            }
            actTip7NameDolzhnEso.doOnTextChanged { text, _, _, _ ->
                actTip7Eso.text = "${actTip7FioEso.text}, $text (${actTip7TelEso.text})"
            }
            actTip7TelEso.doOnTextChanged { text, _, _, _ ->
                actTip7Eso.text = "${actTip7FioEso.text}, ${actTip7NameDolzhnEso.text} ($text)"
            }
            actTip7RemarkDog.setText(_actFields.remark_dog)


        } catch (e: Exception) {
            println("$TAG_ERR ActOgrOtkl: ${e.message}")
        }
        try {
            // слушатель на кнопку создания акта
            // ---------------------------------------
            btnCreateActOrgOtkl.setOnClickListener {
                createAct()
            }

            // слушатель на кнопку подписания акта
            // -------------------------------------
            btnSignActOgrOtkl.setOnClickListener {
                dlgConfirmActSign = DlgConfirmationActSign(
                    _task.tel_podp,
                    _task.email_podp,
                    _actFields.id_file
                )
                dlgConfirmActSign.show(supportFragmentManager, "SIGN_ACT")
            }

            // Спиннер с фразами
            // ----------------------
            actTip7SpCopyText.apply {
                adapter = ArrayAdapter(context!!, R.layout.spitem_purpose, PhrasesCopied)
                setSelection(0)
            }

            actTip7SpCopyText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val clipboard: ClipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("CopiedText", actTip7SpCopyText.selectedItem.toString())
                    clipboard.setPrimaryClip(clip)
                }
            }
        } catch (e: Exception) {
            println("$TAG_ERR ActOgrOtkl: ${e.message}")
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
                    this@ActOgrOtkl,
                    "Не удалось получить путь к шаблонам.",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActOgrOtkl,
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
                    btnSignActOgrOtkl.isEnabled = true

                // Открытие pdf
                // ---------------------------------------
                if (!signed) showPdf(this@ActOgrOtkl, pdfPath)
            } else {
                Toast.makeText(
                    this@ActOgrOtkl,
                    "Произошла ошибка при создании акта ${_actFields.num_act}.",
                    Toast.LENGTH_LONG
                ).show()
            }
            return true
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActOgrOtkl,
                "Произошла ошибка при создании акта ${_actFields.num_act}.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
    }

    // Установка недоступности редактирования, если акт подписан
    // ---------------------------------------------------------
    private fun setEditableFalse() {

        actTip7FilialEso.isEnabled = false
        actTip7DatAct.isEnabled = false
        actTip7PayerName.isEnabled = false
        actTip7NameObj.isEnabled = false
        actTip7DatDog.isEnabled = false
        actTip7DatAct1.isEnabled = false
        actTip7SumDolg.isEnabled = false
        actTip7PredPogashDolgNum.isEnabled = false
        actTip7PredPogashDolgDate.isEnabled = false
        actTip7UvedomlOtklNum.isEnabled = false
        actTip7UvedomlOtklDate.isEnabled = false
        actTip7FioEso.isEnabled = false
        actTip7NameDolzhnEso.isEnabled = false
        actTip7TelEso.isEnabled = false
        actTip7FioPodp.isEnabled = false
        actTip7NameDolzhnPodp.isEnabled = false
        actTip7TelPodp.isEnabled = false
        actTip7OtklProizv.isEnabled = false
        actTip7OtklPuPokazDo.isEnabled = false
        actTip7OtklPuPokazPosle.isEnabled = false
        actTip7DopInfo.isEnabled = false
        actTip7FilialEso1.isEnabled = false
        actTip7FilialAddress.isEnabled = false
        actTip7FilialTel.isEnabled = false
        actTip7ActPoluchil.isEnabled = false
        actTip7RemarkDog.isEnabled = false

        btnCreateActOrgOtkl.isEnabled = false
        btnSignActOgrOtkl.isEnabled = false
        actTip7SpCopyText.isEnabled = false
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
                        actTip7DatAct.text = _actFields.dat_act
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
            filial_eso = actTip7FilialEso.text.toString()
            dat_act = actTip7DatAct.text.toString()
            num_act = actTip7NumAct.text.toString()
            payer_name = actTip7PayerName.text.toString()
            name_obj = actTip7NameObj.text.toString()
            ndog = actTip7NDog.text.toString()
            dat_dog = actTip7DatDog.text.toString()
            sum_dolg = actTip7SumDolg.text.toString()
            pred_pogash_dolg_num = actTip7PredPogashDolgNum.text.toString()
            pred_pogash_dolg_date = actTip7PredPogashDolgDate.text.toString()
            uvedoml_otkl_num = actTip7UvedomlOtklNum.text.toString()
            uvedoml_otkl_date = actTip7UvedomlOtklDate.text.toString()
            fio_eso = actTip7FioEso.text.toString()
            name_dolzhn_eso = actTip7NameDolzhnEso.text.toString()
            tel_eso = actTip7TelEso.text.toString()
            fio_contact = actTip7FioPodp.text.toString()
            name_dolzhn_contact = actTip7NameDolzhnPodp.text.toString()
            tel_contact = actTip7TelPodp.text.toString()
            otkl_proizv = actTip7OtklProizv.text.toString()
            otkl_pu_pokaz_do = actTip7OtklPuPokazDo.text.toString()
            otkl_pu_pokaz_posle = actTip7OtklPuPokazPosle.text.toString()
            dop_info = actTip7DopInfo.text.toString()
            filial_address = actTip7FilialAddress.text.toString()
            filial_tel = actTip7FilialTel.text.toString()
            act_poluchil = actTip7ActPoluchil.text.toString()
            remark_dog = actTip7RemarkDog.text.toString()
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
        val vals: LinkedHashMap<String, String> = LinkedHashMap(25)
        vals["%FILIAL_ESO"] = _actFields.filial_eso
        vals["%NUM_ACT"] = _actFields.num_act
        vals["%DAT_ACT"] = _actFields.dat_act
        vals["%PAYER_NAME"] = _actFields.payer_name
        vals["%NAME_OBJ"] = _actFields.name_obj
        vals["%NDOG"] = _actFields.ndog
        vals["%DAT_DOG"] = _actFields.dat_dog
        vals["%SUM_DOLG"] = _actFields.sum_dolg
        vals["%PRED_POGASH_DOLG_NUM"] = _actFields.pred_pogash_dolg_num
        vals["%PRED_POGASH_DOLG_DATE"] = _actFields.pred_pogash_dolg_date
        vals["%UVEDOML_OTKL_NUM"] = _actFields.uvedoml_otkl_num
        vals["%UVEDOML_OTKL_DATE"] = _actFields.uvedoml_otkl_date
        vals["%FIO_ESO"] = _actFields.fio_eso
        vals["%NAME_DOLZHN_ESO"] = _actFields.name_dolzhn_eso
        vals["%TEL_ESO"] = _actFields.tel_eso
        vals["%FIO_PODP"] = _task.fio_podp
        vals["%NAME_DOLZHN_PODP"] = _task.name_dolzhn_podp
        vals["%TEL_PODP"] = _task.tel_podp
        vals["%OTKL_PROIZV"] = _actFields.otkl_proizv
        vals["%OTKL_PU_POKAZ_DO"] = _actFields.otkl_pu_pokaz_do
        vals["%OTKL_PU_POKAZ_POSLE"] = _actFields.otkl_pu_pokaz_posle
        vals["%DOP_INFO"] = _actFields.dop_info
        vals["%FILIAL_ADDRESS"] = _actFields.filial_address
        vals["%FILIAL_TEL"] = _actFields.filial_tel
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
        if (!btnCreateActOrgOtkl.isEnabled) {
            setResult(RESULT_OK)
            this@ActOgrOtkl.finish()
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
                    this@ActOgrOtkl.finish()
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