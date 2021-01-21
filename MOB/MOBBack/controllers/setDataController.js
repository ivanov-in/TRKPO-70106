const DATA = require('../data/setDataDal');
let oracledb = require('oracledb');
const pool = require('../data/db_pool');


async function updateMinTaskId(id_ins, min_task_id) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.updateMinTaskIdDal(connection, id_ins, min_task_id).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })
}

async function createTask(id_ins, kod_dog, purpose, kod_obj, kod_numobj, ttime, id_task) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.createTaskDal(connection, id_ins, kod_dog, purpose, kod_obj, kod_numobj, ttime, id_task).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })
}




async function addTask(adr, city, street, house, nd, purpose, prim, ttime, id_ins, id_task, kod_dog, kodp, kod_obj, kod_numobj) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.addTaskDal(connection, adr, city, street, house, nd, purpose, prim, ttime, id_ins, id_task, kod_dog, kodp, kod_obj, kod_numobj).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })
}


async function updateTask(id_task, purpose, prim, kod_emp_podp, status, kod_dog, kodp, kod_obj, kod_numobj) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.updateTaskDal(connection, id_task, purpose, prim,kod_emp_podp, status, kod_dog, kodp, kod_obj, kod_numobj).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })
}

async function acceptTask(id_task) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.acceptTaskDal(connection, id_task).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })
}

async function refuseTask(id_task) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.refuseTaskDal(connection, id_task).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })
}

async function insertActsFields(id_task, id_act, npp, purpose_text,
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
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.insertActsFieldsDal(connection, id_task, id_act,npp,purpose_text,
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
                is_signed).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })
}

async function deleteActs(id_task, id_act) {
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.deleteActsDal(connection, id_task).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })
}

async function insertDopActFields(id_task,
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
    return new Promise( function (resolve, reject) {
        oracledb.getConnection().then(function (connection) {
            DATA.insertDopActFieldsDal(connection, id_task,
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
                schema_prisoed_kod).then(function (result) {
                pool.connectionRelease(connection);
                resolve(result);
            }).catch(function (error) {
                pool.connectionRelease(connection);
                reject(error);
            });
        }).catch(function (error) {
            pool.connectionRelease(connection);
            reject(error);
        })

    })
}


module .exports = {
    createTask,
    addTask,
    updateTask,
    acceptTask,
    refuseTask,
    insertActsFields,
    deleteActs,
    insertDopActFields,
    updateMinTaskId

}