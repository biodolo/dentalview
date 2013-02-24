package it.petruzzellis.dentalview.model.parser.ply;

import android.annotation.SuppressLint;
import java.util.ArrayList;

@SuppressLint("DefaultLocale")
public class Element {
	public Element(int size) {
		this.size = size;
	}
	public int size;
	public ArrayList<Property> properties = new ArrayList<Property>();
}
