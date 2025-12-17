package Controllers;

import Config.HibernateUtil;
import Models.*;
import Views.DataUpdateWindow;
import Views.MainWindow;
import Utils.*;
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
 * Główny Kontroler aplikacji.
 * Zarządza przełączaniem widoków, operacjami CRUD oraz relacjami Klient-Aktywność.
 */
public class MainController implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    private final SessionFactory sessionFactory;
    private final MainWindow view;
    
    private final ClientDAO clientDAO = new ClientDAO();
    private final TrainerDAO trainerDAO = new TrainerDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();
    
    private String currentView = "Init";

    private final ClientControllerTable clientControllerTable;
    private final TrainerControllerTable trainerControllerTable;
    private final ActivityControllerTable activityControllerTable;

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

    private void addListeners() {
        view.addClientMenuListener(this);
        view.addTrainerMenuListener(this);
        view.addActivitiesMenuListener(this);
        view.addInitMenuListener(this);

        view.addNowyListener(new ActionListenerForAddButton());
        view.addUsunListener(new ActionListenerForUsunButton());
        view.addAktualizujListener(new ActionListenerForUpdateButton());
    }

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
     * Ustawia renderer dla ComboBoxa i wypełnia go danymi.
     * Dzięki temu użytkownik widzi Nazwę, a my operujemy na Obiekcie.
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
            if (session != null) session.close();
        }
    }

    /**
     * Przenosi Imię i Nazwisko wybranego klienta do pola tekstowego połączenia.
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
     * Obsługa przycisku "Dodaj Aktywność" (relacja Many-to-Many).
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
            if (tr != null) tr.rollback();
            LOGGER.log(Level.SEVERE, "Błąd zapisu relacji", ex);
            JOptionPane.showMessageDialog(view, "Błąd zapisu: " + ex.getMessage());
        } finally {
            if (session != null) session.close();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "ShowClients" -> {
                clientControllerTable.showClients();
                view.setButtonLabels("Dodaj Klienta", "Usuń Klienta", "Edytuj Klienta");
                currentView = "Client";
                updatePanelVisibility(true, false, true);
            }
            case "ShowTrainers" -> {
                trainerControllerTable.showTrainers();
                view.setButtonLabels("Dodaj Trenera", "Usuń Trenera", "Edytuj Trenera");
                currentView = "Trainer";
                updatePanelVisibility(true, false, false);
            }
            case "ShowActivities" -> {
                activityControllerTable.showActivities();
                view.setButtonLabels("Dodaj Aktywność", "Usuń Aktywność", "Edytuj Aktywność");
                currentView = "Activity";
                updatePanelVisibility(true, true, false);
            }
            case "ShowInit" -> showInit();
        }
    }

    private void updatePanelVisibility(boolean crud, boolean calc, boolean enroll) {
        view.jNowy.setVisible(crud);
        view.jUsun.setVisible(crud);
        view.jAktualizuj.setVisible(crud);
        view.jPanelSearch.setVisible(crud);
        view.jPanelCalculate.setVisible(calc);
        view.jPanelClientToActivity.setVisible(enroll);
    }

    private void showInit() {
        view.setTableData(new String[]{"Info"}, new Object[][]{{"Witaj w systemie ISDD. Wybierz opcję z menu."}});
        updatePanelVisibility(false, false, false);
        currentView = "Init";
    }

    // --- LOGIKA FORMULARZY ---
    private void handleFormAction(Object entity, String type) {
        DataUpdateWindow form = new DataUpdateWindow();
        Object ctrl = null;
        boolean isAdding = (entity == null);
        
        switch (type) {
            case "Client" -> ctrl = new ClientDataController(sessionFactory, form, clientControllerTable, (Client) entity);
            case "Trainer" -> ctrl = new TrainerDataController(sessionFactory, form, trainerControllerTable, (Trainer) entity);
            case "Activity" -> ctrl = new ActivityDataController(sessionFactory, form, activityControllerTable, (Activity) entity);
        }

        if (ctrl instanceof ClientDataController c) c.initializeForm();
        else if (ctrl instanceof TrainerDataController t) t.initializeForm();
        else if (ctrl instanceof ActivityDataController a) a.initializeForm();
        
        form.setDefaultCloseOperation(DataUpdateWindow.DISPOSE_ON_CLOSE);
        form.setVisible(true);
    }

    // --- KLASY WEWNĘTRZNE DLA PRZYCISKÓW ---
    private class ActionListenerForAddButton implements ActionListener {
        public void actionPerformed(ActionEvent e) { handleFormAction(null, currentView); }
    }

    private class ActionListenerForUpdateButton implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            Object entity = switch (currentView) {
                case "Client" -> clientControllerTable.getSelectedClient();
                case "Trainer" -> trainerControllerTable.getSelectedTrainer();
                case "Activity" -> activityControllerTable.getSelectedActivity();
                default -> null;
            };
            if (entity != null) handleFormAction(entity, currentView);
            else JOptionPane.showMessageDialog(view, "Proszę zaznaczyć element do edycji.");
        }
    }

    private class ActionListenerForUsunButton implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String code = switch (currentView) {
                case "Client" -> view.getSelectedClientCode();
                case "Trainer" -> view.getSelectedTrainerCode();
                case "Activity" -> view.getSelectedActivityCode();
                default -> null;
            };
            if (code != null) {
                int confirm = JOptionPane.showConfirmDialog(view, "Czy na pewno usunąć: " + code + "?");
                if (confirm == JOptionPane.YES_OPTION) deleteEntity(currentView, code);
            }
        }
    }

    private void deleteEntity(String type, String code) {
        Session session = null;
        Transaction tr = null;
        try {
            session = sessionFactory.openSession();
            tr = session.beginTransaction();
            boolean success = switch (type) {
                case "Client" -> clientDAO.deleteClientByMemberNumber(session, code);
                case "Trainer" -> trainerDAO.deleteTrainerById(session, code);
                case "Activity" -> activityDAO.deleteActivityById(session, code);
                default -> false;
            };
            if (success) {
                tr.commit();
                if ("Client".equals(type)) clientControllerTable.showClients();
                else if ("Trainer".equals(type)) trainerControllerTable.showTrainers();
                else activityControllerTable.showActivities();
            }
        } catch (Exception ex) {
            if (tr != null) tr.rollback();
            LOGGER.log(Level.SEVERE, "Błąd usuwania", ex);
        } finally {
            if (session != null) session.close();
        }
    }
}