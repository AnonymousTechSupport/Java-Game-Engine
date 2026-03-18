package game.engine.logging;

/**
 * Defines the severity levels for log messages. The order is from most verbose
 * (TRACE) to most critical (FATAL).
 */
public enum LogLevel {
    /** Extremely detailed information, useful for low-level debugging. */
    TRACE,
    /** General-purpose information for developers. */
    DEBUG,
    /** Noteworthy application events. */
    INFO,
    /** Indicates a potential issue that is recoverable. */
    WARN,
    /**
     * A serious error occurred, but the application can attempt to continue.
     */
    ERROR,
    /**
     * A critical, unrecoverable error that should terminate the application.
     */
    FATAL
}
