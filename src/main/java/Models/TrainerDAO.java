package Models;

import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Obiekt dostępu do danych (DAO) dla encji {@link Trainer}.
 * Klasa odpowiada za bezpośrednią interakcję z bazą danych w zakresie zarzadzania 
 * rekordami trenerów. Zapewnia metody do wykonywania operacji CRUD, pobierania 
 * danych statystycznych oraz walidacji unikalności identyfikatorów przy użyciu 
 * mechanizmów Hibernate.
 */
public class TrainerDAO {

    /** Logger do rejestrowania zdarzeń systemowych oraz błędów niskiego poziomu bazy danych. */
    private static final Logger LOGGER = Logger.getLogger(TrainerDAO.class.getName());

    /**
     * Konstruktor domyślny klasy TrainerDAO.
     */
    public TrainerDAO() {
    }

    // =========================================================================
    // METODY CRUD DLA TRAINERA
    // =========================================================================

    /**
     * Wstawia nową encję trenera do bazy danych (operacja INSERT).
     * @param session Aktualna sesja Hibernate.
     * @param trainer Obiekt trenera do utrwalenia.
     */
    public void insertTrainer(Session session, Trainer trainer) {
        session.persist(trainer);
    }

    /**
     * Aktualizuje dane istniejącego trenera w bazie danych (operacja UPDATE).
     * @param session Aktualna sesja Hibernate.
     * @param trainer Obiekt trenera z zaktualizowanymi informacjami.
     */
    public void updateTrainer(Session session, Trainer trainer) {
        session.merge(trainer);
    }
    
    /**
     * Usuwa rekord trenera z bazy danych na podstawie klucza głównego (tCod).
     * @param session Aktualna sesja Hibernate.
     * @param trainerId Kod identyfikacyjny trenera (tCod).
     * @return {@code true}, jeśli usunięcie powiodło się; {@code false}, jeśli trener nie został znaleziony.
     */
    public boolean deleteTrainerById(Session session, String trainerId) {
        Trainer trainerToDelete = session.find(Trainer.class, trainerId); 
        
        if (trainerToDelete != null) {
            session.remove(trainerToDelete);
            return true;
        }
        return false;
    }

    // =========================================================================
    // METODY WYSZUKIWANIA / WALIDACJI
    // =========================================================================

    /**
     * Weryfikuje, czy w bazie danych istnieje trener o podanym numerze identyfikacyjnym (tidNumber).
     * @param session Aktualna sesja Hibernate.
     * @param id Numer identyfikacyjny (np. DNI/PESEL) do sprawdzenia.
     * @return {@code true}, jeśli rekord o podanym ID istnieje; {@code false} w przeciwnym razie.
     */
    public boolean existTrainerID(Session session, String id) {
        try {
            Query<Trainer> q = session.createQuery("SELECT t FROM Trainer t WHERE t.tidNumber = :tidNumber", Trainer.class);
            q.setParameter("tidNumber", id);
            
            Trainer result = q.getSingleResultOrNull(); 
            return result != null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Błąd podczas sprawdzania istnienia ID Trenera: " + id, e);
            return false;
        }
    }

    /**
     * Pobiera pełny obiekt trenera na podstawie jego klucza głównego (tCod).
     * @param session Aktualna sesja Hibernate.
     * @param trainerCod Kod trenera (PK).
     * @return Obiekt {@link Trainer} lub {@code null}, jeśli nie znaleziono rekordu.
     */
    public Trainer getTrainerByCod(Session session, String trainerCod) {
        return session.find(Trainer.class, trainerCod); 
    }
    
    /**
     * Wyszukuje trenera na podstawie jego unikalnego numeru identyfikacyjnego (tidNumber).
     * @param session Aktualna sesja Hibernate.
     * @param id Numer identyfikacyjny trenera.
     * @return Obiekt {@link Trainer} lub {@code null}, jeśli rekord nie istnieje.
     */
    public Trainer returnTrainerByID(Session session, String id) {
        try {
            Query<Trainer> q = session.createQuery("SELECT t FROM Trainer t WHERE t.tidNumber = :tidNumber", Trainer.class);
            q.setParameter("tidNumber", id);
            return q.getSingleResultOrNull();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Błąd pobierania Trenera po ID: " + id, e);
            return null;
        }
    }
    
    // =========================================================================
    // METODY POMOCNICZE
    // =========================================================================

    /**
     * Pobiera z bazy danych najwyższy aktualny kod identyfikacyjny trenera (tCod).
     * Metoda wykorzystywana przez kontrolery do generowania automatycznych sekwencji kodów.
     * @param session Aktualna sesja Hibernate.
     * @return String reprezentujący maksymalny kod (np. "T010") lub {@code null}, jeśli baza jest pusta.
     */
    public String getMaxTrainerCode(Session session) {
        try {
            Query<String> query = session.createQuery(
                "SELECT t.tCod FROM Trainer t ORDER BY t.tCod DESC", String.class);
            query.setMaxResults(1);
            
            return query.getSingleResultOrNull();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Brak trenerów w bazie. Błąd pobierania max tCod.", e);
            return null; 
        }
    }

    /**
     * Wyszukuje imiona trenerów na podstawie podanego ciągu znaków.
     * @param session Aktualna sesja Hibernate.
     * @param name Imię i nazwisko trenera do wyszukania.
     * @return Lista nazw (String) pasujących do kryterium wyszukiwania.
     */
    public List<String> getTrainerByName(Session session, String name) {
        Query<String> query = session.createQuery(
            "SELECT t.tName FROM Trainer t WHERE t.tName = :name", String.class);
        query.setParameter("name", name);
        return query.getResultList();
    }
    
    
    /**
 * Pobiera listę wszystkich trenerów z bazy danych.
 * @param session Aktualna sesja Hibernate.
 * @return Lista obiektów {@link Trainer}.
 */
public List<Trainer> findAllTrainers(Session session) {
    try {
        Query<Trainer> query = session.createQuery("FROM Trainer", Trainer.class);
        return query.getResultList();
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Błąd podczas pobierania listy wszystkich trenerów", e);
        return java.util.Collections.emptyList();
    }
}
    
    
    
}