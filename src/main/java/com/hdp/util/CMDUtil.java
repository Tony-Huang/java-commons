
import org.apache.log4j.Logger;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CMDUtil {
    public static final String OS = System.getProperty("os.name");
    public static Logger log = Logger.getLogger(CMDUtil.class);

    static ExecutorService es1 = Executors.newFixedThreadPool(2);
    public static Process broadcastProcess;

    public static void shutDown(){
        if(broadcastProcess!=null){
            JNAUtil.killProcessTree(broadcastProcess);
        }
        es1.shutdown();
    }

    public static void fileFeed(String file,String feedUrl) throws Exception {
        String cmd = "ffmpeg  -i "+file +" -vn -acodec  libmp3lame " +feedUrl;
        runOSCommand(cmd);
    }

    public static void micFeed(String micName, String feedUrl) throws Exception {
        String cmd="";
        if(OS.toLowerCase().contains("windows")) {
            cmd = "ffmpeg -f dshow -i audio=" + micName + " -vn -acodec libmp3lame -ac 1 -b:a 64k -ar 44100 " + feedUrl;
        }
        runOSCommand(cmd);
    }


    public static void runOSCommand(String cmd) throws Exception{
        System.out.println("os="+OS);
        if(OS.toLowerCase().contains("windows")){
            runCMD("cmd",cmd);
        } else if(OS.toLowerCase().contains("linux")){
            runCMD("sh",cmd);
        } else {
            runCMD("sh",cmd);
        }
    }

    protected static void runCMD(String type,String cmd)  throws IOException,InterruptedException {
        Process p = buildProcess(type, cmd);
        InputStream in = p.getInputStream();
        InputStream err = p.getErrorStream();
        es1.submit(()-> readInputStream(in));
        es1.submit(()->readInputStream(err));
        int exitCode = p.waitFor();
        log.info("command exit code="+exitCode);
    }


    /**
     *
     * @param cmdType windows: cmd , linux: sh
     * @param cmd
     * @return
     */
    private static Process buildProcess(String cmdType, String cmd) throws IOException {
        log.info("command = "+cmd);
        ProcessBuilder pb = new ProcessBuilder(cmdType, "/c", cmd);
        pb.redirectErrorStream();
        Process p = pb.start();
        broadcastProcess = p;
        log.info("process="+p);
        return p;

    }

    private static void readInputStream(InputStream in) {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        try {
            StringBuilder buf = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null){
                buf.append(line).append(System.lineSeparator());
            }
            log.info(buf);
        } catch(Exception e){
            log.error(e);
        }

    }


    public static void main(String[]  args) throws  Exception {
        String file="C:\\Users\\xxx\\Desktop\\半壶纱.mp3";
      
        String feedUrl = "http://192.168.0.150:2000/feed3.ffm";
        //runOSCommand("ffmpeg  -i "+file +"  -vn -acodec  libmp3lame " +feedUrl);
        //fileFeed(file,feedUrl);

        String file2 = "\"Microphone Array (Realtek High Definition Audio)\"";
        String micFeedUrl = "http://192.168.0.150:2000/feed2.ffm";
        //runOSCommand("ffmpeg -i "+file2 +" -vn -acodec  libmp3lame "+micFeedUrl);
        micFeed(file2,micFeedUrl);


        es1.shutdown();

    }
}
