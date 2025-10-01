package com.mclarkdev.tools.libloggelf.lib;

import java.net.URI;
import java.net.UnknownHostException;

import org.json.JSONObject;

import com.mclarkdev.tools.liblog.lib.LibLogCachedLogWriter;
import com.mclarkdev.tools.liblog.lib.LibLogMessage;

/**
 * LibLogGELF // LibLogGELFLogWriter
 */
public class LibLogGELFLogWriter extends LibLogCachedLogWriter {

	private final String appName;

	public static String scheme() {
		return "gelf";
	}

	public LibLogGELFLogWriter(URI uri) throws UnknownHostException {
		super(uri, new LibLogGELFStream(uri));

		String path = uri.getPath();
		this.appName = (path != null) ? path.trim().substring(1) : "JavaApp";
	}

	@Override
	public void write(LibLogMessage message) {

		JSONObject obj = (new JSONObject())//
				.put("host", appName)//
				.put("version", "0.1")//
				.put("time", message.getTime())//
				.put("stamp", message.getTimeStamp())//
				.put("facility", message.getLoggedFacility())//
				.put("message", message.getLoggedMessage())//
				.put("throwable", message.getLoggedThrowableString());

		if (debug) {
			obj.put("className", message.getLoggedClassName());
			obj.put("classLine", message.getLoggedLineNumber());
		}

		cache(obj.toString());
	}
}
