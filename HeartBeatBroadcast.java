import java.util.*;

public class HeartBeatBroadcast implements Runnable {

    private Map<ServerInfo, Date> serverStatus;
	private int localPort; 
    private int sequenceNumber;
	private String localIp;
    public HeartBeatBroadcast(Map<ServerInfo, Date> serverStatus, int localPort) {
        this.serverStatus = serverStatus;
		this.localPort = localPort; 
        this.sequenceNumber = 0;
    }

    @Override
    public void run() {
        while(true) {
            // broadcast HeartBeat message to all peers
            ArrayList<Thread> threadArrayList = new ArrayList<>();
			
			//synchronized(serverStatus){
				for (ServerInfo info : serverStatus.keySet()) {
					//System.out.println("sending the heartbeat to " + info.getHost() + " " + info.getPort()); 
					Thread thread = new Thread(new SendHeartBeatRunnable(info.getHost(), info.getPort(), localPort, sequenceNumber));
					threadArrayList.add(thread);
					thread.start();
            	}
			//}
            

            for (Thread thread : threadArrayList) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                }
            }

            // increment the sequenceNumber
            sequenceNumber += 1;

            // sleep for two seconds
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
    }
}





















