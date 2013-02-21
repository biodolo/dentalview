package it.petruzzellis.dentalview;

import it.petruzzellis.dentalview.opengl.OpenGLRenderer;
import android.app.Activity;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class OpenGLViewActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Go fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        
        GLSurfaceView view = new GLSurfaceView(this);
        Bundle extra = getIntent().getExtras();
        String filename = extra.getString("scene");
        OpenGLRenderer renderer= new OpenGLRenderer(filename);
        view.setRenderer(renderer);
        setContentView(view);
    }

}
