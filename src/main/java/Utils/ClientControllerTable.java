package Utils;

import Models.Client;
import Views.MainWindow;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kontroler odpowiedzialny za pobieranie i prezentację danych encji Client
 * w głównym oknie aplikacji (MainWindow). Przechowuje listę obiektów w pamięci
 * dla szybkich operacji edycji i usuwania.
 */
public class ClientControllerTable {
    
    private static final Logger LOGGER = Logger.getLogger(ClientControllerTable.class.getName());
    
    // Pola klasy
    private List<Client> currentClients; 
    private final SessionFactory sessionFactory;
    private final MainWindow view;

    public ClientControllerTable(SessionFactory sessionFactory, MainWindow view) {
        this.sessionFactory = sessionFactory;
        this.view = view;   
    }
    
    // =========================================================================
    // WYŚWIETLANIE WSZYSTKICH DANYCH (READ)
    // =========================================================================

    /**
     * Pobiera klientów z bazy, odświeża tabelę w widoku i zapisuje listę do pamięci.
     */
    public void showClients() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Query<Client> query = session.createQuery("FROM Client", Client.class);
            List<Client> clients = query.getResultList();
            LOGGER.info("Pobrano " + clients.size() + " klientów.");

            // Zapisz listę klientów, aby później móc pobrać pełny obiekt do edycji/usuwania
            this.currentClients = clients; 

            // Mapowanie danych do tabeli (View)
            String[] columns = {"NUM", "Nazwisko/Imię", "ID (PESEL/DNI)","Data Urodzenia", "Telefon", "E-mail", "Data Przyjęcia", "Kategoria"};
            Object[][] data = new Object[clients.size()][8];

            for (int i = 0; i < clients.size(); i++) {
                Client c = clients.get(i);
                data[i][0] = c.getMNum();
                data[i][1] = c.getMName();
                data[i][2] = c.getMId();
                 data[i][3] = c.getMBirthdate();
                data[i][4] = c.getMPhone();
                data[i][5] = c.getMemailMember();
                data[i][6] = c.getMstartingDateMember();
                data[i][7] = c.getMcategoryMember();
            }

            view.setViewName("Clients");
            view.setTableData(columns, data);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd podczas wyświetlania listy Klientów.", ex);
            JOptionPane.showMessageDialog(view, "Błąd pobierania danych: " + ex.getMessage(), "Błąd DB", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
    
    // =========================================================================
    // POBIERANIE ZAZNACZONEJ ENCJ (DO EDYCJI/USUWANIA)
    // =========================================================================

    /**
     * Zwraca obiekt Client na podstawie indeksu zaznaczonego wiersza w tabeli.
     * * @return Zaznaczony obiekt Client lub null, jeśli nic nie zaznaczono.
     */
    public Client getSelectedClient() {
        int selectedRow = view.getSelectedRow(); 
        
        if (selectedRow != -1 && currentClients != null && selectedRow < currentClients.size()) {
            LOGGER.info("Pobrano klienta z wiersza: " + selectedRow);
            return currentClients.get(selectedRow);
        }
        LOGGER.fine("Nie zaznaczono żadnego klienta.");
        return null;
    }
    
    
    
    
    
}