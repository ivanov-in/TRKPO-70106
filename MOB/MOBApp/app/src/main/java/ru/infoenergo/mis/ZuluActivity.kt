package ru.infoenergo.mis

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_zulu.*
import ru.infoenergo.mis.helpers.TAG_ERR
import ru.infoenergo.mis.helpers.Task
import ru.infoenergo.mis.helpers.isNetworkAvailable


/** **************************************************** **/
/**                   ZULU WebView                       **/
/**            Просмотр информации о потребителе         **/
/** **************************************************** **/

class ZuluActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zulu);

        //Разрешить кнопку назад
        if (supportActionBar != null) {
            val actionBar = supportActionBar
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            actionBar.elevation = 4.0F
            actionBar.title = "Zulu"
            actionBar.subtitle = "Электронная схема тепловых сетей (режим просмотра)"
        }
        val networkAvailable = isNetworkAvailable(this)
        if (networkAvailable) {
            try {
                val task = intent.getSerializableExtra("TASK") as Task

                //zuluWebView.webViewClient = MyWebViewClient()
                //zuluWebView.webChromeClient  = WebChromeClient()
                //zuluWebView.settings.javaScriptEnabled = true

                val str = "http://10.7.1.8:6473/zuluweb/#!/map/xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx?lon=${task.lan}&lat=${task.lat}&z=18"
                //val str = "https://www.google.com"

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(str))
                startActivity(intent)

                //zuluWebView.loadUrl(str)


              /* // Сохранить ссылку в текстовом файле, чтобы попробовать открыть в хроме
                 // Сделано только для нас, для тестирования работоспособности ссылки
                 val external = this.baseContext.getExternalFilesDir(null)
                 val zuluFolder = File("$external/zulu")
                 if (!zuluFolder.exists()) zuluFolder.mkdirs()

                 try {
                     // response is the data written to file
                     val zulufile = File("${zuluFolder.path}/ZuluUrl.txt")
                     if (!zulufile.exists()) zulufile.createNewFile()
                     zulufile.writeText(str)
                 } catch (e: Exception) {
                     println("$TAG_ERR  ZuluActivity: ${e.message}")
                 }*/

            } catch (e: Exception) {
                println("$TAG_ERR  ZuluActivity: ${e.message}")
            }
        } else {
            Toast.makeText(
                this@ZuluActivity,
                "Отсутствует подключение к сети Интернет.",
                Toast.LENGTH_LONG
            ).show()
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        this.finish()
        return true
    }

    // При нажатии кнопки назад
    // ---------------------------
    override fun onBackPressed() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Внимание!")
            .setMessage("Закрыть Zulu?")
            .setIcon(R.drawable.ic_question)
            .setNegativeButton("Нет") { dialog, _ ->
                dialog.cancel()
            }
            .setPositiveButton("Да") { _, _ ->
                setResult(RESULT_CANCELED)
                this@ZuluActivity.finish()
            }
        builder.show()
    }
}

private class MyWebViewClient : WebViewClient() {

    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        view.loadUrl(url)
        //val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        //view.context.startActivity(intent)
        return false
    }

    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
        println("$TAG_ERR ZuluWeb onReceivedError ${error!!.errorCode} ${error.description}")
        super.onReceivedError(view, request, error)
    }
}