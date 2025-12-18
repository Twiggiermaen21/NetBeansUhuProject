import Config.HibernateUtil;
import Controllers.ConnectionController;

/**
 * Główna klasa projektu ISDD_Project.
 * Odpowiada za zainicjowanie głównego kontrolera aplikacji oraz 
 * prawidłowe zamknięcie sesji Hibernate przy zakończeniu pracy programu.
 * * @author Kacper Pudełko
 * @version 1.0
 */
public class ISDD_Project {

    /**
     * Punkt wejścia do aplikacji.
     * Metoda tworzy instancję {@link ConnectionController}, która zarządza połączeniem,
     * a następnie zamyka fabrykę sesji za pomocą {@link HibernateUtil#close()}.
     * * @param args Argumenty wiersza poleceń (nieużywane).
     */
    public static void main(String[] args) {

        new ConnectionController();

        HibernateUtil.close();

    }








//        System.out.println("START");
//        SessionFactory sessionFactory = DBConnect();
//        Session session;
//        Transaction tr;
//
//        try (Scanner keyboard = new Scanner(System.in)) {
//            String option;
//
//            do {
//                menu();
//                option = keyboard.nextLine();
//
//                switch (option) {
//                    case "1":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            Query<Client> q = session.createQuery("FROM Client c", Client.class);
//                            List<Client> clients = q.getResultList();
//
//                            System.out.println("====================================================================================================================");
//                            System.out.println("NUM" + "\t" + "NAME" + "\t" + "ID" + "\t" + "BIRTHDATE" + "\t" + "PHONE" + "\t" + "EMAIL" + "\t" + "START DATE" + "\t" + "CATEGORY");
//                            System.out.println("====================================================================================================================");
//
//                            for (Client c : clients) {
//                                System.out.println(c.getMNum() + "\t" + c.getMName() + "\t" + c.getMId() + "\t"
//                                        + c.getMBirthdate() + "\t" + c.getMPhone() + "\t"
//                                        + c.getMemailMember() + "\t" + c.getMstartingDateMember() + "\t"
//                                        + c.getMcategoryMember());
//                            }
//
//                            tr.commit();
//                        } catch (Exception e) {
//                            if (tr != null) tr.rollback();
//                            System.out.print("Error");
//                        } finally {
//                            if (session.isOpen()) {
//                                session.close();
//                            }
//                        }
//                        break;
//
//                    case "2":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            Query<Client> p = session.createNativeQuery("SELECT * FROM CLIENT", Client.class);
//                            List<Client> clients2 = p.getResultList();
//                            for (Client c : clients2) {
//                                System.out.println(c.getMName());
//                            }
//                            tr.commit();
//                        } catch (Exception e) {
//                            if (tr != null) {
//                                tr.rollback();
//                            }
//                            System.err.println("Error: " + e.getMessage());
//                        } finally {
//                            if (session.isOpen()) {
//                                session.close();
//                            }
//                        }
//                        break;
//
//                    case "3":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            Query<Client> p = session.createNamedQuery("Client.findAll", Client.class);
//                            List<Client> clients3 = p.getResultList();
//                            for (Client c : clients3) {
//                                System.out.println(c.getMName());
//                            }
//                            tr.commit();
//                        } catch (Exception e) {
//                            if (tr != null) {
//                                tr.rollback();
//                            }
//                        } finally {
//                            if (session.isOpen()) {
//                                session.close();
//                            }
//                        }
//                        break;
//
//                    case "4":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            Query<Object[]> q = session.createQuery("SELECT Client.mName, Client.mPhone FROM Client", Object[].class);
//                            List<Object[]> rows = q.getResultList();
//                            for (Object[] row : rows) {
//                                System.out.println(row[0] + " " + row[1]);
//                            }
//                            tr.commit();
//                        } catch (Exception e) {
//                            if (tr != null) tr.rollback();
//                        } finally {
//                            if (session.isOpen()) session.close();
//                        }
//                        break;
//
//                    case "5":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            System.out.println("Select category: A, B, C ,D lub E");
//                            String category = keyboard.nextLine().toUpperCase();
//                            Query<Object[]> q = session.createQuery("SELECT c.mName, c.mcategoryMember FROM Client c WHERE c.mcategoryMember = :category", Object[].class);
//                            q.setParameter("category", category.charAt(0));
//                            List<Object[]> rows = q.getResultList();
//                            for (Object[] row : rows) System.out.println(row[0] + " " + row[1]);
//                            tr.commit();
//                        } catch (Exception e) {
//                            if (tr != null) tr.rollback();
//                        } finally {
//                            if (session.isOpen()) session.close();
//                        }
//                    break;
//
//                    case "6":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            System.out.println("Enter name of the trainer:");
//                            String tName = keyboard.nextLine();
//                            Query<String> q = session.createQuery("SELECT t.tName FROM Trainer t WHERE t.tName = :nickname", String.class);
//                            q.setParameter("nickname", tName);
//                            List<String> names = q.getResultList();
//                            if (names.isEmpty()) {
//                                System.out.println("Ni mo");
//                            } else {
//                                for (String name : names) {
//                                    System.out.println("Trainer's name: " + name);
//                                }
//                            }
//                            tr.commit();
//                        } catch (Exception e) {
//                            if (tr != null) tr.rollback();
//                        } finally {
//                            if (session.isOpen()) session.close();
//                        }
//                        break;
//
//                    case "7":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            System.out.println("Enter member's name:");
//                            String mName = keyboard.nextLine();
//                            Query<Client> q = session.createQuery("FROM Client c WHERE c.mName = :name", Client.class);
//                            q.setParameter("name", mName);
//                            List<Client> clients = q.getResultList();
//                            if (clients.isEmpty()) {
//                                System.out.println("Ni ma kurwa");
//                            } else {
//                                for (Client c : clients) {
//                                    System.out.println("Member Info: " + c.getMNum() + ", " + c.getMName() + ", " + c.getMId() + ", " + c.getMBirthdate() + ", " + c.getMPhone() + ", " + c.getMemailMember() + ", " + c.getMstartingDateMember() + ", " + c.getMcategoryMember());
//                                }
//                            }
//                            tr.commit();
//                        } catch (Exception e) {
//                            if (tr != null) tr.rollback();
//                        } finally {
//                            if (session.isOpen()) session.close();
//                        }
//
//                        break;
//
//                    case "8":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            System.out.println("Enter day of the week:");
//                            String day = keyboard.nextLine();
//                            System.out.println("Enter maximum price:");
//                            int price = Integer.parseInt(keyboard.nextLine());
//                            Query<Object[]> q = session.createQuery("SELECT a.aName, a.aDay, a.aPrice FROM Activity a WHERE a.aDay = :day AND a.aPrice <= :price", Object[].class);
//                            q.setParameter("day", day);
//                            q.setParameter("price", price);
//                            List<Object[]> rows = q.getResultList();
//                            if (rows.isEmpty()) {
//                                System.out.println("No activities found.");
//                            } else {
//                                for (Object[] row : rows) {
//                                    System.out.println("Activity: " + row[0] + ", Day: " + row[1] + ", Price: " + row[2]);
//                                }
//                            }
//                            tr.commit();
//                        } catch (Exception e) {
//                            if (tr != null) tr.rollback();
//                        } finally {
//                            if (session.isOpen()) session.close();
//                        }
//                        break;
//
//                    case "9":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            System.out.println("Enter category: A, B, C, D or E");
//                            String category = keyboard.nextLine().toUpperCase();
//                            Query<Client> q = session.createNamedQuery("Client.findByMcategoryMember", Client.class);
//                            firstCharExtractor(tr, category, q);
//                        } catch (Exception e) {
//                            if (tr != null) tr.rollback();
//                        } finally {
//                            if (session.isOpen()) session.close();
//                        }
//
//                        break;
//
//                    case "10":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            System.out.println("Enter category: A, B, C, D or E");
//                            String category = keyboard.nextLine().toUpperCase();
//                            Query<Client> q = session.createNamedQuery("Client.findByMcategoryMemberSQL", Client.class);
//                            firstCharExtractor(tr, category, q);
//                        } catch (Exception e) {
//                            if (tr != null) tr.rollback();
//                        } finally {
//                            if (session.isOpen()) session.close();
//                        }
//
//                        break;
//
//                    case "11":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            Query<String> existingIdsQuery = session.createNativeQuery("SELECT m_id FROM CLIENT", String.class);
//                            List<String> clientsIds = existingIdsQuery.getResultList();
//
//                            String mId;
//                            do {
//                                System.out.println("Enter new member id");
//                                mId = keyboard.nextLine();
//                                if (clientsIds.contains(mId)) {
//                                    System.out.println("Member ID already exists. Please enter a different ID.");
//                                } else {
//                                    break;
//                                }
//                            } while (true);
//
//                            System.out.println("Enter new member name");
//                            String mName = keyboard.nextLine();
//                            System.out.println("Enter new member phone");
//                            String mPhone = keyboard.nextLine();
//                            System.out.println("Enter new member email");
//                            String mEmail = keyboard.nextLine();
//                            System.out.println("Enter new member birthdate (yyyy-MM-dd)");
//                            String mBirthdate = keyboard.nextLine();
//                            System.out.println("Enter member category A, B, C, D or E");
//                            String mCategory = keyboard.nextLine().toUpperCase();
//                            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
//                            String mNum = "S" + (clientsIds.size() + 1);
//
//                            Query<?> insert = session.createNativeQuery(
//                                    "INSERT INTO CLIENT (m_num, m_id, m_name, m_phone, m_emailMember, m_birthdate, m_startingDateMember, m_categoryMember) " +
//                                    "VALUES (:mNum,:mId, :mName, :mPhone, :mEmail, :mBirthdate, :mStartingDate, :mCategory)"
//                            );
//                            insert.setParameter("mNum", mNum);
//                            insert.setParameter("mId", mId);
//                            insert.setParameter("mName", mName);
//                            insert.setParameter("mPhone", mPhone);
//                            insert.setParameter("mEmail", mEmail);
//                            insert.setParameter("mBirthdate", mBirthdate);
//                            insert.setParameter("mStartingDate", currentDate);
//                            insert.setParameter("mCategory", mCategory.charAt(0));
//
//                            int rows = insert.executeUpdate();
//                            if (rows > 0) {
//                                System.out.println("Member inserted successfully.");
//                            } else {
//                                System.out.println("Insert failed.");
//                            }
//
//                            tr.commit();
//                        } catch (Exception e) {
//                            if (tr != null) tr.rollback();
//                            System.err.println("Error: " + e.getMessage());
//                        } finally {
//                            if (session.isOpen()) session.close();
//                        }
//                        break;
//
//                    case "12":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            System.out.println("Enter member ID to delete:");
//                            String mIdToDelete = keyboard.nextLine();
//                            Query<?> deleteQuery = session.createQuery("DELETE FROM Client c WHERE c.mId = :mId");
//                            deleteQuery.setParameter("mId", mIdToDelete);
//                            int rowsAffected = deleteQuery.executeUpdate();
//                            if (rowsAffected > 0) {
//                                System.out.println("Member deleted successfully.");
//                            } else {
//                                System.out.println("No member found with the given ID.");
//                            }
//                            tr.commit();
//                        } catch (Exception e) {
//                            if (tr != null) tr.rollback();
//                            System.err.println("Error: " + e.getMessage());
//                        } finally {
//                            if (session.isOpen()) session.close();
//                        }
//                        break;
//
//                    case "13":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            System.out.println("Enter trainer Id  whose activities you want to list:");
//                            String tCod = keyboard.nextLine();
//
//                            Query<String> getActivitiesQuery = session.createQuery(
//                                    "SELECT a.aName FROM Activity a WHERE a.atrainerInCharge.tCod = :tCod", String.class);
//                            getActivitiesQuery.setParameter("tCod", tCod);
//                            List<String> activities = (List<String>) getActivitiesQuery.getResultList();
//                            if (activities.isEmpty()) {
//                                System.out.println("No activities found for the given trainer.");
//                            } else {
//                                System.out.println("Trainers activity" + tCod + ":");
//                                for (String activityName : activities) {
//                                    System.out.println("- " + activityName);
//                                }
//                            }
//
//                        } catch (Exception e) {
//                            if (tr != null) tr.rollback();
//                            System.err.println("Error: " + e.getMessage());
//                        } finally {
//                            if (session.isOpen()) session.close();
//                        }
//                        break;
//
//                    case "14":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            System.out.println("Enter member ID to update category:");
//                            String mIdToUpdate = keyboard.nextLine();
//
//                            Query<String> findMemberActivitiesQuery = session.createQuery(
//                                "SELECT a.aName FROM Activity a JOIN a.clientSet c WHERE c.mNum = :mNum", String.class);
//                            findMemberActivitiesQuery.setParameter("mNum", mIdToUpdate);
//
//                            List<String> memberActivities = findMemberActivitiesQuery.getResultList();
//                            if (memberActivities.isEmpty()) {
//                                System.out.println("Member has no activities");
//                            } else {
//                                System.out.println("Member's activities:");
//                                for (String activity: memberActivities){
//                                    System.out.println("- " + activity);
//                                }
//                            }
//
//
//                        } catch (Exception e) {
//                            if (tr != null) tr.rollback();
//                            System.err.println("Error: " + e.getMessage());
//                        } finally {
//                            if (session.isOpen()) session.close();
//                        }
//                        break;
//
//                    case "15":
//                        session = sessionFactory.openSession();
//                        tr = session.beginTransaction();
//                        try {
//                            System.out.println("Enter activity ID to list members:");
//                            String aId = keyboard.nextLine();
//
//                            Query<String> getMembersQuery = session.createQuery(
//                                    "SELECT c.mName FROM Client c JOIN c.activitySet a WHERE a.aId = :aId", String.class);
//                            getMembersQuery.setParameter("aId", aId);
//
//                            List<String> members = getMembersQuery.getResultList();
//                            if (members.isEmpty()) {
//                                System.out.println("No members found for the given activity.");
//                            } else {
//                                System.out.println("Members enrolled in activity " + aId + ":");
//                                for (String memberName : members) {
//                                    System.out.println("- " + memberName);
//                                }
//                            }
//
//
//                        } catch (Exception e) {
//                            if (tr != null) tr.rollback();
//                            System.err.println("Error: " + e.getMessage());
//                        } finally {
//                            if (session.isOpen()) session.close();
//                        }
//                        break;
//
//
//                }
//            } while (!option.equals("0"));
//        } catch (HibernateException e) {
//            System.out.println("Error: " + e.getMessage());
//            e.printStackTrace();
//        }
//
//    }
//
//    private static void firstCharExtractor(Transaction tr, String category, Query<Client> q) {
//        q.setParameter("mcategoryMember", category.charAt(0));
//        List<Client> clients = q.getResultList();
//        if (clients.isEmpty()) {
//            System.out.println("No members found.");
//        } else {
//            for (Client c : clients) {
//                System.out.println("Member: " + c.getMName() + ", Category: " + c.getMcategoryMember());
//            }
//        }
//        tr.commit();
//    }
//
//    public static SessionFactory DBConnect() {
//        SessionFactory sessionFactory = null;
//        try {
//            sessionFactory = HibernateUtil.getSessionFactory();
//            System.err.println("Connected");
//
//        } catch (ExceptionInInitializerError e) {
//            Throwable cause = e.getCause();
//            System.err.println("Error" + (cause != null ? cause.getMessage() : e.getMessage()));
//
//        }
//        return sessionFactory;
//    }
//
//    public static void menu() {
//        System.out.println("\n===== MENU =====");
//        System.out.println("1. Show all members (HQL)");
//        System.out.println("2. Show all members (SQL)");
//        System.out.println("3. Show all members (Named Query)");
//        System.out.println("4. Name and phone of all members");
//        System.out.println("5. Name and category of members from selected category");
//        System.out.println("6. Monitor's name by nickname");
//        System.out.println("7. Member information by name");
//        System.out.println("8. Activities by day and payment");
//        System.out.println("9. Members by category (HQL Named Query)");
//        System.out.println("10. Members by category (SQL Named Query)");
//        System.out.println("11. Insert new member");
//        System.out.println("12. Delete member by ID");
//        System.out.println("13. List activities of a trainer");
//        System.out.println("14. List activities of a member");
//        System.out.println("15. List members of an activity");
//        System.out.println("0. Exit");
//    }
}
