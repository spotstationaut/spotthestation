/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Entities;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author xxc9071
 */
@Entity
@Table(name = "NASARegistrationTable")
@XmlRootElement
@NamedQueries(
{
    @NamedQuery(name = "NASARegistrationTable.findAll",
    query = "SELECT n FROM NASARegistrationTable n"), @NamedQuery(
    name = "NASARegistrationTable.findByRegID",
    query = "SELECT n FROM NASARegistrationTable n WHERE n.regID = :regID"), @NamedQuery(
    name = "NASARegistrationTable.findByZone",
    query = "SELECT n FROM NASARegistrationTable n WHERE n.zone = :zone")
})
public class NASARegistrationTable implements Serializable
{
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "regID")
    private String regID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "zone")
    private int zone;

    public NASARegistrationTable()
    {
    }

    public NASARegistrationTable(String regID)
    {
        this.regID = regID;
    }

    public NASARegistrationTable(String regID, int zone)
    {
        this.regID = regID;
        this.zone = zone;
    }

    public String getRegID()
    {
        return regID;
    }

    public void setRegID(String regID)
    {
        this.regID = regID;
    }

    public int getZone()
    {
        return zone;
    }

    public void setZone(int zone)
    {
        this.zone = zone;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (regID != null ? regID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof NASARegistrationTable))
        {
            return false;
        }
        NASARegistrationTable other = (NASARegistrationTable) object;
        if ((this.regID == null && other.regID != null) || (this.regID != null && !this.regID.equals(other.regID)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Entities.NASARegistrationTable[ regID=" + regID + " ]";
    }
    
}
