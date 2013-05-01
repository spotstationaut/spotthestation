package Entities;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value="EclipseLink-2.3.2.v20111125-r10461", date="2013-04-22T00:14:43")
@StaticMetamodel(NASAImageCommentTable.class)
public class NASAImageCommentTable_ { 

    public static volatile SingularAttribute<NASAImageCommentTable, Integer> id;
    public static volatile SingularAttribute<NASAImageCommentTable, String> time;
    public static volatile SingularAttribute<NASAImageCommentTable, String> location;
    public static volatile SingularAttribute<NASAImageCommentTable, String> comment;

}