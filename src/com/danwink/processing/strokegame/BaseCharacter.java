package com.danwink.processing.strokegame;

public class BaseCharacter
{
	public static final int DIMENSIONS = 5;
	
	public static final int DEFAULT_STROKE_COUNT = 10;
	
	public Stroke[] strokes;
	
	public BaseCharacter()
	{
		this( DEFAULT_STROKE_COUNT );
	}
	
	public BaseCharacter( int strokeCount )
	{
		strokes = new Stroke[strokeCount];
	}
	
	public boolean addStroke( Stroke stroke )
	{
		for( int i = 0; i < strokes.length; i++ )
		{
			if( strokes[i] == null )
			{
				strokes[i] = stroke;
				return true;
			}
		}
		return false;
	}
	
	public static class Stroke
	{
		int x1, y1, x2, y2;
		
		public Stroke( int x1, int y1, int x2, int y2 )
		{
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
	}
}
