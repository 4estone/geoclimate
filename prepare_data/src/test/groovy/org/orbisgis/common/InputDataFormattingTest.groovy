package org.orbisgis.common

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import org.orbisgis.PrepareData
import org.orbisgis.datamanager.h2gis.H2GIS

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotEquals
import static org.junit.jupiter.api.Assertions.assertNotEquals
import static org.junit.jupiter.api.Assertions.assertNotEquals
import static org.junit.jupiter.api.Assertions.assertNotEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class InputDataFormattingTest {
    @BeforeAll
    static void init(){
        if(InputDataFormattingTest.class.getResource("bdtopofolder") != null &&
                new File(InputDataFormattingTest.class.getResource("bdtopofolder").toURI()).exists()) {
            H2GIS h2GISDatabase = H2GIS.open("./target/myh2gisbdtopodb", "sa", "")
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("bdtopofolder/IRIS_GE.shp"), "IRIS_GE", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("bdtopofolder/BATI_INDIFFERENCIE.shp"), "BATI_INDIFFERENCIE", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("bdtopofolder/BATI_INDUSTRIEL.shp"), "BATI_INDUSTRIEL", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("bdtopofolder/BATI_REMARQUABLE.shp"), "BATI_REMARQUABLE", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("bdtopofolder/ROUTE.shp"), "ROUTE", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("bdtopofolder/SURFACE_EAU.shp"), "SURFACE_EAU", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("bdtopofolder/ZONE_VEGETATION.shp"), "ZONE_VEGETATION", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("bdtopofolder/TRONCON_VOIE_FERREE.shp"), "TRONCON_VOIE_FERREE", true)

            h2GISDatabase.load(InputDataFormattingTest.class.getResource("BUILDING_ABSTRACT_PARAMETERS.csv"), "BUILDING_ABSTRACT_PARAMETERS", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("BUILDING_ABSTRACT_USE_TYPE.csv"), "BUILDING_ABSTRACT_USE_TYPE", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("BUILDING_BD_TOPO_USE_TYPE.csv"), "BUILDING_BD_TOPO_USE_TYPE", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("RAIL_ABSTRACT_TYPE.csv"), "RAIL_ABSTRACT_TYPE", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("RAIL_BD_TOPO_TYPE.csv"), "RAIL_BD_TOPO_TYPE", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("ROAD_ABSTRACT_PARAMETERS.csv"), "ROAD_ABSTRACT_PARAMETERS", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("ROAD_ABSTRACT_SURFACE.csv"), "ROAD_ABSTRACT_SURFACE", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("ROAD_ABSTRACT_TYPE.csv"), "ROAD_ABSTRACT_TYPE", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("RAIL_ABSTRACT_TYPE.csv"), "RAIL_ABSTRACT_TYPE", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("ROAD_BD_TOPO_TYPE.csv"), "ROAD_BD_TOPO_TYPE", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("VEGET_ABSTRACT_PARAMETERS.csv"), "VEGET_ABSTRACT_PARAMETERS", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("VEGET_ABSTRACT_TYPE.csv"), "VEGET_ABSTRACT_TYPE", true)
            h2GISDatabase.load(InputDataFormattingTest.class.getResource("VEGET_BD_TOPO_TYPE.csv"), "VEGET_BD_TOPO_TYPE", true)

        }
        else{
            System.properties.setProperty("data.bd.topo", "false")
        }
    }

    @Test
    @DisabledIfSystemProperty(named = "data.bd.topo", matches = "false")
    void inputDataFormatting(){
        H2GIS h2GISDatabase = H2GIS.open("./target/myh2gisbdtopodb", "sa", "")
        def processImport = PrepareData.BDTopoGISLayers.importPreprocess()
        assertTrue processImport.execute([datasource: h2GISDatabase, tableIrisName: 'IRIS_GE', tableBuildIndifName: 'BATI_INDIFFERENCIE',
                                    tableBuildIndusName: 'BATI_INDUSTRIEL', tableBuildRemarqName: 'BATI_REMARQUABLE',
                                    tableRoadName: 'ROUTE', tableRailName: 'TRONCON_VOIE_FERREE',
                                    tableHydroName: 'SURFACE_EAU', tableVegetName: 'ZONE_VEGETATION',
                                    distBuffer: 500, expand: 1000, idZone: '56260',
                                    building_bd_topo_use_type: 'BUILDING_BD_TOPO_USE_TYPE' ,
                                    building_abstract_use_type: 'BUILDING_ABSTRACT_USE_TYPE' ,
                                    road_bd_topo_type: 'ROAD_BD_TOPO_TYPE', road_abstract_type: 'ROAD_ABSTRACT_TYPE',
                                    rail_bd_topo_type: 'RAIL_BD_TOPO_TYPE', rail_abstract_type: 'RAIL_ABSTRACT_TYPE',
                                    veget_bd_topo_type: 'VEGET_BD_TOPO_TYPE', veget_abstract_type: 'VEGET_ABSTRACT_TYPE'
        ])
        def resultsImport=processImport.getResults()

        def processFormatting = PrepareData.InputDataFormatting.inputDataFormatting()
        assertTrue processFormatting.execute([datasource: h2GISDatabase,
                         inputBuilding: resultsImport.outputBuildingName, inputRoad: resultsImport.outputRoadName, inputRail: resultsImport.outputRailName,
                         inputHydro: resultsImport.outputHydroName, inputVeget: resultsImport.outputVegetName,
                         inputZone: resultsImport.outputZoneName, inputZoneNeighbors: resultsImport.outputZoneNeighborsName,

                         hLevMin: 3, hLevMax: 15, hThresholdLev2: 10, idZone: '56260',

                         buildingAbstractUseType: 'BUILDING_ABSTRACT_USE_TYPE', buildingAbstractParameters: 'BUILDING_ABSTRACT_PARAMETERS',
                         roadAbstractType: 'ROAD_ABSTRACT_TYPE', roadAbstractParameters: 'ROAD_ABSTRACT_PARAMETERS',
                         railAbstractType: 'RAIL_ABSTRACT_TYPE',
                         vegetAbstractType: 'VEGET_ABSTRACT_TYPE', vegetAbstractParameters: 'VEGET_ABSTRACT_PARAMETERS'])
        processFormatting.getResults().each {
            entry -> assertNotNull h2GISDatabase.getTable(entry.getValue())
        }

        // -----------------------------------------------------------------------------------
        // For BUILDINGS
        // -----------------------------------------------------------------------------------

        // Check if the BUILDING table has the correct number of columns and rows
        def tableName = processFormatting.getResults().outputBuilding
        def table = h2GISDatabase.getTable(tableName)
        assertEquals(10, table.columnCount)
        assertEquals(20568, table.rowCount)
        // Check if the column types are correct
        assertEquals('GEOMETRY', table.getColumnsType('THE_GEOM'))
        assertEquals('INTEGER', table.getColumnsType('ID_BUILD'))
        assertEquals('VARCHAR', table.getColumnsType('ID_SOURCE'))
        assertEquals('INTEGER', table.getColumnsType('HEIGHT_WALL'))
        assertEquals('INTEGER', table.getColumnsType('HEIGHT_ROOF'))
        assertEquals('INTEGER', table.getColumnsType('NB_LEV'))
        assertEquals('VARCHAR', table.getColumnsType('TYPE'))
        assertEquals('VARCHAR', table.getColumnsType('MAIN_USE'))
        assertEquals('INTEGER', table.getColumnsType('ZINDEX'))
        assertEquals('VARCHAR', table.getColumnsType('ID_ZONE'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.THE_GEOM)
            assertNotEquals('', row.THE_GEOM)
            assertNotNull(row.ID_BUILD)
            assertNotEquals('', row.ID_BUILD)
            assertNotNull(row.ID_SOURCE)
            assertNotEquals('', row.ID_SOURCE)
            // Check that the HEIGHT_WALL is smaller than 1000m high
            assertNotNull(row.HEIGHT_WALL)
            assertNotEquals('', row.HEIGHT_WALL)
            assertTrue(row.HEIGHT_WALL >= 0)
            assertTrue(row.HEIGHT_WALL <= 1000)
            // Check that there is no rows with a HEIGHT_ROOF value (will be updated in the following process)
            assertNotEquals('', row.HEIGHT_ROOF)
            assertNotEquals('', row.HEIGHT_ROOF)
            assertTrue(row.HEIGHT_ROOF >= 0)
            assertTrue(row.HEIGHT_ROOF <= 1000)
            // Check that there is no rows with a NB_LEV value (will be updated in the following process)
            assertNotEquals('', row.NB_LEV)
            assertNotEquals('', row.NB_LEV)
            assertTrue(row.NB_LEV >= 0)
            assertTrue(row.NB_LEV <= 1000)
            assertNotNull(row.TYPE)
            assertEquals('', row.MAIN_USE)
            assertNotNull(row.ZINDEX)
            assertNotEquals('', row.ZINDEX)
            assertEquals(0, row.ZINDEX)
            assertNotNull(row.ID_ZONE)
            assertNotEquals('', row.ID_ZONE)
        }

        // Specific cases
        // -------------------------------
        // ... with the building 'BATIMENT0000000290114260' : HAUTEUR = 0 / TYPE = 'Bâtiment sportif'
        assertEquals(3, h2GISDatabase.firstRow("SELECT HEIGHT_WALL FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290114260';")["HEIGHT_WALL"])
        assertEquals(3, h2GISDatabase.firstRow("SELECT HEIGHT_ROOF FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290114260';")["HEIGHT_ROOF"])
        assertEquals(1, h2GISDatabase.firstRow("SELECT NB_LEV FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290114260';")["NB_LEV"])
        assertEquals('sports_centre', h2GISDatabase.firstRow("SELECT TYPE FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290114260';")["TYPE"])

        //... with the building 'BATIMENT0000000290116893' : HAUTEUR = 0 / NATURE = 'Tour, donjon, moulin'
        assertEquals(3, h2GISDatabase.firstRow("SELECT HEIGHT_WALL FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290116893';")["HEIGHT_WALL"])
        assertEquals(3, h2GISDatabase.firstRow("SELECT HEIGHT_ROOF FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290116893';")["HEIGHT_ROOF"])
        assertEquals(1, h2GISDatabase.firstRow("SELECT NB_LEV FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290116893';")["NB_LEV"])
        assertEquals('historic', h2GISDatabase.firstRow("SELECT TYPE FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290116893';")["TYPE"])

        //... with the building 'BATIMENT0000000290116890' : HAUTEUR = 12 / NATURE = 'Chapelle'
        assertEquals(12, h2GISDatabase.firstRow("SELECT HEIGHT_WALL FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290116890';")["HEIGHT_WALL"])
        assertEquals(12, h2GISDatabase.firstRow("SELECT HEIGHT_ROOF FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290116890';")["HEIGHT_ROOF"])
        assertEquals(1, h2GISDatabase.firstRow("SELECT NB_LEV FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290116890';")["NB_LEV"])
        assertEquals('chapel', h2GISDatabase.firstRow("SELECT TYPE FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290116890';")["TYPE"])

        //... with the building 'BATIMENT0000000290120391' : HAUTEUR = 20 / NATURE = 'Mairie'
        assertEquals(20, h2GISDatabase.firstRow("SELECT HEIGHT_WALL FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290120391';")["HEIGHT_WALL"])
        assertEquals(20, h2GISDatabase.firstRow("SELECT HEIGHT_ROOF FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290120391';")["HEIGHT_ROOF"])
        assertEquals(6, h2GISDatabase.firstRow("SELECT NB_LEV FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290120391';")["NB_LEV"])
        assertEquals('townhall', h2GISDatabase.firstRow("SELECT TYPE FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290120391';")["TYPE"])

        //... with the building 'BATIMENT0000000290118405' : HAUTEUR = 13 / NATURE = 'Bâtiment commercial'
        assertEquals(13, h2GISDatabase.firstRow("SELECT HEIGHT_WALL FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290118405';")["HEIGHT_WALL"])
        assertEquals(13, h2GISDatabase.firstRow("SELECT HEIGHT_ROOF FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290118405';")["HEIGHT_ROOF"])
        assertEquals(4, h2GISDatabase.firstRow("SELECT NB_LEV FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290118405';")["NB_LEV"])
        assertEquals('commercial', h2GISDatabase.firstRow("SELECT TYPE FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290118405';")["TYPE"])

        //... with the building 'BATIMENT0000000087495394' : HAUTEUR = 10 / NATURE = 'Bâtiment industriel'
        assertEquals(10, h2GISDatabase.firstRow("SELECT HEIGHT_WALL FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000087495394';")["HEIGHT_WALL"])
        assertEquals(10, h2GISDatabase.firstRow("SELECT HEIGHT_ROOF FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000087495394';")["HEIGHT_ROOF"])
        assertEquals(1, h2GISDatabase.firstRow("SELECT NB_LEV FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000087495394';")["NB_LEV"])
        assertEquals('industrial', h2GISDatabase.firstRow("SELECT TYPE FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000087495394';")["TYPE"])

        //... with the building 'BATIMENT0000000290120660' : HAUTEUR = 0 / Bati indif so no NATURE
        assertEquals(3, h2GISDatabase.firstRow("SELECT HEIGHT_WALL FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290120660';")["HEIGHT_WALL"])
        assertEquals(3, h2GISDatabase.firstRow("SELECT HEIGHT_ROOF FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290120660';")["HEIGHT_ROOF"])
        assertEquals(1, h2GISDatabase.firstRow("SELECT NB_LEV FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290120660';")["NB_LEV"])
        assertEquals('building', h2GISDatabase.firstRow("SELECT TYPE FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290120660';")["TYPE"])

        //... with the building 'BATIMENT0000000290120079' : HAUTEUR = 17 / Bati indif so no NATURE
        assertEquals(17, h2GISDatabase.firstRow("SELECT HEIGHT_WALL FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290120079';")["HEIGHT_WALL"])
        assertEquals(17, h2GISDatabase.firstRow("SELECT HEIGHT_ROOF FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290120079';")["HEIGHT_ROOF"])
        assertEquals(5, h2GISDatabase.firstRow("SELECT NB_LEV FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290120079';")["NB_LEV"])
        assertEquals('building', h2GISDatabase.firstRow("SELECT TYPE FROM BUILDING " +
                "WHERE ID_SOURCE='BATIMENT0000000290120079';")["TYPE"])

        // ------------------
        // Check if the BUILDING_STATS_ZONE table has the correct number of columns and rows
        tableName = processFormatting.getResults().outputBuildingStatZone
        table = h2GISDatabase.getTable(tableName)
        assertEquals(16, table.columnCount)
        assertEquals(1, table.rowCount)
        // Check if the column types are correct
        assertEquals('VARCHAR', table.getColumnsType('ID_ZONE'))
        assertEquals('BIGINT', table.getColumnsType('NB_BUILD'))
        assertEquals('BIGINT', table.getColumnsType('NOT_VALID'))
        assertEquals('BIGINT', table.getColumnsType('IS_EMPTY'))
        assertEquals('BIGINT', table.getColumnsType('IS_EQUALS'))
        assertEquals('BIGINT', table.getColumnsType('OVERLAP'))
        assertEquals('BIGINT', table.getColumnsType('FC_H_ZERO'))
        assertEquals('BIGINT', table.getColumnsType('FC_H_NULL'))
        assertEquals('BIGINT', table.getColumnsType('FC_H_RANGE'))
        assertEquals('BIGINT', table.getColumnsType('H_NULL'))
        assertEquals('BIGINT', table.getColumnsType('H_RANGE'))
        assertEquals('BIGINT', table.getColumnsType('H_ROOF_MIN_WALL'))
        assertEquals('BIGINT', table.getColumnsType('LEV_NULL'))
        assertEquals('BIGINT', table.getColumnsType('LEV_RANGE'))
        assertEquals('BIGINT', table.getColumnsType('NO_TYPE'))
        assertEquals('BIGINT', table.getColumnsType('TYPE_RANGE'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.ID_ZONE)
            assertNotEquals('', row.ID_ZONE)
            assertEquals('56260', row.ID_ZONE)
            assertNotNull(row.NB_BUILD)
            assertNotEquals('', row.NB_BUILD)
            assertNotNull(row.NOT_VALID)
            assertNotEquals('', row.NOT_VALID)
            assertNotNull(row.IS_EMPTY)
            assertNotEquals('', row.IS_EMPTY)
            assertNotNull(row.IS_EQUALS)
            assertNotEquals('', row.IS_EQUALS)
            assertNotNull(row.OVERLAP)
            assertNotEquals('', row.OVERLAP)
            assertNotNull(row.FC_H_ZERO)
            assertNotEquals('', row.FC_H_ZERO)
            assertNotNull(row.FC_H_NULL)
            assertNotEquals('', row.FC_H_NULL)
            assertNotNull(row.FC_H_RANGE)
            assertNotEquals('', row.FC_H_RANGE)
            assertNotNull(row.H_NULL)
            assertNotEquals('', row.H_NULL)
            assertNotNull(row.H_RANGE)
            assertNotEquals('', row.H_RANGE)
            assertNotNull(row.H_ROOF_MIN_WALL)
            assertNotEquals('', row.H_ROOF_MIN_WALL)
            assertNotNull(row.LEV_NULL)
            assertNotEquals('', row.LEV_NULL)
            assertNotNull(row.LEV_RANGE)
            assertNotEquals('', row.LEV_RANGE)
            assertNotNull(row.NO_TYPE)
            assertNotEquals('', row.NO_TYPE)
            assertNotNull(row.TYPE_RANGE)
            assertNotEquals('', row.TYPE_RANGE)
        }

        // ------------------
        // Check if the BUILDING_STATS_EXT_ZONE table has the correct number of columns and rows
        tableName = processFormatting.getResults().outputBuildingStatZoneBuff
        table = h2GISDatabase.getTable(tableName)
        assertEquals(16, table.columnCount)
        assertEquals(1, table.rowCount)
        // Check if the column types are correct
        assertEquals('VARCHAR', table.getColumnsType('ID_ZONE'))
        assertEquals('BIGINT', table.getColumnsType('NB_BUILD'))
        assertEquals('BIGINT', table.getColumnsType('NOT_VALID'))
        assertEquals('BIGINT', table.getColumnsType('IS_EMPTY'))
        assertEquals('BIGINT', table.getColumnsType('IS_EQUALS'))
        assertEquals('BIGINT', table.getColumnsType('OVERLAP'))
        assertEquals('BIGINT', table.getColumnsType('FC_H_ZERO'))
        assertEquals('BIGINT', table.getColumnsType('FC_H_NULL'))
        assertEquals('BIGINT', table.getColumnsType('FC_H_RANGE'))
        assertEquals('BIGINT', table.getColumnsType('H_NULL'))
        assertEquals('BIGINT', table.getColumnsType('H_RANGE'))
        assertEquals('BIGINT', table.getColumnsType('H_ROOF_MIN_WALL'))
        assertEquals('BIGINT', table.getColumnsType('LEV_NULL'))
        assertEquals('BIGINT', table.getColumnsType('LEV_RANGE'))
        assertEquals('BIGINT', table.getColumnsType('NO_TYPE'))
        assertEquals('BIGINT', table.getColumnsType('TYPE_RANGE'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.ID_ZONE)
            assertNotEquals('', row.ID_ZONE)
            assertEquals('56260', row.ID_ZONE)
            assertNotNull(row.NB_BUILD)
            assertNotEquals('', row.NB_BUILD)
            assertNotNull(row.NOT_VALID)
            assertNotEquals('', row.NOT_VALID)
            assertNotNull(row.IS_EMPTY)
            assertNotEquals('', row.IS_EMPTY)
            assertNotNull(row.IS_EQUALS)
            assertNotEquals('', row.IS_EQUALS)
            assertNotNull(row.OVERLAP)
            assertNotEquals('', row.OVERLAP)
            assertNotNull(row.FC_H_ZERO)
            assertNotEquals('', row.FC_H_ZERO)
            assertNotNull(row.FC_H_NULL)
            assertNotEquals('', row.FC_H_NULL)
            assertNotNull(row.FC_H_RANGE)
            assertNotEquals('', row.FC_H_RANGE)
            assertNotNull(row.H_NULL)
            assertNotEquals('', row.H_NULL)
            assertNotNull(row.H_RANGE)
            assertNotEquals('', row.H_RANGE)
            assertNotNull(row.H_ROOF_MIN_WALL)
            assertNotEquals('', row.H_ROOF_MIN_WALL)
            assertNotNull(row.LEV_NULL)
            assertNotEquals('', row.LEV_NULL)
            assertNotNull(row.LEV_RANGE)
            assertNotEquals('', row.LEV_RANGE)
            assertNotNull(row.NO_TYPE)
            assertNotEquals('', row.NO_TYPE)
            assertNotNull(row.TYPE_RANGE)
            assertNotEquals('', row.TYPE_RANGE)
        }

        // -----------------------------------------------------------------------------------
        // For ROADS
        // -----------------------------------------------------------------------------------

        // Check if the ROAD table has the correct number of columns and rows
        tableName = processFormatting.getResults().outputRoad
        table = h2GISDatabase.getTable(tableName)
        assertEquals(8, table.columnCount)
        assertEquals(9769, table.rowCount)
        // Check if the column types are correct
        assertEquals('GEOMETRY', table.getColumnsType('THE_GEOM'))
        assertEquals('INTEGER', table.getColumnsType('ID_ROAD'))
        assertEquals('VARCHAR', table.getColumnsType('ID_SOURCE'))
        assertEquals('DOUBLE', table.getColumnsType('WIDTH'))
        assertEquals('VARCHAR', table.getColumnsType('TYPE'))
        assertEquals('VARCHAR', table.getColumnsType('SURFACE'))
        assertEquals('VARCHAR', table.getColumnsType('SIDEWALK'))
        assertEquals('INTEGER', table.getColumnsType('ZINDEX'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.THE_GEOM)
            assertNotEquals('', row.THE_GEOM)
            assertNotNull(row.ID_ROAD)
            assertNotEquals('', row.ID_ROAD)
            assertNotNull(row.ID_SOURCE)
            assertNotEquals('', row.ID_SOURCE)
            // Check that the WIDTH is smaller than 100m
            assertNotNull(row.WIDTH)
            assertNotEquals('', row.WIDTH)
            assertTrue(row.WIDTH >= 0)
            assertTrue(row.WIDTH <= 100)
            assertNotNull(row.TYPE)
            assertNotEquals('', row.TYPE)
            assertNotNull(row.SURFACE)
            assertNotNull(row.SIDEWALK)
            assertNotNull(row.ZINDEX)
            assertNotEquals('', row.ZINDEX)
        }

        // Specific cases
        // -------------------------------
        //... with the road 'TRONROUT0000000306711343' : LARGEUR = 0 / NATURE = 'Sentier'
        assertEquals(1, h2GISDatabase.firstRow("SELECT WIDTH FROM ROAD " +
                "WHERE ID_SOURCE='TRONROUT0000000306711343';")["WIDTH"])
        assertEquals('path', h2GISDatabase.firstRow("SELECT TYPE FROM ROAD " +
                "WHERE ID_SOURCE='TRONROUT0000000306711343';")["TYPE"])




        // ------------------
        // Check if the ROAD_STATS_ZONE table has the correct number of columns and rows
        tableName = processFormatting.getResults().outputRoadStatZone
        table = h2GISDatabase.getTable(tableName)
        assertEquals(13, table.columnCount)
        assertEquals(1, table.rowCount)
        // Check if the column types are correct
        assertEquals('VARCHAR', table.getColumnsType('ID_ZONE'))
        assertEquals('BIGINT', table.getColumnsType('NB_ROAD'))
        assertEquals('BIGINT', table.getColumnsType('NOT_VALID'))
        assertEquals('BIGINT', table.getColumnsType('IS_EMPTY'))
        assertEquals('BIGINT', table.getColumnsType('IS_EQUALS'))
        assertEquals('BIGINT', table.getColumnsType('OVERLAP'))
        assertEquals('BIGINT', table.getColumnsType('FC_W_ZERO'))
        assertEquals('BIGINT', table.getColumnsType('FC_W_NULL'))
        assertEquals('BIGINT', table.getColumnsType('FC_W_RANGE'))
        assertEquals('BIGINT', table.getColumnsType('W_NULL'))
        assertEquals('BIGINT', table.getColumnsType('W_RANGE'))
        assertEquals('BIGINT', table.getColumnsType('NO_TYPE'))
        assertEquals('BIGINT', table.getColumnsType('TYPE_RANGE'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.ID_ZONE)
            assertNotEquals('', row.ID_ZONE)
            assertEquals('56260', row.ID_ZONE)
            assertNotNull(row.NB_ROAD)
            assertNotEquals('', row.NB_ROAD)
            assertNotNull(row.NOT_VALID)
            assertNotEquals('', row.NOT_VALID)
            assertNotNull(row.IS_EMPTY)
            assertNotEquals('', row.IS_EMPTY)
            assertNotNull(row.IS_EQUALS)
            assertNotEquals('', row.IS_EQUALS)
            assertNotNull(row.OVERLAP)
            assertNotEquals('', row.OVERLAP)
            assertNotNull(row.FC_W_ZERO)
            assertNotEquals('', row.FC_W_ZERO)
            assertNotNull(row.FC_W_NULL)
            assertNotEquals('', row.FC_W_NULL)
            assertNotNull(row.FC_W_RANGE)
            assertNotEquals('', row.FC_W_RANGE)
            assertNotNull(row.W_NULL)
            assertNotEquals('', row.W_NULL)
            assertNotNull(row.W_RANGE)
            assertNotEquals('', row.W_RANGE)
            assertNotNull(row.NO_TYPE)
            assertNotEquals('', row.NO_TYPE)
            assertNotNull(row.TYPE_RANGE)
            assertNotEquals('', row.TYPE_RANGE)
        }

        // ------------------
        // Check if the ROAD_STATS_EXT_ZONE table has the correct number of columns and rows
        tableName = processFormatting.getResults().outputRoadStatZoneBuff
        table = h2GISDatabase.getTable(tableName)
        assertEquals(13, table.columnCount)
        assertEquals(1, table.rowCount)
        // Check if the column types are correct
        assertEquals('VARCHAR', table.getColumnsType('ID_ZONE'))
        assertEquals('BIGINT', table.getColumnsType('NB_ROAD'))
        assertEquals('BIGINT', table.getColumnsType('NOT_VALID'))
        assertEquals('BIGINT', table.getColumnsType('IS_EMPTY'))
        assertEquals('BIGINT', table.getColumnsType('IS_EQUALS'))
        assertEquals('BIGINT', table.getColumnsType('OVERLAP'))
        assertEquals('BIGINT', table.getColumnsType('FC_W_ZERO'))
        assertEquals('BIGINT', table.getColumnsType('FC_W_NULL'))
        assertEquals('BIGINT', table.getColumnsType('FC_W_RANGE'))
        assertEquals('BIGINT', table.getColumnsType('W_NULL'))
        assertEquals('BIGINT', table.getColumnsType('W_RANGE'))
        assertEquals('BIGINT', table.getColumnsType('NO_TYPE'))
        assertEquals('BIGINT', table.getColumnsType('TYPE_RANGE'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.ID_ZONE)
            assertNotEquals('', row.ID_ZONE)
            assertEquals('56260', row.ID_ZONE)
            assertNotNull(row.NB_ROAD)
            assertNotEquals('', row.NB_ROAD)
            assertNotNull(row.NOT_VALID)
            assertNotEquals('', row.NOT_VALID)
            assertNotNull(row.IS_EMPTY)
            assertNotEquals('', row.IS_EMPTY)
            assertNotNull(row.IS_EQUALS)
            assertNotEquals('', row.IS_EQUALS)
            assertNotNull(row.OVERLAP)
            assertNotEquals('', row.OVERLAP)
            assertNotNull(row.FC_W_ZERO)
            assertNotEquals('', row.FC_W_ZERO)
            assertNotNull(row.FC_W_NULL)
            assertNotEquals('', row.FC_W_NULL)
            assertNotNull(row.FC_W_RANGE)
            assertNotEquals('', row.FC_W_RANGE)
            assertNotNull(row.W_NULL)
            assertNotEquals('', row.W_NULL)
            assertNotNull(row.W_RANGE)
            assertNotEquals('', row.W_RANGE)
            assertNotNull(row.NO_TYPE)
            assertNotEquals('', row.NO_TYPE)
            assertNotNull(row.TYPE_RANGE)
            assertNotEquals('', row.TYPE_RANGE)
        }

        // -----------------------------------------------------------------------------------
        // For RAILS
        // -----------------------------------------------------------------------------------

        // Check if the RAIL table has the correct number of columns and rows
        tableName = processFormatting.getResults().outputRail
        table = h2GISDatabase.getTable(tableName)
        assertEquals(5, table.columnCount)
        assertEquals(20, table.rowCount)
        // Check if the column types are correct
        assertEquals('GEOMETRY', table.getColumnsType('THE_GEOM'))
        assertEquals('INTEGER', table.getColumnsType('ID_RAIL'))
        assertEquals('VARCHAR', table.getColumnsType('ID_SOURCE'))
        assertEquals('VARCHAR', table.getColumnsType('TYPE'))
        assertEquals('INTEGER', table.getColumnsType('ZINDEX'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.THE_GEOM)
            assertNotEquals('', row.THE_GEOM)
            assertNotNull(row.ID_RAIL)
            assertNotEquals('', row.ID_RAIL)
            assertNotNull(row.ID_SOURCE)
            assertNotEquals('', row.ID_SOURCE)
            assertNotNull(row.TYPE)
            assertNotEquals('', row.TYPE)
            assertNotNull(row.ZINDEX)
            assertNotEquals('', row.ZINDEX)
        }

        // ------------------
        // Check if the RAIL_STATS_ZONE table has the correct number of columns and rows
        tableName = processFormatting.getResults().outputRailStatZone
        table = h2GISDatabase.getTable(tableName)
        assertEquals(8, table.columnCount)
        assertEquals(1, table.rowCount)
        // Check if the column types are correct
        assertEquals('VARCHAR', table.getColumnsType('ID_ZONE'))
        assertEquals('BIGINT', table.getColumnsType('NB_RAIL'))
        assertEquals('BIGINT', table.getColumnsType('NOT_VALID'))
        assertEquals('BIGINT', table.getColumnsType('IS_EMPTY'))
        assertEquals('BIGINT', table.getColumnsType('IS_EQUALS'))
        assertEquals('BIGINT', table.getColumnsType('OVERLAP'))
        assertEquals('BIGINT', table.getColumnsType('NO_TYPE'))
        assertEquals('BIGINT', table.getColumnsType('TYPE_RANGE'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.ID_ZONE)
            assertNotEquals('', row.ID_ZONE)
            assertEquals('56260', row.ID_ZONE)
            assertNotNull(row.NB_RAIL)
            assertNotEquals('', row.NB_RAIL)
            assertNotNull(row.NOT_VALID)
            assertNotEquals('', row.NOT_VALID)
            assertNotNull(row.IS_EMPTY)
            assertNotEquals('', row.IS_EMPTY)
            assertNotNull(row.IS_EQUALS)
            assertNotEquals('', row.IS_EQUALS)
            assertNotNull(row.OVERLAP)
            assertNotEquals('', row.OVERLAP)
            assertNotNull(row.NO_TYPE)
            assertNotEquals('', row.NO_TYPE)
            assertNotNull(row.TYPE_RANGE)
            assertNotEquals('', row.TYPE_RANGE)
        }

        // -----------------------------------------------------------------------------------
        // For HYDROGRAPHIC AREAS
        // -----------------------------------------------------------------------------------

        // Check if the HYDRO table has the correct number of columns and rows
        tableName = processFormatting.getResults().outputHydro
        table = h2GISDatabase.getTable(tableName)
        assertEquals(3, table.columnCount)
        assertEquals(385, table.rowCount)
        // Check if the column types are correct
        assertEquals('GEOMETRY', table.getColumnsType('THE_GEOM'))
        assertEquals('INTEGER', table.getColumnsType('ID_HYDRO'))
        assertEquals('VARCHAR', table.getColumnsType('ID_SOURCE'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.THE_GEOM)
            assertNotEquals('', row.THE_GEOM)
            assertNotNull(row.ID_HYDRO)
            assertNotEquals('', row.ID_HYDRO)
            assertNotNull(row.ID_SOURCE)
            assertNotEquals('', row.ID_SOURCE)
        }

        // ------------------
        // Check if the HYDRO_STATS_ZONE table has the correct number of columns and rows
        tableName = processFormatting.getResults().outputHydroStatZone
        table = h2GISDatabase.getTable(tableName)
        assertEquals(6, table.columnCount)
        assertEquals(1, table.rowCount)
        // Check if the column types are correct
        assertEquals('VARCHAR', table.getColumnsType('ID_ZONE'))
        assertEquals('BIGINT', table.getColumnsType('NB_HYDRO'))
        assertEquals('BIGINT', table.getColumnsType('NOT_VALID'))
        assertEquals('BIGINT', table.getColumnsType('IS_EMPTY'))
        assertEquals('BIGINT', table.getColumnsType('IS_EQUALS'))
        assertEquals('BIGINT', table.getColumnsType('OVERLAP'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.ID_ZONE)
            assertNotEquals('', row.ID_ZONE)
            assertEquals('56260', row.ID_ZONE)
            assertNotNull(row.NB_HYDRO)
            assertNotEquals('', row.NB_HYDRO)
            assertNotNull(row.NOT_VALID)
            assertNotEquals('', row.NOT_VALID)
            assertNotNull(row.IS_EMPTY)
            assertNotEquals('', row.IS_EMPTY)
            assertNotNull(row.IS_EQUALS)
            assertNotEquals('', row.IS_EQUALS)
            assertNotNull(row.OVERLAP)
            assertNotEquals('', row.OVERLAP)
        }

        // ------------------
        // Check if the HYDRO_STATS_EXT_ZONE table has the correct number of columns and rows
        tableName = processFormatting.getResults().outputHydroStatZoneExt
        table = h2GISDatabase.getTable(tableName)
        assertEquals(6, table.columnCount)
        assertEquals(1, table.rowCount)
        // Check if the column types are correct
        assertEquals('VARCHAR', table.getColumnsType('ID_ZONE'))
        assertEquals('BIGINT', table.getColumnsType('NB_HYDRO'))
        assertEquals('BIGINT', table.getColumnsType('NOT_VALID'))
        assertEquals('BIGINT', table.getColumnsType('IS_EMPTY'))
        assertEquals('BIGINT', table.getColumnsType('IS_EQUALS'))
        assertEquals('BIGINT', table.getColumnsType('OVERLAP'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.ID_ZONE)
            assertNotEquals('', row.ID_ZONE)
            assertEquals('56260', row.ID_ZONE)
            assertNotNull(row.NB_HYDRO)
            assertNotEquals('', row.NB_HYDRO)
            assertNotNull(row.NOT_VALID)
            assertNotEquals('', row.NOT_VALID)
            assertNotNull(row.IS_EMPTY)
            assertNotEquals('', row.IS_EMPTY)
            assertNotNull(row.IS_EQUALS)
            assertNotEquals('', row.IS_EQUALS)
            assertNotNull(row.OVERLAP)
            assertNotEquals('', row.OVERLAP)
        }

        // -----------------------------------------------------------------------------------
        // For VEGETATION AREAS
        // -----------------------------------------------------------------------------------

        // Check if the VEGET table has the correct number of columns and rows
        tableName = processFormatting.getResults().outputVeget
        table = h2GISDatabase.getTable(tableName)
        assertEquals(5, table.columnCount)
        assertEquals(7756, table.rowCount)
        // Check if the column types are correct
        assertEquals('GEOMETRY', table.getColumnsType('THE_GEOM'))
        assertEquals('INTEGER', table.getColumnsType('ID_VEGET'))
        assertEquals('VARCHAR', table.getColumnsType('ID_SOURCE'))
        assertEquals('VARCHAR', table.getColumnsType('TYPE'))
        assertEquals('VARCHAR', table.getColumnsType('HEIGHT_CLASS'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.THE_GEOM)
            assertNotEquals('', row.THE_GEOM)
            assertNotNull(row.ID_VEGET)
            assertNotEquals('', row.ID_VEGET)
            assertNotNull(row.ID_SOURCE)
            assertNotEquals('', row.ID_SOURCE)
            assertNotNull(row.TYPE)
            assertNotEquals('', row.TYPE)
            assertNotNull(row.HEIGHT_CLASS)
            assertNotEquals('', row.HEIGHT_CLASS)
        }

        // ------------------
        // Check if the VEGET_STATS_ZONE table has the correct number of columns and rows
        tableName = processFormatting.getResults().outputVegetStatZone
        table = h2GISDatabase.getTable(tableName)
        assertEquals(8, table.columnCount)
        assertEquals(1, table.rowCount)
        // Check if the column types are correct
        assertEquals('VARCHAR', table.getColumnsType('ID_ZONE'))
        assertEquals('BIGINT', table.getColumnsType('NB_VEGET'))
        assertEquals('BIGINT', table.getColumnsType('NOT_VALID'))
        assertEquals('BIGINT', table.getColumnsType('IS_EMPTY'))
        assertEquals('BIGINT', table.getColumnsType('IS_EQUALS'))
        assertEquals('BIGINT', table.getColumnsType('OVERLAP'))
        assertEquals('BIGINT', table.getColumnsType('NO_TYPE'))
        assertEquals('BIGINT', table.getColumnsType('TYPE_RANGE'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.ID_ZONE)
            assertNotEquals('', row.ID_ZONE)
            assertEquals('56260', row.ID_ZONE)
            assertNotNull(row.NB_VEGET)
            assertNotEquals('', row.NB_VEGET)
            assertNotNull(row.NOT_VALID)
            assertNotEquals('', row.NOT_VALID)
            assertNotNull(row.IS_EMPTY)
            assertNotEquals('', row.IS_EMPTY)
            assertNotNull(row.IS_EQUALS)
            assertNotEquals('', row.IS_EQUALS)
            assertNotNull(row.OVERLAP)
            assertNotEquals('', row.OVERLAP)
            assertNotNull(row.NO_TYPE)
            assertNotEquals('', row.NO_TYPE)
            assertNotNull(row.TYPE_RANGE)
            assertNotEquals('', row.TYPE_RANGE)
        }

        // ------------------
        // Check if the VEGET_STATS_ZONE table has the correct number of columns and rows
        tableName = processFormatting.getResults().outputVegetStatZoneExt
        table = h2GISDatabase.getTable(tableName)
        assertEquals(8, table.columnCount)
        assertEquals(1, table.rowCount)
        // Check if the column types are correct
        assertEquals('VARCHAR', table.getColumnsType('ID_ZONE'))
        assertEquals('BIGINT', table.getColumnsType('NB_VEGET'))
        assertEquals('BIGINT', table.getColumnsType('NOT_VALID'))
        assertEquals('BIGINT', table.getColumnsType('IS_EMPTY'))
        assertEquals('BIGINT', table.getColumnsType('IS_EQUALS'))
        assertEquals('BIGINT', table.getColumnsType('OVERLAP'))
        assertEquals('BIGINT', table.getColumnsType('NO_TYPE'))
        assertEquals('BIGINT', table.getColumnsType('TYPE_RANGE'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.ID_ZONE)
            assertNotEquals('', row.ID_ZONE)
            assertEquals('56260', row.ID_ZONE)
            assertNotNull(row.NB_VEGET)
            assertNotEquals('', row.NB_VEGET)
            assertNotNull(row.NOT_VALID)
            assertNotEquals('', row.NOT_VALID)
            assertNotNull(row.IS_EMPTY)
            assertNotEquals('', row.IS_EMPTY)
            assertNotNull(row.IS_EQUALS)
            assertNotEquals('', row.IS_EQUALS)
            assertNotNull(row.OVERLAP)
            assertNotEquals('', row.OVERLAP)
            assertNotNull(row.NO_TYPE)
            assertNotEquals('', row.NO_TYPE)
            assertNotNull(row.TYPE_RANGE)
            assertNotEquals('', row.TYPE_RANGE)
        }

        // -----------------------------------------------------------------------------------
        // For ZONE
        // -----------------------------------------------------------------------------------

        // Check if the ZONE table has the correct number of columns and rows
        tableName = processFormatting.getResults().outputZone
        table = h2GISDatabase.getTable(tableName)
        assertEquals(2, table.columnCount)
        assertEquals(1, table.rowCount)
        // Check if the column types are correct
        assertEquals('VARCHAR', table.getColumnsType('ID_ZONE'))
        assertEquals('GEOMETRY', table.getColumnsType('THE_GEOM'))
        // For each rows, check if the fields contains the expected values
        table.eachRow { row ->
            assertNotNull(row.ID_ZONE)
            assertNotEquals('', row.ID_ZONE)
            assertEquals('56260', row.ID_ZONE)
            assertNotNull(row.THE_GEOM)
            assertNotEquals('', row.THE_GEOM)
        }

    }
}
