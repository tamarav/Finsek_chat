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

import src.managers.FileManager;
import src.managers.NadzornikManager;

public class ClientNadzornik {
    public static final int TCP_PORT = 9000;
    
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
        
        out.println("nadzornik");
        
        System.out.println("----------------DOBRODOSLI----------------");
        
        Scanner scan;
        String opcija = "";
        boolean uspjesno = false;
        
        do {
            System.out.println("Izaberite jednu opciju:");
            System.out.println("1 - Pregled poruka radnika");
            System.out.println("2 - Pretrazivanje poruka po kljucnim rijecima");
            System.out.println("3 - Slanje poruka prepravljacima na teleekran");
            System.out.println("4 - Kreiranje izvjestaja");
            System.out.println("5 - Pregled sadrzaja foldera Izvjestaji");
            System.out.println("6 - Kraj");
            System.out.println("ZAUSTAVI - Za zavrsetak simulacije");
            
            scan = new Scanner(System.in);
            opcija = scan.nextLine();
            
            uspjesno = true;
            
            switch (opcija) {
                case "1":
                    out.println("1");
                    String brojZaPreuzimanje = "";
                    
                    int broj = NadzornikManager.pregledPorukaRadnika(scan, in, out);
                    if (broj != 0) {
                        do {
                            System.out.println("Koliko fajlova zelite da preuzmete?");
                            brojZaPreuzimanje = scan.nextLine();
                            if ((int) Integer.parseInt(brojZaPreuzimanje) > broj) {
                                System.out.println("Izvrsili ste nekorektan unos, molimo pokusajte ponovo.");
                            }
                            if ((int) Integer.parseInt(brojZaPreuzimanje) <= broj) {
                                out.println(brojZaPreuzimanje);
                                String name = "";
                                try {
                                    for (int i = 0; i < (int) Integer.parseInt(brojZaPreuzimanje); i++) {
                                        name = in.readLine();
                                        FileManager.prihvatanjeFajla("nadzornik/conversations/" + name, is, os, in);
                                    }
                                    System.out.println("Fajlovi su uspjenso preuzeti i nalaze se na lokaciji nadzornik/conversations/.");
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        } while ((int) Integer.parseInt(brojZaPreuzimanje) > broj);
                    }
                    break;
                case "2":
                    out.println("2");
                    NadzornikManager.pretragaPorukaPoKljucnimRijecimaClient(scan, in, out);
                    break;
                case "3":
                    out.println("3");
                    NadzornikManager.slanjePorukePrepravljacu(scan, in, out);
                    break;
                case "4":
                    out.println("4");
                    try {
                        String odgovor = in.readLine();
                        if (odgovor.equals("uspjesno")) {
                            System.out.println("Izvjestaj je napravljen i sacuvan u arhivu.");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "5":
                    out.println("5");
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
                            for (String s : lista) {
                                System.out.println("\u2022" + (++br) + "\u2022" + " " + s);
                            }
                            String zahtjevZaPregled = "";
                            do{
                                System.out.println("Zelite li da pogledate sadrzaj nekog odredjenog izvjestaja? Odgovor uneseite u obliku \"da\" ili \"ne\"");
                                zahtjevZaPregled = scan.nextLine();
                            }while(!zahtjevZaPregled.equalsIgnoreCase("da") && !zahtjevZaPregled.equalsIgnoreCase("ne"));
                            if (zahtjevZaPregled.equalsIgnoreCase("da")) {
                                int redniBroj;
                                do{
                                    System.out.println("Unesite redni broj izvjestaja (Vodite racuna da uneseni broj odgovara ponudjenom opsegu)");
                                    redniBroj = scan.nextInt();
                                }while(redniBroj > lista.length);
                                System.out.println(NadzornikManager.prikaziSadrzajIzvjestaja(lista[redniBroj - 1]));
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "6":
                    out.println("6");
                    uspjesno = true;
                    return;
                case "ZAUSTAVI": 
                    out.println("ZAUSTAVI");
                    break;    
                default:
                    System.out.println("Greska u unosu! Probajte ponovo.");
                    uspjesno = false;
                    break;
            }
        } while (!uspjesno || !opcija.equalsIgnoreCase("ZAUSTAVI"));

        scan.close();
        
        System.out.println("----------------USPJESNO STE SE ODJAVILI----------------");
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
        
    }
    
}
