package Controllers;

import Views.MainWindow;
import Config.HibernateUtil;
import Models.Activity;
import Models.ActivityDAO;
import Models.Client;
import org.hibernate.SessionFactory;
import Utils.*;
import Models.ClientDAO;
import Models.Trainer;
import Models.TrainerDAO;
import Views.DataUpdateWindow;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JOptionPane;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class MainController implements ActionListener {

    private final SessionFactory sessionFactory;
    private final MainWindow view;
    private final ClientDAO clientDAO = new ClientDAO();
    private final TrainerDAO trainerDAO = new TrainerDAO(); // <--- DODANO
    private final ActivityDAO activityDAO = new ActivityDAO();
    private String currentView = "Init";
    // Kontrolery widoków tabelarycznych
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

        showInit();
        view.setVisible(true);

        setCrudButtonsVisible(false);
    }

    // =========================================================================
    // OBSŁUGA LISTENERÓW (PODPINANIE AKCJI)
    // =========================================================================
    private void addListeners() {
        // Listenery dla pozycji Menu (obsługiwane przez actionPerformed(this))
        view.addClientMenuListener(this);
        view.addTrainerMenuListener(this);
        view.addActivitiesMenuListener(this);
        view.addInitMenuListener(this);

        // Listenery dla przycisków CRUD (dedykowane wewnętrzne klasy)
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

    // =========================================================================
    // ACTION PERFORMED (OBSŁUGA MENU)
    // =========================================================================
    @Override
    public void actionPerformed(ActionEvent e) {
        switch (e.getActionCommand()) {
            case "ShowClients" -> {
                clientControllerTable.showClients();
                setCrudButtonsVisible(true);
                currentView = "Client"; // <--- DODANO
            }
            case "ShowTrainers" -> {
                trainerControllerTable.showTrainers();
                setCrudButtonsVisible(true);
                currentView = "Trainer"; // <--- DODANO
            }
            case "ShowActivities" -> {
                activityControllerTable.showActivities();
                setCrudButtonsVisible(true);
                currentView = "Activity"; // <--- DODANO
            }
            case "ShowInit" -> {
                showInit();
                currentView = "Init"; // <--- DODANO
            }
        }

    }

    // =========================================================================
    // LOGIKA WIDOKU
    // =========================================================================
    private void showInit() {
        view.setTableData(new String[]{"Info"}, new Object[][]{{"Welcome to ISDD Project"}});
        setCrudButtonsVisible(false);
    }

    private void setCrudButtonsVisible(boolean visible) {
        view.jNowy.setVisible(visible);
        view.jUsun.setVisible(visible);
        view.jAktualizuj.setVisible(visible);
    }

    // =========================================================================
    // LOGIKA OBSŁUGI FORMULARZY (DODAWANIE I EDYCJA)
    // =========================================================================
    /**
     * Ujednolicona metoda otwierająca formularz dodawania/edycji.
     *
     * @param client Klient do edycji lub null dla trybu dodawania.
     */
   private void handleFormAction(Object entity) {
        
        DataUpdateWindow form = new DataUpdateWindow();
        Object formController = null;
        String title;
        
        // Określenie typu formularza i kontrolera
        if (entity == null || entity instanceof Client) {
            // Logika dla Klienta (Dodawanie - null, lub Edycja - Client)
            Client client = (Client) entity;
            formController = new ClientDataController(
                sessionFactory, form, clientControllerTable, client
            );
            title = (client == null) ? "Dodawanie Nowego Klienta" : "Edycja Klienta: " + client.getMNum();
            
        } else if (entity instanceof Trainer) {
            // Logika dla Trenera
            Trainer trainer = (Trainer) entity;
            formController = new TrainerDataController(
                sessionFactory, form, trainerControllerTable, trainer
            );
            title = (trainer == null) ? "Dodawanie Nowego Trenera" : "Edycja Trenera: " + trainer.getTCod();
            
        } else if (entity instanceof Activity) {
            // Logika dla Aktywności
            Activity activity = (Activity) entity;
            formController = new ActivityDataController(
                sessionFactory, form, activityControllerTable, activity
            );
            title = (activity == null) ? "Dodawanie Nowej Aktywności" : "Edycja Aktywności: " + activity.getAId();
            
        } else {
             // Jeśli nie jest to obsługiwany typ
            JOptionPane.showMessageDialog(view, "Nieznany typ encji do obsługi formularza.", "Błąd", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Musimy założyć, że wszystkie kontrolery formularzy (ClientDataController, 
        // TrainerDataController, ActivityDataController) implementują interfejs 
        // lub mają metodę `initializeForm()`
        
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
   private class ActionListenerForAddButton implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            switch (currentView) {
                case "Client" -> handleFormAction(null); // NULL dla dodawania Klienta
                case "Trainer" -> handleFormAction(null); // NULL dla dodawania Trenera
                case "Activity" -> handleFormAction(null); // NULL dla dodawania Aktywności
                default -> JOptionPane.showMessageDialog(view, "Nie można dodać elementu w widoku początkowym.", "Błąd", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    

    // DEDYKOWANY LISTENER DLA AKTUALIZACJI
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
                    entityToEdit = trainerControllerTable.getSelectedTrainer(); // Zakładam istnienie tej metody
                    warningMessage = "Proszę zaznaczyć trenera do edycji.";
                }
                case "Activity" -> {
                    entityToEdit = activityControllerTable.getSelectedActivity(); // Zakładam istnienie tej metody
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
            handleFormAction(entityToEdit);
        }
    }

   private class ActionListenerForUsunButton implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            
            String codeToDelete = null; // Kod/ID do usunięcia
            String entityName = null;
            Object entityController = null;

            switch (currentView) {
                case "Client" -> {
                    codeToDelete = view.getSelectedClientCode();
                    entityName = "Klient";
                    entityController = clientControllerTable;
                }
                case "Trainer" -> {
                    codeToDelete = view.getSelectedTrainerCode(); // Zakładam istnienie tej metody
                    entityName = "Trener";
                    entityController = trainerControllerTable;
                }
                case "Activity" -> {
                    codeToDelete = view.getSelectedActivityCode(); // Zakładam istnienie tej metody
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

            // Ujednolicone okno potwierdzenia
            int confirm = JOptionPane.showConfirmDialog(view,
                "Czy na pewno chcesz usunąć " + entityName.toLowerCase() + " o kodzie: " + codeToDelete + "?\nTej operacji nie można cofnąć.",
                "Potwierdzenie usunięcia", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                // Ujednolicone wywołanie metody usuwającej
                deleteEntity(currentView, codeToDelete, entityController);
            }
        }
    }

    // Wyodrębniona metoda dla logiki usuwania klienta
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
                case "Trainer" -> trainerDAO.deleteTrainerById(session, codeToDelete); // Zakładam istnienie tej metody
                case "Activity" -> activityDAO.deleteActivityById(session, codeToDelete); // Zakładam istnienie tej metody
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
            JOptionPane.showMessageDialog(view, "Błąd bazy danych podczas usuwania: " + ex.getMessage(), "Błąd DB", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
}
