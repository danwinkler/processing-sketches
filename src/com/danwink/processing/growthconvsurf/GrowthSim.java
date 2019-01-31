package com.danwink.processing.growthconvsurf;

import java.util.ArrayList;
import java.util.List;

import com.danwink.convolutionsurface.Bounds;
import com.danwink.convolutionsurface.Field;
import com.danwink.convolutionsurface.FieldGenerator;
import com.danwink.convolutionsurface.MarchingCubesPolygonizer;
import com.danwink.convolutionsurface.Vector;
import com.danwink.convolutionsurface.primitive.LineSegment;
import com.danwink.convolutionsurface.primitive.Primitive;
import com.danwink.convolutionsurface.primitive.Triangle;

import peasy.PeasyCam;
import processing.core.PApplet;
import processing.core.PShape;

public class GrowthSim extends PApplet
{
	public static final int WIDTH = 1600, HEIGHT = 900;
	
	PeasyCam cam;
	
	List<Primitive> lines;
	List<Triangle> tris;
	
	PShape shape;
	
	public void settings()
	{
		size( WIDTH, HEIGHT, P3D );
	}
	
	public void setup()
	{	
		cam = new PeasyCam(this, 100);
		cam.setMinimumDistance( 1 );
		cam.setMaximumDistance( 50 );
		perspective(80, (float)width/(float)height, .1f, 100f );
		
		Growth g = new Growth();
		lines = g.generate();
		
		for( Primitive p : lines )
		{
			LineSegment l = (LineSegment)p;
			l.setP0( map( l.getP0() ) );
			l.setP1( map( l.getP1() ) );
		}
		
		System.out.println( lines.size() );
		
		Bounds b = Bounds.generateFromPrimitiveList( lines );
		float margin = 3;
		b.min.x -= margin;
		b.min.y -= margin;
		b.min.z -= margin;
		
		b.max.x += margin;
		b.max.y += margin;
		b.max.z += margin;
		
		cam.lookAt( (b.min.x + b.max.x) * .5f, (b.min.y + b.max.y) * .5f, (b.min.z + b.max.z) * .5f );
		
		System.out.println( b.min + ", " + b.max );
		
		Field field = FieldGenerator.getField( lines, b.min, b.max, .15f );
		
		MarchingCubesPolygonizer mcp = new MarchingCubesPolygonizer();
		tris = mcp.polygonize( field, .1f );
		
		System.out.println( tris.size() );
		
		shape = createShape();
		shape.beginShape( TRIANGLES );
		shape.stroke( 100 );
		//shape.noStroke();
		shape.fill( 255 );
		
		for( Triangle tri : tris )
		{
			shape.vertex( tri.a.x, tri.a.y, tri.a.z );
			shape.vertex( tri.b.x, tri.b.y, tri.b.z );
			shape.vertex( tri.c.x, tri.c.y, tri.c.z );
		}
		shape.endShape();
		
		System.out.println( "Done" );
	}
	
	public Vector map( Vector v )
	{
		float xScale = .3f;
		Vector r = new Vector(
			cos( v.x * xScale ) * 5,
			sin( v.x * xScale ) * 5,
			v.y
		);
		return r;
	}
	
	public void draw()
	{
		background(0);
		pushMatrix();
		noStroke();
		lights();
		
		/*
		for( Primitive p : lines )
		{
			LineSegment l = LineSegment.class.cast( p );
			Vector p0 = l.getP0();
			Vector p1 = l.getP1();
			line( p0.x, p0.y, p1.x, p1.y );
		}
		*/
		
		shape( shape );
		
		popMatrix();
	}
	
	public static void main( String[] args )
	{
		PApplet.main(GrowthSim.class.getCanonicalName());
	}
}
