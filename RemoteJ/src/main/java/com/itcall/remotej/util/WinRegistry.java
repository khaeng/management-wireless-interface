package com.itcall.remotej.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WinRegistry {

	private static Logger LOG = LoggerFactory.getLogger(WinRegistry.class);

	public  static final int HKEY_CURRENT_USER = 0x80000001, HKEY_LOCAL_MACHINE = 0x80000002, REG_SUCCESS = 0, REG_NOTFOUND = 2, REG_ACCESSDENIED = 5, KEY_ALL_ACCESS = 0xf003f, KEY_READ = 0x20019;
	private static final Preferences userRoot = Preferences.userRoot(), systemRoot = Preferences.systemRoot();
	private static final Class<? extends Preferences> userClass = userRoot.getClass();
	private static Method regOpenPath, regClosePath, regQueryValueEx, regEnumValue, regQueryInfoKey, regEnumKeyEx, regCreateKeyEx, regSetValueEx, delRegPath, delRegKey;
	static {
		try {
			(regOpenPath = userClass.getDeclaredMethod("WindowsRegOpenKey", new Class[] { int.class, byte[].class, int.class })).setAccessible(true);
			(regClosePath = userClass.getDeclaredMethod("WindowsRegCloseKey", new Class[] { int.class })).setAccessible(true);
			(regQueryValueEx = userClass.getDeclaredMethod("WindowsRegQueryValueEx", new Class[] { int.class, byte[].class })).setAccessible(true);
			(regEnumValue = userClass.getDeclaredMethod("WindowsRegEnumValue", new Class[] { int.class, int.class, int.class })).setAccessible(true);
			(regQueryInfoKey = userClass.getDeclaredMethod("WindowsRegQueryInfoKey1", new Class[] { int.class })).setAccessible(true);
			(regEnumKeyEx = userClass.getDeclaredMethod("WindowsRegEnumKeyEx", new Class[] { int.class, int.class, int.class })).setAccessible(true);
			(regCreateKeyEx = userClass.getDeclaredMethod("WindowsRegCreateKeyEx", new Class[] { int.class, byte[].class })).setAccessible(true);
			(regSetValueEx = userClass.getDeclaredMethod("WindowsRegSetValueEx", new Class[] { int.class, byte[].class, byte[].class })).setAccessible(true);
			(delRegKey = userClass.getDeclaredMethod("WindowsRegDeleteValue", new Class[] { int.class, byte[].class })).setAccessible(true);
			(delRegPath = userClass.getDeclaredMethod("WindowsRegDeleteKey", new Class[] { int.class, byte[].class })) .setAccessible(true);
		} catch (/*NoSuchMethodException | SecurityException*/ Exception ex) {
			// Logger.getLogger(WinRegistry.class.getName()).log(Level.SEVERE, null, ex);
			LOG.error("{} ::: {}", ex.getMessage(), ex);
		}
	}

	/**
	 * Read a value from key and value name
	 *
	 * @param root
	 *            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param path
	 * @param key
	 * @return the value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static String getRegKey(int root, String path, String key) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		switch (root) {
		case HKEY_LOCAL_MACHINE:
			return getRegKey(systemRoot, root, path, key);
		case HKEY_CURRENT_USER:
			return getRegKey(userRoot, root, path, key);
		default:
			throw new IllegalArgumentException("hkey=" + root);
		}
	}

	/**
	 * Read value(s) and value name(s) form given key
	 *
	 * @param root
	 *            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param path
	 * @return the value name(s) plus the value(s)
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Map<String, String> getRegPath(int root, String path) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		switch (root) {
		case HKEY_LOCAL_MACHINE:
			return getRegPath(systemRoot, root, path);
		case HKEY_CURRENT_USER:
			return getRegPath(userRoot, root, path);
		default:
			throw new IllegalArgumentException("hkey=" + root);
		}
	}

	/**
	 * Read the value name(s) from a given key
	 *
	 * @param root
	 *            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param path
	 * @return the value name(s)
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<String> listRegSubPath(int root, String path) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		switch (root) {
		case HKEY_LOCAL_MACHINE:
			return listRegSubPath(systemRoot, root, path);
		case HKEY_CURRENT_USER:
			return listRegSubPath(userRoot, root, path);
		default:
			throw new IllegalArgumentException("hkey=" + root);
		}
	}

	/**
	 * Create a key
	 *
	 * @param root
	 *            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param path
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void createPath(int root, String path) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int[] ret;
		switch (root) {
		case HKEY_LOCAL_MACHINE:
			ret = createPath(systemRoot, root, path);
			regClosePath.invoke(systemRoot, new Object[] { ret[0] });
			break;
		case HKEY_CURRENT_USER:
			ret = createPath(userRoot, root, path);
			regClosePath.invoke(userRoot, new Object[] { ret[0] });
			break;
		default:
			throw new IllegalArgumentException("hkey=" + root);
		}
		if (ret[1] != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + ret[1] + " key=" + path);
		}
	}

	/**
	 * Write a value in a given key/value name
	 *
	 * @param root
	 * @param path
	 * @param key
	 * @param value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void writeRegKey(int root, String path, String key, String value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		switch (root) {
		case HKEY_LOCAL_MACHINE:
			writeRegKey(systemRoot, root, path, key, value);
			break;
		case HKEY_CURRENT_USER:
			writeRegKey(userRoot, root, path, key, value);
			break;
		default:
			throw new IllegalArgumentException("hkey=" + root);
		}
	}

	/**
	 * Delete a given key
	 *
	 * @param root
	 * @param path
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void delRegPath(int root, String path) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int rc = -1;
		switch (root) {
		case HKEY_LOCAL_MACHINE:
			rc = delRegPath(systemRoot, root, path);
			break;
		case HKEY_CURRENT_USER:
			rc = delRegPath(userRoot, root, path);
		}
		if (rc != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + rc + " key=" + path);
		}
	}

	/**
	 * delete a value from a given key/value name
	 *
	 * @param root
	 * @param path
	 * @param key
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void delRegKey(int root, String path, String key) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int rc = -1;
		switch (root) {
		case HKEY_LOCAL_MACHINE:
			rc = delRegKey(systemRoot, root, path, key);
			break;
		case HKEY_CURRENT_USER:
			rc = delRegKey(userRoot, root, path, key);
		}
		if (rc != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + rc + " key=" + path + " value=" + key);
		}
	}

	private static int delRegKey(Preferences winPref, int root, String path, String key) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int[] handles = (int[]) regOpenPath.invoke(winPref, new Object[] { root, toCstr(path), KEY_ALL_ACCESS });
		if (handles[1] != REG_SUCCESS) {
			return handles[1];// Can be REG_NOTFOUND, REG_ACCESSDENIED
		}
		int rc = ((Integer) delRegKey.invoke(winPref, new Object[] { handles[0], toCstr(key) }));
		regClosePath.invoke(winPref, new Object[] { handles[0] });
		return rc;
	}

	private static int delRegPath(Preferences winPref, int root, String path) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int rc = ((Integer) delRegPath.invoke(winPref, new Object[] { root, toCstr(path) }));
		return rc; // Can be REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
	}

	private static String getRegKey(Preferences winPref, int root, String path, String key) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int[] handles = (int[]) regOpenPath.invoke(winPref, new Object[] { root, toCstr(path), KEY_READ });
		if (handles[1] != REG_SUCCESS) {
			return null;
		}
		byte[] valb = (byte[]) regQueryValueEx.invoke(winPref, new Object[] { handles[0], toCstr(key) });
		regClosePath.invoke(winPref, new Object[] { handles[0] });
		return (valb != null ? new String(valb).trim() : null);
	}

	private static Map<String, String> getRegPath(Preferences winPref, int root, String path) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		HashMap<String, String> results = new HashMap<String, String>();
		int[] handles = (int[]) regOpenPath.invoke(winPref, new Object[] { root, toCstr(path), KEY_READ });
		if (handles[1] != REG_SUCCESS) {
			return null;
		}
		int[] info = (int[]) regQueryInfoKey.invoke(winPref, new Object[] { handles[0] });

		int count = info[0]; // Count
		int maxlen = info[3]; // Max value length
		for (int index = 0; index < count; index++) {
			byte[] name = (byte[]) regEnumValue.invoke(winPref, new Object[] { handles[0], index, maxlen + 1 });
			String value = getRegKey(root, path, new String(name));
			results.put(new String(name).trim(), value);
		}
		regClosePath.invoke(winPref, new Object[] { handles[0] });
		return results;
	}

	private static List<String> listRegSubPath(Preferences winPref, int root, String path) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		List<String> results = new ArrayList<String>();
		int[] handles = (int[]) regOpenPath.invoke(winPref, new Object[] { root, toCstr(path), KEY_READ });
		if (handles[1] != REG_SUCCESS) {
			return null;
		}
		int[] info = (int[]) regQueryInfoKey.invoke(winPref, new Object[] { handles[0] });

		int count = info[0];// Count
		int maxlen = info[3]; // Max value length
		for (int index = 0; index < count; index++) {
			byte[] name = (byte[]) regEnumKeyEx.invoke(winPref, new Object[] { handles[0], index, maxlen + 1 });
			results.add(new String(name).trim());
		}
		regClosePath.invoke(winPref, new Object[] { handles[0] });
		return results;
	}

	private static int[] createPath(Preferences winPref, int root, String path) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		return (int[]) regCreateKeyEx.invoke(winPref, new Object[] { root, toCstr(path) });
	}

	private static void writeRegKey(Preferences winPref, int root, String path, String key, String value) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
		int[] handles = (int[]) regOpenPath.invoke(winPref, new Object[] { root, toCstr(path), KEY_ALL_ACCESS });
		regSetValueEx.invoke(winPref, new Object[] { handles[0], toCstr(key), toCstr(value) });
		regClosePath.invoke(winPref, new Object[] { handles[0] });
	}

	private static byte[] toCstr(String str) {

		byte[] result = new byte[str.length() + 1];
		for (int i = 0; i < str.length(); i++) {
			result[i] = (byte) str.charAt(i);
		}
		result[str.length()] = 0;
		return result;
	}
}