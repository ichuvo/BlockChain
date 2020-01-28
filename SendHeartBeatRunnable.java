
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class SendHeartBeatRunnable implements Runnable{


    private int localPort;
	private int sequenceNumber; 
	private String remoteIP; 
	private int remotePort; 
    public SendHeartBeatRunnable(String remoteIP, int remotePort,  int localPort, int sequenceNumber) {
		
       
		this.localPort = localPort; 
		this.sequenceNumber = sequenceNumber; 
		this.remotePort = remotePort; 
		this.remoteIP = remoteIP; 
    }

    @Override
    public void run() {
        try {
            // create socket with a timeout of 2 seconds
			
			//System.out.println("Runs SendHeartBeatThread"); 
            Socket toServer = new Socket();
            toServer.connect(new InetSocketAddress(remoteIP, remotePort), 2000);
			
			//System.out.println("connected " + serverInfo.getHost() + " port" + serverInfo.getPort()); 
            PrintWriter printWriter = new PrintWriter(toServer.getOutputStream(), true);
			
            // send the message forward
			
			//System.out.println("hb|" + localPort + "|" + sequenceNumber+"\n"); 
            printWriter.println("hb|" + localPort + "|" + sequenceNumber+"\n");
            printWriter.flush();

            // close printWriter and socket
            printWriter.close();
            toServer.close();
        } catch (IOException e) {
        }
    }
}




