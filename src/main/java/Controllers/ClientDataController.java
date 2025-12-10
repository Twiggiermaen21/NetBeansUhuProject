package Controllers;

import Models.Client;
import Views.DataUpdateWindow;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import Models.ClientDAO;
import Utils.ClientControllerTable;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ClientDataController {

    private final SessionFactory sessionFactory;
    private final DataUpdateWindow view;
    private final ClientDAO clientDAO;
    private final ClientControllerTable clientControllerTable;
    private final Client clientToUpdate; // null dla DODAWANIA, obiekt dla EDYCJI

    // =========================================================================
    // KONSTRUKTOR
    // =========================================================================
    
    // Konstruktor przyjmuje opcjonalny Client clientToUpdate (będzie null w trybie dodawania)
    public ClientDataController(SessionFactory sessionFactory, DataUpdateWindow view, ClientControllerTable clientControllerTable, Client clientToUpdate) {
        this.sessionFactory = sessionFactory;
        this.view = view;
        this.clientDAO = new ClientDAO();
        this.clientControllerTable = clientControllerTable;
        this.clientToUpdate = clientToUpdate; // Przypisanie obiektu (null lub istniejący klient)

        // Listener Akceptuj obsługuje teraz obie operacje: Dodawanie LUB Edycję
        this.view.addAkceptujListener(new FormSubmitListener()); 
        this.view.addAnulujListener(e -> view.dispose());
    }

    // =========================================================================
    // METODY TRYBU EDYCJI/DODAWANIA (Initialize i Populate)
    // =========================================================================

    /**
     * Wypełnia formularz danymi, różnymi w zależności od trybu (dodawanie/edycja).
     */
    public void initializeForm() {
        if (clientToUpdate != null) {
            // TRYB EDYCJI: Wypełnij danymi istniejącego klienta
            populateForm();
            view.jAkceptuj.setText("ZAPISZ ZMIANY");
            
        } else {
            // TRYB DODAWANIA: Wygeneruj nowy kod
            initializeFormWithAutoData();
            view.jAkceptuj.setText("DODAJ KLIENTA");
        }
    }

    /**
     * Ustawia formularz w trybie DODAWANIA (generuje nowy Kod)
     */
    private void initializeFormWithAutoData() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String maxNum = clientDAO.getMaxMemberNumber(session);
            String newNum = generateNextMemberNumber(maxNum);
            
            view.jKod.setText(newNum);
            view.jKod.setEditable(false); 
            
        } catch (Exception ex) {
            System.err.println("Błąd podczas wstępnej inicjalizacji kodu klienta: " + ex.getMessage());
            view.jKod.setText("BŁĄD GENERACJI");
            view.jKod.setEditable(false);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Ustawia formularz w trybie EDYCJI (wypełnia danymi istniejącego klienta)
     */
    private void populateForm() {
        view.jKod.setText(clientToUpdate.getMNum());
        view.jKod.setEditable(false); // Kod jest kluczem, nie edytujemy go
        
        view.jNazwisko.setText(clientToUpdate.getMName());
        view.jNumer.setText(clientToUpdate.getMId());
        view.jTelefon.setText(clientToUpdate.getMPhone());
        view.jEmail.setText(clientToUpdate.getMemailMember());
        
        // Wymaga metody setKategoria w DataUpdateWindow:
        // view.setKategoria(String.valueOf(clientToUpdate.getMcategoryMember()));
    }
    
    // =========================================================================
    // LOGIKA GENEROWANIA KODU (przeniesiona na dół)
    // =========================================================================

    private String generateNextMemberNumber(String maxNum) {
        if (maxNum == null || maxNum.isEmpty() || maxNum.length() < 2) return "S001";
        try {
            String prefix = maxNum.substring(0, 1);
            String numPartStr = maxNum.substring(1);
            int currentNum = Integer.parseInt(numPartStr);
            int nextNum = currentNum + 1;
            String nextNumStr = String.format("%03d", nextNum);
            return prefix + nextNumStr;
        } catch (NumberFormatException e) {
            System.err.println("Błąd parsowania części numerycznej mNum: " + e.getMessage());
            return "S001";
        }
    }

    // =========================================================================
    // LISTENER (OBSŁUGA ZAPISU / EDYCJI)
    // =========================================================================

    private class FormSubmitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            
            // 1. Zbieranie danych z pól formularza
            String mNumFromForm = view.jKod.getText().trim();
            String mId = view.getNumerIdentyfikacyjny().trim();
            String mName = view.getNazwisko().trim();
            String mPhone = view.getTelefon().trim();
            String mEmail = view.getEmail().trim();
            String category = view.getKategoria().trim().toUpperCase();
            String mBirthdate = view.getFormattedData() != null ? view.getFormattedData().toString() : "";
            
            // 2. Walidacja wstępna
            if (mNumFromForm.isEmpty() || mId.isEmpty() || mName.isEmpty() || category.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Wszystkie wymagane pola muszą być wypełnione.", "Błąd Walidacji", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            char catChar = category.charAt(0);
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            
            Session session = null;
            Transaction tr = null;

            try {
                session = sessionFactory.openSession();
                tr = session.beginTransaction();
                
                // 3. Walidacja DNI (ID)
                // Sprawdź unikalność ID tylko wtedy, gdy ID jest nowe LUB zmienione
                if (clientDAO.existDNI(session, mId)) {
                    // Jeśli ID istnieje, sprawdź, czy należy do edytowanego klienta
                    if (clientToUpdate == null || !mId.equals(clientToUpdate.getMId())) {
                        JOptionPane.showMessageDialog(view, "BŁĄD: Numer Identyfikacyjny (ID) już istnieje w bazie.", "Błąd Walidacji", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                }

                // 4. TRYB ZAPISU: DODAWANIE czy EDYCJA?
                if (clientToUpdate == null) {
                    // --- DODAWANIE ---
                    Client newClient = new Client(mNumFromForm, mName, mId, currentDate, catChar);
                    newClient.setMPhone(mPhone.isBlank() ? null : mPhone);
                    newClient.setMemailMember(mEmail.isBlank() ? null : mEmail);
                    newClient.setMBirthdate(mBirthdate.isBlank() ? null : mBirthdate);
                    clientDAO.insertClient(session, newClient);
                    
                    JOptionPane.showMessageDialog(view, "Klient: " + mName + " został pomyślnie dodany.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                
                } else {
                    // --- EDYCJA ---
                    clientToUpdate.setMName(mName);
                    clientToUpdate.setMId(mId);
                    clientToUpdate.setMPhone(mPhone.isBlank() ? null : mPhone);
                    clientToUpdate.setMemailMember(mEmail.isBlank() ? null : mEmail);
                    clientToUpdate.setMBirthdate(mBirthdate.isBlank() ? null : mBirthdate);
                    clientToUpdate.setMcategoryMember(catChar);

                    clientDAO.updateClient(session, clientToUpdate); // Zakłada istnienie metody updateClient/merge w DAO
                    
                    JOptionPane.showMessageDialog(view, "Klient: " + mName + " został pomyślnie zaktualizowany.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                }

                tr.commit();

                // 5. Odświeżenie tabeli i zamknięcie
                if (clientControllerTable != null) {
                    clientControllerTable.showClients();
                }
                view.dispose();

            } catch (Exception ex) {
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
                JOptionPane.showMessageDialog(view, "Błąd podczas zapisu/edycji klienta: " + ex.getMessage(), "Błąd DB", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }
    }
}