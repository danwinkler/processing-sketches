package com.danwink.processing.strokegame;

import com.danwink.processing.Vector2I;

public class BaseCharacter extends Character
{
	public static final int DIMENSIONS = 5;
	
	public static final int DEFAULT_STROKE_COUNT = 10;
	
	public String name;
	public Stroke[] strokes;
	
	public BaseCharacter()
	{
		this( "", DEFAULT_STROKE_COUNT );
	}
	
	public BaseCharacter( String name, int strokeCount )
	{
		this.name = name;
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
		Vector2I start;
		Vector2I end;
		
		public Stroke( int x1, int y1, int x2, int y2 )
		{
			start = new Vector2I( x1, y1 );
			end = new Vector2I( x2, y2 );
		}
	}
}
