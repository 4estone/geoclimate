package org.orbisgis

import org.orbisgis.datamanager.h2gis.H2GIS
import static org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class BuildingIndicatorsTests {

    @Test
    void testGeometryProperties() {
        def h2GIS = H2GIS.open([databaseName: './target/buildingdb'])
        h2GIS.execute("""
                DROP TABLE IF EXISTS spatial_table, geom_properties;
                CREATE TABLE spatial_table (id int, the_geom LINESTRING);
                INSERT INTO spatial_table VALUES (1, 'LINESTRING(0 0, 0 10)'::GEOMETRY);
        """)
       def  p =  Geoclimate.BuildingIndicators.geometryProperties()
       p.execute([inputTableName: "spatial_table", inputFields:["id", "the_geom"], operations:["st_issimple","st_area", "area", "st_dimension"], outputTableName : "geom_properties",datasource:h2GIS])
        assert p.results.outputTableName == "geom_properties"
        h2GIS.getTable("geom_properties").eachRow {
            row -> assert row.the_geom!=null
                assert row.st_issimple==true
                assertEquals(0,row.st_area)
                assertEquals(1, row.st_dimension)
                assertEquals(1,  row.id)
        }
    }

}
