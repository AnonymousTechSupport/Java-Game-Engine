package game.engine.logging;

import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

/**
 * A simple, static, and performant logging utility for the game engine.
 * This class provides standard logging levels, lazy message evaluation,
 * and category-based filtering.
 */
public final class Logger {

    // Predefined categories
    public static final String RENDER = "Render";
    public static final String ECS = "ECS";
    public static final String ENGINE = "Engine";
    public static final String INPUT = "Input";
    public static final String WORLD = "World";
    public static final String UI = "UI";

    private static LogLevel minLevel = LogLevel.INFO;
    private static boolean showTime = true;
    private static boolean showThread = true;
    private static boolean showCategory = true;
    private static boolean useColors = true;

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    // Default fatal handler prints a clear colored banner then halts.
    private static Runnable fatalHandler = () -> {
        if (useColors) System.err.println("\n" + Ansi.BRIGHT_RED + "--- FATAL ENGINE FAILURE ---" + Ansi.RESET);
        else System.err.println("\n--- FATAL ENGINE FAILURE ---");
        try { Thread.sleep(1200); } catch (InterruptedException ignored) {}
        Runtime.getRuntime().halt(1);
    };

    private static final class Ansi {
        static final String RESET = "\u001B[0m";
        static final String BRIGHT_BLACK = "\u001B[90m";
        static final String BRIGHT_BLUE = "\u001B[94m";
        static final String BRIGHT_GREEN = "\u001B[92m";
        static final String BRIGHT_YELLOW = "\u001B[93m";
        static final String BRIGHT_MAGENTA = "\u001B[95m";
        static final String BRIGHT_RED = "\u001B[91m";
        static final String BOLD = "\u001B[1m";
    }

    private Logger() {}

    // Configuration
    /** Sets the minimum log level required to be shown. */
    public static void setMinLevel(LogLevel level) { minLevel = level; }
    /** Convenience shortcut to set minimum level to WARN. */
    public static void quiet() { setMinLevel(LogLevel.WARN); }
    /** Convenience shortcut to set minimum level to DEBUG. */
    public static void verbose() { setMinLevel(LogLevel.DEBUG); }
    /** Convenience shortcut to set minimum level to TRACE. */
    public static void veryVerbose() { setMinLevel(LogLevel.TRACE); }
    /** Toggles the display of timestamps. */
    public static void setShowTime(boolean show) { showTime = show; }
    /** Toggles the display of the thread name. */
    public static void setShowThread(boolean show) { showThread = show; }
    /** Toggles the display of the category tag. */
    public static void setShowCategory(boolean show) { showCategory = show; }
    /** Sets a custom handler to run after a fatal log. */
    public static void setFatalHandler(Runnable handler) { fatalHandler = handler; }
    /** Enable or disable ANSI colors in console output. */
    public static void setUseColors(boolean use) { useColors = use; }

    // Public Logging API
    public static void trace(String message) { log(LogLevel.TRACE, null, message); }
    public static void trace(Supplier<String> msg) { log(LogLevel.TRACE, null, msg); }
    public static void trace(String cat, String msg) { log(LogLevel.TRACE, cat, msg); }
    public static void trace(String cat, Supplier<String> msg) { log(LogLevel.TRACE, cat, msg); }

    public static void debug(String message) { log(LogLevel.DEBUG, null, message); }
    public static void debug(Supplier<String> msg) { log(LogLevel.DEBUG, null, msg); }
    public static void debug(String cat, String msg) { log(LogLevel.DEBUG, cat, msg); }
    public static void debug(String cat, Supplier<String> msg) { log(LogLevel.DEBUG, cat, msg); }

    public static void info(String message) { log(LogLevel.INFO, null, message); }
    public static void info(String cat, String msg) { log(LogLevel.INFO, cat, msg); }

    public static void warn(String message) { log(LogLevel.WARN, null, message); }
    public static void warn(String cat, String msg) { log(LogLevel.WARN, cat, msg); }

    public static void error(String message) { log(LogLevel.ERROR, null, message); }
    public static void error(String cat, String msg) { log(LogLevel.ERROR, cat, msg); }
    public static void error(String msg, Throwable t) { log(LogLevel.ERROR, null, msg, t); }
    public static void error(String cat, String msg, Throwable t) { log(LogLevel.ERROR, cat, msg, t); }

    public static void fatal(String message) { log(LogLevel.FATAL, null, message); }
    public static void fatal(String cat, String msg) { log(LogLevel.FATAL, cat, msg); }
    public static void fatal(Throwable t) { log(LogLevel.FATAL, null, t.getMessage(), t); }
    public static void fatal(String msg, Throwable t) { log(LogLevel.FATAL, null, msg, t); }
    
    // Assertion Helpers
    /** Checks a condition and logs a fatal error if it is false. */
    public static void check(boolean condition, String message) {
        if (!condition) {
            log(LogLevel.FATAL, ENGINE, message);
        }
    }

    /** Checks a condition and logs a fatal error using a lazily-supplied message if it is false. */
    public static void check(boolean condition, Supplier<String> messageSupplier) {
        if (!condition) {
            log(LogLevel.FATAL, ENGINE, messageSupplier.get());
        }
    }

    // Core Logging Logic
    private static void log(LogLevel level, String category, Supplier<String> msgSupplier) {
        if (minLevel.ordinal() > level.ordinal()) {
            return;
        }
        log(level, category, msgSupplier.get(), null);
    }

    private static void log(LogLevel level, String category, String message) {
        log(level, category, message, null);
    }
    
    private static void log(LogLevel level, String category, String message, Throwable t) {
        if (level != LogLevel.FATAL && minLevel.ordinal() > level.ordinal()) {
            return;
        }

        StringBuilder builder = new StringBuilder();
        if (showTime) {
            builder.append(TIME_FORMATTER.format(java.time.LocalTime.now())).append(" ");
        }
        if (showThread) {
            builder.append("[").append(Thread.currentThread().getName()).append("] ");
        }
        // severity with optional color
        String levelText = String.format("%-5s", level.name());
        if (useColors) {
            String color = getColor(level);
            builder.append(color).append(levelText).append(Ansi.RESET);
        } else {
            builder.append(levelText);
        }

        if (showCategory && category != null && !category.isEmpty()) {
            builder.append(" [").append(category).append("] ");
        } else {
            builder.append(" ");
        }
        builder.append(message);

        synchronized (Logger.class) {
            var stream = (level == LogLevel.ERROR || level == LogLevel.FATAL) ? System.err : System.out;
            stream.println(builder.toString());
            if (t != null) {
                t.printStackTrace(stream);
            }
        }
        
        if (level == LogLevel.FATAL) {
            fatalHandler.run();
        }
    }

    private static String getColor(LogLevel level) {
        switch (level) {
            case TRACE: return Ansi.BRIGHT_BLACK;       // subtle
            case DEBUG: return Ansi.BRIGHT_BLUE;        // developer-focused
            case INFO:  return Ansi.BRIGHT_GREEN;       // positive/ok
            case WARN:  return Ansi.BRIGHT_YELLOW;      // caution
            case ERROR: return Ansi.BRIGHT_MAGENTA;     // visible and distinct
            case FATAL: return Ansi.BOLD + Ansi.BRIGHT_RED; // urgent
            default:    return Ansi.RESET;
        }
    }
}
