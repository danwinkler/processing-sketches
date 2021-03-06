package com.danwink.processing.picross;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.danwink.processing.Vector2I;

public class Solver
{
	Rule[] rules = new Rule[] {
//		new FullRowRule(),
//		new WallCounterRule(),
//		new FillCompletedRowsRule(),
//		new FillImpossibleSpansRule(),
//		new SurroundCompletedSectionsRule(),
		new RowEquationRule(),
	};
	
	public void solve( Picross cross )
	{
		List<Rule> ruleList = Arrays.asList( rules );
		
		//Collections.shuffle( ruleList );
		
		for( Rule rule : ruleList )
		{
			Play play = rule.play( cross );
			if( play != null )
			{
				if( play.state == cross.game.board[play.location.x][play.location.y] )
				{
					System.out.println( "Repeated move: " + play );
				}
				
				if( play.state != cross.image.board[play.location.x][play.location.y] )
				{
					System.out.println( "Invalid move: " + play );
				}
				else 
				{
					cross.game.board[play.location.x][play.location.y] = play.state;
					return;
				}
			}
		}
	}
	
	public static interface Rule
	{
		public Play play( Picross cross );
	}
	
	public static interface RowRule
	{
		public RowPlay apply( BoardState[] row, int[] hints );
		public int getLast();
		public void setLast( int last );
	}
	
	public static boolean isUnique( int num, int[] nums )
	{
		boolean found = false;
		for( int i = 0; i < nums.length; i++ )
		{
			if( nums[i] == num )
			{
				if( found ) return false;
				else found = true;
			}
		}
		return true;
	}
	
	public static int max( int[] nums )
	{
		int max = Integer.MIN_VALUE;
		for( int i = 0; i < nums.length; i++ )
		{
			if( nums[i] > max ) max = nums[i];
		}
		return max;
	}
	
	public static int sum( int[] nums )
	{
		int sum = 0;
		for( int i = 0; i < nums.length; i++ )
		{
			sum += nums[i];
		}
		return sum;
	}
	
	public static int[] getSpanHintOwnership( BoardState[] row, int[] hints )
	{
		// TODO
		return null;
	}
	
	public static class RowEquationRule implements Rule, RowRule
	{
		int last = 0;
		
		public static class Term
		{
			public boolean solid;
			public int min;
			public int max;
			
			public Term( boolean solid, int min, int max )
			{
				this.solid = solid;
				this.min = min;
				this.max = max;
			}
		}
		
		public static class ValueBox
		{
			public int value;
			public boolean solid;
			
			public ValueBox( int value, boolean solid )
			{
				this.value = value;
				this.solid = solid;
			}
		}	
		
		public int determineMaxValue( ArrayList<Term> terms, int currentLocation, int i, int rowLength )
		{
			int minRemaining = terms.stream().skip( i+1 ).mapToInt( t -> t.min ).sum();
			
			//System.out.println( String.format( "%d, %d", currentLocation, minRemaining ) );
			
			return rowLength - currentLocation - minRemaining;
		}
		
		public boolean recursiveBuildValueBoxSolution( ArrayList<Term> terms, ArrayList<LinkedList<ValueBox>> solutions, LinkedList<ValueBox> current, BoardState[] row, int i )
		{
			if( solutions.size() > 2000000 ) 
			{
				return false; // If we end up with too many solutions just give up completely
			}
			
			if( i >= terms.size() )
			{
				solutions.add( (LinkedList<ValueBox>)current.clone() );
			}
			else
			{
				Term t = terms.get( i );
				
				int currentLocation = current.stream().mapToInt( v -> v.value ).sum();
				int maxValue = Math.min( t.max, determineMaxValue( terms, currentLocation, i, row.length ) );
				
				lengthSearch: for( int v = t.min; v <= maxValue; v++ )
				{
					for( int j = currentLocation; j < currentLocation + v; j++ )
					{
						if( row[j] != BoardState.UNKNOWN && row[j] != (t.solid ? BoardState.ON : BoardState.OFF) )
						{
							continue lengthSearch;
						}
					}
					
					current.addLast( new ValueBox( v, t.solid ) );
					boolean success = recursiveBuildValueBoxSolution( terms, solutions, current, row, i+1 );
					if( !success ) return false; 
					current.removeLast();
				}
			}
			return true;
		}
		
		public RowPlay apply( BoardState[] row, int[] hints )
		{
			// Build list of terms
			ArrayList<Term> terms = new ArrayList<Term>();
			int maxEmpty = row.length - sum( hints );
			
			terms.add( new Term( false, 0, maxEmpty ) );
			for( int i = 0; i < hints.length; i++ )
			{
				int hint = hints[i];
				terms.add( new Term( true, hint, hint ) );
				if( i < hints.length-1 ) terms.add( new Term( false, 1, maxEmpty ) );
			}
			terms.add( new Term( false, 0, maxEmpty ) );
			
			ArrayList<LinkedList<ValueBox>> solutionPermutations = new ArrayList<>();
			
			boolean success = recursiveBuildValueBoxSolution( terms, solutionPermutations, new LinkedList<ValueBox>(), row, 0 );
			
			if( !success ) return null;
			
			if( solutionPermutations.size() > 500000 )
				System.out.println( "solutions: " + solutionPermutations.size() );
			
			List<LinkedList<ValueBox>> potentialSolutions = solutionPermutations
				.parallelStream()
				.filter( 
					s -> s.stream().mapToInt( v -> v.value ).sum() == row.length 
				).filter( 
					s -> {
						int i = 0;
						for( ValueBox b : s )
						{
							for( int j = 0; j < b.value; j++ )
							{
								if( row[i] != BoardState.UNKNOWN && row[i] != (b.solid ? BoardState.ON : BoardState.OFF))
								{
									return false;
								}
								i++;
							}
						}
						
						return true;
					}
				).collect(Collectors.toList());
			
			int[] solidOccurances = new int[row.length];
			
			final boolean debug = false;
			
			for( LinkedList<ValueBox> solution : potentialSolutions )
			{
				int i = 0;
				for( ValueBox b : solution )
				{
					for( int j = 0; j < b.value; j++ )
					{
						if( b.solid ) solidOccurances[i]++;
						if( debug ) System.out.print( b.solid ? "O" : "X" );
						
						i++;
					}
					if( debug ) System.out.print( "\n" );
				}
			}
			
//			System.out.println( Arrays.stream(row)
//			        .map((BoardState bs) -> bs.shortString()).reduce("", String::concat) );
//			
//			System.out.println( Arrays.stream(hints)
//			        .mapToObj(String::valueOf)
//			        .collect(Collectors.joining(" - ")) );
//			
			for( int i = 0; i < solidOccurances.length; i++ )
			{
				if( solidOccurances[i] == potentialSolutions.size() && row[i] == BoardState.UNKNOWN )
				{
					return new RowPlay( i, BoardState.ON );
				}
				
				if( solidOccurances[i] == 0 && row[i] == BoardState.UNKNOWN )
				{
					return new RowPlay( i, BoardState.OFF );
				}
			}
			
			return null;
		}

		public Play play( Picross cross )
		{
			return applyRowRule( cross, this );
		}

		@Override
		public int getLast()
		{
			return last;
		}

		@Override
		public void setLast( int last )
		{
			this.last = last;
		}
	}
	
	public static class SurroundCompletedSectionsRule implements Rule, RowRule, ReversibleRowRule
	{
		public RowPlay applyReversible( BoardState[] row, int[] hints )
		{
			int onHint = 0;
			boolean onSpan = false;
			int spanLength = 0;
			for( int i = 0; i < row.length; i++ )
			{
				if( row[i] == BoardState.ON )
				{
					onSpan = true;
					spanLength++;
				}
				
				if( row[i] == BoardState.OFF )
				{
					if( onSpan )
					{
						onSpan = false;
						onHint++;
						spanLength = 0;
					}
				}
				
				if( row[i] == BoardState.UNKNOWN )
				{
					if( onSpan )
					{
						if( spanLength == hints[onHint] && max(hints) == spanLength )
						{
							if( row[i-spanLength] == BoardState.UNKNOWN )
							{
								return new RowPlay( i-spanLength, BoardState.OFF );
							}
							
							if( i+1 < row.length && row[i+1] == BoardState.UNKNOWN )
							{
								return new RowPlay( i, BoardState.OFF );
							}
						}
						else
						{
							return null;
						}
					}
				}
			}
			return null;
		}
		
		public RowPlay apply( BoardState[] row, int[] hints )
		{
			return applyBothWays( this, row, hints );
		}

		public Play play( Picross cross )
		{
			return applyRowRule( cross, this );
		}

		@Override
		public int getLast()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setLast( int last )
		{
			
		}
	}
	
	public static class FillImpossibleSpansRule implements Rule, RowRule, ReversibleRowRule
	{
		public RowPlay applyReversible( BoardState[] row, int[] hints )
		{
			int onHint = 0;
			boolean onSpan = false;
			for( int i = 0; i < row.length; i++ )
			{
				if( row[i] == BoardState.ON )
				{
					onSpan = true;
				}
				
				if( row[i] == BoardState.OFF )
				{
					if( onSpan )
					{
						onSpan = false;
						onHint++;
					}
				}
				
				if( row[i] == BoardState.UNKNOWN )
				{
					if( !onSpan )
					{
						for( int j = i; j < row.length; j++ )
						{
							if( row[j] == BoardState.OFF )
							{
								int unknownSpanLength = j-i;
								
								if( unknownSpanLength < hints[onHint] )
								{
									return new RowPlay( i, BoardState.OFF );
								}
							}
						}
					}
					return null;
				}
			}
			return null;
		}
		
		@Override
		public RowPlay apply( BoardState[] row, int[] hints )
		{
			return applyBothWays( this, row, hints );
		}

		public Play play( Picross cross )
		{
			return applyRowRule( cross, this );
		}

		@Override
		public int getLast()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setLast( int last )
		{
			// TODO Auto-generated method stub
			
		}
	}
	
	public static class FillCompletedRowsRule implements Rule, RowRule
	{
		public RowPlay apply( BoardState[] row, int[] hints )
		{
			// Determine if row is complete
			int onHint = 0;
			int currentHintLength = 0;
			for( int i = 0; i < row.length; i++ )
			{
				if( row[i] == BoardState.ON )
				{
					currentHintLength++;
				}
				
				if( row[i] == BoardState.OFF || i == row.length-1 )
				{
					if( currentHintLength > 0 )
					{
						if( hints[onHint] != currentHintLength ) return null;
						
						currentHintLength = 0;
						onHint++;
					}
				}
			}
			
			if( onHint < hints.length ) return null;
			
			// Fill remaining sections
			for( int i = 0; i < row.length; i++ )
			{
				if( row[i] == BoardState.UNKNOWN )
				{
					return new RowPlay( i, BoardState.OFF );
				}
			}
			return null;
		}

		public Play play( Picross cross )
		{
			return applyRowRule( cross, this );
		}

		@Override
		public int getLast()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setLast( int last )
		{
			// TODO Auto-generated method stub
			
		}
	}
	
	public static class WallCounterRule implements Rule, RowRule, ReversibleRowRule
	{
		public RowPlay applyReversible( BoardState[] row, int[] hints )
		{
			int offset = 0;
			int hintIndex = 0;
			
			if( row[0] != BoardState.UNKNOWN )
			{
				boolean onSpan = false;
				int lastOff = -1;
				BoardState lastState = null;
				
				for( int i = 0; i < row.length; i++ )
				{
					if( row[i] == BoardState.ON )
					{
						onSpan = true;
					}
					
					if( row[i] == BoardState.OFF )
					{
						lastOff = i;
						
						if( onSpan )
						{
							onSpan = false;
							hintIndex++;
						}
					}
					
					if( row[i] == BoardState.UNKNOWN )
					{
						offset = lastOff+1;
						
						break;
					}
					
					lastState = row[i];
				}
			}
			
			if( hintIndex >= hints.length ) return null;
			
			int hint = hints[hintIndex];
			
			boolean drawing = false;
			for( int i = offset; i < offset+hint; i++ )
			{
				if( row[i] == BoardState.ON )
				{
					drawing = true;
				} 
				else if( drawing && row[i] == BoardState.UNKNOWN )
				{
					return new RowPlay( i, BoardState.ON );
				}
			}
			
			if( 
				row[offset] == BoardState.ON && // The first hint span starts at edge
				hint < row.length && // The hint span isn't the full width of the image
				row[offset+hint-1] == BoardState.ON && // The last part of the hint span is already filled in
				row[offset+hint] == BoardState.UNKNOWN // We haven't set the end as unknown yet
			)
			{
				return new RowPlay( offset+hint, BoardState.OFF );
			}
			
			return null;
		}
		
		public RowPlay apply( BoardState[] row, int[] hints )
		{
			return applyBothWays( this, row, hints );
		}

		public Play play( Picross cross )
		{
			Play play = applyRowRule( cross, this );
			
			
			return play;
		}

		@Override
		public int getLast()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setLast( int last )
		{
			// TODO Auto-generated method stub
			
		}
	}
	
	public static String joinObjArr( Object[] arr )
	{
		return Arrays.stream( arr ).map( o -> o.toString() ).collect( Collectors.joining( ", " ) );
	}
	
	public static BoardState[] reverse( BoardState[] arr )
	{
		List<BoardState> rowList = Arrays.asList( arr );
		Collections.reverse( rowList );
		return (BoardState[])rowList.toArray();
	}
	
	public static int[] reverse( int[] arr )
	{
		int[] reverse = new int[arr.length];
		for( int i = 0; i < arr.length; i++ )
		{
			reverse[arr.length-1-i] = arr[i];
		}
		return reverse;
	}
	
	public static class FullRowRule implements Rule, RowRule
	{
		public RowPlay apply( BoardState[] row, int[] hints )
		{
			int sum = 0;
			for( int hint : hints )
			{
				sum += hint;
			}
			sum += hints.length-1;
			
			if( sum == row.length )
			{
				BoardState[] correct = new BoardState[row.length];
				int currentIndex = 0;
				for( int i = 0; i < hints.length; i++ )
				{
					int hint = hints[i];
					for( int j = 0; j < hint; j++ )
					{
						correct[currentIndex + j] = BoardState.ON;
					}
					currentIndex += hint + 1;
					if( currentIndex-1 < row.length )
					{
						correct[currentIndex-1] = BoardState.OFF;
					}
				}
				
				for( int i = 0; i < correct.length; i++ )
				{
					if( correct[i] != row[i] )
					{
						if( correct[i] == null )
						{
							System.out.println( "null correct" );
							String text = Arrays.stream( correct ).map( bs -> bs == null ? "null" : bs.name() ).collect( Collectors.joining( "," ) );
							System.out.println( text );
						}
						return new RowPlay( i, correct[i] );
					}
				}
			}
			return null;
		}

		public Play play( Picross cross )
		{
			return applyRowRule( cross, this );
		}

		@Override
		public int getLast()
		{
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void setLast( int last )
		{
			// TODO Auto-generated method stub
			
		}		
	}
	
	public static RowPlay applyBothWays( ReversibleRowRule rule, BoardState[] row, int[] hints )
	{
		RowPlay play = rule.applyReversible( row, hints );
		if( play != null ) return play;
		
		if( hints.length == 1 ) return null;
		
		
		BoardState[] reversedRow = reverse( row );
		int[] reversedHints = reverse( hints );
		
		play = rule.applyReversible( reversedRow, reversedHints );
		if( play != null )
		{
			play.location = row.length-1-play.location;
			return play;
		}
			
		return null;
	}
	
	public static interface ReversibleRowRule
	{
		public RowPlay applyReversible( BoardState[] row, int[] hints );
	}
	
	public static RowPlay applyWithExceptionMessage( RowRule rule, BoardState[] row, int[] hints )
	{
		// Optimization: If row is already complete, return null
		if( Arrays.stream( row ).filter( bs -> bs == BoardState.UNKNOWN ).count() == 0 )
		{
			return null;
		}
		
		try
		{
			return rule.apply( row, hints );
		}
		catch( Exception e )
		{
			System.out.println( "Exception in " + rule.getClass().getName() );
			System.out.println( 
				Arrays.stream( hints ).mapToObj( String::valueOf ).collect( Collectors.joining( ", " ) )
			);
			System.out.println( joinObjArr( row ) );
			
			throw e;
		}
	}
	
	public static Play applyRowRule( Picross cross, RowRule rule )
	{
		BoardState[] buffer;
		RowPlay play;
		
		int offset = rule.getLast();
		
		for( int _i = 0; _i < cross.height + cross.width; _i++ )
		{
			int i = (_i + offset) % (cross.height + cross.width);
			
			if( i < cross.height )
			{
				buffer = new BoardState[cross.width];
				int y = i;
				for( int x = 0; x < cross.width; x++ )
				{
					buffer[x] = cross.game.get( x, y );
				}
				
				play = applyWithExceptionMessage( rule, buffer, cross.image.yHints[y] );
				if( play != null ) 
				{
					rule.setLast( i );
					return new Play( new Vector2I( play.location, y ), play.state );
				}
			}
			else
			{
				buffer = new BoardState[cross.height];
				int x = i - cross.height;
				for( int y = 0; y < cross.height; y++ )
				{
					buffer[y] = cross.game.get( x, y );
				}
				
				play = applyWithExceptionMessage( rule, buffer, cross.image.xHints[x] );
				if( play != null ) 
				{
					rule.setLast( i );
					return new Play( new Vector2I( x, play.location ), play.state );
				}
			}
		}
		
		rule.setLast( 0 );
		
		/*
		buffer = new BoardState[cross.height];
		for( int x = 0; x < cross.width; x++ )
		{
			for( int y = 0; y < cross.height; y++ )
			{
				buffer[y] = cross.game.get( x, y );
			}
			
			play = applyWithExceptionMessage( rule, buffer, cross.image.xHints[x] );
			if( play != null ) 
			{
				return new Play( new Vector2I( x, play.location ), play.state );
			}
		}
		
		buffer = new BoardState[cross.width];
		for( int y = 0; y < cross.height; y++ )
		{
			for( int x = 0; x < cross.width; x++ )
			{
				buffer[x] = cross.game.get( x, y );
			}
			
			play = applyWithExceptionMessage( rule, buffer, cross.image.yHints[y] );
			if( play != null ) 
			{
				return new Play( new Vector2I( play.location, y ), play.state );
			}
		}
		*/
		
		return null;
	}
	
	public static class RowPlay
	{
		int location;
		BoardState state;
		
		public RowPlay()
		{
			
		}
		
		public RowPlay( int location, BoardState state )
		{
			this.location = location;
			this.state = state;
		}
	}
	
	public static class Play
	{
		Vector2I location;
		BoardState state;
		
		public Play()
		{
			
		}
		
		public Play( Vector2I location, BoardState state )
		{
			this.location = location;
			this.state = state;
		}
		
		@Override
		public String toString()
		{
			return location + ":" + state;
		}
	}
}
