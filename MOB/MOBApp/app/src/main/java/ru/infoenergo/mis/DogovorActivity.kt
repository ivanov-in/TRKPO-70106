//package ru.infoenergo.mis
//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.viewpager.widget.ViewPager
//import com.google.android.material.tabs.TabLayout
//import ru.infoenergo.mis.adapters.AdapterTabsAbonentDog
//import ru.infoenergo.mis.helpers.TAG_ERR
//
///*************************************************/
///**   Карточка Договор Теплоснабжения           **/
///**    Просмотр информации о потребителе        **/
///*************************************************/
//class DogovorActivity: AppCompatActivity() {
//
//    private var tabLayout: TabLayout? = null
//    var viewPager: ViewPager? = null
//    private var _kodDog: Int = 0
//    private var _idTask: Int = 0
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_abonent_tabs)
//
//        //Разрешить кнопку назад
//        if (supportActionBar != null) {
//            val actionBar = supportActionBar
//            actionBar!!.title = "Просмотр информации о потребителе"
//            actionBar.subtitle = "Договор Теплоснабжения ${intent.getStringExtra("NDOG")}"
//            actionBar.setDisplayHomeAsUpEnabled(true)
//            actionBar.elevation = 4.0F
//        }
//        _idTask = intent.getIntExtra("TASK_ID", 0)
//        _kodDog  = intent.getIntExtra("KOD_DOG", -1)
//        if (_idTask == 0)
//            this.finish()
//
//        tabLayout = findViewById(R.id.tabAbonDog)
//        viewPager = findViewById(R.id.viewPagerAbonDog)
//
//        tabLayout!!.addTab(tabLayout!!.newTab().setText("Договор"))
//        tabLayout!!.addTab(tabLayout!!.newTab().setText("Мониторинг"))
//        //tabLayout!!.addTab(tabLayout!!.newTab().setText("Zulu"))
//        tabLayout!!.tabGravity = TabLayout.MODE_FIXED
//
//        try {
//            val adapter = AdapterTabsAbonentDog(
//                this, supportFragmentManager,
//                tabLayout!!.tabCount, _idTask, _kodDog
//            )
//            viewPager!!.adapter = adapter
//        }
//        catch (e: Exception)
//        {
//            println("$TAG_ERR Dogovor Activity: ${e.message}")
//        }
//        viewPager!!.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
//
//        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
//            override fun onTabSelected(tab: TabLayout.Tab) {
//                viewPager!!.currentItem = tab.position
//            }
//            override fun onTabUnselected(tab: TabLayout.Tab) {
//
//            }
//            override fun onTabReselected(tab: TabLayout.Tab) {
//
//            }
//        })
//
//    }
//
//    // чтобы по кнопке стрелки из тублара
//    // вернуться на предыдущее активити
//    // -----------------------------------------
//    override fun onSupportNavigateUp(): Boolean {
//        this.finish()
//        return true
//    }
//}