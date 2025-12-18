package Models;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.util.Set;

/**
 * Klasa encyjna reprezentująca tabelę TRAINER w bazie danych.
 * Przechowuje szczegółowe informacje o trenerach pracujących w systemie, 
 * ich dane kontaktowe, identyfikatory oraz pseudonimy.
 * Klasa definiuje relację jeden-do-wielu z zajęciami (Activity), którymi trener zarządza.
 */
@Entity
@Table(name = "TRAINER")
@NamedQueries({
    @NamedQuery(name = "Trainer.findAll", query = "SELECT t FROM Trainer t"),
    @NamedQuery(name = "Trainer.findByTCod", query = "SELECT t FROM Trainer t WHERE t.tCod = :tCod"),
    @NamedQuery(name = "Trainer.findByTName", query = "SELECT t FROM Trainer t WHERE t.tName = :tName"),
    @NamedQuery(name = "Trainer.findByTidNumber", query = "SELECT t FROM Trainer t WHERE t.tidNumber = :tidNumber"),
    @NamedQuery(name = "Trainer.findByTphoneNumber", query = "SELECT t FROM Trainer t WHERE t.tphoneNumber = :tphoneNumber"),
    @NamedQuery(name = "Trainer.findByTEmail", query = "SELECT t FROM Trainer t WHERE t.tEmail = :tEmail"),
    @NamedQuery(name = "Trainer.findByTDate", query = "SELECT t FROM Trainer t WHERE t.tDate = :tDate"),
    @NamedQuery(name = "Trainer.findByTNick", query = "SELECT t FROM Trainer t WHERE t.tNick = :tNick")})
public class Trainer implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unikalny kod trenera (Klucz podstawowy). */
    @Id
    @Basic(optional = false)
    @Column(name = "t_cod")
    private String tCod;

    /** Imię i nazwisko trenera. */
    @Basic(optional = false)
    @Column(name = "t_name")
    private String tName;

    /** Numer identyfikacyjny trenera (np. DNI lub numer legitymacji). */
    @Basic(optional = false)
    @Column(name = "t_idNumber")
    private String tidNumber;

    /** Numer telefonu kontaktowego trenera. */
    @Column(name = "t_phoneNumber")
    private String tphoneNumber;

    /** Adres e-mail trenera. */
    @Column(name = "t_email")
    private String tEmail;

    /** Data zatrudnienia lub rejestracji trenera w systemie (jako String). */
    @Basic(optional = false)
    @Column(name = "t_date")
    private String tDate;

    /** Pseudonim lub nick trenera używany w systemie. */
    @Column(name = "t_nick")
    private String tNick;

    /** * Zbiór aktywności, za które dany trener jest odpowiedzialny.
     * Relacja jeden-do-wielu mapowana przez pole atrainerInCharge w encji {@link Activity}.
     */
    @OneToMany(mappedBy = "atrainerInCharge")
    private Set<Activity> activitySet;

    /** Konstruktor bezargumentowy wymagany przez specyfikację JPA/Hibernate. */
    public Trainer() {
    }

    /**
     * Tworzy obiekt trenera z przypisanym kodem.
     * @param tCod Unikalny kod trenera.
     */
    public Trainer(String tCod) {
        this.tCod = tCod;
    }

    /**
     * Tworzy obiekt trenera z kompletem wymaganych danych.
     * @param tCod Kod trenera.
     * @param tName Imię i nazwisko.
     * @param tidNumber Numer identyfikacyjny.
     * @param tDate Data zatrudnienia.
     */
    public Trainer(String tCod, String tName, String tidNumber, String tDate) {
        this.tCod = tCod;
        this.tName = tName;
        this.tidNumber = tidNumber;
        this.tDate = tDate;
    }

    public String getTCod() {
        return tCod;
    }

    public void setTCod(String tCod) {
        this.tCod = tCod;
    }

    public String getTName() {
        return tName;
    }

    public void setTName(String tName) {
        this.tName = tName;
    }

    public String getTidNumber() {
        return tidNumber;
    }

    public void setTidNumber(String tidNumber) {
        this.tidNumber = tidNumber;
    }

    public String getTphoneNumber() {
        return tphoneNumber;
    }

    public void setTphoneNumber(String tphoneNumber) {
        this.tphoneNumber = tphoneNumber;
    }

    public String getTEmail() {
        return tEmail;
    }

    public void setTEmail(String tEmail) {
        this.tEmail = tEmail;
    }

    public String getTDate() {
        return tDate;
    }

    public void setTDate(String tDate) {
        this.tDate = tDate;
    }

    public String getTNick() {
        return tNick;
    }

    public void setTNick(String tNick) {
        this.tNick = tNick;
    }

    public Set<Activity> getActivitySet() {
        return activitySet;
    }

    public void setActivitySet(Set<Activity> activitySet) {
        this.activitySet = activitySet;
    }

    /**
     * Generuje kod hash na podstawie kodu trenera.
     * @return Wartość hashCode.
     */
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (tCod != null ? tCod.hashCode() : 0);
        return hash;
    }

    /**
     * Porównuje dwa obiekty Trainer pod kątem ich tożsamości bazodanowej.
     * @param object Obiekt do porównania.
     * @return True, jeśli kody trenerów są identyczne.
     */
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Trainer)) {
            return false;
        }
        Trainer other = (Trainer) object;
        if ((this.tCod == null && other.tCod != null) || (this.tCod != null && !this.tCod.equals(other.tCod))) {
            return false;
        }
        return true;
    }

    @Override
public String toString() {
    // To co tu zwrócisz, pojawi się w liście rozwijanej
    return this.tName; 
}
    
}