package io.github.smclt30p.anthrazit;

/*
 * Copyright (C) 2016  Ognjen Galić (smclt30p@gmail.com)
 * 
 * This file is part of Anthrazit.
 * 
 * Anthrazit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, up to version 2 of the License.
 * 
 * Anthrazit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Anthrazit. If not, see <http://www.gnu.org/licenses/>.
*/

import java.io.File;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * This is Anthrazit's main class used inside code,
 * the Logger.
 *
 * Please see the method init() for more details.
 *
 * <b>This class cannot be instantiated. Please use getLogger() instead.</b>
*/
public class Logger implements LoggerInfo {
   
    /**
     * Debug levels of logging.
    */
    public static final int DEBUG = 0;
    /**
     * Info levels of logging.
    */
    public static final int INFO = 1;
    /**
     * Error levels of logging.
    */
    public static final int ERROR = 2;
    /**
     * FATAL levels of logging.
    */
    public static final int FATAL = 3;

    private static final int EXCEPTION = 4;
    private static final String LOGTAG = "anthrazit";

    private static boolean exitOnFatal;
    private static boolean debug;
    private static long startTime;

    private static Logger instance;
    private static File log;
    private static BufferedOutputStream out;

    private Logger() {

    }

    /**
     * This method is used to initialize the Anthrazit logger.
     *
     * There is 4 levels of severity:
     *          DEBUG: Gets printed only if debug is enabled
     *          INFO: Used for standard information
     *          ERROR: Prints errors
     *          FATAL: Fatal errors, can be specified if the program
     *          should be exited if this error occurs.
     *
     * If debug is enabled, the stack trace is logged to file and printed to stdout.
     *
     * Logs are saved as .log files, with a filename and a Unix Epoch timestamp.
     *
     * @param logPath The path for Anthrazit to save the log to. Without the trailing "/".
     * @param fileName The file name of the log. You specify this "fileName"-timestamp.log.
     * @param exitOnFatal Should Anthrazit bail out of the program if an Fatal error occurs.
     * @param debug Should debug be enabled.
     * @author Ognjen Galic (smclt30p@gmail.com)
     * @since 1.0
    */
    @Override
    public void init(String logPath, String fileName, boolean exitOnFatal, boolean debug) {

        this.exitOnFatal = exitOnFatal;
        this.debug = debug;
        this.startTime = System.currentTimeMillis();

        try {

            String path = logPath + "/" + fileName + "-" + startTime + ".log";

            log = new File(path);
            
            if (!log.createNewFile()) {
                throw new IOException("Error creating new log file: " + path);
            }
            
            out = new BufferedOutputStream(new FileOutputStream(log), 4096);

            write(LOGTAG, "Successfully started at " + startTime, INFO);
            write(LOGTAG, "fileName: " + fileName + "-" + startTime + ".log, logPath: " 
            + logPath + ", exitOnFatal: " + exitOnFatal + ", debug: true", DEBUG);

        } catch (IOException e) {
            errorOnInit(e);
        }

    }

    /**
     * Write messages to the log file.
     *
     * This method is used to write messages to the log file. 
     * The message format is like this:
     *
     * [timestamp] {SEVERITY} logtag: Message
     * 
     * @param logtag The log tag of the specified object.
     * @param reason The message itself.
     * @param severity The severity. Use Logger.SEVERITY here.
     * @author Ognjen Galic (smclt30p@gmail.com)
     * @since 1.0
    */
    @Override
    public synchronized void write(String logtag, String reason, int severity) {
        
        long logtime = System.currentTimeMillis();
        String message = "";

        switch (severity) {
            case DEBUG:
                if (!debug) return;
                message = "[" + logtime + "] {DEBUG} " + logtag + "% " + reason + "\n$";
                break;
            case INFO:
                message = "[" + logtime + "] {INFO} " + logtag + "% " + reason + "\n$";
                break;
            case ERROR:
                message = "[" + logtime + "] {ERROR} " + logtag + "% " + reason + "\n$";
                break;
            case FATAL:
                message = "[" + logtime + "] {FATAL} " + logtag + "% " + reason + "\n$";
               break;
            case EXCEPTION:
                message = "[" + logtime + "] {EXCEPTION} " + logtag + "% " + reason + "\n$";
                break;
            default:
                message = "Anthrazit error! Invalid severity: " + severity;
        } 

        try {
            out.write(message.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            errorOnInit(e);
        }

        if (exitOnFatal && severity == FATAL) {
            System.err.println("Anthrazit exit on fatal: " + message + ". Please check the logs.");
            close();
            System.exit(1);
        }
  
    }
    
    /**
     * Close the log file.
     *
     * This method closes the log file. This should be called at the end
     * of your program, or exceptions may occur. If they do, exit on fatal
     * in init will be respected.
     *
     * @author Ognjen Galic (smclt30p@gmail.com)
     * @since 1.0
    */
    @Override
    public void close() {
        try {
            out.flush();
            out.close();
        } catch (IOException e) {
            errorOnInit(e);
        }
    }

    /**
     * Get the logger instance.
     *
     * A logger cannot be instaniated. Please use getLogger()
     * to get an instance.
     *
     * @return The logger instance
    */
    public synchronized static Logger getLogger() {
        return instance == null ? new Logger() : instance;
    }

    /**
     * Catch an exception and log it to file.
     *
     * This is used for exception logging. If debug is enabled, 
     * the exception will also be printed to stdout.
     *
     * @param e The exception to log.
     * @author Ognjen Galic (smclt30p@gmail.com)
     * @since 1.0
    */
    @Override
    public synchronized void catchException(String logTag, Exception e) {
        
        StringBuilder trace = new StringBuilder(e.toString() + "\n");
        
        for (StackTraceElement el : e.getStackTrace()) {
            trace.append("\t\t at " + el.toString() + "\n");
        }

        write(logTag, trace.toString(), EXCEPTION);

    }

    private void errorOnInit(Exception e) {
        System.err.println("Anthrazit error: " + e.toString());
        if (exitOnFatal) {
            System.err.println("Anthrazit exit on fatal: Bailing...");
            close();
            System.exit(1);
        }
        if (debug) {
            e.printStackTrace();
        }
    }
}
