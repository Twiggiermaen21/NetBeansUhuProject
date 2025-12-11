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
// Brakowało Ci tutaj importów, musiałem je dodać:
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class TrainerDataController {

    private final SessionFactory sessionFactory;
    private final DataUpdateWindow view;
    private final TrainerDAO trainerDAO;
    private final TrainerControllerTable trainerControllerTable;
    private final Trainer trainerToUpdate; // null dla DODAWANIA, obiekt dla EDYCJI

    // =========================================================================
    // KONSTRUKTOR
    // =========================================================================

    public TrainerDataController(SessionFactory sessionFactory, DataUpdateWindow view, TrainerControllerTable trainerControllerTable, Trainer trainerToUpdate) {
        this.sessionFactory = sessionFactory;
        this.view = view;
        this.trainerDAO = new TrainerDAO(); // Zakładamy istnienie TrainerDAO
        this.trainerControllerTable = trainerControllerTable;
        this.trainerToUpdate = trainerToUpdate;

        // Podpięcie listenerów
        this.view.addAkceptujListener(new FormSubmitListener());
        this.view.addAnulujListener(e -> view.dispose());
    }

    // =========================================================================
    // METODY TRYBU EDYCJI/DODAWANIA (Initialize i Populate)
    // =========================================================================

    public void initializeForm() {
        // Ustawienie etykiet pól formularza (zakładając, że DataUpdateWindow ma metodę setFieldLabels)
//        view.setFieldLabels(new String[]{"Kod Trenera:", "Imię:", "Nazwisko:", "ID/Numer:", "Telefon:", "Email:", "Data Zatrudnienia (d/m/yyyy):"});
        
        if (trainerToUpdate != null) {
            // TRYB EDYCJI: Wypełnij danymi istniejącego trenera
            populateForm();
            view.jAkceptuj.setText("ZAPISZ ZMIANY");
            view.setTitle("Edycja Trenera: " + trainerToUpdate.getTCod());
        } else {
            // TRYB DODAWANIA: Wygeneruj nowy kod
            initializeFormWithAutoData();
            view.jAkceptuj.setText("DODAJ TRENERA");
            view.setTitle("Dodawanie Nowego Trenera");
        }
    }
    
    // NOTE: Metoda generowania kodu wymagałaby getMaxTrainerCode() w TrainerDAO.
    // Na potrzeby szablonu, zakładam tymczasowy kod lub ręczne wprowadzanie.
    private void initializeFormWithAutoData() {
        // Pomiń skomplikowaną logikę generowania kodu, na razie ustaw statyczny prefix
        view.jKod.setText("T001"); // Użyjemy T001 lub innej metody z TrainerDAO.getMaxTrainerCode()
        view.jKod.setEditable(false);
    }


    private void populateForm() {
        // Uzupełnienie pól na podstawie istniejącego obiektu
        view.setKod(trainerToUpdate.getTCod());
        view.jKod.setEditable(false); 
        
        // jNazwisko = TName
        view.setNazwisko(trainerToUpdate.getTName()); 
        
        // jNumerIdentyfikacyjny = TidNumber (Numer ID)
        view.setNumerIdentyfikacyjny(trainerToUpdate.getTidNumber()); 
        
        // jTelefon = TphoneNumber
        view.setTelefon(trainerToUpdate.getTphoneNumber());
        
        // jEmail = TEmail
        view.setEmail(trainerToUpdate.getTEmail());
        
        // jNick = TNick
        view.setNick(trainerToUpdate.getTNick());
        
        // jFormattedData = TDate
        // Zakładamy, że TDate to String w formacie parsowalnym przez JFormattedTextField (np. dd/MM/yyyy)
        view.setFormattedData(trainerToUpdate.getTDate()); 
    }

    // =========================================================================
    // LISTENER (OBSŁUGA ZAPISU / EDYCJI)
    // =========================================================================

    private class FormSubmitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            
            Session session = null;
            Transaction tr = null;

            // 1. Zbieranie danych (Używamy gettérów, nie bezpośrednio pól)
            String tCodFromForm = view.getKod().trim();
            String tIdNumberFromForm = view.getNumerIdentyfikacyjny().trim(); // WAŻNE: To jest TidNumber
            
            String tName = view.getNazwisko().trim(); 
            String tNick = view.getNick().trim();
            String tPhone = view.getTelefon().trim();
            String tEmail = view.getEmail().trim();
            
            // Pobieranie daty (może być String lub sparsowany obiekt, w zależności od Formattera)
            Object tJoinDateObj = view.getFormattedData();
            String tDate = (tJoinDateObj != null) ? tJoinDateObj.toString() : "";
            
            // Używamy bieżącej daty, jeśli TDate jest wymagane i puste
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
            // 2. Walidacja wstępna (wymagane pola)
            if (tCodFromForm.isEmpty() || tIdNumberFromForm.isEmpty() || tName.isEmpty() || tDate.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Kod, Imię/Nazwisko, ID Trenera i Data są wymagane.", "Błąd Walidacji", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                session = sessionFactory.openSession();
                tr = session.beginTransaction();
                
                // 3. Walidacja unikalności ID Trenera (TidNumber)
                if (trainerDAO.existTrainerID(session, tIdNumberFromForm)) {
                    // Sprawdź, czy istniejące ID należy do aktualnie edytowanego obiektu
                    if (trainerToUpdate == null || !tIdNumberFromForm.equals(trainerToUpdate.getTidNumber())) {
                        JOptionPane.showMessageDialog(view, "BŁĄD: Numer Identyfikacyjny (ID) Trenera już istnieje w bazie.", "Błąd Walidacji", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // 4. TRYB ZAPISU: DODAWANIE czy EDYCJA?
                if (trainerToUpdate == null) {
                    // --- DODAWANIE ---
                    // Używamy konstruktora, który wymaga: tCod, tName, tidNumber, tDate
                    Trainer newTrainer = new Trainer(tCodFromForm, tName, tIdNumberFromForm, tDate); 
                    
                    // Ustawienie opcjonalnych pól
                    newTrainer.setTphoneNumber(tPhone.isBlank() ? null : tPhone);
                    newTrainer.setTEmail(tEmail.isBlank() ? null : tEmail);
                    newTrainer.setTNick(tNick.isBlank() ? null : tNick);
                    
                    trainerDAO.insertTrainer(session, newTrainer);
                    
                    JOptionPane.showMessageDialog(view, "Trener: " + tName + " został pomyślnie dodany.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                
                } else {
                    // --- EDYCJA ---
                    // Klucza tCod nie zmieniamy, resztę aktualizujemy
                    trainerToUpdate.setTName(tName);
                    trainerToUpdate.setTidNumber(tIdNumberFromForm);
                    trainerToUpdate.setTphoneNumber(tPhone.isBlank() ? null : tPhone);
                    trainerToUpdate.setTEmail(tEmail.isBlank() ? null : tEmail);
                    trainerToUpdate.setTDate(tDate);
                    trainerToUpdate.setTNick(tNick.isBlank() ? null : tNick);
                    
                    trainerDAO.updateTrainer(session, trainerToUpdate);
                    
                    JOptionPane.showMessageDialog(view, "Trener: " + tName + " został pomyślnie zaktualizowany.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                }

                tr.commit();

                // 5. Odświeżenie tabeli i zamknięcie
                if (trainerControllerTable != null) {
                    trainerControllerTable.showTrainers();
                }
                view.dispose();

            } catch (Exception ex) {
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
                JOptionPane.showMessageDialog(view, "Błąd podczas zapisu/edycji trenera: " + ex.getMessage(), "Błąd DB", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }
    }
}

