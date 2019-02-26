package com.danwink.processing.growthconvsurf;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

import org.joml.Vector2f;

import processing.core.PApplet;

public class Growth2D extends PApplet
{
	GrowthSim2D sim;
	
	public void settings()
	{
		size( 1200, 900 );
	}
	
	public void setup()
	{
		//frameRate( 6 );
		sim = new GrowthSim2D();
		sim.init();
	}
	
	public void draw()
	{
		sim.update();
		
		background( 0 );
		pushMatrix();
		scale( 4 );
		translate( 10, 10 );
		render( sim );
		popMatrix();
	}
	
	public void keyPressed()
	{
		if( key == 'r' )
		{
			sim.init();
		}
	}
	
	public void render( GrowthSim2D sim )
	{
		noStroke();
		fill( 200, 200, 100, 128 );
		for( PheromonePoint p : sim.pheromonePoints )
		{
			ellipse( p.p.x, p.p.y, p.r * 2, p.r * 2 );
		}
		
		noFill();
		stroke( 100, 255, 200 );
		for( Segment segment : sim.segments )
		{
			renderSegment( sim, segment );
		}
		
		stroke( 255, 255, 255 );
		rect( sim.minX, sim.minY, sim.maxX - sim.minX, sim.maxY - sim.minY );
	}
	
	public void renderSegment( GrowthSim2D sim, Segment segment )
	{
		stroke( segment.hit ? 200 : 100, 255, 200 );
		line( segment.p.x, segment.p.y, segment.p2.x, segment.p2.y );
	}
	
	public static void main( String[] args )
	{
		PApplet.main( Growth2D.class.getCanonicalName() );
	}
	
	public static float random( Random r, float min, float max )
	{
		return r.nextFloat() * (max - min) + min;
	}
	
	public static float bound( float v, float min, float max )
	{
		return v < min ? min : v > max ? max : v;
	}
	
	public static Vector2f lineLineIntersection(Vector2f p0, Vector2f p1, Vector2f p2, Vector2f p3 )
	{
		float s1_x, s1_y, s2_x, s2_y;
		s1_x = p1.x - p0.x;     
		s1_y = p1.y - p0.y;
		s2_x = p3.x - p2.x;
		s2_y = p3.y - p2.y;
		
		float s, t;
		s = (-s1_y * (p0.x - p2.x) + s1_x * (p0.y - p2.y)) / (-s2_x * s1_y + s1_x * s2_y);
		t = ( s2_x * (p0.y - p2.y) - s2_y * (p0.x - p2.x)) / (-s2_x * s1_y + s1_x * s2_y);
		
		if( s >= 0 && s <= 1 && t >= 0 && t <= 1 )
		{
		    // Collision detected
		    return new Vector2f( p0.x + (t * s1_x), p0.y + (t * s1_y) );
		}
		
		return null; // No collision
	}
	
	public static class GrowthSim2D
	{
		ArrayList<PheromonePoint> pheromonePoints;
		ArrayList<Segment> segments;
		ArrayList<Segment> newSegments;
		Random random = new Random();
		
		float minX = 0;
		float maxX = 200;
		float minY = 0;
		float maxY = 200;
		float pheromoneMinRadius = 7;
		float pheromoneMaxRadius = 7;
		float segmentLength = 3;
		float daVariance = .1f;
		float daMax = .2f;
		int minRoots = 2;
		int maxRoots = 10;
		
		public GrowthSim2D()
		{
			pheromonePoints = new ArrayList<>();
			segments = new ArrayList<>();
			newSegments = new ArrayList<>();
		}
		
		public void init()
		{
			pheromonePoints.clear();
			segments.clear();
			newSegments.clear();
			
			int rootCount = random.nextInt( maxRoots - minRoots ) + minRoots;
			
			for( int i = 0; i < rootCount; i++ )
			{
				float rootAngle = random( random, 0, (float)(Math.PI * 2) );
				Vector2f rp = new Vector2f( random( random, minX, maxX ), random( random, minY, maxY ) );
				Vector2f rv = new Vector2f( (float)Math.cos( rootAngle ) * segmentLength, (float)Math.sin( rootAngle ) * segmentLength ); 
				Segment root = new Segment( rp, rv );
				segments.add( root );
			}
			
			pheromoneSpawnLoop: 
			while( true )
			{
				pheromoneDistanceCheckLoop:
				for( int i = 0; i < 100; i++ )
				{
					Vector2f p = new Vector2f( random( random, minX, maxX ), random( random, minX, maxX ) );
					float r = random( random, pheromoneMinRadius, pheromoneMaxRadius );
					
					for( PheromonePoint pp : pheromonePoints )
					{
						if( pp.p.distanceSquared( p ) < (r + pp.r) * (r + pp.r) )
						{
							continue pheromoneDistanceCheckLoop;
						}
					}
					
					pheromonePoints.add( new PheromonePoint( p, r ) );
					continue pheromoneSpawnLoop;
				}
				break;
			}
		}
		
		public void update()
		{
			for( int i = 0; i < segments.size(); i++ )
			{
				Segment s = segments.get( i );
				updateSegment( s );
			}
			segments.addAll( newSegments );
			newSegments.clear();
		}
		
		public Segment intersectingSegment( Segment segment )
		{
			for( Segment other : segments )
			{
				Vector2f intersection = lineLineIntersection( segment.p, segment.p2, other.p, other.p2 );
				if( other != segment && other != segment.parent && other.parent != segment.parent && intersection != null )
				{
					return other;
				}
			}
			return null;
		}
		
		public void generateChild( Segment segment, float baseAngle )
		{
			Vector2f childPoint = new Vector2f( segment.p2 );
			
			float childDa = bound(
				segment.da + random( random, -daVariance, daVariance ),
				-daMax,
				daMax 
			);
			float childAngle = baseAngle + childDa;
			
			Vector2f childVector = new Vector2f(
				(float)Math.cos( childAngle ) * segmentLength,
				(float)Math.sin( childAngle ) * segmentLength
			);
			
			Segment child = new Segment( childPoint, childVector );
			child.da = childDa;
			child.parent = segment;
			child.depth = segment.depth + 1;
			child.connected.add( segment );
			segment.connected.add( child );
			newSegments.add( child );
			
			Segment intersecting = intersectingSegment( child );
			if( intersecting != null )
			{
				child.connected.add( intersecting );
				intersecting.connected.add( child );
				child.v.set( intersecting.p );
				child.v.sub( child.p );
				child.p2.set( child.p ).add( child.v );
				child.hit = true;
			}
		}
		
		public void updateSegment( Segment segment )
		{
			if( segment.canBranch() && inBounds( segment.p ) )
			{
				generateChild( segment, segment.angle() );
				if( segment.parent != null )
				{
					for( PheromonePoint pp : pheromonePoints )
					{
						if( !pp.inPoint( segment.parent.p ) && pp.inPoint( segment.p ) )
						{
							float angle = (float)Math.atan2( pp.p.y - segment.p.y, pp.p.x - segment.p.x );
							generateChild( segment, angle );
							break;
						}
					}
				}
			}
		}
		
		public boolean inBounds( Vector2f p )
		{
			return p.x > minX && p.x < maxX && p.y > minY && p.y < maxY;
		}
	}
	
	public static class Segment
	{
		Segment parent;
		ArrayList<Segment> connected;
		
		Vector2f p;
		Vector2f v;
		Vector2f p2;
		float da; // First derivative of angle
		boolean hit = false;
		int depth = 0;
		
		public Segment( Vector2f p, Vector2f v )
		{
			this.p = p;
			this.v = v;
			
			this.p2 = new Vector2f( p );
			this.p2.add( v );
			
			connected = new ArrayList<>();
		}
		
		public boolean canBranch()
		{
			return connected.size() <= (depth == 0 ? 0 : 1);
		}

		public float angle()
		{
			return (float)Math.atan2( v.y, v.x );
		}
	}
	
	public static class PheromonePoint
	{
		Vector2f p;
		float r;
		
		public PheromonePoint( Vector2f p, float r )
		{
			this.p = p;
			this.r = r;
		}
		
		public boolean inPoint( Vector2f p )
		{
			return this.p.distanceSquared( p ) < r * r;
		}
	}
}
