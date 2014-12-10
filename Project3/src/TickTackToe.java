import java.util.ArrayList;
import java.util.Scanner;


public class TickTackToe implements Game{

	private ArrayList<String> board;
	private boolean isXTurn;
	private String player;
	private final String title = "Tick Tack Toe";
	private Scanner scan;
	
	public TickTackToe() {
		board = new ArrayList<String>();
		for(int i = 0; i < 9; i++) {
			board.add(Integer.toString(i));
		}
		isXTurn = true;
		player = "X";
		scan = new Scanner(System.in);
	}
	
	@Override
	public String drawBoard() {
		String curBoard = "";
		for(int i = 0; i < 9; i++) {
			curBoard += board.get(i);
			if (i%3 != 2) {
				curBoard += " | ";
			}
			else if (i < 8) {
				curBoard += "\n__|___|__\n";
			}
			else {
				curBoard += "\n";
			}
		}
		return curBoard;
	}

	@Override
	public String getRules() {
		return "Choose square you wish to make a move in. First to get 3 in a"
				+ " row wins! First move is X's, then O's.";
	}

	@Override
	public boolean makeMove() {
		System.out.println("Enter square you wish to make move:");
		String move = scan.next();

		if(isLegal(move)) {
			if(isXTurn) {
				board.set(Integer.parseInt(move), "X");
				isXTurn = false;
			}
			else {
				board.set(Integer.parseInt(move), "O");
				isXTurn = true;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean isLegal(String move) {
		int square;
		
		if(player.equals("X") && !isXTurn) {
			return false;
		}
		else if(player.equals("O") && isXTurn) {
			return false;
		}
		
		try {
			square = Integer.parseInt(move);
		} catch(NumberFormatException e) {
			return false;
		}
		
		if (square > 8 || square < 0 || !board.get(square).equals(move)) {
			return false;
		}
		
		return true;
	}

	@Override
	public boolean isVictory() {
		String currentSquare = board.get(0);
		if(board.get(1).equals(currentSquare) && board.get(2).equals(currentSquare)) {
			return printVictoryOutcome(board.get(0));
		}
		else if(board.get(3).equals(currentSquare) && board.get(6).equals(currentSquare)) {
			return printVictoryOutcome(board.get(0));
		}
		else if(board.get(4).equals(currentSquare) && board.get(8).equals(currentSquare)) {
			return printVictoryOutcome(board.get(0));
		}
		
		currentSquare = board.get(4);
		if(board.get(1).equals(currentSquare) && board.get(7).equals(currentSquare)) {
			return printVictoryOutcome(board.get(4));
		}
		else if(board.get(6).equals(currentSquare) && board.get(2).equals(currentSquare)) {
			return printVictoryOutcome(board.get(4));
		}
		else if(board.get(3).equals(currentSquare) && board.get(5).equals(currentSquare)) {
			return printVictoryOutcome(board.get(4));
		}
		
		currentSquare = board.get(8);
		if(board.get(5).equals(currentSquare) && board.get(2).equals(currentSquare)) {
			return printVictoryOutcome(board.get(8));
		}
		else if(board.get(6).equals(currentSquare) && board.get(7).equals(currentSquare)) {
			return printVictoryOutcome(board.get(8));
		}

		return false;
	}
	
	private boolean printVictoryOutcome(String winner) {
		System.out.println("Winner is: " + winner);
		if(winner.equals(getPlayer())) {
			System.out.println("Congratulations! You win!!!");
		}
		else {
			System.out.println("Better luck next time!");
		}
		return true;
	}

	@Override
	public boolean updateBoard(String currentPlayerTurn, String newBoard) {
		String[] newBoardPieces = newBoard.split(",");
		int Xcount = 0;
		int Ocount = 0;
		
		if (newBoardPieces.length < 9) {
			return false;
		}
		
		for (int i = 0; i < 9; i++) {
			board.set(i, newBoardPieces[i]);
			if(newBoardPieces[i].equals("O")) {
				Ocount++;
			}
			else if(newBoardPieces[i].equals("X")) {
				Xcount++;
			}
		}
		if (Xcount > Ocount) {
			isXTurn = false;
		}
		else {
			isXTurn = true;
		}
		
		return true;
	}
	
	@Override
	public String getBoard() {
		String boardCpy = "";
		for(String piece: board) {
			boardCpy += piece + ",";
		}
		return boardCpy;
	}

	@Override
	public boolean isMyTurn() {
		if (player.equals("X") && isXTurn) {
			return true;
		}
		else if (player.equals("O") && !isXTurn) {
			return true;
		}
		return false;
	}

	@Override
	public boolean setPlayer(String player) {
		if(player.equals("X") || player.equals("O")) {
			this.player = player;
			return true;
		}
		return false;
	}

	@Override
	public String getPlayer() {
		return player;
	}


	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public boolean isMultiplayer() {
		return true;
	}
}
