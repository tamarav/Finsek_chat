package src.managers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

public class PrepravljacManager {

    // metoda koja vraca broj koverzacija radnika ciji jmbg je unesen za datum koji prepravljac unese
    public synchronized static int pregledPorukaRadnika(Scanner scan, BufferedReader in, PrintWriter out) {
        
        String podatak1 = "";
        String podatak2 = "";
        
        System.out.println("Unesite maticni broj radnika od interesa:");
        podatak1 = scan.nextLine();
        System.out.println("Unesite datum za koji zelite da vidite prepiske u formatu dan-mjesec-godina ");
        podatak2 = scan.nextLine();
        out.println(podatak1 + " " + podatak2);
        String brojKonverzacija = "";
        try {
            brojKonverzacija = in.readLine();
            System.out.println("Za unesene podatke postoje " + brojKonverzacija + " prepiske");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Integer.parseInt(brojKonverzacija);
    }
    
    // metoda koja prima fajl sa serverske strane, ocitava njegov sadrzaj, vrsi zamjenu nedozvoljenih rijeci te vraca prepravljeni fajl nazad na serversku stranu
    public static synchronized void prepraviFajl(String path, BufferedReader in, PrintWriter out, OutputStream os,
                                                 InputStream is) {
        File file, fajl;
        BufferedReader br, br2;
        String[] nedozvoljeneRijeci;
        String[] zamjena;
        String line = "", tmp1 = "", tmp2 = "";
        BufferedWriter bw;
        
        try {
            file = new File(path);
            br = new BufferedReader(new FileReader(file));
            File tmp = new File("prepravljac/pomocna.txt");
            if (tmp.exists()) {
                tmp.delete();
            }
            bw = new BufferedWriter(new FileWriter(tmp, true));
            fajl = new File("server/tabela_sa_rijecima.txt");
            br2 = new BufferedReader(new FileReader(fajl));
            
            while ((line = br2.readLine()) != null) {
                tmp1 += line.split(" ")[0] + "#";
                tmp2 += line.split(" ")[1] + "#";
            }
            br2.close();
            nedozvoljeneRijeci = tmp1.split("#");
            zamjena = tmp2.split("#");
            int pom = 0;
            
            while ((line = br.readLine()) != null) {
                pom++;
                for (int i = 0; i < nedozvoljeneRijeci.length; i++) {
                    if (line.contains(nedozvoljeneRijeci[i])) {
                        
                        bw.write(line.replaceAll(nedozvoljeneRijeci[i], zamjena[i]));
                        bw.newLine();
                        bw.flush();
                        
                        pom = 0;
                    }
                }
                if (pom != 0) {
                    bw.write(line);
                    bw.newLine();
                    bw.flush();
                }
            }
            System.out.println("Fajl je uspjesno preuzet i prepravljanje je zavrseno");
            FileManager.slanjeFajla("prepravljac/pomocna.txt", os, is, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
}
