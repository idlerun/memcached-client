package run.idle;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.BinaryConnectionFactory;
import net.spy.memcached.MemcachedClient;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 */
public class Main {

    private static Logger LOG = Logger.getLogger(Main.class);

    public static void main( String[] args ) throws IOException {
        System.setProperty("net.spy.log.LoggerImpl", "net.spy.memcached.compat.log.Log4JLogger");
        String host = System.getProperty("HOST", "localhost:11211");
        LOG.info("Connecting to: " + host);

        // No easy way to override the failure mode setting for binary connection
        BinaryConnectionFactory factory = new BinaryConnectionFactory();
        MemcachedClient client = new MemcachedClient(factory, AddrUtil.getAddresses(host));

        LOG.info("Connected in BINARY mode");
        startKeepalives(client);

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line;

        LOG.info("Ready for command input");
        LOG.info("Usage: \n" +
                "\tSET key value\n" +
                "\tGET key\n" +
                "\tDEL key");

        // Process commands until ^d
        Pattern pattern = Pattern.compile("([a-zA-Z]*) ([^ ]*) ?(.*)?");
        while((line=br.readLine()) != null) {
            Matcher m = pattern.matcher(line);
            if (!m.matches()) {
                LOG.warn("Invalid input line");
            } else {
                String cmd = m.group(1).toUpperCase();
                String key = m.group(2);
                try {
                    if (cmd.equals("SET")) {
                        String value = m.group(3);
                        client.set(key, 0, value).get();
                        LOG.info("SET '" + key + "'='" + value + "'");
                    } else if (cmd.equals("GET")) {
                        String value = (String)client.get(key);
                        LOG.info("GET '" + key + "' " + (value != null ? "=> '" + value + "'" : "<NOT_FOUND>"));
                    } else if (cmd.equals("DEL")) {
                        client.delete(key).get();
                        LOG.info("DEL '" + key + "'");
                    } else {
                        LOG.warn("Invalid command '" + cmd + "'");
                    }
                } catch (Exception e) {
                    LOG.warn(cmd + " failed: " + e.getMessage());
                }
            }
        }

    }

    private static void startKeepalives(MemcachedClient client) {
        // default send a connection keepalive request every 30 seconds
        long keepaliveMs = Long.parseLong(System.getProperty("KEEPALIVE_RATE_MS", "30000"));
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        LOG.info("Scheduling memcached keepalives every " + keepaliveMs + "ms");
        executor.scheduleAtFixedRate(()-> {
            LOG.trace("Sending keepalive request");
            client.get("__KEEPALIVE__");
        }, keepaliveMs, keepaliveMs, TimeUnit.MILLISECONDS);
    }
}
