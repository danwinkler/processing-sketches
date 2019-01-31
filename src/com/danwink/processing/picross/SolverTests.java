package com.danwink.processing.picross;

import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;

import com.danwink.processing.picross.Solver.RowPlay;

public class SolverTests
{
	public void checkRow( BoardState[] row, int[] hints, boolean shouldPlay )
	{
		RowPlay play = (new Solver.WallCounterRule()).applyReversible( row, hints );
		if( shouldPlay )
		{
			assertTrue( play != null );
		}
		else 
		{
			assertTrue( play == null );
		}
	}
	
	@Test
	public void oneOnThenUnknownRowTest()
	{
		BoardState[] row = new BoardState[] {
			BoardState.ON,
			BoardState.UNKNOWN,
			BoardState.UNKNOWN,
			BoardState.UNKNOWN,
			BoardState.UNKNOWN,
		};
		
		int[] hints = new int[] { 1, 3 };
		
		checkRow( row, hints, true );
	}
	
	@Test
	public void finalOnRowTest()
	{
		BoardState[] row = new BoardState[] {
			BoardState.ON,
			BoardState.OFF,
			BoardState.ON,
			BoardState.ON,
			BoardState.UNKNOWN,
		};
		
		int[] hints = new int[] { 1, 3 };
		
		checkRow( row, hints, true );
	}
	
	@Test
	public void fillImpossibleSpansTest()
	{
		BoardState[] row = new BoardState[] {
			BoardState.ON,
			BoardState.ON,
			BoardState.OFF,
			BoardState.UNKNOWN,
			BoardState.OFF,
			BoardState.ON,
			BoardState.ON,
		};
		
		int[] hints = new int[] { 2, 2 };
		
		RowPlay play = (new Solver.FillImpossibleSpansRule()).applyReversible( row, hints );
		assertTrue( play != null );
	}
	
	@Test
	public void surroundCompletedSectionsTest()
	{
		BoardState[] row = new BoardState[] {
			BoardState.ON,
			BoardState.ON,
			BoardState.OFF,
			BoardState.ON,
			BoardState.UNKNOWN,
			BoardState.UNKNOWN,
			BoardState.UNKNOWN,
		};
		
		int[] hints = new int[] { 2, 3 };
		
		RowPlay play = (new Solver.SurroundCompletedSectionsRule()).applyReversible( row, hints );
		assertTrue( play != null );
	}
	
	@Test
	public void rowEquationTest()
	{
		BoardState[] row = new BoardState[] {
			BoardState.UNKNOWN,
			BoardState.UNKNOWN,
			BoardState.UNKNOWN,
			BoardState.UNKNOWN,
			BoardState.UNKNOWN,
			BoardState.UNKNOWN,
			BoardState.UNKNOWN,
		};
		
		int[] hints = new int[] { 8, 9 };
		
		RowPlay play = (new Solver.RowEquationRule()).apply( row, hints );
		assertTrue( play != null );
	}
}
