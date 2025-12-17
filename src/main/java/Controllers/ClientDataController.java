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

public class ClientDataController {

    private static final Logger LOGGER = Logger.getLogger(ClientDataController.class.getName());
    private final SessionFactory sessionFactory;
    private final DataUpdateWindow view;
    private final ClientDAO clientDAO;
    private final ClientControllerTable clientControllerTable;
    private final Client clientToUpdate; 
    
    // Format zgodny z Twoimi Stringami w bazie danych
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

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

    public void initializeForm() {
        // Ustawienie etykiet widoku
        view.setFieldLabels("Imię i Nazwisko", "Numer Identyfikacyjny", "Telefon", 
                            "E-mail", "Data Przyjęcia", "Kategoria (A-D)", "Data Urodzenia");

        view.jBirthdayChooser.setVisible(true);
        view.jLabel6.setVisible(true);
        
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

    private void initializeFormWithAutoData() {
        Session session = null;
        try {
            session = sessionFactory.openSession();
            String maxNum = clientDAO.getMaxMemberNumber(session);
            view.jKod.setText(generateNextMemberNumber(maxNum));
            view.jKod.setEditable(false);
            
            // Domyślne wartości dla nowych pól
            view.setSelectedDate(new Date());
            view.setBirthdayDate(null); 

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, "Błąd inicjalizacji kodu klienta.", ex);
        } finally {
            if (session != null) session.close();
        }
    }

    private void populateForm() {
        // Dane tekstowe
        view.setKod(clientToUpdate.getMNum());
        view.jKod.setEditable(false);
        view.setNazwisko(clientToUpdate.getMName());
        view.setNumerIdentyfikacyjny(clientToUpdate.getMId());
        view.setTelefon(clientToUpdate.getMPhone());
        view.setEmail(clientToUpdate.getMemailMember());
        view.setKategoria(String.valueOf(clientToUpdate.getMcategoryMember()));

        // Data Przyjęcia
        try {
            String entryDateStr = clientToUpdate.getMstartingDateMember();
            if (entryDateStr != null && !entryDateStr.isEmpty()) {
                view.setSelectedDate(dateFormat.parse(entryDateStr));
            }
        } catch (Exception e) { view.setSelectedDate(new Date()); }

        // Data Urodzenia (używa pola mBirthdate z Twojego modelu)
        try {
            String birthDateStr = clientToUpdate.getMBirthdate();
            if (birthDateStr != null && !birthDateStr.isEmpty()) {
                view.setBirthdayDate(dateFormat.parse(birthDateStr));
            }
        } catch (Exception e) { view.setBirthdayDate(null); }
    }

    private String generateNextMemberNumber(String maxNum) {
        if (maxNum == null || maxNum.length() < 2) return "S001";
        try {
            String prefix = maxNum.substring(0, 1);
            int nextNum = Integer.parseInt(maxNum.substring(1)) + 1;
            return prefix + String.format("%03d", nextNum);
        } catch (Exception e) { return "S001"; }
    }
    
    private String getNullIfBlank(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    private class FormSubmitListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Pobranie danych z widoku
            String mNum = view.getKod().trim();
            String mId = view.getNumerIdentyfikacyjny().trim();
            String mName = view.getNazwisko().trim();
            String mPhone = getNullIfBlank(view.getTelefon());
            String mEmail = getNullIfBlank(view.getEmail());
            String category = view.getKategoria().trim().toUpperCase();
            
            Date entryDate = view.getSelectedDate();
            Date birthDate = view.getBirthdayDate();

            // Walidacja pól obowiązkowych
            if (mNum.isEmpty() || mId.isEmpty() || mName.isEmpty() || category.isEmpty() || entryDate == null) {
                JOptionPane.showMessageDialog(view, "Pola Kod, ID, Nazwisko, Kategoria i Data Przyjęcia są wymagane.", "Błąd", JOptionPane.WARNING_MESSAGE);
                return;
            }

            char catChar = category.charAt(0);
            String fmtEntryDate = dateFormat.format(entryDate);
            String fmtBirthDate = (birthDate != null) ? dateFormat.format(birthDate) : null;

            Session session = null;
            Transaction tr = null;

            try {
                session = sessionFactory.openSession();
                tr = session.beginTransaction();

                // Unikalność DNI
                if (clientDAO.existDNI(session, mId)) {
                    if (clientToUpdate == null || !mId.equals(clientToUpdate.getMId())) {
                        JOptionPane.showMessageDialog(view, "ID już istnieje w bazie.", "Błąd", JOptionPane.ERROR_MESSAGE);
                        tr.rollback();
                        return;
                    }
                }

                if (clientToUpdate == null) {
                    // Tworzenie nowego klienta
                    Client nc = new Client(mNum, mName, mId, fmtEntryDate, catChar);
                    nc.setMPhone(mPhone);
                    nc.setMemailMember(mEmail);
                    nc.setMBirthdate(fmtBirthDate); // Zintegrowane z Twoim modelem
                    clientDAO.insertClient(session, nc);
                } else {
                    // Aktualizacja istniejącego
                    clientToUpdate.setMName(mName);
                    clientToUpdate.setMId(mId);
                    clientToUpdate.setMPhone(mPhone);
                    clientToUpdate.setMemailMember(mEmail);
                    clientToUpdate.setMstartingDateMember(fmtEntryDate);
                    clientToUpdate.setMcategoryMember(catChar);
                    clientToUpdate.setMBirthdate(fmtBirthDate); // Zintegrowane z Twoim modelem
                    clientDAO.updateClient(session, clientToUpdate);
                }

                tr.commit();
                if (clientControllerTable != null) clientControllerTable.showClients();
                view.dispose();
                JOptionPane.showMessageDialog(null, "Dane zostały pomyślnie zapisane.");

            } catch (Exception ex) {
                if (tr != null) tr.rollback();
                LOGGER.log(Level.SEVERE, "Błąd podczas zapisu.", ex);
                JOptionPane.showMessageDialog(view, "Błąd: " + ex.getMessage());
            } finally {
                if (session != null) session.close();
            }
        }
    }
}