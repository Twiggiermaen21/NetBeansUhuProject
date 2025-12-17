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
 * Kontroler odpowiedzialny za pobieranie i prezentację danych encji Activity w
 * głównym oknie aplikacji (MainWindow).
 */
public class ActivityControllerTable {

    private static final Logger LOGGER = Logger.getLogger(ActivityControllerTable.class.getName());

    private final SessionFactory sessionFactory;
    private final MainWindow view;
    private final ActivityDAO activityDAO = new ActivityDAO();

    // =========================================================================
    // KONSTRUKTOR
    // =========================================================================
    public ActivityControllerTable(SessionFactory sessionFactory, MainWindow view) {
        this.sessionFactory = sessionFactory;
        this.view = view;
        initController();
    }

    private void initController() {
        // Podpinamy akcję pod przycisk "Oblicz" w oknie głównym
        
        this.view.jButtonCalculate.addActionListener(e -> handleCalculateStats());
    }

  private void handleCalculateStats() {
    // KROK A: Pobieramy ID wybranej aktywności z tabeli
    String selectedId = view.getSelectedActivityCode();

    if (selectedId == null || selectedId.trim().isEmpty()) {
        JOptionPane.showMessageDialog(view, "Najpierw zaznacz aktywność w tabeli!", "Brak wyboru", JOptionPane.WARNING_MESSAGE);
        return;
    }

    Session session = null;
    try {
        session = sessionFactory.openSession();
        // KROK B: Pobieramy dane przefiltrowane przez ID
        Object[] rawData = new Models.ClientDAO().getStatisticsForActivity(session, selectedId);

        long totalClients = (long) rawData[0];
        java.util.List<String> birthdates = (java.util.List<String>) rawData[1];
        java.util.List<Object[]> revenueData = (java.util.List<Object[]>) rawData[2];

        // 1. ŚREDNI WIEK (tylko dla tej aktywności)
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

        // 2. PRZYCHÓD I KATEGORIE (tylko dla tej aktywności)
        java.util.Map<Character, Integer> categoryCounts = new java.util.HashMap<>();
        double totalRevenue = 0;

        for (Object[] row : revenueData) {
            Character cat = (Character) row[0];
            double price = ((Number) row[1]).doubleValue(); // Naprawiony błąd rzutowania
            
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

        // 3. NAJCZĘSTSZA KATEGORIA (wśród zapisanych na tę aktywność)
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

        // 4. WYŚWIETLENIE OKNA
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

    // =========================================================================
    // POBIERANIE POJEDYNCZEJ ENCJ (DO EDYCJI)
    // =========================================================================
    /**
     * Pobiera pełny obiekt Activity na podstawie kodu zaznaczonego w tabeli.
     * Używane przez MainController przed uruchomieniem formularza edycji.
     *
     * * @return Obiekt Activity lub null, jeśli nic nie zaznaczono lub
     * wystąpił błąd.
     */
    public Activity getSelectedActivity() {
        String activityId = view.getSelectedActivityCode();

        if (activityId == null || activityId.trim().isEmpty()) {
            return null; // Nic nie zaznaczono
        }

        Session session = null;
        Activity activity = null;
        try {
            session = sessionFactory.openSession();
            // Delegowanie faktycznego pobrania obiektu do DAO
            activity = activityDAO.findActivityById(session, activityId);

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Błąd pobierania Aktywności o ID: " + activityId, e);
            // Nie wyświetlamy błędu, tylko logujemy, ponieważ to metoda pomocnicza.
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return activity;
    }

    // =========================================================================
    // WYŚWIETLANIE WSZYSTKICH DANYCH (READ)
    // =========================================================================
    /**
     * Pobiera wszystkie Aktywności z bazy danych i ustawia dane w tabeli
     * głównego widoku.
     */
    public void showActivities() {
        Session session = null;
        try {
            session = sessionFactory.openSession();

            // 1. Pobranie danych za pomocą HQL
            Query<Activity> query = session.createQuery("FROM Activity", Activity.class);
            List<Activity> activities = query.getResultList();
            LOGGER.info("Pobrano " + activities.size() + " aktywności.");

            // 2. Przygotowanie danych do tabeli Swing
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
                // Pokazanie nazwy Trenera lub "N/A"
                data[i][6] = a.getAtrainerInCharge() != null ? a.getAtrainerInCharge().getTName() : "N/A";
            }

            // 3. Aktualizacja widoku
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
