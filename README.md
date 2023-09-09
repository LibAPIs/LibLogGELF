# LibLogGELF

An extension to LibLog, adding the ability to forward application generated log messages to a GELF capable log server.

## Configuration

Default configuration values can be modified by setting environment variables prior to launching the application.

```
LOG_GELF_APP	The application name sent with GELF messages.
LOG_GELF_SERVER	The GELF server to send logs to.
				GELF forwarding disabled if server not set.
```

# License

Open source & free for all. ❤
