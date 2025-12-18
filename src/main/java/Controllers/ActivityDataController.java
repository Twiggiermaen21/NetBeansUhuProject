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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kontroler odpowiedzialny za zarządzanie danymi aktywności w oknie
 * edycji/dodawania. Klasa obsługuje logikę biznesową formularza
 * {@link DataUpdateWindow}, integruje komponent {@code JDateChooser} do wyboru
 * daty oraz komunikuje się z bazą danych poprzez obiekty DAO.
 */
public class ActivityDataController {

    /**
     * Logger do rejestrowania zdarzeń i błędów aplikacji.
     */
    private static final Logger LOGGER = Logger.getLogger(ActivityDataController.class.getName());

    /**
     * Fabryka sesji Hibernate.
     */
    private final SessionFactory sessionFactory;

    /**
     * Główne okno widoku formularza.
     */
    private final DataUpdateWindow view;

    /**
     * Obiekt DAO do zarządzania encjami Activity.
     */
    private final ActivityDAO activityDAO;

    /**
     * Kontroler tabeli, używany do odświeżania widoku listy po zmianach.
     */
    private final ActivityControllerTable activityControllerTable;

    /**
     * Obiekt aktywności podlegający aktualizacji (null w przypadku dodawania
     * nowej).
     */
    private final Activity activityToUpdate;

    /**
     * Obiekt DAO do weryfikacji i pobierania danych trenerów.
     */
    private final TrainerDAO trainerDAO;

    /**
     * Konstruktor kontrolera danych aktywności. Inicjalizuje obiekty dostępowe
     * i ustawia słuchacze zdarzeń dla przycisków widoku.
     *
     * * @param sessionFactory Fabryka sesji Hibernate.
     * @param view Instancja okna formularza.
     * @param activityControllerTable Odniesienie do kontrolera tabeli
     * (odświeżanie danych).
     * @param activityToUpdate Obiekt aktywności do edycji lub null dla nowej
     * aktywności.
     */
    public ActivityDataController(SessionFactory sessionFactory, DataUpdateWindow view,
            ActivityControllerTable activityControllerTable, Activity activityToUpdate) {
        this.sessionFactory = sessionFactory;
        this.view = view;
        this.activityDAO = new ActivityDAO();
        this.activityControllerTable = activityControllerTable;
        this.activityToUpdate = activityToUpdate;
        this.trainerDAO = new TrainerDAO();
view.jTelefon.setPreferredSize(new java.awt.Dimension(235, 25));
    view.jTelefon.setMinimumSize(new java.awt.Dimension(235, 25));
        // Rejestracja słuchaczy zdarzeń
        this.view.addAkceptujListener(new FormSubmitListener());
        this.view.addAnulujListener(e -> view.dispose());
    }

    /**
     * Konfiguruje etykiety pól formularza oraz widoczność komponentów
     * kalendarza. Decyduje, czy formularz ma pracować w trybie edycji, czy
     * dodawania.
     */
    public void initializeForm() {
        view.setFieldLabels("Nazwa Aktywności", "Opis/Typ", "Cena (PLN)",
                "Dzień Tygodnia", "Data", "Trener", "Godzina");
view.jComboBoxDay.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
        "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    }));

    view.jComboBoxTime.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{
        "08:00", "09:00", "10:00", "11:00", "12:00", "13:00", "14:00", 
        "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00"
    }));
    fillTrainerComboBox();
        view.jDateChooser.setVisible(true);
        view.jBirthdayChooser.setVisible(false);
        view.jKategoria.setVisible(false);
        view.jEmail.setVisible(false);
        view.jDateChooser.setVisible(false);
        view.jLabel5.setVisible(false);
        view.jComboBoxDay.setVisible(true);
        view.jComboBoxTime.setVisible(true);
        view.jComboBoxTrener.setVisible(true);

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
     * Wypełnia pola formularza danymi z istniejącego obiektu {@link Activity}
     * (tryb edycji).
     */
    private void populateForm() {
        view.setKod(activityToUpdate.getAId());
        view.jKod.setEditable(false);

        view.setNazwisko(activityToUpdate.getAName());
        view.setNumerIdentyfikacyjny(activityToUpdate.getADescription());
        view.setTelefon(String.valueOf(activityToUpdate.getAPrice()));
        view.setEmail(activityToUpdate.getADay());

//        view.setSelectedDate(new java.util.Date());

        Trainer trainer = activityToUpdate.getAtrainerInCharge();
        view.setKategoria(trainer != null ? trainer.getTCod() : "");
    }
private void fillTrainerComboBox() {
    Session session = null;
    try {
        session = sessionFactory.openSession();
        // 1. Teraz metoda findAllTrainers już istnieje w Twoim DAO
        List<Trainer> trainers = trainerDAO.findAllTrainers(session);
        
        // 2. Rzutujemy na surowy typ, aby uniknąć błędów generyków JComboBox<String>
        javax.swing.JComboBox combo = (javax.swing.JComboBox) view.jComboBoxTrener;
        combo.removeAllItems();
        
        for (Trainer t : trainers) {
            combo.addItem(t); 
        }
    } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Błąd pobierania trenerów", e);
    } finally {
        if (session != null) session.close();
    }
}
    /**
     * Inicjalizuje formularz domyślnymi wartościami oraz automatycznie
     * wygenerowanym kodem (tryb dodawania).
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
            view.setTelefon("");
            
            view.setSelectedDate(new java.util.Date());
            view.setKategoria("");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd inicjalizacji danych.", ex);
            view.jKod.setText("BŁĄD");
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Generuje kolejny unikalny kod aktywności na podstawie najwyższego
     * obecnego w bazie.
     *
     * * @param maxNum Obecny najwyższy kod (np. "AC05").
     * @return Nowy kod (np. "AC06") lub "AC01" w przypadku braku danych.
     */
    private String generateNextActivityCode(String maxNum) {
        if (maxNum == null || !maxNum.matches("[A-Z]{2}\\d+")) {
            return "AC01";
        }
        try {
            String prefix = maxNum.substring(0, 2);
            int nextNum = Integer.parseInt(maxNum.substring(2)) + 1;
            return prefix + String.format("%02d", nextNum);
        } catch (Exception e) {
            return "AC01";
        }
    }

    /**
     * Pomocnicza metoda zamieniająca puste ciągi znaków na wartość null.
     *
     * * @param value Ciąg znaków do sprawdzenia.
     * @return Przetworzony ciąg lub null.
     */
    private String getNullIfBlank(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    /**
     * Wewnętrzna klasa obsługująca zdarzenie kliknięcia przycisku
     * zatwierdzenia. Odpowiada za walidację danych wejściowych, obsługę
     * transakcji Hibernate oraz zapis (INSERT/UPDATE) obiektu w bazie danych.
     */
   private class FormSubmitListener implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        // Pobieranie danych z komponentów tekstowych
        String aIdFromForm = view.getKod().trim();
        String aName = view.getNazwisko().trim();
        String aDescription = getNullIfBlank(view.getNumerIdentyfikacyjny());
        String priceStr = view.getTelefon().trim(); // Pole ceny

        // Pobieranie danych z ComboBoxów
        String selectedDay = (String) view.jComboBoxDay.getSelectedItem();
        String selectedTimeStr = (String) view.jComboBoxTime.getSelectedItem();
        Trainer selectedTrainer = (Trainer) view.jComboBoxTrener.getSelectedItem();

        // 1. Walidacja pól wymaganych
        if (aIdFromForm.isEmpty() || aName.isEmpty() || priceStr.isEmpty() || selectedDay == null || selectedTimeStr == null) {
            JOptionPane.showMessageDialog(view, "Wszystkie podstawowe pola muszą być wypełnione.", "Błąd", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Session session = null;
        Transaction tr = null;

        try {
            // 2. Walidacja ceny
            int aPrice = Integer.parseInt(priceStr);
            if (aPrice < 0) {
                JOptionPane.showMessageDialog(view, "Cena nie może być ujemna!", "Błąd walidacji", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 3. Parsowanie godziny
            int aHour = Integer.parseInt(selectedTimeStr.split(":")[0]);

            session = sessionFactory.openSession();

            // 4. LOGIKA: Sprawdzenie dostępności trenera
            if (selectedTrainer != null) {
                // Jeśli edytujemy, bierzemy obecne ID, żeby nie blokować zapisu tej samej aktywności
                String currentActivityId = (activityToUpdate != null) ? activityToUpdate.getAId() : null;
                
                boolean isBusy = activityDAO.isTrainerOccupied(session, selectedTrainer.getTCod(), selectedDay, aHour, currentActivityId);
                
                if (isBusy) {
                    JOptionPane.showMessageDialog(view, 
                        "Trener " + selectedTrainer.getTName() + " ma już przypisaną inną aktywność w " + selectedDay + " o godzinie " + aHour + ":00!", 
                        "Konflikt w grafiku", 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // 5. Zapis/Aktualizacja
            tr = session.beginTransaction();

            if (activityToUpdate == null) {
                // Nowa aktywność
                Activity newActivity = new Activity(aIdFromForm, aName, aDescription, aPrice, selectedDay, aHour);
                newActivity.setAtrainerInCharge(selectedTrainer);
                activityDAO.insertActivity(session, newActivity);
            } else {
                // Aktualizacja istniejącej
                activityToUpdate.setAName(aName);
                activityToUpdate.setADescription(aDescription);
                activityToUpdate.setAPrice(aPrice);
                activityToUpdate.setADay(selectedDay);
                activityToUpdate.setAHour(aHour);
                activityToUpdate.setAtrainerInCharge(selectedTrainer);
                activityDAO.updateActivity(session, activityToUpdate);
            }

            tr.commit();
            if (activityControllerTable != null) activityControllerTable.showActivities();
            view.dispose();
            JOptionPane.showMessageDialog(null, "Pomyślnie zapisano aktywność.");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Cena musi być poprawną liczbą całkowitą.", "Błąd", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            if (tr != null) tr.rollback();
            LOGGER.log(Level.SEVERE, "Błąd bazy danych", ex);
            JOptionPane.showMessageDialog(view, "Błąd bazy danych: " + ex.getMessage());
        } finally {
            if (session != null) session.close();
        }
    }
}
}
