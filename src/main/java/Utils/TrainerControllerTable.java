/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

import Models.Trainer;
import Models.TrainerDAO;
import Views.MainWindow;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.swing.*;
import java.util.List;

public class TrainerControllerTable {

    private final SessionFactory sessionFactory;
    private final MainWindow view;
    private final TrainerDAO trainerDAO = new TrainerDAO(); // Zakładam, że masz DAO        

    public TrainerControllerTable(SessionFactory sessionFactory, MainWindow view) {
        this.sessionFactory = sessionFactory;
        this.view = view;
    }

    public Trainer getSelectedTrainer() {
        String trainerId = view.getSelectedTrainerCode(); // Używamy metody z MainWindow

        if (trainerId == null) {
            return null;
        }

        Session session = null;
        Trainer trainer = null;
        try {
            session = sessionFactory.openSession();
            // Używamy DAO do znalezienia obiektu po ID
            // TrainerDAO musi mieć metodę findById lub findByCode
            trainer = trainerDAO.returnTrainerByID(session, trainerId);
            // Zakładam, że TrainerDAO ma metodę findTrainerById

        } catch (Exception e) {
            System.err.println("Błąd pobierania Trenera: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return trainer;
    }

    public void showTrainers() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Query<Trainer> query = session.createQuery("FROM Trainer", Trainer.class);
            List<Trainer> trainers = query.getResultList();

            String[] columns = {"COD", "NAME", "ID NUMBER", "PHONE", "EMAIL", "DATE", "NICK"};
            Object[][] data = new Object[trainers.size()][7];

            for (int i = 0; i < trainers.size(); i++) {
                Trainer t = trainers.get(i);
                data[i][0] = t.getTCod();
                data[i][1] = t.getTName();
                data[i][2] = t.getTidNumber();
                data[i][3] = t.getTphoneNumber();
                data[i][4] = t.getTEmail();
                data[i][5] = t.getTDate();
                data[i][6] = t.getTNick();
            }

            view.setViewName("Trainers");
            view.setTableData(columns, data);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
}
