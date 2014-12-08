import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class TestCoinFlip {
	
	@Test
	public void baseTest() {
		CoinFlip cp = new CoinFlip();
		assertEquals(cp.drawBoard(), "H");
	}
	
	@Test
	public void rulesTest() {
		CoinFlip cp = new CoinFlip();
		assertEquals(cp.getRules(), "Call Heads(H) or Tails(T). If you call it right, you win!");
	}
	
//	@Test
//	public void makeLegalMoveTest() {
//		CoinFlip cp = new CoinFlip();
//		assertEquals(cp.makeMove("H"), true);
//	}
	
//	@Test
//	public void makeIllegalMoveTest() {
//		CoinFlip cp = new CoinFlip();
//		assertEquals(cp.makeMove("Heads"), false);
//	}
	
	@Test
	public void playerTest() {
		CoinFlip cp = new CoinFlip();
		assertEquals(cp.getPlayer(), "");
	}
	
	@Test
	public void myTurnTest() {
		CoinFlip cp = new CoinFlip();
		assertEquals(cp.isMyTurn(), true);
	}
	
//	@Test
//	public void victoryTest() {
//		CoinFlip cp = new CoinFlip();
//		cp.makeMove("H");
//		String board = cp.drawBoard();
//		if(board.equals("H")) {
//			assertEquals(cp.isVictory(), true);
//		}
//		else {
//			assertEquals(cp.isVictory(), false);
//		}
//	}
	
}