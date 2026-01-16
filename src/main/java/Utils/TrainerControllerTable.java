package Utils;

import Models.Trainer;
import Models.TrainerDAO;
import Views.MainWindow;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kontroler pomocniczy odpowiedzialny za pobieranie i prezentację danych encji {@link Trainer}
 * w głównym oknie aplikacji ({@link MainWindow}).
 * Klasa pośredniczy w procesie odświeżania tabeli trenerów oraz umożliwia pobieranie 
 * szczegółowych danych o wybranym z listy instruktorze na potrzeby edycji.
 */
public class TrainerControllerTable {

    /** Logger do rejestrowania operacji oraz błędów związanych z tabelą trenerów. */
    private static final Logger LOGGER = Logger.getLogger(TrainerControllerTable.class.getName());

    /** Fabryka sesji Hibernate przekazywana przy inicjalizacji. */
    private final SessionFactory sessionFactory;
    
    /** Referencja do głównego okna aplikacji. */
    private final MainWindow view;
    
    /** Obiekt dostępu do danych (DAO) dla trenerów. */
    private final TrainerDAO trainerDAO = new TrainerDAO(); 

    /** Statyczna definicja nazw kolumn dla tabeli trenerów w interfejsie graficznym. */
    private static final String[] COLUMN_NAMES = {"Kod", "Imię i Nazwisko", "ID (Numer)", "Telefon", "E-mail", "Data zatrudnienia", "Nick"};

    /**
     * Konstruktor inicjalizujący kontroler tabeli trenerów.
     * * @param sessionFactory Fabryka sesji Hibernate.
     * @param view           Instancja głównego widoku aplikacji.
     */
    public TrainerControllerTable(SessionFactory sessionFactory, MainWindow view) {
        this.sessionFactory = sessionFactory;
        this.view = view;
        
    }

    // =========================================================================
    // POBIERANIE POJEDYNCZEJ ENCJ (DO EDYCJI)
    // =========================================================================

    /**
     * Pobiera pełny obiekt {@link Trainer} na podstawie kodu zaznaczonego w tabeli interfejsu.
     * Metoda jest wykorzystywana przez kontroler główny do załadowania danych przed otwarciem 
     * okna edycji.
     * * @return Obiekt Trainer lub null, jeśli żaden wiersz nie został zaznaczony lub wystąpił błąd sesji.
     */
    public Trainer getSelectedTrainer() {
        // Pobranie unikalnego kodu trenera (tCod) bezpośrednio z modelu tabeli widoku
        String trainerCod = view.getSelectedTrainerCode(); 

        if (trainerCod == null || trainerCod.trim().isEmpty()) {
            return null;
        }

        Session session = null;
        Trainer trainer = null;
        try {
            session = sessionFactory.openSession();
            // Delegowanie pobrania obiektu po kluczu głównym do warstwy DAO
            trainer = trainerDAO.getTrainerByCod(session, trainerCod); 
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Błąd pobierania Trenera o kodzie: " + trainerCod, e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return trainer;
    }
    
    
    public void addNewRowToTable(Object[] rowData) {
    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) view.dataTable.getModel();
    model.addRow(rowData);
    
    // Zaznacz nowy wiersz na końcu
    int lastRow = model.getRowCount() - 1;
    view.dataTable.setRowSelectionInterval(lastRow, lastRow);
    view.dataTable.scrollRectToVisible(view.dataTable.getCellRect(lastRow, 0, true));
    
    // Dopasuj szerokość kolumn do nowej treści
    view.autoResizeColumns();
}

    // =========================================================================
    // WYŚWIETLANIE WSZYSTKICH DANYCH (READ)
    // =========================================================================

    /**
     * Pobiera listę wszystkich trenerów z bazy danych i odświeża główną tabelę w aplikacji.
     * Metoda wykonuje zapytanie HQL, a następnie mapuje listę obiektów na dwuwymiarową tablicę 
     * typu Object, która jest przesyłana do widoku w celu aktualizacji komponentu JTable.
     */
    public void showTrainers() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            // Wykonanie zapytania o wszystkich trenerów
            Query<Trainer> query = session.createQuery("FROM Trainer", Trainer.class);
            List<Trainer> trainers = query.getResultList();
            LOGGER.info("Pobrano " + trainers.size() + " trenerów.");

            String[] columns = COLUMN_NAMES; 
            Object[][] data = new Object[trainers.size()][7];

            // Mapowanie atrybutów encji Trainer na komórki tabeli
            for (int i = 0; i < trainers.size(); i++) {
                Trainer t = trainers.get(i);
                data[i][0] = t.getTCod();
                data[i][1] = t.getTName();
                data[i][2] = t.getTidNumber();
                data[i][3] = t.getTphoneNumber();
                data[i][4] = t.getTEmail();
                data[i][5] = t.getTDate();
                data[i][6] = t.getTNick();
            }

            // Przesłanie sformatowanych danych do komponentu graficznego
            view.setViewName("Trainers");
            view.setTableData(columns, data);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd podczas wyświetlania listy Trenerów.", ex);
            JOptionPane.showMessageDialog(view, "Błąd pobierania danych: " + ex.getMessage(), "Błąd DB", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}