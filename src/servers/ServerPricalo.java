package src.servers;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerPricalo {
    
    private static final int TCP_PORT = 9000;
    
    private static long startTime = 0;
    
    public static void main(String[] args) {
        
        ServerSocket ss = null;
        Socket socket = null;
        
        setStartTime(System.currentTimeMillis());
        
        try {
            
            File file = new File("server/online.txt");
            
            file.delete();
            
            ss = new ServerSocket(TCP_PORT);
            
            while (true) {
                socket = ss.accept();
                new ServerPricaloThread(socket);
            }
            
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }
    
    public static long getStartTime() {
        return startTime;
    }
    
    private static void setStartTime(long startTime) {
        ServerPricalo.startTime = startTime;
    }
    
}
