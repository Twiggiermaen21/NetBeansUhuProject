package Models;

import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;

public class ActivityDAO {

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
        Activity activityToDelete = session.get(Activity.class, activityId); 
        
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
            // Używamy gotowej NamedQuery, jeśli istnieje:
            // Query<Activity> q = session.createNamedQuery("Activity.findByAId", Activity.class);
            
            // Lub używamy HQL:
            Query<Activity> q = session.createQuery("SELECT a FROM Activity a WHERE a.aId = :aId", Activity.class);
            q.setParameter("aId", aId);
            Activity a = q.getSingleResult();
            return a != null;
        } catch (NoResultException e) {
            return false;
        }
    }
    
    /**
     * Pobiera pełny obiekt Aktywności na podstawie jej klucza głównego (aId).
     */
    public Activity findActivityById(Session session, String activityId) {
        // Używamy session.get(), ponieważ aId jest kluczem głównym
        return session.get(Activity.class, activityId);
    }

    /**
     * Pobiera wszystkie Aktywności.
     */
    public List<Activity> findAllActivities(Session session) {
        return session.createQuery("SELECT a FROM Activity a", Activity.class).getResultList();
    }
}