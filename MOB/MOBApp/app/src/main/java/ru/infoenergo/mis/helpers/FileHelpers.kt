    package ru.infoenergo.mis.helpers

    import android.content.ActivityNotFoundException
    import android.content.Context
    import android.content.Intent
    import android.net.Uri
    import android.widget.Toast
    import androidx.core.content.ContextCompat.startActivity
    import androidx.core.content.FileProvider
    import ru.infoenergo.mis.BuildConfig
    import java.io.*
    import java.time.LocalDateTime
    import java.time.format.DateTimeFormatter
    import java.util.*

    // Создание ByteArray по Uri файла
    fun blobFromUri(context: Context, fileUri: Uri): ByteArray? {
        try {
            val iStream: InputStream? = context.contentResolver.openInputStream(fileUri)
            val byteBuffer = ByteArrayOutputStream()
            val buffer = ByteArray(1024)

            var len = 0
            while (iStream?.read(buffer).also {
                    if (it != null) {
                        len = it
                    }
                } != -1) {
                byteBuffer.write(buffer, 0, len)
            }
            return byteBuffer.toByteArray()
        } catch (e: Exception) {
            println("$TAG_ERR blobFromUri: ${e.message}")
            return null
        }
    }

    // Открыть сформированный pdf
    // ---------------------------------------------
    fun showPdf(context: Context, pdfPath: String): Boolean {
        if (!File(pdfPath).exists()) {
            Toast.makeText(context, "Произошла ошибка при открытии акта.", Toast.LENGTH_LONG).show()
            return false
        }
        // !! FileProvider должен быть прописан в манифесте
        val path = FileProvider.getUriForFile(
            Objects.requireNonNull(context),
            BuildConfig.APPLICATION_ID + ".provider", File(pdfPath)
        )
        val pdfIntent = Intent(Intent.ACTION_VIEW)
        pdfIntent.setDataAndType(path, "application/pdf")
        pdfIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        //без этого флага открывал не стабильно в различных PDF вийверах (google, drive или acrobat)
        pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        // рабочий флаг из старой версии, до android 6 (24)
        pdfIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        try {
            startActivity(context, pdfIntent, null)
        } catch (e: ActivityNotFoundException) {
            println("$TAG_ERR showPdf: ${e.message}")
            //Toast.makeText(context, "showPdf: " + e.message.toString(), Toast.LENGTH_LONG).show()
            return false
        }
        return true
    }

    // Создать PDF-ки из blob
    // ------------------------------------------------------------------
    fun downloadPdf(context: Context, data: ByteArray, filename: String, idTask: Int): String {
        val external = context.getExternalFilesDir(null)
        val tmp = File("$external/tmpFiles")
        if (!tmp.exists()) {
            tmp.mkdirs()
        }
        val tmpIdTask = File("$external/tmpFiles/$idTask")
        if (!tmpIdTask.exists()) {
            tmpIdTask.mkdirs()
        }

        var path = ""
        try {
            val tmpName =
                if (filename.isEmpty())
                    "$tmpIdTask/act_${idTask}_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))}.pdf"
                else
                    "$tmpIdTask/$filename"
            val tmpAct = File(tmpName)
            tmpAct.createNewFile()

            val fileOutputStream = FileOutputStream(tmpAct)
            val inputStream: InputStream = ByteArrayInputStream(data)
            var data: Int
            while (inputStream.read().also { data = it } >= 0) {
                fileOutputStream.write(data)
            }
            inputStream.close()
            path = tmpAct.path
        } catch (e: Exception) {
            println("$TAG_ERR downloadPdf: ${e.message}")
            //Toast.makeText(context, "downloadPdf: " + e.message.toString(), Toast.LENGTH_LONG).show()
            return path
        }
        return path
    }

    // Создать JPG-ки из blob
    // -----------------------------------------------------------------------------------------------------------
    fun downloadJpg(context: Context, data: ByteArray, filename: String, idTask: Int): String {
        val external = context.getExternalFilesDir(null)
        val tmp = File("$external/tmpFiles")
        if (!tmp.exists()) {
            tmp.mkdirs()
        }
        val tmpIdTask = File("$external/tmpFiles/$idTask")
        if (!tmpIdTask.exists()) {
            tmpIdTask.mkdirs()
        }

        var path = ""
        try {
            val tmpName =
                if (filename.isEmpty()) {
                    var extension = filename.substringAfterLast(".").toLowerCase()
                    if (extension !in arrayOf("jpg", "jpeg", "png", "bmp", "gif"))
                        extension = "jpg"

                    "$tmpIdTask/photo_${idTask}_${
                        LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm"))
                    }.$extension"
                }
                else
                    "$tmpIdTask/$filename"
            val tmpAct = File(tmpName)
            tmpAct.createNewFile()

            val fileOutputStream = FileOutputStream(tmpAct)
            val inputStream: InputStream = ByteArrayInputStream(data)
            var data: Int
            while (inputStream.read().also { data = it } >= 0) {
                fileOutputStream.write(data)
            }
            inputStream.close()
            path = tmpAct.path
        } catch (e: Exception) {
            println("$TAG_ERR downloadJpg: ${e.message}")
            return path
        }
        return path
    }
