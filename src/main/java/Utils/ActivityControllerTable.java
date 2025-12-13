package Utils;

import Models.Activity;
import Models.ActivityDAO;
import Views.MainWindow;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kontroler odpowiedzialny za pobieranie i prezentację danych encji Activity
 * w głównym oknie aplikacji (MainWindow).
 */
public class ActivityControllerTable {
    
    private static final Logger LOGGER = Logger.getLogger(ActivityControllerTable.class.getName());

    private final SessionFactory sessionFactory;
    private final MainWindow view;
    private final ActivityDAO activityDAO = new ActivityDAO();

    // =========================================================================
    // KONSTRUKTOR
    // =========================================================================
    
    public ActivityControllerTable(SessionFactory sessionFactory, MainWindow view) {
        this.sessionFactory = sessionFactory;
        this.view = view;
    }

    // =========================================================================
    // POBIERANIE POJEDYNCZEJ ENCJ (DO EDYCJI)
    // =========================================================================

    /**
     * Pobiera pełny obiekt Activity na podstawie kodu zaznaczonego w tabeli.
     * Używane przez MainController przed uruchomieniem formularza edycji.
     * * @return Obiekt Activity lub null, jeśli nic nie zaznaczono lub wystąpił błąd.
     */
    public Activity getSelectedActivity() {
        String activityId = view.getSelectedActivityCode(); 
        
        if (activityId == null || activityId.trim().isEmpty()) {
            return null; // Nic nie zaznaczono
        }
        
        Session session = null;
        Activity activity = null;
        try {
            session = sessionFactory.openSession();
            // Delegowanie faktycznego pobrania obiektu do DAO
            activity = activityDAO.findActivityById(session, activityId); 
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Błąd pobierania Aktywności o ID: " + activityId, e);
            // Nie wyświetlamy błędu, tylko logujemy, ponieważ to metoda pomocnicza.
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return activity;
    }
    
    // =========================================================================
    // WYŚWIETLANIE WSZYSTKICH DANYCH (READ)
    // =========================================================================

    /**
     * Pobiera wszystkie Aktywności z bazy danych i ustawia dane w tabeli głównego widoku.
     */
    public void showActivities() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            
            // 1. Pobranie danych za pomocą HQL
            Query<Activity> query = session.createQuery("FROM Activity", Activity.class);
            List<Activity> activities = query.getResultList();
            LOGGER.info("Pobrano " + activities.size() + " aktywności.");

            // 2. Przygotowanie danych do tabeli Swing
            String[] columns = {"ID", "Nazwa", "Opis/Typ", "Cena", "Dzień", "Godzina", "Trener"};
            Object[][] data = new Object[activities.size()][7];

            for (int i = 0; i < activities.size(); i++) {
                Activity a = activities.get(i);
                data[i][0] = a.getAId();
                data[i][1] = a.getAName();
                data[i][2] = a.getADescription();
                data[i][3] = a.getAPrice();
                data[i][4] = a.getADay();
                data[i][5] = a.getAHour();
                // Pokazanie nazwy Trenera lub "N/A"
                data[i][6] = a.getAtrainerInCharge() != null ? a.getAtrainerInCharge().getTName() : "N/A";
            }

            // 3. Aktualizacja widoku
            view.setViewName("Activities");
            view.setTableData(columns, data);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd podczas wyświetlania listy Aktywności.", ex);
            JOptionPane.showMessageDialog(view, "Błąd pobierania danych: " + ex.getMessage(), "Błąd DB", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
}