import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


@SuppressWarnings("serial")
public class UserData implements Serializable{

	private String pswd;
	private boolean isSignedIn;
	private ArrayList<String> gamesOwned;
	private HashMap<String, Game> gamesInProgress;
	
	public UserData(String password) {
		pswd = password;
		isSignedIn = true;
		gamesOwned = new ArrayList<String>();
		gamesInProgress = new HashMap<String, Game>();
	}
	
	public String getPswd() {
		return pswd;
	}
	
	public boolean isSignedIn() {
		return isSignedIn;
	}
	
	public boolean checkPassword(String password) {
		return pswd.equals(password);
	}
	
	public boolean changePassword(String newPassword) {
		pswd = newPassword;
		return true;
	}
	
	public boolean signIn(String password) {
		if(checkPassword(password) && isSignedIn == false) {
			isSignedIn = true;
			return true;
		}
		return false;
	}
	
	public boolean signOut(String password) {
		if(checkPassword(password) && isSignedIn == true) {
			isSignedIn = false;
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
	
	@SuppressWarnings("unchecked")
	public JSONArray getGames() {
		JSONArray gamesList = new JSONArray();
		gamesList.addAll(gamesOwned);		
		return gamesList;
	}
	
	public UserData returnCopy() {
		UserData copy = new UserData(pswd);
		copy.addGames(gamesOwned);
		return copy;
	}
	
	public String hasOpponent() {
		if(!gamesInProgress.isEmpty()) {
			for(String opponent : gamesInProgress.keySet()) {
				return opponent;
			}
		}
		return "";
	}
	
	public boolean startGame(String opponent, String title, String player) {
		if(title.equals("Tick Tack Toe")) {
			Game g = new TickTackToe();
			if(player.equals("2")) {
				g.setPlayer("O");
			}
			gamesInProgress.put(opponent, g);
			return true;
		}
		else {
			System.out.println("Game: " + title + " is not implemented");
			return false;
		}
	}
	
	public boolean makeUpdate(String opponent, String board) {
		Game g = gamesInProgress.get(opponent);
		if(g.getTitle().equals("Tick Tack Toe")) {
			if(g.getPlayer().equals("X")) {
				g.updateBoard("O", board);
			}
			else {
				g.updateBoard("X", board);
			}
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject getUpdate(String opponent) {
		Game g = gamesInProgress.get(opponent);
		JSONObject obj = new JSONObject();
		obj.put("board", g.drawBoard());
		if(g.isMyTurn()) {
			if(g.getPlayer().equals("X")) {
				obj.put("turn", "X");
			}
			else {
				obj.put("turn", "O");
			}
		}
		else {
			if(g.getPlayer().equals("X")) {
				obj.put("turn", "O");
			}
			else {
				obj.put("turn", "X");
			}
		}
		if(g.isVictory()) {
			gamesInProgress.remove(opponent);
		}
		return obj;
	}
	
}
