package com.danwink.processing.triangleconvtest;

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

public class TriangleTest extends PApplet
{
	public static final int WIDTH = 1600, HEIGHT = 900;
	
	PeasyCam cam;
	
	List<Primitive> prims;
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
		
		prims = new ArrayList<>();
		prims.add(new Triangle(new Vector(1, 1, 1), new Vector(10, 1, 1), new Vector(1, 10, 1), 1));
		
		Bounds b = Bounds.generateFromPrimitiveList( prims );
		float margin = 3;
		b.min.x -= margin;
		b.min.y -= margin;
		b.min.z -= margin;
		
		b.max.x += margin;
		b.max.y += margin;
		b.max.z += margin;
		
		cam.lookAt( (b.min.x + b.max.x) * .5f, (b.min.y + b.max.y) * .5f, (b.min.z + b.max.z) * .5f );
		
		System.out.println( b.min + ", " + b.max );
		
		Field field = FieldGenerator.getField( prims, b.min, b.max, .15f );
		
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
			shape.vertex( tri.p0.x, tri.p0.y, tri.p0.z );
			shape.vertex( tri.p1.x, tri.p1.y, tri.p1.z );
			shape.vertex( tri.p2.x, tri.p2.y, tri.p2.z );
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
		//noStroke();
		stroke(255);
		lights();
		
		
		for( Primitive p : prims )
		{
			Triangle t = Triangle.class.cast( p );
			Vector p0 = t.p0;
			Vector p1 = t.p1;
			Vector p2 = t.p2;
			line( p0.x, p0.y, p0.z, p1.x, p1.y, p1.z );
			line( p2.x, p2.y, p2.z, p1.x, p1.y, p1.z );
			line( p0.x, p0.y, p0.z, p2.x, p2.y, p2.z );
		}
		
		
		shape( shape );
		
		popMatrix();
	}
	
	public static void main( String[] args )
	{
		PApplet.main(TriangleTest.class.getCanonicalName());
	}
}
