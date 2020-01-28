import java.io.*;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

public class BlockchainServerRunnable implements Runnable{

    private Socket clientSocket;
    private Blockchain blockchain;
    private Map<ServerInfo, Date> serverStatus;
	private ObjectInputStream inStream;
	private ObjectOutputStream  outStream = null;

    public BlockchainServerRunnable(Socket clientSocket, Blockchain blockchain, Map<ServerInfo, Date> serverStatus) {
        this.clientSocket = clientSocket;
        this.blockchain = blockchain;
        this.serverStatus = serverStatus;
    }

    public void run() {
        try {
            serverHandler(clientSocket.getInputStream(), clientSocket.getOutputStream());
            clientSocket.close();
        } catch (IOException e) {
        }
    }

    public void serverHandler(InputStream clientInputStream, OutputStream clientOutputStream) {

        BufferedReader inputReader = new BufferedReader(
                new InputStreamReader(clientInputStream));
        PrintWriter outWriter = new PrintWriter(clientOutputStream, true);
 
		//outStream = new ObjectOutputStream(outWriter); 
// 		try{
// 			inStream = new ObjectInputStream(clientSocket.getInputStream()); 
// 			outStream = new ObjectOutputStream(clientSocket.getOutputStream());
// 		}
// 		catch(IOException e){
			
// 		}
		 
		
		String localIP = (((InetSocketAddress) clientSocket.getLocalSocketAddress()).getAddress()).toString().replace("/", "");
        String remoteIP = (((InetSocketAddress) clientSocket.getRemoteSocketAddress()).getAddress()).toString().replace("/", "");
		int localPort = clientSocket.getLocalPort();
	
        try {
            while (true) {
                String inputLine = inputReader.readLine();
                if (inputLine == null) {
                    break;
                }

                String[] tokens = inputLine.split("\\|");
                switch (tokens[0]) {
                    case "tx":
                        if (blockchain.addTransaction(inputLine))
                            outWriter.print("Accepted\n\n");
                        else
                            outWriter.print("Rejected\n\n");
                        outWriter.flush();
                        break;
                    case "pb":
                        outWriter.print(blockchain.toString() + "\n");
                        outWriter.flush();
                        break;
                    case "cc":
                        return;
					case "cu":
						try{
							//inStream = new ObjectInputStream(clientSocket.getInputStream()); 
							if(outStream == null)
								outStream = new ObjectOutputStream(clientSocket.getOutputStream());
							}
						catch(IOException e){
							System.out.println("didnt manage to set up stream in cu");
							break; 
						}
						if(tokens.length == 1){
							outStream.writeObject(blockchain.getHead()); 
							outStream.flush();  
							if(blockchain.getHead() == null){
								System.out.println("sending out block " + null);
							}
							else{
								System.out.println("sending out block " + blockchain.getHead().toString());
							}
							
						}
						else{
							Block block = blockchain.getHead(); 
							System.out.println("finding block");
							while(true){ // searching through blockchain
								String blockHashString = Base64.getEncoder().encodeToString(block.calculateHash());
								if(blockHashString.equals(tokens[1]) ){
									System.out.println("found block "+ block.toString());
									outStream.writeObject(block); 
									outStream.flush(); 
									break; 
								}
								System.out.println("keep searching block");
								if(block.getPreviousBlock() == null){
									System.out.println("could not find block");
									break; 
								}
								block = block.getPreviousBlock(); 
							}
							
						}
						//outStream.close();  
						//outWriter = new PrintWriter(clientOutputStream, true);
						break; 
					case "lb":
						//synchronized(blockchain){
							
							System.out.println("received lb trying to synch");
							int  LbSenderPort = Integer.valueOf(tokens[1]); 
							int nChainLength = Integer.valueOf(tokens[2]); 
							String LbHash = tokens[3]; 
							String OurbHash = null; 


							
							if(blockchain.getHead() != null){
								OurbHash = Base64.getEncoder().encodeToString(blockchain.getHead().calculateHash());
							}
							

							if(blockchain.getLength() >= nChainLength && LbHash.equals(OurbHash))
								return; 

							
							
							try {
								// create socket with a timeout of 2 seconds
								System.out.println("Starting catch up from lb");
								//System.out.println("Runs SendHeartBeatThread"); 
								// Socket toServer = null; 
								// ObjectInputStream inStreamToServer = null; 
								// PrintWriter printWriter = null; 
								System.out.println("Starting catch up from lb 2222 blockchain.getLength() " + blockchain.getLength()+ " nChainLength " + nChainLength);

								if(blockchain.getLength() == nChainLength){
									System.out.println("Chain is equal");
									Socket toServer = new Socket();
									toServer.connect(new InetSocketAddress(remoteIP, LbSenderPort), 2000);

									PrintWriter printWriter = new PrintWriter(toServer.getOutputStream());
									printWriter.println("cu" + "\n");
									printWriter.flush();
									ObjectInputStream inStreamToServer = new ObjectInputStream(toServer.getInputStream()); 

									Block NheadBlock = null;
									try{
										NheadBlock = (Block) inStreamToServer.readObject();
										//inStreamToServer.reset(); 
									}
									catch(ClassNotFoundException ex){
										System.out.println("class not found exception");
									}
									// comapre hashes 
									if(NheadBlock == null){
										System.out.println("NheadBlock is null");
										return; 
									}
									byte[] LbHashByte = Base64.getDecoder().decode(LbHash); 
									byte[] NblockHash = NheadBlock.calculateHash(); 
									byte[] OurBlockHash = blockchain.getHead().calculateHash(); 


									if(LbHashByte.length > OurBlockHash.length){
										Block beforeHeader = blockchain.getHead().getPreviousBlock(); 
										NheadBlock.setPreviousBlock(beforeHeader); 
										NheadBlock.setPreviousHash(blockchain.getHead().getPreviousHash());
										blockchain.setHead(NheadBlock); 
										return; 
									}
									for(int i = 0; i < LbHashByte.length; i++){
										if(LbHashByte[i] > OurBlockHash[i]){
											Block beforeHeader = blockchain.getHead().getPreviousBlock(); 
											NheadBlock.setPreviousBlock(beforeHeader); 
											NheadBlock.setPreviousHash(blockchain.getHead().getPreviousHash());
											blockchain.setHead(NheadBlock); 
											return; 
										}
									}
									printWriter.println("cc"+ "\n");
									printWriter.flush();
									toServer.close(); 
									printWriter.close(); 
									inStreamToServer.close(); 

								}
								else if(blockchain.getLength() < nChainLength){

									int length = 0; 
									
									System.out.println("Chain is less");
									Socket toServer = new Socket();
									toServer.connect(new InetSocketAddress(remoteIP, LbSenderPort), 2000);

									PrintWriter printWriter = new PrintWriter(toServer.getOutputStream());
									printWriter.println("cu" + "\n");
									printWriter.flush();
									ObjectInputStream inStreamToServer = new ObjectInputStream(toServer.getInputStream()); 

									Block NheadBlock = null;
									try{
										NheadBlock = (Block) inStreamToServer.readObject();
										//inStreamToServer.reset(); 
									}
									catch(ClassNotFoundException ex){
										System.out.println("class not found exception");
									}
									// comapre hashes 
									if(NheadBlock == null){
										System.out.println("NheadBlock is null");
										return; 
									}
									printWriter.println("cc"+ "\n");
									printWriter.flush();
									toServer.close(); 
									printWriter.close(); 
									inStreamToServer.close(); 

									Block blockAhead = null;
									Block block = NheadBlock;
									length++; 

									while(true){
										try{

											//build the whole chain
											String tmp = Base64.getEncoder().encodeToString(block.getPreviousHash());
											String end = Base64.getEncoder().encodeToString(new byte[32]);

											if(block.getPreviousHash() == null || tmp.equals(end)){ // AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
												// reached end of the blockchain 
												System.out.println("reached end of the blockchain from if");
												blockchain.setHead(NheadBlock); 
												blockchain.setLength(length); 
												System.out.println(blockchain.toString());
												break; 
											}

											byte[] prevBlockHashBytes = block.getPreviousHash(); 
											String prevBlockHash = Base64.getEncoder().encodeToString(prevBlockHashBytes);


											toServer = new Socket();
											toServer.connect(new InetSocketAddress(remoteIP, LbSenderPort), 2000);
											//System.out.println("stream set up");
											printWriter = new PrintWriter(toServer.getOutputStream());
											//System.out.println("Command for searching is cu|"+prevBlockHash+ "\n");
											printWriter.println("cu|"+prevBlockHash+ "\n");
											printWriter.flush();

											inStreamToServer = new ObjectInputStream(toServer.getInputStream()); 	
											//System.out.println("stream is done");

											try{
												length++; 
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
										catch(IOException ex){
											System.out.println("didnt 2 ");
											System.out.println("reached end of the blockchain from catch");
											blockchain.setHead(NheadBlock); 
											System.out.println(blockchain.toString());
											break; 
										}

									}

								}
								System.out.println("Conditions must be wrong");

							} catch (IOException e) {
								System.out.println("Didnt set up");
							}

							
							
							
							
							
							
							

						//}
						return;  
					case "hb":
						
						System.out.println("received hb "+ tokens[1] + " heartbeat value " + tokens[2]); 
						int heartBeatSenderPort = Integer.valueOf(tokens[1]);
						//serverStatus.put(new ServerInfo(remoteIP, heartBeatSenderPort), new Date()); 
						ServerInfo serverInfo = new ServerInfo(remoteIP, heartBeatSenderPort);
						ArrayList<Thread> threadArrayList = new ArrayList<>();
						//synchronized(serverStatus){
							
							//System.out.println("contains :"+ serverStatus.containsKey(serverInfo));
						
							//if(remoteIP.equals(localIP) == false){
								serverStatus.put(serverInfo, new Date()); 
							//}
							
						//}
						if(tokens[2].equals("0")){

							//synchronized(serverStatus){
								for (ServerInfo info : serverStatus.keySet()) {
									String ip = info.getHost(); 
									
									//System.out.println("ip is " + ip + " port is " + info.getPort()); 
									//System.out.println("remoteIP " + remoteIP + " localIP " + localIP); 
									
									// if (ip.equals(remoteIP) || ip.equals(localIP)) {
									// 	//continue;
									// 	//System.out.println("equals but continue"); 
									// }
									

									Thread thread = new Thread(new HeartBeatInformRunnable(info.getHost(), info.getPort(), localPort, remoteIP, heartBeatSenderPort));
									threadArrayList.add(thread);
									thread.start();
								}
							//}
							
						}
						
							
						for (Thread thread : threadArrayList) {
							thread.join();
						}
							
						
						break; 
					case "si":
						int newPeerPort = Integer.valueOf(tokens[3]); 
						int siSenderPort = Integer.valueOf(tokens[1]); 
						String newPeerIp = tokens[2]; 
						
						System.out.println("received si Q's port is " + tokens[1] + "P IP is " + tokens[2] + "P port is " + tokens[3]);
						//System.out.println("remoteIP " + remoteIP + " localIP " + localIP + "newPeerIp" + newPeerIp); 
						
						serverInfo = new ServerInfo(newPeerIp, newPeerPort); 
						
						
						//synchronized(serverStatus){
						//System.out.println("contains :"+ serverStatus.containsKey(serverInfo));
						if(serverStatus.containsKey(serverInfo) == false){


							threadArrayList = new ArrayList<>();
							//if(newPeerIp.equals(localIP) == false){
							serverStatus.put(serverInfo, new Date());
							//}

							for (ServerInfo info : serverStatus.keySet()) {
								String ip = info.getHost(); 
								

								if(ip.equals(remoteIP) && info.getPort() == siSenderPort){
									continue; 
								}

								if (ip.equals(newPeerIp) && info.getPort() == newPeerPort) {
									continue;
								}
								System.out.println("sending si to: ip is " + info.getHost() + " port is " + info.getPort()); 
								Thread thread = new Thread(new HeartBeatInformRunnable(info.getHost(), info.getPort(), localPort, newPeerIp, newPeerPort));
								threadArrayList.add(thread);
								thread.start();
							}
							for (Thread thread : threadArrayList) {
								thread.join();
							}


						}
						//}
						
						
						
						
						break; 
                    default:
                        outWriter.print("Error\n\n");
                        outWriter.flush();
                }
            }
        } catch (IOException e) {
        } catch (InterruptedException e) {
        }
    }
}



















