package it.petruzzellis.dentalview.model;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.opengles.GL10;

public class Mesh {
	public ArrayList<Float> vertex = new ArrayList<Float>();
	public ArrayList<Float> color = new ArrayList<Float>();
	public ArrayList<Byte> faceVertexIndex = new ArrayList<Byte>();
	public float[] pos = { 0.0f, 0.0f, 0.0f };
	public float[] rot = { 0.0f, 0.0f, 0.0f, 0.0f };
	public List<Mesh> child_mesh_list = new ArrayList<Mesh>();
    private FloatBuffer mVertexBuffer=null;
    private FloatBuffer mColorBuffer=null;
    private ByteBuffer  mFaceVertexIndexBuffer=null;

    public void initBuffer(){
        float[] tmp=new float[vertex.size()];
        int i=0;
        for(Float v:vertex){
        	tmp[i++]=v.floatValue();
        }
        ByteBuffer byteBuf = ByteBuffer.allocateDirect(vertex.size() * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mVertexBuffer = byteBuf.asFloatBuffer();
        mVertexBuffer.put(tmp);
        mVertexBuffer.position(0);
            
        tmp=new float[color.size()];
        i=0;
        for(Float c:color){
        	tmp[i++]=c.floatValue();
        }
        byteBuf = ByteBuffer.allocateDirect(color.size() * 4);
        byteBuf.order(ByteOrder.nativeOrder());
        mColorBuffer = byteBuf.asFloatBuffer();
        mColorBuffer.put(tmp);
        mColorBuffer.position(0);
            
        byte[] tmpIdx=new byte[faceVertexIndex.size()];
        i=0;
        for(Byte fvi:faceVertexIndex){
        	tmpIdx[i++]=fvi.byteValue();
        }
        mFaceVertexIndexBuffer = ByteBuffer.allocateDirect(faceVertexIndex.size());
        mFaceVertexIndexBuffer.put(tmpIdx);
        mFaceVertexIndexBuffer.position(0);
    }
    
    public void draw(GL10 gl) {     
    	if (mVertexBuffer==null || mColorBuffer==null || mFaceVertexIndexBuffer==null)
    		initBuffer();
        gl.glFrontFace(GL10.GL_CW);
        
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
        
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
         
        gl.glDrawElements(GL10.GL_LINE_LOOP, faceVertexIndex.size(), GL10.GL_UNSIGNED_BYTE, mFaceVertexIndexBuffer);
            
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
    }
}
