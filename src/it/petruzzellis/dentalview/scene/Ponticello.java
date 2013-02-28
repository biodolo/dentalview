package it.petruzzellis.dentalview.scene;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Ponticello implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = -1391827092730430239L;

    @Attribute(name="quat",required=false)
    public String quat;
    
    @Attribute(name="position")
    public String position;
    
    @Attribute(name="name",required=false)
    public String name;
      
    @Attribute(name="file")
    public String file;
}
