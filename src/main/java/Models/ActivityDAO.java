package Models;

import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) dla encji Activity.
 * Odpowiada za bezpośrednią interakcję z bazą danych dla Aktywności.
 */
public class ActivityDAO {

    private static final Logger LOGGER = Logger.getLogger(ActivityDAO.class.getName());

    public ActivityDAO() {
    }

    // =========================================================================
    // METODY CRUD DLA AKTYWNOŚCI
    // =========================================================================

    /**
     * Wstawia nową Aktywność do bazy danych (operacja DODAJ).
     */
    public void insertActivity(Session session, Activity activity) {
        session.persist(activity);
    }

    /**
     * Aktualizuje istniejącą Aktywność w bazie danych (operacja EDYTUJ).
     */
    public void updateActivity(Session session, Activity activity) {
        session.merge(activity);
    }
    
    /**
     * Usuwa Aktywność na podstawie jej ID (klucza głównego aId).
     */
    public boolean deleteActivityById(Session session, String activityId) {
        // ZMIANA: Używamy find() zamiast przestarzałego get()
        Activity activityToDelete = session.find(Activity.class, activityId); 
        
        if (activityToDelete != null) {
            session.remove(activityToDelete);
            return true;
        }
        return false;
    }

    // =========================================================================
    // METODY WYSZUKIWANIA / WALIDACJI
    // =========================================================================
    
    /**
     * Sprawdza, czy Aktywność o danym aId istnieje.
     */
    public boolean existAId(Session session, String aId) {
        try {
            Query<Activity> q = session.createQuery("SELECT a FROM Activity a WHERE a.aId = :aId", Activity.class);
            q.setParameter("aId", aId);
            
            // Użycie getSingleResultOrNull() jest czystsze niż łapanie NoResultException
            Activity a = q.getSingleResultOrNull(); 
            return a != null;
        } catch (Exception e) {
            // Logowanie ewentualnych błędów związanych z wykonaniem zapytania
            LOGGER.log(Level.SEVERE, "Błąd podczas sprawdzania istnienia aId: " + aId, e);
            return false;
        }
    }
    
    /**
     * Zwraca maksymalny kod aId Aktywności, używany do generowania kolejnych kodów.
     * @return Maksymalny kod aId (String) lub null, jeśli baza jest pusta.
     */
    public String getMaxActivityCode(Session session) {
        try {
            // Zapytanie, które pobiera tylko klucze (String) i sortuje je malejąco
            Query<String> query = session.createQuery(
                "SELECT a.aId FROM Activity a ORDER BY a.aId DESC", String.class);
            query.setMaxResults(1);
            
            // Użycie getSingleResultOrNull() jest bezpieczniejsze
            return query.getSingleResultOrNull(); 
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Brak aktywności w bazie. Błąd pobierania max aId.", e);
            return null; 
        }
    }
    
    
    /**
     * Pobiera pełny obiekt Aktywności na podstawie jej klucza głównego (aId).
     * @return Obiekt Activity lub null, jeśli nie znaleziono.
     */
    public Activity findActivityById(Session session, String activityId) {
        // ZMIANA: Używamy find() zamiast przestarzałego get()
        return session.find(Activity.class, activityId);
    }

    /**
     * Pobiera wszystkie Aktywności.
     * @return Lista wszystkich obiektów Activity.
     */
    public List<Activity> findAllActivities(Session session) {
        return session.createQuery("SELECT a FROM Activity a", Activity.class).getResultList();
    }
}