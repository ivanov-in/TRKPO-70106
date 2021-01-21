package ru.infoenergo.mis.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import ru.infoenergo.mis.R
import ru.infoenergo.mis.helpers.FileInfo
import ru.infoenergo.mis.helpers.SHARE_IMG
import ru.infoenergo.mis.helpers.TAG_ERR
import ru.infoenergo.mis.helpers.UNPIN_PHOTO
import java.io.ByteArrayOutputStream


/** *************************************************************** **/
/** Адаптер для галереи прикрепленных фотографий в карточке задания **/
/** *************************************************************** **/
// fullSize - если надо увеличить фотографию во весь экран
//  (поменяла увеличение фоток, так что можно убрать или потом переделать)
// orientation - ориентация экрана
// ORIENTATION_PORTRAIT = 1
// ORIENTATION_LANDSCAPE = 2;
class AdapterImageGallery(
    private var photoInfoArray: ArrayList<FileInfo>,
    private var history: Boolean
) :
    RecyclerView.Adapter<AdapterImageGallery.PhotoHolder>() {
    var onItemClick: ((FileInfo) -> Unit)? = { }

    override fun getItemId(position: Int) = photoInfoArray[position].id_file.toLong()

    override fun getItemCount() = photoInfoArray.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
        val inflatedView = parent.inflate(R.layout.rvitem_image_task, false)
        return PhotoHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: PhotoHolder, position: Int) = try {
        holder.bind(photoInfoArray[position])
    } catch (e: Exception) {
        println("$TAG_ERR ImageGallery onBindViewHolder: ${e.message}")
    }

    // расширение для ViewGroup
    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }

    inner class PhotoHolder(v: View) : RecyclerView.ViewHolder(v), View.OnCreateContextMenuListener {
        var img: ImageView = itemView.findViewById(R.id.image_thumb)
        var act: TextView = itemView.findViewById(R.id.image_act)

        init {
            v.setOnCreateContextMenuListener(this)
            v.setOnClickListener {
                onItemClick?.invoke(photoInfoArray[adapterPosition])
            }
        }

        // Привязка фотографий в галерею (recycleView в карточке задания)
        // --------------------------------------------------------------
        fun bind(photo: FileInfo) {
            if (photo.filedata == null) return
            try {
                while (photo.filedata!!.size > 2097152) {
                    photo.filedata = resizeImage(photo)
                }
                val bmp = BitmapFactory.decodeByteArray(photo.filedata, 0, photo.filedata!!.size) ?: null
                img.setImageBitmap(Bitmap.createBitmap(bmp!!))
                act.text = photoInfoArray[adapterPosition].filename
            } catch (e: Exception) {
                println("$TAG_ERR AdapterImage bind: ${e.message}")
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            if (history) return
            // Удалять фотки можно, только если инспектор добавил их сам
            // ---------------------------------------------------------
            if (getItemId(this.adapterPosition) < 0) {
                menu!!.add(UNPIN_PHOTO, this.adapterPosition, 0, "Открепить фотографию")
                menu.add(SHARE_IMG, this.adapterPosition, 1, "Поделиться")
            } else {
                menu!!.add(UNPIN_PHOTO, this.adapterPosition, 0, "Открепить фотографию")
            }
        }
    }

    // Уменьшение размера фотографий
    // ---------------------------------------
    private fun resizeImage(photo: FileInfo): ByteArray {
        val bitmap = BitmapFactory.decodeByteArray(photo.filedata, 0, photo.filedata!!.size)
        val resized = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * 0.9).toInt(), (bitmap.height * 0.9).toInt(), true
        )
        val stream = ByteArrayOutputStream()
        resized.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        return stream.toByteArray()
    }
}