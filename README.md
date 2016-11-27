---
page: https://idle.run/memcached-client
title: Java Memcached Binary Test Client
tags: java, memcached
date: 2016-11-23
---

This is a very simple client for testing a `memcached` instance using the binary protocol

### Compilation

Compile with the `package` command to create an executable jar with dependencies included

```bash
mvn package
```

### Running

```
java -jar target/memcached-client-1.0.jar
```

Default host is `localhost:11211`.

Can be changed with `-DHOST` setting

```
java -DHOST=192.168.0.123:11211 -jar target/memcached-client-1.0.jar
```

Optionally set the rate to send keepalive requests to memcached with `-DKEEPALIVE_RATE_MS=10000`. Default is every 30 seconds (30000)

### Commands

Commands to send are entered through stdin.

Supported commands:

- `SET key value`
- `GET key`
- `DELETE key`
