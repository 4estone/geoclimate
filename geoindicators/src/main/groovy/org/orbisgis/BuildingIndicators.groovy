package org.orbisgis

import groovy.transform.BaseScript
import org.orbisgis.datamanager.JdbcDataSource
import org.orbisgis.datamanagerapi.dataset.ITable
import org.orbisgis.processmanagerapi.IProcess

@BaseScript Geoclimate geoclimate

/**
 * This process extract geometry properties.
 * @return A database table name.
 * @author Erwan Bocher
 */
static IProcess geometryProperties() {
return processFactory.create(
        "Geometry properties",
        [inputTableName: String,inputFields:String[],operations: String[]
         , outputTableName: String, datasource: JdbcDataSource],
        [outputTableName : String],
        { inputTableName,inputFields, operations, outputTableName, datasource ->
            String query = "CREATE TABLE $outputTableName AS SELECT "
            def geometricField = "the_geom";
            def ops = ["st_geomtype","st_srid", "st_length","st_perimeter","st_area", "st_dimension",
                   "st_coorddim", "st_num_geoms", "st_num_pts", "st_issimple", "st_isvalid", "st_isempty"]

            operations.each {operation ->
                if(ops.contains(operation)){
                    query += "$operation($geometricField) as $operation,"
                }
            }
            query+= "${inputFields.join(",")} from $inputTableName"
            logger.info("Executing $query")
            datasource.execute query
            [outputTableName: outputTableName]
            }
)}

/**
 * This process extract building size properties.
 *
 * --> "building_volume": defined as the building area multiplied by the mean of the building
 * wall height and the building roof height.
 * --> "building_floor_area": defined as the number of level multiplied by the building area (cf. Bocher et al. - 2018)
 * --> "building_total_facade_length": defined as the total linear of facade (sum of the building perimeter and
 * the perimeter of the building courtyards)
 *
 * References:
 *   Bocher, E., Petit, G., Bernard, J., & Palominos, S. (2018). A geoprocessing framework to compute
 * urban indicators: The MApUCE tools chain. Urban climate, 24, 153-174.
 *
 * @return A database table name.
 * @author Jérémy Bernard
 */
static IProcess buildingSizeProperties() {
    return processFactory.create(
            "Building size properties",
            [inputBuildingTableName: String,inputFields:String[],operations: String[]
             , outputTableName: String, datasource: JdbcDataSource],
            [outputTableName : String],
            { inputBuildingTableName,inputFields, operations, outputTableName, datasource ->
                String query = "CREATE TABLE $outputTableName AS SELECT "
                def geometricField = "the_geom"
                def dist_passiv = 3
                def ops = ["building_volume","building_floor_area", "building_total_facade_length",
                           "building_passive_volume_ratio"]

                operations.each {operation ->
                    if(operation=="building_volume") {
                        query += "ST_AREA($geometricField)*0.5*(height_wall+height_roof) AS building_volume,"
                    }
                    else if(operation=="building_floor_area"){
                        query += "ST_AREA($geometricField)*nb_lev AS building_floor_area,"
                    }
                    else if(operation=="building_total_facade_length"){
                        query += "ST_PERIMETER($geometricField)+ST_PERIMETER(ST_HOLES($geometricField))" +
                                " AS building_total_facade_length,"
                    }
                    else if(operation=="building_passive_volume_ratio") {
                        query += "ST_AREA(ST_BUFFER($geometricField, -$dist_passiv, 'join=mitre'))/" +
                                "ST_AREA($geometricField) AS building_passive_volume_ratio,"
                    }
                }
                query+= "${inputFields.join(",")} FROM $inputBuildingTableName"
                logger.info("Executing $query")
                datasource.execute query
                [outputTableName: outputTableName]
            }
    )}


/**
 * This process extract building interactions properties.
 *
 * --> "building_contiguity": defined as the shared wall area divided by the total building wall area
 * (cf. Bocher et al. - 2018)
 * --> "building_common_wall_fraction": defined as ratio between the lenght of building wall shared with other buildings
 * and the length of total building walls
 * --> "building_number_building_neighbor": defined as the number of building  neighbors in contact with the building
 * (cf. Bocher et al. - 2018)
 *
 * References:
 *   Bocher, E., Petit, G., Bernard, J., & Palominos, S. (2018). A geoprocessing framework to compute
 * urban indicators: The MApUCE tools chain. Urban climate, 24, 153-174.
 *
 * @return A database table name.
 * @author Jérémy Bernard
 */
static IProcess buildingNeighborsProperties() {
    return processFactory.create(
            "Building interactions properties",
            [inputBuildingTableName: String,inputFields:String[],operations: String[]
             , outputTableName: String, datasource: JdbcDataSource],
            [outputTableName : String],
            { inputBuildingTableName,inputFields, operations, outputTableName, datasource ->
                def geometricField = "the_geom"
                def idField = "id_build"
                def height_wall = "height_wall"
                def ops = ["building_contiguity","building_common_wall_fraction",
                           "building_number_building_neighbor"]
                // To avoid overwriting the output files of this step, a unique identifier is created
                def uid_out = System.currentTimeMillis()
                // Temporary table names
                def build_intersec = "build_intersec"+uid_out.toString()


                String query = "CREATE INDEX IF NOT EXISTS buff_ids ON $inputBuildingTableName($geometricField) USING RTREE; " +
                        "CREATE INDEX IF NOT EXISTS buff_id ON $inputBuildingTableName($idField);" +
                        " CREATE TABLE $build_intersec AS SELECT "

                String query_update = ""

                operations.each {operation ->
                    if(operation=="building_contiguity") {
                        query += "sum(least(a.$height_wall, b.$height_wall)*" +
                                "st_length(ST_INTERSECTION(a.$geometricField,b.$geometricField)))/" +
                                "((ST_PERIMETER(a.$geometricField)+ST_PERIMETER(ST_HOLES(a.$geometricField)))*a.$height_wall)" +
                                " AS $operation,"
                    }
                    else if(operation=="building_common_wall_fraction"){
                        query += "sum(ST_LENGTH(ST_INTERSECTION(a.$geometricField, b.$geometricField)))/" +
                                "(ST_PERIMETER(a.$geometricField)+ST_PERIMETER(ST_HOLES(a.$geometricField))) " +
                                "AS $operation,"
                    }
                    else if(operation=="building_number_building_neighbor"){
                        query += "COUNT(ST_INTERSECTION(a.$geometricField, b.$geometricField))" +
                                " AS $operation,"
                    }
                    // The buildingNeighborProperty is set to 0 for the buildings that have no intersection with their building neighbors
                    if(ops.contains(operation)){
                        query_update+= "UPDATE $outputTableName SET $operation = 0 WHERE $operation IS null;"
                    }
                }
                query+= "a.${inputFields.join(",a.")} FROM $inputBuildingTableName a, $inputBuildingTableName b" +
                        " WHERE a.$geometricField && b.$geometricField AND " +
                        "ST_INTERSECTS(a.$geometricField, b.$geometricField) AND a.$idField <> b.$idField" +
                        " GROUP BY a.$idField;" +
                        "CREATE INDEX IF NOT EXISTS buff_id ON $build_intersec($idField);" +
                        "CREATE TABLE $outputTableName AS SELECT b.${operations.join(",b.")}, a.${inputFields.join(",a.")}" +
                        " FROM $inputBuildingTableName a LEFT JOIN $build_intersec b ON a.$idField = b.$idField;"
                query+= query_update

                // The temporary tables are deleted
                query+= "DROP TABLE IF EXISTS $build_intersec"

                logger.info("Executing $query")
                datasource.execute query
                [outputTableName: outputTableName]
            }
    )}


/**
 * This process extract building form properties.
 *
 * --> "building_concavity": defined as the building area divided by the convex hull area (cf. Bocher et al. - 2018)
 * --> "building_form_factor": defined as ratio between the building area divided by the square of the building
 * perimeter (cf. Bocher et al. - 2018)
 * --> "building_raw_compacity": defined as the ratio between building surfaces (walls and roof) divided by the
 * building volume at the power 2./3.
 *
 * References:
 *   Bocher, E., Petit, G., Bernard, J., & Palominos, S. (2018). A geoprocessing framework to compute
 * urban indicators: The MApUCE tools chain. Urban climate, 24, 153-174.
 *
 * @return A database table name.
 * @author Jérémy Bernard
 */
static IProcess buildingFormProperties() {
    return processFactory.create(
            "Building form properties",
            [inputBuildingTableName: String,inputFields:String[],operations: String[]
             , outputTableName: String, datasource: JdbcDataSource],
            [outputTableName : String],
            { inputBuildingTableName,inputFields, operations, outputTableName, datasource ->
                def geometricField = "the_geom"
                def height_wall = "height_wall"
                def ops = ["building_concavity","building_form_factor",
                           "building_raw_compacity"]

                String query = " CREATE TABLE $outputTableName AS SELECT "

                operations.each {operation ->
                    if(operation=="building_concavity"){
                        query += "ST_AREA($geometricField)/ST_AREA(ST_CONVEXHULL($geometricField)) AS $operation,"
                    }
                    else if(operation=="building_form_factor"){
                        query += "ST_AREA($geometricField)/POWER(ST_PERIMETER($geometricField), 2) AS $operation,"
                    }
                    else if(operation=="building_raw_compacity") {
                        query += "((ST_PERIMETER($geometricField)+ST_PERIMETER(ST_HOLES($geometricField)))*$height_wall+" +
                                "ST_AREA($geometricField))/POWER(ST_AREA($geometricField)*$height_wall, 2./3) AS $operation,"
                    }
                }
                query+= "${inputFields.join(",")} FROM $inputBuildingTableName"

                logger.info("Executing $query")
                datasource.execute query
                [outputTableName: outputTableName]
            }
    )}