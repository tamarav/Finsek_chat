package src.classes;

import java.io.Serializable;

import src.exceptions.NekorektanJMBGException;

public abstract class Osoba implements Serializable {
    
    private String ime, prezime, jmbg;
    
    public Osoba(String ime, String prezime, String jmbg) throws NekorektanJMBGException {
        super();
        this.ime = ime;
        this.prezime = prezime;
        this.provjeraValidnosti(jmbg);
        this.jmbg = jmbg;
    }
    
    public String getIme() {
        return ime;
    }
    
    public void setIme(String ime) {
        this.ime = ime;
    }
    
    public String getPrezime() {
        return prezime;
    }
    
    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }
    
    public String getJmbg() {
        return jmbg;
    }
    
    public void setJmbg(String jmbg) {
        this.jmbg = jmbg;
    }
    
    // metoda za provjeru validnosti maticnog broja, implementirana na osnovu algoritma sa http://en.wikipedia.org/wiki/Unique_Master_Citizen_Number
    public boolean provjeraValidnosti(String jmbg) throws NekorektanJMBGException {
       
        boolean ispravan = true;
        
        if (jmbg.length() != 13) {
            throw new NekorektanJMBGException();
        } else {
            char[] charoviMaticniBroj = new char[13];
            
            jmbg.getChars(0, 13, charoviMaticniBroj, 0); 
            
            int[] niz = new int[13];
            for (int i = 0; i < 13; i++) {
                niz[i] = (int) charoviMaticniBroj[i] - 48; 
            }
            
            int proizvod, checksum;
            proizvod = (7 * (niz[0] + niz[6]) + 6 * (niz[1] + niz[7]) + 5 * (niz[2] + niz[8]) + 4 * (niz[3] + niz[9])
                            + 3 * (niz[4] + niz[10]) + 2 * (niz[5] + niz[11]));
            checksum = 11 - proizvod % 11;
            
            String godinaRodjenjaString = jmbg.substring(4, 7); 
            if (godinaRodjenjaString.startsWith("0")) {
                godinaRodjenjaString = 2 + godinaRodjenjaString;
            } else
                godinaRodjenjaString = 1 + godinaRodjenjaString;
            
            Integer godinaRodjenjaInteger = new Integer(godinaRodjenjaString);
            int godinaRodjenja = godinaRodjenjaInteger.intValue();
            
            boolean prestupnaGodina = false;
            if ((godinaRodjenja % 400 == 0) || ((godinaRodjenja % 4 == 0) && (godinaRodjenja % 100 != 0))) {
                prestupnaGodina = true;
                
            }
            if (!(niz[0] < 3 || (niz[0] == 3 && niz[0] <= 1))) {
                ispravan = false;
            } else if (!(niz[2] == 0 || niz[2] == 1)) {
                ispravan = false;
            } else if (niz[7] == 6) {
                ispravan = false;
            } else if ((niz[12] != checksum) || (checksum >= 10 && niz[12] != 0)) {
                ispravan = false;
            } // provjeriti
            else if ((prestupnaGodina) && niz[1] > 9 && niz[4] == 2) {
                ispravan = false;
            } else if ((!prestupnaGodina) && niz[1] > 8 && niz[4] == 2) {
                ispravan = false;
            } else if (niz[4] == 2 && niz[0] > 2) {
                ispravan = false;
            }
            
            if (!ispravan) {
                throw new NekorektanJMBGException();
            }
        }
        
        return ispravan;
    }
    
}
