package Views;

import java.io.IOException;

public class MessageView {

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

    public void trainerMenu() {
        clearConsole();

        System.out.println("***********************************");
        System.out.println("Trainer Management");
        System.out.println("***********************************");
        System.out.println("1. Activities by Trainer");
        System.out.println("2. Back to main menu");
        System.out.println("Enter option: ");
    }

    public void consoleMessage(String text) {
        System.out.println("***********************************");
        System.out.println(text);
        System.out.println("***********************************");
    }

    public void consoleMessage(String text, String error) {
        System.out.println("***********************************");
        System.out.println(text);
        System.out.println(error);
        System.out.println("***********************************");
    }

    public void showSuccess(String message) {
        javax.swing.JOptionPane.showMessageDialog(null, message, "Success", javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    public void showError(String message) {
        javax.swing.JOptionPane.showMessageDialog(null, message, "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
    }

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