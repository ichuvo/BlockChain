



import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;

public class SendLatestBlockRunnable implements Runnable{


    private int localPort; 
	private String remoteIP; 
	private int remotePort; 
	private int blockChainSize;
	private String latestBlockHash; 
	
    public SendLatestBlockRunnable(String remoteIP, int remotePort,  int localPort, int blockChainSize, String latestBlockHash) {
		
       
		this.localPort = localPort; 
		this.remotePort = remotePort; 
		this.remoteIP = remoteIP; 
		this.blockChainSize = blockChainSize; 
		this.latestBlockHash = latestBlockHash; 
    }

    @Override
    public void run() {
		//synchronized(blockchain){
			try {
            // create socket with a timeout of 2 seconds
			
			//System.out.println("Runs SendHeartBeatThread"); 
            Socket toServer = new Socket();
            toServer.connect(new InetSocketAddress(remoteIP, remotePort), 2000);
			
			//System.out.println("connected " + serverInfo.getHost() + " port" + serverInfo.getPort()); 
            PrintWriter printWriter = new PrintWriter(toServer.getOutputStream(), true); // removed ,true
			
            // send the message forward
			
			System.out.println("lb|" + localPort + "|" + blockChainSize+ "|" + latestBlockHash + "\n"); 
            printWriter.println("lb|" + localPort + "|" + blockChainSize+ "|" + latestBlockHash + "\n");
            printWriter.flush();

            // close printWriter and socket
            printWriter.close();
            toServer.close();
        } catch (IOException e) {
        	}
			
		//}
        
    }
}















