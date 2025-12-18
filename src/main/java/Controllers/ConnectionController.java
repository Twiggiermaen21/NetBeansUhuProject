package Controllers;

import Config.HibernateUtil;
import Views.ConnectionView;
import ViewsCMD.MessageView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.hibernate.SessionFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Główny kontroler odpowiedzialny za nawiązanie początkowego połączenia z bazą danych
 * za pomocą Hibernate i uruchomienie reszty aplikacji.
 * Klasa implementuje interfejs {@link ActionListener}, dzięki czemu może obsługiwać
 * zdarzenia generowane przez interfejs graficzny użytkownika (okno logowania).
 */
public class ConnectionController implements ActionListener {

    /** Obiekt rejestrujący logi systemowe dla klasy ConnectionController. */
    private static final Logger LOGGER = Logger.getLogger(ConnectionController.class.getName());

    /** Widok okna połączenia zawierający pola użytkownika i hasła. */
    private final ConnectionView view;
    
    /** Widok pomocniczy służący do wyświetlania wyskakujących okien z komunikatami. */
    private final MessageView messageView;

    /**
     * Konstruktor kontrolera. Inicjalizuje widoki, rejestruje słuchacze zdarzeń
     * oraz sprawia, że okno logowania staje się widoczne dla użytkownika.
     */
    public ConnectionController() {
        this.view = new ConnectionView();
        this.messageView = new MessageView();

        addListeners();
        // Uruchomienie okna logowania
        this.view.setVisible(true);
    }

    /**
     * Rejestruje bieżący kontroler jako słuchacza zdarzeń dla przycisków
     * "Połącz" oraz "Anuluj" znajdujących się w widoku.
     */
    private void addListeners() {
        view.addConnectListener(this);
        view.addCancelListener(this);
    }

    /**
     * Główna metoda obsługująca akcje użytkownika. Rozpoznaje przesłaną komendę
     * i uruchamia proces logowania lub zamyka aplikację.
     * * @param e Obiekt zdarzenia akcji zawierający polecenie (ActionCommand).
     */
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
     * Zamyka aplikację w bezpieczny sposób. Przed zakończeniem procesu
     * niszczy fabrykę sesji Hibernate, aby zwolnić zasoby i zamknąć połączenia.
     */
    private void exitApplication() {
        HibernateUtil.close(); 
        LOGGER.info("Aplikacja została zamknięta.");
        System.exit(0);
    }

    /**
     * Metoda odpowiedzialna za pobranie poświadczeń z widoku i próbę nawiązania
     * połączenia z bazą danych za pomocą {@link HibernateUtil}.
     * W przypadku powodzenia zamyka okno logowania i przekazuje fabrykę sesji
     * do głównego kontrolera aplikacji (MainController).
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
            LOGGER.log(Level.WARNING, "Połączenie nieudane dla użytkownika: {0}. Sprawdź dane uwierzytelniające.", user);
            messageView.showError("Połączenie nieudane! Sprawdź dane uwierzytelniające.");
        }
    }
}