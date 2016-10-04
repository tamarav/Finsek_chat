package src.servers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import java.util.ArrayList;

import src.classes.Nadzornik;
import src.classes.Radnik;
import src.exceptions.NekorektanJMBGException;
import src.managers.RadnikManager;
import src.managers.FileManager;
import src.managers.NadzornikManager;
import src.agencija.AgencijaZaKontroluKvaliteta;

public class ServerPricaloThread extends Thread {
    
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    OutputStream os;
    InputStream is;
    
    String username;
    
    // cuvanje korisnickog imena kao kljuca i odgovarajuce niti  kao vrijednosti
    private static ConcurrentHashMap<String, ServerPricaloThread> onlineKorisnici = new ConcurrentHashMap<>();
    
    private static String status = "";
    
    public ServerPricaloThread(Socket socket) {
        super();
        this.socket = socket;
        
        try {
            
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
            os = socket.getOutputStream();
            is = socket.getInputStream();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        this.start();
    }
    
    @Override
    public void run() {
        
        boolean uspjesno = false;
        String user = "";
        
        try {
            
            user = in.readLine();
            
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        //  R A D N I K 
        if (user.equals("radnik")) {
            do {
                
                String opcija = null;
                try {
                    opcija = in.readLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                
                switch (opcija) {
                    // registrovanje novog radnika
                    case "1":
                        Radnik radnik = RadnikManager.prihvatiRadnika(is, os, in);
                        
                        if (RadnikManager.verifikujRegistovanogRadnika(radnik)) {
                            
                            RadnikManager.kreirajNalog(radnik);
                            
                            System.out.println("Uspjestno registrovan korisnik " + radnik.getUsername() + "!");
                            
                            out.println("1");
                            
                            uspjesno = true;
                            
                            this.username = radnik.getUsername();
                            onlineKorisnici.put(radnik.getUsername(), this);
                            
                            RadnikManager.upisUOnlineListu(radnik.getUsername());
                            
                        } else {
                            
                            System.out.println("Neuspjesna registracija!");
                            
                            out.println("0");
                            
                        }
                        
                        break;
                        
                    // prijava vec registrovanog radnika na sistem
                    case "2":
                        
                        try {
                        
                        String userPass = in.readLine();
                        
                        System.out.println("Korisnik " + userPass.split(" ")[0] + " se prijavio.");
                        
                        if (RadnikManager.provjeraPrijave(userPass)) {
                            out.println("1");
                            uspjesno = true;
                            this.username = userPass.split(" ")[0];
                            onlineKorisnici.put(this.username, this);
                            RadnikManager.upisUOnlineListu(this.username);
                        } else {
                            out.println("0");
                            uspjesno = false;
                        }
                        
                    } catch (IOException e) {
                        uspjesno = false;
                        e.printStackTrace();
                    }
                    
                    break;
                    //odjava
                    case "3":
                        uspjesno = true;
                        break;
                        
                    default:
                        break;
                }
                
            } while (!uspjesno);
            
            try {
                // ispis korisnika koji su trenutno online
                RadnikManager.posaljiOnlineKorisnike(os, is, out);
            } catch (IOException e1) {
                System.out.println("Neuspjelo slanje online korisnika klijentu.");
            }
            
            if (this.username != null) {
                
                String poruka = null;
                // razmjena poruka izmedju radnika
                do {
                    
                    try {
                        poruka = in.readLine();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    
                    posaljiPoruku(poruka);
                    
                } while (!poruka.equalsIgnoreCase("KRAJ") && !status.equalsIgnoreCase("ZAUSTAVI"));
                
            }
            // N A D Z O R N I K
        } else if (user.equals("nadzornik")) {
            
            onlineKorisnici.put("nadzornik", this);
            
            try {
                Nadzornik admin = new Nadzornik("Admin", "Admin", "2211989125034");
                username = "Nadzornik";
                String request = "";
                boolean uspjesnoN = false;
                do {
                    request = in.readLine();
                    System.out.println(request);
                    
                    switch (request) {
                        // pregled poruka svakog radnika
                        case "1": 
                            String pod1 = "", pod2 = "";
                            int brojKonverzacija, zahtjevaniBroj = 0;
                            ArrayList<File> listOfFiles = new ArrayList<File>();
                            String podaci = null;
                            
                            try {
                                podaci = in.readLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            
                            pod1 = podaci.split(" ")[0];
                            pod2 = podaci.split(" ")[1];
                            System.out.println("maticni: " + pod1);
                            System.out.println("datum: " + pod2);
                            listOfFiles = FileManager.nadjiKonverzacije(in, out, pod1, pod2);
                            System.out.println(listOfFiles);
                            brojKonverzacija = listOfFiles.size();
                            System.out.println("broj konverzacija " + brojKonverzacija);
                            out.println(brojKonverzacija);
                            if (brojKonverzacija != 0) {
                                do {
                                    try {
                                        zahtjevaniBroj = Integer.parseInt(in.readLine());
                                        if (zahtjevaniBroj <= brojKonverzacija) {
                                            System.out.println("Nadzornik trazi " + (int) zahtjevaniBroj);
                                            for (int i = 0; i < (int) zahtjevaniBroj; i++) {
                                                out.println(listOfFiles.get(i).getName());
                                                FileManager.slanjeFajla(listOfFiles.get(i).getAbsolutePath(), os, is, out);
                                            }
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                } while (zahtjevaniBroj > brojKonverzacija);
                            }
                            break;
                        
                        // pretraga konverzacija po kljucnik rijecima    
                        case "2":
                            String kljucneRijeci = in.readLine();
                            String rezultat = NadzornikManager.pretragaPoKljucnimRijecimaServer(kljucneRijeci);
                            out.println(rezultat);
                            String porukaTelekran = "";
                            try {
                                porukaTelekran = in.readLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            posaljiPorukuNaTeleekran(porukaTelekran);
                            break;
                        
                        // slanje poruke prepravljacu na teleekran
                        case "3":
                            String poruka = in.readLine();
                            System.out.println(poruka);
                            posaljiPorukuNaTeleekran("prepravljac#" + poruka);
                            break;
                            
                        // kreiranje novog izvjestaja
                        case "4":
                            admin.kreirajIzvjestaj("nadzornik");
                            out.println("uspjesno");
                            break;
                            
                        // pregled sadrzaja foldera izvjestaji
                        case "5":
                            String[] listaIzvjestaja = NadzornikManager.pregledIzvjestaja();
                            out.println(listaIzvjestaja);
                            String odgovor = "";
                        for (String s : listaIzvjestaja) {
                            odgovor += s + "#";
                        }
                        out.println(odgovor);
                        break;
                        
                        case "6":
                            uspjesnoN = true;
                            break;
                        
                        // zaustavljanje simulacije
                        case "ZAUSTAVI":
                            status = "ZAUSTAVI";
                            int vrijemeTrajanjaSimulacije = Math.round((System.currentTimeMillis() - ServerPricalo.getStartTime()) / 1000);
                            System.out.println("Simulacija je trajala " + vrijemeTrajanjaSimulacije + " sekundi.");
                            zaustaviSveNiti();
                            uspjesnoN = true;
                            break;
                            
                    }
                } while (!uspjesnoN);
            } catch (IOException | NekorektanJMBGException e) {
                e.printStackTrace(); 
            }
        // P R E P R A V LJ A C    
        } else if (user.equals("prepravljac")) {
            onlineKorisnici.put("prepravljac", this);
            username = "Prepravljac";
            String request = "";
            try {
                do {
                    request = in.readLine();
                    switch (request) {
                        // pregled konverzacija
                        case "1":
                            String pod1 = "", pod2 = "";
                            int brojKonverzacija;
                            ArrayList<File> listOfFiles = new ArrayList<File>();
                            String podaci = null;
                            try {
                                podaci = in.readLine();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            pod1 = podaci.split(" ")[0];
                            pod2 = podaci.split(" ")[1];
                            
                            System.out.println("maticni: " + pod1);
                            System.out.println("datum: " + pod2);
                            
                            listOfFiles = FileManager.nadjiKonverzacije(in, out, pod1, pod2);
                            brojKonverzacija = listOfFiles.size();
                            out.println(brojKonverzacija);
                            
                            if (brojKonverzacija != 0) {
                                String zahtjevZaPreuzimanje = in.readLine();
                                if (zahtjevZaPreuzimanje.equalsIgnoreCase("da")) {
                                    Random rand = new Random();
                                    int i = rand.nextInt(brojKonverzacija);
                                    String nazivFajla = listOfFiles.get(i).getName();
                                    out.println(nazivFajla);
                                    System.out.println("naziv fajla je " + nazivFajla);
                                    FileManager.slanjeFajla(listOfFiles.get(i).getAbsolutePath(), os, is, out);
                                    File tmp = new File(listOfFiles.get(i).getAbsolutePath());
                                    tmp.delete();
                                    FileManager.prihvatanjeFajla("server/conversations/" + nazivFajla, is, os, in);
                                }
                                
                            }
                            break;
                    }
                } while (!request.equals("2"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        // A  G E N C I J A
        } else if (user.equals("agencija")) {
            try {
                String nazivAgencije = in.readLine();
                String request = "";
                AgencijaZaKontroluKvaliteta agencija = new AgencijaZaKontroluKvaliteta(nazivAgencije);
                do {
                    request = in.readLine();
                    if(status.equalsIgnoreCase("ZAUSTAVI")) {
                        break;
                    }
                    switch (request) {
                        // kreiranje novog izvjestaja
                        case "1":
                            agencija.kreirajIzvjestaj("agencija " + agencija.getNaziv());
                            out.println("uspjesno");
                            break;
                        // pregled izvjestaja
                        case "2":
                            String[] listaIzvjestaja = NadzornikManager.pregledIzvjestaja();
                            out.println(listaIzvjestaja);
                            String odgovor = "";
                        for (String s : listaIzvjestaja) {
                            odgovor += s + "#";
                        }
                        out.println(odgovor);
                        break;
                    }
                } while (!request.equals("3"));
                
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        }
        
    }
    
    // prihvata poruka sa klijentske strane, parsira ju i prosledjuje odgovarajucoj niti kojoj je poruka namijenjena 
    private void posaljiPoruku(String userMsg) {
        
        if (userMsg.equalsIgnoreCase("KRAJ") || userMsg.equalsIgnoreCase("ZAUSTAVI")) {
            out.println(userMsg);
            System.out.println("Korisnik " + username + " se odjavio.");
            onlineKorisnici.remove(username);
            RadnikManager.odjaviKorisnika(username);
            this.out.println(userMsg);
            return;
        }
        
        if (!userMsg.contains(": ")) {
            return;
        }
        
        String username = userMsg.split(": ", 2)[0];
        String message = userMsg.split(": ", 2)[1];
        
        if (onlineKorisnici.containsKey(username)) {
            // nit kojoj je namijenjena poruka 
            ServerPricaloThread reciever = onlineKorisnici.get(username);
            reciever.slanje(this.username + ": " + message);
            
            System.out.println(this.username + " -> " + reciever.username + ": " + message);
            
            RadnikManager.sacuvajKonverzaciju(username, this.username, message);
            
        }
        
    }
    
    // prihvata poruka sa klijentske strane, parsira ju i prosledjuje odgovarajucoj niti kojoj je poruka namijenjena  na teleekran
    private void posaljiPorukuNaTeleekran(String userMsg) {
        
        String username = userMsg.split("#")[0];
        String message = userMsg.split("#")[1];
        if (onlineKorisnici.containsKey(username)) {
            ServerPricaloThread reciever = onlineKorisnici.get(username);
            reciever.slanje("******Teleekran*******\nNadzornik: " + message);
        }
        
    }
    
    // slanje poruke na klijentsku stranu
    private void slanje(String poruka) {
        out.println(poruka);
    }
    
    //metoda za zaustavljanje simulacije
    private static void zaustaviSveNiti(){
        for (Map.Entry<String, ServerPricaloThread> serverThread: onlineKorisnici.entrySet()) {
            if(!serverThread.getValue().username.equalsIgnoreCase("nadzornik") && !serverThread.getValue().username.equalsIgnoreCase("prepravljac")) {
                serverThread.getValue().posaljiPoruku("ZAUSTAVI");
            }
            serverThread.getValue().interrupt();
        }
        
    }
}
