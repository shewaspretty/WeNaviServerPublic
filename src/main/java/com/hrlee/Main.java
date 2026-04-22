package com.hrlee;

import com.hrlee.transnaviserver.springboot.LoggAble;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.FeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.MultiLineString;
import org.locationtech.jts.geom.Point;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.File;
import java.util.HashMap;
import java.util.Properties;

@SpringBootApplication
public class Main {
    public static void main(String[] args) throws Exception {
        LoggerFactory.getLogger(Main.class.getName()).info("Max Available Memory: " + Runtime.getRuntime().maxMemory() /1024/1024 + "mb");
        SpringApplication.run(Main.class, args);
        LoggerFactory.getLogger(Main.class.getName()).info("Max Available Memory: " + Runtime.getRuntime().maxMemory() /1024/1024 + "mb");
    }
}