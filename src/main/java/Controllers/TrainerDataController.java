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
 * Kontroler odpowiedzialny za obsługę widoku {@link DataUpdateWindow} dla encji
 * {@link Trainer}. Zarządza logiką biznesową podczas dodawania nowych trenerów
 * oraz edycji istniejących rekordów. Klasa integruje komponent
 * {@code JDateChooser} do obsługi dat oraz zapewnia walidację unikalności
 * identyfikatorów trenerów w bazie danych.
 */
public class TrainerDataController {

    /**
     * Logger do rejestrowania zdarzeń diagnostycznych i błędów operacji na
     * danych trenera.
     */
    private static final Logger LOGGER = Logger.getLogger(TrainerDataController.class.getName());

    /**
     * Fabryka sesji Hibernate.
     */
    private final SessionFactory sessionFactory;

    /**
     * Okno dialogowe do aktualizacji danych.
     */
    private final DataUpdateWindow view;

    /**
     * Obiekt dostępu do danych dla encji Trainer.
     */
    private final TrainerDAO trainerDAO;

    /**
     * Kontroler zarządzający tabelą trenerów, używany do odświeżania listy po
     * zmianach.
     */
    private final TrainerControllerTable trainerControllerTable;

    /**
     * Instancja trenera przeznaczona do aktualizacji (null w przypadku
     * dodawania nowego).
     */
    private final Trainer trainerToUpdate;

    /**
     * Formatator służący do konwersji obiektów {@link Date} na format tekstowy
     * akceptowany przez bazę danych.
     */
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    /**
     * Konstruktor inicjalizujący kontroler danych trenera. Ustawia słuchacze
     * zdarzeń dla przycisków zatwierdzania i anulowania zmian w widoku.
     *
     * * @param sessionFactory Fabryka sesji Hibernate.
     * @param view Instancja okna formularza edycji.
     * @param trainerControllerTable Referencja do kontrolera tabeli w celu
     * odświeżania widoku.
     * @param trainerToUpdate Obiekt trenera do edycji lub null dla nowej
     * pozycji.
     */
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

    /**
     * Przygotowuje i konfiguruje pola formularza przed wyświetleniem
     * użytkownikowi. Ustawia odpowiednie etykiety, zarządza widocznością
     * komponentów daty i pseudonimu oraz wypełnia pola danymi w przypadku trybu
     * edycji.
     */
    public void initializeForm() {
        // --- KONFIGURACJA WIDOKU DLA TRENERA ---
        view.setFieldLabels("Imię i Nazwisko", "ID (Numer)", "Telefon", "E-mail",
                "Data zatrudnienia", "Nick/Pseudonim", "");

        // Zarządzanie widocznością komponentów formularza
        view.jDateChooser.setVisible(true);
        view.setFieldVisible(view.jLabel8, true);
        view.setFieldVisible(view.jKategoria, true);
        view.jBirthdayChooser.setVisible(false);
        view.jLabel6.setVisible(false);
        view.jComboBoxDay.setVisible(false);
        view.jComboBoxTime.setVisible(false);
        view.jComboBoxTrener.setVisible(false);
        
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
     * Przenosi dane z obiektu modelu {@link Trainer} do komponentów graficznych
     * widoku. Obsługuje również parsowanie daty zatrudnienia z formatu
     * tekstowego.
     */
    private void populateForm() {
        view.setKod(trainerToUpdate.getTCod());
        view.jKod.setEditable(false);

        view.setNazwisko(trainerToUpdate.getTName());
        view.setNumerIdentyfikacyjny(trainerToUpdate.getTidNumber());
        view.setTelefon(trainerToUpdate.getTphoneNumber());
        view.setEmail(trainerToUpdate.getTEmail());
        view.setKategoria(trainerToUpdate.getTNick());

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

    /**
     * Inicjalizuje formularz dla nowej encji, generując automatycznie kolejny
     * kod trenera oraz ustawiając bieżącą datę jako domyślną datę zatrudnienia.
     */
    private void initializeFormWithAutoData() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String maxNum = trainerDAO.getMaxTrainerCode(session);
            String newNum = generateNextTrainerCode(maxNum);

            view.jKod.setText(newNum);
            view.jKod.setEditable(false);

            view.setSelectedDate(new Date());
            view.setKategoria("");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd podczas inicjalizacji kodu trenera.", ex);
            view.jKod.setText("BŁĄD");
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    /**
     * Generuje następny logiczny kod trenera (np. T001 -> T002).
     *
     * * @param maxNum Aktualny najwyższy kod trenera pobrany z bazy danych.
     * @return Nowy wygenerowany kod w formacie "T00X".
     */
    private String generateNextTrainerCode(String maxNum) {
        if (maxNum == null || maxNum.length() < 2 || !maxNum.matches("[A-Z]\\d+")) {
            return "T001";
        }
        try {
            String prefix = maxNum.substring(0, 1);
            int nextNum = Integer.parseInt(maxNum.substring(1)) + 1;
            return prefix + String.format("%03d", nextNum);
        } catch (Exception e) {
            return "T001";
        }
    }

    /**
     * Przekształca pusty ciąg znaków lub ciąg zawierający same spacje na
     * wartość null.
     *
     * * @param value Ciąg znaków do przetworzenia.
     * @return Przetworzony ciąg lub null.
     */
    private String getNullIfBlank(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    /**
     * Klasa wewnętrzna obsługująca zdarzenie zatwierdzenia formularza.
     * Odpowiada za walidację pól, formatowanie daty oraz wykonanie operacji
     * trwałego zapisu lub aktualizacji w bazie danych przy użyciu sesji
     * Hibernate.
     */
    private class FormSubmitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            // 1. Zbieranie danych z pól formularza
            String tCod = view.getKod().trim();
            String tIdNumber = view.getNumerIdentyfikacyjny().trim();
            String tName = view.getNazwisko().trim();

            String tPhone = getNullIfBlank(view.getTelefon());
            String tEmail = getNullIfBlank(view.getEmail());
            String tNick = getNullIfBlank(view.getKategoria());

            Date selectedDate = view.getSelectedDate();

            // 2. Walidacja pól wymaganych
            // 2. Walidacja pól wymaganych
            if (tCod.isEmpty() || tIdNumber.isEmpty() || tName.isEmpty() || selectedDate == null) {
                JOptionPane.showMessageDialog(view, "Kod, Imię/Nazwisko, ID i Data są wymagane.", "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2a. WALIDACJA DNI (8 cyfr + wielka litera)
            if (!tIdNumber.matches("\\d{8}[A-Z]")) {
                JOptionPane.showMessageDialog(view, "Format ID musi zawierać 8 cyfr i jedną wielką literę.", "Błąd formatu", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2b. WALIDACJA DATY (brak dat przyszłych)
            if (selectedDate.after(new Date())) {
                JOptionPane.showMessageDialog(view, "Data zatrudnienia nie może być z przyszłości.", "Błąd daty", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (tEmail != null) {
                String emailRegex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
                if (!tEmail.matches(emailRegex)) {
                    JOptionPane.showMessageDialog(view,
                            "Podany adres e-mail ma nieprawidłowy format.",
                            "Błąd walidacji",
                            JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }

            String formattedDate = dateFormat.format(selectedDate);

            Session session = null;
            Transaction tr = null;

            try {
                session = sessionFactory.openSession();
                tr = session.beginTransaction();

                // 3. Sprawdzanie unikalności ID Numeru trenera
                if (trainerDAO.existTrainerID(session, tIdNumber)) {
                    boolean isAddingNew = trainerToUpdate == null;
                    boolean isChangingId = trainerToUpdate != null && !tIdNumber.equals(trainerToUpdate.getTidNumber());

                    if (isAddingNew || isChangingId) {
                        JOptionPane.showMessageDialog(view, "ID Trenera już istnieje w bazie.", "Błąd", JOptionPane.ERROR_MESSAGE);
                        tr.rollback();
                        return;
                    }
                }

                // 4. Wykonanie zapisu lub aktualizacji
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
                if (trainerControllerTable != null) {
                    trainerControllerTable.showTrainers();
                }
                view.dispose();
                JOptionPane.showMessageDialog(null, "Dane trenera zostały pomyślnie zapisane.");

            } catch (Exception ex) {
                if (tr != null) {
                    tr.rollback();
                }
                LOGGER.log(Level.SEVERE, "Błąd zapisu trenera.", ex);
                JOptionPane.showMessageDialog(view, "Błąd bazy danych: " + ex.getMessage());
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        }
    }
}
