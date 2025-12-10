package Views;

import java.util.Set;
import Models.Activity;

public class ActivityView {

    public void showTrainerActivities(Set<Activity> activities) {
        System.out.println("Name" + "\t" + "Day" + "\t" + "Hour" + "\t" + "Trainer");
        for (Activity a : activities) {
            System.out.println(a.getAName() + "\t" + a.getADay() + "\t" + a.getAHour()
                    + "\t" + a.getAtrainerInCharge().getTName());
        }
    }

    public void showActivityList(java.util.List<Object[]> activities) {
        if (activities.isEmpty()) {
            System.out.println("No activities found.");
            return;
        }
        System.out.println("Name\tDay\tPrice");
        for (Object[] row : activities) {
            System.out.println(row[0] + "\t" + row[1] + "\t" + row[2]);
        }
    }
}