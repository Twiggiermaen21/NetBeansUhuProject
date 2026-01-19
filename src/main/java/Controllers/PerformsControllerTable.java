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

/**
 * Kontroler zarządzający powiązaniami między Aktywnościami a Klientami (tabela PERFORMS).
 * Odpowiada za wyświetlanie, usuwanie i edycję zapisów w tabeli asocjacyjnej Many-to-Many.
 */
public class PerformsControllerTable {

    private final SessionFactory sessionFactory;
    private final MainWindow view;
    private final ActivityDAO activityDAO = new ActivityDAO();

    public PerformsControllerTable(SessionFactory sessionFactory, MainWindow view) {
        this.sessionFactory = sessionFactory;
        this.view = view;
    }

    /**
     * Pobiera z bazy danych listę wszystkich zapisów (relacji klient-aktywność)
     * i odświeża widok tabeli w oknie głównym.
     */
    public void showPerforms() {
        Session session = sessionFactory.openSession();
        try {
            // HQL łączący Aktywność z kolekcją klientów (clientSet)
            String hql = "SELECT a.aId, a.aName, c.mNum, c.mName, c.mId " +
                         "FROM Activity a JOIN a.clientSet c";
            
            Query<Object[]> query = session.createQuery(hql, Object[].class);
            List<Object[]> results = query.list();

            // Definicja nagłówków tabeli dla widoku
            String[] columnNames = {
                "ID Aktywności", 
                "Nazwa Aktywności", 
                "Nr Klienta (ID)", 
                "Imię i Nazwisko", 
                "Dokument (PESEL/DNI)"
            };
            
            // Konwersja listy wyników na macierz obiektów akceptowaną przez JTable
            Object[][] data = new Object[results.size()][5];

            for (int i = 0; i < results.size(); i++) {
                Object[] row = results.get(i);
                data[i][0] = row[0]; // ID Aktywności
                data[i][1] = row[1]; // Nazwa Aktywności
                data[i][2] = row[2]; // Klucz główny klienta (mNum)
                data[i][3] = row[3]; // Imię i nazwisko
                data[i][4] = row[4]; // PESEL/DNI
            }

            // Aktualizacja nagłówka i danych w GUI
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
     * Pobiera ID Aktywności z aktualnie zaznaczonego wiersza w tabeli.
     * @return ID aktywności (String) lub null, jeśli nic nie wybrano.
     */
    public String getSelectedActivityId() {
        int viewRow = view.dataTable.getSelectedRow();
        if (viewRow != -1) {
            // Konwersja indeksu widoku na model (ważne przy sortowaniu/filtrowaniu tabeli)
            int modelRow = view.dataTable.convertRowIndexToModel(viewRow);
            return (String) view.dataTable.getModel().getValueAt(modelRow, 0);
        }
        return null;
    }

    /**
     * Pobiera Numer Klienta (PK) z zaznaczonego wiersza w tabeli.
     * @return Numer klienta (String) lub null.
     */
    public String getSelectedClientNum() {
        int viewRow = view.dataTable.getSelectedRow();
        if (viewRow != -1) {
            int modelRow = view.dataTable.convertRowIndexToModel(viewRow);
            return (String) view.dataTable.getModel().getValueAt(modelRow, 2);
        }
        return null;
    }
    
    /**
     * Usuwa relację między wybranym klientem a aktywnością.
     * Działa poprzez usunięcie obiektu klienta z kolekcji Set w obiekcie Activity.
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

                // Pobranie pełnych encji z bazy danych
                Activity activity = session.find(Activity.class, actId);
                Client client = session.find(Client.class, clientNum);

                if (activity != null && client != null) {
                    // W relacjach Many-to-Many zarządzanych przez Hibernate, 
                    // usunięcie elementu z Set-a i wykonanie merge usuwa wpis w tabeli pośredniczącej.
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
                if (tr != null) tr.rollback();
                e.printStackTrace();
                JOptionPane.showMessageDialog(view, "Błąd usuwania relacji: " + e.getMessage());
            } finally {
                if (session != null) session.close();
                showPerforms(); // Odświeżenie tabeli po operacji
            }
        }
    }

    /**
     * Edytuje relację: zmienia aktywność, do której przypisany jest dany klient.
     * Proces: Pobierz listę aktywności -> Wyświetl wybór -> Usuń stare powiązanie -> Dodaj nowe.
     */
    public void editPerforms() {
        String oldActId = getSelectedActivityId();
        String clientNum = getSelectedClientNum();

        if (oldActId == null || clientNum == null) {
            JOptionPane.showMessageDialog(view, "Proszę zaznaczyć wiersz do edycji.");
            return;
        }

        // 1. Pobranie listy dostępnych aktywności, aby użytkownik mógł wybrać nową
        Session session = sessionFactory.openSession();
        List<Activity> activities = activityDAO.findAllActivities(session);
        session.close();

        // 2. Przygotowanie tablicy nazw do wyświetlenia w oknie dialogowym (ComboBox)
        String[] activityNames = new String[activities.size()];
        for (int i = 0; i < activities.size(); i++) {
            activityNames[i] = activities.get(i).getAName();
        }

        // 3. Wyświetlenie okna wyboru (input dialog)
        String selectedName = (String) JOptionPane.showInputDialog(
                view,
                "Wybierz nowe zajęcia dla klienta:",
                "Zmiana zajęć",
                JOptionPane.QUESTION_MESSAGE,
                null,
                activityNames,
                null
        );

        // 4. Jeśli użytkownik wybrał nową aktywność
        if (selectedName != null) {

            // Znalezienie obiektu Activity odpowiadającego wybranej nazwie
            Activity newActivity = null;
            for (Activity a : activities) {
                if (a.getAName().equals(selectedName)) {
                    newActivity = a;
                    break;
                }
            }

            if (newActivity != null) {
                // Sprawdzenie, czy użytkownik nie wybrał tej samej aktywności, która już jest przypisana
                if (newActivity.getAId().equals(oldActId)) {
                    return; 
                }

                Session sess = null;
                Transaction tr = null;
                try {
                    sess = sessionFactory.openSession();
                    tr = sess.beginTransaction();

                    // Pobranie obiektów w bieżącej sesji
                    Activity oldActivityEntity = sess.find(Activity.class, oldActId);
                    Activity newActivityEntity = sess.find(Activity.class, newActivity.getAId());
                    Client clientEntity = sess.find(Client.class, clientNum);

                    if (oldActivityEntity != null && newActivityEntity != null && clientEntity != null) {
                        // KROK A: Usunięcie klienta ze starej aktywności
                        oldActivityEntity.getClientSet().remove(clientEntity);
                        sess.merge(oldActivityEntity);

                        // KROK B: Dodanie klienta do nowej aktywności
                        newActivityEntity.getClientSet().add(clientEntity);
                        sess.merge(newActivityEntity);

                        tr.commit();
                        JOptionPane.showMessageDialog(view, "Zmieniono zajęcia na: " + newActivity.getAName());
                    }
                } catch (Exception e) {
                    if (tr != null) tr.rollback();
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(view, "Błąd edycji: " + e.getMessage());
                } finally {
                    if (sess != null) sess.close();
                    showPerforms(); // Odświeżenie tabeli
                }
            }
        }
    }
}