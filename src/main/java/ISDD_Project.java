import Config.HibernateUtil;
import Controllers.ConnectionController;

/**
 * Główna klasa projektu ISDD_Project.
 * Odpowiada za zainicjowanie głównego kontrolera aplikacji oraz 
 * prawidłowe zamknięcie sesji Hibernate przy zakończeniu pracy programu.
 * * @author Kacper Pudełko
 * @version 1.0
 */
public class ISDD_Project {

    /**
     * Punkt wejścia do aplikacji.
     * Metoda tworzy instancję {@link ConnectionController}, która zarządza połączeniem,
     * a następnie zamyka fabrykę sesji za pomocą {@link HibernateUtil#close()}.
     * * @param args Argumenty wiersza poleceń (nieużywane).
     */
    public static void main(String[] args) {
        new ConnectionController();
        HibernateUtil.close();
    }
}
