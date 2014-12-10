
public interface Game {
	
	/**
	 * Returns title of the game being played
	 */
	String getTitle();

	/**
	 * Returns a String representation of the game being played
	 */
	String drawBoard();
	
	/**
	 * Updates the game being played with a new board (for multiplayer)
	 */
	boolean updateBoard(String currentPlayerTurn, String newBoard);
	
	/**
	 * Gets a string copy of the board for update purposes
	 */
	String getBoard();
	
	/**
	 * Returns the rules of the game being played
	 */
	String getRules();
	
	/**
	 * Returns true if the move is successful
	 */
	boolean makeMove();
	
	/**
	 * Returns true if the move is a legal move
	 */
	boolean isLegal(String move);
	
	/**
	 * Returns true if victory has been achieved
	 */
	boolean isVictory();
	
	/**
	 * Returns true if it is current player's turn (for multiplayer)
	 */
	boolean isMyTurn();
	
	/**
	 * Sets currently player of the game
	 */
	boolean setPlayer(String player);
	
	/**
	 * Returns the player of the game
	 */
	String getPlayer();
	
	/**
	 * Returns true if it is current player's turn (for multiplayer)
	 */
	boolean isMultiplayer();
		
}
