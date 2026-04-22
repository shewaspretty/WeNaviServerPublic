import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.osm.coordinate.CoordinateTool;
import com.hrlee.transnaviserver.springboot.service.route.RouteService;
import org.geotools.api.data.DataStore;
import org.geotools.api.data.DataStoreFinder;
import org.geotools.api.data.FeatureSource;
import org.geotools.api.feature.simple.SimpleFeature;
import org.geotools.api.feature.simple.SimpleFeatureType;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public final class RouteTest {

    /*@Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private RouteService routeService;

    @BeforeEach
    public void beforeEach() throws Exception {
        AutoCloseable autoCloseable = null;
        try {
            autoCloseable = MockitoAnnotations.openMocks(this);
        } finally {
            if(autoCloseable == null)
                return;
            autoCloseable.close();;
        }
    }*/

    @Test
    public void test() {
        ArrayList<Integer> a  = new ArrayList<>();
        a.add(1);

    }

    @Test
    public void distanceTest() {
        CoordinateTool coordinateTool = CoordinateTool.getInstance();
        Coordinate coordinate1 = new Coordinate(36.5014204, 127.32556050000001);
        Coordinate coordinate2 = new Coordinate(36.5014095, 127.3255287);

        long mil = System.currentTimeMillis();
        double azimuth = coordinateTool.getAzimuth(coordinate1, coordinate2);
        System.out.println(mil - System.currentTimeMillis());
        mil = System.currentTimeMillis();
        coordinateTool.getDistanceMeter(coordinate1,coordinate2);
        System.out.println(mil-System.currentTimeMillis());

    }

    @Test
    public void shapeFileTest() throws IOException {
        ReferencingFactoryFinder.scanForPlugins();
        HashMap<String, Object> dataStoreMap = new HashMap<>();
        dataStoreMap.put("url", new File("/Users/michaellee/Desktop/nodeLinkData/MOCT_LINK.dbf").toURI().toURL());

        DataStore dataStore = DataStoreFinder.getDataStore(dataStoreMap);
        String typeName = dataStore.getTypeNames()[0];

        FeatureSource<SimpleFeatureType, SimpleFeature> source = dataStore.getFeatureSource(typeName);
        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = source.getFeatures();

        try(FeatureIterator<SimpleFeature> iterator = collection.features()) {
            SimpleFeature it = null;
            while(iterator.hasNext()) {
                it = iterator.next();
                //CRS.findMathTransform(it.getType().getCoordinateReferenceSystem(), CRS.decode("EPSG:3857"));
                //CRS.decode("EPSG:3857");
                System.out.println(it);
            }
        } finally {
            dataStore.dispose();
        }


    }


}
