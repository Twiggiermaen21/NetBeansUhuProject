package Controllers;

import Config.HibernateUtil;
import Views.ConnectionView;
import Views.MessageView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.hibernate.SessionFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Główny kontroler odpowiedzialny za nawiązanie początkowego połączenia z bazą danych
 * za pomocą Hibernate i uruchomienie reszty aplikacji.
 */
public class ConnectionController implements ActionListener {

    private static final Logger LOGGER = Logger.getLogger(ConnectionController.class.getName());

    private final ConnectionView view;
    private final MessageView messageView;

    // =========================================================================
    // KONSTRUKTOR
    // =========================================================================

    public ConnectionController() {
        this.view = new ConnectionView();
        this.messageView = new MessageView();

        addListeners();
        // Uruchomienie okna logowania
        this.view.setVisible(true);
    }

    /**
     * Rejestracja kontrolera jako słuchacza zdarzeń dla przycisków w widoku.
     */
    private void addListeners() {
        view.addConnectListener(this);
        view.addCancelListener(this);
    }

    // =========================================================================
    // OBSŁUGA ZDARZEŃ (ActionListener)
    // =========================================================================

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "AppEntry":
                appEntry(); // Próba połączenia
                break;
            case "Exit":
                exitApplication(); // Zamknięcie aplikacji
                break;
            default:
                LOGGER.warning("Nieznane polecenie: " + command);
                break;
        }

    }
    
    /**
     * Czyści zasoby i zamyka aplikację.
     */
    private void exitApplication() {
        // Dodatkowe zamknięcie SessionFactory, jeśli została utworzona,
        // ale aplikacja jest zamykana przed MainController
        HibernateUtil.close(); 
        LOGGER.info("Aplikacja została zamknięta.");
        System.exit(0);
    }

    // =========================================================================
    // LOGIKA POŁĄCZENIA
    // =========================================================================

    /**
     * Metoda odpowiedzialna za pobranie danych z widoku i próbę nawiązania
     * połączenia z bazą danych za pomocą HibernateUtil.
     */
    public void appEntry() {
        String user = view.getUsername();
        String pass = new String(view.getPassword());
        
        LOGGER.info("Próba połączenia z bazą danych dla użytkownika: " + user);

        // 1. Próba budowania SessionFactory
        SessionFactory sessionFactory = HibernateUtil.buildSessionFactory(user, pass);

        if (sessionFactory != null) {
            // 2. Sukces
            LOGGER.info("Połączenie z bazą danych powiodło się.");
            messageView.showSuccess("Połączenie udane!");
            view.dispose();
            
            // 3. Uruchomienie głównego kontrolera aplikacji
            new MainController(sessionFactory);
            
        } else {
            // 2. Błąd
            // Komunikat błędu został już wyświetlony przez HibernateUtil
            LOGGER.log(Level.WARNING, "Połączenie nieudane dla użytkownika: {0}. Sprawdź dane uwierzytelniające.", user);
            messageView.showError("Połączenie nieudane! Sprawdź dane uwierzytelniające.");
        }
    }

}