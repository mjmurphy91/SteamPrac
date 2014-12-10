import java.util.Random;
import java.util.Scanner;

public class CoinFlip implements Game{

	private boolean isHeads;
	private Random rnd;
	private String lastMove;
	private final String title = "Coin Flip";
	private Scanner scan;
	
	public CoinFlip() {
		isHeads = true;
		rnd = new Random();
		lastMove = "";
		scan = new Scanner(System.in);
	}
	@Override
	public String drawBoard() {
		if(isHeads == true) {
			return "H";
		}
		return "T";
	}

	@Override
	public String getRules() {
		return "Call Heads(H) or Tails(T). If you call it right, you win!";
	}

	@Override
	public boolean makeMove() {
		System.out.println("Take your guess:");
		String move = scan.next();
		if (!isLegal(move)) {
			return false;
		}
		isHeads = rnd.nextBoolean();
		lastMove = move;
		return true;
	}

	@Override
	public boolean isLegal(String move) {
		if(move.equals("H") || move.equals("T")) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isVictory() {
		if(lastMove.equals("H") && isHeads) {
			System.out.println("Correct!");
			lastMove = "";
			return true;
		}
		else if (lastMove.equals("T") && !isHeads) {
			System.out.println("Correct!");
			lastMove = "";
			return true;
		}		
		return false;
	}
	
	@Override
	public boolean updateBoard(String currentPlayerTurn, String newBoard) {
		return false;
	}
	
	@Override
	public boolean isMyTurn() {
		return true;
	}
	
	@Override
	public boolean setPlayer(String player) {
		return true;
	}
	
	@Override
	public String getPlayer() {
		return "";
	}
	
	@Override
	public String getTitle() {
		return title;
	}
	
	@Override
	public boolean isMultiplayer() {
		return false;
	}
	@Override
	public String getBoard() {
		return drawBoard();
	}
}
