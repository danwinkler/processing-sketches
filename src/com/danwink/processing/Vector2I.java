package com.danwink.processing;

public class Vector2I
{
	public int x, y;
	
	public Vector2I()
	{
		
	}
	
	public Vector2I( int x, int y )
	{
		this.x = x;
		this.y = y;
	}
	
	@Override
	public String toString()
	{
		return String.format( "(%d, %d)", x, y );
	}
}
