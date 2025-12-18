package ViewsCMD;

import Models.Client;

/**
 * Klasa widoku odpowiedzialna za tekstową prezentację danych klientów w konsoli.
 * Zapewnia metody do wyświetlania szczegółowych informacji o pojedynczym członku
 * oraz listowania wszystkich klientów zarejestrowanych w systemie.
 */
public class ClientView {

    /**
     * Wyświetla szczegółowe dane dotyczące konkretnego klienta.
     * Metoda wypisuje wszystkie kluczowe atrybuty obiektu {@link Client}, takie jak:
     * numer członkowski, imię, identyfikator, telefon, e-mail, datę rozpoczęcia 
     * członkostwa oraz kategorię.
     * * @param c Obiekt klienta, którego dane mają zostać wyświetlone.
     */
    public void showClientDetails(Client c) {
        System.out.println("New member:");
        System.out.println("Num: " + c.getMNum() + " Name: " + c.getMName() + " ID: " + c.getMId()
                + " Phone: " + c.getMPhone() + " Email: " + c.getMemailMember()
                + " Start: " + c.getMstartingDateMember() + " Category: " + c.getMcategoryMember());
    }

    /**
     * Wyświetla sformatowaną listę wszystkich klientów przekazanych w parametrze.
     * W przypadku pustej listy informuje użytkownika o braku rekordów.
     * Każdy klient na liście jest oddzielony separatorem graficznym dla lepszej czytelności.
     * * @param clients Lista obiektów {@link Client} pobrana z bazy danych.
     */
    public void showClientList(java.util.List<Client> clients) {
        if (clients.isEmpty()) {
            System.out.println("No clients found.");
            return;
        }
        System.out.println("===== Client List =====");
        for (Client c : clients) {
            showClientDetails(c);
            System.out.println("-------------------------");
        }
    }
}