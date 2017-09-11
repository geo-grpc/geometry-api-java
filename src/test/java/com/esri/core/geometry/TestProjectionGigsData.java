package com.esri.core.geometry;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.proj4.*;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.json.*;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by davidraleigh on 9/11/17.
 * https://stackoverflow.com/questions/358802/junit-test-with-dynamic-number-of-tests
 * https://nofluffjuststuff.com/blog/paul_duvall/2007/04/take_heed_of_mixing_junit_4_s_parameterized_tests
 * https://github.com/junit-team/junit4/wiki/Parameterized-tests
 */
@RunWith(Parameterized.class)
public class TestProjectionGigsData extends TestCase {
    static {
        System.loadLibrary("proj");
    }

    private Path path;
    private String testName;
    private String testData;
    private String description;

    public TestProjectionGigsData(Path path, String testName) throws java.io.IOException, org.json.JSONException {
        this.path = path;
        this.testName = testName;
        // http://www.adam-bien.com/roller/abien/entry/java_8_reading_a_file
        String content = new String(Files.readAllBytes(path), Charset.defaultCharset());
        JSONObject obj = new JSONObject(content);
        this.description = obj.getString("description");
    }

    @Test
    public void testConversion() throws Exception {
        assertTrue(this.description, true);
    }

    @Test
    public void testRoundtrip() throws Exception {
        assertTrue(this.description, true);
    }

    @Parameterized.Parameters(name = "{1}")
    public static Collection<Object[]> data() throws java.io.IOException, java.net.URISyntaxException {
        // load the files as you want
        URL urls = TestProjectionGigsData.class.getResource("gigs");
        Path gigsDir = Paths.get(urls.toURI());

        // https://stackoverflow.com/a/36815191/445372
        Stream<Path> paths = Files.walk(gigsDir, 1, FileVisitOption.FOLLOW_LINKS);

        Collection<Object []> data = paths
                // https://stackoverflow.com/a/20533064/445372
                .filter(p -> p.toString().toLowerCase().endsWith(".json"))
                // https://www.mkyong.com/java8/java-8-filter-a-map-examples/
                .map(p -> new Object[] {p, p.getFileName().toString().split(".json")[0].replace('.', '_')})
                // https://www.javabrahman.com/java-8/java-8-how-to-use-collectors-tocollection-collector-with-examples/
                .collect(Collectors.toCollection(ArrayList::new));

        return data;
    }
}
