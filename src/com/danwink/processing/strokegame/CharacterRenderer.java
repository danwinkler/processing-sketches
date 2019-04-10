package com.danwink.processing.strokegame;

import com.danwink.processing.strokegame.CompoundCharacter.Direction;

import processing.core.PGraphics;
import processing.core.PVector;

public class CharacterRenderer
{
	BaseCharacterRenderer r;

	public CharacterRenderer()
	{
		r = new BaseCharacterRenderer();
	}

	public void render( PGraphics g, Character c, PVector pos, PVector dimensions, boolean debug )
	{
		float overlap = .05f;
		if( c instanceof CompoundCharacter )
		{
			CompoundCharacter cc = (CompoundCharacter)c;
			PVector aPos = new PVector( pos.x, pos.y );
			PVector bPos = new PVector( 
				pos.x + (cc.dir == Direction.HORIZONTAL ? dimensions.x * (.5f-overlap) : 0),
				pos.y + (cc.dir == Direction.VERTICAL ? dimensions.y * (.5f-overlap) : 0)
			);
			PVector charScale = new PVector(
				dimensions.x * (cc.dir == Direction.HORIZONTAL ? .5f + overlap : 1),
				dimensions.y * (cc.dir == Direction.VERTICAL ? .5f + overlap : 1)
			);
			
			g.pushMatrix();
			//g.translate( aPos.x, aPos.y );
			render( g, cc.a, aPos, charScale, debug );
			if( debug ) {
				g.pushStyle();
				g.fill( 0, 0 );
				g.color( 255, 0, 0 );
				g.rect( aPos.x, bPos.y, charScale.x, charScale.y );
				g.popStyle();
			}
			g.popMatrix();
			
			g.pushMatrix();
			//g.translate( bPos.x, bPos.y );
			render( g, cc.b, bPos, charScale, debug );
			if( debug ) {
				g.pushStyle();
				g.fill( 0, 0 );
				g.color( 255, 0, 0 );
				g.rect( bPos.x, bPos.y, charScale.x, charScale.y );
				g.popStyle();
			}
			g.popMatrix();
		}
		else if( c instanceof BaseCharacter )
		{
			r.render( g, (BaseCharacter)c, pos, dimensions, debug );
		}
	}
}
