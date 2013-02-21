package it.petruzzellis.dentalview.model.parser;

import it.petruzzellis.dentalview.model.Mesh;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import android.util.Log;

public class PLY {
	private enum FileFormat {
		ASCII, LITTLE_ENDIAN, BIG_ENDIAN
	};

	private ByteBuffer byteBuffer;
	private FileFormat fileFormat;

	private class Property {
		public Property(String name) {
			this.name = name;
		}

		String type = null;
		String listIdxType = null;
		String name;
	}

	private class Element {
		public Element(String type,int size) {
			this.type = type;
			this.size = size;
		}
		String type;
		int size;
		ArrayList<Property> properties = new ArrayList<Property>();
		ArrayList<ArrayList<Number>> dataList = new ArrayList<ArrayList<Number>>();
	};

	public Mesh loadModel(String filename) throws Exception {
		return loadModel(filename,new float[]{1.0f,1.0f,1.0f});
	}
	
	public Mesh loadModel(String filename,float[] defaultColor) throws Exception {
		if (defaultColor==null || defaultColor.length!=3)
			throw new Exception("Wrong default vertex color "+String.valueOf(defaultColor));
		ArrayList<Element> elements = new ArrayList<Element>();
		RandomAccessFile raf = null;
		String line;
		try {
			raf = new RandomAccessFile(filename, "r");
			line = raf.readLine();
			if (line == null || line.compareTo("ply") != 0) {
				throw new Exception("ERRROR: magic bytes 'ply' not found in file.");
			}
			do {
				line = raf.readLine();
				if (line != null)
					processLine(line.split(" "),elements);
			} while (line.compareTo("end_header") != 0);
			if (fileFormat == FileFormat.ASCII) {
				Element element=elements.iterator().next();
				do {
					line = raf.readLine();
					if (line != null){
						processDataLine(line.split(" "),element);
						if (element.dataList.size() == element.size)
							element=elements.iterator().next();
					}
				} while (line != null);
			} else {
				int remain = (int) (raf.length() - raf.getFilePointer());
				byteBuffer = ByteBuffer.allocate(remain);
				if (fileFormat == FileFormat.BIG_ENDIAN)
					byteBuffer.order(ByteOrder.BIG_ENDIAN);
				else
					byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

				byte[] buffer=new byte[remain];
				raf.read(buffer);
				byteBuffer.put(buffer);
				byteBuffer.position(0);
				try {
					processDataBuffer(elements);
				} catch (Exception e) {
					throw new Exception("Couldn't parse model. "
							+ e.toString() + " " + e.getMessage(),e);
				}
			}
		} catch (FileNotFoundException e) {
			throw new Exception("ERROR: File not found: " + filename);
		} catch (IOException e) {
			throw new Exception("ERROR reading file: " + e.getMessage());
		} finally {
			try {
				if (raf != null)
					raf.close();
			} catch (IOException e) {
				Log.w("PLY PARSER","ERROR closing file: " + e.getMessage());
			}
		}
		try {
			byteBuffer = null;
			Element vertexElement = null;
			Element faceElement = null;
			for (Element e : elements) {
				if (e.type.compareTo("vertex") == 0) {
					vertexElement = e;
				}
				if (e.type.compareTo("face") == 0) {
					faceElement = e;
				}
				if (vertexElement != null && faceElement != null)
					break;
			}

			if (vertexElement == null) {
				throw new Exception("ERROR: .ply file doesn't contain vertex definition.");
			}

			if (faceElement == null) {
				throw new Exception("ERROR: .ply file doesn't contain face definition.");
			}

			Mesh mesh = new Mesh();

			int coordIdx[] = new int[] { -1, -1, -1 };
			int colorIdx[] = new int[] { -1, -1, -1 };
			int coordOk = 0;
			int colorOk = 0;
			for (int i = 0; i < vertexElement.properties.size(); i++) {
				Property p = vertexElement.properties.get(i);
				if (p.name.equals("x")) {
					coordIdx[0] = i;
					coordOk|=1;
				} else if (p.name.equals("y")) {
					coordIdx[1] = i;
					coordOk|=2;
				} else if (p.name.equals("z")) {
					coordIdx[2] = i;
					coordOk|=4;
				} else if (p.name.equals("red")) {
					colorIdx[0] = i;
					colorOk|=1;
				} else if (p.name.equals("green")) {
					colorIdx[1] = i;
					colorOk|=2;
				} else if (p.name.equals("blue")) {
					colorIdx[2] = i;
					colorOk|=4;
				}
			}

			if (coordOk != 7) {
				throw new Exception("ERROR: x,y,z not properly defined for vertices in header.");
			}

			for (ArrayList<Number> vertex : vertexElement.dataList) {
				if (vertex.size() < 3) {
					throw new Exception("ERROR: invalid vertex coordinate count: "
									+ vertex.size() + " for vertex index ");
				}
				mesh.vertex.add(vertex.get(coordIdx[0]).floatValue());
				mesh.vertex.add(vertex.get(coordIdx[1]).floatValue());
				mesh.vertex.add(vertex.get(coordIdx[2]).floatValue());
				if (colorOk == 7) {
					mesh.color.add(vertex.get(colorIdx[0]).floatValue());
					mesh.color.add(vertex.get(colorIdx[1]).floatValue());
					mesh.color.add(vertex.get(colorIdx[2]).floatValue());
				}else{
					mesh.color.add(defaultColor[0]);
					mesh.color.add(defaultColor[1]);
					mesh.color.add(defaultColor[2]);
				}
			}

			for (ArrayList<Number> face : faceElement.dataList) {
				if (face.size() != 3) {
					throw new Exception("ERROR: Only triangle rendering implemented");
				} 
				for(Number fvi:face)
					mesh.faceVertexIndex.add(Byte.valueOf(fvi.byteValue()));
			}
			
			mesh.initBuffer();
			return mesh;
		} catch (Exception e) {
			throw new Exception("Couldn't parse model. " + e.toString() + " "
					+ e.getMessage(),e);
		}
		
	}


	private void processLine(String parts[],ArrayList<Element> elements)throws Exception {
		if (parts.length > 2){
			if (parts[0].compareTo("format") == 0) {
				if (parts[1].compareTo("ascii") == 0)
					fileFormat = FileFormat.ASCII;
				else if (parts[1].compareTo("binary_little_endian") == 0)
					fileFormat = FileFormat.LITTLE_ENDIAN;
				else if (parts[1].compareTo("binary_big_endian") == 0)
					fileFormat = FileFormat.BIG_ENDIAN;
				else {
					throw new Exception("ERROR: " + parts[0] + " header unknown: "
							+ parts[1]);
				}
			} else if (parts[0].compareTo("element") == 0) {
				elements.add(new Element(parts[1], Integer.valueOf(parts[2])));
			} else if (parts[0].compareTo("property") == 0) {
				Property p = new Property(parts[parts.length - 1]);
				if (parts[1].compareTo("list") == 0) {
					p.listIdxType = parts[2];
					p.type = parts[3];
				} else {
					p.type = parts[1];
				}
				elements.get(elements.size() - 1).properties.add(p);
			}
		}
	}


	private void processDataLine(String parts[],Element element) {
		int offset = 0;
		int cnt = 1;

		if (element.properties.get(0).listIdxType!=null) {
			cnt = Integer.valueOf(parts[0]);
			offset = 1;
		} else {
			cnt = element.properties.size();
		}

		ArrayList<Number> l = new ArrayList<Number>();
		for (int i = offset; i < cnt + offset; i++) {
			l.add(Float.valueOf(parts[i]));
		}
		element.dataList.add(l);
		
	}


	private Number parseType(String type) throws Exception {
		if (type.equals("char") || type.equals("uchar")) {
			return Integer.valueOf(byteBuffer.get());
		} else if (type.equals("short")) {
			short c = byteBuffer.getShort();
			return Integer.valueOf(c);
		} else if (type.equals("ushort")) {
			short c = byteBuffer.getShort();
			int v = Integer.valueOf(c);
			v += 32768; 
			return Integer.valueOf(v);
		} else if (type.equals("int")) {
			return Integer.valueOf(byteBuffer.getInt());
		} else if (type.equals("uint")) {
			int v = byteBuffer.getInt();
			v += 2147483648l; 
			return Integer.valueOf(v);
		} else if (type.equals("float")) {
			return Float.valueOf(byteBuffer.getFloat());
		} else if (type.equals("double")) {
			return Double.valueOf(byteBuffer.getDouble());
		} else {
			throw new Exception("ERROR: invalid data type: " + type);
		}
	}

	private void processDataBuffer(ArrayList<Element> elements) throws Exception{
		int eidx=0;
		for (Element element:elements) {
			String type = element.properties.get(0).type;
			String listIdxType = element.properties.get(0).listIdxType;
			boolean list = listIdxType!=null;
			int valCnt = element.properties.size();
			for (int j = 0; j < element.size; j++) {
				if (list) {
					valCnt = (Integer) parseType(listIdxType);
				}
				//Log.i("PLYParser", "element "+element.type+" "+j+"/"+element.size+" read "+valCnt+" "+type+" buffer remaining "+byteBuffer.remaining());			
				ArrayList<Number> data = new ArrayList<Number>();
				for (int i = 0; i < valCnt; i++) {
					data.add(parseType(type));
				}			
				element.dataList.add(data);
			}
		}
	}
}
