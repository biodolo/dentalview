package it.petruzzellis.dentalview;

import it.petruzzellis.dentalview.dialog.FolderLayout;
import it.petruzzellis.dentalview.dialog.IFolderItemListener;
import it.petruzzellis.dentalview.model.Mesh;
import it.petruzzellis.dentalview.model.parser.ply.Parser;
import it.petruzzellis.dentalview.scene.Scene;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;

public class MainActivity extends Activity implements IFolderItemListener {

	FolderLayout localFolders;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        localFolders = (FolderLayout)findViewById(R.id.localfolders);
        localFolders.setIFolderItemListener(this);
        localFolders.setDir("/mnt/extsd/505");//change directory if u want,default is root
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
    //Your stuff here for Cannot open Folder
    public void OnCannotFileRead(File file) {
    	new AlertDialog.Builder(this)
        .setIcon(R.drawable.ic_launcher)
        .setTitle(
                "[" + file.getName()
                        + "] folder can't be read!")
        .setPositiveButton("OK",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog,
                            int which) {


                    }
                }).show();
    }


    //Your stuff here for file Click
    public void OnFileClicked(File file) throws Exception {
        new AsyncTask<File, Integer, Scene>() {

        @Override
        protected void onPostExecute(Scene result) {
            super.onPostExecute(result);
            if (result!=null){
                Intent intent = new Intent(MainActivity.this, OpenGLViewActivity.class);
                Bundle b = new Bundle();
                b.putSerializable("scene", result);
                intent.putExtras(b); 
                startActivity(intent);
            }else{
                new AlertDialog.Builder(MainActivity.this)
                .setIcon(R.drawable.ic_launcher)
                .setTitle("Error occurred during scene parsing")
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            public void onClick(DialogInterface dialog,
                                    int which) {


                            }
                        }).show();
            }
        }
        
        @Override
        protected Scene doInBackground(File... params) {
            Scene result=null;
            Serializer serializer= new Persister();
            try {
                result=serializer.read(Scene.class, params[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

    }.execute(file);
    }

    
}



