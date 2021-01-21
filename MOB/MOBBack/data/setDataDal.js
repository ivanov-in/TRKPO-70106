const poolOra = require("./db_pool");
let oracledb = require('oracledb');


async function updateMinTaskIdDal(connection, id_ins, min_task_id) {
    let result;
    let ora_status = 0;
    try {
        result = await connection.execute(`BEGIN MIS_MOB.mis_mobile_w.update_min_task_id(
                     :p_id_inspector,
                     :p_min_task_id,
                     :status,
                     :errmsg); END;`,
            {
                p_id_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_ins},
                p_min_task_id: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: min_task_id},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_INOUT, val: ora_status},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    ora_status = result.outBinds.status;
    let errmsg = "OK";
    if (ora_status != 0) {
        errmsg = result.outBinds.errmsg;
    }

    return errmsg;

};

async function createTaskDal(connection, id_ins, kod_dog, purpose, kod_obj, kod_numobj, ttime, id_task) {
    let result;
    let ora_status = 0;
    try {
        result = await connection.execute(`BEGIN MIS_MOB.mis_mobile_w.create_task(
                     :p_id_inspector,
                     :p_id_task,
                     :p_kod_dog,
                     :p_purpose,
                     :p_kod_obj,
                     :p_kod_numobj,
                     :p_ttime,
                     :status,
                     :errmsg); END;`,
            {
                p_id_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_ins},
                p_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_task},
                p_kod_dog: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_dog},
                p_purpose: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: purpose},
                p_kod_obj: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_obj},
                p_kod_numobj: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_numobj},
                p_ttime: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: ttime},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_INOUT, val: ora_status},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    ora_status = result.outBinds.status;
    let errmsg = "OK";
    if (ora_status != 0) {
        errmsg = result.outBinds.errmsg;
    }

    return errmsg;

};

async function addTaskDal(connection, adr, city, street, house, nd, purpose, prim, ttime, id_ins, id_task, kod_dog, kodp, kod_obj, kod_numobj) {
    let result;
    let ora_status = 0;
    try {
        result = await connection.execute(`BEGIN MIS_MOB.mis_mobile_w.add_task(
                     :p_adr,
                     :p_city,
                     :p_street,
                     :p_house,
                     :p_nd,
                     :p_purpose,
                     :p_prim,
                     :p_ttime,
                     :p_id_inspector,
                     :p_id_task,
                     :p_kod_dog,
                     :p_kodp,
                     :p_kod_obj,
                     :p_kod_numobj,
                     :status,
                     :errmsg); END;`,
            {
                p_adr: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: adr},
                p_city: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: city},
                p_street: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: street},
                p_house: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: house},
                p_nd: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nd},
                p_purpose: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: purpose},
                p_prim: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: prim},
                p_ttime: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: ttime},
                p_id_inspector: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_ins},
                p_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_task},
                p_kod_dog: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_dog ? kod_dog : null},
                p_kodp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kodp ? kodp : null},
                p_kod_obj: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_obj ? kod_obj : null},
                p_kod_numobj: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_numobj ? kod_numobj : null},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_INOUT, val: ora_status},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    ora_status = result.outBinds.status;
    let errmsg = "OK";
    if (ora_status != 0) {
        errmsg = result.outBinds.errmsg;
    }

    return errmsg;

};


async function updateTaskDal(connection, id_task, purpose, prim, kod_emp_podp, status, kod_dog, kodp, kod_obj, kod_numobj) {
    let result;
    let ora_status = 0;
    try {
        result = await connection.execute(`BEGIN MIS_MOB.mis_mobile_w.update_task(
                     :p_id_task,
                     :p_purpose,
                     :p_prim,
                     :p_kod_emp_podp,
                     :p_status,
                     :p_kod_dog,
                     :p_kodp,
                     :p_kod_obj,
                     :p_kod_numobj,
                     :status,
                     :errmsg); END;`,
            {
                p_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_task},
                p_purpose: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: purpose},
                p_prim: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: prim},
                p_kod_emp_podp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_emp_podp ? kod_emp_podp : null},
                p_status: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: status},
                p_kod_dog: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_dog ? kod_dog : null},
                p_kodp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kodp ? kodp : null},
                p_kod_obj: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_obj ? kod_obj : null},
                p_kod_numobj: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: kod_numobj ? kod_numobj : null},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_INOUT, val: ora_status},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    ora_status = result.outBinds.status;
    let errmsg = "OK";
    if (ora_status != 0) {
        errmsg = result.outBinds.errmsg;
    }

    return errmsg;

};

async function acceptTaskDal(connection, id_task) {
    let result;
    let ora_status = 0;
    try {
        result = await connection.execute(`BEGIN MIS_MOB.mis_mobile_w.accept_task(
                     :p_id_task,
                     :status,
                     :errmsg); END;`,
            {
                p_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_task},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_INOUT, val: ora_status},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    ora_status = result.outBinds.status;
    let errmsg = "OK";
    if (ora_status != 0) {
        errmsg = result.outBinds.errmsg;
    }

    return errmsg;

};

async function refuseTaskDal(connection, id_task) {
    let result;
    let ora_status = 0;
    try {
        result = await connection.execute(`BEGIN MIS_MOB.mis_mobile_w.refuse_task(
                     :p_id_task,
                     :status,
                     :errmsg); END;`,
            {
                p_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_task},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_INOUT, val: ora_status},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    ora_status = result.outBinds.status;
    let errmsg = "OK";
    if (ora_status != 0) {
        errmsg = result.outBinds.errmsg;
    }

    return errmsg;

};

async function insertActsFieldsDal(connection, id_task, id_act,npp, purpose_text,
                                   fact_text,
                                   num_act,
                                   dat_act,
                                   payer_name,
                                   adr_org,
                                   fio_contact,
                                   tel_contact,
                                   filial_eso,
                                   fio_eso,
                                   tel_eso,
                                   list_obj,
                                   name_obj,
                                   num_obj,
                                   adr_obj,
                                   ndog,
                                   dat_dog,
                                   otop_period,
                                   id_manometr,
                                   id_aupr_so,
                                   id_aupr_sw,
                                   id_aupr_gvs,
                                   id_aupr_sv,
                                   sost_kip_str,
                                   nal_act_gidro,
                                   id_sost_tube,
                                   id_sost_armatur,
                                   id_sost_izol,
                                   sost_tube_str,
                                   id_sost_net,
                                   sost_net_str,
                                   id_sost_utepl,
                                   sost_utepl_str,
                                   id_nal_pasport,
                                   id_nal_schema,
                                   id_nal_instr,
                                   nal_pasp_str,
                                   id_nal_direct_connect,
                                   nal_direct_connect,
                                   sum_dolg,
                                   remark_dog,
                                   nal_podp_doc,
                                   opl_calcul,
                                   osnov,
                                   city,
                                   comiss_post_gotov,
                                   shablon,
                                   name_dolzhn_contact,
                                   name_dolzhn_eso,
                                   period_dolg,
                                   name_st,
                                   name_mag,
                                   name_tk,
                                   name_aw,
                                   inn_org,
                                   kpp_org,
                                   nazn_name,
                                   nal_so,
                                   nal_sw,
                                   nal_st,
                                   nal_gv,
                                   pred_comiss,
                                   zam_pred_gkh,
                                   podpisi,
                                   director_tatenergo,
                                   director_t_dover_num,
                                   director_t_dover_date,
                                   zayavitel,
                                   zayavitel_dover,
                                   podgotovka,
                                   podgotovka_proj_num,
                                   podgotovka_proj_ispoln,
                                   podgotovka_proj_utvergden,
                                   net_inner_teplonositel,
                                   net_inner_dp,
                                   net_inner_do,
                                   net_inner_tip_kanal,
                                   net_inner_tube_type_p,
                                   net_inner_tube_type_o,
                                   net_inner_l,
                                   net_inner_l_undeground,
                                   net_inner__otstuplenie,
                                   energo_effect_object,
                                   nal_rezerv_istochnik,
                                   nal_svyazi,
                                   vid_connect_system,
                                   elevator_num,
                                   elevator_diam,
                                   podogrev_otop_num,
                                   podogrev_otop_kolvo_sekc,
                                   podogrev_otop_l_sekc,
                                   podogrev_otop_nazn,
                                   podogrev_otop_marka,
                                   d_napor_patrubok,
                                   power_electro_engine,
                                   chastota_vr_engine,
                                   drossel_diafragma_d,
                                   drossel_diafragma_mesto,
                                   drossel_diafragma_tip_otop,
                                   drossel_diafragma_cnt_stoyak,
                                   pu_data,
                                   pu_pover_lico,
                                   pu_pover_pokaz,
                                   pu_pover_rez,
                                   balans_prinadl_obj,
                                   balans_prin_dop,
                                   gr_ekspl_otvetst,
                                   gr_ekspl_otvetst_dop,
                                   st_podkl_rub,
                                   st_podkl_rub_nds,
                                   podkl_dop_sved,
                                   pred_pogash_dolg_num,
                                   pred_pogash_dolg_date,
                                   otkl_proizv,
                                   otkl_pu_pokaz_do,
                                   otkl_pu_pokaz_posle,
                                   filial_name,
                                   filial_address,
                                   filial_tel,
                                   act_poluchil,
                                   nal_document,
                                   dop_info,
                                   ispolnitel,
                                   dog_podkl_num,
                                   dog_podkl_date,
                                   mesto_karta,
                                   uvedoml_otkl_num,
                                   uvedoml_otkl_date,
                                   prichina_otkaza,
                                   otkaz_svidet_1,
                                   otkaz_svidet_2,
                                   pravo_sobstv,
                                   uvedom_aktirov_num,
                                   uvedom_aktirov_date,
                                   predst_potrebit,
                                   predst_potrebit_dover,
                                   volume_obj,
                                   square_obj,
                                   q_sum,
                                   so_q,
                                   sw_q,
                                   st_q,
                                   gw_q,
                                   nal_pu,
                                   nal_aupr,
                                   bezdog_sposob_num,
                                   bezdog_ustanovleno,
                                   bezdog_narushenie,
                                   bezdog_pereraschet_s,
                                   bezdog_pereraschet_po,
                                   bezdog_predpis,
                                   bezdog_obyasn,
                                   bezdog_pretenz,
                                   uslovie_podkl_num,
                                   soglasop_proekte_num,
                                   dopusk_s,
                                   dopusk_po,
                                   tel_spravki,
                                   tel_dispetch,
                                   org_ustanov_pu,
                                   type_oto_prib,
                                   schema_vkl_gvs,
                                   schema_vkl_podogrev,
                                   kolvo_sekc_1,
                                   kolvo_sekc_1_l,
                                   kolvo_sekc_2,
                                   kolvo_sekc_2_l,
                                   kolvo_kalorifer,
                                   poverhnost_nagreva,
                                   podkl_num,
                                   q_max,
                                   itog_text,
                                   is_signed) {
    let result;
    let ora_status = 0;
    try {
        result = await connection.execute(`BEGIN MIS_MOB.mis_mobile_w.insert_acts_fields(
                     :p_id_task,
                     :p_id_act,
                     :p_npp,
                     :p_purpose_text,
                     :p_fact_text,
                     :p_num_act,
                     :p_dat_act,
                     :p_payer_name,
                     :p_adr_org,
                     :p_fio_contact,
                     :p_tel_contact,
                     :p_filial_eso,
                     :p_fio_eso,
                     :p_tel_eso,
                     :p_list_obj,
                     :p_name_obj,
                     :p_num_obj,
                     :p_adr_obj,
                     :p_ndog,
                     :p_dat_dog,
                     :p_otop_period,
                     :p_id_manometr,
                     :p_id_aupr_so,
                     :p_id_aupr_sw,
                     :p_id_aupr_gvs,
                     :p_id_aupr_sv,
                     :p_sost_kip_str,
                     :p_nal_act_gidro,
                     :p_id_sost_tube,
                     :p_id_sost_armatur,
                     :p_id_sost_izol,
                     :p_sost_tube_str,
                     :p_id_sost_net,
                     :p_sost_net_str,
                     :p_id_sost_utepl,
                     :p_sost_utepl_str,
                     :p_id_nal_pasport,
                     :p_id_nal_schema,
                     :p_id_nal_instr,
                     :p_nal_pasp_str,
                     :p_id_nal_direct_connect,
                     :p_nal_direct_connect,
                     :p_sum_dolg,
                     :p_remark_dog,
                     :p_nal_podp_doc,
                     :p_opl_calcul,
                     :p_osnov,
                     :p_city,
                     :p_comiss_post_gotov,
                     :p_shablon,
                     :p_name_dolzhn_contact,
                     :p_name_dolzhn_eso,
                     :p_period_dolg,
                     :p_name_st,
                     :p_name_mag,
                     :p_name_tk,
                     :p_name_aw,
                     :p_inn_org,
                     :p_kpp_org,
                     :p_nazn_name,
                     :p_nal_so,
                     :p_nal_sw,
                     :p_nal_st,
                     :p_nal_gv,
                     :p_pred_comiss,
                     :p_zam_pred_gkh,
                     :p_podpisi,
                     :p_director_tatenergo,
                     :p_director_t_dover_num,
                     :p_director_t_dover_date,
                     :p_zayavitel,
                     :p_zayavitel_dover,
                     :p_podgotovka,
                     :p_podgotovka_proj_num,
                     :p_podgotovka_proj_ispoln,
                     :p_podgotovka_proj_utvergden,
                     :p_net_inner_teplonositel,
                     :p_net_inner_dp,
                     :p_net_inner_do,
                     :p_net_inner_tip_kanal,
                     :p_net_inner_tube_type_p,
                     :p_net_inner_tube_type_o,
                     :p_net_inner_l,
                     :p_net_inner_l_undeground,
                     :p_net_inner__otstuplenie,
                     :p_energo_effect_object,
                     :p_nal_rezerv_istochnik,
                     :p_nal_svyazi,
                     :p_vid_connect_system,
                     :p_elevator_num,
                     :p_elevator_diam,
                     :p_podogrev_otop_num,
                     :p_podogrev_otop_kolvo_sekc,
                     :p_podogrev_otop_l_sekc,
                     :p_podogrev_otop_nazn,
                     :p_podogrev_otop_marka,
                     :p_d_napor_patrubok,
                     :p_power_electro_engine,
                     :p_chastota_vr_engine,
                     :p_drossel_diafragma_d,
                     :p_drossel_diafragma_mesto,
                     :p_drossel_diafragma_tip_otop,
                     :p_drossel_diafragma_cnt_stoyak,
                     :p_pu_data,
                     :p_pu_pover_lico,
                     :p_pu_pover_pokaz,
                     :p_pu_pover_rez,
                     :p_balans_prinadl_obj,
                     :p_balans_prin_dop,
                     :p_gr_ekspl_otvetst,
                     :p_gr_ekspl_otvetst_dop,
                     :p_st_podkl_rub,
                     :p_st_podkl_rub_nds,
                     :p_podkl_dop_sved,
                     :p_pred_pogash_dolg_num,
                     :p_pred_pogash_dolg_date,
                     :p_otkl_proizv,
                     :p_otkl_pu_pokaz_do,
                     :p_otkl_pu_pokaz_posle,
                     :p_filial_name,
                     :p_filial_address,
                     :p_filial_tel,
                     :p_act_poluchil,
                     :p_nal_document,
                     :p_dop_info,
                     :p_ispolnitel,
                     :p_dog_podkl_num,
                     :p_dog_podkl_date,
                     :p_mesto_karta,
                     :p_uvedoml_otkl_num,
                     :p_uvedoml_otkl_date,
                     :p_prichina_otkaza,
                     :p_otkaz_svidet_1,
                     :p_otkaz_svidet_2,
                     :p_pravo_sobstv,
                     :p_uvedom_aktirov_num,
                     :p_uvedom_aktirov_date,
                     :p_predst_potrebit,
                     :p_predst_potrebit_dover,
                     :p_volume_obj,
                     :p_square_obj,
                     :p_q_sum,
                     :p_so_q,
                     :p_sw_q,
                     :p_st_q,
                     :p_gw_q,
                     :p_nal_pu,
                     :p_nal_aupr,
                     :p_bezdog_sposob_num,
                     :p_bezdog_ustanovleno,
                     :p_bezdog_narushenie,
                     :p_bezdog_pereraschet_s,
                     :p_bezdog_pereraschet_po,
                     :p_bezdog_predpis,
                     :p_bezdog_obyasn,
                     :p_bezdog_pretenz,
                     :p_uslovie_podkl_num,
                     :p_soglasop_proekte_num,
                     :p_dopusk_s,
                     :p_dopusk_po,
                     :p_tel_spravki,
                     :p_tel_dispetch,
                     :p_org_ustanov_pu,
                     :p_type_oto_prib,
                     :p_schema_vkl_gvs,
                     :p_schema_vkl_podogrev,
                     :p_kolvo_sekc_1,
                     :p_kolvo_sekc_1_l,
                     :p_kolvo_sekc_2,
                     :p_kolvo_sekc_2_l,
                     :p_kolvo_kalorifer,
                     :p_poverhnost_nagreva,
                     :p_podkl_num,
                     :p_q_max,
                     :p_itog_text,
                     :p_is_signed,
                     :status,
                     :errmsg); END;`,
            {
                p_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_task},
                p_id_act: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_act},
                p_npp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: npp},
                p_purpose_text: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: purpose_text},
                p_fact_text: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: fact_text},
                p_num_act: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: num_act},
                p_dat_act: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: dat_act},
                p_payer_name: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: payer_name},
                p_adr_org: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: adr_org},
                p_fio_contact: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: fio_contact},
                p_tel_contact: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: tel_contact},
                p_filial_eso: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: filial_eso},
                p_fio_eso: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: fio_eso},
                p_tel_eso: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: tel_eso},
                p_list_obj: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: list_obj},
                p_name_obj: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: name_obj},
                p_num_obj: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: num_obj},
                p_adr_obj: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: adr_obj},
                p_ndog: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: ndog},
                p_dat_dog: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: dat_dog},
                p_otop_period: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: otop_period},
                p_id_manometr: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: id_manometr},
                p_id_aupr_so: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: id_aupr_so},
                p_id_aupr_sw: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: id_aupr_sw},
                p_id_aupr_gvs: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: id_aupr_gvs},
                p_id_aupr_sv: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: id_aupr_sv},
                p_sost_kip_str: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: sost_kip_str},
                p_nal_act_gidro: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nal_act_gidro},
                p_id_sost_tube: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: id_sost_tube},
                p_id_sost_armatur: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: id_sost_armatur},
                p_id_sost_izol: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: id_sost_izol},
                p_sost_tube_str: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: sost_tube_str},
                p_id_sost_net: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: id_sost_net},
                p_sost_net_str: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: sost_net_str},
                p_id_sost_utepl: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: id_sost_utepl},
                p_sost_utepl_str: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: sost_utepl_str},
                p_id_nal_pasport: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: id_nal_pasport},
                p_id_nal_schema: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: id_nal_schema},
                p_id_nal_instr: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: id_nal_instr},
                p_nal_pasp_str: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nal_pasp_str},
                p_id_nal_direct_connect: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: id_nal_direct_connect},
                p_nal_direct_connect: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nal_direct_connect},
                p_sum_dolg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: sum_dolg},
                p_remark_dog: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: remark_dog},
                p_nal_podp_doc: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nal_podp_doc},
                p_opl_calcul: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: opl_calcul},
                p_osnov: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: osnov},
                p_city: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: city},
                p_comiss_post_gotov: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: comiss_post_gotov},
                p_shablon: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: shablon},
                p_name_dolzhn_contact: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: name_dolzhn_contact},
                p_name_dolzhn_eso: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: name_dolzhn_eso},
                p_period_dolg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: period_dolg},
                p_name_st: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: name_st},
                p_name_mag: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: name_mag},
                p_name_tk: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: name_tk},
                p_name_aw: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: name_aw},
                p_inn_org: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: inn_org},
                p_kpp_org: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: kpp_org},
                p_nazn_name: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nazn_name},
                p_nal_so: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nal_so},
                p_nal_sw: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nal_sw},
                p_nal_st: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nal_st},
                p_nal_gv: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nal_gv},
                p_pred_comiss: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pred_comiss},
                p_zam_pred_gkh: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: zam_pred_gkh},
                p_podpisi: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: podpisi},
                p_director_tatenergo: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: director_tatenergo},
                p_director_t_dover_num: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: director_t_dover_num},
                p_director_t_dover_date: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: director_t_dover_date},
                p_zayavitel: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: zayavitel},
                p_zayavitel_dover: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: zayavitel_dover},
                p_podgotovka: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: podgotovka},
                p_podgotovka_proj_num: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: podgotovka_proj_num},
                p_podgotovka_proj_ispoln: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: podgotovka_proj_ispoln},
                p_podgotovka_proj_utvergden: {
                    type: oracledb.VARCHAR2,
                    dir: oracledb.BIND_IN,
                    val: podgotovka_proj_utvergden
                },
                p_net_inner_teplonositel: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: net_inner_teplonositel},
                p_net_inner_dp: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: net_inner_dp},
                p_net_inner_do: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: net_inner_do},
                p_net_inner_tip_kanal: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: net_inner_tip_kanal},
                p_net_inner_tube_type_p: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: net_inner_tube_type_p},
                p_net_inner_tube_type_o: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: net_inner_tube_type_o},
                p_net_inner_l: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: net_inner_l},
                p_net_inner_l_undeground: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: net_inner_l_undeground},
                p_net_inner__otstuplenie: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: net_inner__otstuplenie},
                p_energo_effect_object: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: energo_effect_object},
                p_nal_rezerv_istochnik: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nal_rezerv_istochnik},
                p_nal_svyazi: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nal_svyazi},
                p_vid_connect_system: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: vid_connect_system},
                p_elevator_num: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: elevator_num},
                p_elevator_diam: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: elevator_diam},
                p_podogrev_otop_num: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: podogrev_otop_num},
                p_podogrev_otop_kolvo_sekc: {
                    type: oracledb.VARCHAR2,
                    dir: oracledb.BIND_IN,
                    val: podogrev_otop_kolvo_sekc
                },
                p_podogrev_otop_l_sekc: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: podogrev_otop_l_sekc},
                p_podogrev_otop_nazn: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: podogrev_otop_nazn},
                p_podogrev_otop_marka: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: podogrev_otop_marka},
                p_d_napor_patrubok: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: d_napor_patrubok},
                p_power_electro_engine: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: power_electro_engine},
                p_chastota_vr_engine: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: chastota_vr_engine},
                p_drossel_diafragma_d: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: drossel_diafragma_d},
                p_drossel_diafragma_mesto: {
                    type: oracledb.VARCHAR2,
                    dir: oracledb.BIND_IN,
                    val: drossel_diafragma_mesto
                },
                p_drossel_diafragma_tip_otop: {
                    type: oracledb.VARCHAR2,
                    dir: oracledb.BIND_IN,
                    val: drossel_diafragma_tip_otop
                },
                p_drossel_diafragma_cnt_stoyak: {
                    type: oracledb.VARCHAR2,
                    dir: oracledb.BIND_IN,
                    val: drossel_diafragma_cnt_stoyak
                },
                p_pu_data: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pu_data},
                p_pu_pover_lico: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pu_pover_lico},
                p_pu_pover_pokaz: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pu_pover_pokaz},
                p_pu_pover_rez: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pu_pover_rez},
                p_balans_prinadl_obj: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: balans_prinadl_obj},
                p_balans_prin_dop: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: balans_prin_dop},
                p_gr_ekspl_otvetst: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: gr_ekspl_otvetst},
                p_gr_ekspl_otvetst_dop: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: gr_ekspl_otvetst_dop},
                p_st_podkl_rub: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: st_podkl_rub},
                p_st_podkl_rub_nds: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: st_podkl_rub_nds},
                p_podkl_dop_sved: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: podkl_dop_sved},
                p_pred_pogash_dolg_num: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pred_pogash_dolg_num},
                p_pred_pogash_dolg_date: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pred_pogash_dolg_date},
                p_otkl_proizv: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: otkl_proizv},
                p_otkl_pu_pokaz_do: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: otkl_pu_pokaz_do},
                p_otkl_pu_pokaz_posle: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: otkl_pu_pokaz_posle},
                p_filial_name: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: filial_name},
                p_filial_address: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: filial_address},
                p_filial_tel: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: filial_tel},
                p_act_poluchil: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: act_poluchil},
                p_nal_document: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nal_document},
                p_dop_info: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: dop_info},
                p_ispolnitel: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: ispolnitel},
                p_dog_podkl_num: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: dog_podkl_num},
                p_dog_podkl_date: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: dog_podkl_date},
                p_mesto_karta: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: mesto_karta},
                p_uvedoml_otkl_num: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: uvedoml_otkl_num},
                p_uvedoml_otkl_date: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: uvedoml_otkl_date},
                p_prichina_otkaza: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: prichina_otkaza},
                p_otkaz_svidet_1: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: otkaz_svidet_1},
                p_otkaz_svidet_2: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: otkaz_svidet_2},
                p_pravo_sobstv: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pravo_sobstv},
                p_uvedom_aktirov_num: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: uvedom_aktirov_num},
                p_uvedom_aktirov_date: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: uvedom_aktirov_date},
                p_predst_potrebit: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: predst_potrebit},
                p_predst_potrebit_dover: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: predst_potrebit_dover},
                p_volume_obj: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: volume_obj},
                p_square_obj: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: square_obj},
                p_q_sum: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: q_sum},
                p_so_q: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: so_q},
                p_sw_q: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: sw_q},
                p_st_q: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: st_q},
                p_gw_q: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: gw_q},
                p_nal_pu: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nal_pu},
                p_nal_aupr: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nal_aupr},
                p_bezdog_sposob_num: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: bezdog_sposob_num},
                p_bezdog_ustanovleno: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: bezdog_ustanovleno},
                p_bezdog_narushenie: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: bezdog_narushenie},
                p_bezdog_pereraschet_s: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: bezdog_pereraschet_s},
                p_bezdog_pereraschet_po: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: bezdog_pereraschet_po},
                p_bezdog_predpis: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: bezdog_predpis},
                p_bezdog_obyasn: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: bezdog_obyasn},
                p_bezdog_pretenz: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: bezdog_pretenz},
                p_uslovie_podkl_num: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: uslovie_podkl_num},
                p_soglasop_proekte_num: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: soglasop_proekte_num},
                p_dopusk_s: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: dopusk_s},
                p_dopusk_po: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: dopusk_po},
                p_tel_spravki: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: tel_spravki},
                p_tel_dispetch: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: tel_dispetch},
                p_org_ustanov_pu: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: org_ustanov_pu},
                p_type_oto_prib: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: type_oto_prib},
                p_schema_vkl_gvs: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: schema_vkl_gvs},
                p_schema_vkl_podogrev: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: schema_vkl_podogrev},
                p_kolvo_sekc_1: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: kolvo_sekc_1},
                p_kolvo_sekc_1_l: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: kolvo_sekc_1_l},
                p_kolvo_sekc_2: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: kolvo_sekc_2},
                p_kolvo_sekc_2_l: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: kolvo_sekc_2_l},
                p_kolvo_kalorifer: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: kolvo_kalorifer},
                p_poverhnost_nagreva: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: poverhnost_nagreva},
                p_podkl_num: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: podkl_num},
                p_q_max: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: q_max},
                p_itog_text: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: itog_text},
                p_is_signed: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: is_signed},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_INOUT, val: ora_status},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    ora_status = result.outBinds.status;
    let errmsg = "OK";
    if (ora_status != 0) {
        errmsg = result.outBinds.errmsg;
    }

    return errmsg;

};

async function deleteActsDal(connection, id_task) {
    let result;
    let ora_status = 0;
    try {
        result = await connection.execute(`BEGIN MIS_MOB.mis_mobile_w.delete_acts(
                     :p_id_task,
                     :status,
                     :errmsg); END;`,
            {
                p_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_task},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_INOUT, val: ora_status},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    ora_status = result.outBinds.status;
    let errmsg = "OK";
    if (ora_status != 0) {
        errmsg = result.outBinds.errmsg;
    }

    return errmsg;

};

async function insertDopActFieldsDal(connection, id_task,
                                     id_act,
                                     npp,
                                     num_obj,
                                     name_obj,
                                     address_obj,
                                     god_zd,
                                     nazn_name,
                                     tvr,
                                     pr_oto,
                                     pr_sw,
                                     volume,
                                     square_total,
                                     point_name,
                                     etaz,
                                     so_q,
                                     sw_q,
                                     st_q,
                                     gw_qmax,
                                     name_vodo,
                                     nn_wpol,
                                     tt_wpol,
                                     nn_prib,
                                     pr_rec,
                                     pr_psusch,
                                     pr_iz_st,
                                     nom_uch,
                                     tip_name,
                                     pt_d,
                                     pt_l,
                                     name_pr_pt,
                                     ot_d,
                                     ot_l,
                                     name_pr_ot,
                                     uch_hgr,
                                     pu_num,
                                     pu_name,
                                     pu_mesto,
                                     pu_type,
                                     pu_diam,
                                     pu_kolvo,
                                     pu_proba_mesto,
                                     q_sum,
                                     q_sum_max,
                                     pu_srok_poverki,
                                     pu_num_plomba,
                                     pu_pokaz,
                                     schema_prisoed_name,
                                     schema_prisoed_kod) {
    let result;
    let ora_status = 0;
    try {
        result = await connection.execute(`BEGIN MIS_MOB.mis_mobile_w.insert_dop_act_fields(
                    :p_id_task,
                    :p_id_act,
                    :p_npp,
                    :p_num_obj,
                    :p_name_obj,
                    :p_address_obj,
                    :p_god_zd,
                    :p_nazn_name,
                    :p_tvr,
                    :p_pr_oto,
                    :p_pr_sw,
                    :p_volume,
                    :p_square_total,
                    :p_point_name,
                    :p_etaz,
                    :p_so_q,
                    :p_sw_q,
                    :p_st_q,
                    :p_gw_qmax,
                    :p_name_vodo,
                    :p_nn_wpol,
                    :p_tt_wpol,
                    :p_nn_prib,
                    :p_pr_rec,
                    :p_pr_psusch,
                    :p_pr_iz_st,
                    :p_nom_uch,
                    :p_tip_name,
                    :p_pt_d,
                    :p_pt_l,
                    :p_name_pr_pt,
                    :p_ot_d,
                    :p_ot_l,
                    :p_name_pr_ot,
                    :p_uch_hgr,
                    :p_pu_num,
                    :p_pu_name,
                    :p_pu_mesto,
                    :p_pu_type,
                    :p_pu_diam,
                    :p_pu_kolvo,
                    :p_pu_proba_mesto,
                    :p_q_sum,
                    :p_q_sum_max,
                    :p_pu_srok_poverki,
                    :p_pu_num_plomba,
                    :p_pu_pokaz,
                    :p_schema_prisoed_name,
                    :p_schema_prisoed_kod,
                    :status,
                     :errmsg); END;`,
            {
                p_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_task},
                p_id_act: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_act},
                p_npp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: npp},
                p_num_obj: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: num_obj},
                p_name_obj: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: name_obj},
                p_address_obj: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: address_obj},
                p_god_zd: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: god_zd},
                p_nazn_name: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nazn_name},
                p_tvr: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: tvr},
                p_pr_oto: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pr_oto},
                p_pr_sw: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pr_sw},
                p_volume: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: volume},
                p_square_total: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: square_total},
                p_point_name: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: point_name},
                p_etaz: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: etaz},
                p_so_q: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: so_q},
                p_sw_q: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: sw_q},
                p_st_q: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: st_q},
                p_gw_qmax: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: gw_qmax},
                p_name_vodo: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: name_vodo},
                p_nn_wpol: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nn_wpol},
                p_tt_wpol: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: tt_wpol},
                p_nn_prib: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nn_prib},
                p_pr_rec: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pr_rec},
                p_pr_psusch: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pr_psusch},
                p_pr_iz_st: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pr_iz_st},
                p_nom_uch: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: nom_uch},
                p_tip_name: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: tip_name},
                p_pt_d: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pt_d},
                p_pt_l: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pt_l},
                p_name_pr_pt: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: name_pr_pt},
                p_ot_d: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: ot_d},
                p_ot_l: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: ot_l},
                p_name_pr_ot: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: name_pr_ot},
                p_uch_hgr: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: uch_hgr},
                p_pu_num: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pu_num},
                p_pu_name: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pu_name},
                p_pu_mesto: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pu_mesto},
                p_pu_type: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pu_type},
                p_pu_diam: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pu_diam},
                p_pu_kolvo: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pu_kolvo},
                p_pu_proba_mesto: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pu_proba_mesto},
                p_q_sum: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: q_sum},
                p_q_sum_max: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: q_sum_max},
                p_pu_srok_poverki: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pu_srok_poverki},
                p_pu_num_plomba: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pu_num_plomba},
                p_pu_pokaz: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: pu_pokaz},
                p_schema_prisoed_name: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: schema_prisoed_name},
                p_schema_prisoed_kod: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: schema_prisoed_kod},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_INOUT, val: ora_status},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            });
    } catch (e) {
        console.log(e.message)
    }
    ora_status = result.outBinds.status;
    let errmsg = "OK";
    if (ora_status != 0) {
        errmsg = result.outBinds.errmsg;
    }

    return errmsg;

};

async function addFileDal(connection, id_task, filename, filedata, is_signed, paper, date_send_to_client, email_client, npp, id_act) {

    return new Promise(function (resolve, reject) {
        let ora_status = 0;
        connection.execute(`BEGIN MIS_MOB.mis_mobile_w.add_file(
                     :p_id_task,
                     :p_filename,
                     :p_filedata,
                     :p_is_signed,
                     :p_paper,
                     :p_date_send_to_client,
                     :p_email_client,
                     :p_npp,
                     :p_id_act,
                     :status,
                     :errmsg); END;`,
            {
                p_id_task: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_task},
                p_filename: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: filename},
                p_filedata: {type: oracledb.BLOB, dir: oracledb.BIND_IN, val: filedata},
                p_is_signed: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: is_signed},
                p_paper: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: paper},
                p_date_send_to_client: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: date_send_to_client},
                p_email_client: {type: oracledb.VARCHAR2, dir: oracledb.BIND_IN, val: email_client},
                p_npp: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: npp ? npp : null} ,
                p_id_act: {type: oracledb.NUMBER, dir: oracledb.BIND_IN, val: id_act ? id_act : null},
                status: {type: oracledb.NUMBER, dir: oracledb.BIND_INOUT, val: ora_status},
                errmsg: {type: oracledb.VARCHAR2, dir: oracledb.BIND_OUT}
            },
            {
                resultSet: true
            }).then(function (result) {

        let ora_status = result.outBinds.status;
        let errmsg = "OK";
        if (ora_status != 0) {
            errmsg = result.outBinds.errmsg;
            reject(errmsg);
        }
            resolve(errmsg);}).catch(function (err) {
            reject(err);
        })
    })
}

module.exports = {
    addTaskDal,
    createTaskDal,
    updateTaskDal,
    acceptTaskDal,
    refuseTaskDal,
    insertActsFieldsDal,
    deleteActsDal,
    insertDopActFieldsDal,
    addFileDal,
    updateMinTaskIdDal
};