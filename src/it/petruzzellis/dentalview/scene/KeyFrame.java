package it.petruzzellis.dentalview.scene;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;


@Root(strict=false)
public class KeyFrame implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = 506629307817338237L;
    @Attribute(name="visibility")
    public boolean visibility;
    @Attribute(name="scale")
    public String scale;
    @Attribute(name="quat")
    public String quat;
    @Attribute(name="position")
    public String position;
    @Attribute(name="index")
    public String index;


}
