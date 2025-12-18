package ViewsCMD;

import java.util.Set;
import Models.Activity;

/**
 * Klasa widoku odpowiedzialna za prezentację danych dotyczących aktywności w konsoli.
 * Zapewnia metody do wyświetlania sformatowanych list zajęć przypisanych do trenerów
 * oraz ogólnych zestawień aktywności pobranych z bazy danych.
 */
public class ActivityView {

    /**
     * Wyświetla w konsoli listę aktywności przypisanych do konkretnego trenera.
     * Dane są prezentowane w formie tabelarycznej z nagłówkami: Nazwa, Dzień, Godzina i Trener.
     * * @param activities Zbiór obiektów {@link Activity} powiązanych z danym trenerem.
     */
    public void showTrainerActivities(Set<Activity> activities) {
        System.out.println("Name" + "\t" + "Day" + "\t" + "Hour" + "\t" + "Trainer");
        for (Activity a : activities) {
            System.out.println(a.getAName() + "\t" + a.getADay() + "\t" + a.getAHour()
                    + "\t" + a.getAtrainerInCharge().getTName());
        }
    }

    /**
     * Wyświetla uproszczoną listę aktywności na podstawie surowych danych z zapytania HQL/SQL.
     * Metoda obsługuje przypadki, w których lista jest pusta, informując o braku wyników.
     * Dane są prezentowane w formacie: Nazwa, Dzień, Cena.
     * * @param activities Lista tablic obiektów, gdzie:
     * [0] - Nazwa aktywności (String),
     * [1] - Dzień tygodnia (String),
     * [2] - Cena (Integer/Double).
     */
    public void showActivityList(java.util.List<Object[]> activities) {
        if (activities.isEmpty()) {
            System.out.println("No activities found.");
            return;
        }
        System.out.println("Name\tDay\tPrice");
        for (Object[] row : activities) {
            System.out.println(row[0] + "\t" + row[1] + "\t" + row[2]);
        }
    }
}