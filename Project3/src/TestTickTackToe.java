import static org.junit.Assert.assertEquals;
import org.junit.Test;


public class TestTickTackToe {

	@Test
	public void baseTest() {
		TickTackToe ttt = new TickTackToe();

		assertEquals(ttt.getPlayer(), "X");
		assertEquals(ttt.isMyTurn(), true);
		assertEquals(ttt.setPlayer("O"), true);
		assertEquals(ttt.isMyTurn(), false);
	}
	
	@Test
	public void legalMoveTest() {
		TickTackToe ttt = new TickTackToe();
		assertEquals(ttt.isLegal("0"), true);
	}
	
	@Test
	public void illegalMoveTest() {
		TickTackToe ttt = new TickTackToe();
		assertEquals(ttt.isLegal("9"), false);
	}
	
//	@Test
//	public void illegalMoveTest2() {
//		TickTackToe ttt = new TickTackToe();
//		ttt.makeMove("0");
//		assertEquals(ttt.isLegal("0"), false);
//	}
	
//	@Test
//	public void moveTest() {
//		TickTackToe ttt = new TickTackToe();
//		assertEquals(ttt.makeMove("0"), true);
//	}
	
//	@Test
//	public void victoryTest() {
//		TickTackToe ttt = new TickTackToe();
//		ttt.makeMove("0");
//		ttt.setPlayer("O");
//		ttt.makeMove("1");
//		ttt.setPlayer("X");
//		ttt.makeMove("4");
//		ttt.setPlayer("O");
//		ttt.makeMove("2");
//		ttt.setPlayer("X");
//		ttt.makeMove("8");
//	}
}
