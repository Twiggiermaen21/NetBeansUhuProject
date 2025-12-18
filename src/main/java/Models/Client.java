package Models;

import jakarta.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Klasa encyjna reprezentująca tabelę CLIENT w bazie danych.
 * Przechowuje szczegółowe informacje o klientach (członkach) systemu, 
 * w tym dane kontaktowe, daty przystąpienia oraz przynależność do kategorii.
 * Klasa obsługuje relację wiele-do-wielu z zajęciami (Activity).
 */
@NamedNativeQuery(
        name = "Client.findByMcategoryMemberSQL",
        query = "SELECT * FROM CLIENT WHERE m_categoryMember = :mcategoryMember",
        resultClass = Client.class
)
@Entity
@Table(name = "CLIENT")
@NamedQueries({
    @NamedQuery(name = "Client.findAll", query = "SELECT c FROM Client c"),
    @NamedQuery(name = "Client.findByMNum", query = "SELECT c FROM Client c WHERE c.mNum = :mNum"),
    @NamedQuery(name = "Client.findByMName", query = "SELECT c FROM Client c WHERE c.mName = :mName"),
    @NamedQuery(name = "Client.findByMId", query = "SELECT c FROM Client c WHERE c.mId = :mId"),
    @NamedQuery(name = "Client.findByMBirthdate", query = "SELECT c FROM Client c WHERE c.mBirthdate = :mBirthdate"),
    @NamedQuery(name = "Client.findByMPhone", query = "SELECT c FROM Client c WHERE c.mPhone = :mPhone"),
    @NamedQuery(name = "Client.findByMemailMember", query = "SELECT c FROM Client c WHERE c.memailMember = :memailMember"),
    @NamedQuery(name = "Client.findByMstartingDateMember", query = "SELECT c FROM Client c WHERE c.mstartingDateMember = :mstartingDateMember"),
    @NamedQuery(name = "Client.findByMcategoryMember", query = "SELECT c FROM Client c WHERE c.mcategoryMember = :mcategoryMember")})
public class Client implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unikalny numer członkowski klienta (Klucz podstawowy). */
    @Id
    @Basic(optional = false)
    @Column(name = "m_num")
    private String mNum;

    /** Imię i nazwisko klienta. */
    @Basic(optional = false)
    @Column(name = "m_name")
    private String mName;

    /** Numer identyfikacyjny klienta (np. PESEL lub DNI). */
    @Basic(optional = false)
    @Column(name = "m_id")
    private String mId;

    /** Data urodzenia przechowywana jako ciąg znaków. */
    @Column(name = "m_birthdate")
    private String mBirthdate;

    /** Numer telefonu kontaktowego. */
    @Column(name = "m_phone")
    private String mPhone;

    /** Adres e-mail klienta. */
    @Column(name = "m_emailMember")
    private String memailMember;

    /** Data zarejestrowania klienta w systemie. */
    @Basic(optional = false)
    @Column(name = "m_startingDateMember")
    private String mstartingDateMember;

    /** Kategoria członkostwa (np. określająca poziom zniżek lub typ dostępu). */
    @Basic(optional = false)
    @Column(name = "m_categoryMember")
    private Character mcategoryMember;

    /** * Zbiór aktywności, w których uczestniczy dany klient.
     * Relacja wiele-do-wielu mapowana przez pole clientSet w encji {@link Activity}.
     */
    @ManyToMany(mappedBy = "clientSet")
    private Set<Activity> activitySet;

    /** Konstruktor domyślny wymagany przez Hibernate/JPA. */
    public Client() {
    }

    /**
     * Tworzy nowy obiekt klienta z przypisanym numerem członkowskim.
     * @param mNum Numer członkowski.
     */
    public Client(String mNum) {
        this.mNum = mNum;
    }

    /**
     * Tworzy nowy obiekt klienta z kompletem wymaganych danych.
     * @param mNum Numer członkowski.
     * @param mName Imię i nazwisko.
     * @param mId Numer identyfikacyjny.
     * @param mstartingDateMember Data rozpoczęcia członkostwa.
     * @param mcategoryMember Kategoria członka.
     */
    public Client(String mNum, String mName, String mId, String mstartingDateMember, Character mcategoryMember) {
        this.mNum = mNum;
        this.mName = mName;
        this.mId = mId;
        this.mstartingDateMember = mstartingDateMember;
        this.mcategoryMember = mcategoryMember;
    }

    public String getMNum() {
        return mNum;
    }

    public void setMNum(String mNum) {
        this.mNum = mNum;
    }

    public String getMName() {
        return mName;
    }

    public void setMName(String mName) {
        this.mName = mName;
    }

    public String getMId() {
        return mId;
    }

    public void setMId(String mId) {
        this.mId = mId;
    }

    public String getMBirthdate() {
        return mBirthdate;
    }

    public void setMBirthdate(String mBirthdate) {
        this.mBirthdate = mBirthdate;
    }

    public String getMPhone() {
        return mPhone;
    }

    public void setMPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    public String getMemailMember() {
        return memailMember;
    }

    public void setMemailMember(String memailMember) {
        this.memailMember = memailMember;
    }

    public String getMstartingDateMember() {
        return mstartingDateMember;
    }

    public void setMstartingDateMember(String mstartingDateMember) {
        this.mstartingDateMember = mstartingDateMember;
    }

    public Character getMcategoryMember() {
        return mcategoryMember;
    }

    public void setMcategoryMember(Character mcategoryMember) {
        this.mcategoryMember = mcategoryMember;
    }

    public Set<Activity> getActivitySet() {
        return activitySet;
    }

    public void setActivitySet(Set<Activity> activitySet) {
        this.activitySet = activitySet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (mNum != null ? mNum.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Client)) {
            return false;
        }
        Client other = (Client) object;
        if ((this.mNum == null && other.mNum != null) || (this.mNum != null && !this.mNum.equals(other.mNum))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Models.Client[ mNum=" + mNum + " ]";
    }

}