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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


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
		annouceSelf(self, discServer, ds);
		
		while (true) {
			sock = new Socket();
			sock = serversock.accept();
			sock.setSoTimeout(10000);
			executor.execute(new SteamReqProc(discServer, sock, ds));
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
	
	@SuppressWarnings("unchecked")
	public static void annouceSelf(String self, String discServer, DataStore ds) {
		String[] discParts = discServer.split(":");
		Socket steamSock;
		String lineText = "";
		JSONObject request = new JSONObject();
		JSONParser parser = new JSONParser();
		request.put("request", "announce");
		request.put("self", self);
		
		try {
			steamSock = new Socket(discParts[0], Integer.parseInt(discParts[1]));

			String requestbody = request.toJSONString();
			String requestheaders = "Content-Length: "
					+ requestbody.getBytes().length + "\n";
			OutputStream out = steamSock.getOutputStream();
			out.write(requestheaders.getBytes());
			out.write(requestbody.getBytes());

			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(
					steamSock.getInputStream()));

			line = in.readLine();
			int bufferSize = Integer.parseInt(line.split(" ")[1]);
			char[] bytes = new char[bufferSize];
			in.read(bytes, 0, bufferSize);
			lineText = new String(bytes);
						
			out.flush();
			out.close();
			in.close();
			steamSock.close();
			
			JSONObject obj = (JSONObject) parser.parse(lineText);
			ds.setMaster((String) obj.get("master"));

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
