package src.izvjestaji;

import java.io.Serializable;

public class Izvjestaj<T> implements Serializable {

    private String datum;
    private String sadrzaj;
    private T generickiPrilog;
    private String naziv;
    
    public Izvjestaj() {
    }
    
    public Izvjestaj(String naziv, String datum, String sadrzaj, T generickiPrilog) {
        this.naziv = naziv;
        this.datum = datum;
        this.sadrzaj = sadrzaj;
        this.generickiPrilog = generickiPrilog;
    }
    
    public void setDate(String datum) {
        this.datum = datum;
    }
    
    public String getDate() {
        return datum;
    }
    
    public void setSadrzaj(String sadrzaj) {
        this.sadrzaj = sadrzaj;
    }
    
    public String getSadrzaj() {
        return sadrzaj;
    }
    
    public void setGenerickiPrilog(T generickiPrilog) {
        this.generickiPrilog = generickiPrilog;
    }
    
    public T getGenerickiPrilog() {
        return generickiPrilog;
    }
    
    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }
    
    public String getNaziv() {
        return naziv;
    }
}
