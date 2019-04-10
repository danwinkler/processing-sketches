package com.danwink.processing.strokegame;

import com.danwink.processing.Vector2I;

import processing.core.PGraphics;
import processing.core.PVector;

public class BaseCharacterRenderer
{	
	public void render( PGraphics g, BaseCharacter c, PVector pos, PVector dimensions, boolean debug )
	{
		g.pushStyle();
		for( BaseCharacter.Stroke stroke : c.strokes )
		{
			if( stroke != null )
			{
				PVector start = intSpaceToDrawSpace( stroke.start, pos, dimensions );
				PVector end = intSpaceToDrawSpace( stroke.end, pos, dimensions );
				
				// Extend start and end slightly longer
				PVector dir = PVector.sub( end, start );
				float length = dir.mag();
				dir.div( length ); // normalize
				dir.mult( length * .1f );
				//start.sub( dir );
				end.add( dir );
				
				PVector cp1 = buildStrokeControlPoint( start, end, false );
				PVector cp2 = buildStrokeControlPoint( end, start, true );
				
				g.strokeWeight( 8 );
				g.color( 0 );
				g.fill( 0, 0 );				
				g.bezier( start.x, start.y, cp1.x, cp1.y, cp2.x, cp2.y, end.x, end.y );
				
				if( debug )
				{
					g.color( 0, 0 );
					g.fill( 255, 0, 0 );
					g.ellipse( start.x, start.y, 10, 10 );
					g.ellipse( end.x, end.y, 10, 10 );
					
					
					g.fill( 0, 255, 0 );
					g.ellipse( cp1.x, cp1.y, 10, 10 );
					g.ellipse( cp2.x, cp2.y, 10, 10 );
				}
				
			}
		}
		g.popStyle();
	}
	
	public static PVector buildStrokeControlPoint( PVector a, PVector b, boolean switched )
	{
		PVector cp = PVector.sub( b, a );
		cp.mult( .3f );
		cp.rotate( .3f );
		cp.add( a );
		return cp;
	}
	
	public static PVector intSpaceToDrawSpace( Vector2I intSpace, PVector pos, PVector dimensions )
	{
		return new PVector( 
			pos.x + (intSpace.x + .5f) * (dimensions.x/BaseCharacter.DIMENSIONS),
			pos.y + (intSpace.y + .5f) * (dimensions.y/BaseCharacter.DIMENSIONS)
		);
	}
}
