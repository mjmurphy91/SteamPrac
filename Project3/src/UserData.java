import java.util.ArrayList;
import java.util.HashMap;


public class UserData {

	private String pswd;
	private String signedInAt;
	private ArrayList<String> gamesOwned;
	private HashMap<String, ArrayList<Game>> gamesInProgress;
	
	public UserData(String password, String location) {
		pswd = password;
		signedInAt = location;
		gamesOwned = new ArrayList<String>();
		gamesInProgress = new HashMap<String, ArrayList<Game>>();
	}
	
	public String isSignedIn() {
		return signedInAt;
	}
	
	public boolean checkPassword(String password) {
		return pswd.equals(password);
	}
	
	public boolean changePassword(String newPassword) {
		pswd = newPassword;
		return true;
	}
	
	public boolean signIn(String password, String location) {
		if(checkPassword(password) && signedInAt == null) {
			signedInAt = location;
			return true;
		}
		return false;
	}
	
	public boolean signOut(String password, String location) {
		if(checkPassword(password) && signedInAt == location) {
			signedInAt = null;
			return true;
		}
		return false;
	}
	
	public boolean addGame(String game) {
		if(gamesOwned.contains(game)) {
			return false;
		}
		gamesOwned.add(game);
		return true;
	}
	
	public boolean addGames(ArrayList<String> games) {
		gamesOwned.clear();
		gamesOwned.addAll(games);
		return true;
	}
	
	public ArrayList<String> getGames() {
		ArrayList<String> gamesList = new ArrayList<String>();
		gamesList.addAll(gamesOwned);		
		return gamesList;
	}
	
	public ArrayList<Game> whatGamesInProgress(String opponent) {
		ArrayList<Game> games = new ArrayList<Game>();
		if(gamesInProgress.containsKey(opponent)) {
			games.addAll(gamesInProgress.get(opponent));
		}
		return games;
	}
	
	public boolean addGameInProgress(String opponent, Game game) {
		if(gamesInProgress.keySet().contains(opponent)) {
			ArrayList<Game> games = gamesInProgress.get(opponent);
			for(Game g : games) {
				if (g.getTitle().equals(game.getTitle())) {
					return false;
				}
			}
			games.add(game);
			gamesInProgress.put(opponent, games);
			return true;
		}
		else {
			ArrayList<Game> games = new ArrayList<Game>();
			games.add(game);
			gamesInProgress.put(opponent, games);
			return true;
		}
	}
	
	public boolean removeGameInProgress(String opponent, Game game) {
		if(gamesInProgress.keySet().contains(opponent)) {
			ArrayList<Game> games = gamesInProgress.get(opponent);
			for(int i = 0; i < games.size() ; i++) {
				if (games.get(i).getTitle().equals(game.getTitle())) {
					games.remove(i);
					return true;
				}
			}
			return false;
		}
		return false;
	}
	
	public boolean addGamesInProgress(HashMap<String, ArrayList<Game>> newGamesInProgress) {
		gamesInProgress.clear();
		gamesInProgress.putAll(newGamesInProgress);
		return true;
	}
	
	public UserData returnCopy() {
		UserData copy = new UserData(pswd, signedInAt);
		copy.addGames(gamesOwned);
		copy.addGamesInProgress(gamesInProgress);
		return copy;
	}
	
}
