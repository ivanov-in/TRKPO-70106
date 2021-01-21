package ru.infoenergo.mis.email

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.infoenergo.mis.helpers.TAG_ERR
import java.security.Security
import java.util.*
import javax.activation.DataHandler
import javax.activation.DataSource
import javax.activation.FileDataSource
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

class GMailSender2(private val user: String, private val password: String) : Authenticator() {
    private val session: Session

    companion object {
        init {
            Security.addProvider(JSSEProvider())
        }
    }

    init {
        //String host = "smtp.google.com";
        val host = "smtp." + user.substring(user.indexOf("@") + 1)
        val port = "25"
        val props = Properties()
        props.setProperty("mail.transport.protocol", "smtp")
        // String mailHost = App.appResources().getString(R.string.mail_host);
        // props.setProperty("mail.host", mailHost);
        props.setProperty("mail.host", host)
        props["mail.smtp.auth"] = "true"
        //String mailPort = App.appResources().getString(R.string.mail_port);
        //props.put("mail.smtp.port", mailPort);
        props["mail.smtp.port"] = port
        props["mail.smtp.socketFactory.port"] = "25"
        props["mail.smtp.socketFactory.class"] = "javax.net.ssl.SSLSocketFactory"
        props["mail.smtp.socketFactory.fallback"] = "false"
        props.setProperty("mail.smtp.quitwait", "false")
        session = Session.getDefaultInstance(props, this)
    }

    override fun getPasswordAuthentication(): PasswordAuthentication {
        return PasswordAuthentication(user, password)
    }

    // Отправка файла
    // ----------------------------------------------------------------------
    suspend fun sendMailPdf(subject: String?, body: String, pdfPath: String?,
        sender: String?, recipients: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val message: Message = MimeMessage(session)
                message.setFrom(InternetAddress(sender))
                message.subject = subject
                if (recipients.indexOf(',') > 0) message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipients)
                ) else message.setRecipient(
                    Message.RecipientType.TO, InternetAddress(recipients)
                )
                val messageBodyPart: BodyPart = MimeBodyPart()
                messageBodyPart.setText(body)
                messageBodyPart.fileName = pdfPath
                val source: DataSource = FileDataSource(pdfPath)
                messageBodyPart.dataHandler = DataHandler(source)
                val multipart: Multipart = MimeMultipart()
                multipart.addBodyPart(messageBodyPart)
                message.setContent(multipart)
                Transport.send(message)
                return@withContext true
            } catch (ex: Exception) {
                println(TAG_ERR + " GMailSender addAttachment: " + ex.message)
                return@withContext false
            }
        }
    }

    // Отправка кода подтверждения
    // ---------------------------------------------------------------------------------
    suspend fun sendMail(subject: String?, body: String, sender: String?, recipients: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val message = MimeMessage(session)
                val handler = DataHandler(ByteArrayDataSource(body.toByteArray(), "text/plain"))
                message.sender = InternetAddress(sender)
                message.subject = subject
                message.dataHandler = handler
                message.setFrom(InternetAddress(sender))
                if (recipients.indexOf(',') > 0) message.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(recipients)
                ) else message.setRecipient(
                    Message.RecipientType.TO, InternetAddress(recipients)
                )
                Transport.send(message)
                true
            } catch (e: Exception) {
                println("$TAG_ERR sendMail ${e.message}")
                false
            }
        }
    }
}