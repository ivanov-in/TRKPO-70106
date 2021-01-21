package ru.infoenergo.mis.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.solver.widgets.ConstraintWidget
import ru.infoenergo.mis.R
import ru.infoenergo.mis.TaskActivity
import ru.infoenergo.mis.helpers.TAG_ERR
import ru.infoenergo.mis.helpers.TASK_UPDATE
import ru.infoenergo.mis.helpers.Task
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.ArrayList


/*************************************************/
/**   Адаптер для  маршрутного листа            **/
/*************************************************/
class AdapterListTasks() : BaseAdapter() {

    private lateinit var context: Context
    private var taskList: ArrayList<Task> = ArrayList()
    private lateinit var purposeList: Array<String>
    private lateinit var inflater: LayoutInflater
    var isTaskOpened: Boolean = false

    constructor(pContext: Context, pDataSource: ArrayList<Task>, pPurposeList: Array<String>) : this() {
        context = pContext
        taskList = pDataSource
        purposeList = pPurposeList
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var viewHolder = RouteTaskViewHolder()
        var view = convertView

        if (convertView == null) {

            view = inflater.inflate(R.layout.lvitem_task, parent, false)
            viewHolder.id = view.findViewById(R.id.tvIdTask) as TextView
            viewHolder.address = view.findViewById(R.id.etTaskAddress) as EditText
            viewHolder.date = view.findViewById(R.id.tvTaskDate) as TextView
            viewHolder.purpose = view.findViewById(R.id.spTaskPurpose) as Spinner
            viewHolder.status = view.findViewById(R.id.tvTaskStatus) as TextView
            viewHolder.statusImg = view.findViewById(R.id.imageTaskStatus) as ImageView

            view.tag = viewHolder
        } else {
            viewHolder = convertView.tag as RouteTaskViewHolder
        }

        view?.setOnClickListener {
            if (isTaskOpened)
                return@setOnClickListener
            val intent = Intent(context, TaskActivity::class.java)
            intent.putExtra("TASK_ID", getItem(position).id_task)
            intent.putExtra("ID_INSPECTOR", getItem(position).id_inspector)
            intent.putExtra("TASK", getItem(position))
            isTaskOpened = true
            (context as Activity).startActivityForResult(intent, TASK_UPDATE)
        }

        try {
            if (position % 2 == 1) {
                view!!.setBackgroundColor(Color.WHITE)
            } else {
                view!!.setBackgroundResource(R.color.colorLightLightBlue)
            }

            val item = getItem(position)

            // заполнение текствьюеров значениями из списка
            /* if (item.id_task < 0)
                 viewHolder.id?.text = "Добавлено ${(item.id_task.toString()).replace('-', ' ')}"
             else*/

            viewHolder.id?.text = item.id_task.toString()

            viewHolder.address?.apply {
                setText(item.address)
                isClickable = false
                isEnabled = false
            }

            /* viewHolder.address?.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                viewHolder.address!!.measuredHeight) */

            viewHolder.date?.text = item.ttime!!.format( DateTimeFormatter.ofPattern("HH ч. mm мин.    dd MMMM yyyy"))
            viewHolder.status?.text = item.status_name

            viewHolder.purpose?.isClickable = false
            viewHolder.purpose?.isEnabled = false
            viewHolder.purpose?.adapter = ArrayAdapter(
                context, R.layout.spitem_purpose, purposeList
            )
            viewHolder.purpose?.setSelection(item.purpose - 1)

            when (taskList[position].status) {
                // Изменено руководителем – бледно-серый + иконка бледно-оранжевый восклицательный знак
                4, 5, 6, 7 -> {
                    viewHolder.statusImg?.visibility = ConstraintWidget.VISIBLE
                    viewHolder.statusImg?.setImageResource(R.drawable.ic_warning_orange)
                    viewHolder.id?.setBackgroundResource(R.color.colorLightGrayDD)
                    if (item.dat!!.isBefore(LocalDate.now())) {
                        // Просроченное задание (у которого дата < текущей)   - бледно-красным
                        viewHolder.id?.setBackgroundResource(R.color.colorLightPink)
                    }
                }
                // Удалено руководителем – бледно-серый  + иконка с красным крестом
                8, 9, 10, 11 -> {
                    viewHolder.statusImg?.visibility = ConstraintWidget.VISIBLE
                    viewHolder.statusImg?.setImageResource(R.drawable.ic_cancel_red)
                    viewHolder.id?.setBackgroundResource(R.color.colorLightGrayDD)
                }
                // Выполненное  – бледно-зелёный + иконка зелёная галочка
                -12, 12 -> {
                    viewHolder.statusImg?.visibility = ConstraintWidget.VISIBLE
                    viewHolder.statusImg?.setImageResource(R.drawable.ic_done_green)
                    viewHolder.id?.setBackgroundResource(R.color.colorLightGreen)
                }
                else -> {
                    // Новое  –серый
                    // 0, 1, 2, 3, 13, 15 -> {
                    viewHolder.id?.setBackgroundResource(R.color.colorLightGrayDD)
                    viewHolder.statusImg?.visibility = ConstraintWidget.GONE
                    if (item.dat!!.isBefore(LocalDate.now())) {
                        // Просроченное задание (у которого дата < текущей)   - бледно-красным
                        viewHolder.id?.setBackgroundResource(R.color.colorLightPink)
                        //      }
                    }
                }
            }

        } catch (e: Exception) {
            println("$TAG_ERR AdapterListTasks: ${e.message}")
        }
        return view!!
    }


    override fun getItem(position: Int): Task {
        return taskList[position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id_task.toLong()
    }

    override fun getCount(): Int {
        return taskList.size
    }

    // эти два метода решили проблему "перемешивания" элементов листвью
    override fun getViewTypeCount(): Int {
        return if (count == 0) super.getViewTypeCount() else count
    }

    // эти два метода решили проблему "перемешивания" элементов листвью
    override fun getItemViewType(position: Int): Int {
        return position
    }

    fun clear() {
        taskList = ArrayList()
    }
}


class RouteTaskViewHolder {
    var id: TextView? = null
    var date: TextView? = null
    var address: EditText? = null
    var purpose: Spinner? = null
    var status: TextView? = null
    var statusImg: ImageView? = null
}
