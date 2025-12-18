package Controllers;

import Models.Client;
import Models.ClientDAO;
import Views.DataUpdateWindow;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import Utils.ClientControllerTable;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Kontroler odpowiedzialny za obsługę okna edycji i dodawania danych klienta.
 * Zarządza interakcją pomiędzy widokiem {@link DataUpdateWindow} a bazą danych,
 * zapewniając automatyczne generowanie kodów klientów, walidację danych oraz
 * poprawne formatowanie dat (dd/MM/yyyy).
 */
public class ClientDataController {

    /**
     * Logger do rejestrowania zdarzeń diagnostycznych i błędów.
     */
    private static final Logger LOGGER = Logger.getLogger(ClientDataController.class.getName());

    /**
     * Fabryka sesji Hibernate.
     */
    private final SessionFactory sessionFactory;

    /**
     * Okno dialogowe formularza danych.
     */
    private final DataUpdateWindow view;

    /**
     * Obiekt dostępu do danych dla encji Client.
     */
    private final ClientDAO clientDAO;

    /**
     * Kontroler zarządzający tabelą klientów (używany do odświeżania widoku).
     */
    private final ClientControllerTable clientControllerTable;

    /**
     * Obiekt klienta przeznaczony do aktualizacji; null w przypadku tworzenia
     * nowego rekordu.
     */
    private final Client clientToUpdate;

    /**
     * Formater dat zgodny ze strukturą bazy danych (dzień/miesiąc/rok).
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Inicjalizuje kontroler danych klienta. Ustawia słuchacze zdarzeń dla
     * przycisków Akceptuj i Anuluj.
     *
     * * @param sessionFactory Fabryka sesji Hibernate.
     * @param view Instancja okna formularza.
     * @param clientControllerTable Kontroler tabeli do wywołania odświeżenia
     * danych.
     * @param clientToUpdate Obiekt klienta do edycji lub null dla nowego
     * klienta.
     */
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

    /**
     * Przygotowuje formularz do wyświetlenia. Konfiguruje etykiety pól,
     * widoczność kalendarzy oraz wypełnia pola danymi w zależności od trybu
     * (edycja/dodawanie).
     */
    public void initializeForm() {
        view.setFieldLabels("Imię i Nazwisko", "Numer Identyfikacyjny", "Telefon",
                "E-mail", "Data Przyjęcia", "Kategoria (A-D)", "Data Urodzenia");

        view.jBirthdayChooser.setVisible(true);
        view.jLabel6.setVisible(true);
        view.jComboBoxDay.setVisible(false);
        view.jComboBoxTime.setVisible(false);
        view.jComboBoxTrener.setVisible(false);
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
     * Automatycznie generuje kolejny numer członkowski dla nowego klienta i
     * ustawia domyślne wartości dat w formularzu.
     */
    private void initializeFormWithAutoData() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String maxNum = clientDAO.getMaxMemberNumber(session);
            view.jKod.setText(generateNextMemberNumber(maxNum));
            view.jKod.setEditable(false);

            view.setSelectedDate(new Date());
            view.setBirthdayDate(null);

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd inicjalizacji kodu klienta.", ex);
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Wypełnia pola formularza danymi istniejącego klienta. Dokonuje parsowania
     * dat ze Stringów na obiekty typu Date dla komponentów GUI.
     */
    private void populateForm() {
        view.setKod(clientToUpdate.getMNum());
        view.jKod.setEditable(false);
        view.setNazwisko(clientToUpdate.getMName());
        view.setNumerIdentyfikacyjny(clientToUpdate.getMId());
        view.setTelefon(clientToUpdate.getMPhone());
        view.setEmail(clientToUpdate.getMemailMember());
        view.setKategoria(String.valueOf(clientToUpdate.getMcategoryMember()));

        try {
            String entryDateStr = clientToUpdate.getMstartingDateMember();
            if (entryDateStr != null && !entryDateStr.isEmpty()) {
                view.setSelectedDate(dateFormat.parse(entryDateStr));
            }
        } catch (Exception e) {
            view.setSelectedDate(new Date());
        }

        try {
            String birthDateStr = clientToUpdate.getMBirthdate();
            if (birthDateStr != null && !birthDateStr.isEmpty()) {
                view.setBirthdayDate(dateFormat.parse(birthDateStr));
            }
        } catch (Exception e) {
            view.setBirthdayDate(null);
        }
    }

    /**
     * Generuje nowy numer członkowski na podstawie najwyższego numeru w bazie.
     *
     * * @param maxNum Obecnie najwyższy numer (np. "S005").
     * @return Następny numer w sekwencji (np. "S006").
     */
    private String generateNextMemberNumber(String maxNum) {
        if (maxNum == null || maxNum.length() < 2) {
            return "S001";
        }
        try {
            String prefix = maxNum.substring(0, 1);
            int nextNum = Integer.parseInt(maxNum.substring(1)) + 1;
            return prefix + String.format("%03d", nextNum);
        } catch (Exception e) {
            return "S001";
        }
    }

    /**
     * Zamienia puste lub białe znaki na wartość null.
     *
     * * @param value Wartość tekstowa do sprawdzenia.
     * @return Przetworzony String lub null.
     */
    private String getNullIfBlank(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    /**
     * Klasa wewnętrzna obsługująca logikę zapisu danych. Realizuje walidację
     * pól obowiązkowych, sprawdza unikalność numeru DNI i wykonuje operację
     * INSERT lub UPDATE za pośrednictwem Hibernate.
     */
    private class FormSubmitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String mNum = view.getKod().trim();
            String mId = view.getNumerIdentyfikacyjny().trim();
            String mName = view.getNazwisko().trim();
            String mPhone = getNullIfBlank(view.getTelefon());
            String mEmail = getNullIfBlank(view.getEmail());
            String category = view.getKategoria().trim().toUpperCase();

            Date entryDate = view.getSelectedDate();
            Date birthDate = view.getBirthdayDate();

            if (mNum.isEmpty() || mId.isEmpty() || mName.isEmpty() || category.isEmpty() || entryDate == null) {
                JOptionPane.showMessageDialog(view, "Pola Kod, ID, Nazwisko, Kategoria i Data Przyjęcia są wymagane.", "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (!mId.matches("\\d{8}[A-Z]")) {
                JOptionPane.showMessageDialog(view, "Format ID (DNI) musi zawierać 8 cyfr i jedną wielką literę.", "Błąd walidacji", JOptionPane.WARNING_MESSAGE);
                return;
            }

// --- WALIDACJA: Format E-mail (standardowy wzór) ---
            if (mEmail != null) {
                String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
                if (!mEmail.matches(emailRegex)) {
                    JOptionPane.showMessageDialog(view, "Podany adres e-mail ma nieprawidłowy format.", "Błąd walidacji", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

// --- WALIDACJA: Wiek powyżej 18 lat ---
            if (birthDate == null) {
                JOptionPane.showMessageDialog(view, "Data urodzenia jest wymagana.", "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            } else {
                java.util.Calendar calUrodzenia = java.util.Calendar.getInstance();
                calUrodzenia.setTime(birthDate);

                java.util.Calendar calDzis = java.util.Calendar.getInstance();

                // POPRAWIONE: Użycie Calendar.YEAR zamiast VK_UNDEFINED
                int wiek = calDzis.get(java.util.Calendar.YEAR) - calUrodzenia.get(java.util.Calendar.YEAR);

                // Korekta, jeśli urodziny w tym roku jeszcze nie nastąpiły
                if (calDzis.get(java.util.Calendar.MONTH) < calUrodzenia.get(java.util.Calendar.MONTH)
                        || (calDzis.get(java.util.Calendar.MONTH) == calUrodzenia.get(java.util.Calendar.MONTH)
                        && calDzis.get(java.util.Calendar.DAY_OF_MONTH) < calUrodzenia.get(java.util.Calendar.DAY_OF_MONTH))) {
                    wiek--;
                }

                if (wiek < 18) {
                    JOptionPane.showMessageDialog(view, "Klient musi mieć ukończone 18 lat (Obecny wiek: " + wiek + ").", "Błąd wieku", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            char catChar = category.charAt(0);
            String fmtEntryDate = dateFormat.format(entryDate);
            String fmtBirthDate = (birthDate != null) ? dateFormat.format(birthDate) : null;

            Session session = null;
            Transaction tr = null;

            try {
                session = sessionFactory.openSession();
                tr = session.beginTransaction();

                // Walidacja unikalności numeru DNI
                if (clientDAO.existDNI(session, mId)) {
                    if (clientToUpdate == null || !mId.equals(clientToUpdate.getMId())) {
                        JOptionPane.showMessageDialog(view, "ID już istnieje w bazie.", "Błąd", JOptionPane.ERROR_MESSAGE);
                        tr.rollback();
                        return;
                    }
                }

                if (clientToUpdate == null) {
                    Client nc = new Client(mNum, mName, mId, fmtEntryDate, catChar);
                    nc.setMPhone(mPhone);
                    nc.setMemailMember(mEmail);
                    nc.setMBirthdate(fmtBirthDate);
                    clientDAO.insertClient(session, nc);
                } else {
                    clientToUpdate.setMName(mName);
                    clientToUpdate.setMId(mId);
                    clientToUpdate.setMPhone(mPhone);
                    clientToUpdate.setMemailMember(mEmail);
                    clientToUpdate.setMstartingDateMember(fmtEntryDate);
                    clientToUpdate.setMcategoryMember(catChar);
                    clientToUpdate.setMBirthdate(fmtBirthDate);
                    clientDAO.updateClient(session, clientToUpdate);
                }

                tr.commit();
                if (clientControllerTable != null) {
                    clientControllerTable.showClients();
                }
                view.dispose();
                JOptionPane.showMessageDialog(null, "Dane zostały pomyślnie zapisane.");

            } catch (Exception ex) {
                if (tr != null) {
                    tr.rollback();
                }
                LOGGER.log(Level.SEVERE, "Błąd podczas zapisu.", ex);
                JOptionPane.showMessageDialog(view, "Błąd: " + ex.getMessage());
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        }
    }
}
