/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Utils;

/**
 *
 * @author kacpe
 */

import Models.Activity;
import Models.ActivityDAO;
import Views.MainWindow;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import javax.swing.*;
import java.util.List;

public class ActivityControllerTable {
    





    private final SessionFactory sessionFactory;
    private final MainWindow view;
private final ActivityDAO activityDAO = new ActivityDAO(); // Zakładam, że masz DAO
    public ActivityControllerTable(SessionFactory sessionFactory, MainWindow view) {
        this.sessionFactory = sessionFactory;
        this.view = view;
    }
public Activity getSelectedActivity() {
        String activityId = view.getSelectedActivityCode(); // Używamy metody z MainWindow
        
        if (activityId == null) {
            return null;
        }
        
        Session session = null;
        Activity activity = null;
        try {
            session = sessionFactory.openSession();
            // Używamy DAO do znalezienia obiektu po ID
            // ActivityDAO musi mieć metodę findById lub findByCode
            activity = activityDAO.findActivityById(session, activityId); 
            // Zakładam, że ActivityDAO ma metodę findActivityById
            
        } catch (Exception e) {
            System.err.println("Błąd pobierania Aktywności: " + e.getMessage());
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
        return activity;
    }
    public void showActivities() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            Query<Activity> query = session.createQuery("FROM Activity", Activity.class);
            List<Activity> activities = query.getResultList();

            String[] columns = {"ID", "NAME", "DESCRIPTION", "PRICE", "DAY", "HOUR", "TRAINER"};
            Object[][] data = new Object[activities.size()][7];

            for (int i = 0; i < activities.size(); i++) {
                Activity a = activities.get(i);
                data[i][0] = a.getAId();
                data[i][1] = a.getAName();
                data[i][2] = a.getADescription();
                data[i][3] = a.getAPrice();
                data[i][4] = a.getADay();
                data[i][5] = a.getAHour();
                data[i][6] = a.getAtrainerInCharge() != null ? a.getAtrainerInCharge().getTName() : "N/A";
            }

            view.setViewName("Activities");
            view.setTableData(columns, data);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } finally {
            if (session != null && session.isOpen()) session.close();
        }
    }
}
