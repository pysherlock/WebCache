package web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class HandleClient implements Runnable{

	private Socket Socket_Server;
	private InputStream from_client, from_server;
	private OutputStream to_client, to_server;
	private URL URL;
	private byte buffer[] = new byte[2048];
	private String Host, User_Agent;
	private boolean Get = false, Post = false, CONNECT = false;
	private String url = null;


	public HandleClient(Socket socket) {
		Socket_Server = socket;
	}
	
	public void Receive_Cache() throws IOException{
    //    System.out.println("Pipe Receive");
        ArrayList<byte[]> Page = new ArrayList<byte[]>();
        byte[] context = null;
        int len = 0;
        while ((len = from_server.read(buffer)) > 0) {  
            if (len > 0) {
            	context =  new byte[len];
            	for(int i = 0; i < len; i++)
            		context[i] = buffer[i];
            	Page.add(context);
       //         System.out.println(new String(buffer, 0, len));
                to_client.write(buffer, 0, len);
                to_client.flush();
            }
        }
        
        System.out.println("Cache the Page");
        ServerProxy.Cache.Put(url, Page);
        ServerProxy.Cache.ReFresh();
	}
	
	public void run() {
		try {
			int PORT = 80;
			
			to_client = Socket_Server.getOutputStream();
			from_client = Socket_Server.getInputStream();
			
			//Receive the package from local host
			int len = 0;
			len = from_client.read(buffer);
			
			//Get URL, HOST, PORT 
			String[] Lines = new String(ByteToString(buffer)).split("\n");
			for(String line: Lines) {
				if(line.contains("Host")) {
					Host = new String(GetHost(line));
				}
				if(line.contains("User-Agent")) {
					this.User_Agent = line.substring(12);
				}
				if(line.contains("GET")) {
					url = line.split(" ")[1];
					url = url.replaceAll("http", "https");
					if(line.contains("GET"))
						this.Get = true;
				}
				else if(line.contains("CONNECT")) { // https CONNECT?
					PORT = 443;
					url = new String("https://"+line.split("\\s+|:")[1]);
					CONNECT = true;
					Socket_Server.close();
					return;
				}
			}
			/*
			if(this.CONNECT){
				Socket remoteSocket = new Socket(Host,443);
				String ConnectResponse = "HTTP/1.0 200 Connection established\n" +
	                                          "Proxy-agent: ProxyServer/1.0\n" +
	                                          "\r\n";
				to_client.write(ConnectResponse.getBytes());
				to_client.flush();
				to_server = remoteSocket.getOutputStream();
				from_server = remoteSocket.getInputStream();
				
				while(from_client.read(buffer) > 0) {
					System.out.println(new String(buffer));
					to_server.write(buffer);
					to_server.flush();
				}
				
				while(from_server.read(buffer) > 0) {
					System.out.println(new String(buffer));
					to_client.write(buffer);
					to_client.flush();
				}
				
				remoteSocket.close();
			}*/
			
			if(this.Get) { //only handle Get request
			//	System.out.println(url);
				URL = new URL(url);
				//Check local cache
				if(ServerProxy.Cache.containsKey(url)){
					//TODO Find the http in localcache and send the information to client
					System.out.println("-----------Find it in LocalCache--------");
					ArrayList<byte[]> Page = new ArrayList<byte[]>();
					Page = ServerProxy.Cache.Get(url);
					for(byte[] context: Page) {
						System.out.println(new String(context));
						to_client.write(context, 0, context.length);
					}
					System.out.println("-----------Find it in LocalCache--------");
				}
				else {
					try {
						Thread.currentThread().sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					HttpURLConnection con = (HttpURLConnection)URL.openConnection();
					if(this.Get) {
						con.setRequestMethod("GET");
						con.setRequestProperty("User-Agent", User_Agent);
						System.out.println("\nSending 'GET' request to URL : " + url);
					}
					int responseCode = con.getResponseCode();
					System.out.println("Response Code : " + responseCode);
					from_server = con.getInputStream();
					        
					//Receive the package from website and send it back to client, Cache it
					Receive_Cache();
				}
			}
			Socket_Server.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public String GetHost(String line){
		String host = null;
		if (line.contains("Host")) {
			String[] words = line.split("\\s+");
			host = words[1];
		}
		return host;
	}
	
	public String ByteToString(byte[] buffer) throws IOException {
	    StringBuffer sb = new StringBuffer(buffer.length);  
	    for (int i = 0; i < buffer.length; i++) {
	    	if (buffer[i] == 0)
	    		break;
	        sb.append((char)buffer[i]);  
	    }
	//    System.out.print(sb.toString());
	    return sb.toString();  
	}
}
