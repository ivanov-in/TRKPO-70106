package ru.infoenergo.mis.actsTemplates

import android.app.DatePickerDialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.MenuItem
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.doOnPreDraw
import androidx.core.widget.doOnTextChanged
import kotlinx.android.synthetic.main.activity_act_vnutridomovyh_setey.*
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
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter

/** ************************************************************************* **
 **  17 id Акт о результатах проведения гидропневматической промывки  5 tip   **
 ** ************************************************************************* **/
class ActVnutridomovyhSetey : AppCompatActivity(), DialogInterface.OnCancelListener {

    private lateinit var dlgConfirmActSign: DlgConfirmationActSign

    private lateinit var _task: Task
    private lateinit var _act: ActInfo
    private lateinit var _actFields: ActFieldsInfo
    private lateinit var _actFieldsDop: ArrayList<ActFieldsDopInfo>

    private var arrayKip: ArrayList<ActVnutridomovyhSetey.Kip> = ArrayList()

    inner class Kip(
        var pu_num: EditText,
        var pu_name: EditText,
        var pu_mesto: EditText,
        var pu_type: EditText,
        var pu_diam: EditText,
        var pu_kolvo: EditText
    )

    private var new: Boolean = false
    private var npp: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_act_vnutridomovyh_setey)

        _act = intent.getSerializableExtra("ACT") as ActInfo
        _task = intent.getSerializableExtra("TASK") as Task
        _actFields = intent.getSerializableExtra("ACT_FIELDS") as ActFieldsInfo

        _actFieldsDop = ArrayList()
        arrayKip = ArrayList()

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
            dbRead.close()
        }
        npp = _actFields.npp

        if (!new && _task.kod_emp_podp != 0)
            btnSignActVnutridomovyhSetey.isEnabled = true

        try {
            // Получаем значения доп полей из лок БД
            _actFieldsDop = if (new)
               dbRead.getActFieldsDopShablon(_actFields.id_task, _actFields.id_act)
            else
                dbRead.getActFieldsDop(_actFields.id_task, _actFields.id_act, _actFields.npp)
            dbRead.close()

            if (supportActionBar != null) {
                val actionBar = supportActionBar
                actionBar!!.title = _act.name
                actionBar.subtitle = _actFields.num_act
                actionBar.elevation = 4.0F
                actionBar.setDisplayHomeAsUpEnabled(true)
            }

            // Заполнение вьюх
            //------------------------
            actTip5City.setText(_actFields.city)
            actTip5NumAct.text = _actFields.num_act
            actTip5DatAct.setOnClickListener { changeDate() }
            actTip5DatAct.text = if (_actFields.dat_act.isNotEmpty()) _actFields.dat_act else LocalDate.now().format(
                DateTimeFormatter.ofPattern("dd MMMM yyyy г.")
            )

            actTip5FilialEso.setText(_actFields.filial_eso)
            actTip5FilialEso.doOnTextChanged { text, _, _, _ -> actTip5FilialEso1.text = text }
            actTip5DirectorTatenergo.setText(_actFields.director_tatenergo)
            actTip5DirectorTatenergo.doOnTextChanged { text, _, _, _ -> actTip5DirectorTatenergo1.text = text }
            actTip5DirectorTDoverNum.setText(_actFields.director_t_dover_num)
            //actTip5DirectorTDoverDat.setText(_actFields.director_t_dover_date)
            actTip5Zayavitel.setText(_actFields.zayavitel)
            actTip5FioPodp.text = _task.fio_podp
            actTip5ZayavitelDover.setText(_actFields.zayavitel_dover)

            actTip5NameObj.setText(_actFields.name_obj)
            actTip5AdrObj.setText(_actFields.adr_obj)
            actTip5NDog.text = _actFields.ndog
            actTip5DatDog.text = _actFields.dat_dog
            actTip5Podgotovka.setText(_actFields.podgotovka)
            actTip5PodgotovkaProjNum.setText(_actFields.podgotovka_proj_num)
            actTip5PodgotovkaProjIspoln.setText(_actFields.podgotovka_proj_ispoln)
            actTip5PodgotovkaProjUtvergden.setText(_actFields.podgotovka_proj_utvergden)

            actTip5NetInnerTeplonositel.setText(_actFields.net_inner_teplonositel)
            actTip5NetInnerDp.setText(_actFields.net_inner_dp)
            actTip5NetInnerDo.setText(_actFields.net_inner_do)
            actTip5NetInnerTipKanal.setText(_actFields.net_inner_tip_kanal)
            actTip5NetInnerTubeTypeP.setText(_actFields.net_inner_tube_type_p)
            actTip5NetInnerTubeTypeO.setText(_actFields.net_inner_tube_type_o)
            actTip5NetInnerL.setText(_actFields.net_inner_l)
            actTip5NetInnerLUndeground.setText(_actFields.net_inner_l_undeground)
            actTip5NetInnerOtstuplenie.setText(_actFields.net_inner_otstuplenie)
            actTip5EnergoEffectObject.setText(_actFields.energo_effect_object)
            actTip5NalRezervIstochnik.setText(_actFields.nal_rezerv_istochnik)
            actTip5NalSvyazi.setText(_actFields.nal_svyazi)

            actTip5VidConnectSystem.setText(_actFields.vid_connect_system)
            actTip5ElevatorNum.setText(_actFields.elevator_num)
            actTip5ElevatorDiam.setText(_actFields.elevator_diam)
            actTip5PodogrevOtopNum.setText(_actFields.podogrev_otop_num)
            actTip5PodogrevOtopKolvoSekc.setText(_actFields.podogrev_otop_kolvo_sekc)
            actTip5PodogrevOtopLSekc.setText(_actFields.podogrev_otop_l_sekc)
            actTip5PodogrevOtopNazn.setText(_actFields.podogrev_otop_nazn)
            actTip5PodogrevOtopMarka.setText(_actFields.podogrev_otop_marka)
            actTip5DNaporPatrubok.setText(_actFields.d_napor_patrubok)
            actTip5PowerElectroEngine.setText(_actFields.power_electro_engine)
            actTip5ChastotaVrEngine.setText(_actFields.chastota_vr_engine)
            actTip5DrosselDiafragmaD.setText(_actFields.drossel_diafragma_d)
            actTip5DrosselDiafragmaMesto.setText(_actFields.drossel_diafragma_mesto)
            actTip5DrosselDiafragmaTipOtop.setText(_actFields.drossel_diafragma_tip_otop)
            actTip5DrosselDiafragmaCntStoyak.setText(_actFields.drossel_diafragma_cnt_stoyak)
            actTip5TypeOtoPrib.setText(_actFields.type_oto_prib)
            actTip5SchemaVklGvs.setText(_actFields.schema_vkl_gvs)
            actTip5SchemaVklPodogrev.setText(_actFields.schema_vkl_podogrev)
            actTip5KolvoSekc1.setText(_actFields.kolvo_sekc_1)
            actTip5KolvoSekc1L.setText(_actFields.kolvo_sekc_1_l)
            actTip5KolvoSekc2.setText(_actFields.kolvo_sekc_2)
            actTip5KolvoSekc2L.setText(_actFields.kolvo_sekc_2_l)
            actTip5KolvoKalorifer.setText(_actFields.kolvo_kalorifer)
            actTip5PoverhnostNagreva.setText(_actFields.poverhnost_nagreva)

            actTip5MestoKarta.setText(_actFields.mesto_karta)

            if (_actFieldsDop.size > 0) {
                actTip5PuNum.setText(_actFieldsDop.first().pu_num)
                actTip5PuName.setText(_actFieldsDop.first().pu_name)
                actTip5PuMesto.setText(_actFieldsDop.first().pu_mesto)
                actTip5PuType.setText(_actFieldsDop.first().pu_type)
                actTip5PuDiam.setText(_actFieldsDop.first().pu_diam)
                actTip5PuKolvo.setText(_actFieldsDop.first().pu_kolvo)
                if (_actFieldsDop.size > 1)
                    _actFieldsDop.forEach {
                        if (_actFieldsDop.first() != it)
                            addKip(it)
                    }
            }

            actTip5NumObj.setText(_actFields.num_obj)
            actTip5VolumeObj.setText(_actFields.volume_obj)
            actTip5Soq.setText(_actFields.so_q)
            actTip5Swq.setText(_actFields.sw_q)
            actTip5Gwq.setText(_actFields.gw_q)
            actTip5Stq.setText(_actFields.st_q)
            actTip5QSum.setText(_actFields.q_sum)

            actTip5NalDocument.setText(_actFields.nal_document)
            actTip5DopInfo.setText(_actFields.dop_info)
            actTip5RemarkDog.setText(_actFields.remark_dog)
            actTip5FilialEso1.text = _actFields.filial_eso
            actTip5DirectorTatenergo1.text = _actFields.director_tatenergo
            actTip5FioPodp1.text = _task.fio_podp

            arrayKip.add(Kip(actTip5PuNum, actTip5PuName, actTip5PuMesto, actTip5PuType, actTip5PuDiam, actTip5PuKolvo))

            // слушатели на кнопки
            // --------------------------
            btnCreateActVnutridomovyhSetey.setOnClickListener {
                createAct()
            }

            btnSignActVnutridomovyhSetey.setOnClickListener {
                try {
                    dlgConfirmActSign = DlgConfirmationActSign(
                        _task.tel_podp,
                        _task.email_podp,
                        _actFields.id_file
                    )
                    dlgConfirmActSign.show(supportFragmentManager, "SIGN_ACT")
                } catch (e: Exception) {
                    println("$TAG_ERR ActVnutridomovyhSetey: ${e.message}")
                }
            }

            btnAddKip.setOnClickListener {
                if (arrayKip.size > 30) {
                    return@setOnClickListener
                } else addKip()
            }
            //------------------
            // Спиннер с фразами
            // -----------------
            actTip5SpCopyText.apply {
                adapter = ArrayAdapter(context!!, R.layout.spitem_purpose, PhrasesCopied)
                setSelection(0)
            }

            actTip5SpCopyText.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {}
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val clipboard: ClipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("CopiedText", actTip5SpCopyText.selectedItem.toString())
                    clipboard.setPrimaryClip(clip)
                }
            }
        } catch (e: Exception) {
            println("$TAG_ERR ActVnutridomovyhSetey: ${e.message}")
        }
    }

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
                    this@ActVnutridomovyhSetey,
                    "Не удалось получить путь к шаблонам.",
                    Toast.LENGTH_LONG
                ).show()
                return false
            }
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActVnutridomovyhSetey,
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
                    btnSignActVnutridomovyhSetey.isEnabled = true

                // Открытие pdf
                // ---------------------------------------
                if (!signed) showPdf(this@ActVnutridomovyhSetey, pdfPath)
            } else {
                Toast.makeText(
                    this@ActVnutridomovyhSetey,
                    "Произошла ошибка при создании акта ${_actFields.num_act}.",
                    Toast.LENGTH_LONG
                ).show()
            }
            return true
        } catch (e: Exception) {
            println("$TAG_ERR: act ${e.message}")
            Toast.makeText(
                this@ActVnutridomovyhSetey,
                "Произошла ошибка при создании акта ${_actFields.num_act}.",
                Toast.LENGTH_LONG
            ).show()
            return false
        }
    }

    // Добавление в разметку новых строк рекоммендаций
    // (мероприятие EditText, сроки EditText)
    // --------------------------------------
    private fun addKip(dop: ActFieldsDopInfo? = null) {
        try {
            val puNum = EditText(this)
            puNum.apply {
                textSize = 18f
                setText((arrayKip.size + 1).toString())
                layoutParams = LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.MATCH_PARENT, 1f
                )
                minHeight = 40
                setPaddingRelative(2, 2, 2, 2)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_caption_acts)
            }
            puNum.doOnPreDraw {
                try {
                    if (dop != null)
                        puNum.setText(dop.pu_num)
                    else
                        puNum.setText("${arrayKip.size}")
                } catch (e: Exception) {
                    println("$TAG_ERR puNum doOnPreDraw ${e.message}")
                }
            }

            val puName = EditText(this)
            puName.apply {
                textSize = 18f
                hint = "Наименование"
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    3f
                )
                minHeight = 40
                setPaddingRelative(2, 2, 2, 2)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_caption_acts)
            }
            puName.doOnPreDraw {
                if (dop != null)
                    puName.setText(dop.pu_name)
            }

            val puMesto = EditText(this)
            puMesto.apply {
                textSize = 18f
                hint = "Место"
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2f
                )
                minHeight = 40
                setPaddingRelative(2, 2, 2, 2)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_caption_acts)
            }
            puMesto.doOnPreDraw {
                if (dop != null)
                    puMesto.setText(dop.pu_mesto)
            }

            val puType = EditText(this)
            puType.apply {
                textSize = 18f
                hint = "Тип"
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2f
                )
                minHeight = 40
                setPaddingRelative(2, 2, 2, 2)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_caption_acts)
            }
            puType.doOnPreDraw {
                if (dop != null)
                    puType.setText(dop.pu_type)
            }

            val puDiam = EditText(this)
            puDiam.apply {
                textSize = 18f
                hint = "Диаметр"
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2f
                )
                minHeight = 40
                setPaddingRelative(2, 2, 2, 2)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_caption_acts)
            }
            puDiam.doOnPreDraw {
                if (dop != null)
                    puDiam.setText(dop.pu_diam)
            }

            val puKolvo = EditText(this)
            puKolvo.apply {
                textSize = 18f
                hint = "Количество"
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2f
                )
                minHeight = 40
                setPaddingRelative(2, 2, 2, 2)
                setTextColor(ContextCompat.getColor(context, R.color.colorDarkGray))
                inputType = InputType.TYPE_TEXT_FLAG_MULTI_LINE
                imeOptions = EditorInfo.IME_FLAG_NO_ENTER_ACTION
                setBackgroundResource(R.drawable.frame_grid_caption_acts)
            }
            puKolvo.doOnPreDraw {
                if (dop != null)
                    puKolvo.setText(dop.pu_kolvo)
            }

            val lvkip = LinearLayout(this)
            lvkip.apply {
                layoutParams =
                    LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                        .apply {
                            marginEnd = 4
                            marginStart = 4
                            minimumHeight = 40
                        }
            }

            lvkip.addView(puNum)
            lvkip.addView(puName)
            lvkip.addView(puMesto)
            lvkip.addView(puType)
            lvkip.addView(puDiam)
            lvkip.addView(puKolvo)

            actTip5Kips.addView(lvkip)

            arrayKip.add(Kip(puNum, puName, puMesto, puType, puDiam, puKolvo))
        } catch (e: Exception) {
            println("$TAG_ERR add kip ${e.message}")
        }
    }

    // Установка недоступности редактирования, если акт подписан
    // ---------------------------------------------------------
    private fun setEditableFalse() {
        actTip5DirectorTatenergo.isEnabled = false
        actTip5DirectorTDoverNum.isEnabled = false
        //actTip5DirectorTDoverDat.isEnabled = false

        actTip5FioPodp.isEnabled = false
        actTip5Zayavitel.isEnabled = false
        actTip5ZayavitelDover.isEnabled = false

        actTip5NameObj.isEnabled = false
        actTip5AdrObj.isEnabled = false

        actTip5Podgotovka.isEnabled = false
        actTip5PodgotovkaProjNum.isEnabled = false
        actTip5PodgotovkaProjIspoln.isEnabled = false
        actTip5PodgotovkaProjUtvergden.isEnabled = false

        actTip5NetInnerTeplonositel.isEnabled = false
        actTip5NetInnerDp.isEnabled = false
        actTip5NetInnerDo.isEnabled = false
        actTip5NetInnerTipKanal.isEnabled = false
        actTip5NetInnerTubeTypeP.isEnabled = false
        actTip5NetInnerTubeTypeO.isEnabled = false
        actTip5NetInnerL.isEnabled = false
        actTip5NetInnerLUndeground.isEnabled = false
        actTip5NetInnerOtstuplenie.isEnabled = false
        actTip5EnergoEffectObject.isEnabled = false
        actTip5NalRezervIstochnik.isEnabled = false
        actTip5NalSvyazi.isEnabled = false

        actTip5VidConnectSystem.isEnabled = false
        actTip5ElevatorNum.isEnabled = false
        actTip5ElevatorDiam.isEnabled = false
        actTip5PodogrevOtopNum.isEnabled = false
        actTip5PodogrevOtopKolvoSekc.isEnabled = false
        actTip5PodogrevOtopLSekc.isEnabled = false
        actTip5PodogrevOtopNazn.isEnabled = false
        actTip5PodogrevOtopMarka.isEnabled = false
        actTip5DNaporPatrubok.isEnabled = false
        actTip5PowerElectroEngine.isEnabled = false
        actTip5ChastotaVrEngine.isEnabled = false
        actTip5DrosselDiafragmaD.isEnabled = false
        actTip5DrosselDiafragmaMesto.isEnabled = false
        actTip5DrosselDiafragmaTipOtop.isEnabled = false
        actTip5DrosselDiafragmaCntStoyak.isEnabled = false
        actTip5TypeOtoPrib.isEnabled = false
        actTip5SchemaVklGvs.isEnabled = false
        actTip5SchemaVklPodogrev.isEnabled = false
        actTip5KolvoSekc1.isEnabled = false
        actTip5KolvoSekc1L.isEnabled = false
        actTip5KolvoSekc2.isEnabled = false
        actTip5KolvoSekc2L.isEnabled = false
        actTip5KolvoKalorifer.isEnabled = false
        actTip5PoverhnostNagreva.isEnabled = false

        actTip5MestoKarta.isEnabled = false

        actTip5PuNum.isEnabled = false
        actTip5PuName.isEnabled = false
        actTip5PuMesto.isEnabled = false
        actTip5PuType.isEnabled = false
        actTip5PuDiam.isEnabled = false
        actTip5PuKolvo.isEnabled = false

        arrayKip.forEach {
            it.pu_num.isEnabled = false
            it.pu_name.isEnabled = false
            it.pu_mesto.isEnabled = false
            it.pu_type.isEnabled = false
            it.pu_diam.isEnabled = false
            it.pu_kolvo.isEnabled = false
        }

        actTip5NumObj.isEnabled = false
        actTip5VolumeObj.isEnabled = false
        actTip5Soq.isEnabled = false
        actTip5Swq.isEnabled = false
        actTip5Gwq.isEnabled = false
        actTip5Stq.isEnabled = false
        actTip5QSum.isEnabled = false


        actTip5NalDocument.isEnabled = false
        actTip5DopInfo.isEnabled = false
        actTip5RemarkDog.isEnabled = false

        btnCreateActVnutridomovyhSetey.isEnabled = false
        btnSignActVnutridomovyhSetey.isEnabled = false
        actTip5SpCopyText.isEnabled = false
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
                        actTip5DatAct.text = _actFields.dat_act
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
            num_act = actTip5NumAct.text.toString()
            dat_act = actTip5DatAct.text.toString()

            city = actTip5City.text.toString()
            director_tatenergo = actTip5DirectorTatenergo.text.toString()
            director_t_dover_num = actTip5DirectorTDoverNum.text.toString()
            //director_t_dover_date = actTip5DirectorTDoverDat.text.toString()
            zayavitel = actTip5Zayavitel.text.toString()
            zayavitel_dover = actTip5ZayavitelDover.text.toString()
            fio_contact = actTip5FioPodp.text.toString()
            name_obj = actTip5NameObj.text.toString()
            adr_obj = actTip5AdrObj.text.toString()
            podgotovka = actTip5Podgotovka.text.toString()
            podgotovka_proj_num = actTip5PodgotovkaProjNum.text.toString()
            podgotovka_proj_ispoln = actTip5PodgotovkaProjIspoln.text.toString()
            podgotovka_proj_utvergden = actTip5PodgotovkaProjUtvergden.text.toString()

            net_inner_teplonositel = actTip5NetInnerTeplonositel.text.toString()
            net_inner_dp = actTip5NetInnerDp.text.toString()
            net_inner_do = actTip5NetInnerDo.text.toString()
            net_inner_tip_kanal = actTip5NetInnerTipKanal.text.toString()
            net_inner_tube_type_p = actTip5NetInnerTubeTypeP.text.toString()
            net_inner_tube_type_o = actTip5NetInnerTubeTypeO.text.toString()
            net_inner_l = actTip5NetInnerL.text.toString()
            net_inner_l_undeground = actTip5NetInnerLUndeground.text.toString()
            net_inner_otstuplenie = actTip5NetInnerOtstuplenie.text.toString()
            energo_effect_object = actTip5EnergoEffectObject.text.toString()
            nal_rezerv_istochnik = actTip5NalRezervIstochnik.text.toString()
            nal_svyazi = actTip5NalSvyazi.text.toString()

            vid_connect_system = actTip5VidConnectSystem.text.toString()
            elevator_num = actTip5ElevatorNum.text.toString()
            elevator_diam = actTip5ElevatorDiam.text.toString()
            podogrev_otop_num = actTip5PodogrevOtopNum.text.toString()
            podogrev_otop_kolvo_sekc = actTip5PodogrevOtopKolvoSekc.text.toString()
            podogrev_otop_l_sekc = actTip5PodogrevOtopLSekc.text.toString()
            podogrev_otop_nazn = actTip5PodogrevOtopNazn.text.toString()
            podogrev_otop_marka = actTip5PodogrevOtopMarka.text.toString()
            d_napor_patrubok = actTip5DNaporPatrubok.text.toString()
            power_electro_engine = actTip5PowerElectroEngine.text.toString()
            chastota_vr_engine = actTip5ChastotaVrEngine.text.toString()
            drossel_diafragma_d = actTip5DrosselDiafragmaD.text.toString()
            drossel_diafragma_mesto = actTip5DrosselDiafragmaMesto.text.toString()
            drossel_diafragma_tip_otop = actTip5DrosselDiafragmaTipOtop.text.toString()
            drossel_diafragma_cnt_stoyak = actTip5DrosselDiafragmaCntStoyak.text.toString()
            type_oto_prib = actTip5TypeOtoPrib.text.toString()
            schema_vkl_gvs = actTip5SchemaVklGvs.text.toString()
            schema_vkl_podogrev = actTip5SchemaVklPodogrev.text.toString()
            kolvo_sekc_1 = actTip5KolvoSekc1.text.toString()
            kolvo_sekc_1_l = actTip5KolvoSekc1L.text.toString()
            kolvo_sekc_2 = actTip5KolvoSekc2.text.toString()
            kolvo_sekc_2_l = actTip5KolvoSekc2L.text.toString()
            kolvo_kalorifer = actTip5KolvoKalorifer.text.toString()
            poverhnost_nagreva = actTip5PoverhnostNagreva.text.toString()

            mesto_karta = actTip5MestoKarta.text.toString()

            num_obj = actTip5NumObj.text.toString()
            volume_obj = actTip5VolumeObj.text.toString()
            so_q = actTip5Soq.text.toString()
            sw_q = actTip5Swq.text.toString()
            gw_q = actTip5Gwq.text.toString()
            st_q = actTip5Stq.text.toString()
            q_sum = actTip5QSum.text.toString()

            nal_document = actTip5NalDocument.text.toString()
            dop_info = actTip5DopInfo.text.toString()
            remark_dog = actTip5RemarkDog.text.toString()
        }

        val dbWrite = DbHandlerLocalWrite(this, null)
        dbWrite.deleteActFieldsDop(_task.id_task, _act.id_act, npp)

        arrayKip.forEach {
            val dop = ActFieldsDopInfo(
                id_task = _task.id_task,
                id_act = _act.id_act,
                pu_num = it.pu_num.text.toString(),
                pu_name = it.pu_name.text.toString(),
                pu_mesto = it.pu_mesto.text.toString(),
                pu_type = it.pu_type.text.toString(),
                pu_diam = it.pu_diam.text.toString(),
                pu_kolvo = it.pu_kolvo.text.toString(),
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
        val vals: LinkedHashMap<String, String> = LinkedHashMap(65)
        vals["%CITY"] = _actFields.city
        vals["%NUM_ACT"] = _actFields.num_act
        vals["%DAT_ACT"] = _actFields.dat_act
        vals["%FILIAL_ESO"] = _actFields.filial_eso
        vals["%DIRECTOR_TATENERGO"] = _actFields.director_tatenergo
        vals["%DIRECTOR_T_DOVER_NUM"] = _actFields.director_t_dover_num
        // vals["%DIRECTOR_T_DOVER_DATE"] = _actFields.director_t_dover_date
        vals["%ZAYAVITEL_FIO"] = _actFields.zayavitel
        vals["%FIO_PODP"] = _task.fio_podp
        vals["%ZAYAVITEL_DOVER"] = _actFields.zayavitel_dover
        vals["%NAME_OBJ"] = _actFields.name_obj
        vals["%ADR_OBJ"] = _actFields.adr_obj
        vals["%NDOG"] = _actFields.ndog
        vals["%DAT_DOG"] = _actFields.dat_dog
        vals["%PODGOTOVKA_TXT"] = _actFields.podgotovka
        vals["%PODGOTOVKA_PROJ_NUM"] = _actFields.podgotovka_proj_num
        vals["%PODGOTOVKA_PROJ_ISPOLN"] = _actFields.podgotovka_proj_ispoln
        vals["%PODGOTOVKA_PROJ_UTVERGDEN"] = _actFields.podgotovka_proj_utvergden
        vals["%NET_INNER_TEPLONOSITEL"] = _actFields.net_inner_teplonositel
        vals["%NET_INNER_DP"] = _actFields.net_inner_dp
        vals["%NET_INNER_DO"] = _actFields.net_inner_do
        vals["%NET_INNER_TIP_KANAL"] = _actFields.net_inner_tip_kanal
        vals["%NET_INNER_TUBE_TYPE_P"] = _actFields.net_inner_tube_type_p
        vals["%NET_INNER_TUBE_TYPE_O"] = _actFields.net_inner_tube_type_o
        vals["%NET_INNER_LM"] = _actFields.net_inner_l
        vals["%NET_INNER_L_UNDEGROUND"] = _actFields.net_inner_l_undeground
        vals["%NET_INNER_OTSTUPLENIE"] = _actFields.net_inner_otstuplenie
        vals["%ENERGO_EFFECT_OBJECT"] = _actFields.energo_effect_object
        vals["%NAL_REZERV_ISTOCHNIK"] = _actFields.nal_rezerv_istochnik
        vals["%NAL_SVYAZI"] = _actFields.nal_svyazi
        vals["%VID_CONNECT_SYSTEM"] = _actFields.vid_connect_system
        vals["%ELEVATOR_NUM"] = _actFields.elevator_num
        vals["%ELEVATOR_DIAM"] = _actFields.elevator_diam
        vals["%PODOGREV_OTOP_NUM"] = _actFields.podogrev_otop_num
        vals["%PODOGREV_OTOP_KOLVO_SEKC"] = _actFields.podogrev_otop_kolvo_sekc
        vals["%PODOGREV_OTOP_L_SEKC"] = _actFields.podogrev_otop_l_sekc
        vals["%PODOGREV_OTOP_NAZN"] = _actFields.podogrev_otop_nazn
        vals["%PODOGREV_OTOP_MARKA"] = _actFields.podogrev_otop_marka
        vals["%D_NAPOR_PATRUBOK"] = _actFields.d_napor_patrubok
        vals["%POWER_ELECTRO_ENGINE"] = _actFields.power_electro_engine
        vals["%CHASTOTA_VR_ENGINE"] = _actFields.chastota_vr_engine
        vals["%DROSSEL_DIAFRAGMA_D"] = _actFields.drossel_diafragma_d
        vals["%DROSSEL_DIAFRAGMA_MESTO"] = _actFields.drossel_diafragma_mesto
        vals["%DROSSEL_DIAFRAGMA_TIP_OTOP"] = _actFields.drossel_diafragma_tip_otop
        vals["%DROSSEL_DIAFRAGMA_CNT_STOYAK"] = _actFields.drossel_diafragma_cnt_stoyak
        vals["%TYPE_OTO_PRIB"] = _actFields.type_oto_prib
        vals["%SCHEMA_VKL_GVS"] = _actFields.schema_vkl_gvs
        vals["%SCHEMA_VKL_PODOGREV"] = _actFields.schema_vkl_podogrev
        vals["%KOLVO_SEKC_1_CNT"] = _actFields.kolvo_sekc_1
        vals["%KOLVO_SEKC_1_L"] = _actFields.kolvo_sekc_1_l
        vals["%KOLVO_SEKC_2_CNT"] = _actFields.kolvo_sekc_2
        vals["%KOLVO_SEKC_2_L"] = _actFields.kolvo_sekc_2_l
        vals["%KOLVO_KALORIFER"] = _actFields.kolvo_kalorifer
        vals["%POVERHNOST_NAGREVA"] = _actFields.poverhnost_nagreva

        var table = ""
        if (arrayKip.size > 0 ) {
            table = "<table border=\"1px\" style=\"border-color: black; width: 100%;\" cellspacing=\"0\">"
            table += arrayKip.joinToString {
                "<tr><td width=\"5%\">${it.pu_num.text}</td><td width=\"25%\">${it.pu_name.text}</td><td width=\"35%\">${it.pu_mesto.text}</td><td width=\"10%\">${it.pu_type.text}</td><td width=\"10%\">${it.pu_diam.text}</td><td width=\"10%\">${it.pu_kolvo.text}</td></tr>"
            }
            table += "\\n</table>"
        }

        vals["%TABLE1"] = table
        vals["%MESTO_KARTA"] = _actFields.mesto_karta
        vals["%NUM_OBJ"] = _actFields.num_obj
        vals["%VOLUME_OBJ"] = _actFields.volume_obj
        vals["%SO_Q"] = _actFields.so_q
        vals["%SW_Q"] = _actFields.sw_q
        vals["%GW_Q"] = _actFields.gw_q
        vals["%ST_Q"] = _actFields.st_q
        vals["%Q_SUM"] = _actFields.q_sum
        vals["%NAL_DOCUMENT"] = _actFields.nal_document
        vals["%DOP_INFO"] = _actFields.dop_info

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
        if (!btnCreateActVnutridomovyhSetey.isEnabled) {
            setResult(RESULT_OK)
            this@ActVnutridomovyhSetey.finish()
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
                    this@ActVnutridomovyhSetey.finish()
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