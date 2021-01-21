package ru.infoenergo.mis

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import ru.infoenergo.mis.actsTemplates.*
import ru.infoenergo.mis.helpers.*

/** *************************************************** **/
/**   Диалоговое окно Новый акт или редактировать       **/
/** *************************************************** **/
class DlgCreateOrEditAct(
    private var existedActByType: ArrayList<ActFieldsInfo>,
    private var act: ActInfo,
    private var task: Task
) : DialogFragment() {

    var onCancel: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val actsNums = existedActByType.map { it.num_act }.toTypedArray()

        var selectedActId = 0
        var selectedActNpp = 0

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


        return activity?.let {
            AlertDialog.Builder(activity!!)
                .setTitle("Вы хотите отредактировать один из существующих актов или создать новый?")
                //.setMessage("Вы хотите отредактировать один из существующих актов или создать новый?")
                .setSingleChoiceItems(actsNums, 0) { _, which ->
                    selectedActId = existedActByType[which].id_act
                    selectedActNpp = existedActByType[which].npp
                }
                .setNeutralButton("Новый") { dialog, _ ->
                    dialog.cancel()
                    // открыть окно акта с данными из таблицы шаблонов
                    intent.putExtra("ACT_FIELDS", ActFieldsInfo())
                    super.startActivityForResult(intent, CREATE_ACT)
                }
                .setPositiveButton("Редактировать") { dialog, _ ->
                    if (selectedActNpp == 0 || selectedActId == 0) {
                        if (existedActByType.size == 0)
                            dialog.cancel()
                    }
                    try {
                        val actFields =
                            if (existedActByType.size > 0 && (selectedActId == 0 || selectedActNpp == 0))
                                existedActByType.first()
                            else
                                existedActByType.firstOrNull { act ->
                                    act.id_act == selectedActId && act.npp == selectedActNpp
                                }
                        if (actFields == null) {
                            dialog.cancel()
                        } else {
                            dialog.cancel()
                            intent.putExtra("ACT_FIELDS", actFields)
                            super.startActivityForResult(intent, CREATE_ACT)
                        }
                    } catch (e: java.lang.Exception) {
                        println("$TAG_ERR DlgCreateOrEditAct 'Редактировать': ${e.message}")
                    }
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.cancel()
                }
                .create()
        } ?: throw IllegalStateException("$TAG_ERR DlgCreateOrEditAct Activity cannot be null")
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        val activity: Activity? = activity
        if (activity is DialogInterface.OnCancelListener) {
            (activity as DialogInterface.OnCancelListener).onCancel(dialog)
        }
    }
}
