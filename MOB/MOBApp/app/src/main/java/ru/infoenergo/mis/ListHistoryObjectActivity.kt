package ru.infoenergo.mis

import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.listview_history_visiting.*
import ru.infoenergo.mis.adapters.AdapterListHistoryVisit
import ru.infoenergo.mis.dbhandler.DbHandlerLocalRead
import ru.infoenergo.mis.helpers.HistoryItemInfo
import ru.infoenergo.mis.helpers.TAG_ERR
import java.util.*

/** ********************************************************* **/
/**      Окно      «История посещений объекта по адресу»      **/
/** ********************************************************* **/
class ListHistoryObjectActivity : AppCompatActivity() {

    private var _historyList: ArrayList<HistoryItemInfo>? = null
    private var _kodObj: Int = -1
    private var _idTask: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.listview_history_visiting)

        //Разрешить кнопку назад
        if (supportActionBar != null) {
            val actionBar = supportActionBar
            actionBar!!.title = "История посещений объекта по адресу"
            actionBar.elevation = 4.0F
            actionBar.setDisplayHomeAsUpEnabled(true)
        }
        try {
            refreshData()
        } catch (e: Exception) {
            println("$TAG_ERR ListHistoryVisiting: ${e.message}")
        }

    }

    private fun refreshData() {
        _kodObj = intent.getIntExtra("KOD_OBJ", -1)
        _idTask = intent.getIntExtra("TASK_ID", -1)
        if (_kodObj == -1)
            finish()

        // загрузка данных с локальной бд
        // ------------------------------
        _historyList = ArrayList<HistoryItemInfo>()
        try {
            val dbHandlerReadable =
                DbHandlerLocalRead(this, null)
            val ndogs = dbHandlerReadable.getNDogsByKodObj(_kodObj)
            findViewById<TextView>(R.id.tvHistoryAddress).text = intent.getStringExtra("ADDRESS")
            if (ndogs.isNotEmpty())
                tvHistoryContracts.text = ndogs else
                tvHistoryContracts.text = intent.getStringExtra("NDOG")

            _historyList = dbHandlerReadable.getHistoryList(_kodObj)

            if (_historyList != null && _historyList!!.size > 0) {
                val adapter = AdapterListHistoryVisit(this@ListHistoryObjectActivity, _historyList!!)

                val listViewVisitedList: ListView = findViewById(R.id.listViewHistoryVisiting)
                listViewVisitedList.adapter = adapter

            } else {
                findViewById<TextView>(R.id.tvHistoryNoData).visibility = View.VISIBLE
            }

        } catch (e: Exception) {
            println("$TAG_ERR ListHistoryVisiting: ${e.message}")
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        this.finish()
        return true
    }

}

