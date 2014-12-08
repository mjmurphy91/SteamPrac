import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
			int bufferSize = Integer.parseInt(line.split(" ")[1]);
			rootLogger.trace("Buffer Size: " + bufferSize);
			line = in.readLine();
			char[] bytes = new char[bufferSize];
			in.read(bytes, 0, bufferSize);
			lineText = new String(bytes);
						
			out.flush();
			out.close();
			in.close();
			steamSock.close();

		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			return (JSONObject) parser.parse(lineText);
		} catch (ParseException e) {
			return null;
		}
	}
	
	/**
	 * Gets the SteamServer SteamLite will connect to
	 */
	//TODO
	@SuppressWarnings("unchecked")
	private static boolean fillLibrary() {
		JSONObject obj = new JSONObject();
		obj.put("request", "library");
		obj.put("username", username);
		obj.put("password", password);
		
		obj = serverRequest(obj);
		if(!obj.containsKey("library") 
				|| (!obj.get("library").equals("success") 
						&& !obj.get("library").equals("failed"))) {
			System.out.println("Sorry, a problem has occurred with the "
					+ "server");
			return false;
		}
		else if(obj.get("buy").equals("failed")) {
			System.out.println(obj.get("exception"));
			return false;
		}
		
		myLibrary.addAll((Collection<? extends String>) obj.get("games"));
		
		File cur = new File("./src/");
		File[] files = cur.listFiles();
		
		for(String game : myLibrary) {
			for(File file: files) {
				System.out.println(file.getName().replace(".java", ""));
				String fileCheck = file.getName().replace(".java", "");
				if(game.replaceAll(" ", "").equals(fileCheck)) {
//					Class<?> clazz = Class.forName(fileCheck);
//					Constructor<?> constructor = clazz.getConstructor(String.class, Integer.class);
//					Object instance = constructor.newInstance("stringparam", 42);
					if(fileCheck.equals("TickTackToe")) {
						gamesLibrary.put(fileCheck, new TickTackToe());
					}
					else if(fileCheck.equals("CoinFlip")) {
						gamesLibrary.put(fileCheck, new CoinFlip());
					}
				}
			}
		}
		return true;
	}
	
	private static void printCommands() {
		System.out.println("Logout: (o)\nDisplay Library: (l)\nDisplay Store: "
				+ "(s)\nPlay Game: (p)\nBuy a Game: (b)\n");
	}
	
	private static void printGameCommands() {
		System.out.println("Make move: (m)\nDisplay Rules: (r)\nDraw Board: (d)"
				+ "\nPrint Commands Again: (p)");
	}
	
	/**
	 * Play the chosen game.
	 */
	//TODO
	private static void playGame(int choice) {
		Game game = gamesLibrary.get(myLibrary.get(choice));
		String input;
		printGameCommands();
		
		while(!game.isVictory()) {
			if(game.isMultiplayer()) {
				//get update
			}
			
			input = scan.next();
			
			if(input.equals("d")) {
				game.drawBoard();
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
					}
				} 
				else {
					System.out.println("Not your turn yet");
				}
			}
			else if(input.equals("r")) {
				game.getRules();
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
			if(!obj.containsKey("login") 
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
			if(!obj.containsKey("register") 
					|| (!obj.get("register").equals("success") 
							&& !obj.get("register").equals("failed"))) {
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
		if(!obj.containsKey("logout") 
				|| (!obj.get("logout").equals("success") 
						&& !obj.get("logout").equals("failed"))) {
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
		System.out.println("Please enter index of game to purchase:\n Enter (l)"
				+ " to display store");
		//Send Request
		String input = "l";
		while(input.equals("l")) {
			input = scan.next();
			if(input.equals("l")) {
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
	//TODO
	private static boolean getInitialServer() {
		String lineText = "";
		Socket discSock;
		try {
			String IP = discServer[0];
			int PORT = Integer.parseInt(discServer[1]);
			discSock = new Socket(IP, PORT);
			String requestheaders = "GET /tweets HTTP/1.1\n";

			OutputStream out = discSock.getOutputStream();
			out.write(requestheaders.getBytes());

			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(
					discSock.getInputStream()));

			while (!(line = in.readLine().trim()).equals("")) {
				if(line.equalsIgnoreCase("HTTP/1.1 200 OK")) {
					line = in.readLine();
					int bufferSize = Integer.parseInt(line.split(" ")[1]);
					line = in.readLine();
					char[] bytes = new char[bufferSize];
					in.read(bytes, 0, bufferSize);
					lineText = new String(bytes);
					break;
				}
				else {
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
		
		String server = "";
		JSONParser parser = new JSONParser();
		try {
			JSONObject obj=(JSONObject) parser.parse(lineText);
			server = (String) obj.get("min");
		} catch (ParseException e) {
			System.out.println("Could not parse lineText");
		}
		
		if(server.split(":").length == 2) {
			steamServer = server.split(":");
			return true;
		}
		return false;
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
		
		if(!getDiscServer(args[1])) {
			System.exit(0);
		}
		
		//while(!getInitialServer());
		
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
