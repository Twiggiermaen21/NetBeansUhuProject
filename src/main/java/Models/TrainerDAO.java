package Models;

import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class TrainerDAO {

    public TrainerDAO() {
    }

    // =========================================================================
    // METODY CRUD DLA TRAINERA
    // =========================================================================

    /**
     * Wstawia nowego Trenera do bazy danych (operacja DODAJ).
     *
     * @param session Sesja Hibernate.
     * @param trainer Obiekt Trenera do zapisania.
     */
    public void insertTrainer(Session session, Trainer trainer) {
        session.persist(trainer);
    }

    /**
     * Aktualizuje istniejącego Trenera w bazie danych (operacja EDYTUJ).
     * W nowszym Hibernate, persist/merge jest używane do zarządzania stanem.
     *
     * @param session Sesja Hibernate.
     * @param trainer Obiekt Trenera ze zaktualizowanymi danymi.
     */
    public void updateTrainer(Session session, Trainer trainer) {
        session.merge(trainer); // Użycie merge do upewnienia się, że obiekt jest w stanie zarządzanym
    }
    
    /**
     * Usuwa Trenera na podstawie jego ID (klucza głównego).
     *
     * @param session Sesja Hibernate.
     * @param trainerId ID Trenera do usunięcia.
     * @return true, jeśli usunięto, false, jeśli Trener nie istniał.
     */
    public boolean deleteTrainerById(Session session, String trainerId) {
        // Najpierw spróbujmy pobrać obiekt, aby sprawdzić, czy istnieje
        // W Twoim przypadku: szukamy po kluczu głównym
        Trainer trainerToDelete = session.get(Trainer.class, trainerId); 
        
        if (trainerToDelete != null) {
            session.remove(trainerToDelete); // Użycie session.remove() jest poprawne
            return true;
        }
        return false;
    }


    // =========================================================================
    // METODY WYSZUKIWANIA / WALIDACJI
    // =========================================================================

    public boolean existTrainerID(Session session, String id) {
        try {
            // Zakładamy, że 'tidNumber' to numer identyfikacyjny (DNI/ID) Trenera,
            // który musi być unikalny, ale niekoniecznie kluczem głównym.
            Query<Trainer> q = session.createQuery("SELECT t FROM Trainer t WHERE t.tidNumber = :tidNumber", Trainer.class);
            q.setParameter("tidNumber", id);
            Trainer t = q.getSingleResult();
            return t != null;
        } catch (NoResultException e) {
            return false;
        }
    }


    public Trainer returnTrainerByID(Session session, String id) {
        try {
            // W zależności od struktury Trainer (jeśli 'tidNumber' jest unikalny)
            Query<Trainer> q = session.createQuery("SELECT t FROM Trainer t WHERE t.tidNumber = :tidNumber", Trainer.class);
            q.setParameter("tidNumber", id);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    
    // Dodajemy metodę pomocniczą 'findTrainerById' (wzorowaną na dyskusji),
    // która zwraca obiekt po kluczu, aby była spójna z innymi kontrolerami:
    public Trainer findTrainerById(Session session, String trainerCod) {
        // Zakładamy, że 'trainerCod' to klucz główny Trenera, np. tCod
        return session.get(Trainer.class, trainerCod); 
    }


    public java.util.List<String> getTrainerByName(Session session, String name) {
        Query<String> query = session.createQuery(
            "SELECT t.tName FROM Trainer t WHERE t.tName = :name", String.class);
        query.setParameter("name", name);
        return query.getResultList();
    }
}