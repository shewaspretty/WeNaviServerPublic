package com.hrlee.transnaviserver.springboot.osm;

import com.hrlee.transnaviserver.springboot.LoggAble;
import com.hrlee.transnaviserver.springboot.security.AuthorityHolder;
import com.hrlee.transnaviserver.springboot.util.Utils;
import de.topobyte.osm4j.core.model.iface.EntityContainer;
import de.topobyte.osm4j.core.model.iface.OsmTag;
import de.topobyte.osm4j.core.model.impl.Node;
import de.topobyte.osm4j.core.model.impl.Way;
import de.topobyte.osm4j.pbf.seq.PbfIterator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

@SuppressWarnings("FieldCanBeLocal")
@Component
@RequiredArgsConstructor
@EnableAsync(proxyTargetClass = true)
public class OsmDataConfigurer implements LoggAble {

    private static final String URL_OSM_FULL_PBF_DOWNLOAD = "https://download.geofabrik.de/asia/south-korea-latest.osm.pbf";
    private static final String OSM_DATA_FILE_PATH = "/opt/naviServer/south-korea-latest.osm.pbf";

    private final JdbcTemplate jdbcTemplate;

    private boolean lock = false;

    private synchronized boolean getLock() {
        if(lock)
            return false;
        lock = true;
        return true;
    }

    private synchronized void releaseLock() {
        lock = false;
    }

    private boolean isRootUserAuthorized() {
        Iterator<? extends GrantedAuthority> authorityIterator = SecurityContextHolder.getContext().getAuthentication().getAuthorities().iterator();
        while(authorityIterator.hasNext()) {
            if(!authorityIterator.next().getAuthority().equals(AuthorityHolder.ROOT_AUTHORITY))
                continue;
            return true;
        }
        return false;
    }

    @Async
    public void setLatestOsmDataToDB() throws FileNotFoundException {
        if(!isRootUserAuthorized())
            return;

        if(!getLock())
            return;
        getLogger().info("lock gained");

        File osmDataFile = new File(OSM_DATA_FILE_PATH);
        InputStream inputStream = null;

        if(!osmDataFile.exists() || !osmDataFile.canRead())
            if((inputStream = getDataFromInet()) == null) {
                releaseLock();
                return;
            }
        if(inputStream == null)
            inputStream = new FileInputStream(osmDataFile);

        getLogger().info("start to insert..");

        class QueryBuffer {
            private final StringBuffer buffer = new StringBuffer();
            private final int startingPtr;

            private int stringSizeBytes = 0;
            private final int stringSizeMinBytes;

            private static final int stringSizeLimitWhenUsualByte = 61707000;
            private static final int stringSizeLimitWhenMeteredByte = 1 * 1024 * 1024;
            private final int stringSizeLimitByte;

            @Getter
            private final String tableName;
            @Nullable
            private final QueryBuffer priorSending;

            QueryBuffer(String tableName, @Nullable QueryBuffer priorSending) {
                this.tableName = tableName;
                buffer.append("INSERT IGNORE INTO " + tableName + " VALUES");
                startingPtr = buffer.length();

                stringSizeBytes = getSizeByte(buffer.toString());
                stringSizeMinBytes = stringSizeBytes;

                this.priorSending = priorSending;

                if(Utils.isOnDebugging()) // When RAM of Mysql Server is enough
                    stringSizeLimitByte = stringSizeLimitWhenUsualByte;
                else
                    stringSizeLimitByte = stringSizeLimitWhenMeteredByte;
            }

            public void addQueryAndSendIfFilled(String query) {
                String appendAbleQuery = null;
                if(buffer.length() > startingPtr && stringSizeBytes > stringSizeMinBytes)
                    appendAbleQuery = "," + query;
                else
                    appendAbleQuery = query;

                int appendAbleQueryStringSizeBytes = getSizeByte(appendAbleQuery);
                int predictedAppendedStringSizeBytes = stringSizeBytes + appendAbleQueryStringSizeBytes;

                if(predictedAppendedStringSizeBytes >= stringSizeLimitByte) {
                    getLogger().atInfo().log(tableName + " trying to add..");
                    if(priorSending != null)
                        priorSending.sendQuery();
                    sendQuery();

                    predictedAppendedStringSizeBytes = stringSizeBytes + appendAbleQueryStringSizeBytes -1;
                    appendAbleQuery = appendAbleQuery.substring(1);
                }
                buffer.append(appendAbleQuery);
                stringSizeBytes = predictedAppendedStringSizeBytes;
            }

            public void sendQuery() {
                jdbcTemplate.update(buffer.toString());
                onSendQuery();
            }

            private void onSendQuery() {
                getLogger().atInfo().log(tableName + " " + stringSizeBytes/1024/1024 + "mb added");
                stringSizeBytes = stringSizeMinBytes;
                buffer.delete(startingPtr, buffer.length());
            }
            private int getSizeByte(String target) {
                byte[] bytes = target.getBytes(StandardCharsets.UTF_8);
                return bytes.length;
            }
        }

        QueryBuffer nodeQueryBuffer = new QueryBuffer("node", null);
        QueryBuffer nodeTagQueryBuffer = new QueryBuffer("node_tag", nodeQueryBuffer);
        QueryBuffer wayQueryBuffer = new QueryBuffer("way", null);
        QueryBuffer wayTagQueryBuffer= new QueryBuffer("way_tag", wayQueryBuffer);
        QueryBuffer wayNodeQuerybuffer = new QueryBuffer("way_node", wayQueryBuffer);
        QueryBuffer[] queryBuffers = new QueryBuffer[]{nodeQueryBuffer, nodeTagQueryBuffer, wayQueryBuffer, wayTagQueryBuffer, wayNodeQuerybuffer};

        PbfIterator pbfIt = new PbfIterator(inputStream, false);
        EntityContainer it = null;

        while(pbfIt.hasNext()) {
            it = pbfIt.next();
            String typeNameIt = it.getType().name();
            if(!typeNameIt.equals("Node") && !typeNameIt.equals("Way"))
                continue;

            if(typeNameIt.equals("Node")) {
                Node nodeIt = (Node)it.getEntity();
                nodeQueryBuffer.addQueryAndSendIfFilled("(" + nodeIt.getId() + "," + nodeIt.getLatitude() + "," + nodeIt.getLongitude() + ")");

                int numberOfItTags = nodeIt.getNumberOfTags();
                if(numberOfItTags < 1)
                    continue;

                for(int i=0; i<nodeIt.getNumberOfTags(); i++) {
                    OsmTag nodeTagIt = nodeIt.getTag(i);
                    nodeTagQueryBuffer.addQueryAndSendIfFilled("(" + nodeIt.getId() + ",\"" + getRemovedInappropriateMarks(nodeTagIt.getKey()) + "\",\"" + getRemovedInappropriateMarks(nodeTagIt.getValue()) + "\")");
                }
                continue;
            }

            Way wayIt = (Way)it.getEntity();
            wayQueryBuffer.addQueryAndSendIfFilled("(" + wayIt.getId() + ")");

            if(wayIt.getNumberOfNodes() > 0) {
                for(int i=0; i<wayIt.getNumberOfNodes(); i++)
                    wayNodeQuerybuffer.addQueryAndSendIfFilled("(" + wayIt.getId() + "," + i + "," + wayIt.getNodeId(i) + ")");
            }

            if(wayIt.getNumberOfTags() < 1)
                continue;
            for(int i=0; i<wayIt.getNumberOfTags(); i++) {
                OsmTag wayTagIt = wayIt.getTag(i);
                wayTagQueryBuffer.addQueryAndSendIfFilled("(" + wayIt.getId() + ",\"" + getRemovedInappropriateMarks(wayTagIt.getKey()) + "\",\"" + getRemovedInappropriateMarks(wayTagIt.getValue()) + "\")");
            }
        }

        for(int i=0; i<queryBuffers.length; i++)
            queryBuffers[i].sendQuery();

        getLogger().atInfo().log("complete!");
        releaseLock();
    }

    @Nullable
    private InputStream getDataFromInet() {
        try {
            getLogger().warn("trying to open stream from inet..");
            return new URL(URL_OSM_FULL_PBF_DOWNLOAD).openStream();
        } catch (MalformedURLException e) {
            getLogger().error("MalformedURL " + e + " abort.");
        } catch (IOException e) {
            getLogger().error("IOException " + e + " abort.");
        }
        return null;
    }

    private String getRemovedInappropriateMarks(String target) {
        char[] targetCharArray = target.toCharArray();
        StringBuilder returnAble = new StringBuilder();

        for(int i=0; i<targetCharArray.length; i++) {
            char targetCharIt = targetCharArray[i];
            if(targetCharIt == '"' || targetCharIt == '\n' || targetCharIt == '\\')
                continue;
            returnAble.append(targetCharIt);
        }
        return returnAble.toString();
    }

}
