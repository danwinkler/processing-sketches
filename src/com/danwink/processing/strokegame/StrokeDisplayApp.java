package com.danwink.processing.strokegame;

import processing.core.PApplet;
import processing.core.PVector;

public class StrokeDisplayApp extends PApplet
{
	int charsPerRow = 10;
	float characterScale = 200;
	
	CharacterRenderer cr;
	BaseCharacter[] chars;
	CompoundCharacter cc;
	
	public void settings()
	{
		size( 1600, 900 );
	}
	
	public void setup()
	{
		cr = new CharacterRenderer();
		chars = CharacterLoader.loadCharacter( this, "assets/strokegame/base.json" );
		
		cc = new CompoundCharacter( chars[0], new CompoundCharacter( chars[1], chars[2] ) );
	}
	
	public void draw()
	{
		background(255);
		
		for( int i = 0; i < chars.length; i++ )
		{
			float x = (i % charsPerRow) * characterScale;
			float y = (i / charsPerRow) * characterScale;
			PVector p = new PVector( x, y );
			PVector dim = new PVector( characterScale, characterScale );
			
			pushMatrix();
			cr.render( g, chars[i], p, dim, false );
			popMatrix();
		}
		
		pushMatrix();
		fill( 0, 0 );
		rect( 100, 300, 300, 300 );
		cr.render( g, cc, new PVector( 100, 300 ), new PVector( 300, 300 ), false );
		popMatrix();
	}
	
	public static void main( String[] args )
	{
		PApplet.main( StrokeDisplayApp.class.getCanonicalName() );
	}
}
