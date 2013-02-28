package it.petruzzellis.dentalview.model.parser.ply;

import it.petruzzellis.dentalview.model.Mesh;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.annotation.SuppressLint;
import android.os.Environment;
import android.util.Log;

@SuppressLint("DefaultLocale")
@SuppressWarnings("incomplete-switch")
public class Parser {
    private static final String TAG = "PLYPARSER";;

    private static final File datafolder=Environment.getDataDirectory();

    private FileFormat fileFormat = null;
    private Element vertexElement = null;
    private Element faceElement = null;

    private int parseStatus = 0;
    int coordIdx[] = new int[] { -1, -1, -1 };
    
    
    
    public Mesh loadModel(File file, float[] color) throws Exception {
        if (color == null || color.length < 3 || color.length > 4)
            throw new Exception("Wrong default vertex color " + String.valueOf(color));
        RandomAccessFile raf = null;
        String line;
        try {
            raf = new RandomAccessFile(file,"r");
            line = raf.readLine();
            if (line == null || line.compareTo("ply") != 0) {
                throw new Exception("ERRROR: magic bytes 'ply' not found in file.");
            }
            parseHeader(raf);
            if (fileFormat == FileFormat.ASCII) {
                return new Mesh(parseASCIIFaces(raf, parseASCIIVertex(raf)));
            } else {
                return new Mesh(parseBinaryFaces(raf, parseBinaryVertex(raf)));
            }
        } catch (FileNotFoundException e) {
            throw new Exception("ERROR: File not found: " + file.getAbsolutePath());
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
    }

    private float[] parseASCIIVertex(RandomAccessFile raf) throws Exception {
        float[] vertexBuffer = new float[vertexElement.size * 3];
        String line;
        int v = 0;
        for (int e = 0; e < vertexElement.size; e++) {
            line = raf.readLine();
            if (line == null) {
                throw new Exception("File trunked or malformed header");
            }
            float[] parsedRaw = processDataLine(line.split(" "), vertexElement);
            for (int i = 0; i < parsedRaw.length; i++) {
                vertexBuffer[v++] = parsedRaw[i];
            }
        }
        return vertexBuffer;
    }

    private float[] parseASCIIFaces(RandomAccessFile raf, float[] vertexBuffer) throws Exception {
        float[] mesh_vertex = new float[faceElement.size * 3 * 3];// 3 vertici
                                                                  // da 3
                                                                  // coordinate
        String line;
        int v = 0;
        for (int f = 0; f < faceElement.size; f++) {
            line = raf.readLine();
            if (line == null) {
                throw new Exception("File trunked or malformed header");
            }
            float[] parsedRaw = processDataLine(line.split(" "), faceElement);
            if (parsedRaw.length != 3)
                throw new Exception(" At now olny triangle faces implemented");
            for (int i = 0; i < parsedRaw.length; i++) {
                for (int c = 0; c < 3; c++)
                    mesh_vertex[v++] = vertexBuffer[coordIdx[c] + 3 * (int) parsedRaw[i]];
            }
        }
        return mesh_vertex;
    }

    private Number getFromBuffer(ByteBuffer byteBuffer, PropertyType type) throws Exception {
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
                throw new Exception("Double not implemented");
                // return Double.valueOf(byteBuffer.getDouble());
            default:
                throw new Exception("ERROR: invalid data type: " + type);
        }
    }

    private float[] parseBinaryVertex(RandomAccessFile raf) throws Exception {
        int vtxNum = vertexElement.size;
        int coordNum = vertexElement.properties.size();
        if (coordNum != 3)
            throw new Exception("Only 3D vertex implemented");
        PropertyType coordType = vertexElement.properties.get(0).type;
        int bufferSize = vtxNum * coordNum * coordType.sizeOf();
        float[] vertexBuffer = new float[vtxNum * coordNum];
        byte[] buffer = new byte[bufferSize];
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
        if (fileFormat == FileFormat.BINARY_BIG_ENDIAN)
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
        else
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        raf.read(buffer);
        byteBuffer.put(buffer);
        byteBuffer.position(0);
        buffer = null;
        int c = 0;
        for (int V = 0; V < vtxNum; V++) {
            for (int j = 0; j < coordNum; j++) {
                vertexBuffer[c++] = getFromBuffer(byteBuffer, coordType).floatValue();
            }
        }
        return vertexBuffer;
    }

    private float[] parseBinaryFaces(RandomAccessFile raf, float[] vertexBuffer) throws Exception {
        int faceNum = faceElement.size;
        int vtxNum = 3;
        int coordNum = 3;
        PropertyType idxProp = faceElement.properties.get(0).listIdxType;
        PropertyType vtxProp = faceElement.properties.get(0).type;
        int bufferSize = (idxProp.sizeOf() + vtxNum * vtxProp.sizeOf()) * faceNum ;
        float[] meshVertex = new float[faceNum * vtxNum * coordNum];
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufferSize);
        if (fileFormat == FileFormat.BINARY_BIG_ENDIAN)
            byteBuffer.order(ByteOrder.BIG_ENDIAN);
        else
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byte[] buffer = new byte[bufferSize];
        raf.read(buffer);
        byteBuffer.put(buffer);
        byteBuffer.position(0);
        buffer = null;
        int c = 0, vtxIdx;
        for (int f = 0; f < faceNum; f++) {
            if (f % 1000==0)
                Log.d(TAG,"fn"+f);
            if (getFromBuffer(byteBuffer, idxProp).intValue() != 3)
                throw new Exception("Only Triangle faces implemented");
            for (int v = 0; v < 3; v++) {
                vtxIdx = 3 * getFromBuffer(byteBuffer, vtxProp).intValue();
                for (int i = 0; i < 3; i++) {
                    meshVertex[c++] = vertexBuffer[vtxIdx + coordIdx[i]];
                }
            }
        }
        return meshVertex;
    }

    private void parseHeader(RandomAccessFile raf) throws Exception {
        String line;
        do {
            line = raf.readLine();
            if (line != null)
                parseStatus |= processHeaderLine(line.toUpperCase().split(" "));
        } while (!line.equalsIgnoreCase("end_header"));
        if (parseStatus != 7) {
            throw new Exception("Invalid Header structure");
        }
    }

    private void checkHeaderCoord() throws Exception {
        int coordOk = 0;
        for (int i = 0; i < vertexElement.properties.size(); i++) {
            Property p = vertexElement.properties.get(i);
            if (p.name.equalsIgnoreCase("x")) {
                coordIdx[0] = i;
                coordOk |= 1;
            } else if (p.name.equalsIgnoreCase("y")) {
                coordIdx[1] = i;
                coordOk |= 2;
            } else if (p.name.equalsIgnoreCase("z")) {
                coordIdx[2] = i;
                coordOk |= 4;
            }
        }

        if (coordOk != 7) {
            throw new Exception("ERROR: x,y,z not properly defined for vertices in header.");
        }
    }

    private int processHeaderLine(String parts[]) throws Exception {
        if (parts.length > 2) {
            switch (HeaderElement.valueOf(parts[0])) {
                case FORMAT:
                    fileFormat = FileFormat.valueOf(parts[1]);
                    return 1;
                case ELEMENT:
                    switch (ElementType.valueOf(parts[1])) {
                        case VERTEX:
                            if (parseStatus != 1)
                                throw new Exception("Invalid Header structure");
                            vertexElement = new Element(Integer.valueOf(parts[2]));
                            return 2;
                        case FACE:
                            if (parseStatus != 3)
                                throw new Exception("Invalid Header structure");
                            checkHeaderCoord();
                            faceElement = new Element(Integer.valueOf(parts[2]));
                            return 4;
                    }
                case PROPERTY:
                    if (parseStatus <= 1)
                        throw new Exception("Invalid Header structure");
                    Property p = new Property(parts[parts.length - 1]);
                    if (parts[1].equalsIgnoreCase("list")) {
                        p.listIdxType = PropertyType.valueOf(parts[2]);
                        p.type = PropertyType.valueOf(parts[3]);
                    } else {
                        p.type = PropertyType.valueOf(parts[1]);
                    }
                    if (parseStatus == 3) {
                        vertexElement.properties.add(p);
                    } else if (faceElement.properties.size() > 0) {
                        throw new Exception("Invalid Header structure");
                    } else {
                        faceElement.properties.add(p);
                    }
                    break;
            }
        }
        return 0;
    }

    private float[] processDataLine(String parts[], Element element) {
        int offset = 0;
        int cnt = 1;

        if (element.properties.get(0).listIdxType != null) {
            cnt = Integer.valueOf(parts[0]);
            offset = 1;
        } else {
            cnt = element.properties.size();
        }

        float[] res = new float[cnt];
        for (int i = 0; i < cnt; i++) {
            res[i] = (Float.valueOf(parts[i + offset])).floatValue();
        }
        return res;
    }
}
