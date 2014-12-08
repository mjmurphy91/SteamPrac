import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.simple.JSONArray;

@SuppressWarnings("serial")
public class DataStore implements java.io.Serializable {
	private static final Logger rootLogger = LogManager.getRootLogger();

	private ArrayList<String> servers;
	private String self;
	private ReentrantReadWriteLock lock;
	private HashMap<String, Integer> serverLoads;
	private HashMap<String, UserData> userInfo;
	private HashMap<String, String> gameFiles;

	public DataStore(String self) {
		this.self = self;
		lock = new ReentrantReadWriteLock();
		userInfo = new HashMap<String, UserData>();
		gameFiles = new HashMap<String, String>();
		servers = new ArrayList<String>();
	}

	public DataStore() {
		serverLoads = new HashMap<String, Integer>();
		lock = new ReentrantReadWriteLock();
	}
	
	public boolean setSelf(String newSelf) {
		lock.writeLock().lock();
		rootLogger.trace("Acquired setSelf Lock");
		self = newSelf;
		lock.writeLock().unlock();
		rootLogger.trace("Relinquished setSelf Lock");
		return true;
	}
	
	public String getSelf() {
		String selfCopy;
		lock.readLock().lock();
		rootLogger.trace("Acquired getSelf Lock");
		selfCopy = self;
		lock.readLock().unlock();
		rootLogger.trace("Relinquished getSelf Lock");
		return selfCopy;
	}
	
	public boolean addUserInfo(String username, String password, String location) {
		lock.writeLock().lock();
		rootLogger.trace("Acquired addUserInfo Lock");
		if (!userInfo.containsKey(username)) {
			UserData newUser = new UserData(password, location);
			userInfo.put(username, newUser);
			lock.writeLock().unlock();
			rootLogger.trace("Relinquished addUserInfo Lock");
			return true;
		}
		
		lock.writeLock().unlock();
		rootLogger.trace("Relinquished addUserInfo Lock");
		return false;
	}
	
	public boolean addGameToUser(String username, String gameIndex) {
		lock.writeLock().lock();
		rootLogger.trace("Acquired addGameToUser Lock");
		ArrayList<String> storeGames = new ArrayList<String>();
		storeGames.addAll(gameFiles.keySet());
		try {
			String game = storeGames.get(Integer.parseInt(gameIndex));
			if (userInfo.containsKey(username)) {
				JSONArray games = userInfo.get(username).getGames();
				
				if(!games.contains(game)) {
					rootLogger.trace("Adding: " + game + " to user: " + username);
					userInfo.get(username).addGame(game);
					lock.writeLock().unlock();
					rootLogger.trace("Relinquished addGameToUser Lock");
					return true;
				}
			}
		} catch(NumberFormatException e) {
			rootLogger.trace("Relinquished addGameToUser Lock");
		}
		
		lock.writeLock().unlock();
		rootLogger.trace("Relinquished addGameToUser Lock");
		return false;
	}
	
	public boolean signIn(String username, String password, String location) {
		lock.writeLock().lock();
		rootLogger.trace("Acquired signIn Lock");
		if(userInfo.containsKey(username) 
				&& userInfo.get(username).checkPassword(password)) {
			userInfo.get(username).signIn(password, location);
			lock.writeLock().unlock();
			rootLogger.trace("Relinquished signIn Lock");
			return true;
		}
		lock.writeLock().unlock();
		rootLogger.trace("Relinquished signIn Lock");
		return false;
	}
	
	public boolean signOut(String username, String password, String location) {
		lock.writeLock().lock();
		rootLogger.trace("Acquired signIn Lock");
		if(userInfo.containsKey(username) 
				&& userInfo.get(username).checkPassword(password)) {
			userInfo.get(username).signOut(password, location);
			lock.writeLock().unlock();
			rootLogger.trace("Relinquished signOut Lock");
			return true;
		}
		lock.writeLock().unlock();
		rootLogger.trace("Relinquished signOut Lock");
		return false;
	}
	
	public boolean register(String username, String password, String location) {
		lock.writeLock().lock();
		rootLogger.trace("Acquired register Lock");
		if(userInfo.containsKey(username)) {
			rootLogger.trace("Relinquished register Lock");
			return false;
		}
		UserData newData = new UserData(password, location);
		userInfo.put(username, newData);
		lock.writeLock().unlock();
		rootLogger.trace("Relinquished register Lock");
		return true;
	}
	
	public UserData getUserInfo(String username, String password, String location) {
		lock.readLock().lock();
		rootLogger.trace("Acquired getUserInfo Lock");
		if(userInfo.containsKey(username) 
				&& userInfo.get(username).checkPassword(password)) {
			UserData info = userInfo.get(username).returnCopy();
			lock.readLock().unlock();
			rootLogger.trace("Relinquished getUserInfo Lock");
			return info;
		}
		lock.readLock().unlock();
		rootLogger.trace("Relinquished getUserInfo Lock");
		return null;
	}
	
	public boolean addGame(String gameTitle, String filePath) {

		lock.writeLock().lock();
		rootLogger.trace("Acquired addGame Lock");
		if (!gameFiles.containsKey(gameTitle)) {
			gameFiles.put(gameTitle, filePath);
			lock.writeLock().unlock();
			rootLogger.trace("Relinquished addGame Lock");
			return true;
		}
		
		lock.writeLock().unlock();
		rootLogger.trace("Relinquished addGame Lock");
		return false;
	}

	public JSONArray getUserGames(String username) {
		JSONArray userGames = new JSONArray();
		lock.readLock().lock();
		rootLogger.trace("Acquired getUserGames Lock");
		UserData data = userInfo.get(username);
		userGames = data.getGames();
		lock.readLock().unlock();
		rootLogger.trace("Relinquished getUserGames Lock");
		return userGames;
	}
	
	@SuppressWarnings("unchecked")
	public JSONArray getStoreGames(String username) {
		JSONArray gameTitles = new JSONArray();
		lock.readLock().lock();
		rootLogger.trace("Acquired getStoreGames Lock");
		UserData data = userInfo.get(username);
		ArrayList<String> userGames = data.getGames();
		for (String game : gameFiles.keySet()) {
			if(userGames.contains(game)) {
				gameTitles.add(game + " (Purchased)");
			}
			else {
				gameTitles.add(game);
			}
		}
		lock.readLock().unlock();
		rootLogger.trace("Relinquished getStoreGames Lock");
		return gameTitles;
	}

	/**
	 *  refreshing list of servers
	 */
	public String getGameFile(String title) {
		lock.readLock().lock();
		rootLogger.trace("Acquired getGameFile Lock");
		if (gameFiles.containsKey(title)) {
			String fileloc = gameFiles.get(title);
			lock.readLock().unlock();
			rootLogger.trace("Relinquished getGameFile Lock");
			return fileloc;
		}
		lock.readLock().unlock();
		rootLogger.trace("Relinquished getGameFile Lock");
		return null;
	}

	/**
	 * SteamServer refreshing list of servers
	 */
	public void addServer(String newServer) {
		lock.writeLock().lock();
		rootLogger.trace("Acquired addServer Lock");
		if (!servers.contains(newServer)) {
			servers.add(newServer);
		}
		lock.writeLock().unlock();
		rootLogger.trace("Relinquished addServer Lock");
	}
	
	/**
	 * Method for removing a server from the server list
	 */
	public void removeServer(String server) {
		lock.writeLock().lock();
		rootLogger.trace("Acquired removeServer Lock");
		servers.remove(server);
		lock.writeLock().unlock();
		rootLogger.trace("Relinquished removeServer Lock");
	}

	
	
	/**
	 * Sets DataStore's entire content to given snapshot
	 */
	/*
	@SuppressWarnings("unchecked")
	public void setSnapshot(JSONObject snapshot) {
		rootLogger.trace("Updating server with snapshot");
		lock.writeLock().lock();
		rootLogger.trace("Acquired setSnapshot Lock");

		// Set ServerList
		JSONArray newServers = (JSONArray) snapshot.get("servers");
		servers = new ArrayList<String>();
		servers.add(self);
		for (Object obServer : newServers) {
			String server = (String) obServer;
			servers.add(server);
		}

		// Set DataMap
		JSONObject newData = (JSONObject) snapshot.get("data");
		dataMap = new HashMap<String, ArrayList<String>>();
		for (Object obData : newData.keySet()) {
			String data = (String) obData;
			ArrayList<String> newDataList = new ArrayList<String>();
			newDataList.addAll((ArrayList<String>) newData.get(data));
			dataMap.put(data, newDataList);
		}

		// Set DataVersionMap
		JSONObject newVers = (JSONObject) snapshot.get("vers");
		dataVersionMap = new HashMap<String, Integer>();
		for (Object obData : newVers.keySet()) {
			String data = (String) obData;
			int newVersion = Integer.parseInt((String) newVers.get(data));
			dataVersionMap.put(data, newVersion);
		}

		lock.writeLock().unlock();
		rootLogger.trace("Relinquished setSnapshot Lock");
	}
	*/

	/**
	 * Returns a snapshot of the DataStore's entire content
	 */
	/*
	@SuppressWarnings("unchecked")
	public JSONObject getSnapshot() {

		JSONObject obj = new JSONObject();
		rootLogger.trace("Request for snapshot");
		lock.readLock().lock();
		rootLogger.trace("Acquired getSnapshot Lock");

		// Get ServerList
		JSONArray serverListcpy = new JSONArray();
		for (String original : servers) {
			serverListcpy.add(original);
		}
		obj.put("servers", serverListcpy);

		// Get DataMap
		JSONObject dataMapcpy = new JSONObject();
		JSONArray temp2;
		for (String key : dataMap.keySet()) {
			temp2 = new JSONArray();
			temp2.addAll(dataMap.get(key));
			dataMapcpy.put(key, temp2);
		}
		obj.put("data", dataMapcpy);

		// Get DataVersionMap
		JSONObject dataVersionMapcpy = new JSONObject();
		String temp3;
		for (String key : dataVersionMap.keySet()) {
			temp3 = dataVersionMap.get(key).toString();
			dataVersionMapcpy.put(key, temp3);
		}
		obj.put("vers", dataVersionMapcpy);

		lock.readLock().unlock();
		rootLogger.trace("Relinquished getSnapshot Lock");
		return obj;
	}
	*/
}
