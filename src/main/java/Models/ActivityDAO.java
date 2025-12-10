package Models;

import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class ActivityDAO {

    public ActivityDAO() {
    }

    public boolean existActivityID(Session session, String activityId) {
        try {
            Query<Activity> q = session.createQuery("SELECT a FROM Activity a WHERE a.aId = :idValue", Activity.class);
            q.setParameter("idValue", activityId);
            Activity a = q.getSingleResult();
            return a != null;
        } catch (NoResultException e) {
            return false;
        }
    }

    public Activity returnActivityByID(Session session, String activityId) {
        try {
            Query<Activity> q = session.createQuery("SELECT a FROM Activity a WHERE a.aId = :idValue", Activity.class);
            q.setParameter("idValue", activityId);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public java.util.List<Object[]> getActivitiesByTrainer(Session session, Trainer trainer) {
        Query<Object[]> query = session.createQuery(
            "SELECT a.aName, a.aDay, a.aPrice FROM Activity a WHERE a.atrainerInCharge = :trainer",
            Object[].class);
        query.setParameter("trainer", trainer);
        return query.getResultList();
    }

    public java.util.List<Object[]> getActivitiesByClient(Session session, Client client) {
        Query<Object[]> query = session.createQuery(
            "SELECT a.aName, a.aDay, a.aPrice FROM Activity a JOIN a.clientSet c WHERE c = :client",
            Object[].class);
        query.setParameter("client", client);
        return query.getResultList();
    }

    public java.util.List<Object[]> getActivitiesByDayAndPrice(Session session, String day, int price) {
        Query<Object[]> query = session.createQuery(
            "SELECT a.aName, a.aDay, a.aPrice FROM Activity a WHERE a.aDay = :day AND a.aPrice <= :price",
            Object[].class);
        query.setParameter("day", day);
        query.setParameter("price", price);
        return query.getResultList();
    }

    public Activity getActivityByName(Session session, String name) {
        Query<Activity> query = session.createQuery(
            "FROM Activity a WHERE a.aName = :name", Activity.class);
        query.setParameter("name", name);
        return query.uniqueResult();
    }

    public java.util.List<Object[]> getMembersByActivity(Session session, Activity activity) {
        Query<Object[]> query = session.createQuery(
            "SELECT c.mName, c.memailMember FROM Activity a JOIN a.clientSet c WHERE a = :activity",
            Object[].class);
        query.setParameter("activity", activity);
        return query.getResultList();
    }
}
