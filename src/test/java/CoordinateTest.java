import com.hrlee.transnaviserver.springboot.osm.coordinate.Coordinate;
import com.hrlee.transnaviserver.springboot.osm.coordinate.CoordinateTool;
import org.junit.jupiter.api.Test;

public class CoordinateTest {

    private final CoordinateTool coordinateTool = CoordinateTool.getInstance();
    @Test
    public void azimuthTest() {

        System.out.println(coordinateTool.getAzimuth(new Coordinate(36.4967701, 127.3040147), new Coordinate(36.4966822, 127.3042930)));
    }
}
