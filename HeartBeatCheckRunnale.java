import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.*;
import java.util.concurrent.ConcurrentHashMap;

public class HeartBeatCheckRunnale implements Runnable{


    private Map <ServerInfo, Date> serverStatus;

    public HeartBeatCheckRunnale(Map <ServerInfo, Date> serverStatus) {
        this.serverStatus = serverStatus;
    }

    @Override
    public void run() {
        while(true) {
			//System.out.print("Running checker");
			
			synchronized(serverStatus){
				for (Entry<ServerInfo, Date> entry : serverStatus.entrySet()) {
					// if greater than 2T, remove
					if (new Date().getTime() - entry.getValue().getTime() > 4000) {
						serverStatus.remove(entry.getKey());
					
						System.out.println("Removed entry" + entry.getKey().getHost() + " port " + entry.getKey().getPort()+ " size is " + serverStatus.size());
					} else {
						System.out.println(entry.getKey() + "-" + entry.getValue() + " ");
					}
				}
			}
            
            //System.out.println();

            // sleep for two seconds
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
            }
        }
    }
}
