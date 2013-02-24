package it.petruzzellis.dentalview.model.parser;

import it.petruzzellis.dentalview.model.Mesh;
import it.petruzzellis.dentalview.model.parser.ply.FileFormat;
import it.petruzzellis.dentalview.model.parser.ply.Property;
import it.petruzzellis.dentalview.model.parser.ply.PropertyType;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;

import android.util.Log;

public class PLY {

	private static final String TAG = "PLYPARSER";;

	private ByteBuffer byteBuffer;
	private FileFormat fileFormat;

	public Mesh loadModel2(String filename) throws Exception {
		return loadModel2(filename, new float[] { 1.0f, 1.0f, 1.0f, 1.0f });
	}

	public Mesh loadModel2(String filename, float[] color) throws Exception {
		if (color == null || color.length < 3 || color.length > 4)
			throw new Exception("Wrong default vertex color "
					+ String.valueOf(color));
		ArrayList<ElementWithDataList> elements = new ArrayList<ElementWithDataList>();
		RandomAccessFile raf = null;
		String line;
		try {
			raf = new RandomAccessFile(filename, "r");
			line = raf.readLine();
			if (line == null || line.compareTo("ply") != 0) {
				throw new Exception(
						"ERRROR: magic bytes 'ply' not found in file.");
			}
			do {
				line = raf.readLine();
				if (line != null)
					processLine(line.split(" "), elements);
			} while (line.compareTo("end_header") != 0);
			if (fileFormat == FileFormat.ASCII) {
				Iterator<ElementWithDataList> it_e = elements.iterator();
				ElementWithDataList element = it_e.next();
				do {
					line = raf.readLine();
					if (line != null) {
						processDataLine(line.split(" "), element);
						if (element.dataList.size() == element.size) {
							if (it_e.hasNext()) {
								element = it_e.next();
							} else
								break;
						}

					} else
						break;

				} while (true);
			} else {
				int remain = (int) (raf.length() - raf.getFilePointer());
				byteBuffer = ByteBuffer.allocate(remain);
				if (fileFormat == FileFormat.BINARY_BIG_ENDIAN)
					byteBuffer.order(ByteOrder.BIG_ENDIAN);
				else
					byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

				byte[] buffer = new byte[remain];
				raf.read(buffer);
				byteBuffer.put(buffer);
				byteBuffer.position(0);
				try {
					processDataBuffer(elements);
				} catch (Exception e) {
					throw new Exception("Couldn't parse model. " + e.toString()
							+ " " + e.getMessage(), e);
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
				Log.w(TAG, "ERROR closing file: " + e.getMessage());
			}
		}
		try {
			byteBuffer = null;
			ElementWithDataList vertexElement = null;
			ElementWithDataList faceElement = null;
			for (ElementWithDataList e : elements) {
				if (e.stringType.compareTo("vertex") == 0) {
					vertexElement = e;
				}
				if (e.stringType.compareTo("face") == 0) {
					faceElement = e;
				}
				if (vertexElement != null && faceElement != null)
					break;
			}

			if (vertexElement == null) {
				throw new Exception(
						"ERROR: .ply file doesn't contain vertex definition.");
			}

			if (faceElement == null) {
				throw new Exception(
						"ERROR: .ply file doesn't contain face definition.");
			}

			return meshFromElement(vertexElement, faceElement, color);
		} catch (Exception e) {
			throw new Exception("Couldn't parse model. " + e.toString() + " "
					+ e.getMessage(), e);
		}

	}

	public Mesh meshFromElement(ElementWithDataList vertexElement,
			ElementWithDataList faceElement) throws Exception {
		return meshFromElement(vertexElement, faceElement, new float[] { 1.0f,
				1.0f, 1.0f, 1.0f });
	}

	public Mesh meshFromElement(ElementWithDataList vertexElement,
			ElementWithDataList faceElement, float[] color) throws Exception {

		int coordIdx[] = new int[] { -1, -1, -1 };
		int coordOk = 0;
		for (int i = 0; i < vertexElement.properties.size(); i++) {
			Property p = vertexElement.properties.get(i);
			if (p.name.equals("x")) {
				coordIdx[0] = i;
				coordOk |= 1;
			} else if (p.name.equals("y")) {
				coordIdx[1] = i;
				coordOk |= 2;
			} else if (p.name.equals("z")) {
				coordIdx[2] = i;
				coordOk |= 4;
			}
		}

		if (coordOk != 7) {
			throw new Exception(
					"ERROR: x,y,z not properly defined for vertices in header.");
		}

		float[] mesh_vertex = new float[faceElement.dataList.size() * 3 * 3];// 3
																				// vertici
																				// da
																				// 3
																				// coordinate
		int v = 0;
		int f = 0;
		for (ArrayList<Number> face : faceElement.dataList) {
			if (face.size() != 3) {
				throw new Exception(
						"ERROR: Only triangle rendering implemented");
			}
			f += 1;
			if (f % 1000 == 0) {
				Log.d(TAG, "Face " + f + " vertex " + v);
			}
			for (Number fvi : face) {
				ArrayList<Number> vertex = vertexElement.dataList.get(fvi
						.intValue());
				mesh_vertex[v++] = (vertex.get(coordIdx[0]).floatValue());
				mesh_vertex[v++] = (vertex.get(coordIdx[1]).floatValue());
				mesh_vertex[v++] = (vertex.get(coordIdx[2]).floatValue());
			}
		}
		return new Mesh(mesh_vertex);
	}

	private void processLine(String parts[],
			ArrayList<ElementWithDataList> elements) throws Exception {
		if (parts.length > 2) {
			if (parts[0].compareTo("format") == 0) {
				if (parts[1].compareTo("ascii") == 0)
					fileFormat = FileFormat.ASCII;
				else if (parts[1].compareTo("binary_little_endian") == 0)
					fileFormat = FileFormat.BINARY_LITTLE_ENDIAN;
				else if (parts[1].compareTo("binary_big_endian") == 0)
					fileFormat = FileFormat.BINARY_BIG_ENDIAN;
				else {
					throw new Exception("ERROR: " + parts[0]
							+ " header unknown: " + parts[1]);
				}
			} else if (parts[0].compareTo("element") == 0) {
				elements.add(new ElementWithDataList(parts[1], Integer
						.valueOf(parts[2])));
			} else if (parts[0].compareTo("property") == 0) {
				Property p = new Property(parts[parts.length - 1]);
				if (parts[1].compareTo("list") == 0) {
					p.listIdxType = PropertyType.valueOf(parts[2]);
					p.type = PropertyType.valueOf(parts[3]);
				} else {
					p.type = PropertyType.valueOf(parts[1]);
				}
				elements.get(elements.size() - 1).properties.add(p);
			}
		}
	}

	private void processDataLine(String parts[], ElementWithDataList element) {
		int offset = 0;
		int cnt = 1;

		if (element.properties.get(0).listIdxType != null) {
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

	private Number parseType(PropertyType type) throws Exception {
		short s;
		int i;
		switch (type) {
		case CHAR:
		case UCHAR:
		case INT8:
		case UINT8:
			return Integer.valueOf(byteBuffer.get());
		case SHORT:
		case INT16:
			s = byteBuffer.getShort();
			return Integer.valueOf(s);
		case USHORT:
		case UINT16:
			s = byteBuffer.getShort();
			i = Integer.valueOf(s);
			i += 32768;
			return Integer.valueOf(i);
		case INT:
		case INT32:
			return Integer.valueOf(byteBuffer.getInt());
		case UINT:
		case UINT32:
			i = byteBuffer.getInt();
			i += 2147483648l;
			return Integer.valueOf(i);
		case FLOAT:
		case FLOAT32:
			return Float.valueOf(byteBuffer.getFloat());
		case DOUBLE:
		case FLOAT64:
			return Double.valueOf(byteBuffer.getDouble());
		default:
			throw new Exception("ERROR: invalid data type: " + type);
		}
	}

	private void processDataBuffer(ArrayList<ElementWithDataList> elements)
			throws Exception {
		for (ElementWithDataList element : elements) {
			PropertyType type = element.properties.get(0).type;
			PropertyType listIdxType = element.properties.get(0).listIdxType;
			boolean list = listIdxType != null;
			int valCnt = element.properties.size();
			for (int j = 0; j < element.size; j++) {
				if (list) {
					valCnt = (Integer) parseType(listIdxType);
				}
				if (j % 1000 == 0)
					Log.d(TAG, "element " + element.stringType + " " + j + "/"
							+ element.size + " read " + valCnt + " " + type
							+ " buffer remaining " + byteBuffer.remaining());
				ArrayList<Number> data = new ArrayList<Number>();
				for (int i = 0; i < valCnt; i++) {
					data.add(parseType(type));
				}
				element.dataList.add(data);
			}
		}
	}

}
