package ru.infoenergo.mis.actsTemplates

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import kotlinx.android.synthetic.main.activity_act_otop_period.*
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
import android.widget.AdapterView
import android.widget.ArrayAdapter

/** ******************************************************************************* **
 **   14 id Акт проверки филиалом готовности абонента к отопительному периоду 2 tip **
 ** ******************************************************************************* **/
class ActOtopPeriod : AppCompatActivity(), DialogInterface.OnCancelListener {
    private lateinit var dlgConfirmActSign: DlgConfirmationActSign

    private lateinit var _actFields: ActFieldsInfo
    private lateinit var _task: Task
    private lateinit var _act: ActInfo

    private var new: Boolean = false
    private var npp: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_act_otop_period)

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
            btnSignOtopPeriodAct.isEnabled = true

        try {
            if (supportActionBar != null) {
                val actionBar = supportActionBar
                actionBar!!.title = _act.name
                actionBar.subtitle = _actFields.num_act
                actionBar.elevation = 4.0F
                actionBar.setDisplayHomeAsUpEnabled(true)
            }

            // region Заполнение вьюх
            //------------------------
            actTip2NumAct.text = _actFields.num_act
            actTip2DatAct.setOnClickListener { changeDate() }
            actTip2DatAct.text = if (_actFields.dat_act.isNotEmpty()) _actFields.dat_act else LocalDate.now().toString()
            actTip2OtopPeriod.setText(_actFields.otop_period)

            actTip2PayerName.setText(_actFields.payer_name)
            actTip2ListObj.setText(_actFields.list_obj)
            actTip2DatDog.setText(_actFields.dat_dog)
            actTip2Ndog.setText(_actFields.ndog)

            actTip2NameDolzhnPodp.apply {
                text = _task.name_dolzhn_podp
                doAfterTextChanged { actTip2NameDolzhnPodp2.text = it.toString() }
            }
            actTip2FioPodp.text = _task.fio_podp
            actTip2FioPodp.doAfterTextChanged { actTip2FioPodp2.text = it.toString() }

            actTip2TelPodp.text = _task.tel_podp

            actTip2FilialEso.text = _actFields.filial_eso
            actTip2NameDolzhnEso.apply {
                setText(_actFields.name_dolzhn_eso)
                doAfterTextChanged { actTip2NameDolzhnEso2.text = it.toString() }
            }

            actTip2FioEso.setText(_actFields.fio_eso)
            actTip2FioEso.doAfterTextChanged { actTip2FioEso2.text = it.toString() }

            actTip2SpinnerManometr.adapter = ArrayAdapter(
                this,
                R.layout.spitem_purpose,
                arrayOf(
                    "Манометры в наличии, поверены",
                    "Манометры в наличии, не поверены",
                    "Манометры отсутствуют"
                )
            )
            actTip2SpinnerManometr.setSelection(_actFields.id_manometr.toInt())
            actTip2SpinnerAuprSo.adapter = ArrayAdapter(
                this,
                R.layout.spitem_purpose,
                arrayOf("АУПР СО в работе", "АУПР СО неисправен", "Элеваторный узел")
            )
            actTip2SpinnerAuprSo.setSelection(_actFields.id_aupr_so.toInt())
            actTip2SpinnerAuprGvs.adapter = ArrayAdapter(
                this,
                R.layout.spitem_purpose,
                arrayOf("АУПР ГВС в работе", "АУПР ГВС неисправен", "ГВС отсутствует")
            )
            actTip2SpinnerAuprGvs.setSelection(_actFields.id_aupr_gvs.toInt())
            actTip2SpinnerAuprSv.adapter = ArrayAdapter(
                this,
                R.layout.spitem_purpose,
                arrayOf("АУПР СВ в работе", "АУПР СВ неисправен", "СВ отсутствует")
            )
            actTip2SpinnerAuprSv.setSelection(_actFields.id_aupr_sv.toInt())

            //region спиннеры слушатели
            actTip2SpinnerManometr.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    actTip2SostKipStr.text =
                        "${actTip2SpinnerManometr.selectedItem}; ${actTip2SpinnerAuprSo.selectedItem}; ${actTip2SpinnerAuprGvs.selectedItem}; ${actTip2SpinnerAuprSv.selectedItem}."
                }
            }
            actTip2SpinnerAuprSo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    actTip2SostKipStr.text =
                        "${actTip2SpinnerManometr.selectedItem}; ${actTip2SpinnerAuprSo.selectedItem}; ${actTip2SpinnerAuprGvs.selectedItem}; ${actTip2SpinnerAuprSv.selectedItem}"
                }
            }
            actTip2SpinnerAuprGvs.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    actTip2SostKipStr.text =
                        "${actTip2SpinnerManometr.selectedItem}; ${actTip2SpinnerAuprSo.selectedItem}; ${actTip2SpinnerAuprGvs.selectedItem}; ${actTip2SpinnerAuprSv.selectedItem}"
                }
            }
            actTip2SpinnerAuprSv.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    actTip2SostKipStr.text =
                        "${actTip2SpinnerManometr.selectedItem}; ${actTip2SpinnerAuprSo.selectedItem}; ${actTip2SpinnerAuprGvs.selectedItem}; ${actTip2SpinnerAuprSv.selectedItem}"
                }
            }
            //endregion

            actTip2NalActGidro.setText(_actFields.nal_act_gidro)

            actTip2SpinnerSostTube.adapter =
                ArrayAdapter(this, R.layout.spitem_purpose, arrayOf("Соответствует ПТЭ", "Не соответствует ПТЭ"))
            actTip2SpinnerSostTube.setSelection(_actFields.id_sost_tube.toInt())
            actTip2SpinnerSostArmatur.adapter =
                ArrayAdapter(this, R.layout.spitem_purpose, arrayOf("Соответствует ПТЭ", "Не соответствует ПТЭ"))
            actTip2SpinnerSostArmatur.setSelection(_actFields.id_sost_armatur.toInt())
            actTip2SpinnerSostIzol.adapter =
                ArrayAdapter(this, R.layout.spitem_purpose, arrayOf("Соответствует ПТЭ", "Не соответствует ПТЭ"))
            actTip2SpinnerSostIzol.setSelection(_actFields.id_sost_izol.toInt())

            //region спиннеры слушатели
            actTip2SpinnerSostTube.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    actTip2SostTubeStr.text =
                        "Состояние трубопроводов теплового пункта ${actTip2SpinnerSostTube.selectedItem}; Состояние арматуры теплового пункта ${actTip2SpinnerSostArmatur.selectedItem}; Состояние тепловой изоляции ${actTip2SpinnerSostIzol.selectedItem}."
                }
            }
            actTip2SpinnerSostArmatur.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    actTip2SostTubeStr.text =
                        "Состояние трубопроводов теплового пункта ${actTip2SpinnerSostTube.selectedItem}; Состояние арматуры теплового пункта ${actTip2SpinnerSostArmatur.selectedItem}; Состояние тепловой изоляции ${actTip2SpinnerSostIzol.selectedItem}."
                }
            }
            actTip2SpinnerSostIzol.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    actTip2SostTubeStr.text =
                        "Состояние трубопроводов теплового пункта ${actTip2SpinnerSostTube.selectedItem}; Состояние арматуры теплового пункта ${actTip2SpinnerSostArmatur.selectedItem}; Состояние тепловой изоляции ${actTip2SpinnerSostIzol.selectedItem}."
                }
            }
            //endregion

            actTip2SpinnerSostNet.apply {
                adapter = ArrayAdapter(
                    context!!, R.layout.spitem_purpose, arrayOf(
                        "Соответствует ПТЭ",
                        "Не соответствует ПТЭ"
                    )
                )
                setSelection(_actFields.id_sost_net.toInt())
            }
            actTip2SpinnerSostUtepl.apply {
                adapter = ArrayAdapter(
                    context!!, R.layout.spitem_purpose, arrayOf(
                        "Удовлетворительное",
                        "Не удовлетворительное"
                    )
                )
                setSelection(_actFields.id_sost_utepl.toInt())
            }

            actTip2SpinnerNalPasport.adapter =
                ArrayAdapter(this, R.layout.spitem_purpose, arrayOf("В наличии", "Отсутствуют"))
            actTip2SpinnerNalPasport.setSelection(_actFields.id_nal_pasport.toInt())
            actTip2SpinnerNalSchema.adapter =
                ArrayAdapter(this, R.layout.spitem_purpose, arrayOf("В наличии", "Отсутствуют"))
            actTip2SpinnerNalSchema.setSelection(_actFields.id_nal_schema.toInt())
            actTip2SpinnerNalInstr.adapter =
                ArrayAdapter(this, R.layout.spitem_purpose, arrayOf("В наличии", "Отсутствуют"))
            actTip2SpinnerNalInstr.setSelection(_actFields.id_nal_instr.toInt())

            //region спиннеры слушатели
            actTip2SpinnerNalPasport.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    actTip2NalPaspStr.text =
                        "Наличие паспортов тепловых теплоустановок: ${actTip2SpinnerNalPasport.selectedItem}; Наличие схем тепловых пунктов: ${actTip2SpinnerNalSchema.selectedItem}; Наличие инструкций для обслуживающего персонала: ${actTip2SpinnerNalInstr.selectedItem}."
                }
            }
            actTip2SpinnerNalSchema.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    actTip2NalPaspStr.text =
                        "Наличие паспортов тепловых теплоустановок: ${actTip2SpinnerNalPasport.selectedItem}; Наличие схем тепловых пунктов: ${actTip2SpinnerNalSchema.selectedItem}; Наличие инструкций для обслуживающего персонала: ${actTip2SpinnerNalInstr.selectedItem}."
                }
            }
            actTip2SpinnerNalInstr.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    actTip2NalPaspStr.text =
                        "Наличие паспортов тепловых теплоустановок: ${actTip2SpinnerNalPasport.selectedItem}; Наличие схем тепловых пунктов: ${actTip2SpinnerNalSchema.selectedItem}; Наличие инструкций для обслуживающего персонала: ${actTip2SpinnerNalInstr.selectedItem}."
                }
            }
            //endregion

            actTip2SpinnerNalDirectConnect.apply {
                adapter = ArrayAdapter(context!!, R.layout.spitem_purpose, arrayOf("Отсутствуют", "В наличии"))
                setSelection(_actFields.id_nal_direct_connect.toInt())
            }

            actTip2SumDolg.setText(_actFields.sum_dolg)
            actTip2DopInfo.setText(_actFields.dop_info)

            actTip2NalPodpDoc.setText(_actFields.nal_podp_doc)
            actTip2OplCalcul.setText(_actFields.opl_calcul)

            actTip2NameDolzhnEso2.text = _actFields.name_dolzhn_eso
            actTip2FioEso2.text = _actFields.fio_eso

            actTip2NameDolzhnPodp2.text = _task.name_dolzhn_podp
            actTip2FioPodp2.text = _task.fio_podp
            actTip2RemarkDog.setText(_actFields.remark_dog)
            //endregion

            // слушатели на кнопку Создать акт
            // ----------------------------------------
            btnCreateOtopPeriodAct.setOnClickListener {
                createAct()
            }

            // слушатели на кнопку Подписать акт
            // --------------------------------------
            btnSignOtopPeriodAct.setOnClickListener {
                try {
                    dlgConfirmActSign = DlgConfirmationActSign(
                        _task.tel_podp,
                        _task.email_podp,
                        _actFields.id_file
                    )
                    dlgConfirmActSign.show(supportFragmentManager, "SIGN_ACT")
                } catch (e: Exception) {
                    println("$TAG_ERR ActOtopPeriod: ${e.message}")
                }
            }

            // Спиннер с фразами
            // ----------------------
            actTip2SpCopyText.apply {
                adapter = ArrayAdapter(context!!, R.layout.spitem_purpose, PhrasesCopied)
                setSelection(0)
            }

            actTip2SpCopyText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val clipboard: ClipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("CopiedText", actTip2SpCopyText.selectedItem.toString())
                    clipboard.setPrimaryClip(clip)
                }
            }
        } catch (e: Exception) {
            println("$TAG_ERR ActOtopPeriod: ${e.message}")
        }
    }

    // На кнопку Создать акт
    // ---------------------------------------------
    private fun createAct(signed: Boolean = false): Boolean {
        val pdfPath: String
        val htmlPath: String
        val pdfsFolder: File?

        try {
            // Получаем путь к папке приложения, ищем шаблоны
            // ----------------------------------------------
            val external = this.baseContext.getExternalFilesDir(null)
            val template = File("$external/templates")
            if (!template.exists()) {
                Toast.makeText(
                    this@ActOtopPeriod, "Не найден шаблон, невозможно сформировать акт.", Toast.LENGTH_LONG
                ).show()
                return false
            }

            // Создаём папку для актов
            // -----------------------
            pdfsFolder = File("$external/tmpFiles/${_task.id_task}")
            if (!pdfsFolder.exists()) pdfsFolder.mkdirs()

            // Формирование путей html и pdf
            // -----------------------------
            htmlPath = "$template/${_actFields.shablon.replace(".pdf", ".html")}"
            pdfPath = "${pdfsFolder.path}/${_actFields.num_act.replace("\\", "-")}_${_act.name.replace(" ", "_")}"

            if (pdfPath.isEmpty()) {
                Toast.makeText(
                    this@ActOtopPeriod, "Не удалось создать акт ${_actFields.num_act}.", Toast.LENGTH_LONG
                ).show()
                return false
            }
            if (htmlPath.isEmpty()) {
                Toast.makeText(
                    this@ActOtopPeriod, "Не удалось получить шаблон ${_actFields.shablon}.", Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActOtopPeriod,
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
                    it.path.contains(pdfPath.substringAfterLast("/"), true)
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
                    Toast.makeText(this, "Данные акта ${_actFields.num_act} сохранены в базу данных.", Toast.LENGTH_LONG).show()
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

                // Если после подписания, то просто update blob и дату отправки клиенту
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
                    btnSignOtopPeriodAct.isEnabled = true

                // Открытие pdf
                // ---------------------------------------
                if (!signed) showPdf(this@ActOtopPeriod, pdfPath)
            } else {
                Toast.makeText(
                    this@ActOtopPeriod,
                    "Произошла ошибка при создании акта ${_actFields.num_act}.",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActOtopPeriod,
                "Произошла ошибка при создании акта ${_actFields.num_act}.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
        return true
    }

    // Установка недоступности редактирования, если акт подписан
    // ---------------------------------------------------------
    private fun setEditableFalse() {
        actTip2DatAct.isClickable = false
        actTip2OtopPeriod.isEnabled = false
        actTip2NumAct.isEnabled = false
        actTip2PayerName.isEnabled = false
        actTip2ListObj.isEnabled = false
        actTip2Ndog.isEnabled = false
        actTip2DatDog.isEnabled = false
        actTip2NameDolzhnPodp.isEnabled = false
        actTip2FioPodp.isEnabled = false
        actTip2TelPodp.isEnabled = false

        actTip2SpinnerManometr.isEnabled = false
        actTip2SpinnerAuprSv.isEnabled = false
        actTip2SpinnerAuprSo.isEnabled = false
        actTip2SpinnerAuprGvs.isEnabled = false

        actTip2SpinnerNalSchema.isEnabled = false
        actTip2SpinnerNalInstr.isEnabled = false
        actTip2SpinnerNalPasport.isEnabled = false

        actTip2SpinnerSostIzol.isEnabled = false
        actTip2SpinnerSostArmatur.isEnabled = false
        actTip2SpinnerSostTube.isEnabled = false

        actTip2SpinnerSostUtepl.isEnabled = false
        actTip2SpinnerSostNet.isEnabled = false
        actTip2SpinnerNalDirectConnect.isEnabled = false

        actTip2NameDolzhnEso.isEnabled = false
        actTip2OplCalcul.isEnabled = false
        actTip2NalPodpDoc.isEnabled = false
        actTip2SumDolg.isEnabled = false
        actTip2DopInfo.isEnabled = false
        actTip2NalActGidro.isEnabled = false

        actTip2FioEso.isEnabled = false
        actTip2FioPodp2.isEnabled = false
        actTip2RemarkDog.isEnabled = false

        btnCreateOtopPeriodAct.isEnabled = false
        btnSignOtopPeriodAct.isEnabled = false
        actTip2SpCopyText.isEnabled = false
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
                        actTip2DatAct.text = _actFields.dat_act
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
            otop_period = actTip2OtopPeriod.text.toString()

            payer_name = actTip2PayerName.text.toString()
            list_obj = actTip2ListObj.text.toString()
            ndog = actTip2Ndog.text.toString()
            dat_dog = actTip2DatDog.text.toString()

            name_dolzhn_eso = actTip2NameDolzhnEso.text.toString()
            fio_eso = actTip2FioEso.text.toString()

            name_dolzhn_contact = actTip2NameDolzhnPodp.text.toString()
            fio_contact = actTip2FioPodp.text.toString()
            tel_contact = actTip2TelPodp.text.toString()

            id_manometr = actTip2SpinnerManometr.selectedItemId.toString()
            id_aupr_so = actTip2SpinnerAuprSo.selectedItemId.toString()
            id_aupr_sv = actTip2SpinnerAuprSv.selectedItemId.toString()
            id_aupr_gvs = actTip2SpinnerAuprGvs.selectedItemId.toString()
            sost_kip_str = actTip2SostKipStr.text.toString()

            nal_act_gidro = actTip2NalActGidro.text.toString()

            id_sost_tube = actTip2SpinnerSostTube.selectedItemId.toString()
            id_sost_armatur = actTip2SpinnerSostArmatur.selectedItemId.toString()
            id_sost_izol = actTip2SpinnerSostIzol.selectedItemId.toString()
            sost_tube_str = actTip2SostTubeStr.text.toString()

            id_sost_net = actTip2SpinnerSostNet.selectedItemId.toString()
            sost_net_str = actTip2SpinnerSostNet.selectedItem.toString()

            id_sost_utepl = actTip2SpinnerSostUtepl.selectedItemId.toString()
            sost_utepl_str = actTip2SpinnerSostUtepl.selectedItem.toString()

            id_nal_pasport = actTip2SpinnerNalPasport.selectedItemId.toString()
            id_nal_schema = actTip2SpinnerNalSchema.selectedItemId.toString()
            id_nal_instr = actTip2SpinnerNalInstr.selectedItemId.toString()
            nal_pasp_str = actTip2NalPaspStr.text.toString()

            id_nal_direct_connect =
                actTip2SpinnerNalDirectConnect.selectedItemId.toString()
            nal_direct_connect =
                actTip2NalDirectConnect.text.toString().replace("\n", "")

            sum_dolg = actTip2SumDolg.text.toString()
            dop_info = actTip2DopInfo.text.toString()

            nal_podp_doc = actTip2NalPodpDoc.text.toString()
            opl_calcul = actTip2OplCalcul.text.toString()
            remark_dog = actTip2RemarkDog.text.toString()

            podpisi = actTip2FioPodp2.text.toString()
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
    private fun getHashMapVals(signed: Boolean = false): java.util.LinkedHashMap<String, String> {
        val vals: LinkedHashMap<String, String> = LinkedHashMap(24)
        vals["%FILIAL_ESO"] = _actFields.filial_eso
        vals["%NUM_ACT"] = _actFields.num_act
        vals["%DAT_ACT"] = _actFields.dat_act
        vals["%OTOP_PERIOD"] = _actFields.otop_period
        vals["%PAYER_NAME"] = _actFields.payer_name
        vals["%LIST_OBJ"] = _actFields.list_obj
        vals["%NDOG"] = _actFields.ndog
        vals["%DAT_DOG"] = _actFields.dat_dog
        vals["%FIO_PODP"] = _task.fio_podp
        vals["%TEL_PODP"] = _task.tel_podp
        vals["%NAME_DOLZHN_PODP"] = _task.name_dolzhn_podp
        vals["%SOST_KIP_STR"] = _actFields.sost_kip_str
        vals["%NAL_ACT_GIDRO"] = _actFields.nal_act_gidro
        vals["%SOST_TUBE_STR"] = _actFields.sost_tube_str
        vals["%SOST_NET_STR"] = _actFields.sost_net_str
        vals["%SOST_UTEPL_STR"] = _actFields.sost_utepl_str
        vals["%NAL_PASP_STR"] = _actFields.nal_pasp_str
        vals["%NAL_DIRECT_CONNECT"] = _actFields.nal_direct_connect
        vals["%SUM_DOLG"] = _actFields.sum_dolg
        vals["%DOP_INFO"] = _actFields.dop_info
        vals["%NAL_PODP_DOC"] = _actFields.nal_podp_doc
        vals["%OPL_CALCUL"] = _actFields.opl_calcul
        vals["%NAME_DOLZHN_ESO"] = _actFields.name_dolzhn_eso
        vals["%FIO_ESO"] = _actFields.fio_eso

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
        if (!btnCreateOtopPeriodAct.isEnabled) {
            setResult(RESULT_OK)
            this@ActOtopPeriod.finish()
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
                    this@ActOtopPeriod.finish()
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