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
 * Kontroler pomocniczy odpowiedzialny za pobieranie i prezentację danych encji {@link Client} 
 * w głównym oknie aplikacji ({@link MainWindow}).
 * Klasa optymalizuje dostęp do danych poprzez przechowywanie aktualnie wyświetlanej listy 
 * obiektów w pamięci podręcznej (cache), co pozwala na szybkie uzyskiwanie pełnych 
 * referencji do obiektów podczas operacji edycji lub usuwania bez konieczności 
 * ponownego odpytywania bazy danych.
 */
public class ClientControllerTable {
    
    /** Obiekt loggera do rejestrowania operacji na danych klientów. */
    private static final Logger LOGGER = Logger.getLogger(ClientControllerTable.class.getName());
    
    /** Lista klientów przechowywana w pamięci, zsynchronizowana z aktualnym widokiem tabeli. */
    private List<Client> currentClients; 
    
    /** Fabryka sesji Hibernate. */
    private final SessionFactory sessionFactory;
    
    /** Referencja do głównego okna aplikacji. */
    private final MainWindow view;

    /**
     * Konstruktor inicjalizujący kontroler tabeli klientów.
     * * @param sessionFactory Fabryka sesji Hibernate przekazana z kontrolera głównego.
     * @param view Instancja głównego okna aplikacji (widoku).
     */
    public ClientControllerTable(SessionFactory sessionFactory, MainWindow view) {
        this.sessionFactory = sessionFactory;
        this.view = view;   
    }
    
    // =========================================================================
    // WYŚWIETLANIE WSZYSTKICH DANYCH (READ)
    // =========================================================================

    /**
     * Pobiera listę wszystkich klientów z bazy danych za pomocą zapytania HQL, 
     * odświeża zawartość tabeli w widoku oraz aktualizuje wewnętrzną listę buforową.
     * Metoda mapuje pola obiektu {@link Client} na kolumny tabeli widoku:
     * numer członkowski, imię i nazwisko, identyfikator, data urodzenia, telefon, 
     * e-mail, data przyjęcia oraz kategoria.
     */
    public void showClients() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Query<Client> query = session.createQuery("FROM Client", Client.class);
            List<Client> clients = query.getResultList();
            LOGGER.info("Pobrano " + clients.size() + " klientów.");

            // Zapisz listę klientów do pamięci operacyjnej dla operacji CRUD
            this.currentClients = clients; 

            // Mapowanie danych do modelu tabeli Swing (Object[][])
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

            // Aktualizacja komponentu widoku
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
     * Zwraca pełny obiekt {@link Client} odpowiadający wierszowi aktualnie zaznaczonemu 
     * przez użytkownika w interfejsie graficznym.
     * Metoda synchronizuje indeks zaznaczenia tabeli z wewnętrzną listą {@code currentClients}.
     * * @return Zaznaczony obiekt {@link Client} lub null, jeśli żaden wiersz nie jest wybrany 
     * lub lista buforowa jest pusta.
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
    
    
    
    public void addNewRowToTable(Object[] rowData) {
    // Pobieramy model z tabeli znajdującej się w widoku głównym
    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) view.dataTable.getModel();
    
    // Dodajemy wiersz do modelu - JTable odświeży się automatycznie
    model.addRow(rowData);
    
    // Opcjonalnie: przesuń widok do nowego wiersza i zaznacz go
    int lastRow = model.getRowCount() - 1;
    view.dataTable.setRowSelectionInterval(lastRow, lastRow);
    view.dataTable.scrollRectToVisible(view.dataTable.getCellRect(lastRow, 0, true));
    
    // Ważne: zaktualizuj rozmiar kolumn po dodaniu nowych danych
    view.autoResizeColumns();
}
    
}