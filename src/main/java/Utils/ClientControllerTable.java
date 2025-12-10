package Utils;

import Models.Client;
import Views.MainWindow;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.swing.*;
import java.util.List;

public class ClientControllerTable {
    
    // Pola klasy
    private List<Client> currentClients; // Przechowuje pełne obiekty Klientów po pobraniu
    private final SessionFactory sessionFactory;
    private final MainWindow view;

    public ClientControllerTable(SessionFactory sessionFactory, MainWindow view) {
        this.sessionFactory = sessionFactory;
        this.view = view;
    }
    
    // Usunięto niekompletną metodę showClientsNew()

    /**
     * Pobiera klientów z bazy, odświeża tabelę w widoku i zapisuje listę do pamięci.
     */
    public void showClients() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Query<Client> query = session.createQuery("FROM Client", Client.class);
            List<Client> clients = query.getResultList();

            // Zapisz listę klientów, aby później móc pobrać pełny obiekt do edycji/usuwania
            this.currentClients = clients; 

            // Mapowanie danych do tabeli (View)
            String[] columns = {"NUM", "NAME", "ID", "PHONE", "EMAIL", "START DATE", "CATEGORY"};
            Object[][] data = new Object[clients.size()][7];

            for (int i = 0; i < clients.size(); i++) {
                Client c = clients.get(i);
                data[i][0] = c.getMNum();
                data[i][1] = c.getMName();
                data[i][2] = c.getMId();
                data[i][3] = c.getMPhone();
                data[i][4] = c.getMemailMember();
                data[i][5] = c.getMstartingDateMember();
                data[i][6] = c.getMcategoryMember();
            }

            view.setViewName("Clients");
            view.setTableData(columns, data);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
    
    /**
     * Zwraca obiekt Client na podstawie zaznaczonego wiersza w tabeli.
     * Używane do edycji/usuwania.
     */
    public Client getSelectedClient() {
        // Zakładamy, że w MainWindow masz metodę getSelectedRow()
        int selectedRow = view.getSelectedRow(); 
        if (selectedRow != -1 && currentClients != null && selectedRow < currentClients.size()) {
            return currentClients.get(selectedRow);
        }
        return null;
    }
}