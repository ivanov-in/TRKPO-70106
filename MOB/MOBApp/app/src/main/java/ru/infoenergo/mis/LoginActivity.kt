package ru.infoenergo.mis

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Html.FROM_HTML_MODE_COMPACT
import android.text.method.LinkMovementMethod
import android.util.Base64
import android.view.Menu
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.coroutines.*
import ru.infoenergo.mis.dbhandler.*
import ru.infoenergo.mis.helpers.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class LoginActivity : AppCompatActivity() {

    private val viewModelJob = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var _idInspector: Int = 0
    private var _versNew: String = BuildConfig.VERSION_NAME

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Запретить кнопку назад
        if (supportActionBar != null) {
            val actionBar = supportActionBar
            actionBar!!.title = resources.getString(R.string.app_full_name)
            actionBar.subtitle = " "
            actionBar.elevation = 4.0F
            actionBar.setDisplayHomeAsUpEnabled(false)
        }


        loginVersion.text = "Версия ${BuildConfig.VERSION_NAME}"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            checkPermissions()

        val etLogin = findViewById<EditText>(R.id.etLogin)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        // Кнопка Авторизоваться
        // --------------------------------------------------
        val button = findViewById<Button>(R.id.btnLogin)
        button.setOnClickListener {
            //val login = etLogin.text.toString()
            //val psw = etPassword.text.toString()

            val login = "ABLAKOVAII"
            val psw = "123456"

            button.isEnabled = false
            etLogin.isEnabled = false
            etPassword.isEnabled = false

            findViewById<LinearLayout>(R.id.progressBarLogin).visibility = View.VISIBLE

            // Проверка подключения к сети
            // ---------------------------
            val networkAvailable = isNetworkAvailable(this@LoginActivity)
            if (networkAvailable) {
                uiScope.launch {
                    // Получаем с сервера id, ФИО инспектора по логину и паролю (и мин. значение id_task)
                    // ----------------------------------------------------------------------------------
                    // хеширование
                    val ps = getShaPswd(psw)
                    val inspectorAsync = withContext(Dispatchers.IO) {
                        DBHandlerServerRead(this@LoginActivity).idInspectorAsync(login, ps)
                    }

                    // Записываем в бд всю информацию об инспекторе
                    // --------------------------------------------
                    if (inspectorAsync.ok) {
                        // Парсим данные из json
                        //------------------------------------------------
                        writeInspectorDataFromJsonToLocal(inspectorAsync, ps)

                        when {
                            _versNew > BuildConfig.VERSION_NAME && _idInspector != 0 -> {
                                loginMsg.visibility = View.VISIBLE
                                loginMsg.text =
                                    "Версия вашего приложения MIS ${BuildConfig.VERSION_NAME} устарела. \n" +
                                            "Для скачивания новой версии $_versNew нажмите на ссылку ниже. "

                                linkVers.visibility = View.VISIBLE
                                val value = resources.getString(R.string.linkMis)
                                linkVers.movementMethod = LinkMovementMethod.getInstance()
                                linkVers.text = Html.fromHtml(value, FROM_HTML_MODE_COMPACT)
                                btnLogin.visibility = View.GONE
                                progressBarLogin.visibility = View.GONE

                                btnLoginSkip.visibility = View.VISIBLE
                                btnLoginSkip.setOnClickListener {
                                    // Если запись в локальную БД прошла успешно
                                    // открываем маршрутный лист инспектора
                                    //------------------------------------------
                                    val intent = Intent(this@LoginActivity, ListTasksActivity::class.java)
                                    intent.putExtra("ID_INSPECTOR", _idInspector)
                                    startActivityForResult(intent, LOGIN)
                                    this@LoginActivity.finish()

                                }
                                return@launch
                            }

                            // Если запись в локальную БД прошла успешно
                            // открываем маршрутный лист инспектора
                            //------------------------------------------
                            _idInspector != 0 -> {
                                val intent = Intent(this@LoginActivity, ListTasksActivity::class.java)
                                intent.putExtra("ID_INSPECTOR", _idInspector)
                                startActivityForResult(intent, LOGIN)
                                this@LoginActivity.finish()
                                return@launch
                            }

                            // Если запись в локальную БД прошла НЕ успешно
                            // выводим ошибку
                            //---------------------------------------------
                            else -> {
                                button.isEnabled = true
                                etLogin.isEnabled = true
                                etPassword.isEnabled = true
                                progressBarLogin.visibility = View.GONE
                                if (!inspectorAsync.ok && inspectorAsync.error.isNotEmpty() && loginMsg.text.isEmpty()) {
                                    loginMsg.visibility = View.VISIBLE
                                    loginMsg.text = inspectorAsync.error
                                }
                                return@launch
                            }
                        }
                    }
                    // Если произошла ошибка при получении данных с сервера
                    //-----------------------------------------------------
                    else {
                        button.isEnabled = true
                        etLogin.isEnabled = true
                        etPassword.isEnabled = true
                        findViewById<LinearLayout>(R.id.progressBarLogin).visibility = View.GONE
                        loginMsg.visibility = View.VISIBLE
                        loginMsg.text = if (inspectorAsync.error.contains("ошибка", true))
                            inspectorAsync.error
                        else "ОШИБКА: ${inspectorAsync.error}"
                        return@launch
                    }
                }
            } else {
                // Ищем данные по инспектору в лок базе
                // ------------------------------------
                val dbLocalRead = DbHandlerLocalRead(this, null)
                val idInspector = dbLocalRead.getInspectorDataByPuser(login, getShaPswd(psw))
                dbLocalRead.close()
                
                if (idInspector != 0) {
                    val intent = Intent(this@LoginActivity, ListTasksActivity::class.java)
                    intent.putExtra("ID_INSPECTOR", idInspector)
                    startActivity(intent)
                    finish()

                } else {
                    button.isEnabled = true
                    etLogin.isEnabled = true
                    etPassword.isEnabled = true
                    progressBarLogin.visibility = View.GONE
                    loginMsg.visibility = View.VISIBLE
                    loginMsg.text = "Неправильный логин или пароль"
                }
            }
        }
    }

    // Чтение данных по инспектору из json и запись в локальную БД min_id_task, fio
    // Возвращает Id инспектора
    // -------------------------------------------------------------------------------------------------
    private suspend fun writeInspectorDataFromJsonToLocal(res: AsyncResultJson, pswdSha1: String) {
        try {
            val mapper = ObjectMapper()
            val rootNode = mapper.readTree(res.json)

            if (rootNode.elements().hasNext()) {
                val element = rootNode.elements().next()
                if (element.has("errmsg")) {
                    println("$TAG_ERR login errmsg: ${element["errmsg"]!!.asText()}")
                    progressBarLogin.visibility = View.GONE
                    loginMsg.visibility = View.VISIBLE
                    loginMsg.text = element["errmsg"]!!.asText()
                }

                _idInspector = element["ID_INSPECTOR"]!!.asInt()
                val fioInspector = element["FIO"]!!.asText()
                val minIdTaskServer = try {
                    element["MIN_TASK_ID"]!!.asInt()
                } catch (e: Exception) {
                    "-${_idInspector}1".toInt()
                }
                supportActionBar!!.subtitle = fioInspector
                try {
                    // Узнаём есть ли запись об инспекторе в локальной базе
                    // Если есть, то получаем min_id_task
                    val dbLocalRead = DbHandlerLocalRead(this, null)
                    val minIdTaskLocal = dbLocalRead.getMinIdTask(_idInspector, notInc = true)
                    dbLocalRead.close()

                    val dbLocalWrite = DbHandlerLocalWrite(this, null)
                    if (minIdTaskLocal == 0) {
                        // если инспектора не было - записываем всё из json. INSERT
                        // --------------------------------------------------------
                        dbLocalWrite.insertIdInspectorFromServer(res.json, pswdSha1)
                        dbLocalWrite.close()
                    } else {
                        // если запись была, обновляем запись, записываем min_id_task и ФИО. UPDATE
                        // ---------------------------------------------------------------------------
                        val pref = element["NUM_ACT_PREF"]!!.asText()
                        _versNew = try {
                            element["VERS"]!!.asText().replace("null", BuildConfig.VERSION_NAME)
                        } catch (e: Exception) {
                            BuildConfig.VERSION_NAME
                        }
                        dbLocalWrite.updateIdInspector(
                            idInspector = _idInspector,
                            minTaskId = minOf(minIdTaskServer, minIdTaskLocal),
                            pswd = pswdSha1,
                            pref = pref,
                            fio = fioInspector,
                            version = _versNew
                        )
                        dbLocalWrite.close()

                        // если запись была и minIdTask локальный меньше, чем с сервера, то обновляем на сервере
                        // -------------------------------------------------------------------------------------
                        if (minIdTaskServer > minIdTaskLocal)
                            withContext(Dispatchers.IO) {
                                DBHandlerServerWrite(this@LoginActivity).updateMinTaskIdAsync(
                                    _idInspector,
                                    minIdTaskLocal
                                )
                            }
                    }
                } catch (e: Exception) {
                    println("$TAG_ERR minIdTaskLocal inspector data: ${e.message}")
                }
            }
        } catch (e: Exception) {
            println("$TAG_ERR insert/update inspector data: ${e.message}")
        }
    }

    // Спрятать пароль и записать в лок базу
    // -------------------------------------
    private fun getShaPswd(clearString: String): String {
        return try {
            val md = MessageDigest.getInstance("SHA-256")
            md.update(clearString.toByteArray())
            val encodedHash: ByteArray = md.digest()
            var str = ""
            for (b in encodedHash) str += String.format("%02x", b)
            return Base64.encodeToString(str.toByteArray(), Base64.NO_WRAP)
        } catch (e: NoSuchAlgorithmException) {
            println("$TAG_ERR getShaPswd NoSuchAlgorithmException: ${e.message}")
            ""

        } catch (e: Exception) {
            println("$TAG_ERR getShaPswd: ${e.message}")
            ""
        }
    }

    // Check permissions at runtime
    private fun checkPermissions() {
        val list = listOf(
            permission.READ_EXTERNAL_STORAGE,
            permission.WRITE_EXTERNAL_STORAGE,
            permission.CALL_PHONE,
            permission.ACCESS_NETWORK_STATE,
            permission.ACCESS_WIFI_STATE,
            permission.INTERNET,
            permission.CAMERA,
            permission.SEND_SMS,
            permission.READ_SMS,
            permission.RECEIVE_SMS,
            permission.ACCESS_FINE_LOCATION,
            permission.ACCESS_COARSE_LOCATION
        )

        var counter = 0
        for (permission in list) {
            counter += ContextCompat.checkSelfPermission(this, permission)
        }

        if (counter != PackageManager.PERMISSION_GRANTED) {
            val permission = deniedPermission(list)
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                ActivityCompat.requestPermissions(this, list.toTypedArray(), PERMISSIONS)
            }
        }
    }

    // Find the first denied permission
    private fun deniedPermission(list: List<String>): String {
        for (permission in list) {
            if (ContextCompat.checkSelfPermission(this, permission)
                == PackageManager.PERMISSION_DENIED
            ) return permission
        }
        return ""
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS -> {
                var result = 0
                if (grantResults.isNotEmpty()) {
                    grantResults.forEach { result += it }
                }

                if (result == PackageManager.PERMISSION_GRANTED) {
                    // Do the task now
                    Toast.makeText(this, "Разрешения получены.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Разрешения отклонены.", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }


    // При нажатии кнопки ничего не делаем
    // -----------------------------------
    override fun onBackPressed() {
        return
    }

}

