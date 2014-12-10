import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class DiscReqProc implements Runnable {
	
	private static final Logger rootLogger = LogManager.getRootLogger();

	private Socket sock;
	private DataStore ds;
	private JSONParser parser;
	
	public DiscReqProc(Socket sock, DataStore ds) {
		this.sock = sock;
		this.ds = ds;
		parser = new JSONParser();
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
	
	public JSONObject serverRequest(JSONObject request, String server) {
		String lineText = "";
		Socket steamSock;
		String[] steamServer = server.split(":");
		
		try {
			steamSock = new Socket(steamServer[0], Integer.parseInt(steamServer[1]));

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
			//e.printStackTrace();
			rootLogger.trace("UnknownHostException: " + e);
		} catch (IOException e) {
			//e.printStackTrace();
			rootLogger.trace("IOException: " + e);
		}
		
		try {
			return (JSONObject) parser.parse(lineText);
		} catch (ParseException e) {
			//e.printStackTrace();
			rootLogger.trace("ParseException: " + e);
			return null;
		}
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
				if(obj.containsKey("request")) {
					//Get Master
					if(obj.get("request").equals("getmaster")) {
						if(obj.containsKey("master") 
								&& obj.get("master") != null) {
							obj.clear();
							obj.put("request", "alive");
							obj = serverRequest(obj, ds.getMaster());
							
							if(obj == null) {
								ds.removeServer(ds.getMaster());
								obj = new JSONObject();
								obj.put("request", "elect");
								ArrayList<String> servers = ds.getServersList();
								Collections.sort(servers);
								JSONObject obj2;
								for(String server: servers) {
									obj2 = serverRequest(obj, server);
									if(obj2 != null && obj2.containsKey("master")) {
										ds.setMaster((String) obj2.get("master"));
										break;
									}
									else {
										ds.removeServer(server);
									}
								}
							}
						}
						
						String[] masterServer = ds.getMaster().split(":");
						obj.clear();
						obj.put("getmaster", "success");
						obj.put("ip", masterServer[0]);
						obj.put("port", masterServer[1]);
						rootLogger.trace("Getmaster: " + masterServer[0] + ":"
								+ masterServer[1]);
						sendJSON(obj);
					}
					//Announce
					else if(obj.get("request").equals("announce") 
							&& obj.containsKey("self")) {
						String self = (String) obj.get("self");
						ds.addServer(self);
						if(ds.getMaster().equals("")) {
							ds.setMaster(self);
						}
						
						if(!self.equals(ds.getMaster())) {
							obj.clear();
							obj.put("request", "announce");
							obj.put("newserver", self);
							while(serverRequest(obj, ds.getMaster()) == null) {
								ds.removeServer(ds.getMaster());
								JSONObject obj2 = new JSONObject();
								obj2.put("request", "elect");
								ArrayList<String> servers = ds.getServersList();
								Collections.sort(servers);
								JSONObject obj3;
								for(String server: servers) {
									if(!server.equals(self)) {
										obj3 = serverRequest(obj2, server);
										if(obj3 != null && obj3.containsKey("master")) {
											ds.setMaster((String) obj3.get("master"));
											break;
										}
									}
								}
							}
						}
						
						obj.clear();
						obj.put("announce", "success");
						String master = ds.getMaster();
						obj.put("master", master);
						sendJSON(obj);
					}
					//Update Master
					else if(obj.get("request").equals("master") 
							&& obj.containsKey("master")) {
						ds.setMaster((String) obj.get("master"));
						
						obj = new JSONObject();
						obj.put("master", "success");
						sendJSON(obj);
					}	
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
}
