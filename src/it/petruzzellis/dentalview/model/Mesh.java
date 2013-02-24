package it.petruzzellis.dentalview.model;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public class Mesh  implements Serializable{

    private static final long serialVersionUID = 3834148306197079184L;
    public float[] color = { 1.0f, 1.0f, 1.0f, 1.0f };
	public float[] pos = { 0.0f, -0.5f, -0.5f };
	public float[] rot = { 0.33f, 0.0f, 0.0f, 1.0f };
	public List<Mesh> child_mesh_list = new ArrayList<Mesh>();
    private FloatBuffer mVertexBuffer=null;
    private int vertex_num = 0;

    public Mesh() throws Exception{
    	throw new Exception("NOT IMPLEMENTED");
    }
    
    public Mesh(float[] vertex,float[] color){
    	this(vertex);
    	if (color!=null)
    		if (color.length==4)
    			this.color=color;
    		else
    			for(int c=0;c<4 && c<color.length;c++){
    				this.color[c]=color[c];
    			}
    	
    }
    
    public Mesh(float[] vertex){
    	this.vertex_num =vertex.length/3;
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(this.vertex_num * 3 * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuf.asFloatBuffer();
        mVertexBuffer.put(vertex);
        mVertexBuffer.position(0);
    }
    
    public void draw(GL10 gl) {     
        //gl.glFrontFace(GL10.GL_CW);
        
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        gl.glColor4f(color[0],color[1],color[2],color[3]);
        
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        //gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        
        render(gl);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        //gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
        
    }
    
    public void render(GL10 gl) {
    	
    	gl.glPushMatrix();

        gl.glRotatef(rot[0],rot[1],rot[2],rot[3]);
    	gl.glTranslatef(pos[0],pos[1],pos[2]);
    	
    	    gl.glDrawArrays(GL10.GL_TRIANGLES, 0,vertex_num);
    	
        for(Mesh m:child_mesh_list)
        	m.render(gl);
    	gl.glPopMatrix();

    }
    
    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(color);
        oos.writeObject(pos);
        oos.writeObject(rot);
        oos.writeInt(vertex_num);
        float[] array=new float[3*vertex_num];
        mVertexBuffer.get(array);
        oos.writeObject(array);
        oos.writeObject(child_mesh_list);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        color=(float[])ois.readObject();
        pos=(float[])ois.readObject();
        rot=(float[])ois.readObject();
        vertex_num=ois.readInt(); 
        float[] array=(float[])ois.readObject();
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(4*3*vertex_num);
        byteBuf.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuf.asFloatBuffer();
        mVertexBuffer.put(array);
        mVertexBuffer.position(0);
        child_mesh_list=(ArrayList<Mesh>)ois.readObject();
    }

}
