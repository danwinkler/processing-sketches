package com.danwink.processing.picross;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import processing.core.*;

public class AutoSolveSketch extends PApplet
{
	public static final int WIDTH = 1600, HEIGHT = 1200;
	
	Picross picross;
	Solver solver;
	
	public void settings()
	{
		size( WIDTH, HEIGHT );
	}
	
	public void setup()
	{	
		picross = new Picross();
		solver = new Solver();
		
		BufferedImage img = null;
		try
		{
			img = ImageIO.read( new File( "assets/picross/test1.png" ) );
		}
		catch( IOException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Board board = new Board( img );
		
		picross.newGame( board ); 
	}
	
	public void draw()
	{
		if( frameCount % 1 == 0 )
		{
			solver.solve( picross );
		}
		
		int scale = 30;
		pushMatrix();
		drawBoard( picross.image, scale, picross.image );
		translate( (picross.width + picross.image.maxHintSizeX()) * scale, 0 );
		drawBoard( picross.game, scale, picross.image );
		popMatrix();
	}
	
	public void drawBoard( Board board, int scale, Board hintBoard )
	{
		int xHintSize = hintBoard.maxHintSizeX();
		int yHintSize = hintBoard.maxHintSizeY();
		
		int boardX = xHintSize * scale, boardY = yHintSize * scale; 
		
		// TODO: DRY out this hint logic
		for( int x = 0; x < hintBoard.xHints.length; x++ )
		{
			int[] hints = hintBoard.xHints[x];
			for( int y = 0; y < hints.length; y++ )
			{
				text( 
					String.valueOf( hints[hints.length-y-1] ),
					x * scale + boardX + (scale / 2) - 5,
					boardY - y * scale - (scale / 2) + 3
				);
			}
		}
		
		for( int y = 0; y < hintBoard.yHints.length; y++ )
		{
			int[] hints = hintBoard.yHints[y];
			for( int x = 0; x < hints.length; x++ )
			{
				text( 
					String.valueOf( hints[hints.length-x-1] ),
					boardX - x * scale - (scale / 2) - 5,
					boardX + y * scale + (scale / 2) + 3
				);
			}
		}
		
		pushMatrix();
		translate( boardX, boardY );
		for( int x = 0; x < board.width; x++ )
		{
			for( int y = 0; y < board.height; y++ )
			{
				int x1 = x * scale, y1 = y * scale, x2 = (x+1) * scale, y2 = (y+1) * scale;
				switch( board.get( x, y ) )
				{
					case OFF:
						fill( 255 );
						stroke( 0 );
						rect( x1, y1, scale, scale );
						stroke( 0 );
						line( x1, y1, x2, y2 );
						line( x1, y2, x2, y1 );
						break;
					case ON:
						fill( 0 );
						stroke( 255 );
						rect( x1, y1, scale, scale );
						break;
					case UNKNOWN:
						stroke( 0 );
						fill( 255 );
						rect( x1, y1, scale, scale );
						break;
					default:
						break;
				}
			}
		}
		popMatrix();
	}
	
	public static void main( String[] args )
	{
		PApplet.main(AutoSolveSketch.class.getCanonicalName());
	}
}
