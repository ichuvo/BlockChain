

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ConcurrentHashMap;
import java.util.*; 
import java.io.*;
import java.net.ServerSocket;

public class CatchUpRunnable implements Runnable{


    private String remoteIP;
	private int LbSenderPort;
	private String LbHash;
	private int nChainLength; 
	private Blockchain blockchain;
	
    public CatchUpRunnable(String remoteIP, int LbSenderPort, String LbHash, int nChainLength, Blockchain blockchain) {
		
       
		this.remoteIP = remoteIP;
		this.LbSenderPort = LbSenderPort; 
		this.LbHash = LbHash; 
		this.nChainLength = nChainLength; 
		this.blockchain = blockchain; 
		
    }
  
			
			
		

		
			
    @Override
    public void run() {
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
				
				while(true){
				try{
					
						//build the whole chain
						String tmp = Base64.getEncoder().encodeToString(block.getPreviousHash());
						String end = Base64.getEncoder().encodeToString(new byte[32]);
						
							if(block.getPreviousHash() == null || tmp.equals(end)){ // AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA
								// reached end of the blockchain 
								System.out.println("reached end of the blockchain from if");
								blockchain.setHead(NheadBlock); 
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
		System.out.println("End of run");
    }
}




// 				Socket toServer = new Socket();
//             	toServer.connect(new InetSocketAddress(remoteIP, LbSenderPort), 2000);
				
// 				PrintWriter printWriter = new PrintWriter(toServer.getOutputStream());
// 				printWriter.println("cu" + "\n");
// 				printWriter.flush();
// 				ObjectInputStream inStreamToServer = new ObjectInputStream(toServer.getInputStream()); 
				
// 				Block NheadBlock = null;
// 				try{
// 					NheadBlock = (Block) inStreamToServer.readObject();
// 					//inStreamToServer.reset(); 
// 				}
// 				catch(ClassNotFoundException ex){
// 					System.out.println("class not found exception");
// 				}
// 				// comapre hashes 
// 				if(NheadBlock == null){
// 					System.out.println("NheadBlock is null");
// 					return; 
// 				}
// 				printWriter.println("cc"+ "\n");
// 				printWriter.flush();
// 				toServer.close(); 
// 				printWriter.close(); 
// 				inStreamToServer.close(); 
				
				
				
// 				Block blockAhead = null;
// 				Block block = NheadBlock;
				
// 				while(true){
// 					String hashBefore = Base64.getEncoder().encodeToString(block.getPreviousHash()); 
// 					Block ourBlock = CatchUpRunnable.searchBlock(blockchain, hashBefore, blockchain.getHead()); 
					
// 					toServer = new Socket();
//             		toServer.connect(new InetSocketAddress(remoteIP, LbSenderPort), 2000);
				
// 					printWriter = new PrintWriter(toServer.getOutputStream());
// 					printWriter.println("cu|"+hashBefore  + "\n");
// 					printWriter.flush();
// 					inStreamToServer = new ObjectInputStream(toServer.getInputStream()); 
// 					try{
// 						blockAhead = block; 
						
// 						block = (Block) inStreamToServer.readObject(); 

// 						blockAhead.setPreviousBlock(block); 
// 						blockAhead.setPreviousHash(block.calculateHash()); 
// 					}
// 					catch(ClassNotFoundException ex){
// 						System.out.println("class not found exception");
// 					}
					
// 					if(ourBlock != null){
// 						String tmpHash = Base64.getEncoder().encodeToString(ourBlock.calculateHash());
// 						if(tmpHash.equals()  ){
							
// 						}
// 					}
					
					
// 				}


















