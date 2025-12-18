package ControllersCMD;

import Models.Activity;
import Models.ActivityDAO;
import Models.Client;
import ViewsCMD.MessageView;
import ViewsCMD.ClientView;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Scanner;
import java.util.Set;

/**
 * Kontroler zarządzający operacjami związanymi z aktywnościami (zajęciami).
 * Klasa pośredniczy między warstwą modelu (ActivityDAO) a widokiem (MessageView, ClientView),
 * realizując logikę biznesową dotyczącą przeglądania uczestników zajęć.
 */
public class ActivityController {

    /** Fabryka sesji Hibernate używana do komunikacji z bazą danych. */
    private final SessionFactory sessionFactory;
    
    /** Obiekt dostępu do danych (DAO) dla encji Activity. */
    private final ActivityDAO aDAO;
    
    /** Widok odpowiedzialny za wyświetlanie komunikatów systemowych. */
    private final MessageView vMessages;
    
    /** Widok odpowiedzialny za prezentację danych klientów. */
    private final ClientView vClient;

    /**
     * Konstruktor inicjalizujący kontroler aktywności.
     * Automatycznie tworzy niezbędne obiekty DAO i widoków oraz uruchamia menu użytkownika.
     * * @param sessionFactory Fabryka sesji Hibernate przekazana z głównego kontrolera.
     */
    public ActivityController(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.aDAO = new ActivityDAO();
        this.vMessages = new MessageView();
        this.vClient = new ClientView();
        this.menu();
    }

    /**
     * Wyświetla menu tekstowe konsoli i obsługuje interakcję z użytkownikiem.
     * Pozwala na wybór funkcji listowania członków lub wyjście z podmenu.
     */
    private void menu() {
        Scanner keyboard = new Scanner(System.in);
        String option;
        do {
            vMessages.consoleMessage("INFO", "Activity menu:\n1 - List members of activity\n2 - Exit");
            option = keyboard.nextLine();
            switch (option) {
                case "1" -> listMembersByActivityID();
                case "2" -> vMessages.consoleMessage("INFO", "Exiting activity menu...");
                default -> vMessages.consoleMessage("WARNING", "Invalid option. Try again.");
            }
        } while (!option.equals("2"));
    }

    /**
     * Pobiera od użytkownika ID aktywności i wyświetla listę przypisanych do niej klientów.
     * Metoda zarządza sesją Hibernate, przeprowadza walidację istnienia aktywności
     * oraz obsługuje ewentualne błędy transakcji (rollback).
     */
    private void listMembersByActivityID() {
        Session session = null;
        Transaction tr = null;
        Scanner keyboard = new Scanner(System.in);
        try {
            session = sessionFactory.openSession();
            tr = session.beginTransaction();

            System.out.print("Enter activity ID: ");
            String aId = keyboard.nextLine();

            // Sprawdzenie czy aktywność istnieje w bazie
            boolean exists = aDAO.existAId(session, aId);
            if (!exists) {
                vMessages.consoleMessage("ERROR", "Activity ID not found.");
                return;
            }

            // Pobranie obiektu aktywności
            Activity activity = aDAO.findActivityById(session, aId);
            if (activity == null) {
                vMessages.consoleMessage("ERROR", "Activity retrieval failed.");
                return;
            }

            // Pobranie i wyświetlenie listy klientów
            Set<Client> clients = activity.getClientSet();
            if (clients == null || clients.isEmpty()) {
                vMessages.consoleMessage("INFO", "No members enrolled in this activity.");
            } else {
                vMessages.consoleMessage("INFO", "Members of activity " + aId + ":");
                for (Client c : clients) {
                    if (c != null) {
                        vClient.showClientDetails(c);
                    }
                }
            }

            tr.commit();
        } catch (Exception e) {
            // W przypadku błędu wycofaj zmiany
            if (tr != null && tr.isActive()) tr.rollback();
            vMessages.consoleMessage("ERROR", "An error occurred: " + e.getMessage());
        } finally {
            // Zawsze zamknij sesję
            if (session != null && session.isOpen()) session.close();
        }
    }
}