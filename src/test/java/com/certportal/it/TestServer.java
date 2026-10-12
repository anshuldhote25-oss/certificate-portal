package com.certportal.it;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;

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
        Context context = tomcat.addWebapp("/certificate-portal", war.getAbsolutePath());
        tomcat.start();

        if (!context.getState().isAvailable()) {
            throw new IllegalStateException("The web application failed to start. Check the Tomcat log output above.");
        }
        baseUrl = "http://localhost:" + tomcat.getConnector().getLocalPort() + "/certificate-portal";
        System.out.println("[TestServer] Embedded Tomcat started at " + baseUrl);
        Runtime.getRuntime().addShutdownHook(new Thread(TestServer::stop));
        return baseUrl;
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
