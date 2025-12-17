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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kontroler odpowiedzialny za obsługę widoku DataUpdateWindow dla encji Trainer.
 * Zarządza logiką dodawania i edycji z wykorzystaniem JDateChooser.
 */
public class TrainerDataController {

    private static final Logger LOGGER = Logger.getLogger(TrainerDataController.class.getName());

    private final SessionFactory sessionFactory;
    private final DataUpdateWindow view;
    private final TrainerDAO trainerDAO;
    private final TrainerControllerTable trainerControllerTable;
    private final Trainer trainerToUpdate; 

    /** Formatator do zamiany daty z kalendarza na String dla bazy danych */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

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
    // METODY INICJALIZUJĄCE
    // =========================================================================

    public void initializeForm() {
        // --- KONFIGURACJA WIDOKU DLA TRENERA ---
        view.setFieldLabels("Imię i Nazwisko", "ID (Numer)", "Telefon", "E-mail", 
                            "Data zatrudnienia", "Nick/Pseudonim","");
        
        // Upewniamy się, że kalendarz jest widoczny
        view.jDateChooser.setVisible(true);
        view.setFieldVisible(view.jLabel8, true); 
        view.setFieldVisible(view.jKategoria, true); 
        view.jBirthdayChooser.setVisible(false);
        view.jLabel6.setVisible(false);
        
        
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
    
    private void populateForm() {
        view.setKod(trainerToUpdate.getTCod());
        view.jKod.setEditable(false);
        
        view.setNazwisko(trainerToUpdate.getTName());
        view.setNumerIdentyfikacyjny(trainerToUpdate.getTidNumber());
        view.setTelefon(trainerToUpdate.getTphoneNumber());
        view.setEmail(trainerToUpdate.getTEmail());
        view.setKategoria(trainerToUpdate.getTNick()); 

        // Konwersja String z bazy na Date dla kalendarza
        try {
            String dateStr = trainerToUpdate.getTDate();
            if (dateStr != null && !dateStr.isEmpty()) {
                view.setSelectedDate(dateFormat.parse(dateStr));
            } else {
                view.setSelectedDate(new Date());
            }
        } catch (Exception e) {
            view.setSelectedDate(new Date());
        }
    }

    private void initializeFormWithAutoData() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String maxNum = trainerDAO.getMaxTrainerCode(session);
            String newNum = generateNextTrainerCode(maxNum);
            
            view.jKod.setText(newNum);
            view.jKod.setEditable(false);
            
            // Ustawienie aktualnej daty w kalendarzu
            view.setSelectedDate(new Date());
            view.setKategoria("");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd podczas inicjalizacji kodu trenera.", ex);
            view.jKod.setText("BŁĄD");
        } finally {
            if (session != null) session.close();
        }
    }

    private String generateNextTrainerCode(String maxNum) {
        if (maxNum == null || maxNum.length() < 2 || !maxNum.matches("[A-Z]\\d+")) return "T001";
        try {
            String prefix = maxNum.substring(0, 1);
            int nextNum = Integer.parseInt(maxNum.substring(1)) + 1;
            return prefix + String.format("%03d", nextNum);
        } catch (Exception e) { return "T001"; }
    }

    private String getNullIfBlank(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    // =========================================================================
    // LISTENER (OBSŁUGA ZAPISU / EDYCJI)
    // =========================================================================

    private class FormSubmitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            
            // 1. Zbieranie danych
            String tCod = view.getKod().trim();
            String tIdNumber = view.getNumerIdentyfikacyjny().trim();
            String tName = view.getNazwisko().trim();
            
            String tPhone = getNullIfBlank(view.getTelefon());
            String tEmail = getNullIfBlank(view.getEmail());
            String tNick = getNullIfBlank(view.getKategoria());
            
            // Pobieramy Date z jDateChooser
            Date selectedDate = view.getSelectedDate();
            
            // 2. Walidacja pól wymaganych
            if (tCod.isEmpty() || tIdNumber.isEmpty() || tName.isEmpty() || selectedDate == null) {
                JOptionPane.showMessageDialog(view, "Kod, Imię/Nazwisko, ID i Data są wymagane.", "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Formatowanie daty na String dla bazy danych
            String formattedDate = dateFormat.format(selectedDate);
            
            Session session = null;
            Transaction tr = null;

            try {
                session = sessionFactory.openSession();
                tr = session.beginTransaction();
                
                // 3. Walidacja unikalności ID Trenera
                if (trainerDAO.existTrainerID(session, tIdNumber)) { 
                    boolean isAddingNew = trainerToUpdate == null;
                    boolean isChangingId = trainerToUpdate != null && !tIdNumber.equals(trainerToUpdate.getTidNumber());
                    
                    if (isAddingNew || isChangingId) {
                        JOptionPane.showMessageDialog(view, "ID Trenera już istnieje w bazie.", "Błąd", JOptionPane.ERROR_MESSAGE);
                        tr.rollback();
                        return;
                    }
                }

                // 4. TRYB ZAPISU
                if (trainerToUpdate == null) {
                    Trainer newTrainer = new Trainer(tCod, tName, tIdNumber, formattedDate); 
                    newTrainer.setTphoneNumber(tPhone);
                    newTrainer.setTEmail(tEmail);
                    newTrainer.setTNick(tNick); 
                    trainerDAO.insertTrainer(session, newTrainer);
                } else {
                    trainerToUpdate.setTName(tName);
                    trainerToUpdate.setTidNumber(tIdNumber);
                    trainerToUpdate.setTphoneNumber(tPhone);
                    trainerToUpdate.setTEmail(tEmail);
                    trainerToUpdate.setTDate(formattedDate);
                    trainerToUpdate.setTNick(tNick);
                    trainerDAO.updateTrainer(session, trainerToUpdate);
                }

                tr.commit();
                if (trainerControllerTable != null) trainerControllerTable.showTrainers();
                view.dispose();
                JOptionPane.showMessageDialog(null, "Dane trenera zostały pomyślnie zapisane.");

            } catch (Exception ex) {
                if (tr != null) tr.rollback();
                LOGGER.log(Level.SEVERE, "Błąd zapisu trenera.", ex);
                JOptionPane.showMessageDialog(view, "Błąd bazy danych: " + ex.getMessage());
            } finally {
                if (session != null) session.close();
            }
        }
    }
}