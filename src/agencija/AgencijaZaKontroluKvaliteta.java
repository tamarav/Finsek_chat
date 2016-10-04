package src.agencija;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.String;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

import src.izvjestaji.Izvjestaj;
import src.izvjestaji.KreiranjeIzvjestaja;

public class AgencijaZaKontroluKvaliteta implements KreiranjeIzvjestaja {
    private String naziv;
    public static final int TCP_PORT = 9000;
    
    public AgencijaZaKontroluKvaliteta(String naziv) {
        this.naziv = naziv;
    }
    
    public static void main(String[] args) {
        
        InetAddress address = null;
        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;
        OutputStream os = null;
        InputStream is = null;
        
        try {
            address = InetAddress.getByName("127.0.0.1");
            socket = new Socket(address, TCP_PORT);
        } catch (IOException e) {
            System.out.println("Neuspjela konekcija sa serverom!");
            return;
        }
        
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            os = socket.getOutputStream();
            is = socket.getInputStream();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        
        out.println("agencija");
        
        System.out.print("Unesite naziv agencije:");
        Scanner scan = new Scanner(System.in);
        String naziv = scan.nextLine();
        out.println(naziv);
        
        System.out.println("*******Dobrodosli u agenciju za kontrolu kavliteta " + naziv + " *******");
        
        int opcija = 0;
        boolean uspjesno = false;
        
        try {
            do {
                System.out.println("Izaberite jednu opciju:");
                System.out.println("1 - Kreiranje izvjetaja");
                System.out.println("2 - Pregled izvjestaja");
                System.out.println("3 - Kraj");
                
                opcija = scan.nextInt();
                scan.nextLine();
                
                uspjesno = true;
                
                switch (opcija) {
                    case 1:
                        out.println("1");
                        String odgovor = in.readLine();
                        if (odgovor.equals("uspjesno")) {
                            System.out.println("Izvjestaj je napravljen i sacuvan u arhivu.");
                        }
                        break;
                    case 2:
                        out.println("2");
                        try {
                            int duzina = (in.readLine().length());
                            String listaIzvjestaja = in.readLine();
                            String[] lista = new String[duzina];
                            lista = listaIzvjestaja.split("#");
                            if (lista.length == 0) {
                                System.out.println("U folderu se ne nalazi nijedan izvjestaj");
                            } else {
                                int br = 0;
                                
                                System.out.println("Sadrzaj foldera izvjestaji:");
                                for (String s : lista)
                                    System.out.println("\u2022" + (++br) + "\u2022" + " " + s);
                                
                                String zahtjevZaPregled = "";
                                do{
                                    System.out.println("Zelite li da pogledate sadrzaj nekog odredjenog izvjestaja? (da/ne)");
                                    zahtjevZaPregled = scan.nextLine();
                                } while(!zahtjevZaPregled.equalsIgnoreCase("da") && !zahtjevZaPregled.equalsIgnoreCase("ne"));
                                if (zahtjevZaPregled.equalsIgnoreCase("da")) {
                                    System.out.println("Unesite redni broj izvjestaja");
                                    int redniBroj = scan.nextInt();
                                    System.out.println(prikaziSadrzajIzvjestaja(lista[redniBroj - 1]));
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 3:
                        out.println("3");
                        return;
                    default:
                        System.out.println("Greska u unosu! Probajte ponovo.");
                        uspjesno = false;
                        break;
                }
            } while (!uspjesno || opcija != 3);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // -----------------------------------------
        scan.close();
        
        try {
            in.close();
            out.close();
            os.close();
            is.close();
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        System.out.println("----------------POZDRAV----------------");
    }
    
    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }
    
    public String getNaziv() {
        return naziv;
    }
    
    // metoda zza kreiranje izvjestaja 
    public void kreirajIzvjestaj(String koKreiraIzvjestaj) {
        
        Izvjestaj<String> izvjestaj = new Izvjestaj<String>();
        Date d = new Date();
        String sadrzajIzvjestaja[] = { "Izvjestaj o prihodima", "Izvjestaj o rashodima", "Izvjestaj o stanju u skladistu" };
        Random rand = new Random();
        String sadrzaj = sadrzajIzvjestaja[rand.nextInt(3)];
        izvjestaj.setSadrzaj(sadrzaj);
        
        String prilozi[] = { "prilogZaIzvjestaj", "string", "prilog" };
        String p = prilozi[rand.nextInt(3)];
        izvjestaj.setGenerickiPrilog(p);
        
        try {
            
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
            String naziv = "Izvjestaj_za_" + sdf.format(d);
            izvjestaj.setNaziv(naziv);
            String datum = sdf.format(d);
            izvjestaj.setDate(datum);
            
            if (koKreiraIzvjestaj.equals("nadzornik")) {
                
                FileOutputStream fos = new FileOutputStream("server/izvjestaji/" + "nadzornik - " + naziv);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(izvjestaj);
                oos.close();
                fos.close();
                
            } else {
                
                FileOutputStream fos = new FileOutputStream( "server/izvjestaji/" + "agencija " + this.getNaziv() + " - " + naziv);
                ObjectOutputStream upisObjekta = new ObjectOutputStream(fos);
                upisObjekta.writeObject(izvjestaj);
                upisObjekta.close();
                fos.close();
                
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    //metoda koja prikazuje sadrzaj izvjestaja ciji je naziv prosledjen kao parametar
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
