import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class SteamReqProc implements Runnable {

	private static final Logger rootLogger = LogManager.getRootLogger();

	private Socket sock;
	private DataStore ds;
	private JSONParser parser;
	
	public SteamReqProc(Socket sock, DataStore ds) {
		this.sock = sock;
		this.ds = ds;
		parser = new JSONParser();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		BufferedReader in = null;
		String line;
		try {
			in = new BufferedReader(
					new InputStreamReader(sock.getInputStream()));
			
			String jsonText = "";
			
			line = in.readLine().trim();
			rootLogger.trace("Received line: " + line);
			
			int bufferSize = Integer.parseInt(line.split(" ")[1]);
			rootLogger.trace("Buffer Size: " + bufferSize);
			char[] bytes = new char[bufferSize];
			in.read(bytes, 0, bufferSize);
			jsonText = new String(bytes);
			
			rootLogger.trace("JsonText: " + jsonText);
			
			JSONObject obj = null;
			try {
				obj = (JSONObject) parser.parse(jsonText);
				if(obj.containsKey("request")
						&& obj.containsKey("username") 
						&& obj.containsKey("password")) {
					//Login
					if(obj.get("request").equals("login")) {
						boolean signedIn = ds.signIn((String) obj.get("username"), 
								(String) obj.get("password"),  
								sock.getInetAddress().toString() 
								+ ":" + Integer.toString(sock.getPort()));
						if(!signedIn) {
							JSONObject excObj = new JSONObject();
							excObj.put("exception", "Incorrect username or password");
							excObj.put("login", "failed");
							rootLogger.trace("Login failed");
							sendJSON(excObj);
						}
						else {
							JSONObject excObj = new JSONObject();
							excObj.put("login", "success");
							rootLogger.trace("Login success");
							sendJSON(excObj);
						}
					}
					//Register a new User
					else if (obj.get("request").equals("register")) {
						boolean registered = ds.register((String) obj.get("username"), 
								(String) obj.get("password"),  
								sock.getInetAddress().toString() 
								+ ":" + Integer.toString(sock.getPort()));
						if(!registered) {
							JSONObject excObj = new JSONObject();
							excObj.put("exception", "Username taken");
							excObj.put("register", "failed");
							rootLogger.trace("Registration not successful");
							sendJSON(excObj);
						}
						else {
							JSONObject excObj = new JSONObject();
							excObj.put("register", "success");
							rootLogger.trace("Registration successful");
							sendJSON(excObj);
						}
					}
					//User Library
					else if(obj.get("request").equals("library")) {
						JSONObject excObj = new JSONObject();
						excObj.put("store", "success");
						JSONArray games = (JSONArray) ds.getUserGames((String) obj.get("username"));
						excObj.put("games", games);
						rootLogger.trace("Request for user Library complete");
						sendJSON(excObj);
					}
					//Display Store
					else if (obj.get("request").equals("store")) {
						JSONObject excObj = new JSONObject();
						excObj.put("store", "success");
						JSONArray games = (JSONArray) ds.getStoreGames((String) obj.get("username"));
						excObj.put("games", games);
						rootLogger.trace("Request for Store Games complete");
						sendJSON(excObj);
					}
					//Buy Game
					else if (obj.get("request").equals("buy")
							&& obj.containsKey("game")) {
						boolean gameAdded = ds.addGameToUser((String) obj.get("username"), 
								(String) obj.get("game"));
						if(!gameAdded) {
							JSONObject excObj = new JSONObject();
							excObj.put("exception", "Game could not be purchased");
							excObj.put("buy", "failed");
							rootLogger.trace("Purchase not successful");
							sendJSON(excObj);
						}
						else {
							JSONObject excObj = new JSONObject();
							excObj.put("buy", "success");
							rootLogger.trace("Game successfuly purchased");
							sendJSON(excObj);
						}
					}
					//Download Game
					else if (obj.get("request").equals("download")
							&& obj.containsKey("game")) {
						String file = ds.getGameFile((String) obj.get("game"));
						if(file == null) {
							JSONObject excObj = new JSONObject();
							excObj.put("exception", "Game has not been purchased");
							excObj.put("download", "failed");
							rootLogger.trace("Download not successful");
							sendJSON(excObj);
						}
						else {
							Path path = FileSystems.getDefault().getPath(".", file);
							String game = Files.readAllLines(path, Charset.defaultCharset()).get(0);
							JSONObject excObj = new JSONObject();
							excObj.put("buy", "success");
							excObj.put("game", game);
							rootLogger.trace("Game successfuly purchased");
							sendJSON(excObj);
						}
					}
					//Logout
					else if (obj.get("request").equals("logout")) {
						boolean signedOut = ds.signOut((String) obj.get("username"), 
								(String) obj.get("password"),  
								sock.getInetAddress().toString() 
								+ ":" + Integer.toString(sock.getPort()));
						if(!signedOut) {
							JSONObject excObj = new JSONObject();
							excObj.put("exception", "Username taken");
							excObj.put("logout", "failed");
							rootLogger.trace("Could not sign out successful");
							sendJSON(excObj);
						}
						else {
							JSONObject excObj = new JSONObject();
							excObj.put("logout", "success");
							rootLogger.trace("Signed out successfully");
							sendJSON(excObj);
						}
					}
					else {
						JSONObject excObj = new JSONObject();
						excObj.put("exception", "No recognized request given");
						rootLogger.trace("IOException occurred");
						sendJSON(excObj);
					}
				}
				else {
					JSONObject excObj = new JSONObject();
					excObj.put("exception", "No request given");
					rootLogger.trace("IOException occurred");
					sendJSON(excObj);
				}
				
			} catch (ParseException e) {
				rootLogger.trace("position: " + e.getPosition());
				rootLogger.trace(e);
			}

		} catch (IOException e) {
			JSONObject excObj = new JSONObject();
			excObj.put("e", "Exception occurred");
			rootLogger.trace("IOException occurred");
			sendJSON(excObj);
		}
		
		try {
			in.close();
			sock.close();
		} catch (IOException e) {
			rootLogger.trace("Problem closing socket");
		}
		
		rootLogger.trace("Finished Working\n");	
		
	}
	
	public void sendJSON(JSONObject obj) {
		try {
			String requestbody = obj.toJSONString();
			String requestheaders = "Content-Length: "
					+ requestbody.getBytes().length + "\n";
			OutputStream out = sock.getOutputStream();
			out.write(requestheaders.getBytes());
			out.write(requestbody.getBytes());
						
			out.flush();
			out.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
