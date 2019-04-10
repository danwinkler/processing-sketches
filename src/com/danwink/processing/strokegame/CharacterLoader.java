package com.danwink.processing.strokegame;

import processing.core.PApplet;
import processing.data.JSONArray;
import processing.data.JSONObject;

public class CharacterLoader
{
	public static BaseCharacter[] loadCharacter( PApplet applet, String filename )
	{
		JSONObject o = applet.loadJSONObject( filename );
		
		JSONArray charactersJSON = o.getJSONArray( "characters" );
		
		BaseCharacter[] characters = new BaseCharacter[charactersJSON.size()];
		
		for( int i = 0; i < characters.length; i++ )
		{
			JSONObject charJSON = charactersJSON.getJSONObject( i );
			JSONArray strokes = charJSON.getJSONArray( "strokes" );
			BaseCharacter chara = new BaseCharacter( charJSON.getString( "name" ), strokes.size() );
			for( int j = 0; j < strokes.size(); j++ )
			{
				int[] stroke = strokes.getJSONArray( j ).getIntArray();
				chara.strokes[j] = new BaseCharacter.Stroke( stroke[0], stroke[1], stroke[2], stroke[3] );
			}
			characters[i] = chara;
		}
		
		return characters;
	}
}
