package ControllersCMD;

import ViewsCMD.MessageView;
import ViewsCMD.ActivityView;
import Models.Trainer;
import Models.TrainerDAO;
import Models.Activity;
import java.util.Scanner;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import Views.*;

/**
 * Kontroler odpowiedzialny za obsługę operacji związanych z trenerami.
 * Klasa zarządza logiką menu trenera, umożliwiając m.in. przeglądanie 
 * aktywności przypisanych do konkretnego instruktora na podstawie jego identyfikatora.
 */
public class TrainerController {
    
    /** Fabryka sesji Hibernate używana do komunikacji z bazą danych. */
    private SessionFactory sessionFactory = null;
    
    /** Obiekt dostępu do danych (DAO) dla encji Trainer. */
    private TrainerDAO tDAO = null;
    
    /** Widok odpowiedzialny za prezentację danych dotyczących aktywności. */
    private ActivityView vActivity = null;
    
    /** Widok odpowiedzialny za wyświetlanie komunikatów systemowych i menu. */
    private MessageView vMessages = null;

    /**
     * Konstruktor klasy TrainerController.
     * Inicjalizuje wymagane komponenty DAO oraz widoków, a następnie 
     * uruchamia menu konsolowe dla modułu trenera.
     * * @param sessionFactory Fabryka sesji przekazana z głównego kontrolera aplikacji.
     */
    public TrainerController(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.tDAO = new TrainerDAO();
        this.vMessages = new MessageView();
        this.vActivity = new ActivityView();
        this.menu();
    }

    /**
     * Obsługuje menu konsolowe modułu trenera.
     * Pobiera wybór użytkownika i wywołuje odpowiednie metody biznesowe lub zamyka moduł.
     */
    private void menu() {
        Scanner keyboard = new Scanner(System.in);
        String option;
        do {
            vMessages.trainerMenu();
            option = keyboard.nextLine();
            switch (option) {
                case "1" -> ActivitiesByTrainerID();
                case "2" -> vMessages.consoleMessage("INFO", "Exiting trainer menu...");
                default -> vMessages.consoleMessage("WARNING", "Invalid option. Try again.");
            }
        } while (!option.equals("2"));
    }

    /**
     * Pobiera od użytkownika identyfikator trenera i wyświetla listę przypisanych do niego aktywności.
     * Metoda zarządza transakcją Hibernate, weryfikuje istnienie trenera w bazie danych
     * oraz obsługuje pobieranie powiązanego zestawu aktywności (One-to-Many).
     */
    private void ActivitiesByTrainerID() {
        Session session = null;
        Transaction tr = null;
        Scanner keyboard = new Scanner(System.in);
        try {
            session = sessionFactory.openSession();
            tr = session.beginTransaction();

            System.out.print("Write ID of trainer in charge: ");
            String idTrainer = keyboard.nextLine();

            // Weryfikacja istnienia ID trenera
            Boolean checkID = tDAO.existTrainerID(session, idTrainer);
            if (!checkID) {
                vMessages.consoleMessage("ERROR", "ID is not in the database");
                return;
            }

            // Pobranie obiektu trenera i powiązanych z nim aktywności
            Trainer t = tDAO.returnTrainerByID(session, idTrainer);
            Set<Activity> acts = t.getActivitySet();
            
            // Przekazanie danych do widoku
            vActivity.showTrainerActivities(acts);

            tr.commit();
        } catch (Exception e) {
            // Wycofanie transakcji w przypadku błędu
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            vMessages.consoleMessage("ERROR", "An error occurred: " + e.getMessage());
        } finally {
            // Zamknięcie sesji Hibernate
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}