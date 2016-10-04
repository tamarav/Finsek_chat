package src.app.consoles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class RadnikConsole extends Thread {
    
    String mod;
    Scanner scan;
    PrintWriter out;
    BufferedReader in;
    
    public RadnikConsole(String mod, Scanner scan, PrintWriter out, BufferedReader in) {
        this.mod = mod;
        this.scan = scan;
        this.out = out;
        this.in = in;
    }
    
    @Override
    public void run() {
        
        if (mod.equals("salji")) {
            this.salji();
        } else if (mod.equalsIgnoreCase("slusaj")) {
            this.slusaj();
        }
        
    }
    
    // unos poruka u formatu "primaoc:tekst poruke" i slanje na server dok unesena poruka ne bude "kraj" koja se takodje salje na server 
    private void salji() {
        
        String poruka = null;
        
        do {
            
            poruka = scan.nextLine();
            out.println(poruka);
            
        } while (!poruka.equalsIgnoreCase("KRAJ"));
        
    }
    
    // primanje poruka od drugih radnika sa servera
    private void slusaj() {
        
        String poruka = null;
        
        while (true) {
            
            try {
                poruka = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            if (poruka.equalsIgnoreCase("KRAJ")) {
                break;
            }
            
            System.out.println(poruka);
            
        }
        
    }
    
}
