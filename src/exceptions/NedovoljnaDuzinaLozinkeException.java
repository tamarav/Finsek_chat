package src.exceptions;

public class NedovoljnaDuzinaLozinkeException extends Exception {

 private static final long serialVersionUID = 1L;

 public String toString() {
  return "Nedovoljna duzina lozinke! Lozinka mora imati najmanje 3 karaktera.";
 }
 
}
