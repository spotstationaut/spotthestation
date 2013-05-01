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
@Table(name = "NASAImageCommentTable")
@XmlRootElement
@NamedQueries(
{
    @NamedQuery(name = "NASAImageCommentTable.findAll",
    query = "SELECT n FROM NASAImageCommentTable n"), @NamedQuery(
    name = "NASAImageCommentTable.findById",
    query = "SELECT n FROM NASAImageCommentTable n WHERE n.id = :id"), @NamedQuery(
    name = "NASAImageCommentTable.findByLocation",
    query = "SELECT n FROM NASAImageCommentTable n WHERE n.location = :location"), @NamedQuery(
    name = "NASAImageCommentTable.findByComment",
    query = "SELECT n FROM NASAImageCommentTable n WHERE n.comment = :comment"), @NamedQuery(
    name = "NASAImageCommentTable.findByTime",
    query = "SELECT n FROM NASAImageCommentTable n WHERE n.time = :time")
})
public class NASAImageCommentTable implements Serializable
{
    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "ID")
    private Integer id;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "Location")
    private String location;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "Comment")
    private String comment;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "Time")
    private String time;

    public NASAImageCommentTable()
    {
    }

    public NASAImageCommentTable(Integer id)
    {
        this.id = id;
    }

    public NASAImageCommentTable(Integer id, String location, String comment, String time)
    {
        this.id = id;
        this.location = location;
        this.comment = comment;
        this.time = time;
    }

    public Integer getId()
    {
        return id;
    }

    public void setId(Integer id)
    {
        this.id = id;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getComment()
    {
        return comment;
    }

    public void setComment(String comment)
    {
        this.comment = comment;
    }

    public String getTime()
    {
        return time;
    }

    public void setTime(String time)
    {
        this.time = time;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof NASAImageCommentTable))
        {
            return false;
        }
        NASAImageCommentTable other = (NASAImageCommentTable) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "Entities.NASAImageCommentTable[ id=" + id + " ]";
    }
    
}
