package web;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLServerSocketFactory;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class ServerProxy implements Runnable{
	
	private ServerSocket SERVER;
//	static protected LocalCache Cache;
	static protected LocalCache Cache;
	
	public ServerProxy(ServerSocket server){
		SERVER = server;
	}
	
	public int GetPort(String line){
		int port = 0;
		String[] words = line.split("\\s+|:"); //regular expression
		for(String word: words) {
			if(word.matches("\\d+")){
				port = Integer.parseInt(word); //String to int
				break;
			}
		}
		return port;
	}
	
	public void run(){
		Cache = new LocalCache();
		ExecutorService excutor = Executors.newFixedThreadPool(50);
		while(true){
			try {
				System.out.println("Accept");
				Socket socket_server = SERVER.accept();
				HandleClient handle_client = new HandleClient(socket_server);
				excutor.execute(handle_client);
			}
			catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String args[]){
		//add the support to SSL(HTTPS)
	//	SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		try {
	//		ServerSocket server_socket = ssf.createServerSocket(7777);
			ServerSocket server_socket = new ServerSocket(7777);
			ServerProxy Server = new ServerProxy(server_socket);
			Server.run();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}