package Controllers;

import Models.Activity;
import Models.ActivityDAO;
import Models.Trainer;
import Models.TrainerDAO;
import Views.DataUpdateWindow;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import Utils.ActivityControllerTable;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Zaktualizowany kontroler: Usunięto jFormattedData, wprowadzono pełną obsługę JDateChooser.
 */
public class ActivityDataController {

    private static final Logger LOGGER = Logger.getLogger(ActivityDataController.class.getName());

    private final SessionFactory sessionFactory;
    private final DataUpdateWindow view;
    private final ActivityDAO activityDAO;
    private final ActivityControllerTable activityControllerTable; 
    private final Activity activityToUpdate; 
    private final TrainerDAO trainerDAO; 

    public ActivityDataController(SessionFactory sessionFactory, DataUpdateWindow view, 
                                  ActivityControllerTable activityControllerTable, Activity activityToUpdate) {
        this.sessionFactory = sessionFactory;
        this.view = view;
        this.activityDAO = new ActivityDAO();
        this.activityControllerTable = activityControllerTable;
        this.activityToUpdate = activityToUpdate;
        this.trainerDAO = new TrainerDAO();
        
        this.view.addAkceptujListener(new FormSubmitListener());
        this.view.addAnulujListener(e -> view.dispose());
    }

    public void initializeForm() {
        // Label dla pola 5 zmieniony na "Data"
        view.setFieldLabels("Nazwa Aktywności", "Opis/Typ", "Cena (PLN)", 
                            "Dzień Tygodnia", "Data", "Trener (Kod T_COD)","");
        
        // Upewniamy się, że kalendarz jest widoczny
        view.jDateChooser.setVisible(true);
 view.jBirthdayChooser.setVisible(false);
        view.jLabel6.setVisible(false);
        if (activityToUpdate != null) {
            populateForm();
            view.jAkceptuj.setText("ZAPISZ ZMIANY");
            view.setTitle("Edycja Aktywności: " + activityToUpdate.getAId());
        } else {
            initializeFormWithAutoData();
            view.jAkceptuj.setText("DODAJ AKTYWNOŚĆ");
            view.setTitle("Dodawanie Nowej Aktywności");
        }
    }
    
    private void populateForm() {
        view.setKod(activityToUpdate.getAId());
        view.jKod.setEditable(false); 
        
        view.setNazwisko(activityToUpdate.getAName());
        view.setNumerIdentyfikacyjny(activityToUpdate.getADescription());
        view.setTelefon(String.valueOf(activityToUpdate.getAPrice()));
        view.setEmail(activityToUpdate.getADay());
        
        // Przy edycji ustawiamy aktualną datę (lub datę z obiektu, jeśli go posiadasz)
        view.setSelectedDate(new java.util.Date());
        
        Trainer trainer = activityToUpdate.getAtrainerInCharge();
        view.setKategoria(trainer != null ? trainer.getTCod() : "");
    }

    private void initializeFormWithAutoData() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String maxNum = activityDAO.getMaxActivityCode(session); 
            String newNum = generateNextActivityCode(maxNum);
            
            view.jKod.setText(newNum);
            view.jKod.setEditable(false); 
            
            view.setNazwisko("");
            view.setNumerIdentyfikacyjny(""); 
            view.setTelefon("30"); 
            view.setEmail("Monday"); 
            view.setSelectedDate(new java.util.Date()); // Ustawienie domyślnej daty
            view.setKategoria(""); 

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd inicjalizacji danych.", ex);
            view.jKod.setText("BŁĄD");
        } finally {
            if (session != null) session.close();
        }
    }

    private String generateNextActivityCode(String maxNum) {
        if (maxNum == null || !maxNum.matches("[A-Z]{2}\\d+")) return "AC01";
        try {
            String prefix = maxNum.substring(0, 2);
            int nextNum = Integer.parseInt(maxNum.substring(2)) + 1;
            return prefix + String.format("%02d", nextNum);
        } catch (Exception e) { return "AC01"; }
    }

    private String getNullIfBlank(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    // =========================================================================
    // LISTENER - TYLKO JCALENDAR
    // =========================================================================

    private class FormSubmitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Session session = null;
            Transaction tr = null;

            // 1. Zbieranie danych (bez jFormattedData)
            String aIdFromForm = view.getKod().trim();
            String aName = view.getNazwisko().trim();
            String aDescription = getNullIfBlank(view.getNumerIdentyfikacyjny());
            String priceStr = view.getTelefon().trim();
            String aDay = view.getEmail().trim();
            String trainerCod = getNullIfBlank(view.getKategoria());
            
            // Pobieramy Date z jDateChooser
            Date selectedDate = view.getSelectedDate();
            
            // 2. Walidacja
            if (aIdFromForm.isEmpty() || aName.isEmpty() || priceStr.isEmpty() || selectedDate == null) {
                JOptionPane.showMessageDialog(view, "Wszystkie pola (w tym data) muszą być wypełnione.", "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int aPrice = Integer.parseInt(priceStr);
                
                // Wyciągamy godzinę z kalendarza, aby zachować zgodność z modelem Activity (int aHour)
                Calendar cal = Calendar.getInstance();
                cal.setTime(selectedDate);
                int aHourFromCalendar = cal.get(Calendar.HOUR_OF_DAY);

                session = sessionFactory.openSession();
                tr = session.beginTransaction();
                
                Trainer assignedTrainer = (trainerCod != null) ? trainerDAO.getTrainerByCod(session, trainerCod) : null;
                
                if (trainerCod != null && assignedTrainer == null) {
                    JOptionPane.showMessageDialog(view, "Trener o kodzie " + trainerCod + " nie istnieje.", "Błąd", JOptionPane.ERROR_MESSAGE);
                    tr.rollback();
                    return;
                }

                if (activityToUpdate == null) {
                    // DODAWANIE
                    Activity newActivity = new Activity(aIdFromForm, aName, aDescription, aPrice, aDay, aHourFromCalendar);
                    newActivity.setAtrainerInCharge(assignedTrainer);
                    activityDAO.insertActivity(session, newActivity);
                    JOptionPane.showMessageDialog(view, "Dodano aktywność: " + aName);
                } else {
                    // EDYCJA
                    activityToUpdate.setAName(aName);
                    activityToUpdate.setADescription(aDescription);
                    activityToUpdate.setAPrice(aPrice);
                    activityToUpdate.setADay(aDay);
                    activityToUpdate.setAHour(aHourFromCalendar);
                    activityToUpdate.setAtrainerInCharge(assignedTrainer);
                    activityDAO.updateActivity(session, activityToUpdate);
                    JOptionPane.showMessageDialog(view, "Zaktualizowano aktywność: " + aName);
                }

                tr.commit();
                if (activityControllerTable != null) activityControllerTable.showActivities();
                view.dispose();

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view, "Cena musi być liczbą całkowitą.", "Błąd", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                if (tr != null) tr.rollback();
                LOGGER.log(Level.SEVERE, "Błąd podczas operacji na bazie.", ex);
            } finally {
                if (session != null) session.close();
            }
        }
    }
} 