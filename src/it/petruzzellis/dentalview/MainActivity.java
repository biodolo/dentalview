package it.petruzzellis.dentalview;

import it.petruzzellis.dentalview.dialog.FolderLayout;
import it.petruzzellis.dentalview.dialog.IFolderItemListener;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity implements IFolderItemListener {

	FolderLayout localFolders;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        localFolders = (FolderLayout)findViewById(R.id.localfolders);
        localFolders.setIFolderItemListener(this);
        localFolders.setDir("./mnt/extsd/505");//change directory if u want,default is root
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
    	Intent intent = new Intent(this, OpenGLViewActivity.class);
    	Bundle b = new Bundle();
    	b.putString("scene", file.getCanonicalPath()); //Your id
    	intent.putExtras(b); //Put your id to your next Intent
    	startActivity(intent);
    }

    
}



