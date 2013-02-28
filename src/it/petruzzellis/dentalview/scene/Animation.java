package it.petruzzellis.dentalview.scene;

import java.io.Serializable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(strict=false)
public class Animation implements Serializable{
    /**
     * 
     */
    private static final long serialVersionUID = -8997888681398896671L;
    @Element(name="KEYFRAMES")
    public KeyFrames keyframes;
 
}
