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
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.doOnPreDraw
import kotlinx.android.synthetic.main.activity_act_dopuska_uu.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import ru.infoenergo.mis.CreatePdf
import ru.infoenergo.mis.DlgConfirmationActSign
import ru.infoenergo.mis.R
import ru.infoenergo.mis.BuildConfig
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
 **   24 ID     Акт допуска узла учёта (первичного осмотра)    12 tip    **
 **   25 ID     Акт повторного допуска узла учёта              12 tip    **
 ** ******************************************************************** **/
class ActDopuskaUu : AppCompatActivity(), DialogInterface.OnCancelListener {

    private lateinit var dlgConfirmActSign: DlgConfirmationActSign
    private lateinit var _actFields: ActFieldsInfo
    private lateinit var _task: Task
    private lateinit var _act: ActInfo
    private lateinit var _actFieldsDop: ArrayList<ActFieldsDopInfo>

    private var arrayPriborUcheta: ArrayList<ActDopuskaUu.PriborUcheta> = ArrayList()

    inner class PriborUcheta(
        var pu_type: EditText,
        var pu_num: EditText,
        var pu_srok_poverki: EditText,
        var pu_num_plomba: EditText,
        var pu_pokaz: EditText
    )

    private var new: Boolean = false
    private var npp: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_act_dopuska_uu)

        _act = intent.getSerializableExtra("ACT") as ActInfo
        _task = intent.getSerializableExtra("TASK") as Task
        _actFields = intent.getSerializableExtra("ACT_FIELDS") as ActFieldsInfo

        _actFieldsDop = ArrayList()

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
            btnSignActDopuskaUu.isEnabled = true

        try {
            // Получаем значения доп полей из лок БД
            _actFieldsDop = if (new) {
                dbRead.getActFieldsDopShablon(_actFields.id_task, _actFields.id_act)
            } else {
                dbRead.getActFieldsDop(_actFields.id_task, _actFields.id_act, _actFields.npp)
            }
            dbRead.close()
            
            if (supportActionBar != null) {
                val actionBar = supportActionBar
                actionBar!!.title = _act.name
                actionBar.subtitle = _actFields.num_act
                actionBar.elevation = 4.0F
                actionBar.setDisplayHomeAsUpEnabled(true)
            }

            if(_act.id_act == 25) {
                actTip12Name.text = "Акт повторного допуска в эксплуатацию узла учёта тепловой энергии у потребителя"
                actTip12Act24.visibility = View.GONE
            }

            actTip12FioEso.setText(_actFields.fio_eso)
            actTip12Date1.text = _actFields.dat_act
            actTip12Date1.setOnClickListener { changeDate() }
            actTip12NumAct.text = _actFields.num_act
            actTip12Date.text = _actFields.dat_act
            actTip12Date.setOnClickListener { changeDate() }
            actTip12PayerName.setText(_actFields.payer_name)
            actTip12Ndog.setText(_actFields.ndog)
            actTip12NameObj.setText(_actFields.name_obj)
            actTip12AdrObj.setText(_actFields.adr_obj)
            actTip12NalSo.isChecked = _actFields.nal_so.isNotBlank() && _actFields.nal_so.isNotEmpty()
            actTip12NalSw.isChecked = _actFields.nal_sw.isNotBlank() && _actFields.nal_sw.isNotEmpty()
            actTip12NalSt.isChecked = _actFields.nal_st.isNotBlank() && _actFields.nal_st.isNotEmpty()
            actTip12NalGv.isChecked = _actFields.nal_gv.isNotBlank() && _actFields.nal_gv.isNotEmpty()
            actTip12ItogText.setText(_actFields.itog_text)
            actTip12PuData.setText(_actFields.pu_data)
            actTip12DopuskS.setText(_actFields.dopusk_s)
            actTip12DopuskDo.setText(_actFields.dopusk_po)

            if (_actFieldsDop.size > 0) {
                _actFieldsDop.forEach {
                    addPribUcheta(it)
                }
            }

            actTip12FilialAddress.setText(_actFields.filial_address)
            actTip12TelSpravki.setText(_actFields.tel_spravki)
            actTip12DopInfo.setText(_actFields.dop_info)
            actTip12TelDispetch.setText(_actFields.tel_dispetch)
            actTip12FioEso1.setText(_actFields.fio_eso)
            actTip12Podp.text = "${_task.name_dolzhn_podp}, ${_task.fio_podp} (т. ${_task.tel_podp})"
            actTip12Podp2.text = "${_task.fio_podp} ${_actFields.dat_act}"
            actTip12OrgUstanovPu.setText(_actFields.org_ustanov_pu)
            actTip12Podp1.text = "${_task.name_dolzhn_podp}, ${_task.fio_podp} (т. ${_task.tel_podp})"
            actTip12RemarkDog.setText(_actFields.remark_dog)

            btnAddPu.setOnClickListener {
                if (arrayPriborUcheta.size > 30) {
                    return@setOnClickListener
                } else addPribUcheta()
            }

            // слушатель на кнопку создания акта
            // ---------------------------------------
            btnCreateActDopuskaUu.setOnClickListener {
                createAct()
            }

            // слушатель на кнопку подписания акта
            // -------------------------------------
            btnSignActDopuskaUu.setOnClickListener {
                dlgConfirmActSign = DlgConfirmationActSign(
                    _task.tel_podp,
                    _task.email_podp,
                    _actFields.id_file
                )
                dlgConfirmActSign.show(supportFragmentManager, "SIGN_ACT")
            }

            // Спиннер с фразами
            // ----------------------
            actTip12SpCopyText.apply {
                adapter = ArrayAdapter(context!!, R.layout.spitem_purpose, PhrasesCopied)
                setSelection(0)
            }

            actTip12SpCopyText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val clipboard: ClipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("CopiedText", actTip12SpCopyText.selectedItem.toString())
                    clipboard.setPrimaryClip(clip)
                }
            }

        } catch (e: Exception) {
            println("$TAG_ERR ActDopuskaUu: ${e.message}")
        }
    }

    // Добавление в разметку новых строк ПУ
    // ------------------------------------
    private fun addPribUcheta(dop: ActFieldsDopInfo? = null) {
        try {
            val puType = EditText(this, null, android.R.attr.editTextStyle, R.style.GvItemsThemeActs)
            puType.apply {
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                )
                setPaddingRelative(2, 2, 2, 2)
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                setBackgroundResource(R.drawable.frame_grid_item)
            }
            puType.doOnPreDraw {
                if (dop != null)
                    puType.setText(dop.pu_type)
            }

            val puNum = EditText(this, null, android.R.attr.editTextStyle, R.style.GvItemsThemeActs)
            puNum.apply {
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                )
                setPaddingRelative(2, 2, 2, 2)
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                setBackgroundResource(R.drawable.frame_grid_item)
            }
            puNum.doOnPreDraw {
                if (dop != null)
                    puNum.setText(dop.pu_num)
            }


            val puPover = EditText(this, null, android.R.attr.editTextStyle, R.style.GvItemsThemeActs)
            puPover.apply {
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                )
                setPaddingRelative(2, 2, 2, 2)
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                setBackgroundResource(R.drawable.frame_grid_item)
            }
            puPover.doOnPreDraw {
                if (dop != null)
                    puPover.setText(dop.pu_srok_poverki)
            }

            val puPlomba = EditText(this, null, android.R.attr.editTextStyle, R.style.GvItemsThemeActs)
            puPlomba.apply {
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                )
                setPaddingRelative(2, 2, 2, 2)
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                setBackgroundResource(R.drawable.frame_grid_item)
            }
            puPlomba.doOnPreDraw {
                if (dop != null)
                    puPlomba.setText(dop.pu_num_plomba)
            }

            val puPokaz = EditText(this, null, android.R.attr.editTextStyle, R.style.GvItemsThemeActs)
            puPokaz.apply {
                textSize = 18f
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                )
                setPaddingRelative(2, 2, 2, 2)
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                setBackgroundResource(R.drawable.frame_grid_item)
            }
            puPokaz.doOnPreDraw {
                if (dop != null)
                    puPokaz.setText(dop.pu_pokaz)
            }


            val lvkip = LinearLayout(this)
            lvkip.apply {
                layoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        .apply {
                            minimumHeight = 40
                        }
            }

            lvkip.addView(puType)
            lvkip.addView(puNum)
            lvkip.addView(puPover)
            lvkip.addView(puPlomba)
            lvkip.addView(puPokaz)

            actTip12Table.addView(lvkip)

            arrayPriborUcheta.add(PriborUcheta(puType, puNum, puPover, puPlomba, puPokaz))
        } catch (e: Exception) {
            println("$TAG_ERR add pu ${e.message}")
        }
    }

    // На кнопку Создать акт
    // ----------------------
    // На кнопку Создать акт
    // ---------------------------------------------
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
            pdfPath = "${pdfsFolder.path}/${_actFields.num_act.replace("\\", "-")}_${_actFields.shablon.replace(" ", "_")}"

            if (pdfPath.isEmpty() || htmlPath.isEmpty()) {
                Toast.makeText(
                    this@ActDopuskaUu,
                    "Не удалось получить путь к шаблонам.",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActDopuskaUu,
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
                    btnSignActDopuskaUu.isEnabled = true

                // Открытие pdf
                // ---------------------------------------
                if (!signed) showPdf(this@ActDopuskaUu, pdfPath)
            } else {
                Toast.makeText(
                    this@ActDopuskaUu,
                    "Произошла ошибка при создании акта ${_actFields.num_act}.",
                    Toast.LENGTH_LONG
                ).show()
            }
            return true
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActDopuskaUu,
                "Произошла ошибка при создании акта ${_actFields.num_act}.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
    }

    // Установка недоступности редактирования, если акт подписан
    // ---------------------------------------------------------
    private fun setEditableFalse() {
        actTip12FioEso.isEnabled = false
        actTip12PayerName.isEnabled = false
        actTip12Ndog.isEnabled = false
        actTip12NameObj.isEnabled = false
        actTip12AdrObj.isEnabled = false
        actTip12NalSo.isEnabled = false
        actTip12NalSw.isEnabled = false
        actTip12NalSt.isEnabled = false
        actTip12NalGv.isEnabled = false
        actTip12ItogText.isEnabled = false
        actTip12PuData.isEnabled = false
        actTip12DopuskS.isEnabled = false
        actTip12DopuskDo.isEnabled = false
        actTip12FilialAddress.isEnabled = false
        actTip12TelSpravki.isEnabled = false
        actTip12DopInfo.isEnabled = false
        actTip12TelDispetch.isEnabled = false
        actTip12FioEso1.isEnabled = false
        actTip12OrgUstanovPu.isEnabled = false
        actTip12RemarkDog.isEnabled = false

        arrayPriborUcheta.forEach {
            it.pu_num.isEnabled = false
            it.pu_type.isEnabled = false
            it.pu_num_plomba.isEnabled = false
            it.pu_pokaz.isEnabled = false
            it.pu_srok_poverki.isEnabled = false
        }

        btnAddPu.isEnabled = false
        btnCreateActDopuskaUu.isEnabled = false
        btnSignActDopuskaUu.isEnabled = false
        actTip12SpCopyText.isEnabled = false
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
                        actTip12Date.text = _actFields.dat_act
                        actTip12Date1.text = _actFields.dat_act
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
            fio_eso = actTip12FioEso.text.toString()
            dat_act = actTip12Date1.text.toString()
            num_act = actTip12NumAct.text.toString()
            dat_act = actTip12Date.text.toString()
            payer_name = actTip12PayerName.text.toString()
            ndog = actTip12Ndog.text.toString()
            name_obj = actTip12NameObj.text.toString()
            adr_obj = actTip12AdrObj.text.toString()
            nal_so = if (actTip12NalSo.isChecked) actTip12NalSo.text.toString().toUpperCase() else ""
            nal_sw = if (actTip12NalSw.isChecked) actTip12NalSw.text.toString().toUpperCase() else ""
            nal_st = if (actTip12NalSt.isChecked) actTip12NalSt.text.toString().toUpperCase() else ""
            nal_gv = if (actTip12NalGv.isChecked) actTip12NalGv.text.toString().toUpperCase() else ""
            itog_text = actTip12ItogText.text.toString()
            pu_data = actTip12PuData.text.toString()
            dopusk_s = actTip12DopuskS.text.toString()
            dopusk_po = actTip12DopuskDo.text.toString()
            filial_address = actTip12FilialAddress.text.toString()
            tel_spravki = actTip12TelSpravki.text.toString()
            dop_info = actTip12DopInfo.text.toString()
            tel_dispetch = actTip12TelDispetch.text.toString()
            org_ustanov_pu = actTip12OrgUstanovPu.text.toString()
            remark_dog = actTip12RemarkDog.text.toString()
        }

        val dbWrite = DbHandlerLocalWrite(this, null)
        dbWrite.deleteActFieldsDop(_task.id_task, _act.id_act, npp)

        arrayPriborUcheta.forEach {
            val dop = ActFieldsDopInfo(
                id_task = _task.id_task,
                id_act = _act.id_act,
                pu_num = it.pu_num.text.toString(),
                pu_type = it.pu_type.text.toString(),
                pu_srok_poverki = it.pu_srok_poverki.text.toString(),
                pu_num_plomba = it.pu_num_plomba.text.toString(),
                pu_pokaz = it.pu_pokaz.text.toString(),
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
    private fun getHashMapVals(signed: Boolean): java.util.LinkedHashMap<String, String> {
        val vals: LinkedHashMap<String, String> = LinkedHashMap(30)
        vals["%NUM_ACT"] = _actFields.num_act
        vals["%FILIAL_ESO"] = _actFields.filial_eso
        vals["%NUM_ACT"] = _actFields.num_act
        vals["%DAT_ACT"] = _actFields.dat_act
        vals["%PAYER_NAME"] = _actFields.payer_name
        vals["%NDOG"] = _actFields.ndog
        vals["%NAME_OBJ"] = _actFields.name_obj
        vals["%ADR_OBJ"] = _actFields.adr_obj
        vals["%NAL_SO"] = if (actTip12NalSo.isChecked) actTip12NalSo.text.toString() else ""
        vals["%NAL_SW"] = if (actTip12NalSw.isChecked) actTip12NalSw.text.toString() else ""
        vals["%NAL_ST"] = if (actTip12NalSt.isChecked) actTip12NalSt.text.toString() else ""
        vals["%NAL_GW"] = if (actTip12NalGv.isChecked) actTip12NalGv.text.toString() else ""
        vals["%ITOG_TEXT"] = _actFields.itog_text
        vals["%PU_DATA"] = _actFields.pu_data
        vals["%DOPUSK_S"] = _actFields.dopusk_s
        vals["%DOPUSK_PO"] = _actFields.dopusk_po
        vals["%FILIAL_ADDRESS"] = _actFields.filial_address
        vals["%TEL_SPRAVKI"] = _actFields.tel_spravki
        vals["%DOP_INFO"] = _actFields.dop_info
        vals["%TEL_DISPETCH"] = _actFields.tel_dispetch
        vals["%FIO_ESO"] = _actFields.fio_eso
        vals["%NAME_DOLZHN_ESO"] = _actFields.name_dolzhn_eso
        vals["%TEL_ESO"] = _actFields.tel_eso
        vals["%FIO_PODP"] = _task.fio_podp
        vals["%TEL_PODP"] = _task.tel_podp
        vals["%NAME_DOLZHN_PODP"] = _task.name_dolzhn_podp
        if (_actFields.id_act == 24)
            vals["%ORG_USTANOV_PU"] = _actFields.org_ustanov_pu

        var table = ""

        if (arrayPriborUcheta.size > 0 ) {
            table = "<table border=\"1px\" style=\"border-color: black; width: 100%;\" cellspacing=\"0\">"
            table += arrayPriborUcheta.joinToString {
                "<tr><td width=\"20%\">${it.pu_type.text}</td><td width=\"20%\">${it.pu_num.text}</td><td width=\"20%\">${it.pu_srok_poverki.text}</td><td width=\"20%\">${it.pu_num_plomba.text}</td><td width=\"20%\">${it.pu_pokaz.text}</td></tr>"
            }
            table += "\\n</table>"
        }

        vals["%TABLE1"] = table

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
        if (!btnCreateActDopuskaUu.isEnabled) {
            setResult(RESULT_OK)
            this@ActDopuskaUu.finish()
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
                    this@ActDopuskaUu.finish()
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