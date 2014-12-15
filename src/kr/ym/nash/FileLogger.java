package kr.ym.nash;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Date;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.util.Log;

public class FileLogger {

    private static final Executor executor = /*new SerialExecutor();*/ new ThreadPoolExecutor(10, 50, 1,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    private static final long LOG_FILE_MAX_SIZE = 5242880;

    // constants
    private static final String TAG = FileLogger.class.getName();
    private final static String FILE_NAME = "Log.txt";
    private final static String NEW_FILE_NAME = "NewLog.txt";

    public static File getLogFile(Context context) {
        return new File(context.getFilesDir(), FILE_NAME);
    }

    private static File getNewLogFile(Context context) {
        return new File(context.getFilesDir(), NEW_FILE_NAME);
    }

    private static void appendElementaryLog(Context context, String text) {
        FileLogger.trimFileIfNeed(context);
        if (BuildConfig.DEBUG) Log.d(TAG, text);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(getLogFile(context), true));
            writer.append(text + "\n");
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void appendLog(final Context context, final String text) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                int bytesInMb = 1048576;
                Runtime runtime = Runtime.getRuntime();
                long totalMemory = runtime.totalMemory();
                long freeMemory = runtime.freeMemory();
                long maxMemory = runtime.maxMemory();
                long memoryUsage = totalMemory - freeMemory;
                double memoryUsageMb = (double)memoryUsage / bytesInMb;
                double maxMemoryMb = (double)maxMemory / bytesInMb;

                /*String memoryUsageMbFormatted = NumberFormatter.formatDouble(memoryUsageMb, 2);
                String maxMemoryMbFormatted = NumberFormatter.formatDouble(maxMemoryMb, 2);*/

                appendElementaryLog(context, "[" + DateFormatter.fullDateAndTimeMs(new Date()) + "]"/*+"["+memoryUsageMbFormatted+"Mb/"+maxMemoryMbFormatted+"Mb]"*/+": " + text.replace("\\/", "/"));
            }
        });
    }

    private static void trimFileIfNeed(Context context) {
        if (getLogFile(context).length() > LOG_FILE_MAX_SIZE) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(getLogFile(context)));
                BufferedWriter writer = new BufferedWriter(new FileWriter(getNewLogFile(context)));

                // delete old log file and rename new log file
                reader.close();
                writer.close();
                getLogFile(context).delete();
                getNewLogFile(context).renameTo(getLogFile(context));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
