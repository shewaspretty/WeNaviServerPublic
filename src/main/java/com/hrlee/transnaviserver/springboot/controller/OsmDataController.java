package com.hrlee.transnaviserver.springboot.controller;

import com.hrlee.transnaviserver.springboot.osm.OsmDataConfigurer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class OsmDataController {

    private final OsmDataConfigurer osmDataConfigurer;

    @GetMapping("/debug/osmData/maintenance")
    public ResponseEntity<String> getTest() throws Exception {
        osmDataConfigurer.setLatestOsmDataToDB();
        return ResponseEntity.ok("complete");
    }
}
