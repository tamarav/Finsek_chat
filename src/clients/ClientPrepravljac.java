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

import src.app.consoles.Teleekran;
import src.managers.FileManager;
import src.managers.PrepravljacManager;

public class ClientPrepravljac {
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
        Scanner scan = new Scanner(System.in);
        out.println("prepravljac");
        System.out.println("----------------DOBRODOSLI----------------");
        
        Teleekran slusajTeleekran = new Teleekran("slusaj", scan, out, in);
        int opcija = 0;
        boolean uspjesno = false;
        do {
            
            System.out.println("Izaberite jednu opciju:");
            System.out.println("1 - Pregled poruka");
            System.out.println("2 - Kraj");
            
            opcija = scan.nextInt();
            scan.nextLine();
            
            uspjesno = true;
            switch (opcija) {
                case 1:
                    out.println("1");
                    
                    int broj = PrepravljacManager.pregledPorukaRadnika(scan, in, out);
                    if (broj != 0) {
                        String odgovor = "";
                        System.out.println("Zelite li da preuzmete fajl za prepravljanje? (da/ne)");
                        out.println(odgovor = scan.nextLine());
                        
                        if (odgovor.equalsIgnoreCase("da")) {
                            String name;
                            try {
                                name = in.readLine();
                                FileManager.prihvatanjeFajla("prepravljac/" + name, is, os, in);
                                PrepravljacManager.prepraviFajl("prepravljac/" + name, in, out, os, is);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            
                        }
                    }
                    break;
                case 2:
                    out.println("2");
                    slusajTeleekran.start();
                    return;
                default:
                    System.out.println("Greska u unosu! Probajte ponovo.");
                    uspjesno = false;
                    break;
            }
            
        } while (!uspjesno || opcija != 2);
        
        
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
