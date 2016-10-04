package src.classes;

import src.exceptions.NekorektanJMBGException;

public class Prepravljac extends Osoba{
    
    public Prepravljac(String ime, String prezime, String jmbg) throws NekorektanJMBGException {
        super(ime, prezime, jmbg);
    }
    
    @Override
    public String toString() {
        return super.toString();
    }
    
}
