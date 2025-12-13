package Models;

import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object (DAO) dla encji Trainer.
 * Odpowiada za bezpośrednią interakcję z bazą danych dla Trenerów.
 */
public class TrainerDAO {

    private static final Logger LOGGER = Logger.getLogger(TrainerDAO.class.getName());

    public TrainerDAO() {
    }

    // =========================================================================
    // METODY CRUD DLA TRAINERA
    // =========================================================================

    /**
     * Wstawia nowego Trenera do bazy danych.
     */
    public void insertTrainer(Session session, Trainer trainer) {
        session.persist(trainer);
    }

    /**
     * Aktualizuje istniejącego Trenera.
     */
    public void updateTrainer(Session session, Trainer trainer) {
        session.merge(trainer);
    }
    
    /**
     * Usuwa Trenera na podstawie klucza głównego (tCod).
     * @return true, jeśli usunięto, false, jeśli Trener nie istnieje.
     */
    public boolean deleteTrainerById(Session session, String trainerId) {
        // ZMIANA: Używamy find() zamiast przestarzałego get()
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
     * Sprawdza, czy numer identyfikacyjny Trenera (tidNumber) już istnieje.
     * @return true, jeśli rekord istnieje, false w przeciwnym razie.
     */
    public boolean existTrainerID(Session session, String id) {
        try {
            // Szukamy po unikalnym numerze ID (TidNumber)
            Query<Trainer> q = session.createQuery("SELECT t FROM Trainer t WHERE t.tidNumber = :tidNumber", Trainer.class);
            q.setParameter("tidNumber", id);
            
            // ZMIANA: Użycie getSingleResultOrNull() jest czystsze
            Trainer result = q.getSingleResultOrNull(); 
            return result != null;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Błąd podczas sprawdzania istnienia ID Trenera: " + id, e);
            return false;
        }
    }

    /**
     * Zwraca obiekt Trenera na podstawie klucza głównego (tCod).
     *
     * @param trainerCod Kod Trenera (klucz główny tCod).
     * @return Obiekt Trenera lub null, jeśli nie znaleziono.
     */
    public Trainer getTrainerByCod(Session session, String trainerCod) {
        // ZMIANA: Używamy find() zamiast przestarzałego get()
        return session.find(Trainer.class, trainerCod); 
    }
    
    /**
     * Pobiera Trenera po unikalnym Numerze ID (TidNumber).
     * * @return Obiekt Trenera lub null, jeśli nie znaleziono.
     */
    public Trainer returnTrainerByID(Session session, String id) {
        try {
            Query<Trainer> q = session.createQuery("SELECT t FROM Trainer t WHERE t.tidNumber = :tidNumber", Trainer.class);
            q.setParameter("tidNumber", id);
            // ZMIANA: Użycie getSingleResultOrNull() jest czystsze
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
     * Pobiera największy istniejący kod Trenera (tCod), używany do generowania
     * nowego, unikalnego kodu.
     * @return Maksymalny kod Trenera (String) lub null, jeśli baza jest pusta.
     */
    public String getMaxTrainerCode(Session session) {
        try {
            Query<String> query = session.createQuery(
                "SELECT t.tCod FROM Trainer t ORDER BY t.tCod DESC", String.class);
            query.setMaxResults(1);
            
            // ZMIANA: Użycie getSingleResultOrNull() jest bezpieczniejsze
            return query.getSingleResultOrNull();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Brak trenerów w bazie. Błąd pobierania max tCod.", e);
            return null; 
        }
    }

    /**
     * Wyszukuje listę nazw Trenerów.
     * UWAGA: Zwracanie listy stringów (nazw) jest rzadko spotykane w DAO. Zazwyczaj
     * zwraca się pełne obiekty Trainer. Metoda zostaje zachowana w oryginalnej formie.
     */
    public List<String> getTrainerByName(Session session, String name) {
        Query<String> query = session.createQuery(
            "SELECT t.tName FROM Trainer t WHERE t.tName = :name", String.class);
        query.setParameter("name", name);
        return query.getResultList();
    }
}