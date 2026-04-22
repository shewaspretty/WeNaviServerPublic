package com.hrlee.transnaviserver.springboot.service.route.alpha.its;

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

import java.io.File;
import java.util.HashMap;

public class ITSRealtimeVelocityNodeReader {

    private void readTest() throws Exception {
        ReferencingFactoryFinder.scanForPlugins();
        HashMap<String, Object> dataStoreMap = new HashMap<>();
        dataStoreMap.put("url", new File("/Users/michaellee/Desktop/nodeLinkData/MOCT_NODE.dbf").toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(dataStoreMap);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();

        int cnt = 0;
        try (FeatureIterator<SimpleFeature> iterator = collection.features()) {
            SimpleFeature it = null;
            while (iterator.hasNext()) {
                it = iterator.next();
                //CRS.decode("EPSG:3857");
                //System.out.println(a);
                cnt++;

                //if(!(String)it.getAttribute("ROAD_NAME").equals("한누리대로"))
                //    continue;

                //if(!it.getAttribute("ROAD_NAME").equals("미리내로"))
                //   continue;

                if (!it.getAttribute("NODE_ID").equals("4130671100"))
                    continue;

                MathTransform mathTransform = CRS.findMathTransform(it.getType().getCoordinateReferenceSystem(), CRS.decode("EPSG:4326"), true);
                Geometry a = JTS.transform((Geometry) it.getDefaultGeometry(), mathTransform);

                System.out.println("a");
            }
        } finally {
            dataStore.dispose();
        }
    }
}
