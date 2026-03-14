package bigguy.treesitter.java.spi;

import java.util.Map;

/**
 * Service Provider Interface for Java debug adapter integration.
 *
 * <p>Modeled after Zed's Java Debug adapter (DAP) integration.
 * Supports launch and attach configurations as defined in
 * Zed's {@code debug_adapter_schemas/Java.json}.</p>
 *
 * <p>Implementors would typically wrap {@code java-debug} or a compatible DAP.</p>
 */
public interface DebugAdapter {

    enum SessionState { IDLE, LAUNCHING, RUNNING, PAUSED, STOPPED }

    SessionState getState();

    /**
     * Launches a debug session for the given main class.
     *
     * @param mainClass  fully qualified class name
     * @param args       program arguments
     * @param vmArgs     JVM arguments
     * @param classPaths classpath entries
     * @param env        environment variables
     */
    void launch(String mainClass, String[] args, String[] vmArgs,
                String[] classPaths, Map<String, String> env);

    /**
     * Attaches to an already-running JVM.
     *
     * @param hostName the host to connect to
     * @param port     the debug port
     * @param timeout  connection timeout in milliseconds
     */
    void attach(String hostName, int port, int timeout);

    void disconnect();
}
