package com.danwink.processing.picross;

public enum BoardState {
	OFF,
	ON,
	UNKNOWN;
	
	public static BoardState getFromInt( int v )
	{
		int r = (v >> 16) & 0xff; // Use R as a proxy for BLACK and white
		return r == 0 ? ON : OFF;
	}
}