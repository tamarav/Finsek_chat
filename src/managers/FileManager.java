package src.managers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

public class FileManager {
    
    //metoda koja vrsi slanje fajla
    public synchronized static void slanjeFajla(String path, OutputStream os, InputStream is, PrintWriter out)
        throws IOException {
        
        File file = new File(path);
        out.println(((int) file.length()));
        
        byte[] toSent = new byte[(int) file.length()];
        
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);
        bis.read(toSent, 0, toSent.length);
        
        is.read();
        
        os.write(toSent);
        os.flush();
        
        bis.close();
        
    }
    
    // metoda za prihvatanje fajla
    public synchronized static void prihvatanjeFajla(String path, InputStream is, OutputStream os, BufferedReader in) {
        
        int velicinaFajla = 0;
        try {
            velicinaFajla = Integer.parseInt(in.readLine());
        } catch (NumberFormatException | IOException e1) {
            e1.printStackTrace();
        }
        
        int bytesRead = 0;
        byte[] toRecieve = new byte[velicinaFajla];
        
        try {
            os.write(0);
            os.flush();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(path))) {
            
            bytesRead = is.read(toRecieve, 0, toRecieve.length);
            bos.write(toRecieve, 0, bytesRead);
            bos.flush();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // metoda koja vrsi pornalazak koverzacija za radnika ciji jmbg je prosledjen kao argument i datum koji je takodje prosledjen kao argument
    public synchronized static ArrayList<File> nadjiKonverzacije(BufferedReader in, PrintWriter out, String jmbg, String datum) {
        File folder = new File("server/conversations/");
        File[] listOfFiles = folder.listFiles();
        ArrayList<File> foundFiles = new ArrayList<File>();
        
        for (File file : listOfFiles) {
            if (file.getName().contains(jmbg) && file.getName().contains(datum)) {
                foundFiles.add(file);
            }
        }
        
        return foundFiles;
        
    }
}
