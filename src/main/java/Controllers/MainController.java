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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * Główny Kontroler aplikacji. Zarządza przełączaniem widoków tabelarycznych
 * oraz koordynuje operacje CRUD (dodawanie, edycja, usuwanie)
 * dla encji Client, Trainer i Activity.
 */
public class MainController implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(MainController.class.getName());

    private final SessionFactory sessionFactory;
    private final MainWindow view;
    
    // Obiekty DAO używane do operacji usuwania
    private final ClientDAO clientDAO = new ClientDAO();
    private final TrainerDAO trainerDAO = new TrainerDAO();
    private final ActivityDAO activityDAO = new ActivityDAO();
    
    private String currentView = "Init";

    // Kontrolery podrzędne zarządzające widokami tabelarycznymi
    private final ClientControllerTable clientControllerTable;
    private final TrainerControllerTable trainerControllerTable;
    private final ActivityControllerTable activityControllerTable;

    // =========================================================================
    // KONSTRUKTOR I INICJALIZACJA
    // =========================================================================
    public MainController(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.view = new MainWindow();

        // Inicjalizacja kontrolerów podrzędnych
        this.clientControllerTable = new ClientControllerTable(sessionFactory, view);
        this.trainerControllerTable = new TrainerControllerTable(sessionFactory, view);
        this.activityControllerTable = new ActivityControllerTable(sessionFactory, view);

        addListeners();
        addWindowCloseListener();

        showInit(); // Pokaż widok początkowy
        view.setVisible(true);
        LOGGER.info("Główne okno aplikacji uruchomione.");

        setCrudButtonsVisible(false);
    }

    // =========================================================================
    // OBSŁUGA LISTENERÓW (PODPINANIE AKCJI)
    // =========================================================================
    private void addListeners() {
        // Listenery dla pozycji Menu (zmiana widoku tabeli)
        view.addClientMenuListener(this);
        view.addTrainerMenuListener(this);
        view.addActivitiesMenuListener(this);
        view.addInitMenuListener(this);

        // Listenery dla przycisków CRUD (dedykowane wewnętrzne klasy)
        view.addNowyListener(new ActionListenerForAddButton());
        view.addUsunListener(new ActionListenerForUsunButton());
        view.addAktualizujListener(new ActionListenerForUpdateButton());
    }

    /**
     * Zapewnia czyste zamknięcie zasobów Hibernate przy zamknięciu okna.
     */
    private void addWindowCloseListener() {
        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                LOGGER.info("Zamykanie okna. Zamykanie SessionFactory.");
                HibernateUtil.close(); // Czyści zasoby DB
                System.exit(0);
            }
        });
    }

    // =========================================================================
    // ACTION PERFORMED (OBSŁUGA MENU) - ZMIANA WIDOKU
    // =========================================================================
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "ShowClients" -> {
                clientControllerTable.showClients();
                view.setButtonLabels("Dodaj Klienta", "Usuń Klienta", "Edytuj Klienta");
                setCrudButtonsVisible(true);
                currentView = "Client";
                LOGGER.info("Przełączono na widok: Klienci.");
            }
            case "ShowTrainers" -> {
                trainerControllerTable.showTrainers();
                view.setButtonLabels("Dodaj Trenera", "Usuń Trenera", "Edytuj Trenera");
                setCrudButtonsVisible(true);
                currentView = "Trainer";
                LOGGER.info("Przełączono na widok: Trenerzy.");
            }
            case "ShowActivities" -> {
                activityControllerTable.showActivities();
                view.setButtonLabels("Dodaj Aktywność", "Usuń Aktywność", "Edytuj Aktywność");
                setCrudButtonsVisible(true);
                currentView = "Activity";
                LOGGER.info("Przełączono na widok: Aktywności.");
            }
            case "ShowInit" -> {
                showInit();
                view.setButtonLabels("Nowy", "Usuń", "Aktualizuj"); // Reset etykiet
                currentView = "Init";
                LOGGER.info("Przełączono na widok: Początkowy.");
            }
        }
    }

    // =========================================================================
    // LOGIKA WIDOKU I STEROWANIE WIDOCZNOŚCIĄ
    // =========================================================================
    private void showInit() {
        view.setTableData(new String[]{"Info"}, new Object[][]{{"Witaj w systemie zarządzania ISDD"}});
        setCrudButtonsVisible(false);
    }

    private void setCrudButtonsVisible(boolean visible) {
        view.jNowy.setVisible(visible);
        view.jUsun.setVisible(visible);
        view.jAktualizuj.setVisible(visible);
        view.jSearchText.setVisible(visible);
        view.jSearchBox.setVisible(visible);
    }

    // =========================================================================
    // LOGIKA OBSŁUGI FORMULARZY (DODAWANIE I EDYCJA) - DELEGACJA
    // =========================================================================
    /**
     * Ujednolicona metoda otwierająca formularz dodawania/edycji.
     * Wybiera odpowiedni kontroler danych w zależności od typu encji.
     *
     * @param entity Encja do edycji lub null dla trybu dodawania.
     * @param type Typ encji ("Client", "Trainer", "Activity")
     */
    private void handleFormAction(Object entity, String type) {
        
        DataUpdateWindow form = new DataUpdateWindow();
        Object formController = null;
        String title;
        
        boolean isAdding = (entity == null);
        
        // Określenie i utworzenie odpowiedniego kontrolera formularza
        switch (type) {
            case "Client" -> {
                Client client = isAdding ? null : (Client) entity;
                formController = new ClientDataController(sessionFactory, form, clientControllerTable, client);
                title = isAdding ? "Dodawanie Nowego Klienta" : "Edycja Klienta: " + client.getMNum();
            }
            case "Trainer" -> {
                Trainer trainer = isAdding ? null : (Trainer) entity;
                formController = new TrainerDataController(sessionFactory, form, trainerControllerTable, trainer);
                title = isAdding ? "Dodawanie Nowego Trenera" : "Edycja Trenera: " + trainer.getTCod();
            }
            case "Activity" -> {
                Activity activity = isAdding ? null : (Activity) entity;
                formController = new ActivityDataController(sessionFactory, form, activityControllerTable, activity);
                title = isAdding ? "Dodawanie Nowej Aktywności" : "Edycja Aktywności: " + activity.getAId();
            }
            default -> {
                 JOptionPane.showMessageDialog(view, "Nieznany typ encji do obsługi formularza.", "Błąd", JOptionPane.ERROR_MESSAGE);
                 return;
            }
        }

        // Musimy uruchomić logikę inicjalizacji w nowym kontrolerze
        // Użycie instancjowania (instanceof) z rzutowaniem jest poprawnym wzorcem w Java 16+
        if (formController instanceof ClientDataController clientCtrl) {
            clientCtrl.initializeForm();
        } else if (formController instanceof TrainerDataController trainerCtrl) {
            trainerCtrl.initializeForm();
        } else if (formController instanceof ActivityDataController activityCtrl) {
            activityCtrl.initializeForm();
        }
        
        form.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        form.setTitle(title);
        form.setVisible(true);
    }

    // =========================================================================
    // LISTENERY CRUD (WŁAŚCIWA LOGIKA BIZNESOWA)
    // =========================================================================

    /** Obsługa przycisku DODAJ (NOWY). */
    private class ActionListenerForAddButton implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            LOGGER.fine("Kliknięto przycisk DODAJ dla widoku: " + currentView);
            switch (currentView) {
                case "Client" -> handleFormAction(null, "Client");
                case "Trainer" -> handleFormAction(null, "Trainer");
                case "Activity" -> handleFormAction(null, "Activity");
                default -> JOptionPane.showMessageDialog(view, "Nie można dodać elementu w widoku początkowym.", "Błąd", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    /** Obsługa przycisku AKTUALIZUJ (EDYTUJ). */
    private class ActionListenerForUpdateButton implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Object entityToEdit = null;
            String warningMessage = null;

            switch (currentView) {
                case "Client" -> {
                    entityToEdit = clientControllerTable.getSelectedClient();
                    warningMessage = "Proszę zaznaczyć klienta do edycji.";
                }
                case "Trainer" -> {
                    entityToEdit = trainerControllerTable.getSelectedTrainer(); 
                    warningMessage = "Proszę zaznaczyć trenera do edycji.";
                }
                case "Activity" -> {
                    entityToEdit = activityControllerTable.getSelectedActivity(); 
                    warningMessage = "Proszę zaznaczyć aktywność do edycji.";
                }
                default -> {
                    JOptionPane.showMessageDialog(view, "Nie można edytować elementu w widoku początkowym.", "Błąd", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            if (entityToEdit == null) {
                JOptionPane.showMessageDialog(view, warningMessage, "Brak zaznaczenia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Przechodzi do ujednoliconej logiki otwierania formularza w trybie edycji
            handleFormAction(entityToEdit, currentView);
        }
    }

    /** Obsługa przycisku USUŃ. */
    private class ActionListenerForUsunButton implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            
            String codeToDelete = null; 
            String entityName = null;
            Object entityController = null; // Do odświeżenia

            switch (currentView) {
                case "Client" -> {
                    codeToDelete = view.getSelectedClientCode();
                    entityName = "Klient";
                    entityController = clientControllerTable;
                }
                case "Trainer" -> {
                    codeToDelete = view.getSelectedTrainerCode(); 
                    entityName = "Trener";
                    entityController = trainerControllerTable;
                }
                case "Activity" -> {
                    codeToDelete = view.getSelectedActivityCode(); 
                    entityName = "Aktywność";
                    entityController = activityControllerTable;
                }
                default -> {
                    JOptionPane.showMessageDialog(view, "Nie można usunąć elementu w widoku początkowym.", "Błąd", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
            
            if (codeToDelete == null || codeToDelete.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Proszę zaznaczyć " + entityName.toLowerCase() + " do usunięcia.", "Brak zaznaczenia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Potwierdzenie usunięcia
            int confirm = JOptionPane.showConfirmDialog(view,
                "Czy na pewno chcesz usunąć " + entityName.toLowerCase() + " o kodzie: " + codeToDelete + "?\nTej operacji nie można cofnąć.",
                "Potwierdzenie usunięcia", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                deleteEntity(currentView, codeToDelete, entityController);
            }
        }
    }

    /** * Wyodrębniona metoda dla ujednoliconej logiki usuwania encji z bazy.
     */
    private void deleteEntity(String entityType, String codeToDelete, Object controller) {
        Session session = null;
        Transaction tr = null;
        boolean deleted = false;
        String entityName = switch (entityType) {
            case "Client" -> "Klient";
            case "Trainer" -> "Trener";
            case "Activity" -> "Aktywność";
            default -> "Encja";
        };

        try {
            session = sessionFactory.openSession();
            tr = session.beginTransaction();

            // Wywołanie odpowiedniej metody DAO w zależności od typu
            deleted = switch (entityType) {
                case "Client" -> clientDAO.deleteClientByMemberNumber(session, codeToDelete);
                case "Trainer" -> trainerDAO.deleteTrainerById(session, codeToDelete); 
                case "Activity" -> activityDAO.deleteActivityById(session, codeToDelete); 
                default -> false;
            };

            if (deleted) {
                tr.commit();
                JOptionPane.showMessageDialog(view, entityName + " " + codeToDelete + " został(a) pomyślnie usunięty(a).", "Sukces", JOptionPane.INFORMATION_MESSAGE);

                // Odświeżenie tabeli
                if (controller instanceof ClientControllerTable clientCtrl) {
                    clientCtrl.showClients();
                } else if (controller instanceof TrainerControllerTable trainerCtrl) {
                    trainerCtrl.showTrainers();
                } else if (controller instanceof ActivityControllerTable activityCtrl) {
                    activityCtrl.showActivities();
                }

            } else {
                tr.rollback();
                JOptionPane.showMessageDialog(view, "Błąd: Nie znaleziono " + entityName.toLowerCase() + " o kodzie " + codeToDelete + " w bazie lub wystąpił inny błąd.", "Błąd usuwania", JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception ex) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            LOGGER.log(Level.SEVERE, "Błąd bazy danych podczas usuwania encji: " + entityType + " (" + codeToDelete + ")", ex);
            JOptionPane.showMessageDialog(view, "Błąd bazy danych podczas usuwania: " + ex.getMessage(), "Błąd DB", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
}