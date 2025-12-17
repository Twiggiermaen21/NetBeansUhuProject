/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
 *
 * @author stgad
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
    @Id
    @Basic(optional = false)
    @Column(name = "a_id")
    private String aId;
    @Basic(optional = false)
    @Column(name = "a_name")
    private String aName;
    @Basic(optional = false)
    @Column(name = "a_description")
    private String aDescription;
    @Basic(optional = false)
    @Column(name = "a_price")
    private int aPrice;
    @Basic(optional = false)
    @Column(name = "a_day")
    private String aDay;
    @Basic(optional = false)
    @Column(name = "a_hour")
    private int aHour;
    @JoinTable(name = "PERFORMS", joinColumns = {
        @JoinColumn(name = "p_id", referencedColumnName = "a_id")}, inverseJoinColumns = {
        @JoinColumn(name = "p_num", referencedColumnName = "m_num")})
    @ManyToMany
    private Set<Client> clientSet;
    @JoinColumn(name = "a_trainerInCharge", referencedColumnName = "t_cod")
    @ManyToOne
    private Trainer atrainerInCharge;

    public Activity() {
    }

    public Activity(String aId) {
        this.aId = aId;
    }

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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (aId != null ? aId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
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
   public String getDisplayName() {
    return this.aName; // lub cokolwiek innego co ma widzieć użytkownik
} 
}
