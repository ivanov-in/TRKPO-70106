package ru.infoenergo.mis.actsTemplates

import android.app.DatePickerDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.doOnPreDraw
import androidx.core.widget.doOnTextChanged
import kotlinx.android.synthetic.main.activity_act_universal.*
import kotlinx.coroutines.*
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
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap


/** ************************************************************************ **
 **       1 Акт о результатах проведения гидропневматической промывки        **
 **       2 Акт о результатах проведения гидроиспытаний                      **
 **       3 Акт проверки режимов теплопотребления                            **
 **       4 Акт о нарушении режимов                                          **
 **       5 Акт утечки теплоносителя                                         **
 **       6 Акт первичного осмотра                                           **
 **       7 Акт об ограничении с последующим снятием ограничения (за долги)  **
 **       8 Акт об отключении (за долги)                                     **
 **       9 Акт о включении (после оплаты)                                   **
 **       10 Акт об отключении (по заявке)                                   **
 **       11 Акт о включении (по заявке)                                     **
 **       12 Акт проверки сохранности пломбы                                 **
 **       13 Акт о самовольном пуске потребителя  tip 1                      **
 **       26 Универсальный Акт обследования ред. 2019  tip 13                **
 ** ************************************************************************ **/
class ActUniversal : AppCompatActivity(), DialogInterface.OnCancelListener {

    private lateinit var dlgConfirmActSign: DlgConfirmationActSign
    private var arrEtPodgotovka: ArrayList<ActUniversal.Podgotovka> = ArrayList()

    private lateinit var _task: Task
    private lateinit var _act: ActInfo
    private lateinit var _actFields: ActFieldsInfo

    inner class Podgotovka(
        var num: EditText,
        var name: EditText,
        var dat: EditText
    )

    private var new: Boolean = false
    private var npp: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_act_universal)

        _act = intent.getSerializableExtra("ACT") as ActInfo
        _task = intent.getSerializableExtra("TASK") as Task
        _actFields = intent.getSerializableExtra("ACT_FIELDS") as ActFieldsInfo
        arrEtPodgotovka.clear()

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
            btnSignUniversalAct.isEnabled = true

        try {
            if (supportActionBar != null) {
                val actionBar = supportActionBar
                actionBar!!.title = _act.name
                actionBar.subtitle = _actFields.num_act
                actionBar.elevation = 4.0F
                actionBar.setDisplayHomeAsUpEnabled(true)
            }

            arrEtPodgotovka.add(Podgotovka(actTip13PodgotovkaNum, actTip13PodgotovkaName, actTip13PodgotovkaDat))

            btnAddPodgotovka.setOnClickListener {
                if (actTip13PodgotovkaName.text.toString().isEmpty() &&
                    actTip13PodgotovkaDat.text.toString().isEmpty()
                ) {
                    return@setOnClickListener
                } else
                    if (arrEtPodgotovka.size > 30) {
                        return@setOnClickListener
                    } else addPodgotovkaFields()
            }

            // Заполнение вьюх
            //------------------------
            actTip1NumAct.text = _actFields.num_act
            actTip1Date.setOnClickListener { changeDate() }
            actTip1Date.text = if (_actFields.dat_act.isNotEmpty()) _actFields.dat_act
            else LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy г."))

            // если меняется дата, то подставить эту дату в подписи
            actTip1Date.doOnTextChanged { text, _, _, _ ->
                if (actTip1PodpisiDate.text != actTip1Date.text) actTip1PodpisiDate.text = text
            }

            actTip1PodpisiDate.text =
                if (_actFields.dat_act.isNotEmpty()) _actFields.dat_act
                else LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy г."))
            actTip1PodpisiDate.setOnClickListener { changeDate() }

            actTip13PodpisiFioEso.setText(_actFields.fio_eso)

            // Универсальный акт
            // --------------------------
            //if (_actFields.id_act == 26) {
            // если меняется ФИО представителя КТС, то подставить это ФИО в подписи (для типа 26)
            actTip1FioEso.doOnTextChanged { text, _, _, _ ->
                if (actTip13PodpisiFioEso.text != actTip1FioEso.text) actTip13PodpisiFioEso.setText(
                    text
                )
            }

            if (_actFields.podgotovka.isNotEmpty()) {
                actTip1Podgotovka.visibility = View.VISIBLE
                actTip1Podgotovka.setText(_actFields.podgotovka)
            }
            //  }
            // ------------------------------
            actTip1PayerName.setText(_actFields.payer_name)
            actTip1AdrOrg.setText(_actFields.adr_org)
            actTip1TelPodp.text = _task.tel_podp
            actTip1FioEso.setText(_actFields.fio_eso)
            actTip1TelEso.setText(_actFields.tel_eso)
            actTip1FioPodp.text = _task.fio_podp
            actTip13FioPodp2.text = _task.fio_podp
            actTip1DolzhnPodp.text = _task.name_dolzhn_podp
            actTip1NameObj.setText(_actFields.name_obj)
            actTip1PurposeText.setText(_actFields.purpose_text)
            actTip1ItogText.setText(_actFields.itog_text)
            actTip1RemarkDog.setText(_actFields.remark_dog)

            // слушатель на кнопку создания акта
            // ---------------------------------------
            btnCreateUniversalAct.setOnClickListener {
                createAct()
            }

            // слушатель на кнопку подписания акта
            // -------------------------------------
            btnSignUniversalAct.setOnClickListener {
                dlgConfirmActSign = DlgConfirmationActSign(
                    _task.tel_podp,
                    _task.email_podp,
                    _actFields.id_file
                )
                dlgConfirmActSign.show(supportFragmentManager, "SIGN_ACT")
            }

            // Спиннер с фразами
            // ----------------------
            actTip1SpCopyText.apply {
                adapter = ArrayAdapter(context!!, R.layout.spitem_purpose, PhrasesCopied)
                setSelection(0)
            }

            actTip1SpCopyText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val clipboard: ClipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("CopiedText", actTip1SpCopyText.selectedItem.toString())
                    clipboard.setPrimaryClip(clip)
                }
            }

        } catch (e: Exception) {
            println("$TAG_ERR ActUniversal onCreate: ${e.message}")
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
                    this@ActUniversal,
                    "Не найден шаблон, невозможно сформировать акт ${_actFields.num_act}.",
                    Toast.LENGTH_LONG
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
            pdfPath = "${pdfsFolder.path}/${_actFields.num_act.replace("\\", "-")}_${_act.name.replace(" ", "_")}.pdf"

            if (pdfPath.isEmpty()) {
                Toast.makeText(
                    this@ActUniversal, "Не удалось создать акт ${_actFields.num_act}.", Toast.LENGTH_LONG
                ).show()
                return false
            }
            if (htmlPath.isEmpty()) {
                Toast.makeText(
                    this@ActUniversal, "Не удалось получить шаблон ${_actFields.shablon}.", Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActUniversal,
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
                    btnSignUniversalAct.isEnabled = true

                // Открытие pdf
                // ---------------------------------------
                if (!signed) showPdf(this@ActUniversal, pdfPath)
            } else {
                Toast.makeText(
                    this@ActUniversal,
                    "Произошла ошибка при создании акта ${_actFields.num_act}.",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActUniversal,
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
        actTip1Date.isClickable = false
        actTip1PayerName.isEnabled = false
        actTip1AdrOrg.isEnabled = false
        actTip1TelPodp.isEnabled = false
        actTip1FioEso.isEnabled = false
        actTip1TelEso.isEnabled = false
        actTip1FioPodp.isEnabled = false
        actTip1DolzhnPodp.isEnabled = false
        actTip1NameObj.isEnabled = false
        actTip1PurposeText.isEnabled = false
        actTip1ItogText.isEnabled = false
        actTip1RemarkDog.isEnabled = false

        actTip13PodpisiFioEso.isEnabled = false
        actTip1PodpisiDate.isEnabled = false
        actTip13FioPodp2.isEnabled = false

        // if (_act.id_act == 26) {
        btnAddPodgotovka.isEnabled = false

        actTip13PodgotovkaNum.isEnabled = false
        actTip13PodgotovkaName.isEnabled = false
        actTip13PodgotovkaDat.isEnabled = false

        arrEtPodgotovka.forEach {
            it.num.isEnabled = false
            it.name.isEnabled = false
            it.dat.isEnabled = false
        }
        //  }

        btnCreateUniversalAct.isEnabled = false
        btnSignUniversalAct.isEnabled = false
        actTip1SpCopyText.isEnabled = false
    }

    // Смена даты
    // -----------------------
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
                        actTip1Date.text = _actFields.dat_act
                        actTip1PodpisiDate.text = _actFields.dat_act
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
            dat_act = actTip1Date.text.toString()
            payer_name = actTip1PayerName.text.toString()
            adr_org = actTip1AdrOrg.text.toString()
            tel_contact = actTip1TelPodp.text.toString()
            fio_eso = actTip1FioEso.text.toString()
            tel_eso = actTip1TelEso.text.toString()
            fio_contact = actTip1FioPodp.text.toString()
            name_dolzhn_contact = actTip1DolzhnPodp.text.toString()
            name_obj = actTip1NameObj.text.toString()
            purpose_text = actTip1PurposeText.text.toString()
            itog_text = actTip1ItogText.text.toString()
            remark_dog = actTip1RemarkDog.text.toString()

            try {
                if (actTip1Podgotovka.visibility == View.VISIBLE && actTip1Podgotovka.text.isNotEmpty())
                    podgotovka = actTip1Podgotovka.text.toString()

                if (podgotovka.isNotEmpty() && podgotovka.isNotBlank())
                    podgotovka += "\n"
                podgotovka += arrEtPodgotovka.joinToString { p ->
                    if (p.dat.text.isNotEmpty() || p.name.text.isNotEmpty()) {
                        p.num.text.toString() + ".  " + p.name.text.toString() + ".  " + p.dat.text.toString() + ";\n"
                    } else ""
                }
                podgotovka = podgotovka.replace("\n\n", "\n").replace("\n, ", "\n")
            } catch (e: Exception) {
                println("$TAG_ERR ActUniversal podgotovka: ${e.message}")

            }
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
        val vals: LinkedHashMap<String, String> = LinkedHashMap(15)
        vals["%NUM_ACT"] = _actFields.num_act
        vals["%DAT_ACT"] = _actFields.dat_act
        vals["%PAYER_NAME"] = _actFields.payer_name
        vals["%ADR_ORG"] = _actFields.adr_org
        vals["%TEL_PODP"] = _task.tel_podp
        vals["%FIO_ESO"] = _actFields.fio_eso
        vals["%TEL_ESO"] = _actFields.tel_eso
        vals["%FIO_PODP"] = _task.fio_podp
        vals["%NAME_DOLZHN_PODP"] = _task.name_dolzhn_podp
        vals["%NAME_OBJ"] = _actFields.name_obj
        vals["%PURPOSE_TEXT"] = _actFields.purpose_text
        vals["%ITOG_TEXT"] = _actFields.itog_text

        // if (_act.id_act == 26) {
        //vals["%PODGOTOVKA"] = _actFields.podgotovka
        var table = ""//"<table border=\"1px\" style=\"border-color: black; width: 100%;\" cellspacing=\"0\">"

        if (actTip1Podgotovka.visibility == View.VISIBLE && actTip1Podgotovka.text.isNotEmpty() && actTip1Podgotovka.text.isNotBlank()) {
            var str = actTip1Podgotovka.text.toString().replace("\n", "<br/>")
            if (str.length > 5 && str.take(5) == "<br/>")
                str = str.drop(5)
            while (str.length > 5 && str.takeLast(5) == "<br/>")
                str = str.dropLast(5)
            str = str.replace(";<br/>, ", ";<br/>")
            table += "<tr><td></td><td width=\"142px\">$str</td><td width=\"45px\"></td></tr>"
        }

        if (arrEtPodgotovka.size > 0) {
            table += arrEtPodgotovka.joinToString {
                if (it.name.text.toString().isNotEmpty() && it.name.text.toString().isNotBlank() ||
                    it.dat.text.toString().isNotEmpty() && it.dat.text.toString().isNotBlank()
                )
                    "<tr><td style=\"width: 10px; text-align: center;\">${it.num.text}</td><td width=\"132px\">${it.name.text}</td><td width=\"45px\">${it.dat.text}</td></tr>"
                else ""
            }
            // table += "\\n</table>"
        }
        vals["%PODGOTOVKA"] = table
        vals["%REMARK_DOG"] =
            if (_actFields.remark_dog.isNotEmpty()) "Замечания абонента: " + _actFields.remark_dog else ""

        if (signed) {
            vals["%ACT_SIGNED"] = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        }

        //  }
        return vals
    }

    // Добавление в разметку новых строк рекоммендаций
    // (мероприятие EditText, сроки EditText)
    // --------------------------------------
    private fun addPodgotovkaFields() {
        try {
            val etNewPodgotovkaNum = EditText(this)
            etNewPodgotovkaNum.apply {
                textSize = 18f
                hint = "№"
                layoutParams = LinearLayout.LayoutParams(
                    50,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                setPaddingRelative(6, 6, 6, 10)
                setTextColor(ContextCompat.getColor(context, R.color.fontObject))
                inputType = InputType.TYPE_CLASS_NUMBER
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                isSingleLine = true
                maxLines = 1
            }
            etNewPodgotovkaNum.doOnPreDraw {
                try {
                    if (actTip1Podgotovka.visibility == View.GONE || actTip1Podgotovka.text.isEmpty())
                        etNewPodgotovkaNum.setText("${arrEtPodgotovka.size}")
                } catch (e: Exception) {
                    println("$TAG_ERR etNewPodgotovkaNum doOnPreDraw ${e.message}")
                }
            }

            val etNewPodgotovkaName = EditText(this)
            etNewPodgotovkaName.apply {
                textSize = 18f
                hint = "Наименование мероприятий"
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    3f
                )
                setPaddingRelative(6, 6, 6, 10)
                setTextColor(ContextCompat.getColor(context, R.color.fontObject))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                isSingleLine = false
                maxLines = 5
                isVerticalScrollBarEnabled = true
                scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
            }

            val etNewPodgotovkaDat = EditText(this)
            etNewPodgotovkaDat.apply {
                textSize = 18f
                hint = "Срок исполнения"
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                )
                setPaddingRelative(6, 6, 6, 10)
                setTextColor(ContextCompat.getColor(context, R.color.fontObject))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                isSingleLine = false
                maxLines = 5
                isVerticalScrollBarEnabled = true
                scrollBarStyle = View.SCROLLBARS_INSIDE_INSET
            }

            val lvPodgotovka = LinearLayout(this)
            lvPodgotovka.apply {
                layoutParams =
                    ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            }

            lvPodgotovka.addView(etNewPodgotovkaNum)
            lvPodgotovka.addView(etNewPodgotovkaName)
            lvPodgotovka.addView(etNewPodgotovkaDat)

            actTip1Recommend.addView(lvPodgotovka)

            arrEtPodgotovka.add(Podgotovka(etNewPodgotovkaNum, etNewPodgotovkaName, etNewPodgotovkaDat))
        } catch (e: Exception) {
            println("$TAG_ERR: ${e.message}")
        }
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
        if (!btnCreateUniversalAct.isEnabled) {
            setResult(RESULT_OK)
            this@ActUniversal.finish()
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
                    this@ActUniversal.finish()
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
                        val res = withContext(Dispatchers.IO) {
                            dlgConfirmActSign.sendSignedAct()
                        }
                        if (res)
                            Toast.makeText(
                                this@ActUniversal,
                                "Акт успешно отправлен на email: ${_task.email_podp}.", Toast.LENGTH_LONG
                            ).show()
                        else
                            Toast.makeText(
                                this@ActUniversal,
                                "Произошла ошибка при отправке акта на email: ${_task.email_podp}. Попробуйте отправить акт вручную (\"Поделиться\")",
                                Toast.LENGTH_LONG
                            ).show()
                    }
                }
                Toast.makeText(this, "Акт успешно подписан.", Toast.LENGTH_LONG).show()
            } else
                Toast.makeText(this, "Произошла непредвиденная ошибка :(", Toast.LENGTH_LONG).show()

            dbWrite.close()
            setEditableFalse()
        }
    }
}