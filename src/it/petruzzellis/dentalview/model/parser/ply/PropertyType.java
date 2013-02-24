package it.petruzzellis.dentalview.model.parser.ply;

public enum PropertyType {
	CHAR(1),UCHAR(1),SHORT(2),USHORT(2),INT(4)  ,UINT(4)  ,FLOAT(4)  ,DOUBLE(8) ,
	INT8(1),UINT8(1),INT16(2),UINT16(2),INT32(4),UINT32(4),FLOAT32(4),FLOAT64(8);
	
	private int size;
	PropertyType(int size){
		this.size = size;
	}
	public int sizeOf(){
		return size;
	}
}
