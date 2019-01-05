package com.danwink.processing.picross;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Board
{
	int width, height;
	
	BoardState[][] board;
	
	int[][] xHints;
	int[][] yHints;
	
	public Board( int width, int height )
	{
		this.setSize( width, height );
	}
	
	public Board( BufferedImage image )
	{
		this.setSize( image.getWidth(), image.getHeight() );
		
		for( int x = 0; x < width; x++ )
		{
			for( int y = 0; y < height; y++ )
			{
				board[x][y] = BoardState.getFromInt( image.getRGB( x, y ) );
			}
		}
		
		generateHints();
	}
	
	public void setSize( int width, int height )
	{
		this.width = width;
		this.height = height;
		board = new BoardState[width][height];
	}
	
	public void fill( BoardState state )
	{
		for( int x = 0; x < width; x++ )
		{
			for( int y = 0; y < height; y++ )
			{
				board[x][y] = state;
			}
		}
		generateHints();
	}

	public BoardState get( int x, int y )
	{
		return board[x][y];
	}
	
	public void generateHints()
	{
		xHints = new int[width][];
		yHints = new int[height][];
		
		BoardState[] buffer = new BoardState[height];
		for( int x = 0; x < width; x++ )
		{
			for( int y = 0; y < height; y++ )
			{
				buffer[y] = get( x, y );
			}
			xHints[x] = getHintsForRow( buffer );
		}
		
		buffer = new BoardState[width];
		
		for( int y = 0; y < height; y++ )
		{
			for( int x = 0; x < width; x++ )
			{
				buffer[x] = get( x, y );
			}
			yHints[y] = getHintsForRow( buffer );
		}
	}
	
	private static int[] getHintsForRow( BoardState[] row )
	{
		ArrayList<Integer> hints = new ArrayList<Integer>();
		
		int currentValue = 0;
		
		for( int i = 0; i < row.length; i++ )
		{
			BoardState current = row[i];
			
			if( current == BoardState.ON )
			{
				currentValue++;
			}
			
			if( (current == BoardState.OFF && currentValue > 0) || i == row.length-1 )
			{
				if( currentValue != 0 || hints.size() == 0 )
				{
					hints.add( currentValue );
				}
				currentValue = 0;
			}
		}
		
		return hints.stream().mapToInt(i->i).toArray();
	}
	
	public int maxHintSizeX()
	{
		return maxHintSize( xHints );
	}
	
	public int maxHintSizeY()
	{
		return maxHintSize( yHints );
	}
	
	private int maxHintSize( int[][] hints )
	{
		int max = 0;
		for( int i = 0; i < hints.length; i++ )
		{
			max = Math.max( hints[i].length, max );
		}
		return max;
	}
}
