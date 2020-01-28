import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;



public class HeartBeatInformRunnable implements Runnable{
	
	private ServerInfo serverInfo; 
	private String heartBeatSenderIp; 
	private int heartBeatSenderPort; 
	private int localPort; 
	private String remoteIP; 
	private int remotePort; 
	public HeartBeatInformRunnable(String remoteIP, int remotePort, int localPort, String heartBeatSenderIp, int heartBeatSenderPort){
		this.remoteIP = remoteIP; 
		this.remotePort = remotePort; 
		
		this.localPort = localPort; 
		this.heartBeatSenderIp = heartBeatSenderIp; 
		this.heartBeatSenderPort = heartBeatSenderPort;  
	}
	
	 @Override
    public void run() {
        try {
            // create socket with a timeout of 2 seconds
            Socket toServer = new Socket();
            toServer.connect(new InetSocketAddress(remoteIP, remotePort), 2000);
			
			System.out.println("connected " + remoteIP + " port" + remotePort); 
            PrintWriter printWriter = new PrintWriter(toServer.getOutputStream(), true);

            // send the message forward
			//System.out.println("si|"+ localPort + "|" + heartBeatSenderIp +"|" + heartBeatSenderPort+"\n");
			
			printWriter.println("si|"+ localPort + "|" + heartBeatSenderIp +"|" + heartBeatSenderPort+"\n");
            printWriter.flush();
			
			
          

            // close printWriter and socket
            printWriter.close();
            toServer.close();
        } catch (IOException e) {
        }
    }
	
	
	
}




















