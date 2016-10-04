package src.classes;

import java.util.Random;

import src.exceptions.NekorektanJMBGException;

public class Radnik extends Osoba {
    
    private int plata;
    private String username, password;
    
    public Radnik(String ime, String prezime, String jmbg, String username, String password) throws NekorektanJMBGException {
       
        super(ime, prezime, jmbg);
        
        this.username = username;
        this.password = password;
        
        Random generator = new Random();
        this.plata = generator.nextInt(3550) + 1000;
        
    }
    
    public int getPlata() {
        return plata;
    }
    
    public void setPlata(int plata) {
        this.plata = plata;
    }
    
    public void smanjiPlatu() {
        this.plata -= 100;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    @Override
    public String toString() {
        return "Radnik [username[" + this.getJmbg() + "]=" + username + ", password=" + password + "]";
    }
    
}
