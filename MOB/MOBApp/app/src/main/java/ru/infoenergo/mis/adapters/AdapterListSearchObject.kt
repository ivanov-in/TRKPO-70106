package ru.infoenergo.mis.adapters

import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import ru.infoenergo.mis.helpers.SearchObjectInfo
import ru.infoenergo.mis.R
import kotlin.collections.ArrayList


/***************************************************/
/**   Адаптер для поиска объекта (новое задание)  **/
/***************************************************/
class AdapterListSearchObject() : BaseAdapter() {

    private lateinit var context: Context
    private var dataSource: ArrayList<SearchObjectInfo> = ArrayList()
    private lateinit var inflater:  LayoutInflater

    constructor(mcontext: Context, mdataSource: ArrayList<SearchObjectInfo>) : this() {
        context = mcontext
        dataSource = mdataSource
        inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        return dataSource.size
    }

    fun clearData() {
        dataSource.clear()
    }

    override fun getItem(position: Int): SearchObjectInfo {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View

        if (convertView == null) {
            view = inflater.inflate(R.layout.lvitem_new_task, parent, false)
        } else {
            view = convertView
        }

        if (position % 2 == 1) {
            view.setBackgroundColor(Color.WHITE)
        } else {
            view.setBackgroundResource(R.color.colorLightLightBlue)
        }

        view.findViewById<TextView>(R.id.tvNewTaskAbonName).text =
            "${getItem(position).name};   ${getItem(position).adr}"
        view.findViewById<TextView>(R.id.tvNewTaskNumDog).text = getItem(position).ndog

        return view
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

