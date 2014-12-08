import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class SteamServer {

	@SuppressWarnings("resource")
	public static void main (String[] args) throws Exception {
		
		if(args.length < 2) {
			System.out.println("Usage: java SteamServer PORT configFile");
			System.exit(0);
		}
		
		int PORT = Integer.parseInt(args[0]);
		System.out.println("Starting Steam Server");
		String[] fullIP = InetAddress.getLocalHost().toString().split("/");
		String IP = fullIP[fullIP.length - 1];
		String self = IP + ":" + args[0];
		System.out.println("IP: " + IP);
		System.out.println("PORT: " + PORT + "\n");
				
		DataStore ds = new DataStore(self);
		
		ExecutorService executor = Executors.newFixedThreadPool(10);
		ServerSocket serversock = new ServerSocket(PORT);
		Socket sock;
		String discServer = readConfig(args[1], ds);
		//annouceSelf(self, discServer);
		
		while (true) {
			sock = new Socket();
			sock = serversock.accept();
			sock.setSoTimeout(10000);
			executor.execute(new SteamReqProc(sock, ds));
		}
	}
		
	private static String readConfig(String file, DataStore ds) {
		String server = "";
		String gameTitle = "";
		String filePath = "";
		Path path = FileSystems.getDefault().getPath(".", file);
		try {
			List<String> lines = Files.readAllLines(path, 
					Charset.defaultCharset());
			
			server = lines.get(0);
			for(int i = 1; i < lines.size(); i+=2) {
				gameTitle = lines.get(i);
				filePath = lines.get(i+1);
				System.out.println("Adding:" + gameTitle + " " + filePath);
				ds.addGame(gameTitle, filePath);
			}
		} catch (IOException e) {
			System.out.println("Could not read file: " + file);
		}
		return server;
	}
	
	/*
	public static void annouceSelf(String self, String discServer) {
		String[] discParts = discServer.split(":");
		String lineText = "";
		Socket discSock;
		try {
			//Send request to DiscServer
			String IP = discParts[0];
			int PORT = Integer.parseInt(discParts[1]);
			discSock = new Socket(IP, PORT);
			String requestheaders = "POST /steam?me=" + self + " HTTP/1.1\n";

			OutputStream out = discSock.getOutputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(
					discSock.getInputStream()));
			String line = "";
			
			while(!lineText.equalsIgnoreCase("HTTP/1.1 200 OK")) {
				out.write(requestheaders.getBytes());

				while (!(line = in.readLine().trim()).equals("")) {
						lineText = line;
				}

			}
			out.flush();
			out.close();
			in.close();
			discSock.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	*/
}
