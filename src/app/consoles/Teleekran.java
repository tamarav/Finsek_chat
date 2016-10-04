package src.app.consoles;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Teleekran extends Thread {
    String mod;
    Scanner scan;
    PrintWriter out;
    BufferedReader in;
    String jmbg;
    
    public Teleekran(String mod, Scanner scan, PrintWriter out, BufferedReader in, String jmbg) {
        this.mod = mod;
        this.scan = scan;
        this.out = out;
        this.in = in;
        this.jmbg = jmbg;
    }
    
    public Teleekran(String mod, Scanner scan, PrintWriter out, BufferedReader in) {
        this.mod = mod;
        this.scan = scan;
        this.out = out;
        this.in = in;
    }
    
    @Override
    public void run() {
        
        if (mod.equals("saljiR")) {
            this.saljiRadniku();
        } else if (mod.equalsIgnoreCase("slusaj")) {
            this.slusaj();
        }
    }
    
    // metoda za slanje poruka radniku an teleekran
    private void saljiRadniku() {
        String username = pronadjiRadnika();
        String poruka = "#Zbog neadekvatnog ponasanja vasa plata ce biti umanjena za 100.";
        out.println(username + poruka);
    }
    
    //metoda za prijem poruka na teleekran
    private void slusaj() {
        String poruka = null;
        
        while (true) {
            try {
                poruka = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(poruka);
        }
    }
    
    private String pronadjiRadnika() {
        boolean pronadjen = false;
        String username = "";
        do {
            try {
                File path = new File("server//korisnici_sa_jmbg.txt");
                BufferedReader br = new BufferedReader(new FileReader(path));
                String line;
                while ((line = br.readLine()) != null) {
                    if (this.jmbg.equals(line.split(" ")[1])) {
                        username = line.split(" ")[0];
                        pronadjen = true;
                        System.out.println(username);
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
        } while (!pronadjen);
        
        return username;
        
    }
}
