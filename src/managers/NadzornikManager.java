package src.managers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Scanner;

import src.app.consoles.Teleekran;
import src.izvjestaji.Izvjestaj;

import java.util.ArrayList;
import java.util.Date;

public class NadzornikManager {
    
    // vraca broj konverzacija za uneseni datum i maticni broj radnika od interesa
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
    
    // vraca listu konverzacija za uneseni datum i jmbg
    public synchronized static ArrayList<File> nadjiKonverzacije(BufferedReader in, PrintWriter out, String jmbg,
                                                                 String datum) {
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
    
    // klijentska  metoda koja pretrazuje fajlove za trenutni datum i trazi niz rijeci koje su unesene, a zatim ukoliko pronadje neku od rijeci
    // radniku koji ju je napisao salje poruku na teleekran i smanjuje mu platu
    public static synchronized void pretragaPorukaPoKljucnimRijecimaClient(Scanner scan, BufferedReader in,
                                                                           PrintWriter out) {
        String kljucneRijeci = "";
        String odgovorServera = "";
        System.out.println("Unesite niz kljucnih rijeci od ineteresa:");
        kljucneRijeci = scan.nextLine();
        out.println(kljucneRijeci);
        try {
            odgovorServera = in.readLine();
            String[] sviMaticni = odgovorServera.split("#");
            for (String i : sviMaticni) {
                Teleekran saljiRadniku = new Teleekran("saljiR", scan, out, in, i.split("#")[0]);
                saljiRadniku.start();
                String jmbg = i.split("#")[0];
                RadnikManager.smanjiPlatu(jmbg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // metoda na serverskoj strani koja vraca maticne brojeve onih korisnika koji su upotrijebili kljucne rijeci prosledene kao argument
    public static synchronized String pretragaPoKljucnimRijecimaServer(String kljucneRijeci) {
        String maticni = ""; 
        String ime = "";
        String jmbg = "";
        String[] rijeci = kljucneRijeci.split(" ");
        File folder;
        SimpleDateFormat dat;
        try {
            dat = new SimpleDateFormat("dd-MM-yyyy");
            String datum = dat.format(new Date());
            folder = new File("server/conversations/");
            File[] listOfFiles = folder.listFiles();
            for (File f : listOfFiles) {
                if (f.getName().contains(datum)) {
                    BufferedReader citajSadrzaj = new BufferedReader(new FileReader(f));
                    String red;
                    while ((red = citajSadrzaj.readLine()) != null) { 
                        for (int i = 0; i < rijeci.length; i++) {
                            if (red.contains(rijeci[i])) {
                                ime = red.split(":")[0];
                                jmbg = nadjiMaticni(ime);
                                maticni += jmbg + "#";
                            }
                        }
                        
                    }
                    citajSadrzaj.close();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        return maticni;
    }
    
    //metoda koja vraca maticni broj radnika ciji username je prosledjen kao argument
    private static String nadjiMaticni(String ime) {
        try {
            File file = new File("server/korisnici_sa_jmbg.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String red = "";
            while ((red = br.readLine()) != null) {
                if (red.startsWith(ime)) {
                    return (red.split(" ")[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }
    
    //klijentska metoda za slanje poruke na sever koja ce zatim biti prosledjena prepravljacu na teleekran
    public static void slanjePorukePrepravljacu(Scanner scan, BufferedReader in, PrintWriter out) {
        
        System.out.println("Unesite poruku u obliku \"poruka:sadrzaj poruke\"");
        String message = scan.nextLine();
        String poruka = message.split(":")[1];
        out.println(poruka);
    }
    
    //metoda koja vraca listu izvjestaja koji se nalaze na serverskoj strani u folderu izvjestaji
    public static String[] pregledIzvjestaja() {
        File izvjestaji[] = new File("server/izvjestaji/").listFiles();
        String naziviIzvjestaja[] = new String[izvjestaji.length];
        int i = 1;
        
        for (File f : izvjestaji) {
            naziviIzvjestaja[i - 1] = f.getName();
            i++;
        }
        return naziviIzvjestaja;
    }
    
    // prikaz sadrzaja konkretnog izvjestaja ciji naziv je proslijedjen kao argument
    public static String prikaziSadrzajIzvjestaja(String nazivIzvjestaja) {
        System.out.println("Izvjestaj : " + nazivIzvjestaja);
        File izvjestaji[] = new File("server/izvjestaji/").listFiles();
        for (File f : izvjestaji) {
            String naziv = f.getName();
            if (naziv.equals(nazivIzvjestaja)) {
                try {
                    ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
                    Izvjestaj izvjestaj = (Izvjestaj) ois.readObject();
                    ois.close();
                    return izvjestaj.getSadrzaj();
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                break;
            }
        }
        return "Ne postoji trazeni ivjestaj";
    }
}
