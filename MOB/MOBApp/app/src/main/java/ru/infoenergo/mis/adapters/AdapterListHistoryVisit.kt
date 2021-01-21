package ru.infoenergo.mis.adapters

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.TextView
import ru.infoenergo.mis.AttachmentsToObject
import ru.infoenergo.mis.R
import ru.infoenergo.mis.helpers.HistoryItemInfo
import ru.infoenergo.mis.helpers.TAG_ERR
import java.time.format.DateTimeFormatter
import java.util.*

/*************************************************/
/**   Адаптер для  истории посещений         **/
/*************************************************/
class AdapterListHistoryVisit(
    private val context: Context,
    private val dataSource: ArrayList<HistoryItemInfo>
) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        var historyItemHolder = HistoryItemHolder()

        if (view == null) {
            view = inflater.inflate(R.layout.lvitem_history, parent, false)
            historyItemHolder.inspector = view.findViewById(R.id.colHistoryInspector) as TextView
            historyItemHolder.timeDate = view.findViewById(R.id.colHistoryDate) as TextView
            historyItemHolder.purpose = view.findViewById(R.id.colHistoryPurpose) as TextView
            historyItemHolder.prim = view.findViewById(R.id.colHistoryNote) as TextView
            historyItemHolder.files = view.findViewById(R.id.colHistoryImageView) as ImageView

            view.tag = historyItemHolder
        } else {
            historyItemHolder = view.tag as HistoryItemHolder
        }
        try {
            if (position % 2 == 1) {
                view!!.setBackgroundColor(Color.WHITE)
            } else {
                view!!.setBackgroundResource(R.color.colorLightLightBlue)
            }

            val item = getItem(position)

            // заполнение текствьюеров значениями из списка
            historyItemHolder.inspector?.text = item.fio
            historyItemHolder.purpose?.text = item.purpose_name
            historyItemHolder.timeDate?.text = item.ttime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
            historyItemHolder.prim?.text = item.prim
            if (item.cnt_files > 0) {
                historyItemHolder.files?.setImageResource(R.drawable.ic_attach)
                historyItemHolder.files?.setOnClickListener {
                    val intent = Intent(context, AttachmentsToObject::class.java)
                    intent.putExtra("TTIME", item.ttime.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")).toString())
                    intent.putExtra("TASK_ID", item.id_task)
                    intent.putExtra("ADR", item.adr)
                    context.startActivity(intent)
                }
            } else {
                historyItemHolder.files?.setImageResource(0)
                historyItemHolder.files = null
            }

        } catch (e: Exception) {
            print("$TAG_ERR ${e.message}")
        }
        return view!!
    }


    override fun getItem(position: Int): HistoryItemInfo {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).id_task.toLong()
    }

    override fun getCount(): Int {
        return dataSource.size
    }

    // эти два метода решили проблему "перемешивания" элементов листвью
    override fun getViewTypeCount(): Int {
        return if (count == 0) super.getViewTypeCount() else count
    }

    // эти два метода решили проблему "перемешивания" элементов листвью
    override fun getItemViewType(position: Int): Int {
        return position
    }

}


class HistoryItemHolder(
    var idObject: String? = null,
    var timeDate: TextView? = null,
    var inspector: TextView? = null,
    var purpose: TextView? = null,
    var prim: TextView? = null,
    var files: ImageView? = null
)
