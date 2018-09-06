
import com.sun.jna.Platform;
import org.apache.log4j.Logger;
import com.sun.jna.Library;
import com.sun.jna.Native;

import java.lang.reflect.Field;


public class JNAUtil {
    public static Logger log = Logger.getLogger(JNAUtil.class);

    public static void killProcessTree(Process process){
        try {
            log.info("to kill process by JNA");
            Long pid = getProcessId(process);
            log.info("pid = "+pid);
            String cmd = getKillProcessTreeCmd(pid);
            Runtime rt = Runtime.getRuntime();
            Process killPrcess = rt.exec(cmd);
            killPrcess.waitFor();
            killPrcess.destroy();
            log.info("kill process by JNA success ");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getKillProcessTreeCmd(Long pid) {
        String result = "";
        if(pid !=null) {
            result = "cmd.exe /c taskkill /PID " + pid + " /F /T ";
        }
        return result;
    }


    private static long getProcessId(Process process) {
        long pid = -1;
        Field field = null;
        if (Platform.isWindows()) {
            try {
                field = process.getClass().getDeclaredField("handle");
                field.setAccessible(true);
                pid = Kernel32.INSTANCE.GetProcessId((Long) field.get(process));
            } catch (Exception ex) {
                log.error(ex);
            }
        } else if (Platform.isLinux() || Platform.isAIX()) {
            try {
                Class<?> clazz = Class.forName("java.lang.UNIXProcess");
                field = clazz.getDeclaredField("pid");
                field.setAccessible(true);
                pid = (Integer) field.get(process);
            } catch (Throwable e) {
                log.error(e);
            }
        } else {
        }

        return pid;

    }



    interface Kernel32 extends Library {
        public static Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class);
        public long GetProcessId(Long hProcess);
    }


}
