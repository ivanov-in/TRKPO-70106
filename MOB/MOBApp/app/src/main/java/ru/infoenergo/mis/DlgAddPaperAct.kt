package ru.infoenergo.mis

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import ru.infoenergo.mis.helpers.FileInfo

/** ********************************************************************** **
 **           Окно добавления бумажного акта (фотографии)                  **
 ** ********************************************************************** **/

class DlgAddPaperAct(var act: FileInfo) : DialogFragment() {
    override fun onStart() {
        dialog!!.setCanceledOnTouchOutside(false)
        super.onStart()
    }

    var onCancel: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(activity)
        val inflater: LayoutInflater = activity!!.layoutInflater
        val view: View = inflater.inflate(R.layout.dlg_paper_act, null)

        val etName: TextView = view.findViewById(R.id.tvDlgPaperActName)
        val chkSigned: CheckBox = view.findViewById(R.id.etDlgPaperActSigned)

        etName.text = act.filename

        builder
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton("ОК") { dialog, _ ->
                act.is_signed = if (chkSigned.isChecked) 1 else 0
                onCancel = true
                dialog.cancel()
            }
        builder.setView(view)

        return builder.create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        val activity: Activity? = activity
        if (activity is DialogInterface.OnCancelListener) {
            (activity as DialogInterface.OnCancelListener).onCancel(dialog)
        }
    }
}