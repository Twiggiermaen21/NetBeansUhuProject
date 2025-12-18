package Models;
 
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Set;

/**
 * Klasa encyjna reprezentująca tabelę ACTIVITY w bazie danych.
 * Przechowuje informacje o zajęciach (aktywnościach) oferowanych przez system, 
 * ich harmonogramie, cenach oraz przypisanych trenerach i uczestnikach.
 * Implementuje interfejs {@link Serializable} w celu umożliwienia serializacji stanu obiektu.
 */
@Entity
@Table(name = "ACTIVITY")
@NamedQueries({
    @NamedQuery(name = "Activity.findAll", query = "SELECT a FROM Activity a"),
    @NamedQuery(name = "Activity.findByAId", query = "SELECT a FROM Activity a WHERE a.aId = :aId"),
    @NamedQuery(name = "Activity.findByAName", query = "SELECT a FROM Activity a WHERE a.aName = :aName"),
    @NamedQuery(name = "Activity.findByADescription", query = "SELECT a FROM Activity a WHERE a.aDescription = :aDescription"),
    @NamedQuery(name = "Activity.findByAPrice", query = "SELECT a FROM Activity a WHERE a.aPrice = :aPrice"),
    @NamedQuery(name = "Activity.findByADay", query = "SELECT a FROM Activity a WHERE a.aDay = :aDay"),
    @NamedQuery(name = "Activity.findByAHour", query = "SELECT a FROM Activity a WHERE a.aHour = :aHour")})
public class Activity implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unikalny identyfikator aktywności (Klucz podstawowy). */
    @Id
    @Basic(optional = false)
    @Column(name = "a_id")
    private String aId;

    /** Nazwa zajęć. */
    @Basic(optional = false)
    @Column(name = "a_name")
    private String aName;

    /** Opis lub typ aktywności. */
    @Basic(optional = false)
    @Column(name = "a_description")
    private String aDescription;

    /** Cena za udział w aktywności (PLN). */
    @Basic(optional = false)
    @Column(name = "a_price")
    private int aPrice;

    /** Dzień tygodnia, w którym odbywają się zajęcia. */
    @Basic(optional = false)
    @Column(name = "a_day", length = 20)
    private String aDay;

    /** Godzina rozpoczęcia zajęć. */
    @Basic(optional = false)
    @Column(name = "a_hour")
    private int aHour;

    /** * Relacja Many-to-Many z encją {@link Client}. 
     * Mapowanie realizowane przez tabelę pośrednią PERFORMS.
     */
    @JoinTable(name = "PERFORMS", joinColumns = {
        @JoinColumn(name = "p_id", referencedColumnName = "a_id")}, inverseJoinColumns = {
        @JoinColumn(name = "p_num", referencedColumnName = "m_num")})
    @ManyToMany
    private Set<Client> clientSet;

    /** * Relacja Many-to-One z encją {@link Trainer}. 
     * Wskazuje na trenera odpowiedzialnego za dane zajęcia.
     */
    @JoinColumn(name = "a_trainerInCharge", referencedColumnName = "t_cod")
    @ManyToOne
    private Trainer atrainerInCharge;

    /** Konstruktor bezargumentowy wymagany przez specyfikację JPA. */
    public Activity() {
    }

    /**
     * Konstruktor inicjalizujący obiekt z określonym identyfikatorem.
     * @param aId Unikalny kod aktywności.
     */
    public Activity(String aId) {
        this.aId = aId;
    }

    /**
     * Konstruktor inicjalizujący obiekt wszystkimi wymaganymi polami.
     * @param aId Identyfikator aktywności.
     * @param aName Nazwa zajęć.
     * @param aDescription Opis zajęć.
     * @param aPrice Cena.
     * @param aDay Dzień tygodnia.
     * @param aHour Godzina rozpoczęcia.
     */
    public Activity(String aId, String aName, String aDescription, int aPrice, String aDay, int aHour) {
        this.aId = aId;
        this.aName = aName;
        this.aDescription = aDescription;
        this.aPrice = aPrice;
        this.aDay = aDay;
        this.aHour = aHour;
    }

    public String getAId() {
        return aId;
    }

    public void setAId(String aId) {
        this.aId = aId;
    }

    public String getAName() {
        return aName;
    }

    public void setAName(String aName) {
        this.aName = aName;
    }

    public String getADescription() {
        return aDescription;
    }

    public void setADescription(String aDescription) {
        this.aDescription = aDescription;
    }

    public int getAPrice() {
        return aPrice;
    }

    public void setAPrice(int aPrice) {
        this.aPrice = aPrice;
    }

    public String getADay() {
        return aDay;
    }

    public void setADay(String aDay) {
        this.aDay = aDay;
    }

    public int getAHour() {
        return aHour;
    }

    public void setAHour(int aHour) {
        this.aHour = aHour;
    }

    public Set<Client> getClientSet() {
        return clientSet;
    }

    public void setClientSet(Set<Client> clientSet) {
        this.clientSet = clientSet;
    }

    public Trainer getAtrainerInCharge() {
        return atrainerInCharge;
    }

    public void setAtrainerInCharge(Trainer atrainerInCharge) {
        this.atrainerInCharge = atrainerInCharge;
    }

    /**
     * Generuje kod hash dla obiektu na podstawie identyfikatora.
     * @return Wartość hashCode.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (aId != null ? aId.hashCode() : 0);
        return hash;
    }

    /**
     * Porównuje dwa obiekty Activity pod kątem tożsamości.
     * @param object Obiekt do porównania.
     * @return True, jeśli identyfikatory są identyczne.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Activity)) {
            return false;
        }
        Activity other = (Activity) object;
        if ((this.aId == null && other.aId != null) || (this.aId != null && !this.aId.equals(other.aId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Models.Activity[ aId=" + aId + " ]";
    }

    /**
     * Zwraca nazwę aktywności do celów prezentacji w interfejsie użytkownika.
     * @return Nazwa aktywności (aName).
     */
    public String getDisplayName() {
        return this.aName;
    } 
}