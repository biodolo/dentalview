package it.petruzzellis.dentalview.scene;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Gengiva implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 8912402336562498123L;

    @Attribute(name="quat",required=false)
    public String quat;
    
    @Attribute(name="position")
    public String position;
    
    @Attribute(name="name",required=false)
    public String name;
    
    @Attribute(name="file")
    public String file;
    
    @ElementList(type=Dente.class,inline=true,entry="TOOTH")
    public List<Dente> denti;
    
    @ElementList(type=Ponticello.class,inline=true,entry="PONTIC",required=false)
    public List<Ponticello> ponticelli;
}
