package com.mclarkdev.tools.libloggelf.lib;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.UnknownHostException;

import com.mclarkdev.tools.liblog.LibLog;
import com.mclarkdev.tools.liblog.lib.LibLogTCPStream;

public class LibLogGELFStream extends LibLogTCPStream {

	public LibLogGELFStream(URI uri) throws UnknownHostException {
		super(uri);
	}

	/**
	 * Write a message to the log server.
	 * 
	 * @param message
	 * @return
	 */
	@Override
	public boolean write(String message) {
		if (!connected() && !connect(false)) {
			return false;
		}

		try {

			// Get the output stream
			OutputStream out = //
					logSocket.getOutputStream();

			// Only one writer at a time
			synchronized (out) {

				// Send the log message
				out.write(message.getBytes());
				out.write((byte) 0xfffffffe);
				out.flush();
			}
			return true;

		} catch (IOException e) {

			// Log the write failure
			LibLog.log("logger", "Failed to write message.", e);

			// Force reconnect
			disconnect();
			return false;
		}
	}
}
