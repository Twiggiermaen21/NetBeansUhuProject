package Views;

import Models.Trainer;
import java.util.Set;

public class TrainerView {

    public void showTrainerDetails(Trainer t) {
        if (t == null) {
            System.out.println("No trainer to display.");
            return;
        }
        System.out.println("===== Trainer details =====");
        System.out.println(t.toString());
    }

    public void showTrainerList(Set<Trainer> trainers) {
        if (trainers == null || trainers.isEmpty()) {
            System.out.println("No trainers available.");
            return;
        }
        System.out.println("===== Trainers list =====");
        for (Trainer t : trainers) {
            if (t == null) continue;
            System.out.println(t.toString());
        }
    }
}