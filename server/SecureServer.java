package server;

import java.io.*;
import javax.net.ssl.*;
import java.security.*;

public class SecureServer {

	private int port;

	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "./server/assets/SERVERkeystore.ks";
	static final String TRUSTSTORE = "./server/assets/SERVERtruststore.ks";
	static final String KEYPW = "1337x2";
	static final String TRUSTPW = "1337x2";

	private BufferedReader in;
	private PrintWriter out;

	SecureServer(int _port) {
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
			SSLServerSocketFactory sslServerFactory = sslContext.getServerSocketFactory();
			SSLServerSocket socket = (SSLServerSocket) sslServerFactory.createServerSocket(port);
			socket.setEnabledCipherSuites(socket.getSupportedCipherSuites());
			
			System.out.println("\n>>>> Secure Server: active ");
			SSLSocket incoming = (SSLSocket)socket.accept();

			in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
			out = new PrintWriter(incoming.getOutputStream(), true);

			int choice;
			while ( (choice = Integer.parseInt(in.readLine())) != 4) {
				
				String fileName;

				switch (choice) {
					case 1:
						fileName = in.readLine();
						upload(fileName);
						break;
					case 2:
						fileName = in.readLine();
						download(fileName);
						out.println("STATUS:DONE");
						break;
					case 3:
						fileName = in.readLine();
						delete(fileName);
						break;
				
					default:
						System.out.println("Invalid choice");
						out.println("Invalid choice");
						break;
				}
			}
			incoming.close();
		}
		catch(Exception x) {
			System.out.println(x);
			x.printStackTrace();
		}
	}

	private void upload(String fileName) {
		System.out.println("UPLOAD FUNCTION");
		try {
			StringBuilder content = new StringBuilder();
			String s = in.readLine();
			while(!s.equals("STATUS:DONE")) {
				content.append(s);
				s = in.readLine();
				if(!s.equals("STATUS:DONE")) content.append("\n");
			}

			String file = content.toString();
			try {
				BufferedWriter wr = new BufferedWriter(new FileWriter("./server/files/" + fileName));
				wr.write(file, 0, file.length());
				wr.close();
				out.println(" - Upload success -");
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void download(String fileName) {
		try {
			StringBuilder sb = new StringBuilder();
			BufferedReader br = new BufferedReader(new FileReader("./server/files/" + fileName));
			String line = br.readLine();
			while(line != null) {
				sb.append(line);
				line = br.readLine();
				if(line != null) sb.append(System.lineSeparator());
			}
			br.close();
			out.println(sb.toString());
			System.out.println(" - Download success - ");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void delete(String fileName) {
		try {
			File f = new File("./server/files/" + fileName);
			if(f.delete()) out.println(fileName + " deleted from server.");
			else out.println("File could not be deleted.");
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		int port = DEFAULT_PORT;
		SecureServer addServe = new SecureServer( port );
		addServe.run();

	}

}