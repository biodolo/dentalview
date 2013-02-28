package it.petruzzellis.dentalview.scene;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Nodo implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = -3924575946196717343L;

    @Attribute(name="name")
    public String name;
    
    @ElementList(type=KeyFrame.class,inline=true,entry="keyframes")
    public List<KeyFrame> nodi;
}
