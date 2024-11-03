package tests.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * This class simplifies the serialization test.
 * 
 */
public class SerializationTester {

    private static Object lastOutput = null;

    private SerializationTester() {
    }

    /**
	 * Serialize an object and then deserialize it.
	 * 
	 * @param inputObject
	 *            the input object
	 * @return the deserialized object
	 */
    public static Object getDeserilizedObject(Object inputObject) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(inputObject);
        oos.close();
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bis);
        Object outputObject = ois.readObject();
        lastOutput = outputObject;
        ois.close();
        return outputObject;
    }

    /**
	 * Tests the serialization and deserialization of const objects.
	 * 
	 * @param inputObject
	 *            A const object
	 * @return true if the deserialized object is the same as the input object,
	 *         otherwise false
	 * @throws Exception
	 *             If any occurs.
	 */
    public static boolean assertSame(Object inputObject) throws Exception {
        return inputObject == getDeserilizedObject(inputObject);
    }

    /**
	 * Tests the serialization and deserialization of instance objects.
	 * 
	 * @param inputObject
	 *            An object
	 * @return true if the deserialized object is equal to the input object,
	 *         otherwise false
	 * @throws Exception
	 *             If any occurs.
	 */
    public static boolean assertEquals(Object inputObject) throws Exception {
        return inputObject.equals(getDeserilizedObject(inputObject));
    }

    /**
	 * Tests the serialization compatibility with reference const objects.
	 * 
	 * @param obj
	 *            the object to be checked
	 * @param fileName
	 *            the serialization output file generated by reference
	 * @return true if compatible, otherwise false
	 * @throws Exception
	 *             If any occurs.
	 */
    public static boolean assertCompabilitySame(Object obj, String fileName) throws Exception {
        return obj == readObject(obj, fileName);
    }

    /**
	 * Tests the serialization compatibility with reference for instance
	 * objects.
	 * 
	 * @param obj
	 *            the object to be checked
	 * @param fileName
	 *            the serialization output file generated by reference
	 * @return true if compatible, otherwise false
	 * @throws Exception
	 *             If any occurs.
	 */
    public static boolean assertCompabilityEquals(Object obj, String fileName) throws Exception {
        return obj.equals(readObject(obj, fileName));
    }

    /**
	 * Deserialize an object from a file.
	 * 
	 * @param obj
	 *            the object to be serialized if no serialization file is found
	 * @param fileName
	 *            the serialization file
	 * @return the deserialized object
	 * @throws Exception
	 *             If any occurs.
	 */
    public static Object readObject(Object obj, String fileName) throws Exception {
        InputStream input = null;
        ObjectInputStream oinput = null;
        URL url = SerializationTester.class.getClassLoader().getResource(fileName);
        if (null == url) {
            writeObject(obj, new File(fileName).getName());
            throw new Error("Serialization file does not exist, created in the current dir.");
        }
        input = url.openStream();
        try {
            oinput = new ObjectInputStream(input);
            Object newObj = oinput.readObject();
            return newObj;
        } finally {
            try {
                if (null != oinput) {
                    oinput.close();
                }
            } catch (Exception e) {
            }
            try {
                if (null != input) {
                    input.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public static void writeObject(Object obj, String fileName) throws Exception {
        OutputStream output = null;
        ObjectOutputStream ooutput = null;
        try {
            output = new FileOutputStream(fileName);
            ooutput = new ObjectOutputStream(output);
            ooutput.writeObject(obj);
        } finally {
            try {
                if (null != ooutput) {
                    ooutput.close();
                }
            } catch (Exception e) {
            }
            try {
                if (null != output) {
                    output.close();
                }
            } catch (Exception e) {
            }
        }
    }

    /**
	 * Gets the last deserialized object.
	 * 
	 * @return the last deserialized object
	 */
    public static Object getLastOutput() {
        return lastOutput;
    }

    public static void main(String[] args) {
    }
}
