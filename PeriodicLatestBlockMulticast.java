

import java.util.*;

public class PeriodicLatestBlockMulticast implements Runnable{
	
		private Map<ServerInfo, Date> serverStatus;
		private int localPort; 
		private Blockchain blockchain; 
		private Random rand; 
		public PeriodicLatestBlockMulticast(Map<ServerInfo, Date> serverStatus, int localPort, Blockchain blockchain) {
			this.serverStatus = serverStatus;
			this.localPort = localPort; 
			this.blockchain = blockchain; 
			rand = new Random();
    	}
	
    	@Override
    	public void run() {
			
			while(true) {
				// broadcast HeartBeat message to all peers
				ArrayList<Thread> threadArrayList = new ArrayList<>();
				ArrayList<ServerInfo> tmpList = new ArrayList<>(); 
				Block lastBlock = blockchain.getHead();
				
				//synchronized(serverStatus){
					if (lastBlock == null || serverStatus.size() == 0){
						try {
							Thread.sleep(2000);
							continue; 
						} catch (InterruptedException e) {
						}
					}
					for(ServerInfo info : serverStatus.keySet()){
						tmpList.add(info); 
					}
					for (int i = 0; i < 5; i++){
						if(tmpList.size() == 0)
							break; 
						
						//System.out.println("tmpList.size() - 1  = " + (tmpList.size() - 1)); 
						int n = 0; 
						
						if(tmpList.size() > 1)
							n = rand.nextInt((tmpList.size() - 1));
						
						
						
						String host = tmpList.get(n).getHost(); 
						int port = tmpList.get(n).getPort();
						
						 
						String hashString64 = Base64.getEncoder().encodeToString(lastBlock.calculateHash()); 
						
						Thread thread = new Thread(new SendLatestBlockRunnable(host, port, localPort, blockchain.getLength(), hashString64));
						threadArrayList.add(thread);
						thread.start();
						
						tmpList.remove(n); 
					}
					// for (ServerInfo info : serverStatus.keySet()) {
					// 	//System.out.println("sending the heartbeat to " + info.getHost() + " " + info.getPort()); 
					// 	Thread thread = new Thread(new SendHeartBeatRunnable(info.getHost(), info.getPort(), localPort, sequenceNumber));
					// 	threadArrayList.add(thread);
					// 	thread.start();
					// }
				//}


				for (Thread thread : threadArrayList) {
					try {
						thread.join();
					} catch (InterruptedException e) {
					}
				}

				

				// sleep for two seconds
				try {
					Thread.sleep(40000);
				} catch (InterruptedException e) {
				}
        	}
			
			
			
			
			
		}
	
}













