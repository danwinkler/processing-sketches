module processingsketches {
	exports com.danwink.processing.conveyor;
	exports com.danwink.processing.convsurfapp;
	exports com.danwink.processing.strokegame;
	exports com.danwink.processing;
	exports com.danwink.processing.picross;
	exports com.danwink.processing.growthconvsurf;
	exports com.danwink.processing.triangleconvtest;

	requires core;
	requires java.desktop;
	requires junit;
	requires org.junit.jupiter.api;
	requires ConvolutionSurface;
	requires org.joml;
	requires peasycam;
}