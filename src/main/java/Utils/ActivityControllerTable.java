package Utils;

import Models.Activity;
import Models.ActivityDAO;
import Views.CalculateWindow;
import Views.MainWindow;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.swing.JOptionPane;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kontroler pomocniczy odpowiedzialny za zarządzanie prezentacją danych encji {@link Activity} 
 * w głównej tabeli aplikacji.
 * Klasa odpowiada za pobieranie list aktywności, obsługę zaznaczeń w interfejsie 
 * oraz realizację logiki biznesowej związanej z obliczaniem statystyk uczestnictwa, 
 * średniego wieku oraz prognozowanego przychodu.
 */
public class ActivityControllerTable {

    /** Logger do rejestrowania operacji na tabeli aktywności oraz błędów obliczeniowych. */
    private static final Logger LOGGER = Logger.getLogger(ActivityControllerTable.class.getName());

    /** Fabryka sesji Hibernate. */
    private final SessionFactory sessionFactory;
    
    /** Główne okno aplikacji, w którym wyświetlana jest tabela. */
    private final MainWindow view;
    
    /** Obiekt dostępu do danych dla aktywności. */
    private final ActivityDAO activityDAO = new ActivityDAO();

    /**
     * Konstruktor inicjalizujący kontroler tabeli aktywności.
     * * @param sessionFactory Fabryka sesji Hibernate.
     * @param view Instancja głównego okna aplikacji.
     */
    public ActivityControllerTable(SessionFactory sessionFactory, MainWindow view) {
        this.sessionFactory = sessionFactory;
        this.view = view;
        initController();
    }

    /**
     * Inicjalizuje słuchacze zdarzeń dla komponentów powiązanych z tabelą aktywności, 
     * w szczególności podpinając akcję pod przycisk obliczania statystyk.
     */
    private void initController() {
        this.view.jButtonCalculate.addActionListener(e -> handleCalculateStats());
    }

    /**
     * Pobiera dane o uczestnikach zaznaczonej aktywności i wylicza statystyki zbiorcze.
     * Obliczenia obejmują:
     * 1. Całkowitą liczbę zapisanych klientów.
     * 2. Średni wiek uczestników na podstawie ich dat urodzenia.
     * 3. Przychód całkowity z uwzględnieniem zniżek przypisanych do kategorii klientów.
     * 4. Najczęściej występującą kategorię członkowską.
     */
    private void handleCalculateStats() {
        String selectedId = view.getSelectedActivityCode();

        if (selectedId == null || selectedId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Najpierw zaznacz aktywność w tabeli!", "Brak wyboru", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Session session = null;
        try {
            session = sessionFactory.openSession();
            // Pobranie surowych danych statystycznych z bazy
            Object[] rawData = new Models.ClientDAO().getStatisticsForActivity(session, selectedId);

            long totalClients = (long) rawData[0];
            java.util.List<String> birthdates = (java.util.List<String>) rawData[1];
            java.util.List<Object[]> revenueData = (java.util.List<Object[]>) rawData[2];

            // 1. Obliczanie średniego wieku
            double avgAge = 0;
            int currentYear = java.util.Calendar.getInstance().get(java.util.Calendar.YEAR);
            int validDates = 0;
            for (String bday : birthdates) {
                if (bday != null && bday.length() >= 10) {
                    try {
                        int birthYear = Integer.parseInt(bday.substring(6, 10));
                        avgAge += (currentYear - birthYear);
                        validDates++;
                    } catch (Exception e) {}
                }
            }
            if (validDates > 0) avgAge /= validDates;

            // 2. Obliczanie przychodu z uwzględnieniem rabatów (Kategoria B=10%, C=20%, D=30%)
            java.util.Map<Character, Integer> categoryCounts = new java.util.HashMap<>();
            double totalRevenue = 0;

            for (Object[] row : revenueData) {
                Character cat = (Character) row[0];
                double price = ((Number) row[1]).doubleValue(); 
                
                double discount = 0.0;
                switch (Character.toUpperCase(cat)) {
                    case 'B': discount = 0.10; break;
                    case 'C': discount = 0.20; break;
                    case 'D': discount = 0.30; break;
                    default:  discount = 0.0;  break;
                }
                totalRevenue += (price * (1.0 - discount));
                categoryCounts.put(cat, categoryCounts.getOrDefault(cat, 0) + 1);
            }

            // 3. Wyznaczanie najczęstszej kategorii
            String topCategory = "Brak";
            if (!categoryCounts.isEmpty()) {
                topCategory = categoryCounts.entrySet().stream()
                    .max((e1, e2) -> {
                        int res = e1.getValue().compareTo(e2.getValue());
                        if (res == 0) return e1.getKey().compareTo(e2.getKey());
                        return res;
                    })
                    .map(e -> e.getKey().toString()).orElse("Brak");
            }

            // 4. Prezentacja wyników w nowym oknie
            CalculateWindow calcWin = new CalculateWindow();
            calcWin.setTitle("Statystyki dla: " + selectedId);
            calcWin.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
            calcWin.setResults((int) totalClients, avgAge, topCategory, totalRevenue);
            calcWin.setVisible(true);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd statystyk", ex);
            JOptionPane.showMessageDialog(view, "Błąd: " + ex.getMessage());
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Pobiera pełny obiekt {@link Activity} na podstawie wiersza aktualnie zaznaczonego w tabeli.
     * Metoda jest kluczowa dla procesu edycji, pozwalając na pobranie danych z bazy 
     * przed otwarciem formularza aktualizacji.
     * * @return Obiekt Activity lub null, jeśli nie dokonano wyboru.
     */
    public Activity getSelectedActivity() {
        String activityId = view.getSelectedActivityCode();

        if (activityId == null || activityId.trim().isEmpty()) {
            return null;
        }

        Session session = null;
        Activity activity = null;
        try {
            session = sessionFactory.openSession();
            activity = activityDAO.findActivityById(session, activityId);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Błąd pobierania Aktywności o ID: " + activityId, e);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return activity;
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
    /**
     * Pobiera listę wszystkich aktywności z bazy danych i odświeża widok tabeli 
     * w oknie głównym aplikacji. Metoda mapuje listę obiektów na format dwuwymiarowej 
     * tablicy akceptowanej przez model tabeli Swing.
     */
    public void showActivities() {
        Session session = null;
        try {
            session = sessionFactory.openSession();

            Query<Activity> query = session.createQuery("FROM Activity", Activity.class);
            List<Activity> activities = query.getResultList();
            LOGGER.info("Pobrano " + activities.size() + " aktywności.");

            String[] columns = {"ID", "Nazwa", "Opis/Typ", "Cena", "Dzień", "Godzina", "Trener"};
            Object[][] data = new Object[activities.size()][7];

            for (int i = 0; i < activities.size(); i++) {
                Activity a = activities.get(i);
                data[i][0] = a.getAId();
                data[i][1] = a.getAName();
                data[i][2] = a.getADescription();
                data[i][3] = a.getAPrice();
                data[i][4] = a.getADay();
                data[i][5] = a.getAHour();
                data[i][6] = a.getAtrainerInCharge() != null ? a.getAtrainerInCharge().getTName() : "N/A";
            }

            view.setViewName("Activities");
            view.setTableData(columns, data);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd podczas wyświetlania listy Aktywności.", ex);
            JOptionPane.showMessageDialog(view, "Błąd pobierania danych: " + ex.getMessage(), "Błąd DB", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}