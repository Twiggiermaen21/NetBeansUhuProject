package Controllers;

import Models.Activity;
import Models.ActivityDAO;

import Models.Client;
import Views.MainWindow;
import java.util.List;
import javax.swing.JOptionPane;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class PerformsControllerTable {

    private final SessionFactory sessionFactory;
    private final MainWindow view;
    private final ActivityDAO activityDAO = new ActivityDAO();

    public PerformsControllerTable(SessionFactory sessionFactory, MainWindow view) {
        this.sessionFactory = sessionFactory;
        this.view = view;
    }

    public void showPerforms() {
        Session session = sessionFactory.openSession();
        try {
            String hql = "SELECT a.aId, a.aName, c.mNum, c.mName, c.mId " +
                         "FROM Activity a JOIN a.clientSet c";
            
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            List<Object[]> results = query.list();

            String[] columnNames = {
                "ID Aktywności", 
                "Nazwa Aktywności", 
                "Nr Klienta (ID)", 
                "Imię i Nazwisko", 
                "Dokument (PESEL/DNI)"
            };
            
            Object[][] data = new Object[results.size()][5];

            for (int i = 0; i < results.size(); i++) {
                Object[] row = results.get(i);
                data[i][0] = row[0]; // a.aId (Kolumna 0)
                data[i][1] = row[1]; // a.aName
                data[i][2] = row[2]; // c.mNum (Kolumna 2 - Klucz główny klienta)
                data[i][3] = row[3]; // c.mName
                data[i][4] = row[4]; // c.mId
            }

            view.setViewName("Lista Zapisów (Zarządzanie)");
            view.setTableData(columnNames, data);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Pobiera ID Aktywności z zaznaczonego wiersza (Kolumna 0).
     */
    public String getSelectedActivityId() {
        int viewRow = view.dataTable.getSelectedRow();
        if (viewRow != -1) {
            int modelRow = view.dataTable.convertRowIndexToModel(viewRow);
            return (String) view.dataTable.getModel().getValueAt(modelRow, 0);
        }
        return null;
    }

    /**
     * Pobiera Numer Klienta (PK) z zaznaczonego wiersza (Kolumna 2).
     */
    public String getSelectedClientNum() {
        int viewRow = view.dataTable.getSelectedRow();
        if (viewRow != -1) {
            int modelRow = view.dataTable.convertRowIndexToModel(viewRow);
            return (String) view.dataTable.getModel().getValueAt(modelRow, 2);
        }
        return null;
    }
    
    
      // --- NOWE METODY DLA PERFORMS ---
    /**
     * Usuwa relację (wypisuje klienta z aktywności).
     */
   public void deletePerforms() {
        String actId = getSelectedActivityId();
        String clientNum = getSelectedClientNum();

        if (actId == null || clientNum == null) {
            JOptionPane.showMessageDialog(view, "Proszę zaznaczyć wiersz do usunięcia.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(view,
                "Czy wypisać klienta " + clientNum + " z zajęć " + actId + "?",
                "Potwierdź wypisanie", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Session session = null;
            Transaction tr = null;
            try {
                session = sessionFactory.openSession();
                tr = session.beginTransaction();

                Activity activity = session.find(Activity.class, actId);
                Client client = session.find(Client.class, clientNum);

                if (activity != null && client != null) {
                    // Usuwamy klienta z setu aktywności (Hibernate sam usunie wpis w tabeli łączącej PERFORMS)
                    if (activity.getClientSet().contains(client)) {
                        activity.getClientSet().remove(client);
                        session.merge(activity);
                        tr.commit();
                        JOptionPane.showMessageDialog(view, "Wypisano pomyślnie.");
                    } else {
                        JOptionPane.showMessageDialog(view, "Błąd: Brak powiązania w bazie.");
                    }
                }
            } catch (Exception e) {
                if (tr != null) {
                    tr.rollback();
                }
                e.printStackTrace();
                JOptionPane.showMessageDialog(view, "Błąd usuwania relacji: " + e.getMessage());
            } finally {
                if (session != null) {
                    session.close();
                }
                // Odśwież tabelę
                showPerforms();
            }
        }
    }

    /**
     * Edytuje relację (zmienia aktywność dla klienta). Działa to na zasadzie:
     * Usuń stare powiązanie -> Dodaj nowe.
     */
   public void editPerforms() {
        String oldActId = getSelectedActivityId();
        String clientNum = getSelectedClientNum();

        if (oldActId == null || clientNum == null) {
            JOptionPane.showMessageDialog(view, "Proszę zaznaczyć wiersz do edycji.");
            return;
        }

        // 1. Pobieramy listę wszystkich dostępnych aktywności
        Session session = sessionFactory.openSession();
        List<Activity> activities = activityDAO.findAllActivities(session);
        session.close();

        // 2. Tworzymy tablicę samych nazw (Stringów) do wyświetlenia w oknie
        String[] activityNames = new String[activities.size()];
        for (int i = 0; i < activities.size(); i++) {
            activityNames[i] = activities.get(i).getAName();
        }

        // 3. Wyświetlamy okno z listą nazw
        String selectedName = (String) JOptionPane.showInputDialog(
                view,
                "Wybierz nowe zajęcia dla klienta:",
                "Zmiana zajęć",
                JOptionPane.QUESTION_MESSAGE,
                null,
                activityNames, // Przekazujemy tablicę Stringów
                null
        );

        // 4. Jeśli użytkownik coś wybrał (nie kliknął Anuluj)
        if (selectedName != null) {

            // Szukamy obiektu Activity, który pasuje do wybranej nazwy
            Activity newActivity = null;
            for (Activity a : activities) {
                if (a.getAName().equals(selectedName)) {
                    newActivity = a;
                    break;
                }
            }

            // Jeśli znaleziono (zawsze powinno znaleźć) i jest to inna aktywność niż obecna
            if (newActivity != null) {
                if (newActivity.getAId().equals(oldActId)) {
                    return; // To ta sama aktywność, nic nie robimy
                }

                Session sess = null;
                Transaction tr = null;
                try {
                    sess = sessionFactory.openSession();
                    tr = sess.beginTransaction();

                    // Używamy .find() zamiast przestarzałego .get()
                    Activity oldActivityEntity = sess.find(Activity.class, oldActId);
                    Activity newActivityEntity = sess.find(Activity.class, newActivity.getAId());
                    Client clientEntity = sess.find(Client.class, clientNum);

                    if (oldActivityEntity != null && newActivityEntity != null && clientEntity != null) {
                        // A. Usuń ze starej
                        oldActivityEntity.getClientSet().remove(clientEntity);
                        sess.merge(oldActivityEntity);

                        // B. Dodaj do nowej
                        newActivityEntity.getClientSet().add(clientEntity);
                        sess.merge(newActivityEntity);

                        tr.commit();
                        JOptionPane.showMessageDialog(view, "Zmieniono zajęcia na: " + newActivity.getAName());
                    }
                } catch (Exception e) {
                    if (tr != null) {
                        tr.rollback();
                    }
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(view, "Błąd edycji: " + e.getMessage());
                } finally {
                    if (sess != null) {
                        sess.close();
                    }
                    showPerforms();
                }
            }
        }
    }
    
    
}