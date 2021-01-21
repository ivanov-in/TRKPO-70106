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
import kotlinx.android.synthetic.main.activity_act_bezdog_potr.*
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
 **           23 id     Акт бездоговорного потребления     11 tip     **
 ** ***************************************************************** **/
class ActBezdogPotr : AppCompatActivity(), DialogInterface.OnCancelListener {
    private var new: Boolean = false
    private var npp: Int = 0

    private lateinit var _actFields: ActFieldsInfo
    private lateinit var _task: Task
    private lateinit var _act: ActInfo
    private lateinit var dlgConfirmActSign: DlgConfirmationActSign

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_act_bezdog_potr)

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
            btnSignActBezdogPotr.isEnabled = true

        try {
            if (supportActionBar != null) {
                val actionBar = supportActionBar
                actionBar!!.title = _act.name
                actionBar.subtitle = _actFields.num_act
                actionBar.elevation = 4.0F
                actionBar.setDisplayHomeAsUpEnabled(true)
            }

            actTip11City.setText(_actFields.city)
            actTip11DatAct.text = _actFields.dat_act
            actTip11DatAct.setOnClickListener { changeDate() }
            actTip11NumAct.text = _actFields.num_act
            actTip11FilialEso.setText(_actFields.filial_eso)
            actTip11FioEso.setText(_actFields.fio_eso)
            actTip11NameDolzhnEso.setText(_actFields.name_dolzhn_eso)
            actTip11TelEso.setText(_actFields.tel_eso)
            actTip11PayerName.setText(_actFields.payer_name)
            actTip11Inn.setText(_actFields.inn_org)
            actTip11Kpp.setText(_actFields.kpp_org)
            actTip11PravoSobstv.setText(_actFields.pravo_sobstv)
            actTip11FioPodp.text = _task.fio_podp
            actTip11TelPodp.text = _task.tel_podp
            actTip11NameDolzhnPodp.text = _task.name_dolzhn_podp
            actTip11UvedomAktirovNum.setText(_actFields.uvedom_aktirov_num)
            actTip11UvedomAktirovDate.setText(_actFields.uvedom_aktirov_date)
            actTip11AdrOrg.setText(_actFields.adr_org)
            actTip11PredstPotrebitDover.setText(_actFields.predst_potrebit_dover)
            actTip11AdrObj.setText(_actFields.adr_obj)
            actTip11QSum.setText(_actFields.q_sum)
            actTip11SoQ.setText(_actFields.so_q)
            actTip11SwQ.setText(_actFields.sw_q)
            actTip11GwQ.setText(_actFields.gw_q)
            actTip11StQ.setText(_actFields.st_q)
            actTip11NaznName.setText(_actFields.nazn_name)
            actTip11VolumeObj.setText(_actFields.volume_obj)
            actTip11SquareObj.setText(_actFields.square_obj)
            actTip11NalPu.setText(_actFields.nal_pu)
            actTip11NalAupr.setText(_actFields.nal_aupr)
            actTip11DopInfo.setText(_actFields.dop_info)
            actTip11RemarkDog.setText(_actFields.remark_dog)
            actTip11BezdogUstanovleno.setText(
                if(_actFields.bezdog_ustanovleno.isEmpty() || _actFields.bezdog_ustanovleno.isBlank())
                    "Включенные нагрузки: " else _actFields.bezdog_ustanovleno)
            actTip11BezdogNarushenie.setText(_actFields.bezdog_narushenie)
            actTip11BezdogPereraschetS.setText(_actFields.bezdog_pereraschet_s)
            actTip11BezdogPereraschetPo.setText(_actFields.bezdog_pereraschet_po)
            actTip11BezdogPredpis.setText(_actFields.bezdog_predpis)

            actTip11BezdogObyasn.setText(_actFields.bezdog_obyasn)
            actTip11BezdogPretenz.setText(_actFields.bezdog_pretenz)

            actTip11OtkazSvidet1.setText(_actFields.otkaz_svidet_1)
            actTip11OtkazSvidet2.setText(_actFields.otkaz_svidet_2)

            actTip11Eso.text = "${_actFields.fio_eso}, ${_actFields.name_dolzhn_eso} (${_actFields.tel_eso})"
            actTip11Contact.text = "${_task.fio_podp}, ${_task.name_dolzhn_podp} (${_task.tel_podp})"

            actTip11FioEso.doOnTextChanged { text, _, _, _ ->
                actTip11Eso.text = "$text, ${actTip11NameDolzhnEso.text} (${actTip11TelEso.text})"
            }
            actTip11NameDolzhnEso.doOnTextChanged { text, _, _, _ ->
                actTip11Eso.text = "${actTip11FioEso.text}, $text (${actTip11TelEso.text})"
            }
            actTip11TelEso.doOnTextChanged { text, _, _, _ ->
                actTip11Eso.text = "${actTip11FioEso.text}, ${actTip11NameDolzhnEso.text} ($text)"
            }

            actTip11SpinnerRemarkDog.adapter = ArrayAdapter(
                this,
                R.layout.spitem_purpose,
                arrayOf(
                    "",
                    "Без заключения в установленном порядке договора теплоснабжения;",
                    "С использованием тепло-потребляющих установок, подключенных к системе теплоснабжения с нарушением установленного порядка подключения;",
                    "После введения ограничения подачи тепловой энергии в объеме, превышающем допустимый объем потребления;",
                    "После предъявления требования теплоснабжающей организации или теплосетевой организации о введении ограничения подачи тепловой энергии или прекращении потребления тепловой энергии"
                )
            )
            actTip11SpinnerRemarkDog.onItemSelectedListener = object : AdapterView.OnItemSelectedListener  {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    actTip11DopInfo.setText("${actTip11DopInfo.text} ${actTip11SpinnerRemarkDog.selectedItem}")
                }
            }

            actTip11SpinnerBezdogUstanovleno.adapter = ArrayAdapter(
                this,
                R.layout.spitem_purpose,
                arrayOf("", "ГВС;", "СО;", "Вент;", "Техн;")
            )
            actTip11SpinnerBezdogUstanovleno.onItemSelectedListener = object : AdapterView.OnItemSelectedListener  {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    actTip11BezdogUstanovleno.setText("${actTip11BezdogUstanovleno.text} ${actTip11SpinnerBezdogUstanovleno.selectedItem}")
                }
            }


        } catch (e: Exception) {
            println("$TAG_ERR ActBezdogPotr: ${e.message}")
        }
        try {
            // слушатель на кнопку создания акта
            // ---------------------------------------
            btnCreateActBezdogPotr.setOnClickListener {
                createAct()
            }

            // слушатель на кнопку подписания акта
            // -------------------------------------
            btnSignActBezdogPotr.setOnClickListener {
                dlgConfirmActSign = DlgConfirmationActSign(
                    _task.tel_podp,
                    _task.email_podp,
                    _actFields.id_file
                )
                dlgConfirmActSign.show(supportFragmentManager, "SIGN_ACT")
            }

            // Спиннер с фразами
            // ----------------------
            actTip11SpCopyText.apply {
                adapter = ArrayAdapter(context!!, R.layout.spitem_purpose, PhrasesCopied)
                setSelection(0)
            }

            actTip11SpCopyText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val clipboard: ClipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("CopiedText", actTip11SpCopyText.selectedItem.toString())
                    clipboard.setPrimaryClip(clip)
                }
            }
        } catch (e: Exception) {
            println("$TAG_ERR ActBezdogPotr: ${e.message}")
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
            pdfPath = "${pdfsFolder.path}/${_actFields.num_act.replace("\\", "-")}_${_actFields.shablon.replace(" ", "_")}"

            if (pdfPath.isEmpty() || htmlPath.isEmpty()) {
                Toast.makeText(
                    this@ActBezdogPotr,
                    "Не удалось получить путь к шаблону акта ${_actFields.shablon}.",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActBezdogPotr,
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
                    btnSignActBezdogPotr.isEnabled = true

                // Открытие pdf
                // ---------------------------------------
                if (!signed) showPdf(this@ActBezdogPotr, pdfPath)
            } else {
                Toast.makeText(
                    this@ActBezdogPotr,
                    "Произошла ошибка при создании акта ${_actFields.num_act}.",
                    Toast.LENGTH_LONG
                ).show()
            }
            return true
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActBezdogPotr,
                "Произошла ошибка при создании акта ${_actFields.num_act}.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
    }

    // Установка недоступности редактирования, если акт подписан
    // ---------------------------------------------------------
    private fun setEditableFalse() {

        actTip11City.isEnabled = false
        actTip11DatAct.isEnabled = false
        actTip11PayerName.isEnabled = false
        actTip11NumAct.isEnabled = false
        actTip11FilialEso.isEnabled = false
        actTip11FioEso.isEnabled = false
        actTip11NameDolzhnEso.isEnabled = false
        actTip11TelEso.isEnabled = false
        actTip11Inn.isEnabled = false
        actTip11Kpp.isEnabled = false
        actTip11PravoSobstv.isEnabled = false
        actTip11UvedomAktirovNum.isEnabled = false
        actTip11UvedomAktirovDate.isEnabled = false
        actTip11AdrOrg.isEnabled = false
        actTip11QSum.isEnabled = false
        actTip11SoQ.isEnabled = false
        actTip11SwQ.isEnabled = false
        actTip11GwQ.isEnabled = false
        actTip11StQ.isEnabled = false
        actTip11NaznName.isEnabled = false
        actTip11VolumeObj.isEnabled = false
        actTip11SquareObj.isEnabled = false
        actTip11NalPu.isEnabled = false
        actTip11NalAupr.isEnabled = false
        actTip11DopInfo.isEnabled = false
        actTip11BezdogUstanovleno.isEnabled = false
        actTip11BezdogNarushenie.isEnabled = false
        actTip11BezdogPereraschetS.isEnabled = false
        actTip11BezdogPereraschetPo.isEnabled = false
        actTip11BezdogPredpis.isEnabled = false
        actTip11RemarkDog.isEnabled = false

        btnCreateActBezdogPotr.isEnabled = false
        btnSignActBezdogPotr.isEnabled = false
        actTip11SpCopyText.isEnabled = false
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
                        actTip11DatAct.text = _actFields.dat_act
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
            city = actTip11City.text.toString()
            id_act = _act.id_act
            id_task = _task.id_task
            dat_act = actTip11DatAct.text.toString()
            num_act = actTip11NumAct.text.toString()
            filial_eso = actTip11FilialEso.text.toString()
            fio_eso = actTip11FioEso.text.toString()
            name_dolzhn_eso = actTip11NameDolzhnEso.text.toString()
            tel_eso = actTip11TelEso.text.toString()
            payer_name = actTip11PayerName.text.toString()
            inn_org = actTip11Inn.text.toString()
            kpp_org = actTip11Kpp.text.toString()
            pravo_sobstv = actTip11PravoSobstv.text.toString()
            uvedom_aktirov_num = actTip11UvedomAktirovNum.text.toString()
            uvedom_aktirov_date = actTip11UvedomAktirovDate.text.toString()
            adr_org = actTip11AdrOrg.text.toString()
            predst_potrebit_dover = actTip11PredstPotrebitDover.text.toString()
            adr_obj = actTip11AdrObj.text.toString()
            q_sum = actTip11QSum.text.toString()
            so_q = actTip11SoQ.text.toString()
            sw_q = actTip11SwQ.text.toString()
            gw_q = actTip11GwQ.text.toString()
            st_q = actTip11StQ.text.toString()
            nazn_name = actTip11NaznName.text.toString()
            volume_obj = actTip11VolumeObj.text.toString()
            square_obj = actTip11SquareObj.text.toString()
            nal_pu = actTip11NalPu.text.toString()
            nal_aupr = actTip11NalAupr.text.toString()
            dop_info = actTip11DopInfo.text.toString()
            bezdog_ustanovleno = actTip11BezdogUstanovleno.text.toString()
            bezdog_narushenie = actTip11BezdogNarushenie.text.toString()
            bezdog_pereraschet_s = actTip11BezdogPereraschetS.text.toString()
            bezdog_pereraschet_po = actTip11BezdogPereraschetPo.text.toString()
            bezdog_predpis = actTip11BezdogPredpis.text.toString()

            bezdog_obyasn = actTip11BezdogObyasn.text.toString()
            bezdog_pretenz = actTip11BezdogPretenz.text.toString()

            otkaz_svidet_1 = actTip11OtkazSvidet1.text.toString()
            otkaz_svidet_2 = actTip11OtkazSvidet2.text.toString()
            //---
            fio_contact = actTip11FioPodp.text.toString()
            name_dolzhn_contact = actTip11NameDolzhnPodp.text.toString()
            tel_contact = actTip11TelPodp.text.toString()
            remark_dog = actTip11RemarkDog.text.toString()
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
        val vals: LinkedHashMap<String, String> = LinkedHashMap(37)
        vals["%CITY"] = _actFields.city
        vals["%DAT_ACT"] = _actFields.dat_act
        vals["%NUM_ACT"] = _actFields.num_act
        vals["%FILIAL_ESO"] = _actFields.filial_eso
        vals["%FIO_ESO"] = _actFields.fio_eso
        vals["%NAME_DOLZHN_ESO"] = _actFields.name_dolzhn_eso
        vals["%TEL_ESO"] = _actFields.tel_eso
        vals["%PAYER_NAME"] = _actFields.payer_name
        vals["%INN_ORG"] = _actFields.inn_org
        vals["%KPP_ORG"] = _actFields.kpp_org
        vals["%PRAVO_SOBSTV"] = _actFields.pravo_sobstv
        vals["%UVEDOM_AKTIROV_NUM"] = _actFields.uvedom_aktirov_num
        vals["%UVEDOM_AKTIROV_DATE"] = _actFields.uvedom_aktirov_date
        vals["%ADR_ORG"] = _actFields.adr_org
        vals["%FIO_PODP"] = _task.fio_podp
        vals["%NAME_DOLZHN_PODP"] = _task.name_dolzhn_podp
        vals["%TEL_PODP"] = _task.tel_podp
        vals["%PREDST_POTREBIT_DOVER"] = _actFields.predst_potrebit_dover
        vals["%ADR_OBJ"] = _actFields.adr_obj
        vals["%Q_SUM"] = _actFields.q_sum
        vals["%SO_Q"] = _actFields.so_q
        vals["%SW_Q"] = _actFields.sw_q
        vals["%GW_Q"] = _actFields.gw_q
        vals["%ST_Q"] = _actFields.st_q
        vals["%NAZN_NAME"] = _actFields.nazn_name
        vals["%VOLUME_OBJ"] = _actFields.volume_obj
        vals["%SQUARE_OBJ"] = _actFields.square_obj
        vals["%NAL_PU"] = _actFields.nal_pu
        vals["%NAL_AUPR"] = _actFields.nal_aupr
        vals["%DOP_INFO"] = _actFields.dop_info
        vals["%BEZDOG_USTANOVLENO"] = _actFields.bezdog_ustanovleno
        vals["%BEZDOG_NARUSHENIE"] = _actFields.bezdog_narushenie
        vals["%BEZDOG_PERERASCHET_S"] = _actFields.bezdog_pereraschet_s
        vals["%BEZDOG_PERERASCHET_PO"] = _actFields.bezdog_pereraschet_po
        vals["%BEZDOG_PREDPIS"] = _actFields.bezdog_predpis
        vals["%BEZDOG_OBYASN"] = _actFields.bezdog_obyasn
        vals["%BEZDOG_PRETENZ"] = _actFields.bezdog_pretenz
        vals["%OTKAZ_SVIDET_1"] = _actFields.otkaz_svidet_1
        vals["%OTKAZ_SVIDET_2"] = _actFields.otkaz_svidet_2

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
        if (!btnCreateActBezdogPotr.isEnabled) {
            setResult(RESULT_OK)
            this@ActBezdogPotr.finish()
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
                    this@ActBezdogPotr.finish()
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