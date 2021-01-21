package ru.infoenergo.mis;

import android.content.Context;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;

import static ru.infoenergo.mis.helpers.MisClassesKt.TAG_ERR;


class HeaderFooter extends PdfPageEventHelper {
    String footerPath = "";
    public String numAct;
    public String dateAct;
    public String actSigned = "";

    public void onEndPage(PdfWriter writer, Document document) {
        try {
            final String FONT0 = "assets/fonts/times.ttf";
            BaseFont bf = BaseFont.createFont(FONT0, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font font = new Font(bf, 10, Font.NORMAL);

            PdfContentByte cb = writer.getDirectContent();

            //Подпись
            if (actSigned != null) {
                Rectangle rect = new Rectangle(writer.getPageSize().getRight() - writer.getPageSize().getWidth() / 2,
                        writer.getPageSize().getBottom() + 100, writer.getPageSize().getRight() - 20, writer.getPageSize().getBottom() + 120);
                rect.setBorder(Rectangle.BOX);
                rect.setBorderWidth((float) 0.5);
                rect.setBorderColor(BaseColor.BLUE);
                cb.rectangle(rect);

                Font fontBlue = new Font(bf, 10, Font.NORMAL, BaseColor.BLUE);
                Paragraph psign = new Paragraph("     Подписано простой электронной подписью " + actSigned, fontBlue);

                ColumnText ct = new ColumnText(cb);
                ct.setSimpleColumn(rect);
                ct.addElement(psign);
                ct.go();
            }

            //Нумерация
            Paragraph p = new Paragraph(String.valueOf(writer.getCurrentPageNumber()), font);
            Paragraph p1 = new Paragraph(numAct + " от " + dateAct, font);


            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, p1,
                    writer.getPageSize().getRight() - writer.getPageSize().getWidth() / 2,
                    writer.getPageSize().getBottom() + 60, 0);

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, p,
                    writer.getPageSize().getLeft() + 40,
                    writer.getPageSize().getBottom() + 60, 0);


            //нижний логотип
            Image image = Image.getInstance(footerPath);
            image.setAlignment(Element.ALIGN_RIGHT);
            image.setAbsolutePosition(30, 5);
            image.scalePercent(50f, 50f);
            cb.addImage(image, true);

        } catch (IOException | DocumentException e) {
            System.out.println(TAG_ERR + " footer crash " + e.getMessage());
        }

    }
}

public class CreatePdf {
    private static String getFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    public static boolean Create(String htmlPath, String pdfPath, LinkedHashMap<String, String> bdArray, Context context) {
       return Create(htmlPath, pdfPath, bdArray, context, 1);
    }

    public static boolean Create(String htmlPath, String pdfPath, LinkedHashMap<String, String> bdArray, Context context, int pageOrient) {

        //достаём шаблон html
        String html = "";
        try {
            html = getFile(htmlPath);

        } catch (IOException e) {
            System.out.println(TAG_ERR + " CreatePDF: File not found " + e.getMessage());
            return false;
        }
        // замена переменных в шаблоне
        html = ReplaceHtmlVariables(html, bdArray);

        // создаём pdf
        Document doc = pageOrient != 1 ?
                new Document(PageSize.A4.rotate()) :
                new Document(PageSize.A4);
        doc.setMargins(40, 40, 10, 150);

        File file;
        String storagePath = context.getExternalFilesDir(null).getPath();
        final String FONT0 = storagePath + "/fonts/times.ttf";
        if (!(new File(FONT0)).exists()) {
            System.out.println(TAG_ERR + " PDFCreator не подгружены шрифты на устройство.");
            return false;
        }

        final String Logo = storagePath + "/Img/logo.png";
        if (!(new File(Logo)).exists()) {
            System.out.println(TAG_ERR + ": не подгружены логотипы на устройство.");
            return false;
        }

        try {
            file = new File(pdfPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            // Создаем writer для записи целикового html в PDF
            PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(file));
            HeaderFooter event = new HeaderFooter();
            event.footerPath = storagePath + "/Img/footer.png";

            event.numAct = bdArray.get("%NUM_ACT");
            event.dateAct = bdArray.get("%DAT_ACT");
            event.actSigned = bdArray.get("%ACT_SIGNED");

            writer.setBoxSize("art", new Rectangle(36, 54, 559, 788));
            writer.setPageEvent(event);
            doc.open();

            Image logoImg = Image.getInstance(Logo);
            logoImg.scalePercent(40);
            doc.add(logoImg);

            XMLWorkerFontProvider fontImp = new XMLWorkerFontProvider(XMLWorkerFontProvider.DONTLOOKFORFONTS);
            fontImp.register(FONT0);

            XMLWorkerHelper worker = XMLWorkerHelper.getInstance();

            InputStream is = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
            worker.parseXHtml(writer, doc, is, StandardCharsets.UTF_8, fontImp);


        } catch (DocumentException de) {
            System.out.println(TAG_ERR + ": PDFCreator DocumentException:" + de.getMessage());
            doc.close();
            return false;
        } catch (IOException e) {
            System.out.println(TAG_ERR + ": PDFCreator ioException:" + e.getMessage());
            doc.close();
            return false;
        } catch (Exception e) {
            System.out.println(TAG_ERR + ": PDFCreator Exception:" + e.getMessage());
            doc.close();
            return false;
        }
        doc.close();
        return true;
    }

    private static String ReplaceHtmlVariables(String html, LinkedHashMap<String, String> vars) {
        for (String varName : vars.keySet()) {
            html = html.replace(varName, vars.get(varName));
        }
        return html;
    }
}
