package com.danwink.processing.picross;

public class Picross
{
	public int width, height;
	
	Board image; // Represents the final image board
	Board game; // Represents the in progress gameplay board
	
	public Picross()
	{
		
	}

	public void newGame( Board board )
	{
		image = board;
		
		width = image.width;
		height = image.height;
		
		game = new Board( image.width, image.height );
		game.fill( BoardState.UNKNOWN );
	}
}
