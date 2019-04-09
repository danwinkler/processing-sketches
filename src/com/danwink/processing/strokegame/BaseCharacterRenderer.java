package com.danwink.processing.strokegame;

import processing.core.PGraphics;

public class BaseCharacterRenderer
{
	public void render( PGraphics g, BaseCharacter c, float scale )
	{
		for( BaseCharacter.Stroke stroke : c.strokes )
		{
			if( stroke != null )
			{
				g.line(
					(stroke.x1 + .5f) * scale,
					(stroke.y1 + .5f) * scale,
					(stroke.x2 + .5f) * scale,
					(stroke.y2 + .5f) * scale
				);
			}
		}
	}
}
