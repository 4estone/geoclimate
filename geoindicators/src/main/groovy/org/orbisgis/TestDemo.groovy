package org.orbisgis

import org.orbisgis.datamanager.h2gis.H2GIS

def h2GIS = H2GIS.open([databaseName: './target/loadH2GIS'])
h2GIS.execute("""
                DROP TABLE IF EXISTS spatial_table;
                CREATE TABLE spatial_table (id int, the_geom point);
                INSERT INTO spatial_table VALUES (1, 'POINT(10 10)'::GEOMETRY), (2, 'POINT(1 1)'::GEOMETRY);
        """)

Geoclimate.IndicatorDemo.demoProcess.execute([inputA: h2GIS.getSpatialTable("spatial_table")])
assert Geoclimate.IndicatorDemo.demoProcess.results.outputA == ["ID", "THE_GEOM"]