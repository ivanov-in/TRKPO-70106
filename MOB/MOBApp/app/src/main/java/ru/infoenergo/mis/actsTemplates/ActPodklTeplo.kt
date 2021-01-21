package ru.infoenergo.mis.actsTemplates

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import kotlinx.android.synthetic.main.activity_act_podkl_teplo.*
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
 **    18 id  Акт о подключении к системе теплоснабжения .pdf  6 tip     **
 ** ******************************************************************** **/
class ActPodklTeplo : AppCompatActivity(), DialogInterface.OnCancelListener {
    private var new: Boolean = false
    private var npp: Int = 0

    private lateinit var _actFields: ActFieldsInfo
    private lateinit var _task: Task
    private lateinit var _act: ActInfo
    private lateinit var dlgConfirmActSign: DlgConfirmationActSign


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_act_podkl_teplo)

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
            btnSignActPodklTeplo.isEnabled = true

        try {
            if (supportActionBar != null) {
                val actionBar = supportActionBar
                actionBar!!.title = _act.name
                actionBar.subtitle = _actFields.num_act
                actionBar.elevation = 4.0F
                actionBar.setDisplayHomeAsUpEnabled(true)
            }

            actTip6City.setText(_actFields.city)
            actTip6NumAct.text = _actFields.num_act
            actTip6DatAct.text = if (_actFields.dat_act.isNotEmpty()) _actFields.dat_act
            else LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy г."))

            actTip6DatAct.setOnClickListener {
                changeDate()
            }
            actTip6FilialEso.setText(_actFields.filial_eso)
            actTip6FilialEso.doOnTextChanged { text, _, _, _ -> actTip6FilialEso2.text = text }
            actTip6DirectorTatenergo.setText(_actFields.director_tatenergo)
            actTip6DirectorTatenergo.doOnTextChanged { text, _, _, _ -> actTip6DirectorTatenergo2.text = text }
            actTip6DirectorTDoverNum.setText(_actFields.director_t_dover_num)
            //actTip6DirectorTDoverDat.setText(_actFields.director_t_dover_date)
            actTip6Zayavitel.setText(_actFields.zayavitel)
            actTip6FioPodp.text = _task.fio_podp
            actTip6ZayavitelDover.setText(_actFields.zayavitel_dover)
            actTip6NDog.setText(_actFields.ndog)
            actTip6DatDog.setText(_actFields.dat_dog)
            actTip6Podgotovka.setText(_actFields.podgotovka)
            actTip6QSum.setText(_actFields.q_sum)
            actTip6QMax.setText(_actFields.q_max)
            actTip6MestoKarta.setText(_actFields.mesto_karta)
            actTip6PuData.setText(_actFields.pu_data)
            actTip6PuPoverLico.setText(_actFields.pu_pover_lico)
            actTip6PuPoverRez.setText(_actFields.pu_pover_rez)
            actTip6PuPoverPokaz.setText(_actFields.pu_pover_pokaz)
            actTip6BalansPrinadlObj.setText(_actFields.balans_prinadl_obj)
            actTip6BalansPrinDop.setText(_actFields.balans_prin_dop)
            actTip6GgEksplOtvetst1.setText(_actFields.gr_ekspl_otvetst)
            actTip6GgEksplOtvetstDop.setText(_actFields.gr_ekspl_otvetst_dop)
            actTip6StPodklRub1.setText(_actFields.st_podkl_rub)
            actTip6StPodklRubNds.setText(_actFields.st_podkl_rub_nds)
            actTip6PodklDopSved.setText(_actFields.podkl_dop_sved)
            actTip6FilialEso2.text = _actFields.filial_eso
            actTip6DirectorTatenergo2.text = _actFields.director_tatenergo
            actTip6FioPodp2.text = _task.fio_podp
            actTip6RemarkDog.setText(_actFields.remark_dog)

            // слушатель на кнопку создания акта
            // ---------------------------------------
            btnCreateActPodklTeplo.setOnClickListener {
                createAct()
            }

            // слушатель на кнопку подписания акта
            // -------------------------------------
            btnSignActPodklTeplo.setOnClickListener {
                dlgConfirmActSign = DlgConfirmationActSign(
                    _task.tel_podp,
                    _task.email_podp,
                    _actFields.id_file
                )
                dlgConfirmActSign.show(supportFragmentManager, "SIGN_ACT")
            }

            // Спиннер с фразами
            // ----------------------
            actTip6SpCopyText.apply {
                adapter = ArrayAdapter(context!!, R.layout.spitem_purpose, PhrasesCopied)
                setSelection(0)
            }

            actTip6SpCopyText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val clipboard: ClipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("CopiedText", actTip6SpCopyText.selectedItem.toString())
                    clipboard.setPrimaryClip(clip)
                }
            }
        } catch (e: Exception) {
            println("$TAG_ERR ActPodklTeplo onCreate: ${e.message}")
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
                    this@ActPodklTeplo,
                    "Не удалось получить путь к шаблонам.",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActPodklTeplo,
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

            // Сохранение полей
            // -----------------------
            if (!signed) {
                if (saveDataToLocalDb()) {
                    Toast.makeText(this, "Данные акта сохранены в базу данных.", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(
                        this, "Не удалось сохранить данные акта в базу данных.", Toast.LENGTH_LONG
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
                    btnSignActPodklTeplo.isEnabled = true
                // Открытие pdf
                // ---------------------------------------
                showPdf(this@ActPodklTeplo, pdfPath)
            } else {
                Toast.makeText(
                    this@ActPodklTeplo,
                    "Произошла ошибка при создании акта.",
                    Toast.LENGTH_LONG
                ).show()
            }
            return true
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActPodklTeplo,
                "Произошла ошибка при создании акта.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
    }

    // Установка недоступности редактирования, если акт подписан
    // ---------------------------------------------------------
    private fun setEditableFalse() {
        actTip6City.isEnabled = false
        actTip6NumAct.isEnabled = false
        actTip6DatAct.isEnabled = false
        actTip6FilialEso.isEnabled = false
        actTip6DirectorTatenergo.isEnabled = false
        actTip6DirectorTDoverNum.isEnabled = false
        //actTip6DirectorTDoverDat.isEnabled = false
        actTip6Zayavitel.isEnabled = false
        actTip6FioPodp.isEnabled = false
        actTip6ZayavitelDover.isEnabled = false
        actTip6NDog.isEnabled = false
        actTip6DatDog.isEnabled = false
        actTip6Podgotovka.isEnabled = false
        actTip6QSum.isEnabled = false
        actTip6QMax.isEnabled = false
        actTip6MestoKarta.isEnabled = false
        actTip6PuData.isEnabled = false
        actTip6PuPoverLico.isEnabled = false
        actTip6PuPoverRez.isEnabled = false
        actTip6PuPoverPokaz.isEnabled = false
        actTip6BalansPrinadlObj.isEnabled = false
        actTip6BalansPrinDop.isEnabled = false
        actTip6GgEksplOtvetst1.isEnabled = false
        actTip6GgEksplOtvetstDop.isEnabled = false
        actTip6StPodklRub1.isEnabled = false
        actTip6StPodklRubNds.isEnabled = false
        actTip6PodklDopSved.isEnabled = false
        actTip6FilialEso2.isEnabled = false
        actTip6DirectorTatenergo2.isEnabled = false
        actTip6FioPodp2.isEnabled = false
        actTip6RemarkDog.isEnabled = false

        btnCreateActPodklTeplo.isEnabled = false
        btnSignActPodklTeplo.isEnabled = false
        actTip6SpCopyText.isEnabled = false
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
            city = actTip6City.text.toString()
            num_act = actTip6NumAct.text.toString()
            dat_act = actTip6DatAct.text.toString()
            filial_eso = actTip6FilialEso.text.toString()
            director_tatenergo = actTip6DirectorTatenergo.text.toString()
            director_t_dover_num = actTip6DirectorTDoverNum.text.toString()
            zayavitel = actTip6Zayavitel.text.toString()
            fio_contact = actTip6FioPodp.text.toString()
            zayavitel_dover = actTip6ZayavitelDover.text.toString()
            podgotovka = actTip6Podgotovka.text.toString()
            q_sum = actTip6QSum.text.toString()
            q_max = actTip6QMax.text.toString()
            mesto_karta = actTip6MestoKarta.text.toString()
            pu_data = actTip6PuData.text.toString()
            pu_pover_lico = actTip6PuPoverLico.text.toString()
            pu_pover_rez = actTip6PuPoverRez.text.toString()
            pu_pover_pokaz = actTip6PuPoverPokaz.text.toString()
            balans_prinadl_obj = actTip6BalansPrinadlObj.text.toString()
            balans_prin_dop = actTip6BalansPrinDop.text.toString()
            gr_ekspl_otvetst = actTip6GgEksplOtvetst1.text.toString()
            gr_ekspl_otvetst_dop = actTip6GgEksplOtvetstDop.text.toString()
            st_podkl_rub = actTip6StPodklRub1.text.toString()
            st_podkl_rub_nds = actTip6StPodklRubNds.text.toString()
            podkl_dop_sved = actTip6PodklDopSved.text.toString()
            ndog = actTip6NDog.text.toString()
            dat_dog = actTip6DatDog.text.toString()
            remark_dog = actTip6RemarkDog.text.toString()
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
        val vals: LinkedHashMap<String, String> = LinkedHashMap(27)
        vals["%CITY"] = _actFields.city
        vals["%NUM_ACT"] = _actFields.num_act
        vals["%DAT_ACT"] = _actFields.dat_act
        vals["%FILIAL_ESO"] = _actFields.filial_eso
        vals["%DIRECTOR_TATENERGO"] = _actFields.director_tatenergo
        vals["%DIRECTOR_T_DOVER_NUM"] = _actFields.director_t_dover_num
        vals["%FIO_PODP"] = _task.fio_podp
        vals["%ZAYAVITEL_FIO"] = _actFields.zayavitel
        vals["%ZAYAVITEL_DOVER"] = _actFields.zayavitel_dover
        vals["%NDOG"] = _actFields.ndog
        vals["%DAT_DOG"] = _actFields.dat_dog
        vals["%PODGOTOVKA"] = _actFields.podgotovka
        vals["%Q_SUM"] = _actFields.q_sum
        vals["%Q_MAX"] = _actFields.q_max
        vals["%MESTO_KARTA"] = _actFields.mesto_karta
        vals["%PU_DATA"] = _actFields.pu_data
        vals["%PU_POVER_LICO"] = _actFields.pu_pover_lico
        vals["%PU_POVER_REZ"] = _actFields.pu_pover_rez
        vals["%PU_POVER_POKAZ"] = _actFields.pu_pover_pokaz
        vals["%BALANS_PRINADL_OBJ"] = _actFields.balans_prinadl_obj
        vals["%BALANS_PRIN_DOP"] = _actFields.balans_prin_dop
        vals["%GR_EKSPL_OTVETST1"] = _actFields.gr_ekspl_otvetst
        vals["%GR_EKSPL_OTVETST_DOP"] = _actFields.gr_ekspl_otvetst_dop
        vals["%ST_PODKL_RUB1"] = _actFields.st_podkl_rub
        vals["%ST_PODKL_RUB_NDS"] = _actFields.st_podkl_rub_nds
        vals["%PODKL_DOP_SVED"] = _actFields.podkl_dop_sved

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
        if (!btnCreateActPodklTeplo.isEnabled) {
            setResult(RESULT_OK)
            this@ActPodklTeplo.finish()
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
                    this@ActPodklTeplo.finish()
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
                Toast.makeText(this, "Акт успешно подписан.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Произошла непредвиденная ошибка :(", Toast.LENGTH_LONG).show()
            }

            dbWrite.close()
            setEditableFalse()
        }
    }
}