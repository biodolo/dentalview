package it.petruzzellis.dentalview.dialog;

import java.io.File;

public interface IFolderItemListener {
	void OnCannotFileRead(File file);//implement what to do folder is Unreadable
    void OnFileClicked(File file) throws Exception;//What to do When a file is clicked
}
