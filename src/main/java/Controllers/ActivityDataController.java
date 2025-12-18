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
 * Kontroler odpowiedzialny za zarządzanie danymi aktywności w oknie edycji/dodawania.
 * Klasa obsługuje logikę biznesową formularza {@link DataUpdateWindow}, integruje 
 * komponent {@code JDateChooser} do wyboru daty oraz komunikuje się z bazą danych 
 * poprzez obiekty DAO.
 */
public class ActivityDataController {

    /** Logger do rejestrowania zdarzeń i błędów aplikacji. */
    private static final Logger LOGGER = Logger.getLogger(ActivityDataController.class.getName());

    /** Fabryka sesji Hibernate. */
    private final SessionFactory sessionFactory;
    
    /** Główne okno widoku formularza. */
    private final DataUpdateWindow view;
    
    /** Obiekt DAO do zarządzania encjami Activity. */
    private final ActivityDAO activityDAO;
    
    /** Kontroler tabeli, używany do odświeżania widoku listy po zmianach. */
    private final ActivityControllerTable activityControllerTable; 
    
    /** Obiekt aktywności podlegający aktualizacji (null w przypadku dodawania nowej). */
    private final Activity activityToUpdate; 
    
    /** Obiekt DAO do weryfikacji i pobierania danych trenerów. */
    private final TrainerDAO trainerDAO; 

    /**
     * Konstruktor kontrolera danych aktywności.
     * Inicjalizuje obiekty dostępowe i ustawia słuchacze zdarzeń dla przycisków widoku.
     * * @param sessionFactory Fabryka sesji Hibernate.
     * @param view Instancja okna formularza.
     * @param activityControllerTable Odniesienie do kontrolera tabeli (odświeżanie danych).
     * @param activityToUpdate Obiekt aktywności do edycji lub null dla nowej aktywności.
     */
    public ActivityDataController(SessionFactory sessionFactory, DataUpdateWindow view, 
                                  ActivityControllerTable activityControllerTable, Activity activityToUpdate) {
        this.sessionFactory = sessionFactory;
        this.view = view;
        this.activityDAO = new ActivityDAO();
        this.activityControllerTable = activityControllerTable;
        this.activityToUpdate = activityToUpdate;
        this.trainerDAO = new TrainerDAO();
        
        // Rejestracja słuchaczy zdarzeń
        this.view.addAkceptujListener(new FormSubmitListener());
        this.view.addAnulujListener(e -> view.dispose());
    }

    /**
     * Konfiguruje etykiety pól formularza oraz widoczność komponentów kalendarza.
     * Decyduje, czy formularz ma pracować w trybie edycji, czy dodawania.
     */
    public void initializeForm() {
        view.setFieldLabels("Nazwa Aktywności", "Opis/Typ", "Cena (PLN)", 
                            "Dzień Tygodnia", "Data", "Trener (Kod T_COD)","");
        
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
    
    /**
     * Wypełnia pola formularza danymi z istniejącego obiektu {@link Activity} (tryb edycji).
     */
    private void populateForm() {
        view.setKod(activityToUpdate.getAId());
        view.jKod.setEditable(false); 
        
        view.setNazwisko(activityToUpdate.getAName());
        view.setNumerIdentyfikacyjny(activityToUpdate.getADescription());
        view.setTelefon(String.valueOf(activityToUpdate.getAPrice()));
        view.setEmail(activityToUpdate.getADay());
        
        view.setSelectedDate(new java.util.Date());
        
        Trainer trainer = activityToUpdate.getAtrainerInCharge();
        view.setKategoria(trainer != null ? trainer.getTCod() : "");
    }

    /**
     * Inicjalizuje formularz domyślnymi wartościami oraz automatycznie wygenerowanym kodem (tryb dodawania).
     */
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
            view.setSelectedDate(new java.util.Date()); 
            view.setKategoria(""); 

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd inicjalizacji danych.", ex);
            view.jKod.setText("BŁĄD");
        } finally {
            if (session != null) session.close();
        }
    }

    /**
     * Generuje kolejny unikalny kod aktywności na podstawie najwyższego obecnego w bazie.
     * * @param maxNum Obecny najwyższy kod (np. "AC05").
     * @return Nowy kod (np. "AC06") lub "AC01" w przypadku braku danych.
     */
    private String generateNextActivityCode(String maxNum) {
        if (maxNum == null || !maxNum.matches("[A-Z]{2}\\d+")) return "AC01";
        try {
            String prefix = maxNum.substring(0, 2);
            int nextNum = Integer.parseInt(maxNum.substring(2)) + 1;
            return prefix + String.format("%02d", nextNum);
        } catch (Exception e) { return "AC01"; }
    }

    /**
     * Pomocnicza metoda zamieniająca puste ciągi znaków na wartość null.
     * * @param value Ciąg znaków do sprawdzenia.
     * @return Przetworzony ciąg lub null.
     */
    private String getNullIfBlank(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    /**
     * Wewnętrzna klasa obsługująca zdarzenie kliknięcia przycisku zatwierdzenia.
     * Odpowiada za walidację danych wejściowych, obsługę transakcji Hibernate
     * oraz zapis (INSERT/UPDATE) obiektu w bazie danych.
     */
    private class FormSubmitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            Session session = null;
            Transaction tr = null;

            // Pobieranie danych z widoku
            String aIdFromForm = view.getKod().trim();
            String aName = view.getNazwisko().trim();
            String aDescription = getNullIfBlank(view.getNumerIdentyfikacyjny());
            String priceStr = view.getTelefon().trim();
            String aDay = view.getEmail().trim();
            String trainerCod = getNullIfBlank(view.getKategoria());
            Date selectedDate = view.getSelectedDate();
            
            // Walidacja pól wymaganych
            if (aIdFromForm.isEmpty() || aName.isEmpty() || priceStr.isEmpty() || selectedDate == null) {
                JOptionPane.showMessageDialog(view, "Wszystkie pola (w tym data) muszą być wypełnione.", "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            }

            try {
                int aPrice = Integer.parseInt(priceStr);
                
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
                    // Logika zapisu nowej aktywności
                    Activity newActivity = new Activity(aIdFromForm, aName, aDescription, aPrice, aDay, aHourFromCalendar);
                    newActivity.setAtrainerInCharge(assignedTrainer);
                    activityDAO.insertActivity(session, newActivity);
                    JOptionPane.showMessageDialog(view, "Dodano aktywność: " + aName);
                } else {
                    // Logika aktualizacji istniejącej aktywności
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