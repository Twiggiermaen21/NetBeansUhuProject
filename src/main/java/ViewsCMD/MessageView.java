package ViewsCMD;

import java.io.IOException;

/**
 * Klasa widoku odpowiedzialna za interakcję z użytkownikiem poprzez komunikaty.
 * Zapewnia metody do wyświetlania menu konsolowych, komunikatów tekstowych 
 * oraz graficznych okien dialogowych informujących o sukcesach lub błędach operacji.
 */
public class MessageView {

    /**
     * Wyświetla główne menu aplikacji w konsoli.
     * Umożliwia użytkownikowi wybór między modułami Członków, Trenerów i Aktywności.
     */
    public void menu() {
        System.out.println("***********************************");
        System.out.println("CLUB");
        System.out.println("***********************************");
        System.out.println("1. Members");
        System.out.println("2. Trainers");
        System.out.println("3. Activities");
        System.out.println("4. Exit");
        System.out.println("Enter option: ");
    }

    /**
     * Wyświetla podmenu zarządzania trenerami.
     * Przed wyświetleniem menu czyści konsolę, aby zapewnić lepszą czytelność.
     */
    public void trainerMenu() {
        clearConsole();

        System.out.println("***********************************");
        System.out.println("Trainer Management");
        System.out.println("***********************************");
        System.out.println("1. Activities by Trainer");
        System.out.println("2. Back to main menu");
        System.out.println("Enter option: ");
    }

    /**
     * Wyświetla sformatowany komunikat informacyjny w konsoli.
     * @param text Treść komunikatu do wyświetlenia.
     */
    public void consoleMessage(String text) {
        System.out.println("***********************************");
        System.out.println(text);
        System.out.println("***********************************");
    }

    /**
     * Wyświetla sformatowany komunikat o błędzie w konsoli.
     * @param text Tytuł lub opis kontekstu błędu.
     * @param error Szczegółowa treść błędu lub wyjątku.
     */
    public void consoleMessage(String text, String error) {
        System.out.println("***********************************");
        System.out.println(text);
        System.out.println(error);
        System.out.println("***********************************");
    }

    /**
     * Wyświetla graficzne okno dialogowe informujące o pomyślnym wykonaniu operacji.
     * @param message Treść komunikatu sukcesu.
     */
    public void showSuccess(String message) {
        javax.swing.JOptionPane.showMessageDialog(null, message, "Success", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Wyświetla graficzne okno dialogowe informujące o wystąpieniu błędu.
     * @param message Treść komunikatu o błędzie.
     */
    public void showError(String message) {
        javax.swing.JOptionPane.showMessageDialog(null, message, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Czyści okno konsoli. 
     * Próbuje wykonać komendę systemową "cls". W przypadku niepowodzenia (np. w systemach 
     * innych niż Windows lub przy braku uprawnień) wykonuje "pseudoczyszczenie" 
     * poprzez wypisanie pustych linii.
     */
    private void clearConsole() {
        try {
            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
        } catch (IOException | InterruptedException | SecurityException e) {
            for (int i = 0; i < 50; i++) {
                System.out.println();
            }
            Thread.currentThread().interrupt();
        }
    }
}