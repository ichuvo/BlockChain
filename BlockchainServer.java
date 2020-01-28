import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.net.InetSocketAddress;
public class BlockchainServer {

    public static void main(String[] args) {

        if (args.length != 3) {
            return;
        }

        int localPort = 0;
        int remotePort = 0;
        String remoteHost = null;

        try {
            localPort = Integer.parseInt(args[0]);
            remoteHost = args[1];
            remotePort = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            return;
        }

        Blockchain blockchain = new Blockchain();

        Map<ServerInfo, Date> serverStatus = new ConcurrentHashMap<ServerInfo, Date>();
        serverStatus.put(new ServerInfo(remoteHost, remotePort), new Date());
		
		
		
		System.out.println("blockchain is " + blockchain.toString()); 
		
		
		Block headBlock = null;
		try {
			
			Socket toServer = new Socket(); 
			toServer.connect(new InetSocketAddress(remoteHost, remotePort), 2000);
			System.out.println("stream set up");
			
			//ObjectInputStream inStreamToServer = new ObjectInputStream(toServer.getInputStream()); 
			//ObjectOutputStream  outStreamToServer = new ObjectOutputStream(toServer.getOutputStream()); 
			
			PrintWriter printWriter = new PrintWriter(toServer.getOutputStream());
			printWriter.println("cu" + "\n");
			printWriter.flush();
			
			
			ObjectInputStream inStreamToServer = new ObjectInputStream(toServer.getInputStream()); 	
			System.out.println("stream is done");
			
			try{
				headBlock = (Block) inStreamToServer.readObject();
				//inStreamToServer.reset(); 
			}
			catch(ClassNotFoundException ex){
				System.out.println("class not found exception");
			}
		
			
			if(headBlock == null){
				System.out.println("chain is empty"); 
			}
			else{
				 
				System.out.println("HeadBlock is " + headBlock.toString()); 
			}
			
				
			System.out.println("closed connection for catching up"); 
			
			printWriter.println("cc"+ "\n");
			printWriter.flush();
			toServer.close(); 
			printWriter.close(); 
			inStreamToServer.close(); 
		}
		catch (IOException e) {
			System.out.println("didnt connect for first catch up"); 
        }
		
		
		
		Block block = headBlock; 
		Block blockAhead = null;
		while(true){
				try{
					if (headBlock != null){
						//build the whole chain
						String tmp = Base64.getEncoder().encodeToString(block.getPreviousHash());
						String end = Base64.getEncoder().encodeToString(new byte[32]);
						
							if(block.getPreviousHash() == null || tmp.equals(end)){ // AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
								// reached end of the blockchain 
								System.out.println("reached end of the blockchain from if");
								blockchain.setHead(headBlock); 
								System.out.println(blockchain.toString());
								break; 
							}

						byte[] prevBlockHashBytes = block.getPreviousHash(); 
						String prevBlockHash = Base64.getEncoder().encodeToString(prevBlockHashBytes);


						Socket toServer = new Socket();
						toServer.connect(new InetSocketAddress(remoteHost, remotePort), 2000);
						//System.out.println("stream set up");
						PrintWriter printWriter = new PrintWriter(toServer.getOutputStream());
						//System.out.println("Command for searching is cu|"+prevBlockHash+ "\n");
						printWriter.println("cu|"+prevBlockHash+ "\n");
						printWriter.flush();
						
						ObjectInputStream inStreamToServer = new ObjectInputStream(toServer.getInputStream()); 	
						//System.out.println("stream is done");

						try{
							blockAhead = block; 
							//inStreamToServer.reset();
							System.out.println("about to read another block");
							block = (Block) inStreamToServer.readObject(); 

							System.out.println("New received Block is " + block.toString());
							blockAhead.setPreviousBlock(block); 
							blockAhead.setPreviousHash(prevBlockHashBytes); 
						}
						catch(ClassNotFoundException ex){
							System.out.println("class not found exception");
						}

						printWriter.println("cc"+ "\n");
						printWriter.flush();
						toServer.close(); 
						printWriter.close(); 
						inStreamToServer.close();
					
					}
					else{break;}
					System.out.println("running loop");
					}
				catch(IOException ex){
					System.out.println("didnt 2 ");
					System.out.println("reached end of the blockchain from catch");
					blockchain.setHead(headBlock); 
					System.out.println(blockchain.toString());
					break; 
				}
				
		}
		
		// check if seen heartBeat
		System.out.println("start checker");
		new Thread(new HeartBeatCheckRunnale(serverStatus)).start(); 
		System.out.println("start heartBeats ");
		new Thread(new HeartBeatBroadcast(serverStatus, localPort)).start();
		// hashMulticast 
		new Thread(new PeriodicLatestBlockMulticast(serverStatus, localPort, blockchain)).start();
		
			
			
        PeriodicCommitRunnable pcr = new PeriodicCommitRunnable(blockchain);
        Thread pct = new Thread(pcr);
        pct.start();

        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(localPort);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new BlockchainServerRunnable(clientSocket, blockchain, serverStatus)).start();
            }
        } catch (IllegalArgumentException e) {
        } catch (IOException e) {
        } finally {
            try {
                pcr.setRunning(false);
                pct.join();
                if (serverSocket != null)
                    serverSocket.close();
            } catch (IOException e) {
            } catch (InterruptedException e) {
            }
        }
    }
}
