package ru.infoenergo.mis

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ru.infoenergo.mis.actsTemplates.*
import ru.infoenergo.mis.dbhandler.DbHandlerLocalRead
import ru.infoenergo.mis.helpers.*

/** *************************************************** **/
/**   Диалоговое окно 	Оформление документов (актов)   **/
/** *************************************************** **/
class DlgListCreateActs(
    private var arrayActs: java.util.ArrayList<ActInfo>,
    private var task: Task
) : DialogFragment() {

    var ACT: ActInfo = ActInfo()
    var ACT_LIST: ArrayList<ActFieldsInfo> = ArrayList()
    var onCancel: Boolean = false

    private var selectedActId: Int = 0
    private var selectedActTip: Int = 0

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val actsNames = arrayActs.map { it.name }.toTypedArray()

        return activity?.let {
            AlertDialog.Builder(it)
                .setTitle("Выберите документ")
                .setSingleChoiceItems(actsNames, 0) { _, which ->
                    selectedActId = arrayActs[which].id_act
                    selectedActTip = arrayActs[which].tip
                }
                .setPositiveButton("OK") { dialog, _ ->
                    if (selectedActTip == 0 || selectedActId == 0) {
                        selectedActId = arrayActs[0].id_act
                        selectedActTip = arrayActs[0].tip
                    }
                    try {
                        val act =
                            arrayActs.firstOrNull { act -> act.id_act == selectedActId && act.tip == selectedActTip }
                        if (act == null) {
                            dialog.cancel()
                        } else {
                            ACT = act
                            // Получаем неподписанные акты по id_act для задачи id_task
                            ACT_LIST = DbHandlerLocalRead(context!!, null).existedActsByIdAct(task.id_task, act.id_act)
                            when {
                                // Если таких задач нет, сразу создаём новый акт
                                ACT_LIST.size == 0 -> {

                                    val intent: Intent = when (act.tip) {
                                        1, 13 -> Intent(activity, ActUniversal::class.java)
                                        2 -> Intent(activity, ActOtopPeriod::class.java)
                                        3 -> Intent(activity, ActOtopPeriod103::class.java)
                                        4 -> Intent(activity, ActPassport::class.java)
                                        5 -> Intent(activity, ActVnutridomovyhSetey::class.java)
                                        6 -> Intent(activity, ActPodklTeplo::class.java)
                                        7 -> Intent(activity, ActOgrOtkl::class.java)
                                        8 -> Intent(activity, ActOtkazDostTeplo::class.java)
                                        9 -> Intent(activity, ActPreddog::class.java)
                                        10 -> Intent(activity, ActPreddogAsuse::class.java)
                                        11 -> Intent(activity, ActBezdogPotr::class.java)
                                        12 -> Intent(activity, ActDopuskaUu::class.java)
                                        else -> Intent()
                                    }

                                    intent.putExtra("TASK", task)
                                    intent.putExtra("ACT", act)
                                    intent.putExtra("ACT_FIELDS", ActFieldsInfo())

                                    super.startActivityForResult(intent, CREATE_ACT)
                                }

                                // Если таких задач не меньше одной, то выбор новый/редактировать
                                ACT_LIST.size >= 1 -> {
                                    dialog.cancel()
                                    onCancel = true
                                }
                            }
                        }
                    } catch (e: java.lang.Exception) {
                        println("$TAG_ERR DlgCreateActs: ${e.message}")
                    }
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.cancel()
                }
                .create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        val activity: Activity? = activity
        if (activity is DialogInterface.OnCancelListener) {
            (activity as DialogInterface.OnCancelListener).onCancel(dialog)
        }
    }
}
