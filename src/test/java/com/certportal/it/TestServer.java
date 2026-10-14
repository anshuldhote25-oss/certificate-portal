package com.certportal.it;

import java.io.File;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.valves.AccessLogValve;

/**
 * Gives the Selenium tests a running application.
 * <ul>
 *   <li>If {@code -DbaseUrl=http://host:8080/certificate-portal} is set, that already-deployed server is tested
 *       (used by Jenkins after the deploy stage).</li>
 *   <li>Otherwise the packaged WAR (target/certificate-portal.war) is started in an embedded Tomcat on a free port,
 *       with an in-memory database.</li>
 * </ul>
 */
final class TestServer {

    private static Tomcat tomcat;
    private static String baseUrl;

    private TestServer() {
    }

    static synchronized String start() throws Exception {
        if (baseUrl != null) {
            return baseUrl;
        }
        String remote = System.getProperty("baseUrl");
        if (remote != null && !remote.isBlank()) {
            baseUrl = remote.trim().replaceAll("/+$", "");
            System.out.println("[TestServer] Testing deployed application at " + baseUrl);
            return baseUrl;
        }

        File war = new File(System.getProperty("warFile", "target/certificate-portal.war")).getAbsoluteFile();
        if (!war.isFile()) {
            throw new IllegalStateException("WAR not found at " + war + ". Run 'mvn verify' so the package phase runs first.");
        }

        System.setProperty("db.url", "jdbc:h2:mem:certportal-it;DB_CLOSE_DELAY=-1");
        System.setProperty("app.env", "test");

        Path work = Files.createTempDirectory("certportal-tomcat");
        Files.createDirectories(work.resolve("webapps"));

        tomcat = new Tomcat();
        tomcat.setBaseDir(work.toString());
        tomcat.setPort(0);
        tomcat.getConnector();

        // One line per request in target/it-access.log: time, method, URL, status, milliseconds, session id.
        // If a Selenium test ever fails, this shows whether the browser's request reached the server.
        File logDir = new File("target").getAbsoluteFile();
        logDir.mkdirs();
        AccessLogValve accessLog = new AccessLogValve();
        accessLog.setDirectory(logDir.getPath());
        accessLog.setPrefix("it-access");
        accessLog.setSuffix(".log");
        accessLog.setFileDateFormat("");
        accessLog.setBuffered(false);
        accessLog.setPattern("%{HH:mm:ss.SSS}t %m %U%q -> %s in %D microseconds  session=%S");
        tomcat.getHost().getPipeline().addValve(accessLog);

        Context context = tomcat.addWebapp("/certificate-portal", war.getAbsolutePath());
        tomcat.start();

        if (!context.getState().isAvailable()) {
            throw new IllegalStateException("The web application failed to start. Check the Tomcat log output above.");
        }
        baseUrl = "http://localhost:" + tomcat.getConnector().getLocalPort() + "/certificate-portal";
        System.out.println("[TestServer] Embedded Tomcat started at " + baseUrl);
        warmUp(baseUrl);
        Runtime.getRuntime().addShutdownHook(new Thread(TestServer::stop));
        return baseUrl;
    }

    /**
     * The first request to each JSP makes Tomcat compile it, which can take a couple of seconds on a laptop.
     * Visiting every page once before the browser tests start keeps those one-off delays out of the tests.
     */
    private static void warmUp(String base) {
        try {
            HttpClient http = HttpClient.newBuilder()
                    .cookieHandler(new CookieManager())
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();
            get(http, base + "/login");
            HttpRequest login = HttpRequest.newBuilder(URI.create(base + "/login"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("username=admin&password=admin123"))
                    .timeout(Duration.ofSeconds(60))
                    .build();
            http.send(login, HttpResponse.BodyHandlers.discarding());
            for (String path : new String[] {"/dashboard", "/certificates", "/certificates?q=a&status=ISSUED",
                    "/certificates/new", "/certificates/1", "/certificates/1/edit", "/certificates/999999"}) {
                get(http, base + path);
            }
            System.out.println("[TestServer] Pages pre-compiled.");
        } catch (Exception e) {
            System.out.println("[TestServer] Warm-up skipped: " + e);
        }
    }

    private static void get(HttpClient http, String url) throws Exception {
        http.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(60)).GET().build(),
                HttpResponse.BodyHandlers.discarding());
    }

    private static synchronized void stop() {
        if (tomcat != null) {
            try {
                tomcat.stop();
                tomcat.destroy();
            } catch (Exception ignored) {
                // shutting down
            }
            tomcat = null;
        }
    }
}
