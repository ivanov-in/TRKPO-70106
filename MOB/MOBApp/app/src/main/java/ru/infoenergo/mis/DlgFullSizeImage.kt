package ru.infoenergo.mis

import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import ru.infoenergo.mis.adapters.AdapterImageGallery
import ru.infoenergo.mis.helpers.FileInfo


/** ***************************************************** **/
/**    Диалоговое окно    Просмотр фотографий к акту      **/
/** ***************************************************** **/
//TODO наверное надо будет удалить
class DlgFullSizeImage(
    private var arrayPhotos: java.util.ArrayList<FileInfo> = ArrayList(),
) : DialogFragment() {

    var rv: RecyclerView? = null
    var adapter: AdapterImageGallery? = null

    override fun onStart() {
        dialog!!.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, 250)
        dialog!!.setCanceledOnTouchOutside(true)
        super.onStart()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.dlg_photos, container)

        rv = rootView.findViewById<View>(R.id.recyclerDialogPhotos) as RecyclerView
        rv!!.layoutManager = LinearLayoutManager(this.activity, LinearLayoutManager.HORIZONTAL, false)

        adapter = AdapterImageGallery(arrayPhotos, false)
        rv!!.adapter = adapter
        rv!!.layoutManager!!.scrollToPosition(arrayPhotos.indexOf(0))

        return rootView
    }

}