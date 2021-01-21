import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerFontProvider;
import com.itextpdf.tool.xml.XMLWorkerHelper;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;


public class CreatePdf {
    private byte[] pdfByte;
    private String FullPathFILE_NAME, FILE_NAME, HTML;
    private File file;
    private final String FONT0 = "assets/fonts/times.ttf";

    public CreatePdf(String html) {
        this.HTML = html;
    }

    public void createPdf() {
        pdfByte = null;
        Document doc = new Document(PageSize.A4);
        doc.setMargins(20, 15, 10, 10);

        FILE_NAME = "Универсальный Акт обследования ред. 2019.pdf";
        FullPathFILE_NAME = "d:\\WORK\\Web\\МИС\\ConvertedPdf\\" + FILE_NAME;

        try {
            file = new File(FullPathFILE_NAME);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            // Создаем writer для записи целикового html в PDF
            PdfWriter writer =  PdfWriter.getInstance(doc, new FileOutputStream(file));
            doc.open();
            XMLWorkerFontProvider fontImp = new XMLWorkerFontProvider(XMLWorkerFontProvider.DONTLOOKFORFONTS);
            fontImp.register(FONT0);
            XMLWorkerHelper worker = XMLWorkerHelper.getInstance();
            InputStream is = new ByteArrayInputStream(HTML.getBytes("UTF-8"));
            worker.parseXHtml(writer, doc, is, Charset.forName("UTF-8"));

        } catch (DocumentException de) {
            System.out.println("PDFCreator" + "DocumentException:" + de);
        } catch (IOException e) {
            System.out.println("PDFCreator" + "ioException:" + e);
        } finally {
            doc.close();
        }

    }

}
