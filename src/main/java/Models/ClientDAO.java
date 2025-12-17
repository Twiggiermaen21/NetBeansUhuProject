package Models;

import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;

public class ClientDAO {

    public ClientDAO() {
    }

    public boolean existMemberNumber(Session session, String memberNum) {
        try {
            Query<Client> q = session.createQuery("SELECT c FROM Client c WHERE c.mNum = :mNumValue", Client.class);
            q.setParameter("mNumValue", memberNum);
            Client c = q.getSingleResult();
            return c != null;
        } catch (NoResultException e) {
            return false;
        }
    }

    public boolean existDNI(Session session, String dni) {
        try {
            Query<Client> q = session.createQuery("SELECT c FROM Client c WHERE c.mId = :idValue", Client.class);
            q.setParameter("idValue", dni);
            Client c = q.getSingleResult();
            return c != null;
        } catch (NoResultException e) {
            return false;
        }
    }

    public void insertClient(Session session, Client client) throws Exception {
        session.persist(client);
    }

    public Client returnClientByMemberNumber(Session session, String memberNum) {
        try {
            Query<Client> q = session.createQuery("SELECT c FROM Client c WHERE c.mNum = :mNumValue", Client.class);
            q.setParameter("mNumValue", memberNum);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public java.util.List<Client> getAllClients(Session session) {
        Query<Client> query = session.createQuery("FROM Client", Client.class);
        return query.getResultList();
    }

    public void deleteClient(Session session, Client client) {
        session.remove(client);
    }

    public java.util.List<Object[]> getClientsByCategory(Session session, char category) {
        Query<Object[]> query = session.createQuery(
            "SELECT c.mName, c.mcategoryMember FROM Client c WHERE c.mcategoryMember = :cat", Object[].class);
        query.setParameter("cat", category);
        return query.getResultList();
    }

    public java.util.List<Object[]> getNameAndPhone(Session session) {
        Query<Object[]> query = session.createQuery(
            "SELECT c.mName, c.mPhone FROM Client c", Object[].class);
        return query.getResultList();
    }

    public Client getClientByName(Session session, String name) {
        Query<Client> query = session.createQuery("FROM Client c WHERE c.mName = :name", Client.class);
        query.setParameter("name", name);
        return query.uniqueResult();
    }

    public java.util.List<Client> getClientsByCategoryNamed(Session session, char category) {
        Query<Client> query = session.createNamedQuery("Client.findByMcategoryMember", Client.class);
        query.setParameter("mcategoryMember", category);
        return query.getResultList();
    }

    public Client getClientById(Session session, String id) {
        Query<Client> query = session.createQuery("FROM Client c WHERE c.mId = :id", Client.class);
        query.setParameter("id", id);
        return query.uniqueResult();
    }
    
    public String getMaxMemberNumber(Session session) {
        
        String hql = "SELECT MAX(c.mNum) FROM Client c"; 
        
        Query<String> query = session.createQuery(hql, String.class);
        return query.uniqueResult(); 
        
    }
    
    public void updateClient(Session session, Client client) throws Exception {
        session.merge(client); // session.merge() jest zazwyczaj bezpieczniejsze
    }
    public boolean deleteClientByMemberNumber(Session session, String mNum) {
        Client clientToDelete = returnClientByMemberNumber(session, mNum);
        
        if (clientToDelete != null) {
            deleteClient(session, clientToDelete); // Używa metody deleteClient
            return true;
        }
        return false;
    }
    public Object[] getGlobalStatistics(Session session) {
    // 1. Liczba klientów
    Long totalClients = session.createQuery("SELECT COUNT(c) FROM Client c", Long.class).getSingleResult();

    // 2. Pobranie wszystkich dat urodzenia do obliczenia średniego wieku
    java.util.List<String> birthdates = session.createQuery("SELECT c.mBirthdate FROM Client c", String.class).getResultList();

    // 3. Pobranie wszystkich klientów z ich kategoriami i cenami aktywności (do przychodu)
    // Zakładamy relację c.activitySet
    String hql = "SELECT c.mcategoryMember, a.aPrice FROM Client c JOIN c.activitySet a";
    java.util.List<Object[]> categoryRevenueData = session.createQuery(hql, Object[].class).getResultList();

    return new Object[]{totalClients, birthdates, categoryRevenueData};
}
    
    public Object[] getStatisticsForActivity(Session session, String aId) {
    // 1. Liczba klientów zapisanych na tę konkretną aktywność
    String hqlCount = "SELECT COUNT(c) FROM Client c JOIN c.activitySet a WHERE a.aId = :id";
    Long totalClients = session.createQuery(hqlCount, Long.class)
                               .setParameter("id", aId)
                               .getSingleResult();

    // 2. Daty urodzenia klientów zapisanych na tę aktywność
    String hqlBirth = "SELECT c.mBirthdate FROM Client c JOIN c.activitySet a WHERE a.aId = :id";
    java.util.List<String> birthdates = session.createQuery(hqlBirth, String.class)
                                               .setParameter("id", aId)
                                               .getResultList();

    // 3. Kategorie i cena tej konkretnej aktywności (dla przychodu)
    String hqlRev = "SELECT c.mcategoryMember, a.aPrice FROM Client c JOIN c.activitySet a WHERE a.aId = :id";
    java.util.List<Object[]> revenueData = session.createQuery(hqlRev, Object[].class)
                                                  .setParameter("id", aId)
                                                  .getResultList();

    return new Object[]{totalClients, birthdates, revenueData};
}
    
}