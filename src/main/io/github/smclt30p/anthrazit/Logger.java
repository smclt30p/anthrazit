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
import java.io.IOException;

public class Logger implements LoggerInfo {
    
    public static final int DEBUG = 0;
    public static final int INFO = 1;
    public static final int ERROR = 2;
    public static final int FATAL = 3;
    public static final int EXCEPTION = 4;

    private static final String LOGTAG = "anthrazit";

    private static boolean exitOnFatal;
    private static boolean debug;
    private static long startTime;

    private static Logger instance;
    private static File log;
    private static FileOutputStream out;

    private Logger() {

    }

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
            
            out = new FileOutputStream(log);

            write(LOGTAG, "Successfully started at " + startTime, INFO);
            write(LOGTAG, "fileName: " + fileName + "-" + startTime + ".log, logPath: " 
            + logPath + ", exitOnFatal: " + exitOnFatal + ", debug: true", DEBUG);

        } catch (IOException e) {
            errorOnInit(e);
        }

    }
 
    @Override
    public synchronized void write(String logtag, String reason, int severity) {
        
        long logtime = System.currentTimeMillis();
        String message = "";

        switch (severity) {
            case DEBUG:
                if (!debug) return;
                message = "[" + logtime + "] {DEBUG} " + logtag + ": " + reason + "\n";
                break;
            case INFO:
                message = "[" + logtime + "] {INFO} " + logtag + ": " + reason + "\n";
                break;
            case ERROR:
                message = "[" + logtime + "] {ERROR} " + logtag + ": " + reason + "\n";
                break;
            case FATAL:
                message = "[" + logtime + "] {FATAL} " + logtag + ": " + reason + "\n";
               break;
            case EXCEPTION:
                message = "[" + logtime + "] {EXCEPTION} " + reason;
                break;
            default:
                message = "Anthrazit error! Invalid severity: " + severity;
        } 

        try {
            out.write(message.getBytes());
        } catch (IOException e) {
            errorOnInit(e);
        }

        if (exitOnFatal && severity == FATAL) {
            System.err.println("Anthrazit exit on fatal: " + message + ". Please check the logs.");
            close();
            System.exit(1);
        }
  
    }
   
    @Override
    public void close() {
        try {
            out.close();
        } catch (IOException e) {
            errorOnInit(e);
        }
    }

    public static Logger getLogger() {
        return instance == null ? new Logger() : instance;
    }

    @Override
    public synchronized void catchException(Exception e) {
        
        String trace = e.toString() + "\n";
        
        for (StackTraceElement el : e.getStackTrace()) {
            trace += "\t\t at " + el.toString() + "\n";
        }

        write("Exception", trace, EXCEPTION);

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
