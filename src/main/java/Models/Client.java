/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Models;

import jakarta.persistence.*;

import java.io.Serializable;
import java.util.Set;

/**
 *
 * @author stgad
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
    @Id
    @Basic(optional = false)
    @Column(name = "m_num")
    private String mNum;
    @Basic(optional = false)
    @Column(name = "m_name")
    private String mName;
    @Basic(optional = false)
    @Column(name = "m_id")
    private String mId;
    @Column(name = "m_birthdate")
    private String mBirthdate;
    @Column(name = "m_phone")
    private String mPhone;
    @Column(name = "m_emailMember")
    private String memailMember;
    @Basic(optional = false)
    @Column(name = "m_startingDateMember")
    private String mstartingDateMember;
    @Basic(optional = false)
    @Column(name = "m_categoryMember")
    private Character mcategoryMember;
    @ManyToMany(mappedBy = "clientSet")
    private Set<Activity> activitySet;

    public Client() {
    }

    public Client(String mNum) {
        this.mNum = mNum;
    }

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
        // TODO: Warning - this method won't work in the case the id fields are not set
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
