import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public  class Main {
    private static String getFile() throws IOException {
        return new String(Files.readAllBytes(Paths.get("d:\\WORK\\Web\\МИС\\Шаблоны актов в html\\Универсальный Акт обследования ред. 2019.html")));
    }

    public static void main(String args[]) {
        String html = "";
        try {
            html = getFile();

        } catch (IOException e) {
            System.out.println("File not found");
            return;
        }


        CreatePdf creator = new CreatePdf(html);
        creator.createPdf();
    }
}