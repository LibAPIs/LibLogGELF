# LibLogGELF

An extension to LibLog, forwards log messages to configured GELF server.

## Maven Dependency

Include the library in your project by adding the following dependency to your pom.xml

```
<dependency>
	<groupId>com.mclarkdev.tools</groupId>
	<artifactId>libloggelf</artifactId>
	<version>1.5.1</version>
</dependency>
```

## Configuration

Default configuration values can be modified by setting environment variables prior to launching the application.

```
LOG_GELF_APP	The application name sent with GELF messages.
LOG_GELF_SERVER	The GELF server to send logs to.
				GELF forwarding disabled if server not set.
```

## Example

With configuration values provided through environment variables, the extension need only be referenced to start forwarding messages to the configured GELF server.

```
static {
	LibLog._logF("Network logging: %s", LibLogGELF.enabled());
}
```

Any future messages logged with LibLog will be sent to the GELF server.

```
LibLog._log("Hello, world.");
```

# License

Open source & free for all. ❤
