package Models;

import jakarta.persistence.NoResultException;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;

/**
 * Obiekt dostępu do danych (DAO) dla encji {@link Client}.
 * Klasa udostępnia zestaw metod do zarządzania rekordami klientów w bazie danych,
 * w tym operacje CRUD, zaawansowane wyszukiwanie według kategorii oraz
 * generowanie surowych danych statystycznych.
 */
public class ClientDAO {

    /**
     * Konstruktor domyślny klasy ClientDAO.
     */
    public ClientDAO() {
    }

    /**
     * Sprawdza, czy w bazie danych istnieje klient o podanym numerze członkowskim.
     * @param session Aktualna sesja Hibernate.
     * @param memberNum Numer członkowski do weryfikacji.
     * @return true, jeśli numer istnieje; false w przeciwnym razie.
     */
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

    /**
     * Sprawdza, czy w bazie danych istnieje klient o podanym numerze identyfikacyjnym (DNI/PESEL).
     * @param session Aktualna sesja Hibernate.
     * @param dni Numer identyfikacyjny do weryfikacji.
     * @return true, jeśli DNI istnieje; false w przeciwnym razie.
     */
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

    /**
     * Wstawia nową encję klienta do bazy danych.
     * @param session Aktualna sesja Hibernate.
     * @param client Obiekt klienta do utrwalenia.
     * @throws Exception Rzuca wyjątek w przypadku błędu persistencji.
     */
    public void insertClient(Session session, Client client) throws Exception {
        session.persist(client);
    }

    /**
     * Pobiera obiekt klienta na podstawie unikalnego numeru członkowskiego.
     * @param session Aktualna sesja Hibernate.
     * @param memberNum Numer członkowski.
     * @return Obiekt {@link Client} lub null, jeśli nie znaleziono.
     */
    public Client returnClientByMemberNumber(Session session, String memberNum) {
        try {
            Query<Client> q = session.createQuery("SELECT c FROM Client c WHERE c.mNum = :mNumValue", Client.class);
            q.setParameter("mNumValue", memberNum);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Pobiera listę wszystkich klientów zarejestrowanych w systemie.
     * @param session Aktualna sesja Hibernate.
     * @return Lista wszystkich obiektów {@link Client}.
     */
    public java.util.List<Client> getAllClients(Session session) {
        Query<Client> query = session.createQuery("FROM Client", Client.class);
        return query.getResultList();
    }

    /**
     * Usuwa rekord klienta z bazy danych.
     * @param session Aktualna sesja Hibernate.
     * @param client Obiekt klienta do usunięcia.
     */
    public void deleteClient(Session session, Client client) {
        session.remove(client);
    }

    /**
     * Pobiera imiona i kategorie klientów należących do określonej grupy.
     * @param session Aktualna sesja Hibernate.
     * @param category Znak reprezentujący kategorię (np. 'A', 'B').
     * @return Lista tablic obiektów [Imię, Kategoria].
     */
    public java.util.List<Object[]> getClientsByCategory(Session session, char category) {
        Query<Object[]> query = session.createQuery(
            "SELECT c.mName, c.mcategoryMember FROM Client c WHERE c.mcategoryMember = :cat", Object[].class);
        query.setParameter("cat", category);
        return query.getResultList();
    }

    /**
     * Pobiera listę imion oraz numerów telefonów wszystkich klientów.
     * @param session Aktualna sesja Hibernate.
     * @return Lista tablic obiektów [Imię, Telefon].
     */
    public java.util.List<Object[]> getNameAndPhone(Session session) {
        Query<Object[]> query = session.createQuery(
            "SELECT c.mName, c.mPhone FROM Client c", Object[].class);
        return query.getResultList();
    }

    /**
     * Wyszukuje klienta na podstawie dokładnego imienia i nazwiska.
     * @param session Aktualna sesja Hibernate.
     * @param name Imię i nazwisko klienta.
     * @return Obiekt {@link Client} lub null.
     */
    public Client getClientByName(Session session, String name) {
        Query<Client> query = session.createQuery("FROM Client c WHERE c.mName = :name", Client.class);
        query.setParameter("name", name);
        return query.uniqueResult();
    }

    /**
     * Pobiera pełne obiekty klientów danej kategorii przy użyciu zdefiniowanego NamedQuery.
     * @param session Aktualna sesja Hibernate.
     * @param category Znak kategorii.
     * @return Lista obiektów {@link Client}.
     */
    public java.util.List<Client> getClientsByCategoryNamed(Session session, char category) {
        Query<Client> query = session.createNamedQuery("Client.findByMcategoryMember", Client.class);
        query.setParameter("mcategoryMember", category);
        return query.getResultList();
    }

    /**
     * Pobiera klienta na podstawie numeru identyfikacyjnego (mId).
     * @param session Aktualna sesja Hibernate.
     * @param id Numer ID.
     * @return Obiekt {@link Client} lub null.
     */
    public Client getClientById(Session session, String id) {
        Query<Client> query = session.createQuery("FROM Client c WHERE c.mId = :id", Client.class);
        query.setParameter("id", id);
        return query.uniqueResult();
    }
    
    /**
     * Zwraca najwyższą wartość numeru członkowskiego (mNum) obecną w bazie danych.
     * Metoda używana do obliczania kolejnych wolnych numerów członkowskich.
     * @param session Aktualna sesja Hibernate.
     * @return String reprezentujący najwyższy numer członkowski.
     */
    public String getMaxMemberNumber(Session session) {
        String hql = "SELECT MAX(c.mNum) FROM Client c"; 
        Query<String> query = session.createQuery(hql, String.class);
        return query.uniqueResult(); 
    }
    
    /**
     * Aktualizuje dane istniejącego klienta przy użyciu operacji merge.
     * @param session Aktualna sesja Hibernate.
     * @param client Obiekt klienta z nowymi danymi.
     * @throws Exception Rzuca wyjątek w przypadku błędu aktualizacji.
     */
    public void updateClient(Session session, Client client) throws Exception {
        session.merge(client);
    }

    /**
     * Usuwa klienta na podstawie jego numeru członkowskiego.
     * @param session Aktualna sesja Hibernate.
     * @param mNum Numer członkowski klienta do usunięcia.
     * @return true, jeśli usunięcie powiodło się; false, jeśli klient nie istnieje.
     */
    public boolean deleteClientByMemberNumber(Session session, String mNum) {
        Client clientToDelete = returnClientByMemberNumber(session, mNum);
        if (clientToDelete != null) {
            deleteClient(session, clientToDelete);
            return true;
        }
        return false;
    }

    /**
     * Pobiera globalne statystyki systemu dotyczące wszystkich klientów.
     * @param session Aktualna sesja Hibernate.
     * @return Tablica obiektów zawierająca: 
     * [0] całkowitą liczbę klientów (Long), 
     * [1] listę dat urodzenia (List String), 
     * [2] dane o kategoriach i przychodach (List Object[]).
     */
    public Object[] getGlobalStatistics(Session session) {
        Long totalClients = session.createQuery("SELECT COUNT(c) FROM Client c", Long.class).getSingleResult();
        java.util.List<String> birthdates = session.createQuery("SELECT c.mBirthdate FROM Client c", String.class).getResultList();
        String hql = "SELECT c.mcategoryMember, a.aPrice FROM Client c JOIN c.activitySet a";
        java.util.List<Object[]> categoryRevenueData = session.createQuery(hql, Object[].class).getResultList();
        return new Object[]{totalClients, birthdates, categoryRevenueData};
    }

    /**
     * Pobiera statystyki uczestnictwa i przychodów dla konkretnej aktywności.
     * @param session Aktualna sesja Hibernate.
     * @param aId Identyfikator wybranej aktywności.
     * @return Tablica obiektów zawierająca: 
     * [0] liczbę zapisanych osób, 
     * [1] daty urodzenia uczestników, 
     * [2] kategorie członkowskie i ceny.
     */
    public Object[] getStatisticsForActivity(Session session, String aId) {
        String hqlCount = "SELECT COUNT(c) FROM Client c JOIN c.activitySet a WHERE a.aId = :id";
        Long totalClients = session.createQuery(hqlCount, Long.class)
                                   .setParameter("id", aId)
                                   .getSingleResult();

        String hqlBirth = "SELECT c.mBirthdate FROM Client c JOIN c.activitySet a WHERE a.aId = :id";
        java.util.List<String> birthdates = session.createQuery(hqlBirth, String.class)
                                                   .setParameter("id", aId)
                                                   .getResultList();

        String hqlRev = "SELECT c.mcategoryMember, a.aPrice FROM Client c JOIN c.activitySet a WHERE a.aId = :id";
        java.util.List<Object[]> revenueData = session.createQuery(hqlRev, Object[].class)
                                                      .setParameter("id", aId)
                                                      .getResultList();

        return new Object[]{totalClients, birthdates, revenueData};
    }
}