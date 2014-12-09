import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.Scanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class SteamLite {
	
	private static final Logger rootLogger = LogManager.getRootLogger();
	
	private static String username;
	private static String password;
	private static ArrayList<String> myLibrary;
	private static HashMap<String, Game> gamesLibrary;
	private static Scanner scan;
	private static String[] discServer;
	private static String[] steamServer;
	private static JSONParser parser;
	
	private static JSONObject serverRequest(JSONObject request) {
		String lineText = "";
		Socket steamSock;
		
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
		} catch (IOException e) {
			//e.printStackTrace();
		}
		
		try {
			return (JSONObject) parser.parse(lineText);
		} catch (ParseException e) {
			//e.printStackTrace();
			rootLogger.trace("ParseException: " + e);
			return null;
		}
	}
	
	/**
	 * Gets the SteamServer SteamLite will connect to
	 */
	@SuppressWarnings("unchecked")
	private static boolean fillLibrary() {
		rootLogger.trace("fillLibrary Called");
		JSONObject obj = new JSONObject();
		obj.put("request", "library");
		obj.put("username", username);
		obj.put("password", password);
		
		obj = serverRequest(obj);
		if(obj == null) {
			getInitialServer();
			return false;
		}
		if(!obj.containsKey("library") 
				|| (!obj.get("library").equals("success") 
						&& !obj.get("library").equals("failed"))) {
			System.out.println("Sorry, a problem has occurred with the "
					+ "server");
			rootLogger.trace("Key missing");
			return false;
		}
		else if(obj.get("library").equals("failed")) {
			System.out.println(obj.get("exception"));
			rootLogger.trace("Library failed");
			return false;
		}
		
		myLibrary.clear();
		myLibrary.addAll((Collection<? extends String>) obj.get("games"));
		
		File cur = new File("./src/");
		File[] files = cur.listFiles();
		
		for(String game : myLibrary) {
			if(!gamesLibrary.containsKey(game.replaceAll(" ", ""))) {
				for(File file: files) {
					String fileCheck = file.getName().replace(".java", "");
					if(game.replaceAll(" ", "").equals(fileCheck)) {
//						Class<?> clazz = Class.forName(fileCheck);
//						Constructor<?> constructor = clazz.getConstructor(String.class, Integer.class);
//						Object instance = constructor.newInstance("stringparam", 42);
						if(fileCheck.equals("TickTackToe")) {
							gamesLibrary.put(fileCheck, new TickTackToe());
						}
						else if(fileCheck.equals("CoinFlip")) {
							gamesLibrary.put(fileCheck, new CoinFlip());
						}
					}
				}
			}
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private static boolean downloadGame(String game) {
		JSONObject obj = new JSONObject();
		obj.put("request", "download");
		obj.put("username", username);
		obj.put("password", password);
		obj.put("game", game);
		
		obj = serverRequest(obj);
		if(obj == null) {
			getInitialServer();
			return false;
		}
		if(!obj.containsKey("download") || !obj.containsKey("game") 
				|| obj.containsKey("title") 
				|| (!obj.get("download").equals("success")
				&& !obj.get("download").equals("failed"))) {
			System.out.println("Sorry, a problem has occurred with the "
					+ "server");
			return false;
		}
		else if(obj.get("download").equals("failed")) {
			System.out.println(obj.get("exception"));
			return false;
		}
		
		try { 
			File file = new File("/src/" + obj.get("title"));
 
			if (!file.exists()) {
				file.createNewFile();
			}
 
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write((String) obj.get("game"));
			bw.close(); 
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return fillLibrary();
	}
	
	private static void printCommands() {
		System.out.println("Logout: (o)\nDisplay Library: (l)\nDisplay Store: "
				+ "(s)\nPlay Game: (p)\nBuy a Game: (b)\n");
	}
	
	private static void printGameCommands() {
		System.out.println("Make move: (m)\nDisplay Rules: (r)\nDraw Board: (d)");
	}
	
	/**
	 * Play the chosen game.
	 */
	//TODO: Multiplayer
	private static void playGame(int choice) {
		if(!gamesLibrary.containsKey(myLibrary.get(choice).replaceAll(" ", ""))) {
			System.out.println("Game is now downloading");
			downloadGame(myLibrary.get(choice));
			System.out.println("Game download complete");
		}
		Game game = gamesLibrary.get(myLibrary.get(choice).replaceAll(" ", ""));
		String input;
		printGameCommands();
		
		while(!game.isVictory()) {
			System.out.println("Print Commands Again: (p)");
			if(game.isMultiplayer()) {
				//get update
			}
			
			input = scan.next();
			
			if(input.equals("d")) {
				System.out.println(game.drawBoard());
			}
			else if(input.equals("m")) {
				if(game.isMyTurn()) {
					if(!game.makeMove()) {
						System.out.println("Illegal Move Made");
					}
					else {
						if(game.isMultiplayer()) {
							//send update
						}
						if(!game.isVictory()) {
							System.out.println("Victory has not yet been attained");
						}
						else {
							break;
						}
					}
				} 
				else {
					System.out.println("Not your turn yet");
				}
			}
			else if(input.equals("r")) {
				System.out.println(game.getRules());
			}
			else if(input.equals("p")) {
				printGameCommands();
			}
			else {
				System.out.println(input + " is not a legal argument.");
			}
		}
		System.out.println("Returning to main menu.");
	}
	
	/**
	 * User login to SteamServer
	 */
	@SuppressWarnings("unchecked")
	private static boolean login() {		
		JSONObject obj = new JSONObject();
		
		System.out.println("Log In: (l)\nRegister: (r)");
		String input = scan.next();
		if(input.equals("l")) {
			//request login
			obj.put("request", "login");
			System.out.print("Username: ");
			username = scan.next();
			obj.put("username", username);
			System.out.print("Password: ");
			password = scan.next();
			obj.put("password", password);
			//Send Request
			obj = serverRequest(obj);
			if(obj == null) {
				getInitialServer();
				return false;
			}
			else if(!obj.containsKey("login") 
					|| (!obj.get("login").equals("success") 
							&& !obj.get("login").equals("failed"))) {
				System.out.println("Sorry, a problem has occurred with the "
						+ "server");
				return false;
			}
			else if(obj.get("login").equals("failed")) {
				System.out.println(obj.get("exception"));
				return false;
			}
			else {
				return true;
			}
		}
		else if(input.equals("r")) {
			//request login
			obj.put("request", "register");
			System.out.print("New Username: ");
			username = scan.next();
			obj.put("username", username);
			System.out.print("New Password: ");
			password = scan.next();
			obj.put("password", password);
			//Send Request
			obj = serverRequest(obj);
			if(obj == null) {
				getInitialServer();
				return false;
			}
			if(!obj.containsKey("register") 
					|| (!obj.get("register").equals("success") 
							&& !obj.get("register").equals("failed"))) {
				System.out.println("Sorry, a problem has occurred with the "
						+ "server");
				return false;
			}
			else if(obj.get("register").equals("failed")) {
				System.out.println(obj.get("exception"));
				return false;
			}
			else {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Logs user out from the SteamServer
	 */
	@SuppressWarnings("unchecked")
	private static boolean logout() {
		JSONObject obj = new JSONObject();
		obj.put("request", "logout");
		obj.put("username", username);
		obj.put("password", password);
		//Send Request
		obj = serverRequest(obj);
		if(obj == null) {
			getInitialServer();
			return false;
		}
		if(!obj.containsKey("logout") 
				|| (!obj.get("logout").equals("success") 
						&& !obj.get("logout").equals("failed"))) {
			System.out.println("Sorry, a problem has occurred with the "
					+ "server");
			return false;
		}
		else if(obj.get("logout").equals("failed")) {
			System.out.println(obj.get("exception"));
			return false;
		}
		else {
			return true;
		}
	}
	
	/**
	 * Gets the list of games in the store from the SteamServer
	 */
	@SuppressWarnings("unchecked")
	private static boolean displayStore() {
		JSONObject obj = new JSONObject();
		obj.put("username", username);
		obj.put("password", password);
		obj.put("request", "store");
		obj = serverRequest(obj);
		if(obj == null) {
			getInitialServer();
			return false;
		}
		if(!obj.containsKey("store") || !obj.containsKey("games")
				|| (!obj.get("store").equals("success") 
						&& !obj.get("store").equals("failed"))) {
			System.out.println("Sorry, a problem has occurred with the "
					+ "server");
			return false;
		}
		else if(obj.get("store").equals("failed")) {
			System.out.println(obj.get("exception"));
			return false;
		}
		else {
			ArrayList<String> games = (ArrayList<String>) obj.get("games");
			System.out.println("Games in store:");
			for(int i = 0; i < games.size(); i++) {
				System.out.println(i + ") " + games.get(i));
			}
			return true;
		}
	}
	
	/**
	 * Purchases game from the SteamServer
	 */
	@SuppressWarnings("unchecked")
	private static boolean buyGame() {
		JSONObject obj = new JSONObject();
		obj.put("request", "buy");
		obj.put("username", username);
		obj.put("password", password);
		System.out.println("Please enter index of game to purchase:\n Enter (s)"
				+ " to display store");
		//Send Request
		String input = "s";
		while(input.equals("s")) {
			input = scan.next();
			if(input.equals("s")) {
				displayStore();
			}
			else {
				obj.put("game", input);
			}
		}
		obj = serverRequest(obj);
		if(!obj.containsKey("buy") 
				|| (!obj.get("buy").equals("success") 
						&& !obj.get("buy").equals("failed"))) {
			System.out.println("Sorry, a problem has occurred with the "
					+ "server");
			return false;
		}
		else if(obj.get("buy").equals("failed")) {
			System.out.println(obj.get("exception"));
			return false;
		}
		else {
			fillLibrary();
			return true;
		}
	}
	/**
	 * Gets the DiscServer from config file
	 */
	private static boolean getDiscServer(String file) {
		String server = "";
		Path path = FileSystems.getDefault().getPath(".", file);
		try {
			server = Files.readAllLines(path, Charset.defaultCharset()).get(0);
			discServer = server.split(":");
		} catch (IOException e) {
			System.out.println("Could not read file: " + file);
			return false;
		}
		return true;
	}
	
	/**
	 * Gets the SteamServer SteamLite will connect to
	 */
	@SuppressWarnings("unchecked")
	private static boolean getInitialServer() {
		
		
		JSONObject obj = new JSONObject();
		obj.put("request", "getmaster");
		if(steamServer[0] != null) {
			obj.put("master", steamServer[0] + ":" + steamServer[1]);
		}
		
		steamServer[0] = discServer[0];
		steamServer[1] = discServer[1];
		
		obj = serverRequest(obj);
		if(!obj.containsKey("getmaster") || !obj.containsKey("ip") 
				|| !obj.containsKey("port") 
				|| (!obj.get("getmaster").equals("success")
				&& !obj.get("getmaster").equals("failed"))) {
			System.out.println("Sorry, a problem has occurred with the "
					+ "server");
			return false;
		}
		else if(obj.get("getmaster").equals("failed")) {
			System.out.println(obj.get("exception"));
			return false;
		}
		else {
			steamServer[0] = (String) obj.get("ip");
			steamServer[1] = (String) obj.get("port");
			return true;
		}	
	}
	
	public static void main(String[] args) {
		if(args.length < 1) {
			System.out.println("Usage: java SteamLite configFile");
			System.exit(0);
		}
		
		myLibrary = new ArrayList<String>();
		gamesLibrary = new HashMap<String, Game>();
		scan = new Scanner(System.in);
		parser = new JSONParser();
		steamServer = new String[2];
		steamServer[0] = null;
		discServer = new String[2];
		
		if(!getDiscServer(args[0])) {
			System.exit(0);
		}
		
		while(!getInitialServer());
//		steamServer = new String[2];
//		steamServer[0] = "192.168.1.4";
//		steamServer[1] = "2345";
		
		while(!login());
		
		fillLibrary();
		
		String input = "";
		printCommands();
		while(!input.equals("o")) {
			System.out.println("List Commands: (c)\n");
			input = scan.next();
			
			if(input.equals("l")) {
				System.out.println("Game Library:");
				for(int i = 0; i < myLibrary.size(); i++) {
					if(gamesLibrary.containsKey(myLibrary.get(i).replaceAll(" ", ""))) {
						System.out.println(i + ") " + myLibrary.get(i));
					}
					else {
						System.out.println(i + ") " + myLibrary.get(i) 
								+ " (Not Downloaded)");
					}
				}
			}
			else if(input.equals("c")) {
				printCommands();
			}
			else if(input.equals("o")) {
				logout();
			}
			else if(input.equals("s")) {
				displayStore();
			}
			else if(input.equals("p")) {
				System.out.println("Choose Game to Play:");
				for(int i = 0; i < myLibrary.size(); i++) {
					if(gamesLibrary.containsKey(myLibrary.get(i).replaceAll(" ", ""))) {
						System.out.println(i + ") " + myLibrary.get(i));
					}
					else {
						System.out.println(i + ") " + myLibrary.get(i) 
								+ " (Not Downloaded)");
					}
				}
				
				input = scan.next();
				
				try {
					int choice = Integer.parseInt(input);
					if(choice >= myLibrary.size() || choice < 0) {
						System.out.println(input + "is not a valid choice");
					}
					else {
						playGame(choice);
					}
				} catch(NumberFormatException e) {
					System.out.println(input + "is not a valid choice");
				}
			}
			else if(input.equals("b")) {
				buyGame();
			}
		}
	}
}
