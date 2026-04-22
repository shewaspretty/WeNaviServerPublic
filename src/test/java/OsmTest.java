import de.topobyte.osm4j.core.access.OsmIterator;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import de.topobyte.osm4j.pbf.seq.PbfParser;
import de.topobyte.osm4j.pbf.seq.PbfReader;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class OsmTest {

    @AllArgsConstructor
    @Getter
    private static final class Counter {
        private int value;

        public void increase() { value++; }
    }

    @Test
    public void test() {
        int[] arr = new int[3];
    }

    @Test
    public void pbfTest() throws FileNotFoundException {
        File f = new File("/home/michaellee/Downloads", "south-korea-251027.osm.pbf");
        FileInputStream in = new FileInputStream(f);
        PbfIterator pbfIt = new PbfIterator(in, false);
        EntityContainer it = null;

        String typeTmp = null;
        HashMap<String, Counter> map = new HashMap<>();
        while(pbfIt.hasNext()) {
            it = pbfIt.next();
            //if(it.getType().name().equals("Way"))
            if(it.getEntity() instanceof Node && it.getEntity().getNumberOfTags() > 0)
                continue;
           /* String currentType = it.getType().name();

            Counter currentTypeCnt = map.get(currentType);
            if(currentTypeCnt == null) {
                map.put(currentType, new Counter(1));
                continue;
            }
            currentTypeCnt.increase();*/
        }

        Iterator<Map.Entry<String, Counter>> mapIt = map.entrySet().iterator();
        while(mapIt.hasNext()) {
            Map.Entry<String, Counter> entryIt = mapIt.next();
            System.out.println(entryIt.getKey() + " " + entryIt.getValue().value);
        }
    }
}
