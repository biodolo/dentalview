package it.petruzzellis.dentalview.model.parser.ply;

public class Property {
	public Property(String name) {
		this.name = name;
	}

	public PropertyType type = null;
	public PropertyType listIdxType = null;
	public String name;
	
	@Override
	public String toString() {
		if (listIdxType==null)
			return name+" : "+type.toString();
		else
			return name+" : list("+listIdxType.toString()+","+type.toString()+")";
	}
}
