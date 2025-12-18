package Models;

import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Obiekt dostępu do danych (DAO) dla encji {@link Activity}.
 * Klasa stanowi warstwę pośredniczącą między logiką aplikacji a bazą danych. 
 * Zapewnia metody do wykonywania operacji CRUD (Create, Read, Update, Delete) 
 * oraz zapytań agregujących przy użyciu mechanizmu Hibernate Session.
 */
public class ActivityDAO {

    /** Logger do rejestrowania zdarzeń oraz błędów niskiego poziomu bazy danych. */
    private static final Logger LOGGER = Logger.getLogger(ActivityDAO.class.getName());

    /** Konstruktor bezargumentowy inicjalizujący obiekt DAO. */
    public ActivityDAO() {
    }

    // =========================================================================
    // METODY CRUD DLA AKTYWNOŚCI
    // =========================================================================

    /**
     * Utrwala nową instancję aktywności w bazie danych (operacja INSERT).
     * * @param session Aktualna sesja Hibernate.
     * @param activity Obiekt aktywności do zapisania.
     */
    public void insertActivity(Session session, Activity activity) {
        session.persist(activity);
    }

    /**
     * Aktualizuje stan istniejącej aktywności w bazie danych (operacja UPDATE).
     * * @param session Aktualna sesja Hibernate.
     * @param activity Obiekt aktywności z zaktualizowanymi danymi.
     */
    public void updateActivity(Session session, Activity activity) {
        session.merge(activity);
    }
    
    /**
     * Usuwa rekord aktywności z bazy danych na podstawie jej identyfikatora.
     * * @param session Aktualna sesja Hibernate.
     * @param activityId Unikalny identyfikator aktywności (aId).
     * @return {@code true}, jeśli operacja zakończyła się sukcesem; {@code false}, jeśli obiekt nie został znaleziony.
     */
    public boolean deleteActivityById(Session session, String activityId) {
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
    
    
    
    public boolean isTrainerOccupied(Session session, String trainerCod, String day, int hour, String currentActivityId) {
    String hql = "SELECT count(a) FROM Activity a WHERE a.atrainerInCharge.tCod = :tCod " +
                 "AND a.aDay = :aDay AND a.aHour = :aHour";
    
    // Jeśli edytujemy istniejącą aktywność, musimy ją wykluczyć z wyszukiwania
    if (currentActivityId != null) {
        hql += " AND a.aId != :currentId";
    }

    Query<Long> query = session.createQuery(hql, Long.class);
    query.setParameter("tCod", trainerCod);
    query.setParameter("aDay", day);
    query.setParameter("aHour", hour);
    
    if (currentActivityId != null) {
        query.setParameter("currentId", currentActivityId);
    }

    return query.getSingleResult() > 0;
}
    
    
    
    /**
     * Sprawdza obecność aktywności o podanym identyfikatorze w systemie.
     * * @param session Aktualna sesja Hibernate.
     * @param aId Identyfikator aktywności do sprawdzenia.
     * @return {@code true}, jeśli aktywność istnieje; {@code false} w przeciwnym razie.
     */
    public boolean existAId(Session session, String aId) {
        try {
            Query<Activity> q = session.createQuery("SELECT a FROM Activity a WHERE a.aId = :aId", Activity.class);
            q.setParameter("aId", aId);
            
            Activity a = q.getSingleResultOrNull(); 
            return a != null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Błąd podczas sprawdzania istnienia aId: " + aId, e);
            return false;
        }
    }
    
    /**
     * Pobiera z bazy danych najwyższy aktualny kod identyfikacyjny aktywności.
     * Metoda wykorzystywana do generowania sekwencyjnych kodów dla nowych rekordów.
     * * @param session Aktualna sesja Hibernate.
     * @return Ciąg znaków reprezentujący najwyższy kod (np. "AC10") lub {@code null}, jeśli tabela jest pusta.
     */
    public String getMaxActivityCode(Session session) {
        try {
            Query<String> query = session.createQuery(
                "SELECT a.aId FROM Activity a ORDER BY a.aId DESC", String.class);
            query.setMaxResults(1);
            
            return query.getSingleResultOrNull(); 
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Brak aktywności w bazie. Błąd pobierania max aId.", e);
            return null; 
        }
    }
    
    /**
     * Wyszukuje pełny obiekt aktywności na podstawie klucza głównego.
     * * @param session Aktualna sesja Hibernate.
     * @param activityId Identyfikator aktywności.
     * @return Obiekt {@link Activity} lub {@code null}, jeśli rekord nie istnieje.
     */
    public Activity findActivityById(Session session, String activityId) {
        return session.find(Activity.class, activityId);
    }

    /**
     * Pobiera kompletną listę wszystkich aktywności zarejestrowanych w systemie.
     * * @param session Aktualna sesja Hibernate.
     * @return Lista obiektów {@link Activity}.
     */
    public List<Activity> findAllActivities(Session session) {
        return session.createQuery("SELECT a FROM Activity a", Activity.class).getResultList();
    }
    
    /**
     * Pobiera dane statystyczne dotyczące uczestnictwa dla konkretnej aktywności.
     * Wykorzystuje złączenie typu LEFT JOIN, aby uwzględnić aktywności bez przypisanych klientów.
     * * @param session Aktualna sesja Hibernate.
     * @param aId Identyfikator aktywności, dla której liczone są statystyki.
     * @return Tablica obiektów, gdzie:
     * [0] to Nazwa aktywności (String),
     * [1] to Liczba przypisanych klientów (Long).
     * Zwraca {@code null} w przypadku błędu lub braku aktywności.
     */
    public Object[] getActivityStatisticsById(Session session, String aId) {
        try {
            String hql = "SELECT a.aName, COUNT(c) " +
                         "FROM Activity a " +
                         "LEFT JOIN a.clientSet c " +
                         "WHERE a.aId = :targetId " +
                         "GROUP BY a.aId, a.aName";
            
            return session.createQuery(hql, Object[].class)
                          .setParameter("targetId", aId)
                          .getSingleResultOrNull();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Błąd pobierania statystyk dla: " + aId, e);
            return null;
        }
    }
}