package ViewsCMD;

import Models.Trainer;
import java.util.Set;

/**
 * Klasa widoku odpowiedzialna za tekstową prezentację danych trenerów w konsoli.
 * Zapewnia metody do wyświetlania szczegółowych informacji o pojedynczym trenerze
 * oraz listowania zbiorów trenerów zarejestrowanych w systemie.
 */
public class TrainerView {

    /**
     * Wyświetla w konsoli szczegółowe informacje o konkretnym trenerze.
     * Metoda korzysta z implementacji toString() klasy {@link Trainer}.
     * W przypadku przesłania pustego obiektu wyświetla stosowny komunikat informacyjny.
     * * @param t Obiekt trenera, którego dane mają zostać wyświetlone.
     */
    public void showTrainerDetails(Trainer t) {
        if (t == null) {
            System.out.println("No trainer to display.");
            return;
        }
        System.out.println("===== Trainer details =====");
        System.out.println(t.toString());
    }

    /**
     * Wyświetla sformatowaną listę trenerów przekazaną w formie zbioru.
     * Metoda sprawdza, czy zbiór nie jest pusty lub niezainicjalizowany.
     * Każdy element zbioru jest wypisywany w nowej linii przy użyciu metody toString().
     * * @param trainers Zbiór obiektów {@link Trainer} pobrany z bazy danych.
     */
    public void showTrainerList(Set<Trainer> trainers) {
        if (trainers == null || trainers.isEmpty()) {
            System.out.println("No trainers available.");
            return;
        }
        System.out.println("===== Trainers list =====");
        for (Trainer t : trainers) {
            if (t == null) continue;
            System.out.println(t.toString());
        }
    }
}