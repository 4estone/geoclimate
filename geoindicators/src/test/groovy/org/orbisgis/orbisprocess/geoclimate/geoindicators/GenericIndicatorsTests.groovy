package org.orbisgis.orbisprocess.geoclimate.geoindicators

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.orbisgis.orbisdata.datamanager.jdbc.h2gis.H2GIS

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

class GenericIndicatorsTests {

    private static H2GIS h2GIS

    @BeforeAll
    static void init(){
        h2GIS = H2GIS.open('./target/buildingdb;AUTO_SERVER=TRUE')
    }

    @BeforeEach
    void initData(){
        h2GIS.executeScript(getClass().getResourceAsStream("data_for_tests.sql"))
    }

    @Test
    void unweightedOperationFromLowerScaleTest() {
        // Only the first 6 first created buildings are selected since any new created building may alter the results
        h2GIS.execute "DROP TABLE IF EXISTS tempo_build0, tempo_build, tempo_rsu, unweighted_operation_from_lower_scale1, " +
                "unweighted_operation_from_lower_scale2, unweighted_operation_from_lower_scale3; " +
                "CREATE TABLE tempo_build AS SELECT * FROM building_test WHERE " +
                "id_build < 8; CREATE TABLE tempo_rsu AS SELECT * FROM rsu_test WHERE id_rsu < 17"

        def  psum =  Geoindicators.GenericIndicators.unweightedOperationFromLowerScale()
        assertTrue psum.execute([inputLowerScaleTableName: "tempo_build",inputUpperScaleTableName: "block_test",
                   inputIdUp: "id_block", inputIdLow: "id_build", inputVarAndOperations: ["area":["SUM"]],
                   prefixName: "first", datasource: h2GIS])
        def  pavg =  Geoindicators.GenericIndicators.unweightedOperationFromLowerScale()
        assertTrue pavg.execute([inputLowerScaleTableName: "tempo_build",inputUpperScaleTableName: "tempo_rsu",
                      inputIdUp: "id_rsu", inputIdLow: "id_build", inputVarAndOperations: ["number_building_neighbor":["AVG"]],
                      prefixName: "second", datasource: h2GIS])
        def  pgeom_avg =  Geoindicators.GenericIndicators.unweightedOperationFromLowerScale()
        assertTrue pgeom_avg.execute([inputLowerScaleTableName: "tempo_build",inputUpperScaleTableName: "tempo_rsu",
                      inputIdUp: "id_rsu", inputIdLow: "id_build", inputVarAndOperations: ["height_roof": ["GEOM_AVG"]],
                      prefixName: "third", datasource: h2GIS])
        def  pdens =  Geoindicators.GenericIndicators.unweightedOperationFromLowerScale()
        assertTrue pdens.execute([inputLowerScaleTableName: "tempo_build",inputUpperScaleTableName: "tempo_rsu",
                      inputIdUp: "id_rsu", inputIdLow: "id_build", inputVarAndOperations: ["number_building_neighbor":["AVG"],
                                                                   "area":["SUM", "DENS", "NB_DENS"]],
                      prefixName: "fourth", datasource: h2GIS])
        def concat = ["", "", 0, ""]

        h2GIS.eachRow("SELECT * FROM first_unweighted_operation_from_lower_scale WHERE id_block = 1 OR id_block = 4 ORDER BY id_block ASC"){
            row -> concat[0]+= "${row.sum_area}\n"
        }
        h2GIS.eachRow("SELECT * FROM second_unweighted_operation_from_lower_scale WHERE id_rsu = 1 OR id_rsu = 2 ORDER BY id_rsu ASC"){
            row -> concat[1]+= "${row.avg_number_building_neighbor}\n"
        }
        h2GIS.eachRow("SELECT * FROM third_unweighted_operation_from_lower_scale WHERE id_rsu = 1"){
            row -> concat[2]+= row.geom_avg_height_roof
        }
        h2GIS.eachRow("SELECT * FROM fourth_unweighted_operation_from_lower_scale WHERE id_rsu = 1"){
            row ->
                concat[3]+= "${row.avg_number_building_neighbor}\n"
                concat[3]+= "${row.sum_area}\n"
                concat[3]+= "${row.area_density}\n"
                concat[3]+= "${row.area_number_density}\n"
        }
        def nb_rsu = h2GIS.firstRow "SELECT COUNT(*) AS NB FROM ${pgeom_avg.results.outputTableName}"
        def val_zero = h2GIS.firstRow "SELECT area_density AS val FROM ${pdens.results.outputTableName} "+
                                      "WHERE id_rsu = 14"
        assertEquals("156.0\n310.0\n", concat[0])
        assertEquals("0.4\n0.0\n", concat[1])
        assertEquals(10.69, concat[2], 0.01)
        assertEquals("0.4\n606.0\n0.303\n0.0025\n", concat[3])
        assertEquals(16, nb_rsu.nb)
        assertEquals(0, val_zero.val)
        // Test the fix concerning nb_dens_building (initially >0 while no building in RSU...)
        def nb_dens = h2GIS.firstRow("SELECT area_number_density FROM fourth_unweighted_operation_from_lower_scale WHERE id_rsu = 14")
        assertEquals(0, nb_dens["AREA_NUMBER_DENSITY"])
        def geom_ave = h2GIS.firstRow("SELECT geom_avg_height_roof FROM third_unweighted_operation_from_lower_scale WHERE id_rsu = 14")
        assertEquals(0, geom_ave["geom_avg_height_roof"])
    }

    @Test
    void weightedAggregatedStatisticsTest() {
        // Only the first 6 first created buildings are selected since any new created building may alter the results
        h2GIS.execute "DROP TABLE IF EXISTS tempo_build, tempo_rsu, one_weighted_aggregated_statistics, " +
                "two_weighted_aggregated_statistics, three_weighted_aggregated_statistics; " +
                "CREATE TABLE tempo_build AS SELECT * FROM building_test " +
                "WHERE id_build < 8; CREATE TABLE tempo_rsu AS SELECT * FROM rsu_test WHERE id_rsu < 17;"

        def  pavg =  Geoindicators.GenericIndicators.weightedAggregatedStatistics()
        assertTrue pavg.execute([inputLowerScaleTableName: "tempo_build",inputUpperScaleTableName: "tempo_rsu",
                      inputIdUp: "id_rsu", inputVarWeightsOperations: ["height_roof" : ["area": ["AVG"]]],
                      prefixName: "one", datasource: h2GIS])
        def  pstd =  Geoindicators.GenericIndicators.weightedAggregatedStatistics()
        assertTrue pstd.execute([inputLowerScaleTableName: "tempo_build",inputUpperScaleTableName: "tempo_rsu",
                      inputIdUp: "id_rsu", inputVarWeightsOperations: ["height_roof": ["area": ["STD"]]],
                      prefixName: "two", datasource: h2GIS])
        def  pall =  Geoindicators.GenericIndicators.weightedAggregatedStatistics()
        assertTrue pall.execute([inputLowerScaleTableName: "tempo_build",inputUpperScaleTableName: "tempo_rsu",
                      inputIdUp: "id_rsu", inputVarWeightsOperations: ["height_wall": ["area": ["STD"]],
                                                                       "height_roof": ["area": ["AVG", "STD"]]],
                      prefixName: "three", datasource: h2GIS])
        def concat = [0, 0, ""]
        h2GIS.eachRow("SELECT * FROM one_weighted_aggregated_statistics WHERE id_rsu = 1"){
            row -> concat[0]+= row.avg_height_roof_area_weighted
        }
        h2GIS.eachRow("SELECT * FROM two_weighted_aggregated_statistics WHERE id_rsu = 1"){
            row -> concat[1]+= row.std_height_roof_area_weighted
        }
        h2GIS.eachRow("SELECT * FROM three_weighted_aggregated_statistics WHERE id_rsu = 1"){
            row ->
                concat[2]+= "${row.avg_height_roof_area_weighted.round(3)}\n" +
                        "${row.std_height_roof_area_weighted.round(1)}\n" +
                        "${row.std_height_wall_area_weighted.round(2)}\n"
        }
        def nb_rsu = h2GIS.firstRow("SELECT COUNT(*) AS NB FROM ${pavg.results.outputTableName}".toString())
        assertEquals(10.178, concat[0], 0.001)
        assertEquals(2.5, concat[1], 0.1)
        assertEquals("10.178\n2.5\n2.52\n", concat[2])
        assertEquals(16, nb_rsu.nb)
    }

    @Test
    void geometryPropertiesTest() {
        h2GIS.execute """
                DROP TABLE IF EXISTS spatial_table, test_geometry_properties;
                CREATE TABLE spatial_table (id int, the_geom GEOMETRY(LINESTRING));
                INSERT INTO spatial_table VALUES (1, 'LINESTRING(0 0, 0 10)'::GEOMETRY);
        """
        def  p =  Geoindicators.GenericIndicators.geometryProperties()
        assertTrue p.execute([inputTableName: "spatial_table",
                              inputFields:["id", "the_geom"],
                              operations:["st_issimple","st_area", "area", "st_dimension"],
                              prefixName : "test",
                              datasource:h2GIS])
        assert p.results.outputTableName == "test_geometry_properties"
        h2GIS.getTable("test_geometry_properties").eachRow {
            row -> assert row.the_geom!=null
                assert row.issimple==true
                assertEquals(0,row.area)
                assertEquals(1, row.dimension)
                assertEquals(1,  row.id)
        }
    }

    @Test
    void buildingDirectionDistributionTest() {
        // Only the first 6 first created buildings are selected since any new created building may alter the results
        h2GIS.execute "DROP TABLE IF EXISTS tempo_build, test_MAIN_BUILDING_DIRECTION, test_DISTRIBUTION_REPARTITION; " +
                "CREATE TABLE tempo_build AS SELECT * FROM building_test WHERE id_build < 9"

        def  p =  Geoindicators.GenericIndicators.buildingDirectionDistribution()
        assertTrue p.execute([buildingTableName: "tempo_build", inputIdUp: "id_block", angleRangeSize: 15,
                              prefixName: "test", datasource: h2GIS, distribIndicator: ["equality", "uniqueness"]])

        assertEquals(4.0/12, h2GIS.firstRow("SELECT * FROM test_MAIN_BUILDING_DIRECTION " +
                "WHERE id_block = 4")["BUILDING_DIRECTION_EQUALITY"], 0.0001)
        assertEquals("ANG97_5", h2GIS.firstRow("SELECT * FROM test_MAIN_BUILDING_DIRECTION " +
                "WHERE id_block = 4")["main_building_direction"])
        assertEquals((28.0-22.0)/(22+28.0), h2GIS.firstRow("SELECT * FROM test_MAIN_BUILDING_DIRECTION " +
                "WHERE id_block = 4")["BUILDING_DIRECTION_UNIQUENESS"], 0.0001)
    }

    @Test
    void distributionCharacterizationTest1() {
        // Tests with extremum = "GREATEST" and all distribIndicators

        // Create a table containing a distribution between columns
        h2GIS.execute """DROP TABLE IF EXISTS distrib_test,test_DISTRIBUTION_REPARTITION;
                            CREATE TABLE distrib_test(id integer, col1 double, col2 double, col3 double, col4 double);
                            INSERT INTO distrib_test 
                                    VALUES (1, 25, 25, 25, 25), (2, 10, 20, 40, 20),
                                               (3, 0, 0, 60, 40), (4, 0, 0, 0, 100);"""


        def  p1 =  Geoindicators.GenericIndicators.distributionCharacterization()
        assertTrue p1.execute([distribTableName:   "distrib_test",
                    inputId:            "id",
                    distribIndicator:   ["equality", "uniqueness"],
                    extremum:           "GREATEST",
                    prefixName:         "test",
                    datasource:         h2GIS])
        def resultTab = p1.results.outputTableName

        assertEquals(1, h2GIS.firstRow("SELECT * FROM $resultTab " +
                "WHERE id = 1")["EQUALITY_VALUE"])
        assertEquals(0.25, h2GIS.firstRow("SELECT * FROM $resultTab " +
                "WHERE id = 4")["EQUALITY_VALUE"])
        assertEquals(0, h2GIS.firstRow("SELECT * FROM $resultTab " +
                "WHERE id = 1")["UNIQUENESS_VALUE"])
        assertEquals(1, h2GIS.firstRow("SELECT * FROM $resultTab " +
                "WHERE id = 4")["UNIQUENESS_VALUE"])
        assertEquals("COL3", h2GIS.firstRow("SELECT * FROM $resultTab " +
                "WHERE id = 2")["EXTREMUM_COL"])
        assertEquals("COL4", h2GIS.firstRow("SELECT * FROM $resultTab " +
                "WHERE id = 4")["EXTREMUM_COL"])
    }

    @Test
    void distributionCharacterizationTest2() {
        // Tests with extremum = "LOWEST" and only "uniqueness" indicator

        // Create a table containing a distribution between columns
        h2GIS.execute """DROP TABLE IF EXISTS distrib_test,test_DISTRIBUTION_REPARTITION;
                            CREATE TABLE distrib_test(id integer, col1 double, col2 double, col3 double, col4 double);
                            INSERT INTO distrib_test 
                                    VALUES (1, 25, 25, 25, 25), (2, 10, 20, 40, 20),
                                               (3, 0, 0, 60, 40), (4, 0, 0, 0, 100);"""


        def  p1 =  Geoindicators.GenericIndicators.distributionCharacterization()
        assertTrue p1.execute([distribTableName:   "distrib_test",
                               inputId:            "id",
                               distribIndicator:   ["uniqueness"],
                               extremum:           "LEAST",
                               prefixName:         "test",
                               datasource:         h2GIS])

        assertEquals(0, h2GIS.firstRow("SELECT * FROM test_DISTRIBUTION_REPARTITION " +
                "WHERE id = 1")["UNIQUENESS_VALUE"])
        assertEquals(1.0/3, h2GIS.firstRow("SELECT * FROM test_DISTRIBUTION_REPARTITION " +
                "WHERE id = 2")["UNIQUENESS_VALUE"],0.0001)
        assertEquals(0, h2GIS.firstRow("SELECT * FROM test_DISTRIBUTION_REPARTITION " +
                "WHERE id = 4")["UNIQUENESS_VALUE"])
        assertEquals("COL1", h2GIS.firstRow("SELECT * FROM test_DISTRIBUTION_REPARTITION " +
                "WHERE id = 2")["EXTREMUM_COL"])
    }

    @Test
    void distributionCharacterizationTest3() {
        // Tests with only "inequality" indicator

        // Create a table containing a distribution between columns
        h2GIS.execute """DROP TABLE IF EXISTS distrib_test,test_DISTRIBUTION_REPARTITION;
                            CREATE TABLE distrib_test(id integer, col1 double, col2 double, col3 double, col4 double);
                            INSERT INTO distrib_test 
                                    VALUES (1, 25, 25, 25, 25), (2, 10, 20, 40, 20),
                                               (3, 0, 0, 60, 40), (4, 0, 0, 0, 100);"""


        def  p1 =  Geoindicators.GenericIndicators.distributionCharacterization()
        assertTrue p1.execute([distribTableName:   "distrib_test",
                               inputId:            "id",
                               distribIndicator:   ["equality"],
                               extremum:           "LEAST",
                               prefixName:         "test",
                               datasource:         h2GIS])

        assertEquals(1, h2GIS.firstRow("SELECT * FROM test_DISTRIBUTION_REPARTITION " +
                "WHERE id = 1")["EQUALITY_VALUE"])
        assertEquals(0.25, h2GIS.firstRow("SELECT * FROM test_DISTRIBUTION_REPARTITION " +
                "WHERE id = 4")["EQUALITY_VALUE"])
        assertEquals("COL1", h2GIS.firstRow("SELECT * FROM test_DISTRIBUTION_REPARTITION " +
                "WHERE id = 2")["EXTREMUM_COL"])
        assertEquals("COL1", h2GIS.firstRow("SELECT * FROM test_DISTRIBUTION_REPARTITION " +
                "WHERE id = 4")["EXTREMUM_COL"])
    }
}