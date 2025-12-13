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
import java.time.format.DateTimeParseException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kontroler odpowiedzialny za obsługę widoku DataUpdateWindow
 * dla encji Client (Klient). Zarządza logiką dodawania i edycji.
 */
public class ClientDataController {

    private static final Logger LOGGER = Logger.getLogger(ClientDataController.class.getName());
    
    private final SessionFactory sessionFactory;
    private final DataUpdateWindow view;
    private final ClientDAO clientDAO;
    private final ClientControllerTable clientControllerTable;
    private final Client clientToUpdate; 
    
    /** Ustalony, spójny format daty dla całej aplikacji (np. 31/12/2025). */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // =========================================================================
    // KONSTRUKTOR
    // =========================================================================

    public ClientDataController(SessionFactory sessionFactory, DataUpdateWindow view, 
                                ClientControllerTable clientControllerTable, Client clientToUpdate) {
        this.sessionFactory = sessionFactory;
        this.view = view;
        this.clientDAO = new ClientDAO();
        this.clientControllerTable = clientControllerTable;
        this.clientToUpdate = clientToUpdate;

        this.view.addAkceptujListener(new FormSubmitListener());
        this.view.addAnulujListener(e -> view.dispose());
    }

    // =========================================================================
    // METODY INICJALIZUJĄCE (Tryb Edycji / Dodawania)
    // =========================================================================

    public void initializeForm() {
        // Ustawienie etykiet widoku (Jeśli to jest standardowy widok formularza, 
        // warto dodać też wywołanie view.setFieldLabels("...", "..."))

        if (clientToUpdate != null) {
            populateForm();
            view.jAkceptuj.setText("ZAPISZ ZMIANY");
            view.setTitle("Edycja Klienta: " + clientToUpdate.getMNum());

        } else {
            initializeFormWithAutoData();
            view.jAkceptuj.setText("DODAJ KLIENTA");
            view.setTitle("Dodawanie Nowego Klienta");
        }
    }

    /**
     * Wstępna inicjalizacja formularza w trybie dodawania: 
     * generuje unikalny numer członkowski i ustawia domyślną datę.
     */
    private void initializeFormWithAutoData() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String maxNum = clientDAO.getMaxMemberNumber(session);
            String newNum = generateNextMemberNumber(maxNum);

            view.jKod.setText(newNum);
            view.jKod.setEditable(false);
            
            // Ustawiamy aktualną datę jako domyślną datę przyjęcia
            String defaultDate = LocalDate.now().format(DATE_FORMATTER);
            view.setFormattedData(defaultDate);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd podczas wstępnej inicjalizacji kodu klienta.", ex);
            view.jKod.setText("BŁĄD GENERACJI");
            view.jKod.setEditable(false);
        } finally {
            if (session != null && session.isOpen()) {
                session.close();
            }
        }
    }

    /**
     * Wypełnia pola formularza danymi z istniejącego obiektu Client (tryb edycji).
     */
    private void populateForm() {
        view.jKod.setText(clientToUpdate.getMNum());
        view.jKod.setEditable(false);

        view.jNazwisko.setText(clientToUpdate.getMName());
        view.jNumer.setText(clientToUpdate.getMId());
        view.jTelefon.setText(clientToUpdate.getMPhone());
        view.jEmail.setText(clientToUpdate.getMemailMember());
        
        // Ustawienie Daty Wejścia/Przyjęcia
        String entryDateFromDB = clientToUpdate.getMstartingDateMember();
        if (entryDateFromDB != null) { 
            view.setFormattedData(entryDateFromDB);
        } else {
             view.setFormattedData(""); 
        }

        // Ustawienie Kategorii
        view.setKategoria(String.valueOf(clientToUpdate.getMcategoryMember()));
    }

    // =========================================================================
    // LOGIKA GENEROWANIA KODU
    // =========================================================================

    /**
     * Generuje następny numer członkowski (np. S001 -> S002).
     * Zakłada, że numer składa się z prefixu (S) i 3 cyfr.
     * @param maxNum Maksymalny numer klienta z bazy.
     * @return Następny, inkrementowany kod.
     */
    private String generateNextMemberNumber(String maxNum) {
        if (maxNum == null || maxNum.isEmpty() || maxNum.length() < 2) return "S001";
        try {
            String prefix = maxNum.substring(0, 1);
            String numPartStr = maxNum.substring(1);
            
            // Walidacja formatu przed parsowaniem (dobre zabezpieczenie)
            if (!numPartStr.matches("\\d+")) {
                 LOGGER.warning("Niepoprawna część numeryczna w kodzie klienta: " + maxNum);
                 return "S001";
            }
            
            int currentNum = Integer.parseInt(numPartStr);
            int nextNum = currentNum + 1;
            
            // Formatowanie z zachowaniem zer wiodących (3 cyfry)
            String nextNumStr = String.format("%03d", nextNum);
            return prefix + nextNumStr;
        } catch (NumberFormatException e) {
            LOGGER.log(Level.SEVERE, "Błąd parsowania części numerycznej mNum: " + maxNum, e);
            return "S001";
        }
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

            // 1. Zbieranie i czyszczenie danych z pól formularza
            String mNumFromForm = view.jKod.getText().trim();
            String mId = view.getNumerIdentyfikacyjny().trim();
            String mName = view.getNazwisko().trim();
            
            // Użycie getNullIfBlank dla pól opcjonalnych
            String mPhone = getNullIfBlank(view.getTelefon());
            String mEmail = getNullIfBlank(view.getEmail());
            
            String category = view.getKategoria().trim().toUpperCase();
            String dataInput = view.getFormattedData().toString().trim(); // Data Wejścia/Przyjęcia


            // 2. WSTĘPNA WALIDACJA PÓL WYMAGANYCH
            if (mNumFromForm.isEmpty() || mId.isEmpty() || mName.isEmpty() || category.isEmpty() || dataInput.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Pola Kod, ID, Nazwisko, Kategoria oraz Data Przyjęcia są wymagane.", "Błąd Walidacji", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 3. WALIDACJA KATEGORII (A-D)
            char catChar;
            if (category.length() != 1) {
                 JOptionPane.showMessageDialog(view, "BŁĄD: Kategoria musi być jedną literą.", "Błąd Walidacji Kategorii", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            catChar = category.charAt(0);

            if (catChar != 'A' && catChar != 'B' && catChar != 'C' && catChar != 'D') {
                JOptionPane.showMessageDialog(view, "BŁĄD: Kategoria musi być jedną z wartości A, B, C lub D.", "Błąd Walidacji Kategorii", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 4. WALIDACJA I FORMATOWANIE DATY WEJŚCIA
            String acceptedEntryDate;
            try {
                // Parsowanie (konwersja na obiekt LocalDate)
                LocalDate parsedDate = LocalDate.parse(dataInput, DATE_FORMATTER);
                // Ponowne formatowanie do stałego formatu Stringa
                acceptedEntryDate = parsedDate.format(DATE_FORMATTER);
                
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(view, "BŁĄD: Niepoprawny format daty. Oczekiwany format: DD/MM/RRRR.", "Błąd Walidacji Daty", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Używamy `null` dla daty urodzenia (zakładamy, że nie jest używana w tym formularzu/systemie)
            String nullBirthDate = null; 

            Session session = null;
            Transaction tr = null;

            try {
                session = sessionFactory.openSession();
                tr = session.beginTransaction();

                // 5. Walidacja unikalności ID (DNI/PESEL)
                // Sprawdzamy, czy ID już istnieje...
                if (clientDAO.existDNI(session, mId)) {
                    // ... i czy jest to ID innego klienta (tryb dodawania lub zmiana ID w edycji)
                    boolean isAddingNewClient = clientToUpdate == null;
                    boolean isChangingExistingClientsId = clientToUpdate != null && !mId.equals(clientToUpdate.getMId());
                    
                    if (isAddingNewClient || isChangingExistingClientsId) {
                        JOptionPane.showMessageDialog(view, "BŁĄD: Numer Identyfikacyjny (ID) już istnieje w bazie.", "Błąd Walidacji", JOptionPane.ERROR_MESSAGE);
                        tr.rollback(); // Wycofujemy, jeśli zaczęliśmy transakcję
                        return;
                    }
                }

                // 6. TRYB ZAPISU: DODAWANIE czy EDYCJA?
                if (clientToUpdate == null) {
                    // --- DODAWANIE ---
                    Client newClient = new Client(mNumFromForm, mName, mId, acceptedEntryDate, catChar);
                    
                    // Ustawienie pól opcjonalnych (null lub wartość)
                    newClient.setMPhone(mPhone);
                    newClient.setMemailMember(mEmail);
                    newClient.setMBirthdate(nullBirthDate); 
                    
                    clientDAO.insertClient(session, newClient);

                    JOptionPane.showMessageDialog(view, "Klient: " + mName + " został pomyślnie dodany.", "Sukces", JOptionPane.INFORMATION_MESSAGE);

                } else {
                    // --- EDYCJA ---
                    clientToUpdate.setMName(mName);
                    clientToUpdate.setMId(mId); // ID może zostać zmienione, jeśli przeszło walidację unikalności
                    clientToUpdate.setMPhone(mPhone);
                    clientToUpdate.setMemailMember(mEmail);
                    clientToUpdate.setMstartingDateMember(acceptedEntryDate); 
                    clientToUpdate.setMcategoryMember(catChar);
                    
                    // UWAGA: Nie ruszamy daty urodzenia, zakładając, że jest zarządzana gdzie indziej.
                    
                    clientDAO.updateClient(session, clientToUpdate);

                    JOptionPane.showMessageDialog(view, "Klient: " + mName + " został pomyślnie zaktualizowany.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                }

                tr.commit();

                // 7. Odświeżenie tabeli i zamknięcie
                if (clientControllerTable != null) {
                    clientControllerTable.showClients();
                }
                view.dispose();

            } catch (Exception ex) {
                // Obsługa błędu DB: Wycofanie transakcji
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
                LOGGER.log(Level.SEVERE, "Błąd podczas zapisu/edycji klienta: " + mNumFromForm, ex);
                JOptionPane.showMessageDialog(view, "Błąd podczas zapisu/edycji klienta: " + ex.getMessage(), "Błąd DB", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }
    }
}