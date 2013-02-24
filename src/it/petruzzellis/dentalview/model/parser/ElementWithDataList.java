package it.petruzzellis.dentalview.model.parser;

import it.petruzzellis.dentalview.model.parser.ply.Element;

import java.util.ArrayList;

public class ElementWithDataList extends Element {
	
	public ElementWithDataList(String type, int size) {
		super(size);
		this.stringType=type;
	}

	public String stringType;
	
	public ArrayList<ArrayList<Number>> dataList = new ArrayList<ArrayList<Number>>();
}
