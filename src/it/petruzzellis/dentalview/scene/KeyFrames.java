package it.petruzzellis.dentalview.scene;

import java.io.Serializable;
import java.util.List;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class KeyFrames implements Serializable{
   
    /**
     * 
     */
    private static final long serialVersionUID = -1461970560820019303L;
    @ElementList(type=Nodo.class,inline=true,entry="SCENENODE")
    public List<Nodo> nodi;

}
