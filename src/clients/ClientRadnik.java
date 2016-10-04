package src.clients;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import src.app.consoles.RadnikConsole;
import src.app.consoles.Teleekran;
import src.classes.Radnik;
import src.managers.RadnikManager;
import src.exceptions.LozinkaSeNePoklapaException;
import src.exceptions.NedovoljnaDuzinaLozinkeException;
import src.exceptions.NekorektanJMBGException;
import src.exceptions.NekorektnoKorisnickoImeException;
import src.exceptions.PrazanUnosException;

public class ClientRadnik {
    
    public static final int TCP_PORT = 9000;
    
    public static void main(String[] args) {
        
        InetAddress address = null;
        Socket socket = null;
        BufferedReader in = null;
        PrintWriter out = null;
        OutputStream os = null;
        InputStream is = null;
        
        String username = null;
        
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
        
        out.println("radnik");
        System.out.println("----------------DOBRODOSLI NA PRICALO----------------");
        
        Scanner scan;
        int opcija = 0;
        
        boolean uspjesno = false;
        
        do {
            
            System.out.println("Izaberite jednu opciju:");
            System.out.println("1 - Registrujte se");
            System.out.println("2 - Prijavite se");
            System.out.println("3 - Kraj");
            
            scan = new Scanner(System.in);
            opcija = scan.nextInt();
            scan.nextLine();
            
            uspjesno = true;
            
            switch (opcija) {
                case 1:
                    
                    Radnik radnik = null;
                    try {
                        radnik = RadnikManager.registrujRadnika(scan);
                        
                        if (radnik == null) {
                            uspjesno = false;
                            break;
                        }
                    } catch (NekorektanJMBGException | LozinkaSeNePoklapaException | NekorektnoKorisnickoImeException
                                 | NedovoljnaDuzinaLozinkeException | PrazanUnosException ex) {
                        uspjesno = false;
                        System.out.println(ex);
                        break;
                    }
                    
                    try {
                        out.println("1");
                        RadnikManager.posaljiRadnika(radnik, os, is, out);
                        String uspjesnostRegistracije = in.readLine();
                        
                        if (uspjesnostRegistracije.equals("1")) {
                            System.out.println("Uspjesno ste se registrovali!");
                            username = radnik.getUsername();
                        } else {
                            System.out.println("Registracija nije uspjela! Mozda je korisnicko ime zauzeto. Probajte ponovo.");
                            uspjesno = false;
                        }
                        
                    } catch (IOException e) {
                        uspjesno = false;
                        System.out.println("Doslo je do greske!");
                        break;
                    }
                    
                    break;
                case 2:
                    
                    out.println("2");
                    
                    username = RadnikManager.prijaviRadnika(scan, out, in);
                    
                    try {
                        if (in.readLine().equals("1")) {
                            System.out.println("Uspjesno ste se prijavili.");
                            uspjesno = true;
                        } else {
                            System.out.println("Neuspjela prijava! Probajte opet.");
                            uspjesno = false;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                    break;
                case 3:
                    scan.close();
                    out.println("3");
                    System.out.println("----------------POZDRAV----------------");
                    return;
                    
                default:
                    System.out.println("Greska u unosu! Probajte ponovo.");
                    uspjesno = false;
                    break;
            }
        } while (!uspjesno);
        
        RadnikManager.prihvatiOnlineKorisnike(is, os, username, in);
        
        RadnikManager.ispisiOnlineKorisnike(username);
        
        System.out.println("Poruke pisete u formatu: 'korisnickoIme: poruka'");
        
        RadnikConsole salji = new RadnikConsole("salji", scan, out, in);
        RadnikConsole slusaj = new RadnikConsole("slusaj", scan, out, in);
        Teleekran slusajTeleekran = new Teleekran("slusaj", scan, out, in);
        
        salji.start();
        slusaj.start();
        slusajTeleekran.start();
        
        try {
            
            salji.join();
            slusaj.join();
            slusajTeleekran.join();
            
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        
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
    
}
