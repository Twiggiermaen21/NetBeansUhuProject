/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
 *
 * @author stgad
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
    @Id
    @Basic(optional = false)
    @Column(name = "t_cod")
    private String tCod;
    @Basic(optional = false)
    @Column(name = "t_name")
    private String tName;
    @Basic(optional = false)
    @Column(name = "t_idNumber")
    private String tidNumber;
    @Column(name = "t_phoneNumber")
    private String tphoneNumber;
    @Column(name = "t_email")
    private String tEmail;
    @Basic(optional = false)
    @Column(name = "t_date")
    private String tDate;
    @Column(name = "t_nick")
    private String tNick;
    @OneToMany(mappedBy = "atrainerInCharge")
    private Set<Activity> activitySet;

    public Trainer() {
    }

    public Trainer(String tCod) {
        this.tCod = tCod;
    }

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

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (tCod != null ? tCod.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
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
        return "Models.Trainer[ tCod=" + tCod + " ]";
    }
    
}
