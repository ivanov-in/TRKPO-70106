package ru.infoenergo.mis.helpers

import android.net.Uri
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

const val ID_NEW_TASK = -1000     // ID задачи при добавлении задачи вручную
const val PERMISSIONS = 123

const val LOGIN = 10                  // ID для создания акта
const val UNPIN_ACT = 11              // ID для открепления акта
const val UNPIN_PHOTO = 12            // ID для открепления фото
const val SHARE_PDF = 13              // ID для поделиться pdf файлом
const val SHARE_IMG = 14              // ID для поделиться фоткой
const val PHOTO_TO_ACT = 15           // ID для прикрепить фото к акту
const val CAMERA_TO_ACT = 17          // ID для прикрепить снимок с камеры к акту
const val PAPER_TO_ACT = 16           // ID для  прикрепить бум акт к акту
const val CREATE_ACT = 20             // ID для создания акта
const val SIGN_ACT = 30               // подписание акта
const val TASK_UPDATE = 41            // обновление задачи
const val ADD_NEW_TASK = 51           // добавление задачи вручную
const val CREATE_NEW_TASK = 52        // добавление задачи через сервер
const val CAMERA_REQUEST = 61         // пришла фотография с камеры
const val PICK_IMAGE_REQUEST = 71     // прикрепление фотографии с устройства
const val PICK_PAPER_REQUEST = 72     // прикрепление фотографии акта с устройства
const val RESULT_NEW_ABON = 2         // Создание задачи вручную с подобранным адресом
const val RESULT_ADD_TASK = 1         // Создание задачи вручную


const val TAG_ERR = "MYLOG ERR"
const val TAG_OK = "MYLOG ERR OK"

// Стандартные фразы для составления актов
// ---------------------------------------
val PhrasesCopied = arrayOf("Предъявлена гидропневмапромывка ")

// Задача маршрутного листа
// -----------------------------
class Task(
    var dat: LocalDate? = null, // NOT NULL,  DATE дата задания
    var id_task: Int = 0, //  NOT NULL, NUMBER уникальный номер задания PK
    var address: String = "", // VARCHAR2(500) основной адрес объекта посещения
    var purpose: Int = 1, //  NOT NULL, NUMBER цель посещения
    var purpose_name: String = "",
    var prim: String = "", // VARCHAR2(500) примечания к заданию
    var ttime: LocalDateTime? = null, // DATE примерное планируемое время посещения
    var status: Int = 0, // NUMBER статус записи
    var status_name: String = "", // статус записи
    var id_inspector: Int = 0, // NUMBER инспектор
    var fio: String = "", // фио инспектора
    var kod_obj: Int = 0, // NUMBER Код объекта из АСУСЭ
    var kod_dog: Int = 0,
    var kod_numobj: Int = 0,
    var kodp: Int = 0,
    var ndog: String = "",
    var payer_name: String = "",
    var fio_contact: String = "", // VARCHAR2(4000) ФИО контактного лица
    var tel_contact: String = "", // VARCHAR2(200) Телефон контактного лица
    var email_contact: String = "", // VARCHAR2(200) Телефон контактного лица
    var lat: String = "",
    var lan: String = "",
    var schema_zulu: String = "",
    var border_zulu: String = "",
    var city: String = "",
    var street: String = "",
    var house: String = "",
    var nd: String = "",
    var diff: String = "", // различия при сравнении
    var is_send: Int = 0,
    var fio_podp: String = "",
    var email_podp: String = "",
    var tel_podp: String = "",
    var name_dolzhn_podp: String = "",
    var kod_emp_podp: Int = 0
) : Serializable {
    override fun hashCode(): Int {
        var result = dat?.hashCode() ?: 0
        result = 31 * result + id_task
        result = 31 * result + address.hashCode()
        result = 31 * result + purpose
        result = 31 * result + purpose_name.hashCode()
        result = 31 * result + prim.hashCode()
        result = 31 * result + (ttime?.hashCode() ?: 0)
        result = 31 * result + status
        result = 31 * result + status_name.hashCode()
        result = 31 * result + id_inspector
        result = 31 * result + fio.hashCode()
        result = 31 * result + kod_obj
        result = 31 * result + kod_dog
        result = 31 * result + kodp
        result = 31 * result + ndog.hashCode()
        result = 31 * result + payer_name.hashCode()
        result = 31 * result + fio_contact.hashCode()
        result = 31 * result + tel_contact.hashCode()
        result = 31 * result + email_contact.hashCode()
        result = 31 * result + lat.hashCode()
        result = 31 * result + lan.hashCode()
        result = 31 * result + schema_zulu.hashCode()
        result = 31 * result + border_zulu.hashCode()
        result = 31 * result + city.hashCode()
        result = 31 * result + street.hashCode()
        result = 31 * result + house.hashCode()
        result = 31 * result + nd.hashCode()
        result = 31 * result + diff.hashCode()
        result = 31 * result + is_send
        result = 31 * result + fio_podp.hashCode()
        result = 31 * result + email_podp.hashCode()
        result = 31 * result + tel_podp.hashCode()
        result = 31 * result + name_dolzhn_podp.hashCode()
        result = 31 * result + kod_emp_podp
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Task

        if (id_task != other.id_task) return false
        if (dat != other.dat) {
            diff += "\nдата: с '${other.dat}' на '${dat}'; "
            other.diff += "\nдата: с '${dat}' на '${other.dat}'; "
            //return false
        }
        if (address.replace("null", "") != other.address.replace("null", "")) {
            diff += "\nадрес: с '${other.address}' на '${address}'; "
            other.diff += "\nадрес: с '${address}' на '${other.address}'; "
            //return false
        }
        if (purpose != other.purpose) {
            diff += "\nцель посещения: с '${other.purpose_name}' на '${purpose_name}'; "
            other.diff += "\nцель посещения: с '${purpose_name}' на '${other.purpose_name}'; "
            //return false
        }
        if (prim.replace("null", "") != other.prim.replace("null", "")) {
            diff += "\nпримечание: с '${other.prim}' на '${prim}'; "
            other.diff += "\nпримечание: с '${prim}' на '${other.prim}'; "
            //return false
        }
        if (ttime != other.ttime) {
            diff += "\nвремя: с '${other.ttime.toString().replace("T", " ")}' на '${
                ttime.toString().replace("T", " ")
            }'; "
            other.diff += "\nвремя: с '${ttime.toString().replace("T", " ")}' на '${
                other.ttime.toString().replace("T", " ")
            }'; "
            //return false
        }

        if (status != other.status) {
            // return false
        }

        if (status_name.replace("null", "") != other.status_name.replace("null", "")) {
            diff += "\nстатус: с '${other.status_name}' на '${status_name}'; "
            other.diff += "\nстатус с '${status_name}' на '${other.status_name}'; "
            //return false
        }

        if (kod_obj != other.kod_obj) {
            diff += "\nкод объекта: с '${other.kod_obj}' на '${kod_obj}'; "
            other.diff += "\nкод объекта: с '${kod_obj}' на '${other.kod_obj}'; "
            //return false
        }
        if (kod_dog != other.kod_dog) {
            diff += "\nкод договора: с '${other.kod_dog}' на '${kod_dog}'; "
            other.diff += "\nкод договора: с '${kod_dog}' на '${other.kod_dog}'; "
            //return false
        }
        if (kodp != other.kodp) {
            diff += "\nкод договора: с '${other.kodp}' на '${kodp}'; "
            other.diff += "\nкод договора: с '${kodp}' на '${other.kodp}'; "
            //return false
        }
        if (ndog.replace("null", "") != other.ndog.replace("null", "")) {
            diff += "\nномер договора: с '${other.ndog}' на '${ndog}'; "
            other.diff += "\nномер договора: с '${ndog}' на '${other.ndog}'; "
            //return false
        }
        if (payer_name.replace("null", "") != other.payer_name.replace("null", "")) {
            diff += "\nнаименование абонента: с '${other.payer_name}' на '${payer_name}'; "
            other.diff += "\nнаименование абонента: с '${payer_name}' на '${other.payer_name}'; "
            //return false
        }
        if (fio_contact.replace("null", "") != other.fio_contact.replace("null", "")) {
            diff += "\nФИО конт. лица: с '${other.fio_contact}' на '${fio_contact}'; "
            other.diff += "\nФИО конт. лица: с '${fio_contact}' на '${other.fio_contact}'; "
            //return false
        }
        if (tel_contact.replace("null", "") != other.tel_contact.replace("null", "")) {
            diff += "\nтел. конт. лица: с '${other.tel_contact}' на '${tel_contact}'; "
            other.diff += "\nтел. конт. лица: с '${tel_contact}' на '${other.tel_contact}'; "
            //return false
        }
        if (email_contact.replace("null", "") != other.email_contact.replace("null", "")) {
            diff += "\nemail конт. лица: с '${other.email_contact}' на '${email_contact}'; "
            other.diff += "\nemail конт. лица: с '${email_contact}' на '${other.email_contact}'; "
            //return false
        }

        if (kod_emp_podp != other.kod_emp_podp) {
            diff += "\nподписант: с '${other.fio_podp} (${other.tel_podp}/${other.email_podp})' на '${fio_podp} (${tel_podp}/${email_podp})'; "
            other.diff += "\nподписант с '${fio_podp} (${tel_podp}/${email_podp})' на '${other.fio_podp} (${other.tel_podp}/${other.email_podp})'; "
        }

        if (diff.isNotEmpty() || other.diff.isNotEmpty()) return false
        if (id_inspector != other.id_inspector) return false
        if (fio.replace("null", "") != other.fio.replace("null", "")) return false
        if (lat.replace("null", "") != other.lat.replace("null", "")) return false
        if (lan.replace("null", "") != other.lan.replace("null", "")) return false
        if (schema_zulu.replace("null", "") != other.schema_zulu.replace("null", "")) return false
        if (border_zulu.replace("null", "") != other.border_zulu.replace("null", "")) return false

        return true
    }
}

// Информация о фотографии, прикреплённой к задаче
// -----------------------------------------------
class FileInfo(
    var id_task: Int,
    var id_act: Int,
    var npp: Int,
    var filename: String = "",
    var filedata: ByteArray? = null,
    var id_file: Int,
    var uri: Uri? = Uri.EMPTY,
    var is_signed: Int,
    var is_send: Int,
    var paper: Int,
    var date_send_to_client: LocalDateTime?,
    var email_client: String,
    var cnt_attaches: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FileInfo

        if (id_task != other.id_task) return false

        if (filename.replace("null", "") != other.filename.replace("null", "")) return false
        if (filedata != null) {
            if (other.filedata == null) return false
            if (!filedata.contentEquals(other.filedata)) return false
        } else if (other.filedata != null) return false
        if (id_file != other.id_file) return false
        if (is_signed != other.is_signed) return false
        if (paper != other.paper) return false
        if (date_send_to_client != other.date_send_to_client) return false
        if (email_client != other.email_client) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id_task
        result = 31 * result + filename.hashCode()
        result = 31 * result + (filedata?.contentHashCode() ?: 0)
        result = 31 * result + id_file.hashCode()
        result = 31 * result + (uri?.hashCode() ?: 0)
        result = 31 * result + is_signed
        result = 31 * result + paper
        result = 31 * result + date_send_to_client.hashCode()
        result = 31 * result + email_client.hashCode()
        return result
    }
}

// Информация о посещенном объекте для истории посещений
// -----------------------------------------------------
class HistoryItemInfo(
    val dat: LocalDate,
    val id_task: Int,
    val adr: String,
    val purpose: Int,
    val purpose_name: String,
    val prim: String,
    val ttime: LocalDateTime,
    val status: Int,
    val status_name: String,
    val id_inspector: Int,
    val kod_dog: Int,
    val kod_obj: Int,
    val kodp: Int,
    val ndog: String,
    val payer_name: String,
    val fio_contact: String,
    val email_contact: String,
    val tel_contact: String,
    val fio: String,
    var cnt_files: Int,
    var fio_podp: String = "",
    var email_podp: String = "",
    var tel_podp: String = "",
    var name_dolzhn_podp: String = "",
    var kod_emp_podp: Int = 0
)

// Информация о подписанте
// -----------------------
class Podpisant
    (
    var id_task: Int = 0,
    var fio: String = "",
    var email: String = "",
    var tel: String = "",
    var name_dolzhn: String = "",
    var kod_emp: Int = 0
) : Serializable

// Информация об акте
// ------------------
class ActInfo(
    var id_purpose: Int = 0,
    var id_act: Int = 0,
    var name: String = "",
    var tip: Int = 0
) : Serializable

// Информация об полях акта
// --------------------------------------
class ActFieldsInfo(
    var id_task: Int = 0, //  NOT NULL, NUMBER уникальный номер задания PK
    var npp: Int = 0,
    var id_act: Int = 0,
    var id_file: Int = 0,
    var is_signed: Int = 0,
    var kodp: Int = 0,
    var kod_dog: Int = 0,
    var kod_obj: Int = 0, // NUMBER Код объекта из АСУСЭ
    var num_act: String = "",
    var dat_act: String = "",
    var payer_name: String = "",
    var adr_org: String = "",
    var fio_contact: String = "", // VARCHAR2(4000) ФИО контактного лица
    var tel_contact: String = "", // VARCHAR2(200) Телефон контактного лица
    var filial_eso: String = "",
    var fio_eso: String = "",
    var tel_eso: String = "",
    var list_obj: String = "",
    var name_obj: String = "",
    var num_obj: String = "",
    var adr_obj: String = "",
    var ndog: String = "",
    var dat_dog: String = "",
    var otop_period: String = "",
    var sum_dolg: String = "",
    var remark_dog: String = "",
    var nal_podp_doc: String = "",
    var opl_calcul: String = "",
    var osnov: String = "",
    var city: String = "",
    var shablon: String = "",
    var name_dolzhn_contact: String = "",
    var name_dolzhn_eso: String = "",
    var period_dolg: String = "",
    var name_st: String = "",
    var name_mag: String = "",
    var name_tk: String = "",
    var name_aw: String = "",
    var inn_org: String = "",
    var kpp_org: String = "",
    var nazn_name: String = "",
    var nal_so: String = "",
    var nal_sw: String = "",
    var nal_st: String = "",
    var nal_gv: String = "",
    var dat: LocalDate? = null,

    var purpose_text: String = "",
    var fact_text: String = "",
    var id_manometr: String = "0",
    var id_aupr_so: String = "0",
    var id_aupr_sw: String = "0",
    var id_aupr_gvs: String = "0",
    var id_aupr_sv: String = "0",
    var sost_kip_str: String = "",
    var nal_act_gidro: String = "",
    var id_sost_tube: String = "0",
    var id_sost_armatur: String = "0",
    var id_sost_izol: String = "0",
    var sost_tube_str: String = "",
    var id_sost_net: String = "0",
    var sost_net_str: String = "",
    var id_sost_utepl: String = "0",
    var sost_utepl_str: String = "",
    var id_nal_pasport: String = "0",
    var id_nal_schema: String = "0",
    var id_nal_instr: String = "0",
    var nal_pasp_str: String = "",
    var id_nal_direct_connect: String = "0",
    var nal_direct_connect: String = "",
    var comiss_post_gotov: String = "",
    var pred_comiss: String = "",
    var zam_pred_gkh: String = "",
    var podpisi: String = "",
    var director_tatenergo: String = "",
    var director_t_dover_num: String = "",
    //var director_t_dover_date: String = "",
    var zayavitel: String = "",
    var zayavitel_dover: String = "",
    var podgotovka: String = "",
    var podgotovka_proj_num: String = "",
    var podgotovka_proj_ispoln: String = "",
    var podgotovka_proj_utvergden: String = "",
    var net_inner_teplonositel: String = "",
    var net_inner_dp: String = "",
    var net_inner_do: String = "",
    var net_inner_tip_kanal: String = "",
    var net_inner_tube_type_p: String = "",
    var net_inner_tube_type_o: String = "",
    var net_inner_l: String = "",
    var net_inner_l_undeground: String = "",
    var net_inner_otstuplenie: String = "",
    var energo_effect_object: String = "",
    var nal_rezerv_istochnik: String = "",
    var nal_svyazi: String = "",
    var vid_connect_system: String = "",
    var elevator_num: String = "",
    var elevator_diam: String = "",
    var podogrev_otop_num: String = "",
    var podogrev_otop_kolvo_sekc: String = "",
    var podogrev_otop_l_sekc: String = "",
    var podogrev_otop_nazn: String = "",
    var podogrev_otop_marka: String = "",
    var d_napor_patrubok: String = "",
    var power_electro_engine: String = "",
    var chastota_vr_engine: String = "",
    var drossel_diafragma_d: String = "",
    var drossel_diafragma_mesto: String = "",
    var drossel_diafragma_tip_otop: String = "",
    var drossel_diafragma_cnt_stoyak: String = "",
    var pu_data: String = "",
    var pu_pover_lico: String = "",
    var pu_pover_pokaz: String = "",
    var pu_pover_rez: String = "",
    var balans_prinadl_obj: String = "",
    var balans_prin_dop: String = "",
    var gr_ekspl_otvetst: String = "",
    var gr_ekspl_otvetst_dop: String = "",
    var st_podkl_rub: String = "",
    var st_podkl_rub_nds: String = "",
    var podkl_dop_sved: String = "",
    var pred_pogash_dolg_num: String = "",
    var pred_pogash_dolg_date: String = "",
    var otkl_proizv: String = "",
    var otkl_pu_pokaz_do: String = "",
    var otkl_pu_pokaz_posle: String = "",
    var filial_name: String = "",
    var filial_address: String = "",
    var filial_tel: String = "",
    var act_poluchil: String = "",
    var nal_document: String = "",
    var dop_info: String = "",
    var ispolnitel: String = "",
    var dog_podkl_num: String = "",
    var dog_podkl_date: String = "",
    var mesto_karta: String = "",
    var uvedoml_otkl_num: String = "",
    var uvedoml_otkl_date: String = "",
    var prichina_otkaza: String = "",
    var otkaz_svidet_1: String = "",
    var otkaz_svidet_2: String = "",
    var pravo_sobstv: String = "",
    var uvedom_aktirov_num: String = "",
    var uvedom_aktirov_date: String = "",
    var predst_potrebit: String = "",
    var predst_potrebit_dover: String = "",
    var volume_obj: String = "",
    var square_obj: String = "",
    var q_sum: String = "",
    var so_q: String = "",
    var sw_q: String = "",
    var st_q: String = "",
    var gw_q: String = "",
    var nal_pu: String = "",
    var nal_aupr: String = "",
    var bezdog_sposob_num: String = "",
    var bezdog_ustanovleno: String = "",
    var bezdog_narushenie: String = "",
    var bezdog_pereraschet_s: String = "",
    var bezdog_pereraschet_po: String = "",
    var bezdog_predpis: String = "",
    var bezdog_obyasn: String = "",
    var bezdog_pretenz: String = "",
    var uslovie_podkl_num: String = "",
    var soglasov_proekte_num: String = "",
    var dopusk_s: String = "",
    var dopusk_po: String = "",
    var tel_spravki: String = "",
    var tel_dispetch: String = "",
    var org_ustanov_pu: String = "",
    var type_oto_prib: String = "",
    var schema_vkl_gvs: String = "",
    var schema_vkl_podogrev: String = "",
    var kolvo_sekc_1: String = "",
    var kolvo_sekc_1_l: String = "",
    var kolvo_sekc_2: String = "",
    var kolvo_sekc_2_l: String = "",
    var kolvo_kalorifer: String = "",
    var poverhnost_nagreva: String = "",
    var podkl_num: String = "",
    var q_max: String = "",
    var itog_text: String = ""
) : Serializable {

    override fun hashCode(): Int {
        var result = id_task
        result = 31 * result + id_act
        result = 31 * result + is_signed
        result = 31 * result + id_file
        result = 31 * result + npp
        result = 31 * result + kodp
        result = 31 * result + kod_dog
        result = 31 * result + kod_obj
        result = 31 * result + num_act.hashCode()
        result = 31 * result + dat_act.hashCode()
        result = 31 * result + payer_name.hashCode()
        result = 31 * result + adr_org.hashCode()
        result = 31 * result + fio_contact.hashCode()
        result = 31 * result + tel_contact.hashCode()
        result = 31 * result + filial_eso.hashCode()
        result = 31 * result + fio_eso.hashCode()
        result = 31 * result + tel_eso.hashCode()
        result = 31 * result + list_obj.hashCode()
        result = 31 * result + name_obj.hashCode()
        result = 31 * result + num_obj.hashCode()
        result = 31 * result + adr_obj.hashCode()
        result = 31 * result + ndog.hashCode()
        result = 31 * result + dat_dog.hashCode()
        result = 31 * result + otop_period.hashCode()
        result = 31 * result + sum_dolg.hashCode()
        result = 31 * result + remark_dog.hashCode()
        result = 31 * result + nal_podp_doc.hashCode()
        result = 31 * result + opl_calcul.hashCode()
        result = 31 * result + osnov.hashCode()
        result = 31 * result + city.hashCode()
        result = 31 * result + shablon.hashCode()
        result = 31 * result + name_dolzhn_contact.hashCode()
        result = 31 * result + name_dolzhn_eso.hashCode()
        result = 31 * result + period_dolg.hashCode()
        result = 31 * result + name_st.hashCode()
        result = 31 * result + name_mag.hashCode()
        result = 31 * result + name_tk.hashCode()
        result = 31 * result + name_aw.hashCode()
        result = 31 * result + inn_org.hashCode()
        result = 31 * result + kpp_org.hashCode()
        result = 31 * result + nazn_name.hashCode()
        result = 31 * result + nal_so.hashCode()
        result = 31 * result + nal_sw.hashCode()
        result = 31 * result + nal_st.hashCode()
        result = 31 * result + nal_gv.hashCode()
        result = 31 * result + (dat?.hashCode() ?: 0)
        result = 31 * result + purpose_text.hashCode()
        result = 31 * result + fact_text.hashCode()
        result = 31 * result + id_manometr.hashCode()
        result = 31 * result + id_aupr_so.hashCode()
        result = 31 * result + id_aupr_sw.hashCode()
        result = 31 * result + id_aupr_gvs.hashCode()
        result = 31 * result + id_aupr_sv.hashCode()
        result = 31 * result + sost_kip_str.hashCode()
        result = 31 * result + nal_act_gidro.hashCode()
        result = 31 * result + id_sost_tube.hashCode()
        result = 31 * result + id_sost_armatur.hashCode()
        result = 31 * result + id_sost_izol.hashCode()
        result = 31 * result + sost_tube_str.hashCode()
        result = 31 * result + id_sost_net.hashCode()
        result = 31 * result + sost_net_str.hashCode()
        result = 31 * result + id_sost_utepl.hashCode()
        result = 31 * result + sost_utepl_str.hashCode()
        result = 31 * result + id_nal_pasport.hashCode()
        result = 31 * result + id_nal_schema.hashCode()
        result = 31 * result + id_nal_instr.hashCode()
        result = 31 * result + nal_pasp_str.hashCode()
        result = 31 * result + id_nal_direct_connect.hashCode()
        result = 31 * result + nal_direct_connect.hashCode()
        result = 31 * result + comiss_post_gotov.hashCode()
        result = 31 * result + pred_comiss.hashCode()
        result = 31 * result + zam_pred_gkh.hashCode()
        result = 31 * result + podpisi.hashCode()
        result = 31 * result + director_tatenergo.hashCode()
        result = 31 * result + director_t_dover_num.hashCode()
        //result = 31 * result + director_t_dover_date.hashCode()
        result = 31 * result + zayavitel.hashCode()
        result = 31 * result + zayavitel_dover.hashCode()
        result = 31 * result + podgotovka.hashCode()
        result = 31 * result + podgotovka_proj_num.hashCode()
        result = 31 * result + podgotovka_proj_ispoln.hashCode()
        result = 31 * result + podgotovka_proj_utvergden.hashCode()
        result = 31 * result + net_inner_teplonositel.hashCode()
        result = 31 * result + net_inner_dp.hashCode()
        result = 31 * result + net_inner_do.hashCode()
        result = 31 * result + net_inner_tip_kanal.hashCode()
        result = 31 * result + net_inner_tube_type_p.hashCode()
        result = 31 * result + net_inner_tube_type_o.hashCode()
        result = 31 * result + net_inner_l.hashCode()
        result = 31 * result + net_inner_l_undeground.hashCode()
        result = 31 * result + net_inner_otstuplenie.hashCode()
        result = 31 * result + energo_effect_object.hashCode()
        result = 31 * result + nal_rezerv_istochnik.hashCode()
        result = 31 * result + nal_svyazi.hashCode()
        result = 31 * result + vid_connect_system.hashCode()
        result = 31 * result + elevator_num.hashCode()
        result = 31 * result + elevator_diam.hashCode()
        result = 31 * result + podogrev_otop_num.hashCode()
        result = 31 * result + podogrev_otop_kolvo_sekc.hashCode()
        result = 31 * result + podogrev_otop_l_sekc.hashCode()
        result = 31 * result + podogrev_otop_nazn.hashCode()
        result = 31 * result + podogrev_otop_marka.hashCode()
        result = 31 * result + d_napor_patrubok.hashCode()
        result = 31 * result + power_electro_engine.hashCode()
        result = 31 * result + chastota_vr_engine.hashCode()
        result = 31 * result + drossel_diafragma_d.hashCode()
        result = 31 * result + drossel_diafragma_mesto.hashCode()
        result = 31 * result + drossel_diafragma_tip_otop.hashCode()
        result = 31 * result + drossel_diafragma_cnt_stoyak.hashCode()
        result = 31 * result + pu_data.hashCode()
        result = 31 * result + pu_pover_lico.hashCode()
        result = 31 * result + pu_pover_pokaz.hashCode()
        result = 31 * result + pu_pover_rez.hashCode()
        result = 31 * result + balans_prinadl_obj.hashCode()
        result = 31 * result + balans_prin_dop.hashCode()
        result = 31 * result + gr_ekspl_otvetst.hashCode()
        result = 31 * result + gr_ekspl_otvetst_dop.hashCode()
        result = 31 * result + st_podkl_rub.hashCode()
        result = 31 * result + st_podkl_rub_nds.hashCode()
        result = 31 * result + podkl_dop_sved.hashCode()
        result = 31 * result + pred_pogash_dolg_num.hashCode()
        result = 31 * result + pred_pogash_dolg_date.hashCode()
        result = 31 * result + otkl_proizv.hashCode()
        result = 31 * result + otkl_pu_pokaz_do.hashCode()
        result = 31 * result + otkl_pu_pokaz_posle.hashCode()
        result = 31 * result + filial_name.hashCode()
        result = 31 * result + filial_address.hashCode()
        result = 31 * result + filial_tel.hashCode()
        result = 31 * result + act_poluchil.hashCode()
        result = 31 * result + nal_document.hashCode()
        result = 31 * result + dop_info.hashCode()
        result = 31 * result + ispolnitel.hashCode()
        result = 31 * result + dog_podkl_num.hashCode()
        result = 31 * result + dog_podkl_date.hashCode()
        result = 31 * result + mesto_karta.hashCode()
        result = 31 * result + uvedoml_otkl_num.hashCode()
        result = 31 * result + uvedoml_otkl_date.hashCode()
        result = 31 * result + prichina_otkaza.hashCode()
        result = 31 * result + otkaz_svidet_1.hashCode()
        result = 31 * result + otkaz_svidet_2.hashCode()
        result = 31 * result + pravo_sobstv.hashCode()
        result = 31 * result + uvedom_aktirov_num.hashCode()
        result = 31 * result + uvedom_aktirov_date.hashCode()
        result = 31 * result + predst_potrebit.hashCode()
        result = 31 * result + predst_potrebit_dover.hashCode()
        result = 31 * result + volume_obj.hashCode()
        result = 31 * result + square_obj.hashCode()
        result = 31 * result + q_sum.hashCode()
        result = 31 * result + so_q.hashCode()
        result = 31 * result + sw_q.hashCode()
        result = 31 * result + st_q.hashCode()
        result = 31 * result + gw_q.hashCode()
        result = 31 * result + nal_pu.hashCode()
        result = 31 * result + nal_aupr.hashCode()
        result = 31 * result + bezdog_sposob_num.hashCode()
        result = 31 * result + bezdog_ustanovleno.hashCode()
        result = 31 * result + bezdog_narushenie.hashCode()
        result = 31 * result + bezdog_pereraschet_s.hashCode()
        result = 31 * result + bezdog_pereraschet_po.hashCode()
        result = 31 * result + bezdog_predpis.hashCode()
        result = 31 * result + bezdog_obyasn.hashCode()
        result = 31 * result + bezdog_pretenz.hashCode()
        result = 31 * result + uslovie_podkl_num.hashCode()
        result = 31 * result + soglasov_proekte_num.hashCode()
        result = 31 * result + dopusk_s.hashCode()
        result = 31 * result + dopusk_po.hashCode()
        result = 31 * result + tel_spravki.hashCode()
        result = 31 * result + tel_dispetch.hashCode()
        result = 31 * result + org_ustanov_pu.hashCode()
        result = 31 * result + type_oto_prib.hashCode()
        result = 31 * result + schema_vkl_gvs.hashCode()
        result = 31 * result + schema_vkl_podogrev.hashCode()
        result = 31 * result + kolvo_sekc_1.hashCode()
        result = 31 * result + kolvo_sekc_1_l.hashCode()
        result = 31 * result + kolvo_sekc_2.hashCode()
        result = 31 * result + kolvo_sekc_2_l.hashCode()
        result = 31 * result + kolvo_kalorifer.hashCode()
        result = 31 * result + poverhnost_nagreva.hashCode()
        result = 31 * result + podkl_num.hashCode()
        result = 31 * result + q_max.hashCode()
        result = 31 * result + itog_text.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActFieldsInfo

        if (id_task != other.id_task) return false
        if (id_act != other.id_act) return false
        //if (is_signed != other.is_signed) return false
        if (kodp != other.kodp) return false
        if (kod_dog != other.kod_dog) return false
        if (kod_obj != other.kod_obj) return false
        if (num_act.replace("null", "") != other.num_act.replace("null", "")) return false
        if (dat_act.replace("null", "") != other.dat_act.replace("null", "")) return false
        if (payer_name.replace("null", "") != other.payer_name.replace("null", "")) return false
        if (adr_org.replace("null", "") != other.adr_org.replace("null", "")) return false
        if (fio_contact.replace("null", "") != other.fio_contact.replace("null", "")) return false
        if (tel_contact.replace("null", "") != other.tel_contact.replace("null", "")) return false
        if (filial_eso.replace("null", "") != other.filial_eso.replace("null", "")) return false
        if (fio_eso.replace("null", "") != other.fio_eso.replace("null", "")) return false
        if (tel_eso.replace("null", "") != other.tel_eso.replace("null", "")) return false
        if (list_obj.replace("null", "") != other.list_obj.replace("null", "")) return false
        if (name_obj.replace("null", "") != other.name_obj.replace("null", "")) return false
        if (num_obj.replace("null", "") != other.num_obj.replace("null", "")) return false
        if (adr_obj.replace("null", "") != other.adr_obj.replace("null", "")) return false
        if (ndog.replace("null", "") != other.ndog.replace("null", "")) return false
        if (dat_dog.replace("null", "") != other.dat_dog.replace("null", "")) return false
        if (otop_period.replace("null", "") != other.otop_period.replace("null", "")) return false
        if (sum_dolg.replace("null", "") != other.sum_dolg.replace("null", "")) return false
        if (remark_dog.replace("null", "") != other.remark_dog.replace("null", "")) return false
        if (nal_podp_doc.replace("null", "") != other.nal_podp_doc.replace("null", "")) return false
        if (opl_calcul.replace("null", "") != other.opl_calcul.replace("null", "")) return false
        if (osnov.replace("null", "") != other.osnov.replace("null", "")) return false
        if (city.replace("null", "") != other.city.replace("null", "")) return false
        if (shablon.replace("null", "") != other.shablon.replace("null", "")) return false
        if (name_dolzhn_contact.replace("null", "") != other.name_dolzhn_contact.replace("null", "")) return false
        if (name_dolzhn_eso.replace("null", "") != other.name_dolzhn_eso.replace("null", "")) return false
        if (period_dolg.replace("null", "") != other.period_dolg.replace("null", "")) return false
        if (name_st.replace("null", "") != other.name_st.replace("null", "")) return false
        if (name_mag.replace("null", "") != other.name_mag.replace("null", "")) return false
        if (name_tk.replace("null", "") != other.name_tk.replace("null", "")) return false
        if (name_aw.replace("null", "") != other.name_aw.replace("null", "")) return false
        if (inn_org.replace("null", "") != other.inn_org.replace("null", "")) return false
        if (kpp_org.replace("null", "") != other.kpp_org.replace("null", "")) return false
        if (nazn_name.replace("null", "") != other.nazn_name.replace("null", "")) return false
        if (nal_so.replace("null", "") != other.nal_so.replace("null", "")) return false
        if (nal_sw.replace("null", "") != other.nal_sw.replace("null", "")) return false
        if (nal_st.replace("null", "") != other.nal_st.replace("null", "")) return false
        if (nal_gv.replace("null", "") != other.nal_gv.replace("null", "")) return false
        if (director_tatenergo.replace("null", "") != other.director_tatenergo.replace("null", "")) return false
        if (volume_obj.replace("null", "") != other.volume_obj.replace("null", "")) return false
        if (so_q.replace("null", "") != other.so_q.replace("null", "")) return false
        if (sw_q.replace("null", "") != other.sw_q.replace("null", "")) return false
        if (st_q.replace("null", "") != other.st_q.replace("null", "")) return false
        if (gw_q.replace("null", "") != other.gw_q.replace("null", "")) return false
        if (q_sum.replace("null", "") != other.q_sum.replace("null", "")) return false
        if (dat != other.dat) return false
        if (nal_act_gidro.replace("null", "") != other.nal_act_gidro.replace("null", "")) return false

        /*if (purpose_text.replace("null", "") != other.purpose_text.replace("null", "")) return false
        if (fact_text.replace("null", "") != other.fact_text.replace("null", "")) return false
        if (id_manometr.replace("null", "") != other.id_manometr.replace("null", "")) return false
        if (id_aupr_so.replace("null", "") != other.id_aupr_so.replace("null", "")) return false
        if (id_aupr_sw.replace("null", "") != other.id_aupr_sw.replace("null", "")) return false
        if (id_aupr_gvs.replace("null", "") != other.id_aupr_gvs.replace("null", "")) return false
        if (id_aupr_sv.replace("null", "") != other.id_aupr_sv.replace("null", "")) return false
        if (sost_kip_str.replace("null", "") != other.sost_kip_str.replace("null", "")) return false
        if (id_sost_tube.replace("null", "") != other.id_sost_tube.replace("null", "")) return false
        if (id_sost_armatur.replace("null", "") != other.id_sost_armatur.replace("null", "")) return false
        if (id_sost_izol.replace("null", "") != other.id_sost_izol.replace("null", "")) return false
        if (sost_tube_str.replace("null", "") != other.sost_tube_str.replace("null", "")) return false
        if (id_sost_net.replace("null", "") != other.id_sost_net.replace("null", "")) return false
        if (sost_net_str.replace("null", "") != other.sost_net_str.replace("null", "")) return false
        if (id_sost_utepl.replace("null", "") != other.id_sost_utepl.replace("null", "")) return false
        if (sost_utepl_str.replace("null", "") != other.sost_utepl_str.replace("null", "")) return false
        if (id_nal_pasport.replace("null", "") != other.id_nal_pasport.replace("null", "")) return false
        if (id_nal_schema.replace("null", "") != other.id_nal_schema.replace("null", "")) return false
        if (id_nal_instr.replace("null", "") != other.id_nal_instr.replace("null", "")) return false
        if (nal_pasp_str.replace("null", "") != other.nal_pasp_str.replace("null", "")) return false
        if (id_nal_direct_connect.replace("null", "") != other.id_nal_direct_connect.replace("null", "")) return false
        if (nal_direct_connect.replace("null", "") != other.nal_direct_connect.replace("null", "")) return false
        if (comiss_post_gotov.replace("null", "") != other.comiss_post_gotov.replace("null", "")) return false
        if (pred_comiss.replace("null", "") != other.pred_comiss.replace("null", "")) return false
        if (zam_pred_gkh.replace("null", "") != other.zam_pred_gkh.replace("null", "")) return false
        if (podpisi.replace("null", "") != other.podpisi.replace("null", "")) return false
        if (director_t_dover_num.replace("null", "") != other.director_t_dover_num.replace("null", "")) return false
        if (director_t_dover_date.replace("null", "") != other.director_t_dover_date.replace("null", "")) return false
        if (zayavitel.replace("null", "") != other.zayavitel.replace("null", "")) return false
        if (zayavitel_dover.replace("null", "") != other.zayavitel_dover.replace("null", "")) return false
        if (podgotovka.replace("null", "") != other.podgotovka.replace("null", "")) return false
        if (podgotovka_proj_num.replace("null", "") != other.podgotovka_proj_num.replace("null", "")) return false
        if (podgotovka_proj_ispoln.replace("null", "") != other.podgotovka_proj_ispoln.replace("null", "")) return false
        if (podgotovka_proj_utvergden.replace("null", "") != other.podgotovka_proj_utvergden.replace("null", "")) return false
        if (net_inner_teplonositel.replace("null", "") != other.net_inner_teplonositel.replace("null", "")) return false
        if (net_inner_dp.replace("null", "") != other.net_inner_dp.replace("null", "")) return false
        if (net_inner_do.replace("null", "") != other.net_inner_do.replace("null", "")) return false
        if (net_inner_tip_kanal.replace("null", "") != other.net_inner_tip_kanal.replace("null", "")) return false
        if (net_inner_tube_type_p.replace("null", "") != other.net_inner_tube_type_p.replace("null", "")) return false
        if (net_inner_tube_type_o.replace("null", "") != other.net_inner_tube_type_o.replace("null", "")) return false
        if (net_inner_l.replace("null", "") != other.net_inner_l.replace("null", "")) return false
        if (net_inner_l_undeground.replace("null", "") != other.net_inner_l_undeground.replace("null", "")) return false
        if (net_inner_otstuplenie.replace("null", "") != other.net_inner_otstuplenie.replace("null", "")) return false
        if (energo_effect_object.replace("null", "") != other.energo_effect_object.replace("null", "")) return false
        if (nal_rezerv_istochnik.replace("null", "") != other.nal_rezerv_istochnik.replace("null", "")) return false
        if (nal_svyazi.replace("null", "") != other.nal_svyazi.replace("null", "")) return false
        if (vid_connect_system.replace("null", "") != other.vid_connect_system.replace("null", "")) return false
        if (elevator_num.replace("null", "") != other.elevator_num.replace("null", "")) return false
        if (elevator_diam.replace("null", "") != other.elevator_diam.replace("null", "")) return false
        if (podogrev_otop_num.replace("null", "") != other.podogrev_otop_num.replace("null", "")) return false
        if (podogrev_otop_kolvo_sekc.replace("null", "") != other.podogrev_otop_kolvo_sekc.replace("null", "")) return false
        if (podogrev_otop_l_sekc.replace("null", "") != other.podogrev_otop_l_sekc.replace("null", "")) return false
        if (podogrev_otop_nazn.replace("null", "") != other.podogrev_otop_nazn.replace("null", "")) return false
        if (podogrev_otop_marka.replace("null", "") != other.podogrev_otop_marka.replace("null", "")) return false
        if (d_napor_patrubok.replace("null", "") != other.d_napor_patrubok.replace("null", "")) return false
        if (power_electro_engine.replace("null", "") != other.power_electro_engine.replace("null", "")) return false
        if (chastota_vr_engine.replace("null", "") != other.chastota_vr_engine.replace("null", "")) return false
        if (drossel_diafragma_d.replace("null", "") != other.drossel_diafragma_d.replace("null", "")) return false
        if (drossel_diafragma_mesto.replace("null", "") != other.drossel_diafragma_mesto.replace("null", "")) return false
        if (drossel_diafragma_tip_otop.replace("null", "") != other.drossel_diafragma_tip_otop.replace("null", "")) return false
        if (drossel_diafragma_cnt_stoyak.replace("null", "") != other.drossel_diafragma_cnt_stoyak.replace("null", "")) return false
        if (pu_data.replace("null", "") != other.pu_data.replace("null", "")) return false
        if (pu_pover_lico.replace("null", "") != other.pu_pover_lico.replace("null", "")) return false
        if (pu_pover_pokaz.replace("null", "") != other.pu_pover_pokaz.replace("null", "")) return false
        if (pu_pover_rez.replace("null", "") != other.pu_pover_rez.replace("null", "")) return false
        if (balans_prinadl_obj.replace("null", "") != other.balans_prinadl_obj.replace("null", "")) return false
        if (balans_prin_dop.replace("null", "") != other.balans_prin_dop.replace("null", "")) return false
        if (gr_ekspl_otvetst.replace("null", "") != other.gr_ekspl_otvetst.replace("null", "")) return false
        if (gr_ekspl_otvetst_dop.replace("null", "") != other.gr_ekspl_otvetst_dop.replace("null", "")) return false
        if (st_podkl_rub.replace("null", "") != other.st_podkl_rub.replace("null", "")) return false
        if (st_podkl_rub_nds.replace("null", "") != other.st_podkl_rub_nds.replace("null", "")) return false
        if (podkl_dop_sved.replace("null", "") != other.podkl_dop_sved.replace("null", "")) return false
        if (pred_pogash_dolg_num.replace("null", "") != other.pred_pogash_dolg_num.replace("null", "")) return false
        if (pred_pogash_dolg_date.replace("null", "") != other.pred_pogash_dolg_date.replace("null", "")) return false
        if (otkl_proizv.replace("null", "") != other.otkl_proizv.replace("null", "")) return false
        if (otkl_pu_pokaz_do.replace("null", "") != other.otkl_pu_pokaz_do.replace("null", "")) return false
        if (otkl_pu_pokaz_posle.replace("null", "") != other.otkl_pu_pokaz_posle.replace("null", "")) return false
        if (filial_name.replace("null", "") != other.filial_name.replace("null", "")) return false
        if (filial_address.replace("null", "") != other.filial_address.replace("null", "")) return false
        if (filial_tel.replace("null", "") != other.filial_tel.replace("null", "")) return false
        if (act_poluchil.replace("null", "") != other.act_poluchil.replace("null", "")) return false
        if (nal_document.replace("null", "") != other.nal_document.replace("null", "")) return false
        if (dop_info.replace("null", "") != other.dop_info.replace("null", "")) return false
        if (ispolnitel.replace("null", "") != other.ispolnitel.replace("null", "")) return false
        if (dog_podkl_num.replace("null", "") != other.dog_podkl_num.replace("null", "")) return false
        if (dog_podkl_date.replace("null", "") != other.dog_podkl_date.replace("null", "")) return false
        if (mesto_karta.replace("null", "") != other.mesto_karta.replace("null", "")) return false
        if (uvedoml_otkl_num.replace("null", "") != other.uvedoml_otkl_num.replace("null", "")) return false
        if (uvedoml_otkl_date.replace("null", "") != other.uvedoml_otkl_date.replace("null", "")) return false
        if (prichina_otkaza.replace("null", "") != other.prichina_otkaza.replace("null", "")) return false
        if (otkaz_svidet_1.replace("null", "") != other.otkaz_svidet_1.replace("null", "")) return false
        if (otkaz_svidet_2.replace("null", "") != other.otkaz_svidet_2.replace("null", "")) return false
        if (pravo_sobstv.replace("null", "") != other.pravo_sobstv.replace("null", "")) return false
        if (uvedom_aktirov_num.replace("null", "") != other.uvedom_aktirov_num.replace("null", "")) return false
        if (uvedom_aktirov_date.replace("null", "") != other.uvedom_aktirov_date.replace("null", "")) return false
        if (predst_potrebit.replace("null", "") != other.predst_potrebit.replace("null", "")) return false
        if (predst_potrebit_dover.replace("null", "") != other.predst_potrebit_dover.replace("null", "")) return false
        if (volume_obj.replace("null", "") != other.volume_obj.replace("null", "")) return false
        if (square_obj.replace("null", "") != other.square_obj.replace("null", "")) return false
        if (q_sum.replace("null", "") != other.q_sum.replace("null", "")) return false
        if (so_q.replace("null", "") != other.so_q.replace("null", "")) return false
        if (sw_q.replace("null", "") != other.sw_q.replace("null", "")) return false
        if (st_q.replace("null", "") != other.st_q.replace("null", "")) return false
        if (gw_q.replace("null", "") != other.gw_q.replace("null", "")) return false
        if (nal_pu.replace("null", "") != other.nal_pu.replace("null", "")) return false
        if (nal_aupr.replace("null", "") != other.nal_aupr.replace("null", "")) return false
        if (bezdog_sposob_num.replace("null", "") != other.bezdog_sposob_num.replace("null", "")) return false
        if (bezdog_ustanovleno.replace("null", "") != other.bezdog_ustanovleno.replace("null", "")) return false
        if (bezdog_narushenie.replace("null", "") != other.bezdog_narushenie.replace("null", "")) return false
        if (bezdog_pereraschet_s.replace("null", "") != other.bezdog_pereraschet_s.replace("null", "")) return false
        if (bezdog_pereraschet_po.replace("null", "") != other.bezdog_pereraschet_po.replace("null", "")) return false
        if (bezdog_predpis.replace("null", "") != other.bezdog_predpis.replace("null", "")) return false
        if (bezdog_obyasn.replace("null", "") != other.bezdog_obyasn.replace("null", "")) return false
        if (bezdog_pretenz.replace("null", "") != other.bezdog_pretenz.replace("null", "")) return false
        if (uslovie_podkl_num.replace("null", "") != other.uslovie_podkl_num.replace("null", "")) return false
        if (soglasop_proekte_num.replace("null", "") != other.soglasop_proekte_num.replace("null", "")) return false
        if (dopusk_s.replace("null", "") != other.dopusk_s.replace("null", "")) return false
        if (dopusk_po.replace("null", "") != other.dopusk_po.replace("null", "")) return false
        if (tel_spravki.replace("null", "") != other.tel_spravki.replace("null", "")) return false
        if (tel_dispetch.replace("null", "") != other.tel_dispetch.replace("null", "")) return false
        if (org_ustanov_pu.replace("null", "") != other.org_ustanov_pu.replace("null", "")) return false
        if (type_oto_prib.replace("null", "") != other.type_oto_prib.replace("null", "")) return false
        if (schema_vkl_gvs.replace("null", "") != other.schema_vkl_gvs.replace("null", "")) return false
        if (schema_vkl_podogrev.replace("null", "") != other.schema_vkl_podogrev.replace("null", "")) return false
        if (kolvo_sekc_1.replace("null", "") != other.kolvo_sekc_1.replace("null", "")) return false
        if (kolvo_sekc_1_l.replace("null", "") != other.kolvo_sekc_1_l.replace("null", "")) return false
        if (kolvo_sekc_2.replace("null", "") != other.kolvo_sekc_2.replace("null", "")) return false
        if (kolvo_sekc_2_l.replace("null", "") != other.kolvo_sekc_2_l.replace("null", "")) return false
        if (kolvo_kalorifer.replace("null", "") != other.kolvo_kalorifer.replace("null", "")) return false
        if (poverhnost_nagreva.replace("null", "") != other.poverhnost_nagreva.replace("null", "")) return false
        if (podkl_num.replace("null", "") != other.podkl_num.replace("null", "")) return false
        if (q_max.replace("null", "") != other.q_max.replace("null", "")) return false
        if (itog_text.replace("null", "") != other.itog_text.replace("null", "")) return false*/

        return true
    }
}

// Информация об доп полях акта
// -----------------------------------------
class ActFieldsDopInfo(
    var dat: LocalDate? = null,
    var id_task: Int = 0, //  NOT NULL, NUMBER уникальный номер задания PK
    var id_act: Int = 0,
    var id_file: Int = 0,
    var npp: Int = 0,
    var num_obj: String = "",
    var name_obj: String = "",
    var address_obj: String = "",
    var god_zd: String = "",
    var nazn_name: String = "",
    var tvr: String = "",
    var pr_oto: String = "",
    var pr_sw: String = "",
    var volume: Double = 0.0,
    var square_total: Double = 0.0,
    var point_name: String = "",
    var etaz: String = "",
    var so_q: Double = 0.0,
    var sw_q: Double = 0.0,
    var st_q: Double = 0.0,
    var gw_qmax: Double = 0.0,
    var name_vodo: String = "",
    var nn_wpol: Double = 0.0,
    var tt_wpol: Double = 0.0,
    var nn_prib: Double = 0.0,
    var pr_rec: String = "",
    var pr_psusch: String = "",
    var pr_iz_st: String = "",
    var nom_uch: String = "",
    var tip_name: String = "",
    var pt_d: Double = 0.0,
    var pt_l: Double = 0.0,
    var name_pr_pt: String = "",
    var ot_d: Double = 0.0,
    var ot_l: Double = 0.0,
    var name_pr_ot: String = "",
    var uch_hgr: String = "",
    var pu_num: String = "",
    var pu_name: String = "",
    var pu_mesto: String = "",
    var pu_type: String = "",
    var pu_diam: String = "",
    var pu_kolvo: String = "",
    var pu_proba_mesto: String = "",
    var q_sum: String = "",
    var q_sum_max: String = "",
    var pu_srok_poverki: String = "",
    var pu_num_plomba: String = "",
    var pu_pokaz: String = "",
    var schema_prisoed_name: String = "",
    var schema_prisoed_kod: String = "",
    var is_signed: Int = 0
) : Serializable {

    override fun hashCode(): Int {
        var result = id_task
        result = 31 * result + id_act
        result = 31 * result + id_file
        result = 31 * result + npp
        result = 31 * result + num_obj.hashCode()
        result = 31 * result + name_obj.hashCode()
        result = 31 * result + address_obj.hashCode()
        result = 31 * result + god_zd.hashCode()
        result = 31 * result + nazn_name.hashCode()
        result = 31 * result + tvr.hashCode()
        result = 31 * result + pr_oto.hashCode()
        result = 31 * result + pr_sw.hashCode()
        result = 31 * result + volume.hashCode()
        result = 31 * result + square_total.hashCode()
        result = 31 * result + point_name.hashCode()
        result = 31 * result + etaz.hashCode()
        result = 31 * result + so_q.hashCode()
        result = 31 * result + sw_q.hashCode()
        result = 31 * result + st_q.hashCode()
        result = 31 * result + gw_qmax.hashCode()
        result = 31 * result + name_vodo.hashCode()
        result = 31 * result + nn_wpol.hashCode()
        result = 31 * result + tt_wpol.hashCode()
        result = 31 * result + nn_prib.hashCode()
        result = 31 * result + pr_rec.hashCode()
        result = 31 * result + pr_psusch.hashCode()
        result = 31 * result + pr_iz_st.hashCode()
        result = 31 * result + nom_uch.hashCode()
        result = 31 * result + tip_name.hashCode()
        result = 31 * result + pt_d.hashCode()
        result = 31 * result + pt_l.hashCode()
        result = 31 * result + name_pr_pt.hashCode()
        result = 31 * result + ot_d.hashCode()
        result = 31 * result + ot_l.hashCode()
        result = 31 * result + name_pr_ot.hashCode()
        result = 31 * result + uch_hgr.hashCode()
        result = 31 * result + pu_num.hashCode()
        result = 31 * result + pu_name.hashCode()
        result = 31 * result + pu_mesto.hashCode()
        result = 31 * result + pu_type.hashCode()
        result = 31 * result + pu_diam.hashCode()
        result = 31 * result + pu_kolvo.hashCode()
        result = 31 * result + pu_proba_mesto.hashCode()
        result = 31 * result + q_sum.hashCode()
        result = 31 * result + q_sum_max.hashCode()
        result = 31 * result + pu_srok_poverki.hashCode()
        result = 31 * result + pu_num_plomba.hashCode()
        result = 31 * result + pu_pokaz.hashCode()
        result = 31 * result + schema_prisoed_name.hashCode()
        result = 31 * result + schema_prisoed_kod.hashCode()
        result = 31 * result + (dat?.hashCode() ?: 0)
        result = 31 * result + is_signed
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ActFieldsDopInfo

        if (id_task != other.id_task) return false
        if (id_act != other.id_act) return false
        //if (npp != other.npp) return false
        if (num_obj.replace("null", "") != other.num_obj.replace("null", "")) return false
        if (name_obj.replace("null", "") != other.name_obj.replace("null", "")) return false
        if (address_obj.replace("null", "") != other.address_obj.replace("null", "")) return false
        if (god_zd.replace("null", "") != other.god_zd.replace("null", "")) return false
        if (nazn_name.replace("null", "") != other.nazn_name.replace("null", "")) return false
        if (tvr.replace("null", "") != other.tvr.replace("null", "")) return false
        if (pr_oto.replace("null", "") != other.pr_oto.replace("null", "")) return false
        if (pr_sw.replace("null", "") != other.pr_sw.replace("null", "")) return false
        if (volume != other.volume) return false
        if (square_total != other.square_total) return false
        if (point_name.replace("null", "") != other.point_name.replace("null", "")) return false
        if (etaz.replace("null", "") != other.etaz.replace("null", "")) return false
        if (so_q != other.so_q) return false
        if (sw_q != other.sw_q) return false
        if (st_q != other.st_q) return false
        if (gw_qmax != other.gw_qmax) return false
        if (name_vodo.replace("null", "") != other.name_vodo.replace("null", "")) return false
        if (nn_wpol != other.nn_wpol) return false
        if (tt_wpol != other.tt_wpol) return false
        if (nn_prib != other.nn_prib) return false
        if (pr_rec.replace("null", "") != other.pr_rec.replace("null", "")) return false
        if (pr_psusch.replace("null", "") != other.pr_psusch.replace("null", "")) return false
        if (pr_iz_st.replace("null", "") != other.pr_iz_st.replace("null", "")) return false
        if (nom_uch.replace("null", "") != other.nom_uch.replace("null", "")) return false
        if (tip_name.replace("null", "") != other.tip_name.replace("null", "")) return false
        if (pt_d != other.pt_d) return false
        if (pt_l != other.pt_l) return false
        if (name_pr_pt.replace("null", "") != other.name_pr_pt.replace("null", "")) return false
        if (ot_d != other.ot_d) return false
        if (ot_l != other.ot_l) return false
        if (name_pr_ot.replace("null", "") != other.name_pr_ot.replace("null", "")) return false
        if (uch_hgr.replace("null", "") != other.uch_hgr.replace("null", "")) return false
        if (pu_num.replace("null", "") != other.pu_num.replace("null", "")) return false
        if (pu_name.replace("null", "") != other.pu_name.replace("null", "")) return false
        if (pu_mesto.replace("null", "") != other.pu_mesto.replace("null", "")) return false
        if (pu_type.replace("null", "") != other.pu_type.replace("null", "")) return false
        if (pu_diam.replace("null", "") != other.pu_diam.replace("null", "")) return false
        if (pu_kolvo.replace("null", "") != other.pu_kolvo.replace("null", "")) return false
        if (pu_proba_mesto.replace("null", "") != other.pu_proba_mesto.replace("null", "")) return false
        if (q_sum.replace("null", "") != other.q_sum.replace("null", "")) return false
        if (q_sum_max.replace("null", "") != other.q_sum_max.replace("null", "")) return false
        if (pu_srok_poverki.replace("null", "") != other.pu_srok_poverki.replace("null", "")) return false
        if (pu_num_plomba.replace("null", "") != other.pu_num_plomba.replace("null", "")) return false
        if (pu_pokaz.replace("null", "") != other.pu_pokaz.replace("null", "")) return false
        if (schema_prisoed_name.replace("null", "") != other.schema_prisoed_name.replace("null", "")) return false
        if (schema_prisoed_kod.replace("null", "") != other.schema_prisoed_kod.replace("null", "")) return false
        if (dat != other.dat) return false

        return true
    }
}

// Информация о найденном объекте (для создания новой задачи)
// ----------------------------------------------------------
class SearchObjectInfo(
    var kod_dog: Int = 0,
    var kodp: Int = 0,
    var kod_obj: Int = 0,
    var kod_numobj: Int = 0,
    var ndog: String = "",
    var name: String = "",
    var adr: String = ""
)

// Информация об абоненте (договор теплоснабжения)
// -----------------------------------------------
class DogData(
    var id_task: Int = 0,
    var dat: LocalDate? = null,
    var kod_dog: Int = 0,
    var kodp: Int = 0,
    var name: String = "", // наименование абонента
    var inn: String = "", // инн
    var contact: String = "", // контактные данные
    var ndog: String = "", // номер договора
    var dat_dog: LocalDateTime = LocalDateTime.now(), // дата договора
    var dog_har: String = "", // Договорные нагрузки строкой
    var nal_pu: Int = 0, // наличие ПУ
    var last_nachisl: String = "", //последние начисления
    var last_opl: String = "",// последние оплаты
    var sum_dolg_total: String = "", //-- лицевая карта, сумма долга
    var remark_dog: String = "",
    var remark_rasch: String = "",
    var remark_ur: String = "",
    var remark_kontrol: String = "",
    var remark_tu: String = "",
    var pusk_tu: String = "",
    var otkl_tu: String = "",
    var listDogObjects: ArrayList<DogObject> = ArrayList(),
    var listDogTu: ArrayList<DogTu>? = ArrayList(),
    var listDogUu: ArrayList<DogUu>? = ArrayList(),
    var listDogUuSi: ArrayList<ArrayList<DogSiUu>>? = ArrayList()
) : Serializable

// Информация об объектах договора абонента
// ----------------------------------------
class DogObject(
    var kod_dog: Int = 0,
    var name: String = "",
    var adr: String = ""
)

// Информация о ТУ договора абонента
// ---------------------------------
class DogTu(
    var kod_dog: Int = 0,
    var nomer: Int = 0,
    var name: String = "",
    var so_q: Double = 0.0,
    var so_g: Double = 0.0,
    var sw_q: Double = 0.0,
    var sw_g: Double = 0.0,
    var st_q: Double = 0.0,
    var st_g: Double = 0.0,
    var gw_qmax: Double = 0.0,
    var gw_qsr: Double = 0.0,
    var name_tarif: String = ""
)

// Информация об УУ договора абонента
// ----------------------------------
class DogUu(
    var kod_uu: Int = 0,
    var kod_dog: Int = 0,
    var mesto_uu: String = "",
    var name: String = "",
    var time_uu: String = ""
)


// Информация о СИ УУ договора абонента
// --------------------------------------
class DogSiUu(
    var id_task: Int = 0,
    var dat: String = "",
    var npp: String = "", //Int = 0,
    var kod_uu: String = "", // Int = 0,
    var name_si: String = "",
    var mesto: String = "",
    var obozn_t: String = "",
    var name_tip: String = "",
    var nomer: String = "",
    var dim: String = "", // Int = 0,
    var izm: String = "",
    var data_pov: String = "", // LocalDate?,
    var int: String = "", // Int = 0,
    var data_pov_end: String = "", // LocalDate?,
    var per_chas_arx: String = "", // Double = 0.0,
    var per_sut_arx: String = "", // Double = 0.0,
    var n_greest: String = "",
    var work: String = "",
    var loss_press: String = "", // Double = 0.0,
    var data_out: String = "", // LocalDate?,
    var prim: String = ""
)

// Результат получения JSON данных с сервера
// ------------------------------------------------------------------------
class AsyncResultJson(var ok: Boolean, var error: String, val json: String)

// Результат получения JSON ActFieldsDop доп полей актов с сервера
// ------------------------------------------------------------------------------------------------
class AsyncResultActDop(var ok: Boolean, val error: String, val array: ArrayList<ActFieldsDopInfo>)

// Результат получения JSON ActFields полей актов с сервера
// ------------------------------------------------------------------------------------------------
class AsyncResultActFields(var ok: Boolean, val error: String, val array: ArrayList<ActFieldsInfo>)

// Результат получения JSON File файлов с сервера
// ---------------------------------------------------------------------------------------
class AsyncResultFiles(var ok: Boolean, var error: String, val array: ArrayList<FileInfo>)

// Результат получения JSON Task задач с сервера
// -----------------------------------------------------------------------------------
class AsyncResultTasks(var ok: Boolean, val error: String, val array: ArrayList<Task>)

// Результат получения Blob фоток с сервера
// -----------------------------------------------------------------------------
class AsyncResultBlob(var ok: Boolean, var error: String, val array: ByteArray?)

