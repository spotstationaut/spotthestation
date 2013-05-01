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
import javax.persistence.Lob;
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
@Table(name = "NASAImageTable")
@XmlRootElement
@NamedQueries(
{
    @NamedQuery(name = "NASAImageTable.findAll",
    query = "SELECT n FROM NASAImageTable n"), @NamedQuery(
    name = "NASAImageTable.findByLocation",
    query = "SELECT n FROM NASAImageTable n WHERE n.location = :location"), @NamedQuery(
    name = "NASAImageTable.findByComment",
    query = "SELECT n FROM NASAImageTable n WHERE n.comment = :comment")
})
public class NASAImageTable implements Serializable
{
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "Location")
    private String location;
    @Lob
    @Column(name = "Image")
    private byte[] image;
    @Size(max = 300)
    @Column(name = "Comment")
    private String comment;

    public NASAImageTable()
    {
    }

    public NASAImageTable(String location)
    {
        this.location = location;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public byte[] getImage()
    {
        return image;
    }

    public void setImage(byte[] image)
    {
        this.image = image;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (location != null ? location.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof NASAImageTable))
        {
            return false;
        }
        NASAImageTable other = (NASAImageTable) object;
        if ((this.location == null && other.location != null) || (this.location != null && !this.location.equals(other.location)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Entities.NASAImageTable[ location=" + location + " ]";
    }
    
}
