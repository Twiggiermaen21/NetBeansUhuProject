package Config;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

/**
 * Klasa narzędziowa do zarządzania cyklem życia obiektu SessionFactory w
 * Hibernate. Jest to klasyczne podejście typu "Utility Class" (lub Singleton)
 * do konfiguracji połączenia z bazą danych.
 */
public class HibernateUtil {

    // Globalne i statyczne instancje kluczowych obiektów Hibernate
    private static SessionFactory sessionFactory;
    private static StandardServiceRegistry serviceRegistry;

    /**
     * Buduje SessionFactory, używając podanych dynamicznie danych
     * uwierzytelniających (login i hasło) oraz nadpisując URL bazy danych.
     * Używa konfiguracji domyślnej z pliku hibernate.cfg.xml.
     *
     * @param username Login użytkownika bazy danych.
     * @param password Hasło użytkownika bazy danych.
     * @return Utworzony obiekt SessionFactory lub null w przypadku błędu.
     */
    public static SessionFactory buildSessionFactory(String username, String password) {
        try {
            // 1. Utworzenie rejestru usług (Service Registry)
            // Rejestr ten zarządza usługami, z których korzysta Hibernate.
            serviceRegistry = new StandardServiceRegistryBuilder()
                    // Ładowanie ustawień z pliku konfiguracyjnego
                    .configure("hibernate.cfg.xml")
                    // Nadpisanie dynamicznymi danymi: login, hasło
                    .applySetting("hibernate.connection.username", username)
                    .applySetting("hibernate.connection.password", password)
                    // Nadpisanie URL połączenia (z użyciem loginu jako nazwy bazy)
                    .applySetting(
                            "hibernate.connection.url",
                            "jdbc:mariadb://172.18.1.241:3306/" + username // Uwaga na stały adres IP/port
                    )
                    .build();

            // 2. Utworzenie metadanych
            // Metadane są używane do zbierania informacji o mapowaniach klas do tabel.
            MetadataSources metadataSources = new MetadataSources(serviceRegistry);
            Metadata metadata = metadataSources.getMetadataBuilder().build();

            // 3. Zbudowanie SessionFactory na podstawie metadanych
            sessionFactory = metadata.getSessionFactoryBuilder().build();

            return sessionFactory;

        } catch (HibernateException e) {
            System.err.println("Błąd podczas tworzenia SessionFactory: " + e.getMessage());

            // Ważne: W przypadku błędu, rejestr usług musi zostać zniszczony!
            if (serviceRegistry != null) {
                StandardServiceRegistryBuilder.destroy(serviceRegistry);
            }

            sessionFactory = null;
            return null;
        }
    }

    /**
     * Zwraca statyczną instancję SessionFactory. Zakłada, że wcześniej wywołano
     * buildSessionFactory().
     *
     * @return Statyczna instancja SessionFactory.
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Czyści zasoby, zamykając SessionFactory oraz niszcząc ServiceRegistry.
     * Należy to wywołać raz przy zamknięciu aplikacji.
     */
    public static void close() {
        try {
            // Zamknięcie SessionFactory, jeśli istnieje i nie jest już zamknięta
            if (sessionFactory != null && !sessionFactory.isClosed()) {
                sessionFactory.close();
            }
        } finally {
            // W sekcji 'finally' dbamy o ustawienie referencji na null i zniszczenie rejestru
            // niezależnie od tego, czy zamknięcie SessionFactory się powiodło.
            sessionFactory = null;

            if (serviceRegistry != null) {
                StandardServiceRegistryBuilder.destroy(serviceRegistry);
                serviceRegistry = null;
            }
        }
    }
}
