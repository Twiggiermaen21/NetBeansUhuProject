package Controllers;

import Models.Activity;
import Models.ActivityDAO;
import Models.Client;
import Views.MessageView;
import Views.ClientView;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.Scanner;
import java.util.Set;

public class ActivityController {

    private final SessionFactory sessionFactory;
    private final ActivityDAO aDAO;
    private final MessageView vMessages;
    private final ClientView vClient;

    public ActivityController(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.aDAO = new ActivityDAO();
        this.vMessages = new MessageView();
        this.vClient = new ClientView();
        this.menu();
    }

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

    private void listMembersByActivityID() {
        Session session = null;
        Transaction tr = null;
        Scanner keyboard = new Scanner(System.in);
        try {
            session = sessionFactory.openSession();
            tr = session.beginTransaction();

            System.out.print("Enter activity ID: ");
            String aId = keyboard.nextLine();

            boolean exists = aDAO.existAId(session, aId);
            if (!exists) {
                vMessages.consoleMessage("ERROR", "Activity ID not found.");
                return;
            }

            Activity activity = aDAO.findActivityById(session, aId);
            if (activity == null) {
                vMessages.consoleMessage("ERROR", "Activity retrieval failed.");
                return;
            }

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
            if (tr != null && tr.isActive()) tr.rollback();
            vMessages.consoleMessage("ERROR", "An error occurred: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
}
