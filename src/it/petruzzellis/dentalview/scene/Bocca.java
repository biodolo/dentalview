package it.petruzzellis.dentalview.scene;

import java.io.Serializable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Bocca implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -3747896542859247750L;
    @Element(name="GUMINF")
    public Gengiva gengiva_inf;
    @Element(name="GUMSUP")
    public Gengiva gengiva_sup;

}
