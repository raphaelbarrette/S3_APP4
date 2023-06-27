package Layers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static java.lang.System.arraycopy;
import static java.lang.System.lineSeparator;

public class ApplicationLayer extends Layer{
    private static ApplicationLayer instance;
    public static ApplicationLayer getInstance(){
        if (instance == null){
            instance = new ApplicationLayer();
        }
        return instance;
    }

    @Override
    protected void getFromUp(byte[] PDU) {

    }

    @Override
    protected void getFromDown(byte[] PDU) throws TransmissionErrorException {
        System.out.println("Receiving");
        String title = new String(Arrays.copyOfRange(PDU, 0, 188), StandardCharsets.US_ASCII).trim();
        System.out.println("title" + title);
        byte[] data_bytes = Arrays.copyOfRange(PDU, 188, PDU.length);
        try {
            String filePath = new File("").getAbsolutePath();
            System.out.println("filepath " + filePath);
            File file = new File(filePath + "/ProbReseau/dest/" + title);
            if (file.exists()) {
                file.delete();
            }
            if (file.createNewFile()) {
                System.out.println("Fichier Cree: " + file.getName());
            } else {
                System.out.println("Fichier existant");
            }
            try (FileOutputStream fos = new FileOutputStream(file.getPath())) {
                System.out.println("Écrire Stream");
                fos.write(data_bytes);
                System.out.println("Écrire fini");
            }
        } catch (Exception exception) {
            System.out.println("Erreur");
            exception.printStackTrace();
        }
    }

    public void EnvoyerFichier(String path) throws IOException, InterruptedException {
        File file = new File(path);
        byte[] APDU;
        byte[] filename = file.getName().getBytes();
        Path filePath = file.toPath();
        byte[] fileBytes = Files.readAllBytes(filePath);
        APDU = new byte[188 + fileBytes.length];
        arraycopy(filename, 0, APDU, 0, filename.length);
        arraycopy(fileBytes, 0, APDU, 188, fileBytes.length);
        sendDown(APDU);
        Thread.sleep(1000);
        System.exit(0);
    }
}
