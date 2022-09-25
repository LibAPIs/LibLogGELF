package com.mclarkdev.tools.libloggelf;

import java.io.IOException;
import java.net.Socket;

import org.json.JSONObject;

public class LibLogGELF {

	private static String logSource = "LibGL";
	private static String logHost = "127.0.0.1";
	private static int logPort = 5575;

	private static Socket logSocket = null;

	static {

		// Log Source
		String gelfSource = System.getenv("LOG_GELF_SOURCE");
		if (gelfSource != null) {
			setLogSource(gelfSource);
		}

		// Log Server
		String gelfServer = System.getenv("LOG_GELF_SERVER");
		if (gelfServer != null) {
			String[] parts = gelfServer.split(":");
			setLogHost(parts[0], Integer.parseInt(parts[1]));
		}
	}

	/**
	 * Set the log source.
	 * 
	 * @param host
	 */
	public static void setLogSource(String host) {

		logSource = host;
	}

	/**
	 * Set the log server.
	 * 
	 * @param host
	 * @param port
	 */
	public static void setLogHost(String host, int port) {

		logHost = host;
		logPort = port;
		connect(true);
	}

	/**
	 * Returns true if connected to the log service.
	 * 
	 * @return
	 */
	public static boolean connected() {
		return ((logSocket != null) && (logSocket.isConnected()));
	}

	private static boolean connect(boolean force) {
		if (connected() && !force) {
			return true;
		}

		try {

			disconnect();
			logSocket = new Socket(logHost, logPort);
			return true;

		} catch (IOException e) {

			System.err.println("LibGL: Failed to connect.");
			e.printStackTrace(System.err);
			return false;
		}
	}

	/**
	 * Disconnects the existing socket.
	 */
	private static void disconnect() {
		if (logSocket == null) {
			return;
		}

		try {
			logSocket.close();
		} catch (IOException e) {
		} finally {
			logSocket = null;
		}
	}

	/**
	 * Write a message to the log server.
	 * 
	 * @param gelfObject
	 * @return
	 */
	private static boolean write(JSONObject gelfObject) {
		if (!connected() && !connect(false)) {
			return false;
		}

		try {

			logSocket.getOutputStream().write(gelfObject.toString().getBytes());
			logSocket.getOutputStream().write((byte) 0x00);
			logSocket.getOutputStream().flush();
			return true;

		} catch (IOException e) {

			System.err.println("LibGELF: Failed to write message.");
			System.out.println(gelfObject.toString());
			disconnect();
			return false;
		}
	}

	/**
	 * Write a message to the log server.
	 * 
	 * @param message
	 * @return
	 */
	public static boolean write(String message) {
		return write(LibLogGELF.format(message, null, 1));
	}

	/**
	 * Write a message to the log server.
	 * 
	 * @param message
	 * @param longMessage
	 * @return
	 */
	public static boolean write(String message, String longMessage) {
		return write(LibLogGELF.format(message, longMessage, 1));
	}

	/**
	 * Write a message to the log server.
	 * 
	 * @param message
	 * @param longMessage
	 * @param level
	 * @return
	 */
	public static boolean write(String message, String longMessage, int level) {
		return write(LibLogGELF.format(message, longMessage, level));
	}

	/**
	 * Write a message to the log server.
	 * 
	 * @param message
	 * @param longMessage
	 * @param level
	 * @param params
	 * @return
	 */
	public static boolean write(String message, String longMessage, int level, String... params) {
		if (params == null || params.length % 2 != 0) {
			throw new IllegalArgumentException();
		}

		JSONObject gelf = LibLogGELF.format(message, longMessage, level);

		for (int x = 0; x < params.length;) {
			String key = params[x++];
			String val = params[x++];

			gelf.put("_" + key, val);
		}

		return write(gelf);
	}

	/**
	 * Returns a JSON formatted GELF message.
	 * 
	 * @param shortMessage
	 * @param fullMessage
	 * @param level
	 * @return
	 */
	public static JSONObject format(String shortMessage, String fullMessage, int level) {
		return new JSONObject()//
				.put("version", "1.1")//
				.put("host", logSource)//
				.put("level", level)//
				.put("short_message", shortMessage)//
				.put("full_message", fullMessage);
	}
}
