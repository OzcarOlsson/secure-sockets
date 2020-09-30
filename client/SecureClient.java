package client;

import java.io.*;
import java.net.*;
import java.security.KeyStore;
import javax.net.ssl.*;



public class SecureClient {

	private int port;
	private InetAddress host;

	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "./client/assets/CLIENTkeystore.ks";
	static final String TRUSTSTORE = "./client/assets/CLIENTtruststore.ks";
	static final String KEYPW = "1337x2";
	static final String TRUSTPW = "1337x2";

	private BufferedReader socketIn;
	private PrintWriter socketOut;

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

			
		
			socketIn = new BufferedReader(new InputStreamReader(client.getInputStream()));
			socketOut = new PrintWriter(client.getOutputStream(), true);
			
			int option = 0;
			while(option != 4) {
				menu();
				String choice = new BufferedReader(new InputStreamReader(System.in)).readLine();
				option = Integer.parseInt(choice);
				socketOut.println(choice);
					
				switch (option) {
					case 1:
						System.out.println("Enter the filename to upload: ");
						try {
							String filename = new BufferedReader(new InputStreamReader(System.in)).readLine();
							String filedata = uploadToServer(filename);
							socketOut.println(filename);
							socketOut.println(filedata);

							socketOut.println("STATUS:DONE");
							System.out.println(socketIn.readLine());
						}
						catch (Exception e) {
							System.out.println("Something went terrible wrong....");
							e.printStackTrace();	
						}	
						break;
					case 2: //download
						System.out.println("Enter the filename of which you want to download");
						try {
							String filename = new BufferedReader(new InputStreamReader(System.in)).readLine();
							socketOut.println(filename);
							String fileData = downloadFromServer(socketIn);
							createFile(filename, fileData);
						}
						catch(Exception e) {
							System.out.println("Something went terrible wrong....");	
							e.printStackTrace();
						}
						break;
					case 3: // delete
						System.out.println("Enter the name of the file you want to delete");
						try {
							String filename = new BufferedReader(new InputStreamReader(System.in)).readLine();
							socketOut.println(filename);
							System.out.println(socketIn.readLine());
						}
						catch(Exception e) {
							System.out.println("Something went terrible wrong....");	
							e.printStackTrace();
						}
						break;
					
					case 4: //exit
						System.out.println("Thank you for using our services, see you next time cowboy!");
						socketOut.println(4);
						break;

					default:
						System.out.println("Wrong input, please choose a number between 1-4");
						break;
				}
			}
			
			client.close();
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
				stringB.append(row);
				row = buffR.readLine();
				if(row != null) stringB.append(System.lineSeparator());
			}
			buffR.close();
			return stringB.toString();
		}
		catch(Exception e) {
			System.out.println(e);
			e.printStackTrace();
			return "ERROR";
		}
		
	}
	private String downloadFromServer(BufferedReader socketIn) {
		try {
			StringBuilder stringB = new StringBuilder();
			String row = socketIn.readLine();
			String data = "";
	
			while(!row.equals("STATUS:DONE")) {
				stringB.append(row);
				stringB.append(System.lineSeparator());
				row = socketIn.readLine();
			}
			data = stringB.toString();

			return data;
		}
		catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			return "Some shit went crazy";
		}
	}

	private void createFile(String _filename, String _fileData) {
		String filename = _filename;
		PrintWriter writer;
		try {
			writer = new PrintWriter("./client/files/" + filename);
			writer.print(_fileData);
			writer.close();
			System.out.println(filename + " succesfully downloaded");
		}
		catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}


	public void menu() {
		System.out.println("\nPlease choose your operation");
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