package Controllers;

import Config.HibernateUtil;
import Views.ConnectionView;
import Views.MessageView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.hibernate.SessionFactory;

public class ConnectionController implements ActionListener {

    private final ConnectionView view;
    private final MessageView messageView;

    public ConnectionController() {
        this.view = new ConnectionView();
        this.messageView = new MessageView();

        addListeners();
        this.view.setVisible(true);
    }

    private void addListeners() {
        view.addConnectListener(this);
        view.addCancelListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        switch (command) {
            case "AppEntry":
                appEntry();
                break;
            case "Exit":
                System.exit(0);
                break;
        }

    }

    // ============================
    // LOGOWANIE
    // ============================
    public void appEntry() {
        String user = view.getUsername();
        String pass = new String(view.getPassword());

        SessionFactory sessionFactory = HibernateUtil.buildSessionFactory(user, pass);

        if (sessionFactory != null) {
            messageView.showSuccess("Connection successful!");
            view.dispose();
            new MainController(sessionFactory);
        } else {
            messageView.showError("Connection failed! Check credentials.");
        }
    }

}
