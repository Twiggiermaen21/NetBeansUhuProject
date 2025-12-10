package Views;

import Models.Client;

public class ClientView {

    public void showClientDetails(Client c) {
        System.out.println("New member:");
        System.out.println("Num: " + c.getMNum() + " Name: " + c.getMName() + " ID: " + c.getMId()
                + " Phone: " + c.getMPhone() + " Email: " + c.getMemailMember()
                + " Start: " + c.getMstartingDateMember() + " Category: " + c.getMcategoryMember());
    }

    public void showClientList(java.util.List<Client> clients) {
        if (clients.isEmpty()) {
            System.out.println("No clients found.");
            return;
        }
        System.out.println("===== Client List =====");
        for (Client c : clients) {
            showClientDetails(c);
            System.out.println("-------------------------");
        }
    }
}