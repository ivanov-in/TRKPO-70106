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
import kotlinx.android.synthetic.main.activity_act_preddog_asuse.*
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


/** *************************************************************************** **
 ** 22 id Акт преддоговорного осмотра (Карточка АСУСЭ по тепловым сетям) 10 tip **
 ** *************************************************************************** **/
class ActPreddogAsuse : AppCompatActivity(), DialogInterface.OnCancelListener {

    private lateinit var dlgConfirmActSign: DlgConfirmationActSign
    private lateinit var _actFields: ActFieldsInfo
    private lateinit var _task: Task
    private lateinit var _act: ActInfo
    private lateinit var _actFieldsDop: ArrayList<ActFieldsDopInfo>

    private var new: Boolean = false
    private var npp: Int = 0

    private var arrayUch: ArrayList<ActPreddogAsuse.Uch> = ArrayList()

    class Uch(
        var nom_uch: EditText,    // №п/п
        var tip_name: EditText,   // Тип участка
        var pt_d: EditText,       // Диаметр под. тр-д
        var pt_l: EditText,       // Длина под. тр-д
        var name_pr_pt: EditText, // Тип прокл. под. тр-д
        var ot_d: EditText,       // Диаметр обр. тр-д
        var ot_l: EditText,       // Длина обр. тр-д
        var name_pr_ot: EditText, // Тип прокл.обр. тр-д
        var uch_hgr: EditText     // Глубина залегания
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_act_preddog_asuse)

        _act = intent.getSerializableExtra("ACT") as ActInfo
        _task = intent.getSerializableExtra("TASK") as Task
        _actFields = intent.getSerializableExtra("ACT_FIELDS") as ActFieldsInfo

        _actFieldsDop = ArrayList()
        arrayUch = ArrayList()

        if (_act.id_act == 0 || _task.id_task == 0)
            finish()

        val dbRead = DbHandlerLocalRead(this, null)

        // Если пришли поля ACT_FIELDS, то это редактирование
        // Если не пришли, то это новый акт, получаем поля из act_fields_shablon
        if (_actFields.id_task == 0 && _actFields.id_act == 0 && _actFields.npp == 0) {
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
        }
        npp = _actFields.npp

        if (!new && _task.kod_emp_podp != 0)
            btnSignActPreddogAsuse.isEnabled = true

        try {
            if (supportActionBar != null) {
                val actionBar = supportActionBar
                actionBar!!.title = _act.name
                actionBar.subtitle = _actFields.num_act
                actionBar.elevation = 4.0F
                actionBar.setDisplayHomeAsUpEnabled(true)
            }

            // Получаем значения доп полей из лок БД
            _actFieldsDop = if (new)
                dbRead.getActFieldsDopShablon(_actFields.id_task, _actFields.id_act)
            else
                dbRead.getActFieldsDop(_actFields.id_task, _actFields.id_act, _actFields.npp)
            dbRead.close()

            actTip10NumAct.text = _actFields.num_act
            actTip10Date.setOnClickListener { changeDate() }
            actTip10Date.text = if (_actFields.dat_act.isNotEmpty()) _actFields.dat_act
            else LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy г."))
            actTip10NameTk.setText(_actFields.name_tk)
            actTip10NameAw.setText(_actFields.name_aw)
            actTip10NameObj.setText(_actFields.name_obj)
            actTip10NalSo.setText(_actFields.nal_so)
            actTip10NalSw.setText(_actFields.nal_sw)
            actTip10NalGv.setText(_actFields.nal_gv)
            actTip10PodpisiFioEso.setText(_actFields.fio_eso)

            if (_actFieldsDop.size > 0)
                _actFieldsDop.forEach {
                    addUch(it)
                }

            btnActTip10AddGridRow.setOnClickListener { addUch() }

            // слушатели на кнопки
            // --------------------------
            btnCreateActPreddogAsuse.setOnClickListener {
                createAct()
            }

            btnSignActPreddogAsuse.setOnClickListener {
                try {
                    dlgConfirmActSign = DlgConfirmationActSign(
                        _task.tel_podp,
                        _task.email_podp,
                        _actFields.id_file
                    )
                    dlgConfirmActSign.show(supportFragmentManager, "SIGN_ACT")
                } catch (e: Exception) {
                    println("$TAG_ERR ActPreddogAsuse: ${e.message}")
                }
            }

            //------------------
            // Спиннер с фразами
            // -----------------
            actTip10SpCopyText.apply {
                adapter = ArrayAdapter(context!!, R.layout.spitem_purpose, PhrasesCopied)
                setSelection(0)
            }

            actTip10SpCopyText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val clipboard: ClipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("CopiedText", actTip10SpCopyText.selectedItem.toString())
                    clipboard.setPrimaryClip(clip)
                }
            }
        } catch (e: Exception) {
            println("$TAG_ERR ActPreddogAsuse: ${e.message}")
        }
    }

    // Добавление участка в разметку
    // ------------------------------------------------
    private fun addUch(dop: ActFieldsDopInfo? = null) {
        try {
            val nomUch = EditText(this)
            nomUch.apply {
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    50, LinearLayout.LayoutParams.MATCH_PARENT
                )
                minHeight = 40
                setPaddingRelative(1, 1, 1, 1)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_item)
            }
            nomUch.doOnPreDraw {
                try {
                    if (dop != null)
                        nomUch.setText(dop.nom_uch)
                    else
                        nomUch.setText("${arrayUch.size}")
                } catch (e: Exception) {
                    println("$TAG_ERR addUch puNum doOnPreDraw ${e.message}")
                }
            }

            val tipName = EditText(this)
            tipName.apply {
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    200, LinearLayout.LayoutParams.MATCH_PARENT
                )
                minHeight = 40
                isSingleLine = false
                setPaddingRelative(1, 1, 1, 1)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_item)
            }
            tipName.doOnPreDraw {
                try {
                    if (dop != null)
                        tipName.setText(dop.tip_name)
                } catch (e: Exception) {
                    println("$TAG_ERR addUch tipName doOnPreDraw ${e.message}")
                }
            }

            val ptd = EditText(this)
            ptd.apply {
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    120, LinearLayout.LayoutParams.MATCH_PARENT
                )
                minHeight = 40
                setPaddingRelative(1, 1, 1, 1)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_item)
            }
            ptd.doOnPreDraw {
                try {
                    if (dop != null)
                        ptd.setText(dop.pt_d.toString())
                } catch (e: Exception) {
                    println("$TAG_ERR addUch ptd doOnPreDraw ${e.message}")
                }
            }

            val ptl = EditText(this)
            ptl.apply {
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    120, LinearLayout.LayoutParams.MATCH_PARENT
                )
                minHeight = 40
                setPaddingRelative(1, 1, 1, 1)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_item)
            }
            ptl.doOnPreDraw {
                try {
                    if (dop != null)
                        ptl.setText(dop.pt_l.toString())
                } catch (e: Exception) {
                    println("$TAG_ERR addUch ptl doOnPreDraw ${e.message}")
                }
            }

            val namePrPt = EditText(this)
            namePrPt.apply {
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    150, LinearLayout.LayoutParams.MATCH_PARENT
                )
                minHeight = 40
                setPaddingRelative(1, 1, 1, 1)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_item)
            }
            namePrPt.doOnPreDraw {
                try {
                    if (dop != null)
                        namePrPt.setText(dop.name_pr_pt)
                } catch (e: Exception) {
                    println("$TAG_ERR addUch namePrPt doOnPreDraw ${e.message}")
                }
            }

            val otl = EditText(this)
            otl.apply {
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    120, LinearLayout.LayoutParams.MATCH_PARENT
                )
                minHeight = 40
                setPaddingRelative(1, 1, 1, 1)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_item)
            }
            otl.doOnPreDraw {
                try {
                    if (dop != null)
                        otl.setText(dop.ot_l.toString())
                } catch (e: Exception) {
                    println("$TAG_ERR addUch otl doOnPreDraw ${e.message}")
                }
            }

            val otd = EditText(this)
            otd.apply {
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    120, LinearLayout.LayoutParams.MATCH_PARENT
                )
                minHeight = 40
                setPaddingRelative(1, 1, 1, 1)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_item)
            }
            otd.doOnPreDraw {
                try {
                    if (dop != null)
                        otd.setText(dop.ot_d.toString())
                } catch (e: Exception) {
                    println("$TAG_ERR addUch otd doOnPreDraw ${e.message}")
                }
            }

            val namePrOt = EditText(this)
            namePrOt.apply {
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    150, LinearLayout.LayoutParams.MATCH_PARENT
                )
                minHeight = 40
                setPaddingRelative(1, 1, 1, 1)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                isSingleLine = false
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_item)
            }
            namePrOt.doOnPreDraw {
                try {
                    if (dop != null)
                        namePrOt.setText(dop.name_pr_ot)
                } catch (e: Exception) {
                    println("$TAG_ERR addUch namePrOt doOnPreDraw ${e.message}")
                }
            }

            val uchHgr = EditText(this)
            uchHgr.apply {
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    150, LinearLayout.LayoutParams.MATCH_PARENT
                )
                minHeight = 40
                setPaddingRelative(1, 1, 1, 1)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_item)
            }
            uchHgr.doOnPreDraw {
                try {
                    if (dop != null)
                        uchHgr.setText(dop.uch_hgr)
                } catch (e: Exception) {
                    println("$TAG_ERR addUch uchHgr doOnPreDraw ${e.message}")
                }
            }

            val lvUch = LinearLayout(this)
            lvUch.apply {
                layoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        .apply {
                        minimumHeight = 40
                    }
            }

            lvUch.addView(nomUch)
            lvUch.addView(tipName)
            lvUch.addView(ptd)
            lvUch.addView(ptl)
            lvUch.addView(namePrPt)
            lvUch.addView(otd)
            lvUch.addView(otl)
            lvUch.addView(namePrOt)
            lvUch.addView(uchHgr)

            actTip10LvUch.addView(lvUch)

            arrayUch.add(
                Uch(
                    nom_uch = nomUch, tip_name = tipName, pt_d = ptd, pt_l = ptl,
                    name_pr_pt = namePrPt, ot_d = otd, ot_l = otl, name_pr_ot = namePrOt, uch_hgr = uchHgr
                )
            )

        } catch (e: Exception) {
            println("$TAG_ERR add Uch ${e.message}")
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
                    this@ActPreddogAsuse,
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
                    this@ActPreddogAsuse, "Не удалось создать акт ${_actFields.num_act}.", Toast.LENGTH_LONG
                ).show()
                return false
            }
            if (htmlPath.isEmpty()) {
                Toast.makeText(
                    this@ActPreddogAsuse, "Не удалось получить шаблон ${_actFields.shablon}.", Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActPreddogAsuse,
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
            if (CreatePdf.Create(htmlPath, pdfPath, vals, this, 2)) {
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
                    btnSignActPreddogAsuse.isEnabled = true

                // Открытие pdf
                // ---------------------------------------
                if (!signed) showPdf(this@ActPreddogAsuse, pdfPath)
            } else {
                Toast.makeText(
                    this@ActPreddogAsuse,
                    "Произошла ошибка при создании акта ${_actFields.num_act}.",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActPreddogAsuse,
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
        actTip10Date.isEnabled = false
        actTip10NameTk.isEnabled = false
        actTip10NameAw.isEnabled = false
        actTip10NameObj.isEnabled = false
        actTip10NalSo.isEnabled = false
        actTip10NalSw.isEnabled = false
        actTip10NalGv.isEnabled = false
        actTip10PodpisiFioEso.isEnabled = false

        arrayUch.forEach {
            it.name_pr_ot.isEnabled = false
            it.name_pr_pt.isEnabled = false
            it.nom_uch.isEnabled = false
            it.ot_d.isEnabled = false
            it.ot_l.isEnabled = false
            it.pt_d.isEnabled = false
            it.pt_l.isEnabled = false
            it.tip_name.isEnabled = false
            it.uch_hgr.isEnabled = false
        }

        btnSignActPreddogAsuse.isEnabled = false
        btnCreateActPreddogAsuse.isEnabled = false
        btnActTip10AddGridRow.isEnabled = false
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
                        actTip10Date.text = _actFields.dat_act
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
            dat_act = actTip10Date.text.toString()
            name_tk = actTip10NameTk.text.toString()
            name_aw = actTip10NameAw.text.toString()
            name_obj = actTip10NameObj.text.toString()
            fio_eso = actTip10PodpisiFioEso.text.toString()
            nal_so = actTip10NalSo.text.toString()
            nal_sw = actTip10NalSw.text.toString()
            nal_gv = actTip10NalGv.text.toString()
        }

        val dbWrite = DbHandlerLocalWrite(this, null)
        dbWrite.deleteActFieldsDop(_task.id_task, _act.id_act, npp)

        arrayUch.forEach {
            val dop = ActFieldsDopInfo(
                id_task = _task.id_task,
                id_act = _act.id_act,
                name_pr_ot = it.name_pr_ot.text.toString(),
                name_pr_pt = it.name_pr_pt.text.toString(),
                nom_uch = it.nom_uch.text.toString(),
                ot_d = if (it.ot_d.text.toString().isNotBlank()) it.ot_d.text.toString().toDouble() else 0.0,
                ot_l = if (it.ot_l.text.toString().isNotBlank()) it.ot_l.text.toString().toDouble() else 0.0,
                pt_d = if (it.pt_d.text.toString().isNotBlank()) it.pt_d.text.toString().toDouble() else 0.0,
                pt_l = if (it.pt_l.text.toString().isNotBlank()) it.pt_l.text.toString().toDouble() else 0.0,
                tip_name = it.tip_name.text.toString(),
                uch_hgr = it.uch_hgr.text.toString(),
                npp = npp
            )
            dbWrite.insertActFieldsDop(dop)
        }
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
        vals["%NDOG"] = _actFields.ndog
        vals["%NAME_OBJ"] = _actFields.name_obj
        vals["%NAME_TK"] = _actFields.name_tk
        vals["%NAME_AW"] = _actFields.name_aw
        vals["%NAL_SO"] = _actFields.nal_so
        vals["%NAL_SW"] = _actFields.nal_sw
        vals["%NAL_GV"] = _actFields.nal_gv
        vals["%FIO_ESO"] = _actFields.fio_eso
        vals["%FIO_PODP"] = _task.fio_podp

        vals["%TABLE1"] = "<table border=\"1px\" style=\"border-color: black; width: 100%;\" cellspacing=\"0\">" +
                "<tr>" +
                "    <th>№п/п</th>" +
                "    <th>Тип участка</th>" +
                "    <th>Диаметр под. тр-д</th>" +
                "    <th>Длина под. тр-д</th>" +
                "    <th>Тип прокл. под. тр-д</th>" +
                "    <th>Диаметр обр. тр-д</th>" +
                "    <th>Длина обр. тр-д</th>" +
                "    <th>Тип прокл.обр. тр-д</th>" +
                "    <th>Глубина залегания</th>" +
                "</tr>" +

                arrayUch.joinToString {
                    "<tr><td>${it.nom_uch.text}</td>" +
                            "  <td>${it.tip_name.text}</td>" +
                            "  <td>${it.pt_d.text}</td>" +
                            "  <td>${it.pt_l.text}</td>" +
                            "  <td>${it.name_pr_pt.text}</td>" +
                            "  <td>${it.ot_d.text}</td>" +
                            "  <td>${it.ot_l.text}</td>" +
                            "  <td>${it.name_pr_ot.text}</td>" +
                            "  <td>${it.uch_hgr.text}</td></tr>"
                } +

                "</table>"

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
        if (!btnCreateActPreddogAsuse.isEnabled) {
            setResult(RESULT_OK)
            this@ActPreddogAsuse.finish()
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
                    this@ActPreddogAsuse.finish()
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
                                this@ActPreddogAsuse,
                                "Акт успешно отправлен на email: ${_task.email_podp}.", Toast.LENGTH_LONG
                            ).show()
                        else
                            Toast.makeText(
                                this@ActPreddogAsuse,
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