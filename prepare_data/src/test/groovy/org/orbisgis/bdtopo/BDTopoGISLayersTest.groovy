package org.orbisgis.bdtopo

import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfSystemProperty
import org.orbisgis.PrepareData
import org.orbisgis.datamanager.h2gis.H2GIS

import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull
import static org.junit.jupiter.api.Assertions.assertTrue

class BDTopoGISLayersTest {

    private final static String bdTopoDb = System.getProperty("user.home") + "/myh2gisbdtopodb.mv.db"

    @BeforeAll
    static void init(){
        System.setProperty("test.bdtopo", Boolean.toString(new File(bdTopoDb).exists()))
    }

    //TODO create a dummy dataset (from BD Topo) to run the test

    @Test
    @EnabledIfSystemProperty(named = "test.bdtopo", matches = "true")
    void importPreprocessTest(){
        H2GIS h2GISDatabase = H2GIS.open(bdTopoDb, "sa", "")
        def process = PrepareData.BDTopoGISLayers.importPreprocess()
        assertTrue process.execute([datasource: h2GISDatabase, tableIrisName: 'IRIS_GE', tableBuildIndifName: 'BATI_INDIFFERENCIE',
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
        process.getResults().each {
            entry -> assertNotNull(h2GISDatabase.getTable(entry.getValue()))
        }
    }

    @Test
    @EnabledIfSystemProperty(named = "test.bdtopo", matches = "true")
    void initTypes(){
        H2GIS h2GISDatabase = H2GIS.open("./target/myh2gisbdtopodb")
        def process = PrepareData.BDTopoGISLayers.initTypes()
        assertTrue process.execute([datasource: h2GISDatabase, buildingAbstractUseType: 'BUILDING_ABSTRACT_USE_TYPE',
                         roadAbstractType: 'ROAD_ABSTRACT_TYPE',  railAbstractType: 'RAIL_ABSTRACT_TYPE',
                         vegetAbstractType: 'VEGET_ABSTRACT_TYPE'])
        process.getResults().each {
            entry -> assertNull h2GISDatabase.getTable(entry.getValue())
        }
    }


}
