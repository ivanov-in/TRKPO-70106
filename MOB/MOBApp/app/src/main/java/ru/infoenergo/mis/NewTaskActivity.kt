package ru.infoenergo.mis

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.content.res.Configuration.ORIENTATION_PORTRAIT
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_new_task.*
import kotlinx.coroutines.*
import ru.infoenergo.mis.adapters.AdapterListSearchObject
import ru.infoenergo.mis.dbhandler.DBHandlerServerRead
import ru.infoenergo.mis.dbhandler.DBHandlerServerWrite
import ru.infoenergo.mis.dbhandler.DbHandlerLocalRead
import ru.infoenergo.mis.helpers.*
import java.util.*


/** ********************************************************** **/
/**   Карточка добавления нового задания в маршрутный лист     **/
/**        поиск объекта при наличии интернета                 **/
/** ********************************************************** **/
class NewTaskActivity : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var spinnerPurpose: Spinner

    private var _idInspector: Int = 0

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)
    private lateinit var btnCreateNewTask: Button // создать задание через сервер
    private lateinit var btnAddNewTask: Button // создать задание вручную

    private var foundObjectInfo: SearchObjectInfo = SearchObjectInfo()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_task)

        btnCreateNewTask = findViewById(R.id.btnCreateNewTask)
        btnAddNewTask = findViewById(R.id.btnAddNewTask)

        //Разрешить кнопку назад
        if (supportActionBar != null) {
            val actionBar = supportActionBar
            actionBar!!.title = "Добавление нового задания"
            actionBar.elevation = 4.0F
            actionBar.setDisplayHomeAsUpEnabled(true)
        }

        _idInspector = intent.getIntExtra("ID_INSPECTOR", 0)

        val networkAvailable = isNetworkAvailable(this)

        // Если нет подключения к интернету - создаём вручную
        if (!networkAvailable) {
            val intentAddTask = Intent(parent ?: this, TaskActivity::class.java)
            intentAddTask.putExtra("TASK_ID", ID_NEW_TASK)
            intentAddTask.putExtra("ID_INSPECTOR", _idInspector)
            startActivityForResult(intentAddTask, ADD_NEW_TASK)
        }

        spinnerPurpose = findViewById(R.id.spNewTaskPurpose)
        spinnerPurpose.adapter = ArrayAdapter(this, R.layout.spitem_purpose, getListPurpose())
        spinnerPurpose.setSelection(0)

        listView = findViewById(R.id.listViewSearchObject)
        listView.setOnItemClickListener { _, _, position, _ ->
            if (!listView.adapter.isEmpty) {
                val element = (listView.adapter as AdapterListSearchObject).getItem(position)
                findViewById<TextView>(R.id.tvNewTaskObject).text = "${element.name};    ${element.adr}"
                btnCreateNewTask.isEnabled = true
                btnNewAbonNewTask.isEnabled = true
                foundObjectInfo = element
            }
        }


        // Слушатели на поисковик
        // -------------------------------------------------------------------------
        val searchView: SearchView = findViewById(R.id.searchViewObject)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }

            override fun onQueryTextSubmit(query: String): Boolean {
                var objects: ArrayList<SearchObjectInfo>
                uiScope.launch {
                    try {
                        objects = withContext(Dispatchers.IO) {
                            DBHandlerServerRead(this@NewTaskActivity).findObjectAsync(query)
                        }

                        progressBarNewTask.visibility = View.VISIBLE
                        if (objects.size == 0) {
                            findViewById<TextView>(R.id.tvNewTaskObject).text =
                                "Объект не найден, попробуйте ввести вручную."
                            if (listView.adapter != null)
                                (listView.adapter as AdapterListSearchObject).apply {
                                    clearData()
                                    val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0)
                                    listView.layoutParams = params

                                    notifyDataSetChanged()
                                }
                            btnCreateNewTask.isEnabled = false
                            btnAddNewTask.visibility = View.VISIBLE

                        } else {
                            findViewById<TextView>(R.id.tvNewTaskObject).text = ""
                            btnCreateNewTask.isEnabled = false
                            btnAddNewTask.visibility = View.GONE

                            listView.adapter = AdapterListSearchObject(this@NewTaskActivity, objects)

                            val totalHeight = calcListViewHeight(listView)
                            val params =
                                LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, totalHeight)
                            listView.layoutParams = params

                            (listView.adapter as AdapterListSearchObject).notifyDataSetChanged()
                        }

                        progressBarNewTask.visibility = View.GONE
                    } catch (e: Exception) {
                        println("$TAG_ERR: ${e.message}")
                        progressBarNewTask.visibility = View.GONE
                    }
                }
                return false
            }
        })

        // Слушатель на кнопку "Новый абонент"
        // При выборе строки в результатх поиска, взять оттуда поле adr,
        // после чего открыть окно заполнения задачи вручную и прописать туда поле adr  в поле адрес объекта.
        // Другие поля (номер договора, абонента, объекта и прочее прописывать не надо!)"
        // ------------------------------------------------------
        btnNewAbonNewTask.setOnClickListener {
            val intent = Intent(this, TaskActivity::class.java)
            intent.putExtra("TASK_ID", ID_NEW_TASK)
            intent.putExtra("ID_INSPECTOR", _idInspector)
            intent.putExtra("ADDRESS", foundObjectInfo.adr)
            startActivityForResult(intent, RESULT_NEW_ABON)
        }

        // Слушатель на скрытую кнопку Добавить задание (вручную)
        // ------------------------------------------------------
        btnAddNewTask.setOnClickListener {
            // Создаём задание вручную из маршрутного листа,
            // чтобы сюда не возвращаться после выполнения/отмены интента
            setResult(RESULT_ADD_TASK)
            this.finish()
            /*val intent = Intent(this, TaskActivity::class.java)
            intent.putExtra("TASK_ID", ID_NEW_TASK)
            intent.putExtra("ID_INSPECTOR", _idInspector)
            startActivityForResult(intent, ADD_NEW_TASK)*/
        }

        // Слушатель на кнопку Создать задание (найден в поисковике)
        // ---------------------------------------------------------
        btnCreateNewTask.setOnClickListener {
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("Внимание!")
                .setMessage("Подтвердите создание задания.")
                .setPositiveButton("Создать") { dialog, _ ->
                    uiScope.launch {
                        val createTask = async(Dispatchers.IO) {
                            // Получаем минимальный id task
                            val minIdTaskLocal =
                                DbHandlerLocalRead(this@NewTaskActivity, null).getMinIdTask(_idInspector)

                            // Отправляем запрос на сервер - создать задачу
                            DBHandlerServerWrite(this@NewTaskActivity).createTask(
                                foundObjectInfo,
                                _idInspector,
                                spinnerPurpose.selectedItemId + 1,
                                minIdTaskLocal
                            )
                            true
                        }
                        if (createTask.await()) {
                            setResult(Activity.RESULT_OK)
                            dialog.cancel()
                            this@NewTaskActivity.finish()
                        }
                    }
                }
                .setNegativeButton("Отмена") { dialog, _ ->
                    dialog.cancel()
                }
            builder.show()

        }
    }

    // Если повернули планшет, то сменить размеры листьвюх
    // ----------------------------------------------------------
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val totalHeight = if (resources.configuration.orientation == ORIENTATION_PORTRAIT)
            800 else 300
        val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, totalHeight)
        listView.layoutParams = params

        if (listView.adapter != null)
            (listView.adapter as AdapterListSearchObject).notifyDataSetChanged()
    }

    // Вычисление высоты Listview (но не больше 650/300dp)
    // -------------------------------------------------------
    private fun calcListViewHeight(lvObjects: ListView): Int {
       // val maxHeight =
        return   if (resources.configuration.orientation == ORIENTATION_PORTRAIT)
                800 else 300

       /* var totalHeight = 5
        for (i in 0 until lvObjects.adapter.count) {
            if (totalHeight > maxHeight) {
                totalHeight = maxHeight
                break
            }
            val listItem: View = lvObjects.adapter.getView(i, null, lvObjects)
            listItem.measure(0, 0)
            totalHeight += (listItem.measuredHeight* 1.4).roundToInt()
        }
        return totalHeight*/
    }

    override fun onSupportNavigateUp(): Boolean {
        setResult(Activity.RESULT_CANCELED)
        this@NewTaskActivity.finish()
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.home -> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            // если вернулись из создания задачи вручную
            // (когда объект не был найден на сервере)
            ADD_NEW_TASK -> {
                setResult(resultCode)
                this@NewTaskActivity.finish()
            }
            RESULT_NEW_ABON -> {
                setResult(RESULT_NEW_ABON)
                this@NewTaskActivity.finish()
            }
        }
    }

    // При нажатии кнопки назад спрашиваем подтверждение
    // -------------------------------------------------
    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        this@NewTaskActivity.finish()
    }


    // Список целей для spinner
    // ------------------------------------------
    private fun getListPurpose(): Array<String> {
        val db = DbHandlerLocalRead(this, null)
        val res = db.getPurposeList().toTypedArray()
        db.close()
        return res
    }

    override fun onResume() {
        super.onResume()
    }

}