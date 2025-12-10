package Config;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateUtil {

    private static SessionFactory sessionFactory;
    private static StandardServiceRegistry serviceRegistry;

    
    public static SessionFactory buildSessionFactory(String user, String pass) {
        try {
            serviceRegistry = new StandardServiceRegistryBuilder()
                    .configure("hibernate.cfg.xml")
                    .applySetting("hibernate.connection.username", user)
                    .applySetting("hibernate.connection.password", pass)
                    .applySetting(
                            "hibernate.connection.url",
                            "jdbc:mariadb://172.18.1.241:3306/" + user
                    )
                    .build();

            Metadata metadata = new MetadataSources(serviceRegistry)
                    .getMetadataBuilder()
                    .build();

            sessionFactory = metadata.getSessionFactoryBuilder().build();
            return sessionFactory;

        } catch (HibernateException e) {
            System.err.println("Error creating SessionFactory: " + e.getMessage());

            if (serviceRegistry != null) {
                StandardServiceRegistryBuilder.destroy(serviceRegistry);
            }

            sessionFactory = null;
            return null;
        }
    }

    
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

 
    public static void close() {
        try {
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                sessionFactory.close();
            }
        } finally {
            sessionFactory = null;
            if (serviceRegistry != null) {
                StandardServiceRegistryBuilder.destroy(serviceRegistry);
                serviceRegistry = null;
            }
        }
    }
}
