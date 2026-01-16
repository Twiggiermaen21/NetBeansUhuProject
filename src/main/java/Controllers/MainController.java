package Controllers;

import Config.HibernateUtil;
import Models.*;
import Views.DataUpdateWindow;
import Views.MainWindow;
import Utils.*;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * Główny Kontroler aplikacji zarządza przepływem danych i interakcją między
 * modelami a widokiem głównym. Klasa odpowiada za dynamiczne przełączanie
 * między widokami klientów, trenerów i aktywności, obsługę pełnego cyklu
 * operacji CRUD oraz zarządzanie relacjami biznesowymi, takimi jak zapisywanie
 * klientów na wybrane aktywności (relacja Many-to-Many).
 */
public class MainController implements ActionListener {

    /**
     * Obiekt loggera do rejestrowania zdarzeń systemowych i błędów w warstwie
     * kontrolera.
     */
    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    /**
     * Fabryka sesji Hibernate współdzielona między wszystkimi komponentami
     * aplikacji.
     */
    private final SessionFactory sessionFactory;

    /**
     * Główne okno interfejsu graficznego użytkownika.
     */
    private final MainWindow view;

    /**
     * Obiekty dostępu do danych (DAO) dla głównych encji systemowych.
     */
    private final ClientDAO clientDAO = new ClientDAO();
    private final TrainerDAO trainerDAO = new TrainerDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();

    /**
     * Flaga określająca aktualnie wyświetlany moduł (np. "Client", "Trainer",
     * "Activity").
     */
    private String currentView = "Init";

    /**
     * Kontrolery pomocnicze odpowiedzialne za zarządzanie danymi wyświetlanymi
     * w tabelach.
     */
    private final ClientControllerTable clientControllerTable;
    private final TrainerControllerTable trainerControllerTable;
    private final ActivityControllerTable activityControllerTable;

    /**
     * Konstruktor głównego kontrolera. Inicjalizuje podrzędne kontrolery tabel,
     * rejestruje słuchacze zdarzeń menu i przycisków oraz przygotowuje
     * komponenty do obsługi relacji między encjami (np. ComboBox aktywności).
     *
     * * @param sessionFactory Fabryka sesji przekazana z ConnectionController
     * po udanym logowaniu.
     */
    public MainController(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.view = new MainWindow();

        // Inicjalizacja kontrolerów podrzędnych
        this.clientControllerTable = new ClientControllerTable(sessionFactory, view);
        this.trainerControllerTable = new TrainerControllerTable(sessionFactory, view);
        this.activityControllerTable = new ActivityControllerTable(sessionFactory, view);

        addListeners();
        addWindowCloseListener();

        // --- INICJALIZACJA KOMPONENTÓW POŁĄCZEŃ ---
        refreshActivityCombo();
        initTableSelectionLogic();
        initClientToActivityButton();

        showInit();
        view.setVisible(true);
    }

    /**
     * Rejestruje słuchacze zdarzeń dla pozycji menu oraz głównych przycisków
     * akcji (Nowy, Usuń, Aktualizuj).
     */
    private void addListeners() {
        view.addClientMenuListener(this);
        view.addTrainerMenuListener(this);
        view.addActivitiesMenuListener(this);
        view.addInitMenuListener(this);

        view.addNowyListener(new ActionListenerForAddButton());
        view.addUsunListener(new ActionListenerForUsunButton());
        view.addAktualizujListener(new ActionListenerForUpdateButton());
    }

    /**
     * Dodaje słuchacza zamknięcia okna, który zapewnia poprawne zamknięcie
     * fabryki sesji Hibernate przed wyjściem z programu.
     */
    private void addWindowCloseListener() {
        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                HibernateUtil.close();
                System.exit(0);
            }
        });
    }

    /**
     * Pobiera listę aktywności z bazy danych i odświeża komponent JComboBox.
     * Stosuje niestandardowy renderer, aby wyświetlać nazwy aktywności,
     * zachowując dostęp do pełnych obiektów {@link Activity}.
     */
    public void refreshActivityCombo() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            List<Activity> list = activityDAO.findAllActivities(session);

            // Renderer: Wyświetla nazwę (getAName) zamiast domyślnego toString
            view.jComboBoxClientToActivity.setRenderer(new javax.swing.DefaultListCellRenderer() {
                @Override
                public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                    if (value instanceof Activity a) {
                        setText(a.getAName());
                    }
                    return this;
                }
            });

            // Rzutowanie na surowy typ JComboBox, aby uniknąć błędów generyków w Swing
            javax.swing.JComboBox combo = (javax.swing.JComboBox) view.jComboBoxClientToActivity;
            combo.removeAllItems();
            for (Activity a : list) {
                combo.addItem(a);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Błąd odświeżania ComboBox", e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Inicjalizuje logikę śledzenia zaznaczenia w tabeli. Przenosi Imię i
     * Nazwisko zaznaczonego klienta do pola tekstowego połączenia.
     */
    private void initTableSelectionLogic() {
        view.dataTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = view.dataTable.getSelectedRow();
                // Działa tylko w widoku Klientów
                if (selectedRow != -1 && "Client".equals(currentView)) {
                    Object name = view.getSelectedValueAt(1); // Kolumna 1: Imię i Nazwisko
                    if (name != null) {
                        view.jTextFieldClientToActivity.setText(name.toString());
                    }
                }
            }
        });
    }

    /**
     * Konfiguruje obsługę przycisku przypisywania klienta do aktywności.
     * Weryfikuje poprawność zaznaczenia oraz inicjuje zapis relacji w bazie
     * danych.
     */
    private void initClientToActivityButton() {
        view.jButtonClientToActivity.addActionListener(e -> {
            String clientCode = view.getSelectedClientCode(); // ID z kolumny 0
            Object selectedItem = view.jComboBoxClientToActivity.getSelectedItem();

            if (clientCode == null || selectedItem == null || !(selectedItem instanceof Activity)) {
                JOptionPane.showMessageDialog(view, "Błąd: Wybierz klienta w tabeli i aktywność z listy!",
                        "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            }

            handleSaveEnrollment(clientCode, (Activity) selectedItem);
        });
    }

    
    /**
     * Zapisuje powiązanie Many-to-Many między Klientem a Aktywnością.
     * Wykorzystuje sesję Hibernate do pobrania obiektów i aktualizacji zbioru
     * uczestników.
     *
     * * @param clientCode Kod zaznaczonego klienta.
     * @param activity Wybrany obiekt aktywności.
     */
    private void handleSaveEnrollment(String clientCode, Activity activity) {
        Session session = null;
        Transaction tr = null;
        try {
            session = sessionFactory.openSession();
            tr = session.beginTransaction();

            Client client = session.find(Client.class, clientCode);
            Activity managedActivity = session.find(Activity.class, activity.getAId());

            if (client != null && managedActivity != null) {
                if (managedActivity.getClientSet() == null) {
                    managedActivity.setClientSet(new java.util.HashSet<>());
                }

                boolean added = managedActivity.getClientSet().add(client);

                if (!added) {
                    JOptionPane.showMessageDialog(view, "Ten klient jest już zapisany na tę aktywność!");
                    tr.rollback();
                    return;
                }

                session.merge(managedActivity);
                tr.commit();
                JOptionPane.showMessageDialog(view, "Zapisano pomyślnie: " + client.getMName() + " -> " + managedActivity.getAName());
            }
        } catch (Exception ex) {
            if (tr != null) {
                tr.rollback();
            }
            LOGGER.log(Level.SEVERE, "Błąd zapisu relacji", ex);
            JOptionPane.showMessageDialog(view, "Błąd zapisu: " + ex.getMessage());
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Obsługuje zdarzenia wyboru modułu z menu górnego. Zmienia kontekst
     * aplikacji, etykiety przycisków i odświeża dane w tabeli.
     *
     * * @param e Obiekt zdarzenia menu.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    // Wywołaj czyszczenie przed zmianą danych (opcjonalnie przed switchem lub w każdym case)
    view.clearSearchFields(); 
     view.setupTableSorter();
    switch (e.getActionCommand()) {
        case "ShowClients" -> {
            resetTableSize();
            clientControllerTable.showClients();
            view.setButtonLabels("Dodaj Klienta", "Usuń Klienta", "Edytuj Klienta");
            currentView = "Client";
            updatePanelVisibility(true, false, true);
            view.setupTableSorter();
        }
        case "ShowTrainers" -> {
            resetTableSize();
            trainerControllerTable.showTrainers();
            view.setButtonLabels("Dodaj Trenera", "Usuń Trenera", "Edytuj Trenera");
            currentView = "Trainer";
            updatePanelVisibility(true, false, false);
            view.setupTableSorter();
        }
        case "ShowActivities" -> {
            resetTableSize();
            activityControllerTable.showActivities();
            view.setButtonLabels("Dodaj Aktywność", "Usuń Aktywność", "Edytuj Aktywność");
            currentView = "Activity";
            updatePanelVisibility(true, true, false);
            view.setupTableSorter();
        }
        case "ShowInit" -> showInit();
    }
}

    /**
     * Zarządza widocznością paneli interfejsu użytkownika w zależności od
     * kontekstu.
     *
     * * @param crud Widoczność przycisków operacji podstawowych.
     * @param calc Widoczność panelu kalkulacji.
     * @param enroll Widoczność panelu zapisów na aktywności.
     */
    private void updatePanelVisibility(boolean crud, boolean calc, boolean enroll) {
        view.jNowy.setVisible(crud);
        view.jUsun.setVisible(crud);
        view.jAktualizuj.setVisible(crud);
        view.jPanelSearch.setVisible(crud);
        view.jPanelCalculate.setVisible(calc);
        view.jPanelClientToActivity.setVisible(enroll);
//         view.dataTable.setVisible(crud);
//        view.jScrollPane1.setVisible(crud);

    }

    /**
     * Przywraca stan początkowy interfejsu z instrukcją powitalną.
     */
    private void showInit() {
        // Wymiary dla ekranu powitalnego
        int targetWidth = 845;
        int targetHeight = 235;

        java.net.URL imgURL = getClass().getResource("/gym.png");
        if (imgURL != null) {
            javax.swing.ImageIcon icon = new javax.swing.ImageIcon(imgURL);
            Image scaledImage = icon.getImage().getScaledInstance(targetWidth, targetHeight, Image.SCALE_SMOOTH);
            javax.swing.ImageIcon welcomeIcon = new javax.swing.ImageIcon(scaledImage);

            view.setTableData(new String[]{"Witamy"}, new Object[][]{{welcomeIcon}});

            // Ustawienie rozmiaru JScrollPane dla INIT
            view.jScrollPane1.setPreferredSize(new java.awt.Dimension(targetWidth, targetHeight));
        }

        updatePanelVisibility(false, false, false);
        view.viewNameLabel.setText("System ISDD - Ekran Startowy");
        currentView = "Init";

        // Kluczowe: wymuszenie na LayoutManagerze przeliczenia rozmiarów
        view.pack();
        view.revalidate();
        view.repaint();
    }

    /**
     * Otwiera okno edycji/dodawania i inicjalizuje odpowiedni kontroler
     * szczegółowy.
     *
     * * @param entity Obiekt do edycji lub null w przypadku tworzenia nowego
     * rekordu.
     * @param type Typ danych ("Client", "Trainer" lub "Activity").
     */
    private void handleFormAction(Object entity, String type) {
        DataUpdateWindow form = new DataUpdateWindow();
        Object ctrl = null;

        switch (type) {
            case "Client" ->
                ctrl = new ClientDataController(sessionFactory, form, clientControllerTable, (Client) entity);
            case "Trainer" ->
                ctrl = new TrainerDataController(sessionFactory, form, trainerControllerTable, (Trainer) entity);
            case "Activity" ->
                ctrl = new ActivityDataController(sessionFactory, form, activityControllerTable, (Activity) entity);
        }

        if (ctrl instanceof ClientDataController c) {
            c.initializeForm();
        } else if (ctrl instanceof TrainerDataController t) {
            t.initializeForm();
        } else if (ctrl instanceof ActivityDataController a) {
            a.initializeForm();
        }

        form.setDefaultCloseOperation(DataUpdateWindow.DISPOSE_ON_CLOSE);
        form.setVisible(true);
    }

    /**
     * Klasa wewnętrzna obsługująca zdarzenie dodawania nowej pozycji.
     */
    private class ActionListenerForAddButton implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            handleFormAction(null, currentView);
        }
    }

    /**
     * Klasa wewnętrzna obsługująca zdarzenie aktualizacji zaznaczonej pozycji.
     */
    private class ActionListenerForUpdateButton implements ActionListener {

        public void actionPerformed(ActionEvent e) {
            Object entity = switch (currentView) {
                case "Client" ->
                    clientControllerTable.getSelectedClient();
                case "Trainer" ->
                    trainerControllerTable.getSelectedTrainer();
                case "Activity" ->
                    activityControllerTable.getSelectedActivity();
                default ->
                    null;
            };
            if (entity != null) {
                handleFormAction(entity, currentView);
            } else {
                JOptionPane.showMessageDialog(view, "Proszę zaznaczyć element do edycji.");
            }
        }
    }

    /**
     * Klasa wewnętrzna obsługująca zdarzenie usuwania zaznaczonej pozycji.
     */
    private class ActionListenerForUsunButton implements ActionListener {

    public void actionPerformed(ActionEvent e) {
        String code = switch (currentView) {
            case "Client" -> view.getSelectedClientCode();
            case "Trainer" -> view.getSelectedTrainerCode();
            case "Activity" -> view.getSelectedActivityCode();
            default -> null;
        };

        if (code != null) {
            // Logika potwierdzenia usunięcia
            int confirm = JOptionPane.showConfirmDialog(
                view, 
                "Czy na pewno usunąć: " + code + "?", 
                "Potwierdzenie usunięcia", 
                JOptionPane.YES_NO_OPTION, 
                JOptionPane.QUESTION_MESSAGE
            );
            
            if (confirm == JOptionPane.YES_OPTION) {
                deleteEntity(currentView, code);
            }
        } else {
            // NOWE: Informacja dla użytkownika, gdy nic nie zaznaczono
            JOptionPane.showMessageDialog(
                view, 
                "Proszę najpierw zaznaczyć element w tabeli, który chcesz usunąć.", 
                "Brak zaznaczenia", 
                JOptionPane.WARNING_MESSAGE
            );
        }
    }
}
    /**
     * Wykonuje operację trwałego usunięcia encji z bazy danych za pomocą DAO.
     *
     * * @param type Typ danych do usunięcia.
     * @param code Unikalny kod (klucz) encji.
     */
    private void deleteEntity(String type, String code) {
        Session session = null;
        Transaction tr = null;
        try {
            session = sessionFactory.openSession();
            tr = session.beginTransaction();
            boolean success = switch (type) {
                case "Client" ->
                    clientDAO.deleteClientByMemberNumber(session, code);
                case "Trainer" ->
                    trainerDAO.deleteTrainerById(session, code);
                case "Activity" ->
                    activityDAO.deleteActivityById(session, code);
                default ->
                    false;
            };
            if (success) {
                tr.commit();
                if ("Client".equals(type)) {
                    clientControllerTable.showClients();
                } else if ("Trainer".equals(type)) {
                    trainerControllerTable.showTrainers();
                } else {
                    activityControllerTable.showActivities();
                }
            }
        } catch (Exception ex) {
            if (tr != null) {
                tr.rollback();
            }
            LOGGER.log(Level.SEVERE, "Błąd usuwania", ex);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    private void resetTableSize() {
        // Standardowe wymiary dla tabel z danymi
        view.jScrollPane1.setPreferredSize(new java.awt.Dimension(900, 600));

        // Wywołujemy pack(), jeśli chcemy, aby całe okno MainWindow dopasowało się do nowej tabeli
        // Jeśli okno ma mieć stały rozmiar, użyj tylko revalidate()
        view.pack();
        view.revalidate();
        view.repaint();
      
    }

}
