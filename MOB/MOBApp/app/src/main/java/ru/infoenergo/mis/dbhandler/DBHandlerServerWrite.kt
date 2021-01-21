package ru.infoenergo.mis.dbhandler

import android.content.Context
import android.net.Uri
import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.*
import org.apache.http.HttpResponse
import org.apache.http.client.methods.HttpPost
import org.apache.http.impl.client.DefaultHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import ru.infoenergo.mis.R
import ru.infoenergo.mis.helpers.*
import java.io.ByteArrayOutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/** ******************************************** **/
/**          Отправка данных на сервер           **/
/** ******************************************** **/
class DBHandlerServerWrite constructor(var context: Context) {

    // Отправка на сервер всех данных по ID_INSPECTOR
    // **********************************************
    fun sendAllData(idInspector: Int): Pair <Boolean, String> {
        val dbRead = DbHandlerLocalRead(context, null)
        val dbWrite = DbHandlerLocalWrite(context, null)

        val minIdTaskLocal = DbHandlerLocalRead(context, null).getMinIdTask(idInspector, notInc = true)
        updateMinTaskIdAsync(idInspector, minIdTaskLocal)

        // Задачи
        // ----------------------------------------------------
        val allTasks = dbRead.getTaskList(idInspector)
        try {
            allTasks.forEach {
                when (it.status) {
                    // Созданную вручную задачу отправляем и меняем статус на "Отправлен инспектором"
                    0 -> {
                        if (addTaskAsync(it)) {
                            dbWrite.apply {
                                updateTaskStatus(it.id_task, 15)
                                updateTaskSend(it.id_task)
                            }
                        }
                    }
                    // Созданную вручную выполненную задачу делаем insert со статусом 0, потом update со статусом 12
                    -12 -> {
                        it.status = 0
                        if (addTaskAsync(it)) {
                            it.status = 12
                            if (updateTaskAsync(it))
                                dbWrite.apply {
                                    updateTaskStatus(it.id_task, 12)
                                    updateTaskSend(it.id_task)
                                }
                        }
                    }
                    // Остальные задачи просто update
                    else -> {
                        if (updateTaskAsync(it)) {
                            dbWrite.apply {
                                updateTaskSend(it.id_task)
                            }
                        }
                    }
                }
            }
            println("$TAG_OK tasks отправлены на сервер")
            // ------------------------------------------
        } catch (e: Exception) {
            println("$TAG_ERR tasks to server: ${e.message}")
        }

        val tasksIds = allTasks.joinToString(separator = ",", prefix = "(", postfix = ")") { "${it.id_task}" }
        // Файлы
        // ----------------------------------------------------------------------------
        val files = dbRead.getAllFiles(tasksIds)

        files.forEach {
            if (addFileAsync(it)) {
                dbWrite.updateFileSend(it.id_file)
            }
        }

        // Акты
        // ----------------------------------------------------------------------------
        //val actsFields = db.getAllActFields(tasksIds)

        dbRead.close()
        dbWrite.close()
        return Pair(true, tasksIds)
    }

    // update_acts_fields
    // Запись данных по актам
    // -------------------------
    fun updateActsFields(fields: ActFieldsInfo): Boolean {
        var params = ""

        if (fields.id_act in 11..13 || fields.id_act in arrayOf(16, 26))
            params = "p_id_task=${fields.id_task}&p_id_act=${fields.id_act}" +
                    "&p_purpose_text=${fields.purpose_text}&p_num_act=${fields.num_act}" +
                    "&p_dat_act=${fields.dat_act}&p_payer_name=${fields.payer_name}&p_adr_org=${fields.adr_org}" +
                    "&p_fio_contact=${fields.fio_contact}&p_tel_contact=${fields.tel_contact}&p_filial_eso=${fields.filial_eso}" +
                    "&p_fio_eso=${fields.fio_eso}&p_tel_eso=${fields.tel_eso}&p_list_obj=${fields.list_obj}" +
                    "&p_name_obj=${fields.name_obj}&p_num_obj=${fields.num_obj}&p_adr_obj=${fields.adr_obj}" +
                    "&p_ndog=${fields.ndog}&p_dat_dog=${fields.dat_dog}&p_otop_period=${fields.otop_period}" +
                    "&p_sum_dolg=${fields.sum_dolg}&p_remark_dog=${fields.remark_dog}&p_nal_podp_doc=${fields.nal_podp_doc}" +
                    "&p_opl_calcul=${fields.opl_calcul}&p_osnov=${fields.osnov}&p_city=${fields.city}" +
                    "&p_shablon=${fields.shablon}&p_name_dolzhn_contact=${fields.name_dolzhn_contact}" +
                    "&p_name_dolzhn_eso=${fields.name_dolzhn_eso}&p_period_dolg=${fields.period_dolg}&p_name_st=${fields.name_st}" +
                    "&p_name_mag=${fields.name_mag}&p_name_tk=${fields.name_tk}&p_name_aw=${fields.name_aw}" +
                    "&p_inn_org=${fields.inn_org}&p_kpp_org=${fields.kpp_org}&p_nazn_name=${fields.nazn_name}" +
                    "&p_nal_so=${fields.nal_so}&p_nal_sw=${fields.nal_sw}&p_nal_st=${fields.nal_st}" +
                    "&p_nal_gv=${fields.nal_gv}&p_podgotovka=${fields.podgotovka}&p_itog_text=${fields.itog_text}" +
                    "&p_podpisi=${fields.podpisi}&p_is_signed=${fields.is_signed}" +
                    "&p_director_tatenergo=${fields.director_tatenergo}&p_volume_obj=${fields.volume_obj}" +
                    "&p_q_sum=${fields.q_sum}&p_so_q=${fields.so_q}&p_sw_q=${fields.sw_q}" +
                    "&p_st_q=${fields.st_q}&p_gw_q=${fields.gw_q}"
        else
            params = "p_id_task=${fields.id_task}&p_id_act=${fields.id_act}&p_is_signed=${fields.is_signed}" +
                    "&p_purpose_text=${fields.purpose_text}&p_fact_text=${fields.fact_text}&p_num_act=${fields.num_act}" +
                    "&p_dat_act=${fields.dat_act}&p_payer_name=${fields.payer_name}&p_adr_org=${fields.adr_org}" +
                    "&p_fio_contact=${fields.fio_contact}&p_tel_contact=${fields.tel_contact}&p_filial_eso=${fields.filial_eso}" +
                    "&p_fio_eso=${fields.fio_eso}&p_tel_eso=${fields.tel_eso}&p_list_obj=${fields.list_obj}" +
                    "&p_name_obj=${fields.name_obj}&p_num_obj=${fields.num_obj}&p_adr_obj=${fields.adr_obj}" +
                    "&p_ndog=${fields.ndog}&p_dat_dog=${fields.dat_dog}&p_otop_period=${fields.otop_period}" +
                    "&p_id_manometr=${fields.id_manometr}&p_id_aupr_so=${fields.id_aupr_so}&p_id_aupr_sw=${fields.id_aupr_sw}" +
                    "&p_id_aupr_gvs=${fields.id_aupr_gvs}&p_id_aupr_sv=${fields.id_aupr_sv}&p_sost_kip_str=${fields.sost_kip_str}" +
                    "&p_nal_act_gidro=${fields.nal_act_gidro}&p_id_sost_tube=${fields.id_sost_tube}&p_id_sost_armatur=${fields.id_sost_armatur}" +
                    "&p_id_sost_izol=${fields.id_sost_izol}&p_sost_tube_str=${fields.sost_tube_str}&p_id_sost_net=${fields.id_sost_net}" +
                    "&p_sost_net_str=${fields.sost_net_str}&p_id_sost_utepl=${fields.id_sost_utepl}&p_sost_utepl_str=${fields.sost_utepl_str}" +
                    "&p_id_nal_pasport=${fields.id_nal_pasport}&p_id_nal_schema=${fields.id_nal_schema}&p_id_nal_instr=${fields.id_nal_instr}" +
                    "&p_nal_pasp_str=${fields.nal_pasp_str}&p_id_nal_direct_connect=${fields.id_nal_direct_connect}&p_nal_direct_connect=${fields.nal_direct_connect}" +
                    "&p_sum_dolg=${fields.sum_dolg}&p_remark_dog=${fields.remark_dog}&p_nal_podp_doc=${fields.nal_podp_doc}" +
                    "&p_opl_calcul=${fields.opl_calcul}&p_osnov=${fields.osnov}&p_city=${fields.city}" +
                    "&p_comiss_post_gotov=${fields.comiss_post_gotov}&p_shablon=${fields.shablon}&p_name_dolzhn_contact=${fields.name_dolzhn_contact}" +
                    "&p_name_dolzhn_eso=${fields.name_dolzhn_eso}&p_period_dolg=${fields.period_dolg}&p_name_st=${fields.name_st}" +
                    "&p_name_mag=${fields.name_mag}&p_name_tk=${fields.name_tk}&p_name_aw=${fields.name_aw}" +
                    "&p_inn_org=${fields.inn_org}&p_kpp_org=${fields.kpp_org}&p_nazn_name=${fields.nazn_name}" +
                    "&p_nal_so=${fields.nal_so}&p_nal_sw=${fields.nal_sw}&p_nal_st=${fields.nal_st}" +
                    "&p_nal_gv=${fields.nal_gv}&p_pred_comiss=${fields.pred_comiss}&p_zam_pred_gkh=${fields.zam_pred_gkh}" +
                    "&p_podpisi=${fields.podpisi}&p_director_tatenergo=${fields.director_tatenergo}&p_director_t_dover_num=${fields.director_t_dover_num}" +
                    "&p_zayavitel=${fields.zayavitel}&p_zayavitel_dover=${fields.zayavitel_dover}" +
                    "&p_podgotovka=${fields.podgotovka}&p_podgotovka_proj_num=${fields.podgotovka_proj_num}" +
                    "&p_podgotovka_proj_ispoln=${fields.podgotovka_proj_ispoln}&p_podgotovka_proj_utvergden=${fields.podgotovka_proj_utvergden}" +
                    "&p_net_inner_teplonositel=${fields.net_inner_teplonositel}&p_net_inner_dp=${fields.net_inner_dp}" +
                    "&p_net_inner_do=${fields.net_inner_do}&p_net_inner_tip_kanal=${fields.net_inner_tip_kanal}" +
                    "&p_net_inner_tube_type_p=${fields.net_inner_tube_type_p}&p_net_inner_tube_type_o=${fields.net_inner_tube_type_o}" +
                    "&p_net_inner_l=${fields.net_inner_l}&p_net_inner_l_undeground=${fields.net_inner_l_undeground}" +
                    "&p_net_inner_otstuplenie=${fields.net_inner_otstuplenie}&p_energo_effect_object=${fields.energo_effect_object}" +
                    "&p_nal_rezerv_istochnik=${fields.nal_rezerv_istochnik}&p_nal_svyazi=${fields.nal_svyazi}&p_vid_connect_system=${fields.vid_connect_system}" +
                    "&p_elevator_num=${fields.elevator_num}&p_elevator_diam=${fields.elevator_diam}&p_podogrev_otop_num=${fields.podogrev_otop_num}" +
                    "&p_podogrev_otop_kolvo_sekc=${fields.podogrev_otop_kolvo_sekc}&p_podogrev_otop_l_sekc=${fields.podogrev_otop_l_sekc}" +
                    "&p_podogrev_otop_nazn=${fields.podogrev_otop_nazn}&p_podogrev_otop_marka=${fields.podogrev_otop_marka}" +
                    "&p_d_napor_patrubok=${fields.d_napor_patrubok}&p_power_electro_engine=${fields.power_electro_engine}" +
                    "&p_chastota_vr_engine=${fields.chastota_vr_engine}&p_drossel_diafragma_d=${fields.drossel_diafragma_d}" +
                    "&p_drossel_diafragma_mesto=${fields.drossel_diafragma_mesto}&p_drossel_diafragma_tip_otop=${fields.drossel_diafragma_tip_otop}" +
                    "&p_drossel_diafragma_cnt_stoyak=${fields.drossel_diafragma_cnt_stoyak}&p_pu_data=${fields.pu_data}" +
                    "&p_pu_pover_lico=${fields.pu_pover_lico}&p_pu_pover_pokaz=${fields.pu_pover_pokaz}&p_pu_pover_rez=${fields.pu_pover_rez}" +
                    "&p_balans_prinadl_obj=${fields.balans_prinadl_obj}&p_balans_prin_dop=${fields.balans_prin_dop}" +
                    "&p_gr_ekspl_otvetst=${fields.gr_ekspl_otvetst}&p_gr_ekspl_otvetst_dop=${fields.gr_ekspl_otvetst_dop}" +
                    "&p_st_podkl_rub=${fields.st_podkl_rub}&p_st_podkl_rub_nds=${fields.st_podkl_rub_nds}&p_podkl_dop_sved=${fields.podkl_dop_sved}" +
                    "&p_pred_pogash_dolg_num=${fields.pred_pogash_dolg_num}&p_pred_pogash_dolg_date=${fields.pred_pogash_dolg_date}" +
                    "&p_otkl_proizv=${fields.otkl_proizv}&p_otkl_pu_pokaz_do=${fields.otkl_pu_pokaz_do}&p_otkl_pu_pokaz_posle=${fields.otkl_pu_pokaz_posle}" +
                    "&p_filial_name=${fields.filial_name}&p_filial_address=${fields.filial_address}&p_filial_tel=${fields.filial_tel}" +
                    "&p_act_poluchil=${fields.act_poluchil}&p_nal_document=${fields.nal_document}&p_dop_info=${fields.dop_info}" +
                    "&p_ispolnitel=${fields.ispolnitel}&p_dog_podkl_num=${fields.dog_podkl_num}&p_dog_podkl_date=${fields.dog_podkl_date}" +
                    "&p_mesto_karta=${fields.mesto_karta}&p_uvedoml_otkl_num=${fields.uvedoml_otkl_num}&p_uvedoml_otkl_date=${fields.uvedoml_otkl_date}" +
                    "&p_prichina_otkaza=${fields.prichina_otkaza}&p_otkaz_svidet_1=${fields.otkaz_svidet_1}&p_otkaz_svidet_2=${fields.otkaz_svidet_2}" +
                    "&p_pravo_sobstv=${fields.pravo_sobstv}&p_uvedom_aktirov_num=${fields.uvedom_aktirov_num}&p_uvedom_aktirov_date=${fields.uvedom_aktirov_date}" +
                    "&p_predst_potrebit=${fields.predst_potrebit}&p_predst_potrebit_dover=${fields.predst_potrebit_dover}&p_volume_obj=${fields.volume_obj}" +
                    "&p_square_obj=${fields.square_obj}&p_q_sum=${fields.q_sum}&p_so_q=${fields.so_q}&p_sw_q=${fields.sw_q}" +
                    "&p_st_q=${fields.st_q}&p_gw_q=${fields.gw_q}&p_nal_pu=${fields.nal_pu}&p_nal_aupr=${fields.nal_aupr}" +
                    "&p_bezdog_sposob_num=${fields.bezdog_sposob_num}&p_bezdog_ustanovleno=${fields.bezdog_ustanovleno}" +
                    "&p_bezdog_narushenie=${fields.bezdog_narushenie}&p_bezdog_pereraschet_s=${fields.bezdog_pereraschet_s}" +
                    "&p_bezdog_pereraschet_po=${fields.bezdog_pereraschet_po}&p_bezdog_predpis=${fields.bezdog_predpis}" +
                    "&p_bezdog_obyasn${fields.bezdog_obyasn}=&p_bezdog_pretenz=${fields.bezdog_pretenz}" +
                    "&p_uslovie_podkl_num=${fields.uslovie_podkl_num}&p_soglasop_proekte_num=${fields.soglasov_proekte_num}" +
                    "&p_dopusk_s=${fields.dopusk_s}&p_dopusk_po=${fields.dopusk_po}&p_tel_spravki=${fields.tel_spravki}" +
                    "&p_tel_dispetch=${fields.tel_dispetch}&p_org_ustanov_pu=${fields.org_ustanov_pu}&p_type_oto_prib=${fields.type_oto_prib}" +
                    "&p_schema_vkl_gvs=${fields.schema_vkl_gvs}&p_schema_vkl_podogrev=${fields.schema_vkl_podogrev}" +
                    "&p_kolvo_sekc_1=${fields.kolvo_sekc_1}&p_kolvo_sekc_1_l=${fields.kolvo_sekc_1_l}" +
                    "&p_kolvo_sekc_2=${fields.kolvo_sekc_2}&p_kolvo_sekc_2_l=${fields.kolvo_sekc_2_l}" +
                    "&p_kolvo_kalorifer=${fields.kolvo_kalorifer}&p_poverhnost_nagreva=${fields.poverhnost_nagreva}" +
                    "&p_podkl_num=${fields.podkl_num}&p_q_max=${fields.q_max}&p_itog_text=${fields.itog_text}"
        return try {
            val del = sendDataToServer("update_acts_fields", params)
            if (del.ok) {
                true
            } else {
                if (del.error.isNotEmpty()) {
                    println("$TAG_ERR updateActsFields: ${del.error}")
                }
                false
            }
        } catch (e: Exception) {
            println("$TAG_ERR updateActsFields: ${e.message}")
            false
        }
    }

    // Добавление файла к заданию
    // -------------------------------------------------
    fun addFileAsync(file: FileInfo): Boolean {
        return try {
            sendDataBlobToServer(file)
        } catch (e: Exception) {
            println("$TAG_ERR addFile (): ${e.message}")
            false
        }
    }


    //  Добавление дополнительной информации по актам
    // ------------------------------------------------------------------------------
    fun insertDopActFields(idTask: Int, idAct: Int, dop: ActFieldsDopInfo): Boolean {
        val params = "p_id_task=$idTask&p_id_act=$idAct" +
                "&p_num_obj=${dop.num_obj} &p_name_obj=${dop.name_obj}&p_address_obj=${dop.address_obj}" +
                "&p_god_zd=${dop.god_zd}&p_nazn_name=${dop.nazn_name}&p_tvr=${dop.tvr}" +
                "&p_pr_oto=${dop.pr_oto}&p_pr_sw=${dop.pr_sw}&p_volume=${dop.volume}" +
                "&p_square_total=${dop.square_total}&p_point_name=${dop.point_name}&p_etaz=${dop.etaz}" +
                "&p_so_q=${dop.so_q}&p_sw_q=${dop.sw_q}&p_st_q=${dop.st_q}&p_gw_qmax=${dop.gw_qmax}" +
                "&p_name_vodo=${dop.name_vodo}&p_nn_wpol=${dop.nn_wpol}&p_tt_wpol=${dop.tt_wpol}" +
                "&p_nn_prib=${dop.nn_prib}&p_pr_rec=${dop.pr_rec}&p_pr_psusch=${dop.pr_psusch}" +
                "&p_pr_iz_st=${dop.pr_iz_st}&p_nom_uch=${dop.nom_uch}&p_tip_name=${dop.tip_name}" +
                "&p_pt_d=${dop.pt_d}&p_pt_l=${dop.pt_l}&p_name_pr_pt=${dop.name_pr_pt}" +
                "&p_ot_d=${dop.ot_d}&p_ot_l=${dop.ot_l}&p_name_pr_ot=${dop.name_pr_ot}&p_uch_hgr=${dop.uch_hgr}" +
                "&p_pu_num=${dop.pu_num}&p_pu_name=${dop.pu_name}&p_pu_mesto=${dop.pu_mesto}" +
                "&p_pu_type=${dop.pu_type}&p_pu_diam=${dop.pu_diam}&p_pu_kolvo=${dop.pu_kolvo}" +
                "&p_pu_proba_mesto=${dop.pu_proba_mesto}&p_q_sum=${dop.q_sum}&p_q_sum_max=${dop.q_sum_max}" +
                "&p_pu_srok_poverki=${dop.pu_srok_poverki}&p_pu_num_plomba=${dop.pu_num_plomba}&p_pu_pokaz=${dop.pu_pokaz}" +
                "&p_schema_prisoed_name=${dop.schema_prisoed_name}&p_schema_prisoed_kod=${dop.schema_prisoed_kod}"
        return try {
            val del = sendDataToServer("insert_dop_act_fields", params)
            if (del.ok) {
                true
            } else {
                if (del.error.isNotEmpty()) {
                    println("$TAG_ERR insertDopActFields: ${del.error}")
                }
                false
            }
        } catch (e: Exception) {
            println("$TAG_ERR insertDopActFields: ${e.message}")
            false
        }
    }

    //  Удаление доп. полей акта перед обновлением
    //  (таблиц всяких, сначала удаляем то что было)
    // -------------------------------------------------------
    fun deleteDopActFields(idTask: Int, idAct: Int): Boolean {
        val params = "p_id_task=$idTask&p_id_act=$idAct"
        return try {
            val del = sendDataToServer("delete_dop_act_fields", params)
            if (del.ok) {
                true
            } else {
                if (del.error.isNotEmpty()) {
                    println("$TAG_ERR deleteDopActFields: ${del.error}")
                }
                false
            }
        } catch (e: Exception) {
            println("$TAG_ERR deleteDopActFields: ${e.message}")
            false
        }
    }

    // Обновление полей задачи (в тч статус на Выполнен)
    // *************************************************
    private fun updateTaskAsync(task: Task): Boolean {
        val params =
            "p_id_task=${task.id_task}&p_purpose=${task.purpose}&p_prim=${task.prim}&p_status=${task.status}&" +
                    "p_kod_emp_podp=${task.kod_emp_podp}"
        return try {
            val objs = sendDataToServer("update_task", params)
            if (objs.ok) {
                true
            } else {
                if (objs.error.isNotEmpty()) {
                    println("$TAG_ERR update_task: ${objs.error}")
                }
                false
            }
        } catch (e: Exception) {
            println("$TAG_ERR update_task: ${e.message}")
            false
        }
    }

    // Обновить min_task_id для инспектора
    // *************************************************************
    fun updateMinTaskIdAsync(idInspector: Int, minTaskId: Int): Boolean {
        val searchString = "p_id_inspector=${idInspector}&p_min_task_id=${minTaskId}"
        return try {
            val objs = sendDataToServer("update_min_task_id", searchString)
            if (objs.ok) {
                true
            } else {
                if (objs.error.isNotEmpty()) {
                    println("$TAG_ERR updateMinTaskId: ${objs.error}")
                }
                false
            }
        } catch (e: Exception) {
            println("$TAG_ERR updateMinTaskId: ${e.message}")
            false
        }
    }

    // Создание задания (найдено в поисковике)
    // **************************************************************************************************
    fun createTask(objectInfo: SearchObjectInfo, idInspector: Int, purpose: Long, idTask: Int): Boolean {
        val ttime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        val searchString = "p_id_inspector=$idInspector&p_kod_dog=${objectInfo.kod_dog}&" +
                "p_purpose=$purpose&p_kod_obj=${objectInfo.kod_obj}&" +
                "p_kod_numobj=${objectInfo.kod_numobj}&p_ttime=${ttime}&" +
                "p_id_task=$idTask"
        return try {
            val objs = sendDataToServer("create_task", searchString)
            if (objs.ok) {
                true
            } else {
                if (objs.error.isNotEmpty()) {
                    println("$TAG_ERR createTask: ${objs.error}")
                }
                false
            }
        } catch (e: Exception) {
            println("$TAG_ERR createTask: ${e.message}")
            false
        }
    }

    // Добавление задания на сервер (вручную)
    // *********************************************************
    private fun addTaskAsync(task: Task): Boolean {
        val ttime = if (task.ttime != null)
            task.ttime!!.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        else
            LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
        val params = "p_adr=${task.address}&p_city=${task.city}&p_street=${task.street}&p_house=${task.house}&" +
                "p_nd=${task.nd}&p_purpose=${task.purpose}&p_prim=${task.prim}&p_ttime=$ttime&" +
                "p_id_inspector=${task.id_inspector}&p_id_task=${task.id_task}&p_kod_dog=${task.kod_dog}&" +
                "p_kodp=${task.kodp}&p_kod_obj=${task.kod_obj}&p_kod_numobj=${task.kod_numobj}"
        return try {
            val objs = sendDataToServer("add_task", params)
            if (objs.ok) {
                true
            } else {
                if (objs.error.isNotEmpty()) {
                    println("$TAG_ERR addTaskAsync (id ${task.id_task}): ${objs.error}")
                }
                false
            }
        } catch (e: Exception) {
            println("$TAG_ERR addTaskAsync (id ${task.id_task}): ${e.message}")
            false
        }
    }

    // Сообщить на сервер, что задание получено устройством
    // ****************************************************
    fun acceptTask(idTask: Int): Boolean {
        try {
            val objs = sendDataToServer(
                "accept_task",
                "p_id_task=$idTask"
            )
            return if (objs.ok) {
                true
            } else {
                if (objs.error.isNotEmpty()) {
                    println("$TAG_ERR acceptTask: ${objs.error}")
                }
                false
            }
        } catch (e: Exception) {
            println("$TAG_ERR acceptTask: ${e.message}")
            return false
        }
    }

    // Сообщить на сервер, что задание отклонено устройством
    // *****************************************************
    fun refuseTask(idTask: Int): Boolean {
        try {
            val objs = sendDataToServer(
                "refuse_task",
                "p_id_task=$idTask"
            )
            return if (objs.ok) {
                true
            } else {
                if (objs.error.isNotEmpty()) {
                    println("$TAG_ERR acceptTask: ${objs.error}")
                }
                false
            }
        } catch (e: Exception) {
            println("$TAG_ERR acceptTask: ${e.message}")
            return false
        }
    }

    // Отправка email подписанного акта (перед этим скидывание файлов по задаче)
    // ****************************************************************************************
    fun sendActAsync(file: FileInfo): AsyncResultJson {
        return try {
            val params =
                "pi_id_task=${file.id_task}&pi_id_act=${file.id_act}&pi_npp=${file.npp}&pi_filename=${file.filename}&email_to=" +
                        "${file.email_client}&subject=Подписанные документы по результатам обхода испектора"
            sendDataToServer("send_email_kod", params)
        } catch (e: Exception) {
            println("$TAG_ERR sendCodeEmailAsync: ${e.message}")

            AsyncResultJson(false, "Произошла ошибка при отправке email: ${e.message}", "")
        }
    }

    // Отправка email подписанного акта (перед этим скидывание файлов по задаче)
    // ****************************************************************************************
    fun sendCodeEmailAsync(email: String, code: String): AsyncResultJson {
        return try {
            val params = "email_to=$email&kod=$code"
            sendDataToServer("send_email", params)
        } catch (e: Exception) {
            println("$TAG_ERR sendActAsync: ${e.message}")

            AsyncResultJson(false, "Произошла ошибка при отправке кода на email: ${e.message}", "")
        }
    }
    // Отправка данных на сервер
    // Передаём имя функции и список параметров
    // ******************************************************************************************
    private fun sendDataToServer(functionName: String, functionParameters: String): AsyncResultJson {
        var path =
                "${context.getString(R.string.server_uri)}:${context.getString(R.string.server_port)}/${context.getString(R.string.server_path_set)}/${functionName}"

        if (functionParameters.isNotEmpty())
        //path += "?" + URLEncoder.encode(functionParameters ,"UTF-8")
            path += "?" + functionParameters.trim().replace(" ", "%20")

        val b = Uri.parse(path).buildUpon().build()

        val response: HttpResponse
        var json = ""
        try {

            val request = HttpPost(b.toString())
            response = DefaultHttpClient().execute(request)

            val statusLine = response.statusLine
            when (statusLine.statusCode) {
                200 -> {
                    val entity = response.entity
                    val inputStream = entity.content
                    val outputStream = ByteArrayOutputStream()
                    val buffer = ByteArray(1024)

                    var len: Int = inputStream.read(buffer)
                    while (len > 0) {
                        outputStream.write(buffer, 0, len)
                        len = inputStream.read(buffer)
                    }

                    json = outputStream.toString().replace("\"", "")

                    outputStream.close()
                    inputStream.close()

                    println("$TAG_OK send ${functionName}: Status code: ${statusLine.statusCode}")
                    return AsyncResultJson(true, "Status code: ${statusLine.statusCode}", json)
                }
                else -> {
                    // Проверим, не пришла ли ошибка с неизвестным нам статусом
                    var errmsg = ""
                    val mapper = ObjectMapper()
                    val rootNode = mapper.readTree(json)

                    if (rootNode.elements().hasNext()) {
                        val element = rootNode.elements().next()
                        if (element.has("errmsg")) {
                            errmsg = element["errmsg"]!!.asText()
                        }
                    }

                    println("$TAG_OK send ${functionName}: Status code: ${statusLine.statusCode}")
                    return AsyncResultJson(
                        false,
                        "Status code: ${statusLine.statusCode} ${if (errmsg.isNotEmpty()) "; errmsg: $errmsg" else ""}",
                        json
                    )
                }
            }
        } catch (e: Exception) {
            println("$TAG_ERR send ${functionName}: ${e.message}")
            return AsyncResultJson(false, "Ошибка: ${e.message}", json)
        }
    }

    private fun sendDataBlobToServer(file: FileInfo): Boolean {
        val path =
            "${context.getString(R.string.server_uri)}:${context.getString(R.string.server_port)}/" +
                    "${context.getString(R.string.server_path_set)}/"

        val uri = Uri.parse(path).buildUpon().build().toString()

        var res = false
        try {

            val client = OkHttpClient.Builder().build()
            val retrofit = Retrofit.Builder()
                .baseUrl(uri)
                .client(client)
                .build()
            val apiService = retrofit.create(ApiService::class.java)

            val reqFile: RequestBody = RequestBody.create(
                MediaType.parse(
                    if (file.filename.takeLast(4) == ".pdf") "application/pdf" else "image/*"
                ), file.filedata!!
            )
            val body = MultipartBody.Part.createFormData("upload", file.filename, reqFile)
            val name = RequestBody.create(MediaType.parse("text/plain"), "upload")

            val dat = if (file.date_send_to_client != null)
                file.date_send_to_client!!.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            else
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            //dat = dat.replace(" ", "%20")

            val req = apiService.postImage(
                body, name, file.id_task.toString(), file.is_signed.toString(),
                file.paper.toString(), dat, file.email_client, file.id_act.toString(), file.npp.toString()
            )

            val y = req.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.code() == 200) {
                        println("$TAG_OK: файл ${file.id_file}/task ${file.id_task} отправлен. response.code:${response.code()} ")
                    } else {
                        println("$TAG_ERR: файл ${file.id_file}/task ${file.id_task} отправлен response.code:${response.code()}")
                    }
                    DbHandlerLocalWrite(context, null).updateFileSend(file.id_file)
                    res = true
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable?) {
                    println("$TAG_ERR: файл ${file.id_file}/task ${file.id_task} НЕ отправлен; ${t!!.message} ")
                    res = false
                }
            })
            return res

        } catch (e: Exception) {
            res = false
            println("$TAG_ERR: файл ${file.id_file}/task ${file.id_task} e.message: ${e.message} ")
            return res
        }
    }
}

