package ru.infoenergo.mis.dbhandler

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.infoenergo.mis.helpers.TAG_ERR

open class DbHandlerLocal(context: Context, name: String?, factory: SQLiteDatabase.CursorFactory?, version: Int) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        const val ASSETS_PATH = "databases"
        const val DATABASE_NAME = "mis_db.db"
        const val DATABASE_VERSION = 17
    }

    override fun onCreate(db: SQLiteDatabase) {
        try {
            createTables(db)
            createSequences(db)
        } catch (e: Exception) {
            db.close()
            print("$TAG_ERR on create local db: ${e.message}")
        }
    }

    private fun createTables(db: SQLiteDatabase) {

        db.execSQL(
            "create table IF NOT EXISTS MI_INSPECTOR_DATA " +
                    "(   id_inspector    NUMBER not null primary key ON CONFLICT IGNORE,  " +
                    "    fio             VARCHAR2(1000), " +
                    "    puser           VARCHAR2(1000), " +
                    "    PSWD            VARCHAR2(4000)," +
                    "    MIN_TASK_ID     NUMBER not null," +
                    "    VERS            VARCHAR2(100)," +
                    "    NUM_ACT_PREF    VARCHAR2(100))"
        )

        db.execSQL(
            "create table IF NOT EXISTS MI_MOB_TASK       " +
                    "(   dat                DATE   not null,  " +
                    "    id_task            NUMBER not null primary key on conflict replace,  " +
                    "    adr                VARCHAR2(500),   " +
                    "    purpose            NUMBER not null, " +
                    "    purpose_name       VARCHAR2(500),   " +
                    "    prim               VARCHAR2(500),   " +
                    "    ttime              DATE,            " +
                    "    status             NUMBER,          " +
                    "    status_name        VARCHAR2(500),   " +
                    "    id_inspector       NUMBER not null, " +
                    "    kod_obj            NUMBER,          " +
                    "    kod_numobj         NUMBER,          " +
                    "    kod_dog            NUMBER,          " +
                    "    kodp               NUMBER,          " +
                    "    ndog               VARCHAR2(300),   " +
                    "    payer_name         VARCHAR2(4000),  " +
                    "    fio_contact        VARCHAR2(4000),  " +
                    "    email_contact      VARCHAR2(200),   " +
                    "    tel_contact        VARCHAR2(200),   " +
                    "    lat                VARCHAR2(4000),  " +
                    "    lan                VARCHAR2(4000),  " +
                    "    schema_zulu        VARCHAR2(4000),  " +
                    "    border_zulu        VARCHAR2(4000),  " +
                    "    fio_inspector      VARCHAR2(2000),  " +
                    "    city               VARCHAR2(100),   " +
                    "    street             VARCHAR2(300),   " +
                    "    house              NUMBER,          " +
                    "    houses             VARCHAR2(100),   " +
                    "    nd                 VARCHAR2(50),    " +
                    "    kod_emp_podp       NUMBER,          " +
                    "    fio_podp           VARCHAR2(1000),  " +
                    "    email_podp         VARCHAR2(1000),  " +
                    "    tel_podp           VARCHAR2(100),   " +
                    "    name_dolzhn_podp   VARCHAR2(1000),  " +
                    "    is_send            NUMBER DEFAULT 0)"
        )

        db.execSQL(
            "create table IF NOT EXISTS MI_PURPOSE  " +
                    "(   purpose    NUMBER not null unique ON CONFLICT REPLACE,  " +
                    "    name       VARCHAR2(99))"
        )

        //region Акты
        db.execSQL(
            "create table IF NOT EXISTS MI_ACTS  " +
                    "  ( id_purpose        NUMBER not null," +
                    "    id_act            NUMBER not null," +
                    "    name              VARCHAR2(1000)," +
                    "    tip               NUMBER," +
                    "    constraint MI_ACTS_pk" +
                    "   unique (id_purpose , id_act) on conflict replace)"
        )

        db.execSQL(
            "create table IF NOT EXISTS MI_ACT_FIELDS_SHABLON  " +
                    "( dat                          DATE," +
                    "  id_task                      NUMBER, " +
                    "  id_act                       NUMBER, " +
                    "  kodp                         NUMBER, " +
                    "  kod_dog                      NUMBER, " +
                    "  kod_obj                      NUMBER, " +
                    "  num_act                      VARCHAR2(1000), " +
                    "  dat_act                      VARCHAR2(100), " +
                    "  payer_name                   VARCHAR2(4000), " +
                    "  adr_org                      VARCHAR2(4000), " +
                    "  fio_contact                  VARCHAR2(500), " +
                    "  tel_contact                  VARCHAR2(100), " +
                    "  filial_eso                   VARCHAR2(200), " +
                    "  fio_eso                      VARCHAR2(500), " +
                    "  tel_eso                      VARCHAR2(100), " +
                    "  list_obj                     VARCHAR2(4000), " +
                    "  name_obj                     VARCHAR2(500), " +
                    "  num_obj                      VARCHAR2(500), " +
                    "  adr_obj                      VARCHAR2(500), " +
                    "  ndog                         VARCHAR2(500), " +
                    "  dat_dog                      VARCHAR2(100), " +
                    "  otop_period                  VARCHAR2(50), " +
                    "  sum_dolg                     VARCHAR2(100), " +
                    "  remark_dog                   VARCHAR2(4000), " +
                    "  nal_podp_doc                 VARCHAR2(100), " +
                    "  opl_calcul                   VARCHAR2(100), " +
                    "  osnov                        VARCHAR2(200), " +
                    "  city                         VARCHAR2(200), " +
                    "  shablon                      VARCHAR2(1000), " +
                    "  name_dolzhn_contact          VARCHAR2(200), " +
                    "  name_dolzhn_eso              VARCHAR2(200), " +
                    "  period_dolg                  VARCHAR2(200), " +
                    "  name_st                      VARCHAR2(1000), " +
                    "  name_mag                     VARCHAR2(1000), " +
                    "  name_tk                      VARCHAR2(1000), " +
                    "  name_aw                      VARCHAR2(1000), " +
                    "  inn_org                      VARCHAR2(100), " +
                    "  kpp_org                      VARCHAR2(100), " +
                    "  nazn_name                    VARCHAR2(200), " +
                    "  nal_so                       VARCHAR2(100), " +
                    "  nal_sw                       VARCHAR2(100), " +
                    "  nal_st                       VARCHAR2(100), " +
                    "  nal_gv                       VARCHAR2(100), " +
                    "  director_tatenergo           VARCHAR2(400), " +
                    "  director_t_dover_num         VARCHAR2(400), " +
                    "  volume_obj                   VARCHAR2(100), " +
                    "  so_q                         VARCHAR2(100), " +
                    "  sw_q                         VARCHAR2(100), " +
                    "  st_q                         VARCHAR2(100), " +
                    "  gw_q                         VARCHAR2(100), " +
                    "  q_sum                        VARCHAR2(100), " +
                    "  nal_act_gidro                VARCHAR2(100), " +
                    "  filial_address               VARCHAR2(1000), " +
                    "  filial_tel                   VARCHAR2(100), " +
                    "    constraint MI_ACT_FIELDS_SHABLON_pk" +
                    "        unique (id_task, id_act) on conflict replace " +
                    ")"
        )

        db.execSQL(
            "create table IF NOT EXISTS MI_ACT_FIELDS  " +
                    "( id_task                      NUMBER, " +
                    "  dat                          DATE," +
                    "  npp                          NUMBER, " +
                    "  id_act                       NUMBER, " +
                    "  id_file                      NUMBER, " +
                    "  kodp                         NUMBER, " +
                    "  kod_dog                      NUMBER, " +
                    "  kod_obj                      NUMBER, " +
                    "  purpose_text                 VARCHAR2(4000), " +
                    "  fact_text                    VARCHAR2(4000), " +
                    "  num_act                      VARCHAR2(1000), " +
                    "  dat_act                      VARCHAR2(100), " +
                    "  payer_name                   VARCHAR2(4000), " +
                    "  adr_org                      VARCHAR2(4000), " +
                    "  fio_contact                  VARCHAR2(500), " +
                    "  tel_contact                  VARCHAR2(100), " +
                    "  filial_eso                   VARCHAR2(200), " +
                    "  fio_eso                      VARCHAR2(500), " +
                    "  tel_eso                      VARCHAR2(100), " +
                    "  list_obj                     VARCHAR2(4000), " +
                    "  name_obj                     VARCHAR2(500), " +
                    "  num_obj                      VARCHAR2(500), " +
                    "  adr_obj                      VARCHAR2(500), " +
                    "  ndog                         VARCHAR2(500), " +
                    "  dat_dog                      VARCHAR2(100), " +
                    "  otop_period                  VARCHAR2(50), " +
                    "  id_manometr                  NUMBER, " +
                    "  id_aupr_so                   NUMBER, " +
                    "  id_aupr_sw                   NUMBER, " +
                    "  id_aupr_gvs                  NUMBER, " +
                    "  id_aupr_sv                   NUMBER, " +
                    "  sost_kip_str                 VARCHAR2(1000), " +
                    "  nal_act_gidro                VARCHAR2(100), " +
                    "  id_sost_tube                 NUMBER, " +
                    "  id_sost_armatur              NUMBER, " +
                    "  id_sost_izol                 NUMBER, " +
                    "  sost_tube_str                VARCHAR2(1000), " +
                    "  id_sost_net                  NUMBER, " +
                    "  sost_net_str                 VARCHAR2(1000), " +
                    "  id_sost_utepl                NUMBER, " +
                    "  sost_utepl_str               VARCHAR2(1000), " +
                    "  id_nal_pasport               NUMBER, " +
                    "  id_nal_schema                NUMBER, " +
                    "  id_nal_instr                 NUMBER, " +
                    "  nal_pasp_str                 VARCHAR2(1000), " +
                    "  id_nal_direct_connect        NUMBER, " +
                    "  nal_direct_connect           VARCHAR2(1000), " +
                    "  sum_dolg                     VARCHAR2(100), " +
                    "  remark_dog                   VARCHAR2(4000), " +
                    "  nal_podp_doc                 VARCHAR2(100), " +
                    "  opl_calcul                   VARCHAR2(100), " +
                    "  osnov                        VARCHAR2(200), " +
                    "  city                         VARCHAR2(200), " +
                    "  comiss_post_gotov            VARCHAR2(100), " +
                    "  shablon                      VARCHAR2(1000), " +
                    "  name_dolzhn_contact          VARCHAR2(200), " +
                    "  name_dolzhn_eso              VARCHAR2(200), " +
                    "  period_dolg                  VARCHAR2(200), " +
                    "  name_st                      VARCHAR2(1000), " +
                    "  name_mag                     VARCHAR2(1000), " +
                    "  name_tk                      VARCHAR2(1000), " +
                    "  name_aw                      VARCHAR2(1000), " +
                    "  inn_org                      VARCHAR2(100), " +
                    "  kpp_org                      VARCHAR2(100), " +
                    "  nazn_name                    VARCHAR2(200), " +
                    "  nal_so                       VARCHAR2(100), " +
                    "  nal_sw                       VARCHAR2(100), " +
                    "  nal_st                       VARCHAR2(100), " +
                    "  nal_gv                       VARCHAR2(100), " +
                    "  pred_comiss                  VARCHAR2(400), " +
                    "  zam_pred_gkh                 VARCHAR2(400), " +
                    "  podpisi                      VARCHAR2(400), " +
                    "  director_tatenergo           VARCHAR2(400), " +
                    "  director_t_dover_num         VARCHAR2(400), " +
                    "  zayavitel                    VARCHAR2(400), " +
                    "  zayavitel_dover              VARCHAR2(400), " +
                    "  podgotovka                   VARCHAR2(3000), " +
                    "  podgotovka_proj_num          VARCHAR2(400), " +
                    "  podgotovka_proj_ispoln       VARCHAR2(400), " +
                    "  podgotovka_proj_utvergden    VARCHAR2(400), " +
                    "  net_inner_teplonositel       VARCHAR2(400), " +
                    "  net_inner_dp                 VARCHAR2(50), " +
                    "  net_inner_do                 VARCHAR2(50), " +
                    "  net_inner_tip_kanal          VARCHAR2(100), " +
                    "  net_inner_tube_type_p        VARCHAR2(100), " +
                    "  net_inner_tube_type_o        VARCHAR2(100), " +
                    "  net_inner_l                  VARCHAR2(100), " +
                    "  net_inner_l_undeground       VARCHAR2(100), " +
                    "  net_inner_otstuplenie        VARCHAR2(3000), " +
                    "  energo_effect_object         VARCHAR2(100), " +
                    "  nal_rezerv_istochnik         VARCHAR2(100), " +
                    "  nal_svyazi                   VARCHAR2(100), " +
                    "  vid_connect_system           VARCHAR2(100), " +
                    "  elevator_num                 VARCHAR2(100), " +
                    "  elevator_diam                VARCHAR2(100), " +
                    "  podogrev_otop_num            VARCHAR2(100), " +
                    "  podogrev_otop_kolvo_sekc     VARCHAR2(100), " +
                    "  podogrev_otop_l_sekc         VARCHAR2(100), " +
                    "  podogrev_otop_nazn           VARCHAR2(100), " +
                    "  podogrev_otop_marka          VARCHAR2(100), " +
                    "  d_napor_patrubok             VARCHAR2(100), " +
                    "  power_electro_engine         VARCHAR2(100), " +
                    "  chastota_vr_engine           VARCHAR2(100), " +
                    "  drossel_diafragma_d          VARCHAR2(100), " +
                    "  drossel_diafragma_mesto      VARCHAR2(100), " +
                    "  drossel_diafragma_tip_otop   VARCHAR2(100), " +
                    "  drossel_diafragma_cnt_stoyak VARCHAR2(100), " +
                    "  pu_data                      VARCHAR2(100), " +
                    "  pu_pover_lico                VARCHAR2(400), " +
                    "  pu_pover_pokaz               VARCHAR2(100), " +
                    "  pu_pover_rez                 VARCHAR2(1000), " +
                    "  balans_prinadl_obj           VARCHAR2(3000), " +
                    "  balans_prin_dop              VARCHAR2(3000), " +
                    "  gr_ekspl_otvetst             VARCHAR2(3000), " +
                    "  gr_ekspl_otvetst_dop         VARCHAR2(3000), " +
                    "  st_podkl_rub                 VARCHAR2(100), " +
                    "  st_podkl_rub_nds             VARCHAR2(100), " +
                    "  podkl_dop_sved               VARCHAR2(3000), " +
                    "  pred_pogash_dolg_num         VARCHAR2(100), " +
                    "  pred_pogash_dolg_date        VARCHAR2(100), " +
                    "  otkl_proizv                  VARCHAR2(400), " +
                    "  otkl_pu_pokaz_do             VARCHAR2(100), " +
                    "  otkl_pu_pokaz_posle          VARCHAR2(100), " +
                    "  filial_name                  VARCHAR2(1000), " +
                    "  filial_address               VARCHAR2(1000), " +
                    "  filial_tel                   VARCHAR2(100), " +
                    "  act_poluchil                 VARCHAR2(1000), " +
                    "  nal_document                 VARCHAR2(100), " +
                    "  dop_info                     VARCHAR2(1000), " +
                    "  ispolnitel                   VARCHAR2(1000), " +
                    "  dog_podkl_num                VARCHAR2(100), " +
                    "  dog_podkl_date               VARCHAR2(100), " +
                    "  mesto_karta                  VARCHAR2(1000), " +
                    "  uvedoml_otkl_num             VARCHAR2(100), " +
                    "  uvedoml_otkl_date            VARCHAR2(100), " +
                    "  prichina_otkaza              VARCHAR2(1000), " +
                    "  otkaz_svidet_1               VARCHAR2(1000), " +
                    "  otkaz_svidet_2               VARCHAR2(1000), " +
                    "  pravo_sobstv                 VARCHAR2(1000), " +
                    "  uvedom_aktirov_num           VARCHAR2(100), " +
                    "  uvedom_aktirov_date          VARCHAR2(100), " +
                    "  predst_potrebit              VARCHAR2(1000), " +
                    "  predst_potrebit_dover        VARCHAR2(1000), " +
                    "  volume_obj                   VARCHAR2(100), " +
                    "  square_obj                   VARCHAR2(100), " +
                    "  q_sum                        VARCHAR2(100), " +
                    "  so_q                         VARCHAR2(100), " +
                    "  sw_q                         VARCHAR2(100), " +
                    "  st_q                         VARCHAR2(100), " +
                    "  gw_q                         VARCHAR2(100), " +
                    "  nal_pu                       VARCHAR2(100), " +
                    "  nal_aupr                     VARCHAR2(100), " +
                    "  bezdog_sposob_num            VARCHAR2(100), " +
                    "  bezdog_ustanovleno           VARCHAR2(3000), " +
                    "  bezdog_narushenie            VARCHAR2(3000), " +
                    "  bezdog_pereraschet_s         VARCHAR2(100), " +
                    "  bezdog_pereraschet_po        VARCHAR2(100), " +
                    "  bezdog_predpis               VARCHAR2(3000), " +
                    "  bezdog_obyasn                VARCHAR2(3000), " +
                    "  bezdog_pretenz               VARCHAR2(3000), " +
                    "  uslovie_podkl_num            VARCHAR2(100), " +
                    "  soglasov_proekte_num         VARCHAR2(100), " +
                    "  dopusk_s                     VARCHAR2(100), " +
                    "  dopusk_po                    VARCHAR2(100), " +
                    "  tel_spravki                  VARCHAR2(100), " +
                    "  tel_dispetch                 VARCHAR2(100), " +
                    "  org_ustanov_pu               VARCHAR2(3000), " +
                    "  type_oto_prib                VARCHAR2(100), " +
                    "  schema_vkl_gvs               VARCHAR2(100), " +
                    "  schema_vkl_podogrev          VARCHAR2(100), " +
                    "  kolvo_sekc_1                 VARCHAR2(100), " +
                    "  kolvo_sekc_1_l               VARCHAR2(100), " +
                    "  kolvo_sekc_2                 VARCHAR2(100), " +
                    "  kolvo_sekc_2_l               VARCHAR2(100), " +
                    "  kolvo_kalorifer              VARCHAR2(100), " +
                    "  poverhnost_nagreva           VARCHAR2(100), " +
                    "  podkl_num                    VARCHAR2(100), " +
                    "  q_max                        VARCHAR2(100), " +
                    "  itog_text                    VARCHAR2(4000), " +
                    "  is_signed                    NUMBER default 0,  " +
                    "    constraint MI_ACT_FIELDS_pk" +
                    "        unique (id_task, id_act, npp) on conflict ignore " +
                    ")"
        )

        db.execSQL(
            "create table IF NOT EXISTS MI_ACT_FIELDS_DOP  " +
                    "(   id_task             NUMBER," +
                    "    id_act              NUMBER," +
                    "    npp                 NUMBER," +
                    "    id_file             NUMBER, " +
                    "    num_obj             NUMBER," +
                    "    name_obj            VARCHAR2(1000)," +
                    "    address_obj         VARCHAR2(4000)," +
                    "    god_zd              VARCHAR2(200)," +
                    "    nazn_name           VARCHAR2(200)," +
                    "    tvr                 VARCHAR2(100)," +
                    "    pr_oto              VARCHAR2(100)," +
                    "    pr_sw               VARCHAR2(100)," +
                    "    volume              NUMBER," +
                    "    square_total        NUMBER," +
                    "    point_name          VARCHAR2(4000)," +
                    "    etaz                VARCHAR2(1000)," +
                    "    so_q                NUMBER," +
                    "    sw_q                NUMBER," +
                    "    st_q                NUMBER," +
                    "    gw_qmax             NUMBER," +
                    "    name_vodo           VARCHAR2(1000)," +
                    "    nn_wpol             NUMBER," +
                    "    tt_wpol             NUMBER," +
                    "    nn_prib             NUMBER," +
                    "    pr_rec              VARCHAR2(20)," +
                    "    pr_psusch           VARCHAR2(20)," +
                    "    pr_iz_st            VARCHAR2(20)," +
                    "    nom_uch             VARCHAR2(100)," +
                    "    tip_name            VARCHAR2(300)," +
                    "    pt_d                NUMBER," +
                    "    pt_l                NUMBER," +
                    "    name_pr_pt          VARCHAR2(200)," +
                    "    ot_d                NUMBER," +
                    "    ot_l                NUMBER," +
                    "    name_pr_ot          VARCHAR2(200)," +
                    "    uch_hgr             VARCHAR2(100)," +
                    "    pu_num              VARCHAR2(100)," +
                    "    pu_name             VARCHAR2(1000)," +
                    "    pu_mesto            VARCHAR2(1000)," +
                    "    pu_type             VARCHAR2(100)," +
                    "    pu_diam             VARCHAR2(100)," +
                    "    pu_kolvo            VARCHAR2(100)," +
                    "    pu_proba_mesto      VARCHAR2(1000)," +
                    "    q_sum               VARCHAR2(100)," +
                    "    q_sum_max           VARCHAR2(100)," +
                    "    pu_srok_poverki     VARCHAR2(1000)," +
                    "    pu_num_plomba       VARCHAR2(100)," +
                    "    pu_pokaz            VARCHAR2(100)," +
                    "    schema_prisoed_name VARCHAR2(1000)," +
                    "    schema_prisoed_kod  VARCHAR2(100)," +
                    "    dat                 DATE, " +
                    "    is_signed           NUMBER default 0  " +
                    ")"
        )

        db.execSQL(
            "create table IF NOT EXISTS MI_ACT_FIELDS_DOP_SHABLON  " +
                    "(   id_task             NUMBER," +
                    "    id_act              NUMBER," +
                    "    num_obj             NUMBER," +
                    "    name_obj            VARCHAR2(1000)," +
                    "    address_obj         VARCHAR2(4000)," +
                    "    god_zd              VARCHAR2(200)," +
                    "    nazn_name           VARCHAR2(200)," +
                    "    tvr                 VARCHAR2(100)," +
                    "    pr_oto              VARCHAR2(100)," +
                    "    pr_sw               VARCHAR2(100)," +
                    "    volume              NUMBER," +
                    "    square_total        NUMBER," +
                    "    point_name          VARCHAR2(4000)," +
                    "    etaz                VARCHAR2(1000)," +
                    "    so_q                NUMBER," +
                    "    sw_q                NUMBER," +
                    "    st_q                NUMBER," +
                    "    gw_qmax             NUMBER," +
                    "    name_vodo           VARCHAR2(1000)," +
                    "    nn_wpol             NUMBER," +
                    "    tt_wpol             NUMBER," +
                    "    nn_prib             NUMBER," +
                    "    pr_rec              VARCHAR2(20)," +
                    "    pr_psusch           VARCHAR2(20)," +
                    "    pr_iz_st            VARCHAR2(20)," +
                    "    nom_uch             VARCHAR2(100)," +
                    "    tip_name            VARCHAR2(300)," +
                    "    pt_d                NUMBER," +
                    "    pt_l                NUMBER," +
                    "    name_pr_pt          VARCHAR2(200)," +
                    "    ot_d                NUMBER," +
                    "    ot_l                NUMBER," +
                    "    name_pr_ot          VARCHAR2(200)," +
                    "    uch_hgr             VARCHAR2(100)," +
                    "    pu_num              VARCHAR2(100)," +
                    "    pu_name             VARCHAR2(1000)," +
                    "    pu_mesto            VARCHAR2(1000)," +
                    "    pu_type             VARCHAR2(100)," +
                    "    pu_diam             VARCHAR2(100)," +
                    "    pu_kolvo            VARCHAR2(100)," +
                    "    pu_proba_mesto      VARCHAR2(1000)," +
                    "    q_sum               VARCHAR2(100)," +
                    "    q_sum_max           VARCHAR2(100)," +
                    "    pu_srok_poverki     VARCHAR2(1000)," +
                    "    pu_num_plomba       VARCHAR2(100)," +
                    "    pu_pokaz            VARCHAR2(100)," +
                    "    schema_prisoed_name VARCHAR2(1000)," +
                    "    schema_prisoed_kod  VARCHAR2(100)," +
                    "    dat                 DATE " +
                    ")"
        )

        //endregion

        //region Файлы
        db.execSQL(
            "create table IF NOT EXISTS  MI_MOB_TASK_FILES  " +
                    "(   id_file                  NUMBER, " +
                    "    id_task                  NUMBER,  " +
                    "    filename                 VARCHAR2(1000),  " +
                    "    filedata                 BLOB,  " +
                    "    uri                      VARCHAR2(1000)," +
                    "    is_signed                NUMBER default 0,  " +
                    "    paper                    NUMBER default 0,  " +
                    "    is_send                  NUMBER default 0,  " +
                    "    date_send_to_client      DATE," +
                    "    email_client             VARCHAR2(1000)," +
                    "    id_act                   NUMBER default 0, " +
                    "    npp                      NUMBER default 0, " +
                    //  "    cnt_attaches             NUMBER default 0, " +
                    "    constraint MI_MOB_TASK_FILES_pk " +
                    "         unique (id_task, id_file) on conflict replace)"
        )

        db.execSQL(
            "create table IF NOT EXISTS  MI_MOB_TASK_FILES_HISTORY  " +
                    "(   id_file                  NUMBER, " +
                    "    id_task                  NUMBER,  " +
                    "    filename                 VARCHAR2(1000),  " +
                    "    filedata                 BLOB,  " +
                    "    uri                      VARCHAR2(1000)," +
                    "    is_signed                NUMBER default 0,  " +
                    "    paper                    NUMBER default 0,  " +
                    "    is_send                  NUMBER default 0,  " +
                    "    date_send_to_client      DATE," +
                    "    email_client             VARCHAR2(1000)," +
                    "constraint MI_MOB_TASK_FILES_HISTORY_pk " +
                    "         unique (id_task, id_file) on conflict replace)"
        )
        //endregion


        //region Договор (+ объекты, ТУ, УУ)
        db.execSQL(
            "create table IF NOT EXISTS MI_DOG_DATA  " +
                    "(   id_task                NUMBER,    " +
                    "    dat                    DATE,    " +
                    "    kod_dog                NUMBER,    " +
                    "    kodp                   NUMBER,    " +
                    "    name                   VARCHAR2(200),    " +
                    "    inn                    VARCHAR2(15),    " +
                    "    contact_string         VARCHAR2(4000),    " +
                    "    ndog                   VARCHAR2(60),    " +
                    "    dat_dog                DATE,    " +
                    "    dog_har                VARCHAR2(4000),    " +
                    "    nal_pu                 NUMBER,    " +
                    "    last_nachisl           VARCHAR2(4000),    " +
                    "    last_opl               VARCHAR2(4000),    " +
                    "    sum_dolg_total         VARCHAR2(100),    " +
                    "    remark_dog             VARCHAR2(4000),    " +
                    "    remark_rasch           VARCHAR2(4000),    " +
                    "    remark_ur              VARCHAR2(4000),    " +
                    "    remark_kontrol         VARCHAR2(4000),    " +
                    "    remark_tu              VARCHAR2(4000),    " +
                    "    pusk_tu                VARCHAR2(4000),    " +
                    "    otkl_tu                VARCHAR2(4000)," +
                    "    constraint MI_DOG_DATA_pk" +
                    "        unique (id_task, kod_dog)  on conflict replace)"
        )

        db.execSQL(
            "create table IF NOT EXISTS  MI_DOG_OBJ  " +
                    "(   id_task                 NUMBER," +
                    "    kod_dog                 NUMBER," +
                    "    dat                     DATE," +
                    "    name                    VARCHAR2(4000)," +
                    "    adr                     VARCHAR2(4000)," +
                    "    kod_obj                 NUMBER not null," +
                    "    constraint MI_DOG_OBJ_pk" +
                    "        unique (id_task, kod_obj, kod_dog) on conflict replace)"
        )

        db.execSQL(
            "create table IF NOT EXISTS  MI_DOG_TU" +
                    "(   id_task         NUMBER," +
                    "    dat             DATE," +
                    "    kod_dog         NUMBER," +
                    "    kod_obj         NUMBER," +
                    "    nomer           NUMBER not null," +
                    "    name            VARCHAR2(4000)," +
                    "    so_q            NUMBER," +
                    "    so_g            NUMBER," +
                    "    sw_q            NUMBER," +
                    "    sw_g            NUMBER," +
                    "    st_q            NUMBER," +
                    "    st_g            NUMBER," +
                    "    gw_qmax         NUMBER," +
                    "    gw_qsr          NUMBER," +
                    "    name_tarif      VARCHAR2(4000)," +
                    "    constraint MI_DOG_TU_pk" +
                    "        unique (id_task, kod_dog, kod_obj, nomer) on conflict replace)"
        )

        db.execSQL(
            "create table IF NOT EXISTS  MI_DOG_UU  " +
                    "(   id_task         NUMBER," +
                    "    dat             DATE," +
                    "    kod_dog         NUMBER," +
                    "    name            VARCHAR2(1000)," +
                    "    mesto_uu        VARCHAR2(1000)," +
                    "    time_uu         VARCHAR2(1000)," +
                    "    kod_uu          NUMBER," +
                    "    constraint MI_DOG_UU_pk" +
                    "        unique (id_task, kod_dog, kod_uu) on conflict replace)"
        )

        db.execSQL(
            "create table IF NOT EXISTS  MI_DOG_UU_SI " +
                    "( id_task      NUMBER,               " +
                    "  dat          DATE,                 " +
                    "  npp          NUMBER,               " +
                    "  kod_uu       NUMBER(10) not null,  " +
                    "  name_si      VARCHAR2(100),        " +
                    "  mesto        VARCHAR2(15),         " +
                    "  obozn_t      VARCHAR2(100),        " +
                    "  name_tip     VARCHAR2(100),        " +
                    "  nomer        VARCHAR2(20),         " +
                    "  dim          NUMBER(10),           " +
                    "  izm          VARCHAR2(1000),       " +
                    "  data_pov     DATE,                 " +
                    "  int          VARCHAR2(1000),       " +
                    "  data_pov_end DATE,                 " +
                    "  per_chas_arx NUMBER(10,2),         " +
                    "  per_sut_arx  NUMBER(10,2),         " +
                    "  n_greest     VARCHAR2(20),         " +
                    "  work         VARCHAR2(100),        " +
                    "  loss_press   NUMBER(7,2),          " +
                    "  data_out     DATE,                 " +
                    "  prim         VARCHAR2(20))"
        )
        //endregion

        db.execSQL(
            "create table IF NOT EXISTS MI_HISTORY  " +
                    "(   dat                   DATE,  " +
                    "    id_task               NUMBER,  " +
                    "    adr                   VARCHAR2(500),  " +
                    "    purpose               NUMBER,  " +
                    "    purpose_name          VARCHAR2,  " +
                    "    prim                  VARCHAR2(500),  " +
                    "    ttime                 DATE,  " +
                    "    status                NUMBER,  " +
                    "    status_name           VARCHAR2,  " +
                    "    id_inspector          NUMBER not null ,  " +
                    "    fio                   VARCHAR2(2000),  " +
                    "    cnt_files             NUMBER,  " +
                    "    kod_dog               NUMBER,  " +
                    "    kod_obj               NUMBER,  " +
                    "    kodp                  NUMBER,  " +
                    "    ndog                  VARCHAR2(300),  " +
                    "    payer_name            VARCHAR2(4000),  " +
                    "    fio_contact           VARCHAR2(4000),  " +
                    "    email_contact         VARCHAR2(200),  " +
                    "    tel_contact           VARCHAR2(4000),  " +
                    "    kod_emp_podp          NUMBER," +
                    "    fio_podp              VARCHAR2(1000)," +
                    "    email_podp            VARCHAR2(1000)," +
                    "    tel_podp              VARCHAR2(100)," +
                    "    name_dolzhn_podp      VARCHAR2(1000)," +
                    "    constraint MI_HISTORY_pk  " +
                    "        unique (kod_obj, id_task)  on conflict replace)"
        )

        db.execSQL(
            "create table IF NOT EXISTS MI_PODPISANT" +
                    "( id_task         NUMBER constraint MI_PODPISANT_MI_MOB_TASK_id_task_fk references MI_MOB_TASK," +
                    "  fio             VARCHAR2(60)," +
                    "  e_mail          VARCHAR2(250)," +
                    "  tel             VARCHAR2(90)," +
                    "  name_dolzhn     VARCHAR2(160)," +
                    "  kod_emp         NUMBER(10) not null)"
        )

    }

    private fun createSequences(db: SQLiteDatabase) {
        db.execSQL("CREATE table IF NOT EXISTS  sequences (name varchar, seq NUMBER);")
        db.execSQL("insert into sequences (name, seq) VALUES ('SEQ_TASK_FILES', -2);")
    }

    private fun createTriggers(db: SQLiteDatabase) {

    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (newVersion <= oldVersion) return

        if (oldVersion < 11) {
            try {
                db.execSQL("alter table mi_mob_task_files add date_send_to_client DATE;")
                db.execSQL("alter table mi_mob_task_files add email_client VARCHAR2(1000);")

                db.execSQL("alter table mi_mob_task_files_history add date_send_to_client DATE;")
                db.execSQL("alter table mi_mob_task_files_history add email_client VARCHAR2(1000);")
            } catch (e: Exception) {
                println("$TAG_ERR onUpgrade 11: ${e.message}")
            }
        }

        if (oldVersion < 12) {
            try {
                db.execSQL("alter table MI_INSPECTOR_DATA add vers VARCHAR2(100);")
            } catch (e: Exception) {
                println("$TAG_ERR onUpgrade 12: ${e.message}")
            }
        }

        if (oldVersion < 14) {
            try {
                db.execSQL("alter table MI_MOB_TASK_FILES add id_act NUMBER default 0;")
                db.execSQL("alter table MI_MOB_TASK_FILES add npp NUMBER default 0;")
            } catch (e: Exception) {
                println("$TAG_ERR onUpgrade 14: ${e.message}")
            }
        }

        if (oldVersion < 15) {
            try {
                db.execSQL("UPDATE MI_ACT_FIELDS SET dop_info = remark_dog WHERE id_task != 0;")
                db.execSQL("UPDATE MI_ACT_FIELDS SET remark_dog = '' WHERE id_task != 0;")
            } catch (e: Exception) {
                println("$TAG_ERR onUpgrade 15: ${e.message}")
            }
        }

        if (oldVersion < 16) {
            try {
                db.execSQL("alter table MI_MOB_TASK add houses VARCHAR2(100);")
                db.execSQL("UPDATE MI_MOB_TASK SET houses = house WHERE id_task != 0;")
            } catch (e: Exception) {
                println("$TAG_ERR onUpgrade 16: ${e.message}")
            }
        }

        if (oldVersion < 17) {
            try {
                db.execSQL(
                    "create table IF NOT EXISTS  MI_DOG_UU_SI " +
                            "( id_task      NUMBER,               " +
                            "  dat          DATE,                 " +
                            "  npp          NUMBER,               " +
                            "  kod_uu       NUMBER(10) not null,  " +
                            "  name_si      VARCHAR2(100),        " +
                            "  mesto        VARCHAR2(15),         " +
                            "  obozn_t      VARCHAR2(100),        " +
                            "  name_tip     VARCHAR2(100),        " +
                            "  nomer        VARCHAR2(20),         " +
                            "  dim          NUMBER(10),           " +
                            "  izm          VARCHAR2(1000),       " +
                            "  data_pov     DATE,                 " +
                            "  int          VARCHAR2(1000),       " +
                            "  data_pov_end DATE,                 " +
                            "  per_chas_arx NUMBER(10,2),         " +
                            "  per_sut_arx  NUMBER(10,2),         " +
                            "  n_greest     VARCHAR2(20),         " +
                            "  work         VARCHAR2(100),        " +
                            "  loss_press   NUMBER(7,2),          " +
                            "  data_out     DATE,                 " +
                            "  prim         VARCHAR2(20))"
                )
            } catch (e: Exception) {
                println("$TAG_ERR onUpgrade 17: ${e.message}")
            }
        }
    }
}