package server;

import java.io.*;
import java.net.*;
import javax.net.ssl.*;
import java.security.*;

public class SecureServer {

	private int port;

	static final int DEFAULT_PORT = 8189;
	static final String KEYSTORE = "./server/assets/JOCKEkeystore.ks";
	static final String TRUSTSTORE = "./server/assets/JOCKEtruststore.ks";
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
			
			System.out.println("\n>>>> Jockes säkra SÄRVER: active ");
			SSLSocket incoming = (SSLSocket)socket.accept();

			in = new BufferedReader(new InputStreamReader(incoming.getInputStream()));
			out = new PrintWriter(incoming.getOutputStream(), true);

			int choice;
			while ( (choice = Integer.parseInt(in.readLine())) != 4) {
				
				String fileName;

				switch (choice) {
					case 2:
						fileName = in.readLine();
						download(fileName);
						break;
					case 3:
						System.out.println("Server: Delete");
						out.println("Sent from server: Delete");
						break;
					case 1:
						System.out.println("UPLOAD");
						fileName = in.readLine();
						System.out.println(fileName);
						upload(fileName);
						break;
				
					default:
						System.out.println("Server: Neither");
						out.println("Sent from server: Neither");
						break;
				}
				System.out.println("LOPP");
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
			while(s != null) {
				System.out.println("LINE : " + s);
				content.append(s + "\n");
				s = in.readLine();
			}

			String file = content.toString();
			System.out.println("FILE: " + file);
			try {
				BufferedWriter wr = new BufferedWriter(new FileWriter(fileName));
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
			String s;
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			while((s = br.readLine()) != null) {
				out.println(s);
			}
			br.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void deleteFile() {

	}
	
	public static void main(String[] args) {
		int port = DEFAULT_PORT;
		SecureServer addServe = new SecureServer( port );
		addServe.run();

	}

}