package ru.infoenergo.mis.email;

import org.jetbrains.annotations.NotNull;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import java.security.Security;
import java.util.Properties;

import static ru.infoenergo.mis.helpers.MisClassesKt.TAG_ERR;

public class GMailSender extends javax.mail.Authenticator {
    private final String user;
    private final String password;
    private final Session session;
    private Multipart multipart;


    static {
        Security.addProvider(new JSSEProvider());
    }

    public GMailSender(@NotNull String user, String password) {
        this.user = user;
        this.password = password;


        //String host = "smtp.google.com";
        String host = "smtp." + user.substring(user.indexOf("@") + 1);
        String port = "465";

        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", "smtp");
        // String mailHost = App.appResources().getString(R.string.mail_host);
        // props.setProperty("mail.host", mailHost);
        props.setProperty("mail.host", host);
        props.put("mail.smtp.auth", "true");
        //String mailPort = App.appResources().getString(R.string.mail_port);
        //props.put("mail.smtp.port", mailPort);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.quitwait", "false");

        session = Session.getDefaultInstance(props, this);
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password);
    }

    // Отправка файла
    // ---------------------------------------------------------------------------------------
    public synchronized void sendMailPdf(String subject, @NotNull String body, String pdfPath,
                                         String sender, @NotNull String recipients) {
        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender));
            message.setSubject(subject);
            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));


            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setText(body);
            messageBodyPart.setFileName(pdfPath);

            DataSource source = new FileDataSource(pdfPath);
            messageBodyPart.setDataHandler(new DataHandler(source));

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(messageBodyPart);

            message.setContent(multipart);

            Transport.send(message);

        } catch (Exception ex) {
            System.out.println(TAG_ERR + " GMailSender addAttachment: " + ex.getMessage());
        }
    }

    // Отправка кода подтверждения
    // --------------------------------------------------------------------------
    public synchronized void sendMail(String subject, String body, String sender,
                                      String recipients) throws Exception {

        try {
            MimeMessage message = new MimeMessage(session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(body.getBytes(), "text/plain"));
            message.setSender(new InternetAddress(sender));
            message.setSubject(subject);
            message.setDataHandler(handler);
            message.setFrom(new InternetAddress(sender));

            if (recipients.indexOf(',') > 0)
                message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipients));
            else
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipients));

            Transport.send(message);
        } catch (Exception e) {
        }
    }

}