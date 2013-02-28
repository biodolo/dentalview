package it.petruzzellis.dentalview.scene;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Scene implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 4076788085530070934L;

    @Attribute(required=false)
    public String version="";

    @Element(name="STATIC_SCENE")
    public Bocca static_scene;
        
    @Element(name="ANIMATION_SECTION")
    public Animation animazione;
}
