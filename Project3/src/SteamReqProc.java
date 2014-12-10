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
import java.util.ArrayList;
import java.util.Collections;

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
	
	public SteamReqProc(String discServer, Socket sock, DataStore ds) {
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
						boolean signedIn = ds.signIn(
								(String) obj.get("username"), 
								(String) obj.get("password"));
						if(!signedIn) {
							JSONObject excObj = new JSONObject();
							excObj.put("exception", 
									"Incorrect username or password");
							excObj.put("login", "failed");
							rootLogger.trace("Login failed");
							sendJSON(excObj);
						}
						else {
							JSONObject excObj = new JSONObject();
							ArrayList<String> servers = ds.getServersList();
							Collections.sort(servers);
							if(ds.getSelf().equals(ds.getMaster())) {
								for(String server: servers) {
									if(server != ds.getSelf()) {
										serverRequest(obj, server.split(":"));
									}
								}
							}
							excObj.put("login", "success");
							rootLogger.trace("Login success");
							sendJSON(excObj);
						}
					}
					//Register a new User
					else if (obj.get("request").equals("register")) {
						boolean registered = ds.register(
								(String) obj.get("username"), 
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
							ArrayList<String> servers = ds.getServersList();
							Collections.sort(servers);
							if(ds.getSelf().equals(ds.getMaster())) {
								for(String server: servers) {
									if(server != ds.getSelf()) {
										serverRequest(obj, server.split(":"));
									}
								}
							}
							JSONObject excObj = new JSONObject();
							excObj.put("register", "success");
							rootLogger.trace("Registration successful");
							sendJSON(excObj);
						}
					}
					//User Library
					else if(obj.get("request").equals("library")) {
						JSONObject excObj = new JSONObject();
						excObj.put("library", "success");
						JSONArray games = ds.getUserGames(
								(String) obj.get("username"));
						excObj.put("games", games);
						rootLogger.trace("Request for user Library complete");
						sendJSON(excObj);
					}
					//Display Store
					else if (obj.get("request").equals("store")) {
						JSONObject excObj = new JSONObject();
						excObj.put("store", "success");
						JSONArray games = ds.getStoreGames(
								(String) obj.get("username"));
						excObj.put("games", games);
						rootLogger.trace("Request for Store Games complete");
						sendJSON(excObj);
					}
					//Buy Game
					else if (obj.get("request").equals("buy")
							&& obj.containsKey("game")) {
						boolean gameAdded = ds.addGameToUser(
								(String) obj.get("username"), 
								(String) obj.get("game"));
						if(!gameAdded) {
							JSONObject excObj = new JSONObject();
							excObj.put("exception", 
									"Game could not be purchased");
							excObj.put("buy", "failed");
							rootLogger.trace("Purchase not successful");
							sendJSON(excObj);
						}
						else {
							ArrayList<String> servers = ds.getServersList();
							Collections.sort(servers);
							if(ds.getSelf().equals(ds.getMaster())) {
								for(String server: servers) {
									if(server != ds.getSelf()) {
										serverRequest(obj, server.split(":"));
									}
								}
							}
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
							excObj.put("exception", 
									"Game has not been purchased");
							excObj.put("download", "failed");
							rootLogger.trace("Download not successful");
							sendJSON(excObj);
						}
						else {
							Path path = FileSystems.getDefault().getPath(".", 
									file);
							String game = Files.readAllLines(path, 
									Charset.defaultCharset()).get(0);
							JSONObject excObj = new JSONObject();
							excObj.put("download", "success");
							excObj.put("game", game);
							excObj.put("title", file);
							rootLogger.trace("Game successfuly purchased");
							sendJSON(excObj);
						}
					}
					//Update Multiplayer Game
					else if (obj.get("request").equals("upmulti")
							&& obj.containsKey("opponent")
							&& obj.containsKey("board")) {
						ds.updateMulti((String) obj.get("username"), 
								(String) obj.get("opponent"), 
								(String) obj.get("board"));
						ds.updateMulti((String) obj.get("opponent"), 
								(String) obj.get("username"), 
								(String) obj.get("board"));
						if(ds.getSelf().equals(ds.getMaster())) {
							ArrayList<String> servers = ds.getServersList();
							Collections.sort(servers);
							for(String server: servers) {
								if(server != ds.getSelf()) {
									serverRequest(obj, server.split(":"));
								}
							}
						}
						JSONObject excObj = new JSONObject();
						excObj.put("upmulti", "success");
						rootLogger.trace("Game successfuly updated");
						sendJSON(excObj);
					}
					//Get Multiplayer Game Update
					else if (obj.get("request").equals("multiup")
							&& obj.containsKey("opponent")) {
						JSONObject excObj = ds.multiUpdate(
								(String) obj.get("username"), 
								(String) obj.get("opponent"));
						if(ds.getSelf().equals(ds.getMaster())) {
							ArrayList<String> servers = ds.getServersList();
							Collections.sort(servers);
							for(String server: servers) {
								if(server != ds.getSelf()) {
									serverRequest(obj, server.split(":"));
								}
							}
						}
						excObj.put("multiup", "success");
						rootLogger.trace("Get Multiplayer Update Finished");
						rootLogger.trace("Board: " + excObj.get("board"));
						sendJSON(excObj);
					}
					//FindOpponent
					else if (obj.get("request").equals("findOpponent")
							&& obj.containsKey("username")) {
						JSONObject excObj = new JSONObject();
						String opponent = ds.multiPlayerCheck(
								(String) obj.get("username"));
						if(!opponent.equals("")) {
							excObj.put("opponent", opponent);
						}
						if(ds.getSelf().equals(ds.getMaster())) {
							ArrayList<String> servers = ds.getServersList();
							Collections.sort(servers);
							for(String server: servers) {
								if(server != ds.getSelf()) {
									serverRequest(obj, server.split(":"));
								}
							}
						}
						
						excObj.put("findOpponent", "success");
						rootLogger.trace("Opponent found: " 
								+ excObj.get("opponent"));
						sendJSON(excObj);
						
					}
					//Logout
					else if (obj.get("request").equals("logout")) {
						boolean signedOut = ds.signOut(
								(String) obj.get("username"), 
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
							ArrayList<String> servers = ds.getServersList();
							Collections.sort(servers);
							if(ds.getSelf().equals(ds.getMaster())) {
								for(String server: servers) {
									if(server != ds.getSelf()) {
										serverRequest(obj, server.split(":"));
									}
								}
							}
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
				//Announced
				else if (obj.containsKey("request") 
						&& obj.get("request").equals("announce")
						&& obj.containsKey("newserver")) {
					ds.addServer((String) obj.get("newserver"));
					
					JSONObject excObj = new JSONObject();
					excObj.put("announce", "success");
					rootLogger.trace("New Server Added from Announcement");
					sendJSON(excObj);
					
					ArrayList<String> servers = ds.getServersList();
					Collections.sort(servers);
					if(ds.getSelf().equals(ds.getMaster())) {
						JSONObject snapshot = ds.getSnapshot();
						snapshot.put("request", "snapshot");
						serverRequest(snapshot, 
								((String) obj.get("newserver")).split(":"));
						for(String server: servers) {
							if(!server.equals(ds.getSelf()) 
									&& !server.equals(
											(String) obj.get("newserver"))) {
								serverRequest(obj, server.split(":"));
							}
						}
					}
				}
				//Snapshot
				else if (obj.containsKey("request") 
						&& obj.get("request").equals("snapshot")) {
					ds.setSnapshot(obj);
					JSONObject excObj = new JSONObject();
					excObj.put("snapshot", "success");
					rootLogger.trace("Snapshot placed");
					sendJSON(excObj);
				}
				//Alive Check
				else if (obj.containsKey("request") 
						&& obj.get("request").equals("alive")) {
					JSONObject excObj = new JSONObject();
					excObj.put("alive", "success");
					rootLogger.trace("Replied that it's alive");
					sendJSON(excObj);
				}
				//Update Master
				else if (obj.containsKey("request") 
						&& obj.get("request").equals("master")
						&& obj.containsKey("master")) {
					ds.setMaster((String) obj.get("master"));
					ArrayList<String> servers = ds.getServersList();
					Collections.sort(servers);
					if(ds.getSelf().equals(ds.getMaster())) {
						obj = ds.getSnapshot();
						obj.put("request", "snapshot");
						for(String server: servers) {
							if(!server.equals(ds.getSelf())) {
								if(serverRequest(obj, server.split(":")) == null) {
									ds.removeServer(server);
								}
							}
						}
					}
					
					JSONObject excObj = new JSONObject();
					excObj.put("master", "success");
					rootLogger.trace("Master Updated to:" 
							+ (String) obj.get("master"));
					sendJSON(excObj);
				}
				//Election
				else if (obj.containsKey("request") 
						&& obj.get("request").equals("elect")) {
					obj.clear();
					obj.put("request", "alive");
					obj = serverRequest(obj, ds.getMaster().split(":"));
					
					JSONObject excObj = new JSONObject();
					if(obj == null) {
						ds.removeServer(ds.getMaster());
						bully();
						excObj.put("elect", "success");
					}
					else {
						excObj.put("elect", "failed");
					}
					excObj.put("master", ds.getMaster());
					
					sendJSON(excObj);
				}
				//No Request
				else {
					JSONObject excObj = new JSONObject();
					excObj.put("exception", "No recognized request given");
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
	
	private boolean sendJSON(JSONObject obj) {
		try {
			String requestbody = obj.toJSONString();
			String requestheaders = "Content-Length: "
					+ requestbody.getBytes().length + "\n";
			OutputStream out = sock.getOutputStream();
			out.write(requestheaders.getBytes());
			out.write(requestbody.getBytes());
						
			out.flush();
			out.close();
			return true;

		} catch (UnknownHostException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
	}
	
	private JSONObject serverRequest(JSONObject request, 
			String[] steamServer) {
		String lineText = "";
		Socket steamSock;
		
		try {
			steamSock = new Socket(steamServer[0], 
					Integer.parseInt(steamServer[1]));

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
			rootLogger.trace("Received line: " + line);
			int bufferSize = Integer.parseInt(line.split(" ")[1]);
			rootLogger.trace("Buffer Size: " + bufferSize);
			char[] bytes = new char[bufferSize];
			in.read(bytes, 0, bufferSize);
			lineText = new String(bytes);
			rootLogger.trace("LineText: " + lineText);
						
			out.flush();
			out.close();
			in.close();
			steamSock.close();

		} catch (UnknownHostException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
		
		try {
			return (JSONObject) parser.parse(lineText);
		} catch (ParseException e) {
			e.printStackTrace();
			rootLogger.trace("ParseException: " + e);
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private void bully() {
		ArrayList<String> servers = ds.getServersList();
		Collections.sort(servers);
		String master = "";
		
		JSONObject obj = new JSONObject();
		obj.put("request", "alive");
		
		for(String server: servers) {
			if(serverRequest(obj, server.split(":")) != null) {
				master = server;
				break;
			}
			else {
				ds.removeServer(server);
			}
		}
		
		obj.clear();
		obj.put("request", "master");
		obj.put("master", master);
		
		for(String server: servers) {
			serverRequest(obj, server.split(":"));
		}		
	}
}
