package it.petruzzellis.dentalview.opengl;

import it.petruzzellis.dentalview.model.Mesh;
import it.petruzzellis.dentalview.model.parser.PLY;
import it.petruzzellis.dentalview.model.parser.ply.Parser;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLU;

public class OpenGLRenderer implements Renderer {

    private ArrayList<Mesh> mMeshList = new ArrayList<Mesh>();
    private float mMeshListRotation=0.0f;
    private int vn=2;

    public OpenGLRenderer(ArrayList<Mesh> meshList){
    	super();
    	mMeshList =meshList;
    }
    
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f); 
            
        gl.glClearDepthf(1.0f);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        gl.glDepthFunc(GL10.GL_LEQUAL);
        
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT,
                  GL10.GL_NICEST);
            
    }

    public void onDrawFrame(GL10 gl) {
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);        
        gl.glLoadIdentity();
        
        gl.glTranslatef(0.0f, 0.0f, -100.0f);
        gl.glRotatef(mMeshListRotation, 0.0f, 1.0f, 0.0f);
           
        for(Mesh m:mMeshList){
        	m.draw(gl);        	
        }
           
        gl.glLoadIdentity();                                    
            
        mMeshListRotation += 1.0f;
        
        
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL10.GL_PROJECTION);
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 10000.0f);
        gl.glViewport(0, 0, width, height);

        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    
}