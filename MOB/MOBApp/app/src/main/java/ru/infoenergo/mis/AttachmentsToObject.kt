package ru.infoenergo.mis

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.infoenergo.mis.adapters.AdapterImageGallery
import ru.infoenergo.mis.adapters.AdapterListTasksActs
import ru.infoenergo.mis.dbhandler.DbHandlerLocalRead
import ru.infoenergo.mis.helpers.FileInfo
import ru.infoenergo.mis.helpers.TAG_ERR


/** ********************************************************* **/
/**   Прикрепленные файлы к задаче  в отдельном окне          **/
/** ********************************************************* **/
class AttachmentsToObject : AppCompatActivity() {
    // ID задачи из истории посещений
    private var _idTask: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attachments)

        //Разрешить кнопку назад
        // ----------------------------
        if (supportActionBar != null) {
            val actionbar = supportActionBar
            val ttime = intent.getStringExtra("TTIME")
            val adr = intent.getStringExtra("ADR")
            actionbar!!.title = "Прикрепленные файлы к посещению $ttime"
            actionbar.subtitle = adr
            actionbar.setDisplayHomeAsUpEnabled(true)
        }

        try {
            _idTask = intent.getIntExtra("TASK_ID", -1)
            refreshData()
        } catch (e: Exception) {
            println("$TAG_ERR onCreate AttachmentsToObject" + e.message)
        }
    }

    // загрузка данных с локальной бд
    // ------------------------------
    private fun refreshData() {
        val arrayFilesInfo = DbHandlerLocalRead(this, null).getFilesHistory(_idTask)
        if (arrayFilesInfo.size > 0) {
            try {
                // акты
                // ----------------------------
                val lvActs: ListView = findViewById(R.id.lvAttachmentsActs)
                val arrayActs = arrayFilesInfo.filter {
                    it.filename.takeLast(4).contains(".pdf", true) ||
                            it.paper == 1
                } as java.util.ArrayList<FileInfo>

                if (arrayActs.size > 0) {
                    lvActs.adapter = AdapterListTasksActs(this@AttachmentsToObject, arrayActs)
                    (lvActs.adapter as AdapterListTasksActs).onItemClick = {
                        if (!it.filename.takeLast(4).contains(".pdf", true))
                            openPhoto(it)
                    }
                    lvActs.visibility = View.VISIBLE
                } else {
                    lvActs.visibility = View.GONE
                }

                // фотографии
                // ---------------------------------------------------------------------------
                val rvImages = findViewById<RecyclerView>(R.id.rvAttachmentsPhotos)

                val arrayPhotos = arrayFilesInfo.filter {
                    it.filename.substringAfterLast(".")
                        .toLowerCase() in arrayOf("jpg", "jpeg", "bmp", "png", "gif")
                            && it.paper == 0 && it.filedata != null
                } as java.util.ArrayList<FileInfo>

                if (arrayPhotos.size > 0) {
                    val photoAdapter = AdapterImageGallery(arrayPhotos, history = false)
                    photoAdapter.onItemClick = { photo ->
                        openPhoto(photo)
                    }
                    rvImages.apply {
                        //setHasFixedSize(true)
                        layoutManager = LinearLayoutManager(this@AttachmentsToObject, LinearLayoutManager.HORIZONTAL, false)
                        (layoutManager as LinearLayoutManager).orientation = LinearLayoutManager.VERTICAL
                        adapter = photoAdapter
                        rvImages.visibility = View.VISIBLE
                    }
                    //(rvImages.adapter as AdapterImageGallery).notifyDataSetChanged()
                    /*  rvImages.visibility = View.VISIBLE
                                     */

                } else {
                    rvImages.visibility = View.GONE
                }

            } catch (e: Exception) {
                println("$TAG_ERR attachmentsToObject" + e.message)
            }
        }
    }

    // Открыть фото внутри TaskActivity
    // -------------------------------------
    private fun openPhoto(photo: FileInfo) {
        try {
            val bmp = BitmapFactory.decodeByteArray(photo.filedata, 0, photo.filedata!!.size)
            val touchImg = findViewById<TouchImageView>(R.id.tchAttachmentFullPhoto)
            touchImg.visibility = View.VISIBLE
            val attachments = findViewById<LinearLayout>(R.id.lvAttachments)
            attachments.visibility = View.GONE
            touchImg?.setImageBitmap(Bitmap.createBitmap(bmp))
            touchImg.setOnClickListener {
                touchImg.visibility = View.GONE
                attachments.visibility = View.VISIBLE
            }
        } catch (e: Exception) {
            println("$TAG_ERR touchImg: ${e.message}")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        this.finish()
        return true
    }
}