package com.danwink.processing.strokegame;

import processing.core.PApplet;

public class StrokeDrawApp extends PApplet
{
	float characterScale = 100;
	
	boolean drawingLine = false;
	int xLineStart, yLineStart;
	
	BaseCharacter c;
	BaseCharacterRenderer cr;
	
	public void settings()
	{
		size( 1600, 900 );
	}
	
	public void setup()
	{
		c = new BaseCharacter();
		cr = new BaseCharacterRenderer();
	}
	
	public void draw()
	{
		background(255);
		
		for( int i = 0; i <= BaseCharacter.DIMENSIONS; i++ )
		{
			line( i * characterScale, 0, i * characterScale, BaseCharacter.DIMENSIONS * characterScale );
			line( 0, i * characterScale, BaseCharacter.DIMENSIONS * characterScale, i * characterScale );
		}
		cr.render( g, c, characterScale );
		
		if( mousePressed )
		{
			line( (xLineStart+.5f) * characterScale, (yLineStart+.5f) * characterScale, mouseX, mouseY );
		}
		
		if( key == 'r' )
		{
			setup();
		}
	}
	
	@Override
	public void mousePressed()
	{
		xLineStart = (int)(mouseX / characterScale);
		yLineStart = (int)(mouseY / characterScale);
		if( xLineStart >= 0 && xLineStart < BaseCharacter.DIMENSIONS && yLineStart >= 0 && yLineStart < BaseCharacter.DIMENSIONS )
		{
			drawingLine = true;
		}
	}
	
	@Override
	public void mouseReleased()
	{
		if( drawingLine )
		{
			drawingLine = false;
			int xLineEnd = (int)(mouseX / characterScale);
			int yLineEnd = (int)(mouseY / characterScale); 
			if( xLineEnd >= 0 && xLineEnd < BaseCharacter.DIMENSIONS && yLineEnd >= 0 && yLineEnd < BaseCharacter.DIMENSIONS )
			{
				c.addStroke( new BaseCharacter.Stroke( xLineStart, yLineStart, xLineEnd, yLineEnd ) );
			}
		}
	}
	
	
	public static void main( String[] args )
	{
		PApplet.main( StrokeDrawApp.class.getCanonicalName() );
	}
}
