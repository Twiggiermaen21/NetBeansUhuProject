package Controllers;

import Models.Activity;
import Models.ActivityDAO;
import Views.DataUpdateWindow;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import Utils.ActivityControllerTable;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// Zmieniono na int, ponieważ model Activity używa int dla ceny i godziny.
// import java.math.BigDecimal; 

public class ActivityDataController {

    private final SessionFactory sessionFactory;
    private final DataUpdateWindow view;
    private final ActivityDAO activityDAO;
    private final ActivityControllerTable activityControllerTable;
    private final Activity activityToUpdate; 

    // =========================================================================
    // KONSTRUKTOR
    // =========================================================================

    public ActivityDataController(SessionFactory sessionFactory, DataUpdateWindow view, ActivityControllerTable activityControllerTable, Activity activityToUpdate) {
        // ... (kod bez zmian)
        this.sessionFactory = sessionFactory;
        this.view = view;
        this.activityDAO = new ActivityDAO(); 
        this.activityControllerTable = activityControllerTable;
        this.activityToUpdate = activityToUpdate;

        this.view.addAkceptujListener(new FormSubmitListener());
        this.view.addAnulujListener(e -> view.dispose());
    }

    // =========================================================================
    // METODY TRYBU EDYCJI/DODAWANIA (Initialize i Populate)
    // =========================================================================

    public void initializeForm() {
        // Ujednolicone etykiety (zakładając, że mapujemy na: Kod, Nazwa, Koszt, Czas, Opis/Dzień)
        // view.setFieldLabels(new String[]{"Kod:", "Nazwa:", "Koszt (PLN):", "Czas (godz.):", "Dzień Tygodnia:", "Opis:"});

        if (activityToUpdate != null) {
            populateForm();
            view.jAkceptuj.setText("ZAPISZ ZMIANY");
            view.setTitle("Edycja Aktywności: " + activityToUpdate.getAId()); // Użyto getAId()
        } else {
            initializeFormWithAutoData();
            view.jAkceptuj.setText("DODAJ AKTYWNOŚĆ");
            view.setTitle("Dodawanie Nowej Aktywności");
        }
    }
    
    // NOTE: Wymaga getMaxActivityCode() w DAO (powinno być zaimplementowane)
    private void initializeFormWithAutoData() {
        // Prawidłowa implementacja wymaga użycia DAO do generacji nowego kodu
        view.jKod.setText("A001"); 
        view.jKod.setEditable(false);
    }

    private void populateForm() {
        view.setKod(activityToUpdate.getAId()); // Użyto setKod()
        view.jKod.setEditable(false); 
        
        // Mapowanie pól (założenia z poprzedniej dyskusji):
        view.setNazwisko(activityToUpdate.getAName());
        view.setNick(String.valueOf(activityToUpdate.getAPrice())); // jNick = Cena
        view.setKategoria(String.valueOf(activityToUpdate.getAHour())); // jKategoria = Godzina
        
        // view.setNumerIdentyfikacyjny(activityToUpdate.getADay()); // jNumer = Dzień
        // view.setEmail(activityToUpdate.getADescription()); // jEmail = Opis
    }

    // =========================================================================
    // LISTENER (OBSŁUGA ZAPISU / EDYCJI)
    // =========================================================================

    private class FormSubmitListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            
            // Deklaracja zmiennych Session i Transaction na zewnątrz try-catch
            Session session = null;
            Transaction tr = null;

            // 1. Zbieranie danych (Zmieniono: Użycie getKod, getNick, getKategoria)
            String aIdFromForm = view.getKod().trim(); // Kod (klucz)
            String aName = view.getNazwisko().trim();
            
            // Mapowanie: jNick (Cena), jKategoria (Godzina), jNumer (Dzień), jEmail (Opis)
            String priceStr = view.getNick().trim();
            String hourStr = view.getKategoria().trim();
            String aDay = view.getNumerIdentyfikacyjny().trim(); // Założenie mapowania
            String aDescription = view.getEmail().trim();         // Założenie mapowania
            
            int aPrice;
            int aHour;
            try {
                aPrice = Integer.parseInt(priceStr);
                aHour = Integer.parseInt(hourStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view, "Cena (" + priceStr + ") i Czas (godzina) (" + hourStr + ") muszą być poprawnymi liczbami całkowitymi.", "Błąd Walidacji Numerycznej", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // 2. Walidacja wstępna
            if (aIdFromForm.isEmpty() || aName.isEmpty() || aDay.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Kod, Nazwa i Dzień Aktywności muszą być wypełnione.", "Błąd Walidacji", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            try {
                session = sessionFactory.openSession();
                tr = session.beginTransaction();
                
                // Walidacja unikalności Kodu Aktywności (ID)
                if (activityDAO.existAId(session, aIdFromForm) && activityToUpdate == null) {
                    JOptionPane.showMessageDialog(view, "BŁĄD: Kod Aktywności (" + aIdFromForm + ") już istnieje w bazie.", "Błąd Walidacji", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 4. TRYB ZAPISU: DODAWANIE czy EDYCJA?
                if (activityToUpdate == null) {
                    // --- DODAWANIE ---
                    Activity newActivity = new Activity(aIdFromForm, aName, aDescription, aPrice, aDay, aHour); 
                    // TrainerInCharge jest opcjonalny/nieobsługiwany w formularzu, pomijamy
                    
                    activityDAO.insertActivity(session, newActivity);
                    
                    JOptionPane.showMessageDialog(view, "Aktywność: " + aName + " została pomyślnie dodana.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                
                } else {
                    // --- EDYCJA ---
                    activityToUpdate.setAName(aName);
                    activityToUpdate.setADescription(aDescription);
                    activityToUpdate.setAPrice(aPrice);
                    activityToUpdate.setADay(aDay);
                    activityToUpdate.setAHour(aHour);
                    
                    activityDAO.updateActivity(session, activityToUpdate);
                    
                    JOptionPane.showMessageDialog(view, "Aktywność: " + aName + " została pomyślnie zaktualizowana.", "Sukces", JOptionPane.INFORMATION_MESSAGE);
                }

                tr.commit();

                // 5. Odświeżenie tabeli i zamknięcie
                if (activityControllerTable != null) {
                    activityControllerTable.showActivities();
                }
                view.dispose();

            } catch (Exception ex) {
                if (tr != null && tr.isActive()) {
                    tr.rollback();
                }
                JOptionPane.showMessageDialog(view, "Błąd podczas zapisu/edycji aktywności: " + ex.getMessage(), "Błąd DB", JOptionPane.ERROR_MESSAGE);
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        }
    }
}