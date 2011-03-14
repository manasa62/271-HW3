package hw3;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author Manasa Chandrasekhar
 * @author Kowshik Prakasam
 *
 */
public class hw3Logger {
        public static Logger getLogger(String classId, String logFile) throws IOException {
                File logDir = new File(Constants.LOG_DIR);

                if(!logDir.exists() &&  !logDir.mkdirs()){
                        throw new IOException("Can't create root log dir : "+Constants.LOG_DIR);
                }
                Logger aLogger = Logger.getLogger(classId);
                //logger.setUseParentHandlers(false);
                Handler logHandle = null;
                try {
                        logHandle = new FileHandler(logFile);
                } catch (SecurityException e) {
                        System.err.println("Can't init logger in "+classId);
                        e.printStackTrace();
                } catch (IOException e) {
                        System.err.println("Can't init logger in "+classId);
                        e.printStackTrace();
                }
                logHandle.setFormatter(new SimpleFormatter());
                aLogger.addHandler(logHandle);
                aLogger.info("init "+classId+" Logger successful");
                return aLogger;
        }
}

