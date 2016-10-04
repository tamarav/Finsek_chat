package src.classes;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import src.exceptions.NekorektanJMBGException;
import src.izvjestaji.Izvjestaj;
import src.izvjestaji.KreiranjeIzvjestaja;

public class Nadzornik extends Osoba implements KreiranjeIzvjestaja {
    
    public Nadzornik(String ime, String prezime, String jmbg) throws NekorektanJMBGException {
        super(ime, prezime, jmbg);
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
    
    //metoda koja kreira novi izvjestaj i cuva ga na serveru u folderu izvjestaji (kao argument prima ko kreira izvjestaj)
    @Override
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
                
                FileOutputStream fos = new FileOutputStream(("server/izvjestaji/" + "nadzornik " + "-" + naziv), true);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(izvjestaj);
                oos.close();
                fos.close();
                
            } else {
                
                FileOutputStream fos = new FileOutputStream("server/izvjestaji/" + "agencija - " + naziv);
                ObjectOutputStream upisObjekta = new ObjectOutputStream(fos);
                upisObjekta.writeObject(izvjestaj);
                upisObjekta.close();
                fos.close();
                
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
