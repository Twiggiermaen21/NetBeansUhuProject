package Controllers;

import Models.Trainer;
import Models.TrainerDAO;
import Views.DataUpdateWindow;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import Utils.TrainerControllerTable;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kontroler odpowiedzialny za obsługę widoku DataUpdateWindow
 * dla encji Trainer (Trener). Zarządza logiką dodawania i edycji.
 */
public class TrainerDataController {

    private static final Logger LOGGER = Logger.getLogger(TrainerDataController.class.getName());

    private final SessionFactory sessionFactory;
    private final DataUpdateWindow view;
    private final TrainerDAO trainerDAO;
    private final TrainerControllerTable trainerControllerTable;
    private final Trainer trainerToUpdate; 

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // =========================================================================
    // KONSTRUKTOR
    // =========================================================================

    public TrainerDataController(SessionFactory sessionFactory, DataUpdateWindow view, 
                                 TrainerControllerTable trainerControllerTable, Trainer trainerToUpdate) {
        this.sessionFactory = sessionFactory;
        this.view = view;
        this.trainerDAO = new TrainerDAO();
        this.trainerControllerTable = trainerControllerTable;
        this.trainerToUpdate = trainerToUpdate;

        this.view.addAkceptujListener(new FormSubmitListener());
        this.view.addAnulujListener(e -> view.dispose());
    }

    // =========================================================================
    // METODY INICJALIZUJĄCE (Tryb Edycji / Dodawania)
    // =========================================================================

    public void initializeForm() {
        // --- KONFIGURACJA WIDOKU DLA TRENERA ---
        view.setFieldLabels("Imię i Nazwisko", "ID (Numer)", "Telefon", "E-mail", 
                            "Data zatrudnienia (DD/MM/RRRR)", "Nick/Pseudonim");
        
        // Pola używane: Kod (tCod), Nazwisko (tName), NumerIdentyfikacyjny (tIdNumber), 
        // Telefon (tphoneNumber), Email (tEmail), FormattedData (tDate), Kategoria (tNick)
        
        // Upewniamy się, że pole Nick/Kategoria jest widoczne
        view.setFieldVisible(view.jLabel8, true); 
        view.setFieldVisible(view.jKategoria, true); 
        // ---------------------------------------

        if (trainerToUpdate != null) {
            populateForm();
            view.jAkceptuj.setText("ZAPISZ ZMIANY");
            view.setTitle("Edycja Trenera: " + trainerToUpdate.getTCod());
        } else {
            initializeFormWithAutoData();
            view.jAkceptuj.setText("DODAJ TRENERA");
            view.setTitle("Dodawanie Nowego Trenera");
        }
    }
    
    /**
     * Generuje następny numer kodu trenera (np. T001 -> T002).
     */
    private String generateNextTrainerCode(String maxNum) {
        if (maxNum == null || maxNum.isEmpty() || maxNum.length() < 2 || !maxNum.matches("[A-Z]\\d+")) return "T001";
        try {
            String prefix = maxNum.substring(0, 1);
            String numPartStr = maxNum.substring(1);
            int currentNum = Integer.parseInt(numPartStr);
            int nextNum = currentNum + 1;
            String nextNumStr = String.format("%03d", nextNum);
            return prefix + nextNumStr;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Błąd parsowania części numerycznej tCod: " + maxNum, e);
            return "T001";
        }
    }

    /**
     * Wstępna inicjalizacja formularza w trybie dodawania.
     */
    private void initializeFormWithAutoData() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String maxNum = trainerDAO.getMaxTrainerCode(session);
            String newNum = generateNextTrainerCode(maxNum);
            
            view.jKod.setText(newNum);
            view.jKod.setEditable(false);
            
            // Ustawienie aktualnej daty jako domyślnej daty zatrudnienia
            String defaultDate = LocalDate.now().format(DATE_FORMATTER);
            view.setFormattedData(defaultDate);
            
            // Ustawienie Nicku/Kategorii na puste w trybie dodawania
            view.setKategoria("");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd podczas wstępnej inicjalizacji kodu trenera.", ex);
            view.jKod.setText("BŁĄD GENERACJI");
            view.jKod.setEditable(false);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Wypełnia pola formularza danymi z istniejącego obiektu Trainer.
     */
    private void populateForm() {
        view.setKod(trainerToUpdate.getTCod());
        view.jKod.setEditable(false);
        
        view.setNazwisko(trainerToUpdate.getTName());
        view.setNumerIdentyfikacyjny(trainerToUpdate.getTidNumber());
        view.setTelefon(trainerToUpdate.getTphoneNumber());
        view.setEmail(trainerToUpdate.getTEmail());
        
        // MAPOWANIE: Pole Kategoria używane do TNick
        view.setKategoria(trainerToUpdate.getTNick()); 
        
        // Data zatrudnienia
        view.setFormattedData(trainerToUpdate.getTDate());
    }
    
    /**
     * Zwraca null, jeśli przekazany string jest pusty lub zawiera tylko białe znaki.
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

            // 1. Zbieranie danych i walidacja wstępna (pola wymagane)
            String tCodFromForm = view.getKod().trim();
            String tIdNumberFromForm = view.getNumerIdentyfikacyjny().trim();
            String tName = view.getNazwisko().trim();
            String tDateInput = view.getFormattedData().toString().trim();
            
            // Pola opcjonalne (używamy metody pomocniczej)
            String tPhone = getNullIfBlank(view.getTelefon());
            String tEmail = getNullIfBlank(view.getEmail());
            String tNick = getNullIfBlank(view.getKategoria());
            
            String tDate; // Po sparowaniu daty

            // 2. Walidacja wstępna (wymagane pola)
            if (tCodFromForm.isEmpty() || tIdNumberFromForm.isEmpty() || tName.isEmpty() || tDateInput.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Kod, Imię/Nazwisko, ID Trenera i Data zatrudnienia są wymagane.", "Błąd Walidacji", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // 3. Walidacja formatu daty
            try {
                LocalDate parsedDate = LocalDate.parse(tDateInput, DATE_FORMATTER);
                tDate = parsedDate.format(DATE_FORMATTER); // Formatowanie na stały String (DD/MM/RRRR)
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(view, "BŁĄD: Niepoprawny format daty. Oczekiwany format: DD/MM/RRRR.", "Błąd Walidacji", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            try {
                session = sessionFactory.openSession();
                tr = session.beginTransaction();
                
                // 4. Walidacja unikalności ID Trenera (TidNumber)
                if (trainerDAO.existTrainerID(session, tIdNumberFromForm)) { 
                    // Sprawdzamy, czy ID jest zajęte przez INNEGO trenera
                    boolean isAddingNewTrainer = trainerToUpdate == null;
                    boolean isChangingExistingTrainersId = trainerToUpdate != null && !tIdNumberFromForm.equals(trainerToUpdate.getTidNumber());
                    
                    if (isAddingNewTrainer || isChangingExistingTrainersId) {
                        JOptionPane.showMessageDialog(view, "BŁĄD: Numer Identyfikacyjny (ID) Trenera już istnieje w bazie.", "Błąd Walidacji", JOptionPane.ERROR_MESSAGE);
                        tr.rollback();
                        return;
                    }
                }

                // 5. TRYB ZAPISU: DODAWANIE czy EDYCJA?
                if (trainerToUpdate == null) {
                    // --- DODAWANIE ---
                    Trainer newTrainer = new Trainer(tCodFromForm, tName, tIdNumberFromForm, tDate); 
                    
                    newTrainer.setTphoneNumber(tPhone);
                    newTrainer.setTEmail(tEmail);
                    newTrainer.setTNick(tNick); 
                    
                    trainerDAO.insertTrainer(session, newTrainer);
                    
                    JOptionPane.showMessageDialog(view, "Trener: " + tName + " został pomyślnie dodany.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                
                } else {
                    // --- EDYCJA ---
                    trainerToUpdate.setTName(tName);
                    trainerToUpdate.setTidNumber(tIdNumberFromForm); // Zmienione ID (jeśli walidacja przeszła)
                    trainerToUpdate.setTphoneNumber(tPhone);
                    trainerToUpdate.setTEmail(tEmail);
                    trainerToUpdate.setTDate(tDate);
                    trainerToUpdate.setTNick(tNick);
                    
                    trainerDAO.updateTrainer(session, trainerToUpdate);
                    
                    JOptionPane.showMessageDialog(view, "Trener: " + tName + " został pomyślnie zaktualizowany.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                }

                tr.commit();

                // 6. Odświeżenie tabeli i zamknięcie
                if (trainerControllerTable != null) {
                    trainerControllerTable.showTrainers();
                }
                view.dispose();

            } catch (Exception ex) {
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
                LOGGER.log(Level.SEVERE, "Błąd podczas zapisu/edycji trenera: " + tCodFromForm, ex);
                JOptionPane.showMessageDialog(view, "Błąd podczas zapisu/edycji trenera: " + ex.getMessage(), "Błąd DB", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }
    }
}