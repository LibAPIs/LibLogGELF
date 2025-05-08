# LibLogGELF

An extension to LibLog, forwards log messages to configured GELF server.

## Maven Dependency

Include the library in your project by adding the following dependency to your pom.xml

```
<dependency>
	<groupId>com.mclarkdev.tools</groupId>
	<artifactId>libloggelf</artifactId>
	<version>1.6.1</version>
</dependency>
```

## Example

Simply register the handler and add a new stream to LOG_STREAMS;

Edit environment variables to add new stream.

```
LOG_STREAMS=console:/;gelf://127.0.0.1:9904
```

Register the handler in code.

```
LibLog.cfg().registerLogger(LibLogGELFLogWriter.class);
```

Any future messages logged with LibLog will be sent to the GELF server.

```
LibLog._log("Hello, world.");
```

# License

Open source & free for all. ❤
