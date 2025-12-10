package Controllers;

import Models.Trainer;
import Models.TrainerDAO;
import Models.Activity;
import java.util.Scanner;
import java.util.Set;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import Views.*;


public class TrainerController {
    private SessionFactory sessionFactory = null;
    private TrainerDAO tDAO = null;
    private ActivityView vActivity = null;
    private MessageView vMessages = null;

    public TrainerController(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
        this.tDAO = new TrainerDAO();
        this.vMessages = new MessageView();
        this.vActivity = new ActivityView();
        this.menu();
    }

    private void menu() {
        Scanner keyboard = new Scanner(System.in);
        String option;
        do {
            vMessages.trainerMenu();
            option = keyboard.nextLine();
            switch (option) {
                case "1" -> ActivitiesByTrainerID();
                case "2" -> vMessages.consoleMessage("INFO", "Exiting trainer menu...");
                default -> vMessages.consoleMessage("WARNING", "Invalid option. Try again.");
            }
        } while (!option.equals("2"));
    }

    private void ActivitiesByTrainerID() {
        Session session = null;
        Transaction tr = null;
        Scanner keyboard = new Scanner(System.in);
        try {
            session = sessionFactory.openSession();
            tr = session.beginTransaction();

            System.out.print("Write ID of trainer in charge: ");
            String idTrainer = keyboard.nextLine();

            Boolean checkID = tDAO.existTrainerID(session, idTrainer);
            if (!checkID) {
                vMessages.consoleMessage("ERROR", "ID is not in the database");
                return;
            }

            Trainer t = tDAO.returnTrainerByID(session, idTrainer);
            Set<Activity> acts = t.getActivitySet();
            vActivity.showTrainerActivities(acts);

            tr.commit();
        } catch (Exception e) {
            if (tr != null && tr.isActive()) {
                tr.rollback();
            }
            vMessages.consoleMessage("ERROR", "An error occurred: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
