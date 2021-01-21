package ru.infoenergo.mis.adapters

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import ru.infoenergo.mis.dbhandler.*
import ru.infoenergo.mis.helpers.DogData
import ru.infoenergo.mis.R
import ru.infoenergo.mis.helpers.TAG_ERR
import ru.infoenergo.mis.helpers.Task
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt


/** **************************************************** **/
/**           Адаптер для табов в карточке               **/
/**                  Договор Теплоснабжения              **/
/**            Просмотр информации о потребителе         **/
/** **************************************************** **/
class AdapterTabsAbonentDog(
    private val myContext: Context, fm: FragmentManager, private var totalTabs: Int, _idTask: Int, _kodDog: Int
) : FragmentPagerAdapter(fm) {

    private val abonentData = DbHandlerLocalRead(myContext, null).getAbonentInfo(_kodDog, _idTask)
    private val task = DbHandlerLocalRead(myContext, null).getTaskById(_idTask)

    // this is for fragment tabs
    override fun getItem(position: Int): Fragment {
        when (position) {
            2 -> {
                return try {
                    ZuluFragment(myContext, task)
                } catch (e: Exception) {
                    println("$TAG_ERR ZuluFragment: ${e.message}")
                    ZuluFragment()
                }
            }
            1 -> {
                return try {
                    MonitoringFragment(myContext, abonentData)
                } catch (e: Exception) {
                    println("$TAG_ERR MonitoringFragment: ${e.message}")
                    MonitoringFragment()
                }
            }
            else -> {
                return try {
                    DogovorFragment(myContext, abonentData)
                } catch (e: Exception) {
                    println("$TAG_ERR DogovorFragment: ${e.message}")
                    DogovorFragment()
                }
            }
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}

// *********************************************
//              TAB Договор
// *********************************************
class DogovorFragment() : Fragment() {

    private var myContext: Context? = null
    private var dogData: DogData = DogData()

    constructor(context: Context, abonent: DogData) : this() {
        myContext = context
        dogData = abonent

        val args = Bundle()
        args.putSerializable("abonent", dogData)
        this.arguments = args
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dogData = arguments?.getSerializable("abonent") as DogData
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_abonent_dogovor, container, false)
        if (dogData.kod_dog == 0) {
            if (arguments != null) {
                dogData = arguments?.getSerializable("abonent") as DogData
            } else
                return view
        }
        try {

            val lvObjects = view.findViewById<ListView>(R.id.listViewAbonentDogObjects)
            val lvDogTu = view.findViewById<ListView>(R.id.listViewAbonentDogTu)
            val lvDogUu = view.findViewById<ExpandableListView>(R.id.listViewAbonentDogUu)

            view.findViewById<Button>(R.id.btnOpenGridDogObjects).setOnClickListener {
                lvObjects.visibility = if (lvObjects.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
            view.findViewById<Button>(R.id.btnOpenGridDogTu).setOnClickListener {
                lvDogTu.visibility = if (lvDogTu.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }
            view.findViewById<Button>(R.id.btnOpenGridDogUu).setOnClickListener {
                lvDogUu.visibility = if (lvDogUu.visibility == View.VISIBLE) View.GONE else View.VISIBLE
            }

            view.findViewById<TextView>(R.id.etAbonentName).text = dogData.name
            view.findViewById<TextView>(R.id.erAbonentInn).text = dogData.inn
            view.findViewById<TextView>(R.id.tvAbonentDogNum).text = dogData.ndog
            view.findViewById<TextView>(R.id.etAbonentDogDate).text =
                dogData.dat_dog.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString()
            view.findViewById<TextView>(R.id.etAbonentContacts).text =
                dogData.contact.replace("\\n ", "\n").replace("\\n", "\n")
            view.findViewById<TextView>(R.id.etAbonentDogNagr).text =
                dogData.dog_har.replace(",Нагрузка", ",\nНагрузка")
            view.findViewById<CheckBox>(R.id.checkboxAbonentPu).isChecked = dogData.nal_pu == 1

            view.findViewById<TextView>(R.id.etAsuseCard).text = dogData.sum_dolg_total
            view.findViewById<TextView>(R.id.etAsuseNachisl).text =
                dogData.last_nachisl.replace("\\n ", "\n").replace("\\n", "\n")
            view.findViewById<TextView>(R.id.etAsuseMonit).text = ""
            view.findViewById<TextView>(R.id.etAsuseOpl).text =
                dogData.last_opl.replace("\\n ", "\n").replace("\\n", "\n")
            /*  if (dogData.pusk_tu.isNotEmpty() || dogData.otkl_tu.isNotEmpty())
                  view.findViewById<TextView>(R.id.etAsuseTu).text =
                      "Вкл.: ${if (dogData.pusk_tu.isEmpty()) " --- " else dogData.pusk_tu}; " +
                              "\n\nОткл.:${if (dogData.otkl_tu.isEmpty()) " --- " else dogData.otkl_tu}"*/

            // listview объекты договора
            if (dogData.listDogObjects.size > 0) {
                val adapter = context?.let { AdapterListDogObjects(it, dogData.listDogObjects) }
                lvObjects.adapter = adapter
                val totalHeight = calcListViewHeight(lvObjects)
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    view.findViewById<View>(R.id.view2).visibility = if (totalHeight > 500) View.VISIBLE else View.GONE
                } else {
                    view.findViewById<View>(R.id.view2).visibility = if (totalHeight > 500) View.VISIBLE else View.GONE
                }
                val params = ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, totalHeight)
                params.topToBottom = R.id.lvHeaderDogObjects
                params.startToStart = R.id.parent
                params.endToEnd = R.id.parent
                lvObjects.layoutParams = params
            }

            // listview ТУ договора
            if (dogData.listDogTu != null && dogData.listDogTu!!.size > 0) {
                val adapter = context?.let { AdapterListDogTu(it, dogData.listDogTu) }
                lvDogTu.adapter = adapter
                val totalHeight = calcListViewHeight(lvDogTu)
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    view.findViewById<View>(R.id.view3).visibility = if (totalHeight > 500) View.VISIBLE else View.GONE
                } else {
                    view.findViewById<View>(R.id.view3).visibility = if (totalHeight > 500) View.VISIBLE else View.GONE
                }
                val params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, totalHeight)
                lvDogTu.layoutParams = params
            }

            // listview УУ договора
            if (dogData.listDogUu != null && dogData.listDogUu!!.size > 0) {
                val adapter = AdaptedExpandedUuSi(context!!, dogData.listDogUu, dogData.listDogUuSi)

                lvDogUu.setAdapter(adapter)
                val totalHeight = calcListViewHeight(lvDogUu)
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    view.findViewById<View>(R.id.view4).visibility = if (totalHeight > 500) View.VISIBLE else View.GONE
                } else {
                    view.findViewById<View>(R.id.view4).visibility = if (totalHeight > 500) View.VISIBLE else View.GONE
                }
                var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, totalHeight)

                lvDogUu.layoutParams = params
                adapter.onItemClickExpand = {
                    params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 500)
                    lvDogUu.layoutParams = params
                    view.findViewById<LinearLayout>(R.id.lvHeaderDogUu).minimumWidth = 2600
                }
                adapter.onItemClickCollapse = {
                    params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, totalHeight)
                    lvDogUu.layoutParams = params
                    if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        view.findViewById<LinearLayout>(R.id.lvHeaderDogUu).minimumWidth = 780
                    } else {
                        view.findViewById<LinearLayout>(R.id.lvHeaderDogUu).minimumWidth = 1250
                    }
                }
                lvDogUu.setAdapter(adapter)
            }
        } catch (e: Exception) {
            println("$TAG_ERR adapterTabsAbonentDog: ${e.message}")
        } finally {
            return view!!
        }
    }

    // Вычисление высоты Listview (но не больше 500dp)
    // -----------------------------------------------
    private fun calcListViewHeight(lvObjects: ListView): Int {
        val maxHeight =
            if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                500 else 200
        var totalHeight = 4
        for (i in 0 until lvObjects.adapter.count) {
            if (totalHeight > maxHeight) break
            val listItem: View = lvObjects.adapter.getView(i, null, lvObjects)
            listItem.measure(0, 0)
            totalHeight += (listItem.measuredHeight * 1.2).roundToInt()
        }
        return totalHeight
    }

    fun newInstance(abonentData: DogData): DogovorFragment {
        val args = Bundle()
        args.putSerializable("abonent", abonentData)
        val fragment = DogovorFragment()
        fragment.arguments = args
        return fragment
    }
}

// *********************************************
//              TAB Мониторинг
// *********************************************
class MonitoringFragment() : Fragment() {

    private var myContext: Context? = null
    private var dogData: DogData = DogData()


    constructor(context: Context, abonent: DogData) : this() {
        myContext = context
        dogData = abonent

        val args = Bundle()
        args.putSerializable("abonent", dogData)
        this.arguments = args
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            dogData = arguments?.getSerializable("abonent") as DogData
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_abonent_monitoring, container, false)

        if (dogData.kod_dog == 0) {
            if (arguments != null) {
                dogData = arguments?.getSerializable("abonent") as DogData
            } else
                return view
        }
        try {
            view.findViewById<TextView>(R.id.tvRemarkDog).text = dogData.remark_dog.replace("\\n", "\n")
            view.findViewById<TextView>(R.id.tvRemarkKontrol).text = dogData.remark_kontrol.replace("\\n", "\n")
            view.findViewById<TextView>(R.id.tvRemarkRasch).text = dogData.remark_rasch.replace("\\n", "\n")
            view.findViewById<TextView>(R.id.tvRemarkTu).text = dogData.remark_tu.replace("\\n", "\n")
            view.findViewById<TextView>(R.id.tvRemarkUr).text = dogData.remark_ur.replace("\\n", "\n")
            view.findViewById<TextView>(R.id.tvPuskTu).text = dogData.pusk_tu.replace("\\n", "\n")
            view.findViewById<TextView>(R.id.tvOtklTu).text = dogData.otkl_tu.replace("\\n", "\n")

        } catch (e: Exception) {
            println("$TAG_ERR MonitoringFragment: ${e.message}")
        } finally {
            return view
        }
    }
}

// *********************************************
//              TAB ZULU
// *********************************************
class ZuluFragment() : Fragment() {
    private var _context: Context? = null
    private var _task: Task = Task()

    constructor(context: Context, task: Task) : this() {
        _context = context
        _task = task

        val args = Bundle()
        args.putSerializable("task", _task)
        this.arguments = args
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            _task = arguments?.getSerializable("task") as Task
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.activity_abonent_zulu, container, false)

        view.findViewById<TextView>(R.id.dogBorderZulu).text = _task.border_zulu
        view.findViewById<TextView>(R.id.dogBorderZulu).text = _task.border_zulu

        return view
    }
}