package client;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;



public class SecureClient {

	private int port;
	private InetAddress host;

	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "./client/assets/CLIENTstore.ks";
	static final String TRUSTSTORE = "./client/assets/CLIENTtruststore.ks";
	static final String KEYPW = "1337x2";
	static final String TRUSTPW = "1337x2";

	public SecureClient(InetAddress _host, int _port) {
		this.host = _host;
		this.port = _port;
	}


	public void run() {
		try {
			KeyStore ks = KeyStore.getInstance("JCEKS");
			ks.load(new FileInputStream(KEYSTORE), KEYPW.toCharArray());
			
			KeyStore ts = KeyStore.getInstance("JCEKS");
			ts.load(new FileInputStream(TRUSTSTORE), TRUSTPW.toCharArray());
			
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks, KEYPW.toCharArray());
			
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			tmf.init(ts);
			
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
			SSLSocketFactory sslFact = sslContext.getSocketFactory();      	
			SSLSocket client = (SSLSocket)sslFact.createSocket(host, port);
			client.setEnabledCipherSuites(client.getSupportedCipherSuites());
			System.out.println("\n>>>> SSL/TLS handshake completed");

			
			BufferedReader socketIn;
			socketIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
			PrintWriter socketOut = new PrintWriter(client.getOutputStream(), true);
			
			menu();
			String choice = new BufferedReader(new InputStreamReader(System.in)).readLine();
			int option = Integer.parseInt(choice);
			socketOut.println(choice);

			switch (option) {
				case 1:
					System.out.println("Enter the filename to upload: ");
					try {
						String filename = new BufferedReader(new InputStreamReader(System.in)).readLine();
						String filedata = uploadToServer(filename);
						socketOut.println(filename);
						socketOut.println(filedata);
						// socketOut.println("eof");
						System.out.println(socketIn.readLine());
					}
					catch (Exception e) {
						System.out.println("Something went terrible wrong....");	
					}	
					break;
				case 2:
					System.out.println("Inte klar för fan");
					break;
				case 3:
					System.out.println("Inte klar för fan");
					break; 

				default:
					break;
			}
			
			socketOut.println(4);
		}
		catch(Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}
	}


	private String uploadToServer(String filename) {
		try {
			BufferedReader buffR = new BufferedReader(new FileReader("./client/files/" + filename));
			StringBuilder stringB = new StringBuilder();
	
			String row = buffR.readLine();
			while(row != null) {
				System.out.println(row);
				stringB.append(row);
				stringB.append(System.lineSeparator());
				row = buffR.readLine();
			}
			buffR.close();
			return stringB.toString();
		}
		catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
			return "Something went fkn wrong!!!!";
		}
		
		
	}

	//createFile()

	//deleteFile()

	public void menu() {
		System.out.println("Please choose your operation");
		System.out.println("1. Upload a file");
		System.out.println("2. Download a file");
		System.out.println("3. Delete a file");
		System.out.println("4. Exit");

	}
	public static void main(String[] args) {
		try {
			InetAddress host = InetAddress.getLocalHost();
			int port = DEFAULT_PORT;
			if (args.length > 0) {
				port = Integer.parseInt(args[0]);
			}
			if (args.length > 1) {
				host = InetAddress.getByName(args[1]);	
			}
			SecureClient addClient = new SecureClient(host, port);
			addClient.run();
		}
		catch (UnknownHostException uhx) {
			System.out.println(uhx);
			uhx.printStackTrace();
		}
	}
}