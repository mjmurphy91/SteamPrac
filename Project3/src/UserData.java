import java.io.Serializable;
import java.util.ArrayList;

import org.json.simple.JSONArray;


@SuppressWarnings("serial")
public class UserData implements Serializable{

	private String pswd;
	private boolean isSignedIn;
	private ArrayList<String> gamesOwned;
	
	public UserData(String password) {
		pswd = password;
		isSignedIn = true;
		gamesOwned = new ArrayList<String>();
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
	
}
