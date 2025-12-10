// java
package Controllers;

import Models.Client;
import Models.ClientDAO;
import Views.MessageView;
import Views.ClientView;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;

public class ClientController {

    private final SessionFactory sessionFactory;
    private final ClientDAO cDAO;
    private final MessageView vMessages;
    private final ClientView vClient;

    public ClientController(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.cDAO = new ClientDAO();
        this.vMessages = new MessageView();
        this.vClient = new ClientView();
        this.menu();
    }

    private void menu() {
        Scanner keyboard = new Scanner(System.in);
        String option;
        do {
            // Zaktualizowane menu
            vMessages.consoleMessage("INFO", "Client menu:\n1 - Add new client\n2 - Show client by member number\n3 - List all clients\n4 - Delete client\n5 - Exit");
            option = keyboard.nextLine();
            switch (option) {
                case "1" -> addClient();
                case "2" -> showClientByMemberNumber();
                case "3" -> listAllClients(); // Nowa metoda
                case "4" -> deleteClient();   // Nowa metoda
                case "5" -> vMessages.consoleMessage("INFO", "Exiting client menu...");
                default -> vMessages.consoleMessage("WARNING", "Invalid option. Try again.");
            }
        } while (!option.equals("5"));
    }

    private void addClient() {
        Session session = null;
        Transaction tr = null;
        Scanner keyboard = new Scanner(System.in);
        try {
            session = sessionFactory.openSession();
            tr = session.beginTransaction();

            System.out.print("Enter member number (mNum): ");
            String mNum = keyboard.nextLine();
            if (cDAO.existMemberNumber(session, mNum)) {
                vMessages.consoleMessage("ERROR", "Member number already exists.");
                return;
            }

            System.out.print("Enter member ID (DNI): ");
            String mId = keyboard.nextLine();
            if (cDAO.existDNI(session, mId)) {
                vMessages.consoleMessage("ERROR", "Member ID already exists.");
                return;
            }

            System.out.print("Enter name: ");
            String mName = keyboard.nextLine();

            System.out.print("Enter phone (optional): ");
            String mPhone = keyboard.nextLine();

            System.out.print("Enter email (optional): ");
            String mEmail = keyboard.nextLine();

            System.out.print("Enter birthdate (yyyy-MM-dd) (optional): ");
            String mBirthdate = keyboard.nextLine();

            System.out.print("Enter category (A-E): ");
            String category = keyboard.nextLine().trim().toUpperCase();
            char catChar = (category.isEmpty() ? 'A' : category.charAt(0));

            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

            Client client = new Client(mNum, mName, mId, currentDate, catChar);
            client.setMPhone(mPhone != null && !mPhone.isBlank() ? mPhone : null);
            client.setMemailMember(mEmail != null && !mEmail.isBlank() ? mEmail : null);
            client.setMBirthdate(mBirthdate != null && !mBirthdate.isBlank() ? mBirthdate : null);

            cDAO.insertClient(session, client);

            tr.commit();
            vMessages.consoleMessage("INFO", "Client inserted successfully.");
        } catch (Exception e) {
            if (tr != null && tr.isActive()) tr.rollback();
            vMessages.consoleMessage("ERROR", "Insert failed: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    private void showClientByMemberNumber() {
        Session session = null;
        Scanner keyboard = new Scanner(System.in);
        try {
            session = sessionFactory.openSession();

            System.out.print("Enter member number (mNum): ");
            String mNum = keyboard.nextLine();

            Client c = cDAO.returnClientByMemberNumber(session, mNum);
            if (c == null) {
                vMessages.consoleMessage("INFO", "Client not found.");
            } else {
                vClient.showClientDetails(c);
            }
        } catch (Exception e) {
            vMessages.consoleMessage("ERROR", "An error occurred: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    private void listAllClients() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            java.util.List<Client> clients = cDAO.getAllClients(session);
            vClient.showClientList(clients);
        } catch (Exception e) {
            vMessages.consoleMessage("ERROR", "Error listing clients: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }

    private void deleteClient() {
        Session session = null;
        Transaction tr = null;
        Scanner keyboard = new Scanner(System.in);
        try {
            session = sessionFactory.openSession();
            tr = session.beginTransaction();

            System.out.print("Enter member ID (DNI) to delete: ");
            String id = keyboard.nextLine();

            Client c = cDAO.getClientById(session, id);
            if (c != null) {
                cDAO.deleteClient(session, c);
                tr.commit();
                vMessages.consoleMessage("INFO", "Client deleted successfully.");
            } else {
                vMessages.consoleMessage("ERROR", "Client not found.");
            }
        } catch (Exception e) {
            if (tr != null) tr.rollback();
            vMessages.consoleMessage("ERROR", "Error deleting client: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
}