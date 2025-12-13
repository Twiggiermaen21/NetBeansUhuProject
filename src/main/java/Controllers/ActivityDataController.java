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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kontroler odpowiedzialny za obsługę widoku DataUpdateWindow
 * dla encji Activity (Aktywność). Zarządza logiką dodawania i edycji
 * z wykorzystaniem DAO i Hibernate.
 */
public class ActivityDataController {

    private static final Logger LOGGER = Logger.getLogger(ActivityDataController.class.getName());

    private final SessionFactory sessionFactory;
    private final DataUpdateWindow view;
    private final ActivityDAO activityDAO;
    private final ActivityControllerTable activityControllerTable; 
    private final Activity activityToUpdate; 
    private final TrainerDAO trainerDAO; 

    // =========================================================================
    // KONSTRUKTOR
    // =========================================================================

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

    // =========================================================================
    // METODY INICJALIZUJĄCE (Tryb Edycji / Dodawania)
    // =========================================================================

    public void initializeForm() {
        // Ustawienie etykiet pól dla widoku Aktywności (mapowanie generycznych pól na kontekst)
        view.setFieldLabels("Nazwa Aktywności", "Opis/Typ", "Cena (PLN)", 
                            "Dzień Tygodnia", "Godzina (HH)", "Trener (Kod T_COD)");
        
        view.setFieldVisible(view.jLabel5, true); 
        view.setFieldVisible(view.jFormattedData, true); 

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
     * Wypełnia pola formularza danymi z istniejącego obiektu Activity (tryb edycji).
     */
    private void populateForm() {
        view.setKod(activityToUpdate.getAId());
        view.jKod.setEditable(false); 
        
        view.setNazwisko(activityToUpdate.getAName());
        view.setNumerIdentyfikacyjny(activityToUpdate.getADescription());
        view.setTelefon(String.valueOf(activityToUpdate.getAPrice()));
        view.setEmail(activityToUpdate.getADay());
        view.setFormattedData(String.valueOf(activityToUpdate.getAHour()));
        
        Trainer trainer = activityToUpdate.getAtrainerInCharge();
        if (trainer != null) {
            view.setKategoria(trainer.getTCod());
        } else {
            view.setKategoria("");
        }
    }

    /**
     * Wstępna inicjalizacja formularza w trybie dodawania, w tym
     * generowanie unikalnego kodu ID i ustawianie domyślnych wartości.
     */
    private void initializeFormWithAutoData() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String maxNum = activityDAO.getMaxActivityCode(session); 
            String newNum = generateNextActivityCode(maxNum);
            
            view.jKod.setText(newNum);
            view.jKod.setEditable(false); 
            
            // Ustawianie wartości domyślnych dla pól
            view.setNazwisko("");
            view.setNumerIdentyfikacyjny(""); // Opis/Typ
            view.setTelefon("30"); // Cena (PLN)
            view.setEmail("Monday"); // Dzień
            view.setFormattedData("18"); // Godzina (HH)
            view.setKategoria(""); // Kod Trenera

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd podczas wstępnej inicjalizacji kodu aktywności.", ex);
            view.jKod.setText("BŁĄD GENERACJI");
            view.jKod.setEditable(false);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }
    
    // =========================================================================
    // METODY POMOCNICZE
    // =========================================================================

    /**
     * Główna logika generowania następnego kodu (np. AC10 -> AC11).
     * @param maxNum Maksymalny kod ID pobrany z bazy.
     * @return Następny, inkrementowany kod.
     */
    private String generateNextActivityCode(String maxNum) {
        if (maxNum == null || maxNum.length() < 3 || !maxNum.matches("[A-Z]{2}\\d+")) {
             return "AC01";
        }

        try {
            String prefix = maxNum.substring(0, 2);
            String numPartStr = maxNum.substring(2);
            
            int currentNum = Integer.parseInt(numPartStr);
            int nextNum = currentNum + 1;
            
            String nextNumStr = String.format("%0" + numPartStr.length() + "d", nextNum);
            
            return prefix + nextNumStr;
        } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
            LOGGER.log(Level.WARNING, "Błąd parsowania kodu Aktywności: {0}. Zwracam kod domyślny.", maxNum);
            return "AC01";
        }
    }
    
    /**
     * Zwraca null, jeśli przekazany string jest pusty lub zawiera tylko białe znaki.
     * Używane do pól opcjonalnych (np. Opis, Kod Trenera).
     */
    private String getNullIfBlank(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }

    // =========================================================================
    // LISTENER (OBSŁUGA ZAPISU / EDYCJI)
    // =========================================================================

    private class FormSubmitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            
            Session session = null;
            Transaction tr = null;

            // 1. Zbieranie i czyszczenie danych z formularza
            String aIdFromForm = view.getKod().trim();
            String aName = view.getNazwisko().trim();
            String aDescription = getNullIfBlank(view.getNumerIdentyfikacyjny());
            String priceStr = view.getTelefon().trim();
            String aDay = view.getEmail().trim();
            String hourStr = view.getFormattedData().toString().trim();
            String trainerCod = getNullIfBlank(view.getKategoria()); // Użycie metody pomocniczej
            
            int aPrice;
            int aHour;
            
            // 2. Walidacja wstępna (wymagane pola)
            if (aIdFromForm.isEmpty() || aName.isEmpty() || priceStr.isEmpty() || aDay.isEmpty() || hourStr.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Kod, Nazwa, Cena, Dzień i Godzina Aktywności muszą być wypełnione.", "Błąd Walidacji", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 3. Walidacja numeryczna i zakresy
            try {
                aPrice = Integer.parseInt(priceStr);
                aHour = Integer.parseInt(hourStr);
                
                if (aPrice <= 0 || aHour < 0 || aHour > 23) {
                    throw new NumberFormatException("Wartości poza zakresem.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view, "Cena (" + priceStr + ") i Czas (godzina) (" + hourStr + ") muszą być poprawnymi liczbami całkowitymi. Godzina musi być 0-23, Cena > 0.", "Błąd Walidacji Numerycznej", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            Trainer assignedTrainer = null;
            
            try {
                session = sessionFactory.openSession();
                tr = session.beginTransaction();
                
                // 4. Walidacja i pobieranie Trenera (jeśli kod został podany)
                if (trainerCod != null) {
                    assignedTrainer = trainerDAO.getTrainerByCod(session, trainerCod); 
                    
                    if (assignedTrainer == null) {
                        JOptionPane.showMessageDialog(view, "BŁĄD: Trener o kodzie " + trainerCod + " nie istnieje.", "Błąd Walidacji", JOptionPane.ERROR_MESSAGE);
                        tr.rollback();
                        return;
                    }
                }
                
                // Walidacja unikalności ID (tylko w trybie dodawania)
                if (activityToUpdate == null && activityDAO.existAId(session, aIdFromForm)) {
                    JOptionPane.showMessageDialog(view, "BŁĄD: Kod Aktywności (" + aIdFromForm + ") już istnieje w bazie.", "Błąd Walidacji", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 5. ZAPIS DO BAZY: DODAWANIE czy EDYCJA?
                if (activityToUpdate == null) {
                    // --- DODAWANIE ---
                    Activity newActivity = new Activity(aIdFromForm, aName, aDescription, aPrice, aDay, aHour);
                    newActivity.setAtrainerInCharge(assignedTrainer); 
                    activityDAO.insertActivity(session, newActivity);
                    
                    JOptionPane.showMessageDialog(view, "Aktywność: " + aName + " została pomyślnie dodana.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                
                } else {
                    // --- EDYCJA ---
                    activityToUpdate.setAName(aName);
                    activityToUpdate.setADescription(aDescription);
                    activityToUpdate.setAPrice(aPrice);
                    activityToUpdate.setADay(aDay);
                    activityToUpdate.setAHour(aHour);
                    activityToUpdate.setAtrainerInCharge(assignedTrainer);
                    
                    activityDAO.updateActivity(session, activityToUpdate);
                    
                    JOptionPane.showMessageDialog(view, "Aktywność: " + aName + " została pomyślnie zaktualizowana.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                }

                tr.commit(); 
                
                // 6. Odświeżenie widoku głównego i zamknięcie formularza
                if (activityControllerTable != null) {
                    activityControllerTable.showActivities();
                }
                view.dispose();

            } catch (Exception ex) {
                // Obsługa błędu DB: Wycofanie transakcji
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
                LOGGER.log(Level.SEVERE, "Błąd podczas zapisu/edycji aktywności: " + aIdFromForm, ex);
                JOptionPane.showMessageDialog(view, "Błąd podczas zapisu/edycji aktywności: " + ex.getMessage(), "Błąd DB", JOptionPane.ERROR_MESSAGE);
            } finally {
                // Zawsze zamykamy sesję
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }
    }
}