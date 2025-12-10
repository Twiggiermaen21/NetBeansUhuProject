package Controllers;

import Views.MainWindow;
import Config.HibernateUtil;
import Models.Client;
import org.hibernate.SessionFactory;
import Utils.*;
import Models.ClientDAO;
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
            }
            case "ShowTrainers" -> {
                trainerControllerTable.showTrainers();
                setCrudButtonsVisible(true);
            }
            case "ShowActivities" -> {
                activityControllerTable.showActivities();
                setCrudButtonsVisible(true);
            }
            case "ShowInit" -> showInit();
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
     * @param client Klient do edycji lub null dla trybu dodawania.
     */
    private void handleFormAction(Client client) {
        DataUpdateWindow form = new DataUpdateWindow();
        
        // Tworzenie Kontrolera Formularza (uniwersalny kontroler)
        ClientDataController formController = new ClientDataController(
            sessionFactory, 
            form, 
            clientControllerTable, 
            client // Jeśli null -> dodawanie; Jeśli obiekt -> edycja
        );

        // Inicjalizacja formularza (wypełnienie danych lub generacja kodu)
        formController.initializeForm(); 
        
        form.setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        
        String title = (client == null) ? "Dodawanie Nowego Klienta" : "Edycja Klienta: " + client.getMNum();
        form.setTitle(title);
        
        form.setVisible(true);
    }

    // =========================================================================
    // LISTENERY CRUD (WŁAŚCIWA LOGIKA BIZNESOWA)
    // =========================================================================

    private class ActionListenerForAddButton implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // TRYB DODAWANIA: ZAWSZE przekazujemy NULL do uniwersalnego kontrolera
            handleFormAction(null); 
        }
    }
    
    // DEDYKOWANY LISTENER DLA AKTUALIZACJI
    private class ActionListenerForUpdateButton implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            
            Client client = clientControllerTable.getSelectedClient(); 

            if (client == null) {
                JOptionPane.showMessageDialog(view, "Proszę zaznaczyć klienta do edycji.", "Brak zaznaczenia", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Przechodzi do logiki otwierania formularza w trybie edycji
            handleFormAction(client);
        }
    }
    
    private class ActionListenerForUsunButton implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            
            String mNumToDelete = view.getSelectedClientCode(); 
            
            if (mNumToDelete == null || mNumToDelete.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Proszę zaznaczyć klienta do usunięcia.", "Brak zaznaczenia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(view, 
                "Czy na pewno chcesz usunąć klienta o kodzie: " + mNumToDelete + "?\nTej operacji nie można cofnąć.", 
                "Potwierdzenie usunięcia", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                Session session = null;
                Transaction tr = null;
                try {
                    session = sessionFactory.openSession();
                    tr = session.beginTransaction();
                    
                    // Wykonanie usunięcia przez DAO
                    boolean deleted = clientDAO.deleteClientByMemberNumber(session, mNumToDelete);

                    if (deleted) {
                        tr.commit();
                        JOptionPane.showMessageDialog(view, "Klient " + mNumToDelete + " został pomyślnie usunięty.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                        
                        // Odświeżenie tabeli
                        clientControllerTable.showClients(); 
                    } else {
                        tr.rollback();
                        JOptionPane.showMessageDialog(view, "Błąd: Nie znaleziono klienta o kodzie " + mNumToDelete + " w bazie.", "Błąd usuwania", JOptionPane.ERROR_MESSAGE);
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
    }
}