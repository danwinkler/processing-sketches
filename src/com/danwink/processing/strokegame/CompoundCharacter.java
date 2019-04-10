package com.danwink.processing.strokegame;

public class CompoundCharacter extends Character
{
	Direction dir;
	Character a, b;
	
	public CompoundCharacter( Character a, Character b )
	{
		this.a = a;
		this.b = b;
		
		if( a instanceof CompoundCharacter )
		{
			CompoundCharacter ca = (CompoundCharacter)a;
			dir = ca.dir.opposite();
		}
		else if( b instanceof CompoundCharacter )
		{
			CompoundCharacter cb = (CompoundCharacter)b;
			dir = cb.dir.opposite();
		}
		else 
		{
			dir = Direction.HORIZONTAL;
		}
	}
	
	public enum Direction {
		HORIZONTAL,
		VERTICAL;
		
		public Direction opposite()
		{
			return this == HORIZONTAL ? VERTICAL : HORIZONTAL;
		}
	}
}
