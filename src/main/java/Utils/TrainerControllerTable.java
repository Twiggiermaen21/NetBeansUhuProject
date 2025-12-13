package Utils;

import Models.Trainer;
import Models.TrainerDAO;
import Views.MainWindow;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kontroler odpowiedzialny za pobieranie i prezentację danych encji Trainer
 * w głównym oknie aplikacji (MainWindow).
 */
public class TrainerControllerTable {

    private static final Logger LOGGER = Logger.getLogger(TrainerControllerTable.class.getName());

    private final SessionFactory sessionFactory;
    private final MainWindow view;
    private final TrainerDAO trainerDAO = new TrainerDAO(); 

    // Ujednolicone nazwy kolumn dla widoku
    private static final String[] COLUMN_NAMES = {"Kod", "Imię i Nazwisko", "ID (Numer)", "Telefon", "E-mail", "Data zatrudnienia", "Nick"};

    public TrainerControllerTable(SessionFactory sessionFactory, MainWindow view) {
        this.sessionFactory = sessionFactory;
        this.view = view;
    }

    // =========================================================================
    // POBIERANIE POJEDYNCZEJ ENCJ (DO EDYCJI)
    // =========================================================================

    /**
     * Pobiera pełny obiekt Trainer na podstawie kodu zaznaczonego w tabeli.
     * Używane przez MainController przed uruchomieniem formularza edycji.
     * * @return Obiekt Trainer lub null, jeśli nic nie zaznaczono lub wystąpił błąd.
     */
    public Trainer getSelectedTrainer() {
        // Zwraca KOD Trenera (tCod) z kolumny 0
        String trainerCod = view.getSelectedTrainerCode(); 

        if (trainerCod == null || trainerCod.trim().isEmpty()) {
            return null;
        }

        Session session = null;
        Trainer trainer = null;
        try {
            session = sessionFactory.openSession();
            // Wyszukiwanie obiektu po kluczu głównym (tCod)
            trainer = trainerDAO.getTrainerByCod(session, trainerCod); 
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Błąd pobierania Trenera o kodzie: " + trainerCod, e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return trainer;
    }

    // =========================================================================
    // WYŚWIETLANIE WSZYSTKICH DANYCH (READ)
    // =========================================================================

    /**
     * Pobiera wszystkich Trenerów z bazy danych i ustawia dane w tabeli głównego widoku.
     */
    public void showTrainers() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            // Zapytanie HQL pobierające wszystkich Trenerów
            Query<Trainer> query = session.createQuery("FROM Trainer", Trainer.class);
            List<Trainer> trainers = query.getResultList();
            LOGGER.info("Pobrano " + trainers.size() + " trenerów.");

            String[] columns = COLUMN_NAMES; 
            Object[][] data = new Object[trainers.size()][7];

            // Mapowanie danych do dwuwymiarowej tablicy
            for (int i = 0; i < trainers.size(); i++) {
                Trainer t = trainers.get(i);
                data[i][0] = t.getTCod();
                data[i][1] = t.getTName();
                data[i][2] = t.getTidNumber();
                data[i][3] = t.getTphoneNumber();
                data[i][4] = t.getTEmail();
                data[i][5] = t.getTDate();
                data[i][6] = t.getTNick();
            }

            // Aktualizacja widoku
            view.setViewName("Trainers");
            view.setTableData(columns, data);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd podczas wyświetlania listy Trenerów.", ex);
            JOptionPane.showMessageDialog(view, "Błąd pobierania danych: " + ex.getMessage(), "Błąd DB", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}