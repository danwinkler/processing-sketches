package com.danwink.processing.convsurfapp;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.util.ArrayList;

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
import processing.data.JSONArray;
import processing.data.JSONObject;

public class App extends PApplet
{
	public static String[] args;

	ModelGenerator g;
	PeasyCam cam;
	
	public void settings()
	{
		size( 1600, 900, P3D );
	}
	
	public void loadModel()
	{
		g = new ModelGenerator();
		new Thread(g).start();
	}
	
	public void setup()
	{
		frameRate(60);
		surface.setResizable( true );
		loadModel();
	}
	
	public void draw()
	{
		background(0);
		
		if( g.shape == null )
		{
			// Show progress bar
			stroke(255);
			noFill();
			rect(50, height/2 - 50, width - 100, 100);
			noStroke();
			fill(255);
			rect(50, height/2 - 50, (width-100)*g.getProgress(), 100);
		}
		else
		{
			shape( g.shape );
		}
	}
	
	public void keyPressed()
	{
		if( key == 's' && g.shape != null )
		{
			selectOutput( "Choose file", "saveFileSelected" );
		}
	}
	
	public void saveFileSelected(File file)
	{
		try
		{
			Files.write( file.toPath(), g.getSTL().getBytes() );
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}
	
	public static void main( String[] args )
	{
		App.args = args;
		PApplet.main(MethodHandles.lookup().lookupClass().getCanonicalName());
	}
	
	public class ModelGenerator implements Runnable
	{
		FieldGenerator fg;
		PShape shape;
		ArrayList<Triangle> tris;
		
		public Vector getVectorFromJSONArray( JSONArray jsonVec )
		{
			return new Vector( jsonVec.getFloat( 0 ), jsonVec.getFloat( 1 ), jsonVec.getFloat( 2 ) );
		}
		
		public void run()
		{
			try 
			{
				JSONObject top = loadJSONObject(args[0]);
				
				float margin = top.getFloat( "margins", 0 );
				float res = top.getFloat( "resolution" );
				
				ArrayList<Primitive> prims = new ArrayList<>();
				
				JSONArray jsonLines = top.getJSONArray( "lines" );
				for( int i = 0; i < jsonLines.size(); i++ )
				{
					JSONArray jsonLine = jsonLines.getJSONArray( i );
					JSONArray jsonP0 = jsonLine.getJSONArray( 0 );
					JSONArray jsonP1 = jsonLine.getJSONArray( 1 );
					float scalar = jsonLine.getFloat( 2 );
					Vector p0 = getVectorFromJSONArray( jsonP0 );
					Vector p1 = getVectorFromJSONArray( jsonP1 );
					
					prims.add( new LineSegment( p0, p1, scalar ) );
				}
				
				Bounds b = Bounds.generateFromPrimitiveList( prims );
				
				b.min.x -= margin;
				b.min.y -= margin;
				b.min.z -= margin;
				
				b.max.x += margin;
				b.max.y += margin;
				b.max.z += margin;
				
				fg = new FieldGenerator( b.min, b.max, res );
				fg.setPrimitives( prims );
				Field field = fg.generate();
				
				MarchingCubesPolygonizer mcp = new MarchingCubesPolygonizer();
				tris = mcp.polygonize( field, .1f );
				
				PShape tmpShape = createShape(); 
				tmpShape.beginShape( TRIANGLES );
				tmpShape.stroke( 100 );
				//tmpShape.noStroke();
				tmpShape.fill( 255 );
				
				for( Triangle tri : tris )
				{
					tmpShape.vertex( tri.a.x, tri.a.y, tri.a.z );
					tmpShape.vertex( tri.b.x, tri.b.y, tri.b.z );
					tmpShape.vertex( tri.c.x, tri.c.y, tri.c.z );
				}
				tmpShape.endShape();
				
				shape = tmpShape;
				
				// Set up camera at end
				cam = new PeasyCam(App.this, 100);
				float maxSize = max( b.max.x - b.min.x, b.max.y - b.min.y, b.max.z - b.min.z );
				float near = maxSize / 100.f;
				cam.setMinimumDistance( near );
				cam.setMaximumDistance( maxSize);
				perspective(80, (float)width/(float)height, near, maxSize*2 );
			} catch( Exception e )
			{
				e.printStackTrace();
				System.exit( 1 );
			}
		}
		
		public float getProgress()
		{
			if( fg == null ) return 0;
			return fg.getProgress();
		}
		
		public String getSTL()
		{
			StringBuilder sb = new StringBuilder();
			
			String name = "thing";
			
			sb.append( String.format( "solid %s\n", name ) );
			
			for( Triangle t : tris )
			{
				Vector norm = t.getNormal();
				sb.append( String.format( "facet normal %f %f %f\n", norm.x, norm.y, norm.z ) );
				sb.append( "outer loop\n" );
				sb.append( String.format( "vertex %f %f %f\n", t.a.x, t.a.y, t.a.z ) );
				sb.append( String.format( "vertex %f %f %f\n", t.b.x, t.b.y, t.b.z ) );
				sb.append( String.format( "vertex %f %f %f\n", t.c.x, t.c.y, t.c.z ) );
				sb.append( "endloop\n" );
				sb.append( "endfacet\n" );
			}
			
			sb.append( String.format( "endsolid %s\n", name ) );
			
			return sb.toString();
		}
		
		public PShape getPShape()
		{
			return shape;
		}
	}
}
