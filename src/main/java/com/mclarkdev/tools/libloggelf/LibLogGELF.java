package com.mclarkdev.tools.libloggelf;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;

import com.mclarkdev.tools.liblog.LibLog;
import com.mclarkdev.tools.liblog.LibLog.LogWriter;
import com.mclarkdev.tools.liblog.LibLogMessage;

public class LibLogGELF {

	private static boolean enabled = false;

	private static String appName;

	private static String logHost;
	private static int logPort;

	private static Socket logSocket = null;

	private static Set<JSONObject> messageCache;

	static {

		// Determine app name
		appName = System.getenv("LOG_GELF_APP");

		// GELF log server
		String server = System.getenv("LOG_GELF_SERVER");
		if (server != null) {

			// Initialize the logger
			setupLogger(server);
		}

		// Print app name
		LibLog._logF("Logging as: %s", appName);
	}

	private static void setupLogger(String server) {

		try {
			// Determine server address
			String[] hostParts = server.split(":");
			logPort = Integer.parseInt(hostParts[1]);
			logHost = hostParts[0];
		} catch (Exception e) {
			enabled = false;
			return;
		}

		// Setup log cache
		messageCache = ConcurrentHashMap.newKeySet();

		// Intercept log messages
		LibLog.addLogger(new LogWriter() {

			@Override
			public void write(boolean debug, LibLogMessage message) {
				JSONObject obj = (new JSONObject())//
						.put("host", appName)//
						.put("version", "1.1")//
						.put("time", message.getTime())//
						.put("stamp", message.getTimeStamp())//
						.put("facility", message.getLoggedFacility())//
						.put("message", message.getLoggedMessage())//
						.put("throwable", message.getLoggedThrowableString());

				if (debug) {
					obj.put("className", message.getLoggedClassName());
					obj.put("classLine", message.getLoggedLineNumber());
				}

				messageCache.add(obj);
			}
		});

		// Cache flushing thread
		new Thread() {
			public void run() {
				this.setName("LogSend");
				while (true) {
					try {
						Thread.sleep(1000);
						flushCache();
					} catch (InterruptedException e) {
						return;
					}
				}
			}
		}.start();

		enabled = true;
	}

	/**
	 * Returns true if GELF logging is enabled.
	 * 
	 * @return
	 */
	public static boolean enabled() {
		return enabled;
	}

	/**
	 * Returns the name of the application sent to GELF server.
	 * 
	 * @return
	 */
	public static String getAppName() {
		return appName;
	}

	/**
	 * Returns the current number of messages in the cache.
	 * 
	 * @return
	 */
	public static int cacheSize() {
		return messageCache.size();
	}

	/**
	 * Flush any messages in the cache to the socket.
	 */
	public static boolean flushCache() {
		if (messageCache.size() == 0) {
			return true;
		}

		for (JSONObject msg : messageCache) {
			synchronized (msg) {
				if (write(msg)) {
					messageCache.remove(msg);
				} else {
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * Returns true if connected to the log service.
	 * 
	 * @return
	 */
	public static boolean connected() {
		return ((logSocket != null) && (logSocket.isConnected()));
	}

	/**
	 * Connect to the remote logging server.
	 * 
	 * @param force Force a reconnect.
	 * @return
	 */
	private static boolean connect(boolean force) {
		if (connected() && !force) {
			return true;
		}

		try {

			disconnect();
			logSocket = new Socket(logHost, logPort);
			return true;

		} catch (IOException e) {

			System.err.println(String.format(//
					"LibLogGELF: Failed to connect\n - Host: %s\n -- (%s)\n - Messages cached: %d", //
					(logHost + ":" + logPort), e.getMessage(), cacheSize()));
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
}
