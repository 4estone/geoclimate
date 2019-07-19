package org.orbisgis.osm
1+1

//import org.junit.jupiter.api.Disabled
//import org.orbisgis.PrepareData
//import org.slf4j.Logger
//import org.slf4j.LoggerFactory
//
//import static org.junit.jupiter.api.Assertions.*
//import org.junit.jupiter.api.Test
//import org.orbisgis.datamanager.h2gis.H2GIS
//
//class OSMGISLayersTests {
//
//    private static final Logger logger = LoggerFactory.getLogger(OSMGISLayersTests.class)
//
//    @Test
//    void prepareBuildingsTest() {
//        def h2GIS = H2GIS.open('./target/osmdb')
//        h2GIS.load(new File(this.class.getResource("zoneExtended.osm").toURI()).getAbsolutePath(),"ext",true)
//        h2GIS.execute "drop table if exists RAW_INPUT_BUILDING;"
//        assertNotNull(h2GIS.getTable("EXT_NODE"))
//        logger.info('Load OSM tables OK')
//        h2GIS.execute OSMGISLayers.createIndexesOnOSMTables("ext")
//        logger.info('Index OSM tables OK')
//        h2GIS.execute OSMGISLayers.zoneSQLScript('ext',"35236",1000, 500)
//        def process = PrepareData.OSMGISLayers.prepareBuildings()
//        process.execute([
//                datasource   : h2GIS,
//                osmTablesPrefix : "EXT",
//                buildingTableColumnsNames: ['height':'height','building:height':'b_height','roof:height':'r_height','building:roof:height':'b_r_height',
//                                    'building:levels':'b_lev','roof:levels':'r_lev','building:roof:levels':'b_r_lev','building':'building',
//                                    'amenity':'amenity','layer':'zindex','aeroway':'aeroway','historic':'historic','leisure':'leisure','monument':'monument',
//                                    'place_of_worship':'place_of_worship','military':'military','railway':'railway','public_transport':'public_transport',
//                                    'barrier':'barrier','government':'government','historic:building':'historic_building','grandstand':'grandstand',
//                                    'house':'house','shop':'shop','industrial':'industrial','man_made':'man_made', 'residential':'residential',
//                                    'apartments':'apartments','ruins':'ruins','agricultural':'agricultural','barn':'barn', 'healthcare':'healthcare',
//                                    'education':'education','restaurant':'restaurant','sustenance':'sustenance','office':'office'],
//                buildingTagKeys: ['building'],
//                buildingTagValues: null,
//                tablesPrefix: "RAW_",
//                buildingFilter: "ZONE_BUFFER"])
//        assertNotNull(h2GIS.getTable("RAW_INPUT_BUILDING"))
//        assertTrue(h2GIS.getTable("RAW_INPUT_BUILDING").getColumnNames().contains("HEIGHT"))
//        assertTrue(h2GIS.getTable("RAW_INPUT_BUILDING").getColumnNames().contains("OFFICE"))
//    }
//
//    //@Test
//    void prepareRoadsTest() {
//        def h2GIS = H2GIS.open('./target/osmdb')
//        h2GIS.load(new File(this.class.getResource("zoneExtended.osm").toURI()).getAbsolutePath(),"ext",true)
//        h2GIS.execute "drop table if exists RAW_INPUT_ROAD;"
//        assertNotNull(h2GIS.getTable("EXT_NODE"))
//        logger.info('Load OSM tables OK')
//        h2GIS.execute OSMGISLayers.createIndexesOnOSMTables("ext")
//        logger.info('Index OSM tables OK')
//        h2GIS.execute OSMGISLayers.zoneSQLScript('ext',"35236",1000, 500)
//        def process = PrepareData.OSMGISLayers.prepareRoads()
//        process.execute([
//                datasource   : h2GIS,
//                osmTablesPrefix : "EXT",
//                roadTableColumnsNames: ['width':'width','highway':'highway', 'surface':'surface', 'sidewalk':'sidewalk',
//                                    'lane':'lane','layer':'zindex','maxspeed':'maxspeed','oneway':'oneway',
//                                    'h_ref':'h_ref','route':'route','cycleway':'cycleway',
//                                    'biclycle_road':'biclycle_road','cyclestreet':'cyclestreet','junction':'junction'],
//                roadTagKeys: ['highway','cycleway','biclycle_road','cyclestreet','route','junction'],
//                roadTagValues: null,
//                tablesPrefix: "RAW_",
//                roadFilter: "ZONE_BUFFER"])
//        assertNotNull(h2GIS.getTable("RAW_INPUT_ROAD"))
//        assertTrue(h2GIS.getTable("RAW_INPUT_ROAD").getColumnNames().contains("junction"))
//    }
//
//    //@Test
//    void prepareRailsTest() {
//        def h2GIS = H2GIS.open('./target/osmdb')
//        h2GIS.load(new File(this.class.getResource("zoneExtended.osm").toURI()).getAbsolutePath(),"ext",true)
//        h2GIS.execute "drop table if exists RAW_INPUT_RAIL;"
//        assertNotNull(h2GIS.getTable("EXT_NODE"))
//        logger.info('Load OSM tables OK')
//        h2GIS.execute OSMGISLayers.createIndexesOnOSMTables("ext")
//        logger.info('Index OSM tables OK')
//        h2GIS.execute OSMGISLayers.zoneSQLScript('ext',"35236",1000, 500)
//        def process = PrepareData.OSMGISLayers.prepareRails()
//        process.execute([
//                datasource   : h2GIS,
//                osmTablesPrefix : "EXT",
//                railTableColumnsNames: ['highspeed':'highspeed','railway':'railway','service':'service',
//                                    'tunnel':'tunnel','layer':'layer','bridge':'bridge'],
//                railTagKeys: ['railway'],
//                railTagValues: null,
//                tablesPrefix: "RAW_",
//                railFilter: "ZONE_BUFFER"])
//        assertNotNull(h2GIS.getTable("RAW_INPUT_RAIL"))
//        assertTrue(h2GIS.getTable("RAW_INPUT_RAIL").getColumnNames().contains("highspeed"))
//    }
//
//    //@Test
//    void prepareVegetTest() {
//        def h2GIS = H2GIS.open('./target/osmdb')
//        h2GIS.load(new File(this.class.getResource("zoneExtended.osm").toURI()).getAbsolutePath(),"ext",true)
//        h2GIS.execute "drop table if exists RAW_INPUT_VEGET;"
//        assertNotNull(h2GIS.getTable("EXT_NODE"))
//        logger.info('Load OSM tables OK')
//        h2GIS.execute OSMGISLayers.createIndexesOnOSMTables("ext")
//        logger.info('Index OSM tables OK')
//        h2GIS.execute OSMGISLayers.zoneSQLScript('ext',"35236",1000, 500)
//        def process = PrepareData.OSMGISLayers.prepareVeget()
//        process.execute([
//                datasource   : h2GIS,
//                osmTablesPrefix : "EXT",
//                vegetTableColumnsNames: ['natural':'natural','landuse':'landuse','landcover':'landcover',
//                                    'vegetation':'vegetation','barrier':'barrier','fence_type':'fence_type',
//                                    'hedge':'hedge','wetland':'wetland','vineyard':'vineyard',
//                                    'trees':'trees','crop':'crop','produce':'produce'],
//                vegetTagKeys: ['natural', 'landuse','landcover'],
//                vegetTagValues: ['fell', 'heath', 'scrub', 'tree', 'tree_row', 'trees', 'wood','farmland',
//                            'forest','grass','grassland','greenfield','meadow','orchard','plant_nursery',
//                            'vineyard','hedge','hedge_bank','mangrove','banana_plants','banana','sugar_cane'],
//                tablesPrefix: "RAW_",
//                vegetFilter: "ZONE_EXTENDED"])
//        assertNotNull(h2GIS.getTable("RAW_INPUT_VEGET"))
//        assertTrue h2GIS.getTable("RAW_INPUT_VEGET").getColumnNames().contains("produce")
//    }
//
//    //@Test
//    void prepareHydroTest() {
//        def h2GIS = H2GIS.open('./target/osmdb')
//        h2GIS.load(new File(this.class.getResource("zoneExtended.osm").toURI()).getAbsolutePath(),"ext",true)
//        h2GIS.execute "drop table if exists RAW_INPUT_HYDRO;"
//        assertNotNull(h2GIS.getTable("EXT_NODE"))
//        logger.info('Load OSM tables OK')
//        h2GIS.execute OSMGISLayers.createIndexesOnOSMTables("ext")
//        logger.info('Index OSM tables OK')
//        h2GIS.execute OSMGISLayers.zoneSQLScript('ext',"35236",1000, 500)
//        def process = PrepareData.OSMGISLayers.prepareHydro()
//        process.execute([
//                datasource   : h2GIS,
//                osmTablesPrefix : "EXT",
//                hydroTableColumnsNames: ['natural':'natural','water':'water','waterway':'waterway'],
//                hydroTags: ['natural':['water','waterway','bay'],'water':[],'waterway':[]],
//                tablesPrefix: "RAW_",
//                hydroFilter: "ZONE_EXTENDED"])
//        assertNotNull(h2GIS.getTable("RAW_INPUT_HYDRO"))
//        assertTrue h2GIS.getTable("RAW_INPUT_HYDRO").getColumnNames().contains("waterway")
//    }
//
//    //@Test
//    void loadInitialDataTest() {
//        def h2GIS = H2GIS.open('./target/osmdb')
//        h2GIS.execute OSMGISLayers.dropOSMTables("EXT")
//        h2GIS.execute "drop table if exists ZONE;"
//        h2GIS.execute "drop table if exists ZONE_EXTENDED;"
//        h2GIS.execute "drop table if exists ZONE_BUFFER;"
//        h2GIS.execute "drop table if exists ZONE_NEIGHBORS;"
//        def process = PrepareData.OSMGISLayers.loadInitialData()
//        process.execute([
//                datasource :h2GIS,
//                osmTablesPrefix: "EXT",
//                idZone : "35236",
//                expand : 1000,
//                distBuffer:500])
//        assertNotNull h2GIS.getTable("EXT_NODE")
//        assertNotNull h2GIS.getTable("ZONE")
//        assertNotNull h2GIS.getTable("ZONE_EXTENDED")
//        assertNotNull h2GIS.getTable("ZONE_BUFFER")
//        assertNotNull h2GIS.getTable("ZONE_NEIGHBORS")
//    }
//
//}