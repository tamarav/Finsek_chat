package src.managers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

import src.exceptions.KorisnickoImeZauzetoException;
import src.exceptions.LozinkaSeNePoklapaException;
import src.exceptions.NedovoljnaDuzinaLozinkeException;
import src.exceptions.NekorektanJMBGException;
import src.exceptions.NekorektnoKorisnickoImeException;
import src.exceptions.PrazanUnosException;

import src.classes.Radnik;

public class RadnikManager {
    
    // metoda na klijentskoj strani koja vrsi interakciju korisnika sa sistmom pri registrovanju uz provjeru svih uesenih podataka
    public synchronized static Radnik registrujRadnika(Scanner scan) throws NekorektanJMBGException, LozinkaSeNePoklapaException, NekorektnoKorisnickoImeException, NedovoljnaDuzinaLozinkeException, PrazanUnosException{
        
        System.out.println("----------------REGISTRACIJA----------------");
        System.out.println("Unesite sljedece podatke:");
        
        System.out.println("Korisnicko ime: ");
        String username = scan.nextLine();
        
        if(Character.isDigit(username.charAt(0)) || username.length() < 4) {
            throw new NekorektnoKorisnickoImeException();
        }
        
        System.out.println("Lozinka: ");
        String password = scan.nextLine();
        System.out.println("Lozinku ponovo: ");
        String passwordAgain = scan.nextLine();
        
        if(!password.equals(passwordAgain)) {
            throw new LozinkaSeNePoklapaException();
        }
        
        if(password.length() < 3) {
            throw new NedovoljnaDuzinaLozinkeException();
        }
        
        System.out.println("Ime: ");
        String ime = scan.nextLine();
        
        if(ime.isEmpty()) {
            throw new PrazanUnosException();
        }
        
        System.out.println("Prezime: ");
        String prezime = scan.nextLine();
        
        if(prezime.isEmpty()) {
            throw new PrazanUnosException();
        }
        
        System.out.println("JMBG");
        String jmbg = scan.nextLine();
        upisiRadnikaUTXT(username, jmbg);
        return new Radnik(ime, prezime, jmbg, username, password);
    }
    
    // serverska metoda za upis registrovanog radika u txt fajl
    private synchronized static void upisiRadnikaUTXT(String username, String jmbg){
        try{
            File file = new File("server//korisnici_sa_jmbg.txt");
            BufferedWriter bw = new BufferedWriter(new FileWriter(file, true));
            bw.write(username + " " + jmbg);
            bw.newLine();
            bw.flush();
            bw.close();
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    // metoda na klijentskoj strani koja salje serijalizovan fajl registrovanog radnika koji se dalje provjerava na serverskoj strani
    public synchronized static void posaljiRadnika(Radnik radnik, OutputStream os, InputStream is, PrintWriter out) throws IOException{
        
        out.println(radnik.getUsername());
        
        String path = "client/" + radnik.getUsername() + ".ser";
        
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
        oos.writeObject(radnik);
        oos.close();
        
        FileManager.slanjeFajla(path, os, is, out);
        
    }
    
    //metoda na serverskoj strani koja prihvata serijalizovan fajl radnika kojeg treba registrovati uz provjeru podataka za registraciju
    public synchronized static Radnik prihvatiRadnika(InputStream is, OutputStream os, BufferedReader in){
        
        String username = null;
        
        try {
            username = in.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        String path = "server/clients/" + username + ".ser";
        
        FileManager.prihvatanjeFajla(path, is, os, in);
        
        return ucitajRadnikaIzMemorije(path);
        
    }
    
    // deserijalizuje fajl radnika kreirajuci novi objekat klase Radnik 
    private synchronized static Radnik ucitajRadnikaIzMemorije(String path){
        
        Radnik radnik = null;
        
        try {
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fis);
            radnik = (Radnik) ois.readObject();
            fis.close();
            ois.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        return radnik;
        
    }
    
    //koristi samo server za provjeru validnosti jmbg-a i zauzetosti korisnickog imena  pristiglog radnika sa klijentske strane
    public synchronized static boolean verifikujRegistovanogRadnika(Radnik radnik) {
        
        try {
            
            radnik.provjeraValidnosti(radnik.getJmbg());
            provjeraZauzetostiKorisnickogImena(radnik.getUsername());
            
            return true;
            
        } catch (NekorektanJMBGException | KorisnickoImeZauzetoException e) {
            System.out.println(e);
            return false;
        }
        
    }
    
    //koristi samo server za provjeru zauzetosti korisnickog imena 
    private synchronized static void provjeraZauzetostiKorisnickogImena(String username) throws KorisnickoImeZauzetoException{
        
        try(BufferedReader br = new BufferedReader(new FileReader("server/korisnici.txt"))){
            
            String line = null;
            
            while((line = br.readLine()) != null) {
                
                if(username.equals(line.split(" ")[0])) {
                    throw new KorisnickoImeZauzetoException();
                }
                
            }
            br.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    //serverska metoda za upis novog radnika u txt fajl
    public synchronized static void kreirajNalog(Radnik radnik){
        
        String userPass = radnik.getUsername() + " " + radnik.getPassword();
        
        File file = new File("server/korisnici.txt");
        
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            
            bw.write(userPass);
            bw.newLine();
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    // prijava radnika na klijentskoj strani uz slanje korisnickog imena i sifre serveru za provjeru
    public synchronized static String prijaviRadnika(Scanner scan, PrintWriter out, BufferedReader in){
        
        System.out.println("----------------PRIJAVA----------------");
        System.out.println("Unesite sljedece podatke:");
        
        System.out.println("Korisnicko ime: ");
        String username = scan.nextLine();
        System.out.println("Lozinka: ");
        String password = scan.nextLine();
        
        out.println(username + " " + password); 
        
        return username;
        
    }
    
    //koristi sreverska strana a provjeru pristiglih podataka (korisnickog imena i lozinke) pri prijavi radnika na sistem
    public synchronized static boolean provjeraPrijave(String userPass){
        
        String username = userPass.split(" ")[0];
        String password = userPass.split(" ")[1];
        
        boolean uspjesno = false;
        
        try (BufferedReader br = new BufferedReader(new FileReader("server/korisnici.txt"))) {
            
            String line;
            
            while((line = br.readLine()) != null){
                
                if(username.equals(line.split(" ")[0]) && password.equals(line.split(" ")[1])) {
                    uspjesno = true;
                    break;
                }
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        return uspjesno;
        
    }
    
    // na serverskoj strani upisuje prijavljenog radnika u txt fajl online korisnika 
    public synchronized static void upisUOnlineListu(String username){
        
        File file = new File("server/online.txt");
        
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            
            bw.write(username);
            bw.write(System.getProperty("line.separator"));
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    //pri prijavi radnika na sistem, prosledjuje mu se lista trenutnih onine korisnika 
    public synchronized static void posaljiOnlineKorisnike(OutputStream os, InputStream is, PrintWriter out) throws IOException{
        
        String path = "server/online.txt";
        
        FileManager.slanjeFajla(path, os, is, out);
        
    }
    
    //na klijentskoj strani prihvata se fajla online korisnika i memorise na klijentsku stranu
    public synchronized static void prihvatiOnlineKorisnike(InputStream is, OutputStream out, String username, BufferedReader in) {
        
        String path = "client/" + username + "/online.txt";
        
        File file = new File("client/" + username);
        file.mkdir();
        
        FileManager.prihvatanjeFajla(path, is, out, in);
        
    }
    
    // na klijentskoj strani se ispisuju online korisnici pristigli sa servera poslije prijave 
    public synchronized static void ispisiOnlineKorisnike(String username){
        
        File file = new File("client/" + username + "/online.txt");
        
        System.out.println("\nLISTA ONLINE KORISNIKA:");
        
        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            
            String line = null;
            
            while((line = br.readLine()) != null){
                //  /u2022 - ispisuje unicode karakter bullet
                System.out.println("\u2022 " + line);    
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        } 
        
        System.out.println();
        
    }
    
    // na serverskoj strani brise odjavljenog korisnika iz liste online korisnika 
    public synchronized static void odjaviKorisnika(String username){
        
        File file = new File("server/online.txt");
        File tempFile = new File("server/temp_online.txt");
        
        try(
            BufferedReader br = new BufferedReader(new FileReader(file));
            BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile));
            ) {
                
                String line;
                
                while((line = br.readLine()) != null) {
                    String trimmedLine = line.trim();
                    if(trimmedLine.equals(username)) continue;
                    // "line.separator" je isto sto i "\n" (isto tako se moglo koristiti i metoda newLine())
                    bw.write(line + System.getProperty("line.separator"));
                }
                
            } catch (IOException e) {
                e.printStackTrace();
            } 
            
            file.delete();
            tempFile.renameTo(file);
            
    }
    
    // metoda na serverskoj strani za cuvanje razgovora izmedju radnika
    public synchronized static void sacuvajKonverzaciju(String userKome, String userKo, String message){
        
        Radnik primaoc = ucitajRadnikaIzMemorije("server/clients/" + userKome + ".ser");
        Radnik posiljaoc = ucitajRadnikaIzMemorije("server/clients/" + userKo + ".ser");
        
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        DateFormat dateFormat1 = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss");
        Calendar cal = Calendar.getInstance();
        
        File file = new File("server/conversations/" + posiljaoc.getJmbg() + "-" + primaoc.getJmbg() + "-" + dateFormat1.format(cal.getTime()) + ".txt");;
        
        File folder = new File("server/conversations");
        File[] listOfFiles = folder.listFiles();
        
        String fileName = null;
        
        for(int i = 0; i < listOfFiles.length; i++) {
            
            if(listOfFiles[i].isFile()) {
                
                fileName = listOfFiles[i].getName();
                
                if(fileName.startsWith(primaoc.getJmbg() + "-" + posiljaoc.getJmbg() + "-" + dateFormat.format(cal.getTime()))) {
                    file = new File("server/conversations/" + fileName);
                } else if(fileName.startsWith(posiljaoc.getJmbg() + "-" + primaoc.getJmbg() + "-" + dateFormat.format(cal.getTime()))) {
                    file = new File("server/conversations/" + fileName);
                } 
                
            }
        }
        
        // stream objekat otvoren na ovaj nacin ne treba se eksplicitno zatvarati u finally bloku, vec se to automatski radi
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            
            bw.write(userKo + ": " + message);
            bw.newLine();
            bw.flush();
            bw.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    // metoda na severskoj strani koja umanjuje platu radnika ciji jmbg je proslijedjen
    public synchronized static void smanjiPlatu(String jmbg){
        Radnik radnik = null;
        String username = "";
        
        try {
            
            File fileLista = new File("server/clients/");
            File[] listaFajlova = fileLista.listFiles();
            File file = new File("server/korisnici_sa_jmbg.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line = "";
            FileInputStream fis;
            ObjectInputStream  ois;
            FileOutputStream fos;
            ObjectOutputStream oos;
            
            while((line = br.readLine()) != null){
                
                if(line.contains(jmbg)){
                    
                    username = line.split(" ")[0];
                    
                    for(File f:listaFajlova){
                        
                        if(f.getName().contains(username)){
                            
                            fis = new FileInputStream(f);
                            ois = new ObjectInputStream(fis);
                            radnik = (Radnik) ois.readObject();
                            
                            System.out.println("Radnik koji je upotrijebio nedovoljenu rijec je " + radnik.getUsername());
                            System.out.println("Trenutna plata je " + radnik.getPlata());
                            radnik.smanjiPlatu();
                            System.out.println("Nakon smanjivanja plata radnika iznosi: " + radnik.getPlata());
                            
                            ois.close();
                            fis.close();
                            
                            fos = new FileOutputStream(f);
                            oos = new ObjectOutputStream(fos);
                            oos.writeObject(radnik);
                            oos.flush();
                            
                        }
                    }
                }
            }
            
            br.close();
            
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

