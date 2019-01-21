import java.io.UnsupportedEncodingException;

public class Util {
	static final byte[] HEX_CHAR_TABLE = { (byte) '0', (byte) '1', (byte) '2',
					(byte) '3', (byte) '4', (byte) '5', (byte) '6', (byte) '7',
					(byte) '8', (byte) '9', (byte) 'A', (byte) 'B', (byte) 'C',
					(byte) 'D', (byte) 'E', (byte) 'F' };
	public static String fixWindowsNameingProblem(String deviceName) {
		String fixedDeviceName = deviceName;
		int cut = -1;
		if ((cut = deviceName.indexOf('\n')) != -1) {
			fixedDeviceName = deviceName.substring(0, cut);
		}
		return fixedDeviceName;
	}
	
	public static void fixWindowsNameingProblem(String[] deviceNames) {
		for (int i = 0; i < deviceNames.length; i++) {
			deviceNames[i] = Util.fixWindowsNameingProblem(deviceNames[i]);
		}
	}
	
	public static String getHexa(byte[] raw) throws
		UnsupportedEncodingException {
		byte[] hex = new byte[2 * raw.length];
		int index = 0;
		for (byte b : raw) {
			int v = b & 0xFF;
			hex[index++] = HEX_CHAR_TABLE[v >>> 4];
			hex[index++] = HEX_CHAR_TABLE[v & 0xF];
		}
		return new String(hex, "ASCII");
	}
}