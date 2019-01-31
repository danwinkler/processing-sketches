package com.danwink.processing.growthconvsurf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.danwink.convolutionsurface.Vector;
import com.danwink.convolutionsurface.primitive.LineSegment;
import com.danwink.convolutionsurface.primitive.Primitive;

public class Growth
{
	float lineScalar = 4;
	
	Random random = new Random(5);
	
	public List<Primitive> generate()
	{
		Segment root = new Segment( new LineSegment( new Vector( 0, 0, 0 ), new Vector( 0, 1, 0 ), lineScalar ), null );
		
		root.line.normalize();
		
		generate( root, 0 );
		
		ArrayList<Primitive> lines = new ArrayList<>();
			
		generateLineSegments( root, lines );
		
		return lines;
	}
	
	private void generate( Segment parent, int depth )
	{
		if( depth > 20 ) return;
		
		int numChildren = 1;
		if( random.nextFloat() > .8 )
		{
			numChildren++;
		}
		
		for( int i = 0; i < numChildren; i++ )
		{
			Vector v = new Vector( parent.line.getVector() );
			
			if( i > 0 ) {
				v = v.cross( new Vector( 0, 0, 1 ) );
				v.normalize();
			}
			
			
			v.x += (random.nextFloat() - .5f);
			v.y += (random.nextFloat() - .5f);
			//v.z += (random.nextFloat() - .5f);
			
			v.normalize();
			
			v.add( parent.line.getP1() );
			
			Segment child = new Segment( new LineSegment( parent.line.getP1(), v, lineScalar ), parent );
			parent.children.add( child );
			generate( child, depth+1 );
		}
	}
	
	private void generateLineSegments( Segment root, ArrayList<Primitive> lines )
	{
		lines.add( root.line );
		for( Segment child : root.children )
		{
			generateLineSegments( child, lines );
		}
	}
	
	public static class Segment
	{
		LineSegment line;
		Segment parent;
		List<Segment> children = new LinkedList<Segment>(); 
		
		public Segment()
		{
		
		}
		
		public Segment( LineSegment line, Segment parent )
		{
			this.line = line;
			this.parent = parent;
		}
	}
}
