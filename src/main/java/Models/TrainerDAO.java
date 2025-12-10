package Models;

import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class TrainerDAO {

    public TrainerDAO() {
    }

    public boolean existTrainerID(Session session, String id) {
        try {
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
            Query<Trainer> q = session.createQuery("SELECT t FROM Trainer t WHERE t.tidNumber = :tidNumber", Trainer.class);
            q.setParameter("tidNumber", id);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public java.util.List<String> getTrainerByName(Session session, String name) {
        Query<String> query = session.createQuery(
            "SELECT t.tName FROM Trainer t WHERE t.tName = :name", String.class);
        query.setParameter("name", name);
        return query.getResultList();
    }
}

