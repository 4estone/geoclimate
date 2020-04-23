package org.orbisgis.orbisprocess.geoclimate.processingchain

import groovy.transform.BaseScript
import org.orbisgis.orbisdata.datamanager.jdbc.JdbcDataSource
import org.orbisgis.orbisprocess.geoclimate.geoindicators.Geoindicators

@BaseScript ProcessingChain processingChain

/**
 * Compute the geoindicators at building scale
 *
 * @param indicatorUse The use defined for the indicator. Depending on this use, only a part of the indicators could
 * be calculated (default is all indicators : ["LCZ", "URBAN_TYPOLOGY", "TEB"])
 * 
 * @return
 */
def computeBuildingsIndicators() {
    def final BASE_NAME = "building_indicators"
    return create({
        title "Compute the geoindicators at building scale"
        inputs datasource: JdbcDataSource, inputBuildingTableName: String, inputRoadTableName: String,
                indicatorUse: ["LCZ", "URBAN_TYPOLOGY", "TEB"], prefixName: ""
        outputs outputTableName: String
        run { datasource, inputBuildingTableName, inputRoadTableName, indicatorUse, prefixName ->

            info "Start computing building indicators..."

            def idColumnBu = "id_build"

            // Maps for intermediate or final joins
            def finalTablesToJoin = [:]
            finalTablesToJoin.put(inputBuildingTableName, idColumnBu)

            // The name of the outputTableName is constructed
            def outputTableName = getOutputTableName(prefixName, BASE_NAME)
            def buildingPrefixName = "building_indicator_"
            def buildTableJoinNeighbors = "A$uuid"

            // building_area + building_perimeter
            def geometryOperations = ["st_area"]
            if (indicatorUse*.toUpperCase().contains("URBAN_TYPOLOGY")) {
                geometryOperations = ["st_perimeter", "st_area"]
            }
            def computeGeometryProperties = Geoindicators.GenericIndicators.geometryProperties()
            if (!computeGeometryProperties([inputTableName: inputBuildingTableName, inputFields: ["id_build"],
                                            operations    : geometryOperations, prefixName: buildingPrefixName,
                                            datasource    : datasource])) {
                info "Cannot compute the length,perimeter,area properties of the buildings"
                return
            }
            def buildTableGeometryProperties = computeGeometryProperties.results.outputTableName
            finalTablesToJoin.put(buildTableGeometryProperties, idColumnBu)

            // For indicators that are useful for urban_typology OR for LCZ classification
            if (indicatorUse*.toUpperCase().contains("LCZ") || indicatorUse*.toUpperCase().contains("URBAN_TYPOLOGY")) {
                // building_volume + building_floor_area + building_total_facade_length
                def sizeOperations = ["volume", "floor_area", "total_facade_length"]
                if (!indicatorUse*.toUpperCase().contains("URBAN_TYPOLOGY")) {
                    sizeOperations = ["total_facade_length"]
                }
                def computeSizeProperties = Geoindicators.BuildingIndicators.sizeProperties()
                if (!computeSizeProperties([inputBuildingTableName: inputBuildingTableName,
                                            operations            : sizeOperations,
                                            prefixName            : buildingPrefixName,
                                            datasource            : datasource])) {
                    info "Cannot compute the building_volume, building_floor_area, building_total_facade_length " +
                            "indicators for the buildings"
                    return
                }
                def buildTableSizeProperties = computeSizeProperties.results.outputTableName
                finalTablesToJoin.put(buildTableSizeProperties, idColumnBu)

                // building_contiguity + building_common_wall_fraction + building_number_building_neighbor
                def neighborOperations = ["contiguity", "common_wall_fraction", "number_building_neighbor"]
                if (indicatorUse*.toUpperCase().contains("LCZ") && !indicatorUse*.toUpperCase().contains("URBAN_TYPOLOGY")) {
                    neighborOperations = ["contiguity"]
                }
                def computeNeighborsProperties = Geoindicators.BuildingIndicators.neighborsProperties()
                if (!computeNeighborsProperties([inputBuildingTableName: inputBuildingTableName,
                                                 operations            : neighborOperations,
                                                 prefixName            : buildingPrefixName,
                                                 datasource            : datasource])) {
                    info "Cannot compute the building_contiguity, building_common_wall_fraction, " +
                            "number_building_neighbor indicators for the buildings"
                    return
                }
                def buildTableComputeNeighborsProperties = computeNeighborsProperties.results.outputTableName
                finalTablesToJoin.put(buildTableComputeNeighborsProperties, idColumnBu)

                if (indicatorUse*.toUpperCase().contains("URBAN_TYPOLOGY")) {
                    // area_concavity + building_form_factor + building_raw_compactness + perimeter_convexity
                    def computeFormProperties = Geoindicators.BuildingIndicators.formProperties()
                    if (!computeFormProperties([inputBuildingTableName: inputBuildingTableName,
                                                operations            : ["area_concavity", "form_factor",
                                                                         "raw_compactness",
                                                                         "perimeter_convexity"],
                                                prefixName            : buildingPrefixName,
                                                datasource            : datasource])) {
                        info "Cannot compute the area_concavity, form_factor, raw_compactness, " +
                                "perimeter_convexity indicators for the buildings"
                        return
                    }
                    def buildTableFormProperties = computeFormProperties.results.outputTableName
                    finalTablesToJoin.put(buildTableFormProperties, idColumnBu)

                    // building_minimum_building_spacing
                    def computeMinimumBuildingSpacing = Geoindicators.BuildingIndicators.minimumBuildingSpacing()
                    if (!computeMinimumBuildingSpacing([inputBuildingTableName: inputBuildingTableName,
                                                        bufferDist            : 100,
                                                        prefixName            : buildingPrefixName, datasource: datasource])) {
                        info "Cannot compute the minimum building spacing indicator"
                        return
                    }
                    def buildTableComputeMinimumBuildingSpacing = computeMinimumBuildingSpacing.results.outputTableName
                    finalTablesToJoin.put(buildTableComputeMinimumBuildingSpacing, idColumnBu)

                    // building_road_distance
                    def computeRoadDistance = Geoindicators.BuildingIndicators.roadDistance()
                    if (!computeRoadDistance([inputBuildingTableName: inputBuildingTableName,
                                              inputRoadTableName    : inputRoadTableName,
                                              bufferDist            : 100,
                                              prefixName            : buildingPrefixName,
                                              datasource            : datasource])) {
                        info "Cannot compute the closest minimum distance to a road at 100 meters."
                        return
                    }
                    def buildTableComputeRoadDistance = computeRoadDistance.results.outputTableName
                    finalTablesToJoin.put(buildTableComputeRoadDistance, idColumnBu)

                    // Join for building_likelihood
                    def computeJoinNeighbors = Geoindicators.DataUtils.joinTables()
                    if (!computeJoinNeighbors([inputTableNamesWithId: [(buildTableComputeNeighborsProperties): idColumnBu,
                                                                       (inputBuildingTableName)              : idColumnBu],
                                               outputTableName      : buildingPrefixName + "_neighbors",
                                               datasource           : datasource])) {
                        info "Cannot join the number of neighbors of a building."
                        return
                    }
                    buildTableJoinNeighbors = computeJoinNeighbors.results.outputTableName

                    // building_likelihood_large_building
                    def computeLikelihoodLargeBuilding = Geoindicators.BuildingIndicators.likelihoodLargeBuilding()
                    if (!computeLikelihoodLargeBuilding([inputBuildingTableName: buildTableJoinNeighbors,
                                                         nbOfBuildNeighbors    : "number_building_neighbor",
                                                         prefixName            : buildingPrefixName,
                                                         datasource            : datasource])) {
                        info "Cannot compute the like lihood large building indicator."
                        return
                    }
                    def buildTableComputeLikelihoodLargeBuilding = computeLikelihoodLargeBuilding.results.outputTableName
                    finalTablesToJoin.put(buildTableComputeLikelihoodLargeBuilding, idColumnBu)
                }
            }

            def buildingTableJoin = Geoindicators.DataUtils.joinTables()
            if (!buildingTableJoin([inputTableNamesWithId: finalTablesToJoin,
                                    outputTableName      : buildingPrefixName,
                                    datasource           : datasource])) {
                info "Cannot merge all indicator in the table $buildingPrefixName."
                return
            }

            // Rename the last table to the right output table name
            datasource.execute "DROP TABLE IF EXISTS $outputTableName;" +
                    "ALTER TABLE ${buildingTableJoin.results.outputTableName} RENAME TO $outputTableName"

            // Remove all intermediate tables (indicators alone in one table)
            // Recover all created tables in an array
            def finTabNames = finalTablesToJoin.keySet().toArray()
            // Remove the block table from the list of "tables to remove" (since it needs to be conserved)
            finTabNames = finTabNames - inputBuildingTableName
            datasource.execute """DROP TABLE IF EXISTS ${finTabNames.join(",")}, $buildTableJoinNeighbors"""

            [outputTableName: outputTableName]

        }
    })
}


/**
 * Compute the geoindicators at block scale
 *
 * @return
 */
def computeBlockIndicators(){
    def final BASE_NAME = "block_indicators"
    return create({
        title "Compute the geoindicators at block scale"
        inputs datasource: JdbcDataSource, inputBuildingTableName: String, inputBlockTableName: String, prefixName: ""
        outputs outputTableName: String
        run { datasource, inputBuildingTableName, inputBlockTableName, prefixName ->

            info "Start computing block indicators..."
            // The name of the outputTableName is constructed
            def outputTableName = getOutputTableName(prefixName, BASE_NAME)
            def blockPrefixName = "block_indicator_"
            def id_block = "id_block"
            def id_build = "id_build"

            // Maps for intermediate or final joins
            def finalTablesToJoin = [:]
            finalTablesToJoin.put(inputBlockTableName, id_block)

            //Compute :
            //Sum of the building area
            //Sum of the building volume composing the block
            //Sum of block floor area
            def computeSimpleStats = Geoindicators.GenericIndicators.unweightedOperationFromLowerScale()
            if(!computeSimpleStats([inputLowerScaleTableName: inputBuildingTableName,
                                    inputUpperScaleTableName: inputBlockTableName,
                                    inputIdUp               : id_block,
                                    inputIdLow              : id_build,
                                    inputVarAndOperations   : ["area"               :["SUM"],
                                                               "floor_area":["SUM"],
                                                               "volume" :["SUM"]],
                                    prefixName: blockPrefixName,
                                    datasource: datasource])){
                info "Cannot compute the sum of of the building area, building volume and block floor area."
                return
            }

            finalTablesToJoin.put(computeSimpleStats.results.outputTableName, id_block)

            //Ratio between the holes area and the blocks area
            // block_hole_area_density
            def computeHoleAreaDensity = Geoindicators.BlockIndicators.holeAreaDensity()
            if(!computeHoleAreaDensity(blockTable: inputBlockTableName,
                                       prefixName: blockPrefixName,
                                       datasource: datasource)){
                info "Cannot compute the hole area density."
                return
            }
            finalTablesToJoin.put(computeHoleAreaDensity.results.outputTableName, id_block)

            //Perkins SKill Score of the distribution of building direction within a block
            // block_perkins_skill_score_building_direction
            def computePerkinsSkillScoreBuildingDirection = Geoindicators.GenericIndicators.buildingDirectionDistribution()
            if(!computePerkinsSkillScoreBuildingDirection([buildingTableName: inputBuildingTableName,
                                                           inputIdUp        : id_block,
                                                           angleRangeSize   : 15,
                                                           prefixName       : blockPrefixName,
                                                           datasource       : datasource])) {
                info "Cannot compute perkins skill indicator. "
                return
            }
            finalTablesToJoin.put(computePerkinsSkillScoreBuildingDirection.results.outputTableName, id_block)


            //Block closingness
            def computeClosingness = Geoindicators.BlockIndicators.closingness()
            if(!computeClosingness(correlationTableName: inputBuildingTableName,
                                   blockTable          : inputBlockTableName,
                                   prefixName          : blockPrefixName,
                                   datasource          : datasource)){
                info "Cannot compute closingness indicator. "
                return
            }
            finalTablesToJoin.put(computeClosingness.results.outputTableName, id_block)

            //Block net compactness
            def computeNetCompactness = Geoindicators.BlockIndicators.netCompactness()
            if(!computeNetCompactness([buildTable             : inputBuildingTableName,
                                     buildingVolumeField    : "volume",
                                     buildingContiguityField: "contiguity",
                                     prefixName             : blockPrefixName,
                                     datasource             : datasource])){
                info "Cannot compute the net compactness indicator. "
                return
            }
            finalTablesToJoin.put(computeNetCompactness.results.outputTableName, id_block)

            //Block mean building height
            //Block standard deviation building height
            def computeWeightedAggregatedStatistics = Geoindicators.GenericIndicators.weightedAggregatedStatistics()
            if(!computeWeightedAggregatedStatistics([inputLowerScaleTableName : inputBuildingTableName,
                                                     inputUpperScaleTableName : inputBlockTableName,
                                                     inputIdUp                : id_block,
                                                     inputVarWeightsOperations: ["height_roof": ["area": ["AVG", "STD"]]],
                                                     prefixName               : blockPrefixName,
                                                     datasource               : datasource])){
                info "Cannot compute the block mean building height and standard deviation building height indicators. "
                return
            }
            finalTablesToJoin.put(computeWeightedAggregatedStatistics.results.outputTableName, id_block)

            //Merge all in one table
            def blockTableJoin = Geoindicators.DataUtils.joinTables()
            if(!blockTableJoin([inputTableNamesWithId: finalTablesToJoin,
                                outputTableName: blockPrefixName,
                                datasource: datasource])){
                info "Cannot merge all tables in $blockPrefixName. "
                return
            }

            // Rename the last table to the right output table name
            datasource.execute "DROP TABLE IF EXISTS $outputTableName;" +
                    "ALTER TABLE ${blockTableJoin.results.outputTableName} RENAME TO $outputTableName"

            // Modify all indicators which do not have the expected name
            def listColumnNames = datasource.getTable(outputTableName).columns
            def mapIndic2Change = ["SUM_AREA": "AREA", "SUM_FLOOR_AREA": "FLOOR_AREA",
                                   "SUM_VOLUME": "VOLUME"]
            def query2ModifyNames = ""
            for (ind in mapIndic2Change.keySet()){
                if (listColumnNames.contains(ind)) {
                    query2ModifyNames += "ALTER TABLE $outputTableName RENAME COLUMN $ind TO ${mapIndic2Change[ind]};"
                }
            }
            if (query2ModifyNames != ""){
                datasource.execute query2ModifyNames
            }

            // Remove all intermediate tables (indicators alone in one table)
            // Recover all created tables in an array
            def finTabNames = finalTablesToJoin.keySet().toArray()
            // Remove the block table from the list of "tables to remove" (since it needs to be conserved)
            finTabNames = finTabNames - inputBlockTableName
            datasource.execute """DROP TABLE IF EXISTS ${finTabNames.join(",")}"""

            [outputTableName: outputTableName]
        }
    })
}

/**
 * Compute the geoindicators at RSU scale
 *
 * @param buildingTable The table where are stored informations concerning buildings (and the id of the corresponding rsu)
 * @param rsuTable The table where are stored informations concerning RSU
 * @param roadTable The table where are stored informations concerning roads
 * @param vegetationTable The table where are stored informations concerning vegetation
 * @param hydrographicTable The table where are stored informations concerning water
 * @param imperviousTable The table where are stored the impervious areas
 * @param facadeDensListLayersBottom the list of height corresponding to the bottom of each vertical layers used for calculation
 * of the rsu_projected_facade_area_density which is then used to calculate the height of roughness (default [0, 10, 20, 30, 40, 50])
 * @param facadeDensNumberOfDirection The number of directions used for the calculation - according to the method used it should
 * be divisor of 360 AND a multiple of 2 (default 12)
 * @param pointDensity The density of points (nb / free m²) used to calculate the spatial average SVF (default 0.008)
 * @param rayLength The maximum distance to consider an obstacle as potential sky cover (default 100)
 * @param numberOfDirection the number of directions considered to calculate the SVF (default 60)
 * @param heightColumnName The name of the column (in the building table) used for roughness length calculation (default "height_roof")
 * @param fractionTypePervious The type of surface that should be consider to calculate the fraction of pervious soil
 * (default ["low_vegetation", "water"] but possible parameters are ["low_vegetation", "high_vegetation", "water"])
 * @param fractionTypeImpervious The type of surface that should be consider to calculate the fraction of impervious soil
 * (default ["road"] but possible parameters are ["road", "building"])
 * @param inputFields The fields of the buildingTable that should be kept in the analysis (default ["the_geom", "id_build"]
 * @param levelForRoads If the road surfaces are considered for the calculation of the impervious fraction,
 * you should give the list of road zindex to consider (default [0])
 * @param angleRangeSizeBuDirection The range size (in °) of each interval angle used to calculate the distribution
 * of building direction (used in the Perkins Skill Score direction - should be a divisor of 180 - default 15°)
 * @param prefixName A prefix used to name the output table
 * @param svfSimplified A boolean to use a simplified version of the SVF calculation (default false)
 * @param indicatorUse The use defined for the indicator. Depending on this use, only a part of the indicators could
 * be calculated (default is all indicators : ["LCZ", "URBAN_TYPOLOGY", "TEB"])
 * @param surfSuperpositions Map where are stored the overlaying layers as keys and the overlapped
 * layers as values. Note that the priority order for the overlapped layers is taken according to the priority variable
 * name and (default ["high_vegetation": ["water", "building", "low_vegetation", "road", "impervious"]])
 * @param surfPriorities List indicating the priority order to set between layers in order to remove potential double count
 * of overlapped layers (for example a geometry containing water and low_vegetation must be either water
 * or either low_vegetation, not both (default ["water", "building", "high_vegetation", "low_vegetation",
 * "road", "impervious"]
 * @param urbanTypoSurfFraction Map containing as key the name of the fraction indicators useful for the urban typology classification
 * and as value a list of the fractions that have to be summed up to calculate the indicator. No need to modify
 * these values if not interested by the urban typology
 * @param lczSurfFraction Map containing as key the name of the fraction indicators useful for the LCZ classification
 * and as value a list of the fractions that have to be summed up to calculate the indicator. No need to modify
 * these values if not interested by the lcz classification.
 * @param buildingFractions List of fractions to sum to calculate the total building fraction which is useful as input of the aspect ratio
 * @param datasource A connection to a database
 *
 * @return
 */
def computeRSUIndicators() {
    def final BASE_NAME = "rsu_indicators"

    return create({
        title "Compute the geoindicators at RSU scale"
        inputs  datasource                 : JdbcDataSource,   buildingTable               : "",
                rsuTable                   : "",           prefixName                  : "",
                vegetationTable            : "",           roadTable                   : "",
                hydrographicTable          : "",           imperviousTable             : "",
                facadeDensListLayersBottom  : [0, 10, 20, 30, 40, 50],
                facadeDensNumberOfDirection: 12,               svfPointDensity             : 0.008,
                svfRayLength               : 100,              svfNumberOfDirection        : 60,
                heightColumnName           : "height_roof",
                inputFields                : ["id_build", "the_geom"],
                levelForRoads              : [0],              angleRangeSizeBuDirection   : 30,
                svfSimplified              : false,
                indicatorUse               : ["LCZ", "URBAN_TYPOLOGY", "TEB"],
                surfSuperpositions         : ["high_vegetation": ["water", "building", "low_vegetation", "road", "impervious"]],
                surfPriorities             : ["water", "building", "high_vegetation", "low_vegetation", "road", "impervious"],
                urbanTypoSurfFraction      : ["vegetation_fraction_urb"                 : ["high_vegetation_fraction",
                                                                                           "low_vegetation_fraction",
                                                                                           "high_vegetation_low_vegetation_fraction",
                                                                                           "high_vegetation_road_fraction",
                                                                                           "high_vegetation_impervious_fraction",
                                                                                           "high_vegetation_water_fraction",
                                                                                           "high_vegetation_building_fraction"],
                                             "low_vegetation_fraction_urb"                  : ["low_vegetation_fraction"],
                                             "high_vegetation_impervious_fraction_urb"  : ["high_vegetation_road_fraction",
                                                                                           "high_vegetation_impervious_fraction"],
                                             "high_vegetation_pervious_fraction_urb"    : ["high_vegetation_fraction",
                                                                                           "high_vegetation_low_vegetation_fraction",
                                                                                           "high_vegetation_water_fraction"],
                                             "road_fraction_urb"                        : ["road_fraction",
                                                                                           "high_vegetation_road_fraction"],
                                             "impervious_fraction_urb"                  : ["road_fraction",
                                                                                           "high_vegetation_road_fraction",
                                                                                           "impervious_fraction",
                                                                                           "high_vegetation_impervious_fraction"]],
                lczSurfFraction             : ["building_fraction_lcz"                  : ["building_fraction",
                                                                                           "high_vegetation_building_fraction"],
                                              "pervious_fraction_lcz"                   : ["high_vegetation_fraction",
                                                                                           "low_vegetation_fraction",
                                                                                           "water_fraction",
                                                                                           "high_vegetation_low_vegetation_fraction",
                                                                                           "high_vegetation_water_fraction"],
                                              "high_vegetation_fraction_lcz"            : ["high_vegetation_fraction",
                                                                                           "high_vegetation_low_vegetation_fraction",
                                                                                           "high_vegetation_road_fraction",
                                                                                           "high_vegetation_impervious_fraction",
                                                                                           "high_vegetation_water_fraction",
                                                                                           "high_vegetation_building_fraction"],
                                              "low_vegetation_fraction_lcz"             : ["low_vegetation_fraction"],
                                              "impervious_fraction_lcz"                 : ["impervious_fraction",
                                                                                            "road_fraction",
                                                                                            "high_vegetation_impervious_fraction",
                                                                                            "high_vegetation_road_fraction"],
                                              "water_fraction_lcz"                      : ["water_fraction",
                                                                                            "high_vegetation_water_fraction"]],
                buildingFractions          : ["high_vegetation_building_fraction","building_fraction"]
        outputs outputTableName: String
        run { datasource                , buildingTable                     , rsuTable,
              prefixName                , vegetationTable                   , roadTable,
              hydrographicTable         , imperviousTable,
              facadeDensListLayersBottom        , facadeDensNumberOfDirection,
              svfPointDensity           , svfRayLength                      , svfNumberOfDirection,
              heightColumnName          , inputFields                       , levelForRoads,
              angleRangeSizeBuDirection , svfSimplified                     , indicatorUse,
              surfSuperpositions        , surfPriorities                    , urbanTypoSurfFraction,
              lczSurfFraction           , buildingFractions ->

            info "Start computing RSU indicators..."
            def to_start = System.currentTimeMillis()

            def columnIdRsu = "id_rsu"
            def columnIdBuild = "id_build"

            // Maps for intermediate or final joins
            def finalTablesToJoin = [:]
            def intermediateJoin = [:]
            finalTablesToJoin.put(rsuTable, columnIdRsu)
            intermediateJoin.put(rsuTable, columnIdRsu)

            // Name of the output table
            def outputTableName = getOutputTableName(prefixName, BASE_NAME)

            // PrefixName for intermediate table (start with a letter to avoid table name issue if start with a number)
            def temporaryPrefName = "rsu_indicator_"

            // Other temporary tables that have to be deleted at the end of the process
            def urbanTypoFractionIndic = "urban_typo_fraction_indic"
            def lczFractionIndic = "lcz_fraction_indic"
            def preAspectRatioTable = "pre_HW_table"

            // Intermediate table that needs to be delete at the end
            def SVF = "SVF"
            def computeExtFF

            // Calculate all surface fractions indicators
            // Need to create the smallest geometries used as input of the surface fraction process
            def  computeSmallestGeom =  Geoindicators.RsuIndicators.smallestCommunGeometry()
            if (!computeSmallestGeom.execute([
                    rsuTable: rsuTable,buildingTable: buildingTable,roadTable : roadTable, vegetationTable: vegetationTable,waterTable: hydrographicTable,
                    imperviousTable:imperviousTable,
                    prefixName: temporaryPrefName, datasource: datasource])){
                info "Cannot compute the smallest commun geometries"
                return
            }
            def superpositionsTable = computeSmallestGeom.results.outputTableName
            // Calculate the surface fractions from the commun geom
            def  computeSurfaceFractions =  Geoindicators.RsuIndicators.surfaceFractions()
            if (!computeSurfaceFractions.execute([
                    rsuTable: rsuTable, spatialRelationsTable: superpositionsTable,
                    superpositions: surfSuperpositions,
                    priorities: surfPriorities,
                    prefixName: temporaryPrefName, datasource: datasource])){
                info "Cannot compute the surface fractions"
                return
            }
            def surfaceFractions = computeSurfaceFractions.results.outputTableName
            finalTablesToJoin.put(surfaceFractions, columnIdRsu)

            // Get all column names from the surfaceFraction IProcess to make verifications
            def surfFracList = datasource.getTable(surfaceFractions).getColumns()

            // Calculate the surface fractions needed for the urban typology classification
            if (indicatorUse*.toUpperCase().contains("URBAN_TYPOLOGY")) {
                logger.info """Processing urban typology surface fraction calculation"""
                // Get all columns needed for the calculations and verify that they exist
                def neededSurfUrb = urbanTypoSurfFraction.findResults { k, v -> true ? v : null }.flatten()
                def missingElementsUrb = neededSurfUrb - neededSurfUrb.findAll { indUrb -> surfFracList.contains(indUrb.toUpperCase()) }
                if (missingElementsUrb.size() == 0) {
                    def queryUrbSurfFrac = """DROP TABLE IF EXISTS $urbanTypoFractionIndic;
                                            CREATE TABLE $urbanTypoFractionIndic AS SELECT $columnIdRsu, """
                    urbanTypoSurfFraction.each { urbIndicator, indicatorList ->
                        queryUrbSurfFrac += "${indicatorList.join("+")} AS $urbIndicator, "
                    }
                    queryUrbSurfFrac += " FROM $surfaceFractions"
                    datasource.execute queryUrbSurfFrac
                    finalTablesToJoin.put(urbanTypoFractionIndic, columnIdRsu)
                } else {
                    logger.error """'urbanTypoSurfFraction' and 'surfSuperpositions' parameters given by the user are not consistent.
                                    Impossible to find the following indicators in the surface fractions table: ${missingElementsUrb.join(", ")}"""
                }
            }


            // Calculate the surface fractions needed for the LCZ classification
            if (indicatorUse*.toUpperCase().contains("LCZ")) {
                logger.info """Processing LCZ surface fraction indicators calculation"""
                // Get all columns needed for the calculations and verify that they exist
                def neededSurfLcz = urbanTypoSurfFraction.findResults { k, v -> true ? v : null }.flatten()
                def missingElementsLcz = neededSurfLcz - neededSurfLcz.findAll { indLcz -> surfFracList.contains(indLcz.toUpperCase()) }
                if (missingElementsLcz.size() == 0) {
                    def querylczSurfFrac = """DROP TABLE IF EXISTS $lczFractionIndic;
                                                CREATE TABLE $lczFractionIndic AS SELECT $columnIdRsu, """
                    lczSurfFraction.each { urbIndicator, indicatorList ->
                        querylczSurfFrac += "${indicatorList.join("+")} AS $urbIndicator, "
                    }
                    querylczSurfFrac += " FROM $surfaceFractions"
                    datasource.execute querylczSurfFrac
                    finalTablesToJoin.put(lczFractionIndic, columnIdRsu)
                } else {
                    logger.error """'lczSurfFraction' and 'surfSuperpositions' parameters given by the user are not consistent.
                                    Impossible to find the following indicators in the surface fractions table: ${missingElementsLcz.join(", ")}"""
                }
            }

            // rsu_area (note that the uuid is used as prefix for intermediate tables - indicator alone in a table)
            if (indicatorUse*.toUpperCase().contains("URBAN_TYPOLOGY")) {
                def computeGeometryProperties = Geoindicators.GenericIndicators.geometryProperties()
                if (!computeGeometryProperties([inputTableName: rsuTable, inputFields: [columnIdRsu],
                                                operations    : ["st_area"], prefixName: temporaryPrefName,
                                                datasource    : datasource])) {
                    info "Cannot compute the area of the RSU"
                    return
                }
                def rsuTableGeometryProperties = computeGeometryProperties.results.outputTableName
                finalTablesToJoin.put(rsuTableGeometryProperties, columnIdRsu)
            }


            // Building free external facade density
            if (indicatorUse*.toUpperCase().contains("URBAN_TYPOLOGY") || indicatorUse*.toUpperCase().contains("LCZ")) {
                def computeFreeExtDensity = Geoindicators.RsuIndicators.freeExternalFacadeDensity()
                if (!computeFreeExtDensity([buildingTable            : buildingTable, rsuTable: rsuTable,
                                            buContiguityColumn       : "contiguity",
                                            buTotalFacadeLengthColumn: "total_facade_length",
                                            prefixName               : temporaryPrefName,
                                            datasource               : datasource])) {
                    info "Cannot compute the free external facade density for the RSU"
                    return
                }
                def rsu_free_ext_density = computeFreeExtDensity.results.outputTableName
                intermediateJoin.put(rsu_free_ext_density, columnIdRsu)
            }

            // rsu_building_density + rsu_building_volume_density + rsu_mean_building_volume
            // + rsu_mean_building_neighbor_number + rsu_building_floor_density + rsu_roughness_length
            // + rsu_building_number_density (RSU number of buildings divided RSU area)
            def inputVarAndOperations = [:]
            if (indicatorUse*.toUpperCase().contains("LCZ") || indicatorUse*.toUpperCase().contains("TEB")) {
                inputVarAndOperations = inputVarAndOperations << [(heightColumnName): ["GEOM_AVG"]]
            }
            if (indicatorUse*.toUpperCase().contains("URBAN_TYPOLOGY")) {
                inputVarAndOperations = inputVarAndOperations << ["volume"                  : ["DENS", "AVG"],
                                                                  (heightColumnName)        : ["GEOM_AVG"],
                                                                  "number_building_neighbor": ["AVG"],
                                                                  "floor_area"              : ["DENS"],
                                                                  "minimum_building_spacing": ["AVG"],
                                                                  "building": ["NB_DENS"]]
            }
            def computeRSUStatisticsUnweighted = Geoindicators.GenericIndicators.unweightedOperationFromLowerScale()
            if (!computeRSUStatisticsUnweighted([inputLowerScaleTableName: buildingTable,
                                                 inputUpperScaleTableName: rsuTable,
                                                 inputIdUp               : columnIdRsu,
                                                 inputIdLow              : columnIdBuild,
                                                 inputVarAndOperations   : inputVarAndOperations,
                                                 prefixName              : temporaryPrefName,
                                                 datasource              : datasource])) {
                info "Cannot compute the statistics : building, building volume densities, building number density" +
                        " and mean building neighbor number for the RSU"
                return
            }
            def rsuStatisticsUnweighted = computeRSUStatisticsUnweighted.results.outputTableName
            // Join in an intermediate table (for perviousness fraction)
            intermediateJoin.put(rsuStatisticsUnweighted, columnIdRsu)


            // rsu_mean_building_height weighted by their area + rsu_std_building_height weighted by their area.
            if (indicatorUse*.toUpperCase().contains("URBAN_TYPOLOGY")) {
                def computeRSUStatisticsWeighted = Geoindicators.GenericIndicators.weightedAggregatedStatistics()
                if (!computeRSUStatisticsWeighted([inputLowerScaleTableName : buildingTable,
                                                   inputUpperScaleTableName : rsuTable,
                                                   inputIdUp                : columnIdRsu,
                                                   inputVarWeightsOperations: ["height_roof"    : ["area": ["AVG", "STD"]]],
                                                   prefixName               : temporaryPrefName,
                                                   datasource               : datasource])) {
                    info "Cannot compute the weighted indicators mean, std height building and \n\
                    mean volume building."
                    return
                }
                def rsuStatisticsWeighted = computeRSUStatisticsWeighted.results.outputTableName
                finalTablesToJoin.put(rsuStatisticsWeighted, columnIdRsu)
            }

            // rsu_linear_road_density + rsu_road_direction_distribution
            if (indicatorUse*.toUpperCase().contains("URBAN_TYPOLOGY") || indicatorUse*.toUpperCase().contains("TEB")) {
                def roadOperations = ["linear_road_density"]
                if (indicatorUse*.toUpperCase().contains("TEB")) {
                    roadOperations = ["road_direction_distribution", "linear_road_density"]
                }
                def computeLinearRoadOperations = Geoindicators.RsuIndicators.linearRoadOperations()
                if (!computeLinearRoadOperations([rsuTable         : rsuTable,
                                                  roadTable        : roadTable,
                                                  operations       : roadOperations,
                                                  levelConsiderated: [0],
                                                  datasource       : datasource,
                                                  prefixName       : temporaryPrefName])) {
                    info "Cannot compute the linear road density and road direction distribution"
                    return
                }
                def linearRoadOperations = computeLinearRoadOperations.results.outputTableName
                finalTablesToJoin.put(linearRoadOperations, columnIdRsu)
            }

            // rsu_free_vertical_roof_area_distribution + rsu_free_non_vertical_roof_area_distribution
            if (indicatorUse*.toUpperCase().contains("TEB")) {
                def computeRoofAreaDist = Geoindicators.RsuIndicators.roofAreaDistribution()
                if (!computeRoofAreaDist([rsuTable        : rsuTable,
                                          buildingTable   : buildingTable,
                                          listLayersBottom: facadeDensListLayersBottom,
                                          prefixName      : temporaryPrefName,
                                          datasource      : datasource])) {
                    info "Cannot compute the roof area distribution. "
                    return
                }
                def roofAreaDist = computeRoofAreaDist.results.outputTableName
                finalTablesToJoin.put(roofAreaDist, columnIdRsu)
            }

            // rsu_projected_facade_area_distribution
            if (indicatorUse*.toUpperCase().contains("LCZ") || indicatorUse*.toUpperCase().contains("TEB")) {
                if (!indicatorUse*.toUpperCase().contains("TEB")) {
                    facadeDensListLayersBottom:
                    [0, 50, 200]
                    facadeDensNumberOfDirection: 8
                }
                def computeProjFacadeDist = Geoindicators.RsuIndicators.projectedFacadeAreaDistribution()
                if (!computeProjFacadeDist([buildingTable    : buildingTable,
                                            rsuTable         : rsuTable,
                                            listLayersBottom : facadeDensListLayersBottom,
                                            numberOfDirection: facadeDensNumberOfDirection,
                                            prefixName       : temporaryPrefName,
                                            datasource       : datasource])) {
                    info "Cannot compute the projected facade distribution. "
                    return
                }
                def projFacadeDist = computeProjFacadeDist.results.outputTableName
                intermediateJoin.put(projFacadeDist, columnIdRsu)
            }

            // // Need to have the total building fraction in one indicator (by default building alone fraction and building high vegetation fractions are separated)
            if (indicatorUse*.toUpperCase().contains("LCZ") || indicatorUse*.toUpperCase().contains("URBAN_TYPOLOGY")) {
                datasource.execute """DROP TABLE IF EXISTS $preAspectRatioTable;
                                   CREATE TABLE $preAspectRatioTable 
                                        AS SELECT $columnIdRsu, ${buildingFractions.join("+")} AS BUILDING_TOTAL_FRACTION 
                                        FROM $surfaceFractions"""
                intermediateJoin.put(preAspectRatioTable, columnIdRsu)
            }

            // Create an intermediate join tables to have all needed input fields for future indicator calculation
            def computeIntermediateJoin = Geoindicators.DataUtils.joinTables()
            if (!computeIntermediateJoin([inputTableNamesWithId: intermediateJoin,
                                          outputTableName      : "tab4aspratio",
                                          datasource           : datasource])) {
                info "Cannot merge the tables used for aspect ratio calculation. "
                return
            }
            def intermediateJoinTable = computeIntermediateJoin.results.outputTableName
            finalTablesToJoin.put(intermediateJoinTable, columnIdRsu)


            // rsu_aspect_ratio
            if (indicatorUse*.toUpperCase().contains("LCZ") || indicatorUse*.toUpperCase().contains("URBAN_TYPOLOGY")) {
                def computeAspectRatio = Geoindicators.RsuIndicators.aspectRatio()
                if (!computeAspectRatio([rsuTable                          : intermediateJoinTable,
                                         rsuFreeExternalFacadeDensityColumn: "free_external_facade_density",
                                         rsuBuildingDensityColumn          : "BUILDING_TOTAL_FRACTION",
                                         prefixName                        : temporaryPrefName,
                                         datasource                        : datasource])) {
                    info "Cannot compute the aspect ratio calculation "
                    return
                }
                def aspectRatio = computeAspectRatio.results.outputTableName
                finalTablesToJoin.put(aspectRatio, columnIdRsu)
            }

            // rsu_ground_sky_view_factor
            if (indicatorUse*.toUpperCase().contains("LCZ")) {
                // If the fast version is chosen (SVF derived from extended RSU free facade fraction
                if (svfSimplified == true) {
                    computeExtFF =  Geoindicators.RsuIndicators.extendedFreeFacadeFraction()
                    if (!computeExtFF([buildingTable: buildingTable,
                                          rsuTable: intermediateJoinTable,
                                          buContiguityColumn: "contiguity",
                                          buTotalFacadeLengthColumn: "total_facade_length",
                                          prefixName: temporaryPrefName, buffDist : 10, datasource: datasource])){
                        info "Cannot compute the SVF calculation. "
                        return
                        }
                    datasource.execute "DROP TABLE IF EXISTS $SVF; CREATE TABLE SVF " +
                            "AS SELECT 1-extended_free_facade_fraction AS GROUND_SKY_VIEW_FACTOR, $columnIdRsu " +
                            "FROM ${computeExtFF.results.outputTableName}"
                    }
                else {
                    def computeSVF = Geoindicators.RsuIndicators.groundSkyViewFactor()
                    if (!computeSVF([rsuTable                : intermediateJoinTable,
                                     correlationBuildingTable: buildingTable,
                                     pointDensity            : svfPointDensity,
                                     rayLength               : svfRayLength,
                                     numberOfDirection       : svfNumberOfDirection,
                                     prefixName              : temporaryPrefName,
                                     datasource              : datasource])) {
                        info "Cannot compute the SVF calculation. "
                        return
                        }
                    SVF = computeSVF.results.outputTableName
                    }
                finalTablesToJoin.put(SVF, columnIdRsu)
            }

            // rsu_effective_terrain_roughness
            if (indicatorUse*.toUpperCase().contains("LCZ") || indicatorUse*.toUpperCase().contains("TEB")) {
                // Create the join tables to have all needed input fields for aspect ratio computation
                def computeEffRoughHeight = Geoindicators.RsuIndicators.effectiveTerrainRoughnessLength()
                if (!computeEffRoughHeight([rsuTable                       : intermediateJoinTable,
                                            projectedFacadeAreaName        : "projected_facade_area_distribution",
                                            geometricMeanBuildingHeightName: "geom_avg_$heightColumnName",
                                            prefixName                     : temporaryPrefName,
                                            listLayersBottom               : facadeDensListLayersBottom,
                                            numberOfDirection              : facadeDensNumberOfDirection,
                                            datasource                     : datasource])) {
                    info "Cannot compute the projected_facade_area_distribution."
                    return
                }
                def effRoughHeight = computeEffRoughHeight.results.outputTableName
                finalTablesToJoin.put(effRoughHeight, columnIdRsu)

                // rsu_terrain_roughness_class
                if (indicatorUse*.toUpperCase().contains("LCZ")){
                    def computeRoughClass = Geoindicators.RsuIndicators.effectiveTerrainRoughnessClass()
                    if (!computeRoughClass([datasource                     : datasource,
                                            rsuTable                       : effRoughHeight,
                                            effectiveTerrainRoughnessLength: "effective_terrain_roughness_length",
                                            prefixName                     : temporaryPrefName])) {
                        info "Cannot compute the SVF calculation."
                        return
                    }
                    def roughClass = computeRoughClass.results.outputTableName
                    finalTablesToJoin.put(roughClass, columnIdRsu)
                }
            }

            // rsu_perkins_skill_score_building_direction_variability
            if (indicatorUse*.toUpperCase().contains("URBAN_TYPOLOGY")) {
                 def computePerkinsDirection = Geoindicators.GenericIndicators.buildingDirectionDistribution()
                if (!computePerkinsDirection([buildingTableName: buildingTable, inputIdUp: columnIdRsu,
                                              angleRangeSize   : angleRangeSizeBuDirection, prefixName: temporaryPrefName,
                                              datasource       : datasource])) {
                    info "Cannot compute the perkins Skill Score building direction distribution."
                    return
                }
                def perkinsDirection = computePerkinsDirection.results.outputTableName
                finalTablesToJoin.put(perkinsDirection, columnIdRsu)
            }

            // Merge all in one table
            // To avoid duplicate the_geom in the join table, remove it from the intermediate table
            datasource.execute("ALTER TABLE $intermediateJoinTable DROP COLUMN the_geom;")
            def rsuTableJoin = Geoindicators.DataUtils.joinTables()
            if (!rsuTableJoin([inputTableNamesWithId: finalTablesToJoin,
                               outputTableName      : outputTableName,
                               datasource           : datasource])) {
                info "Cannot merge all tables. "
                return
            }

            // Modify all indicators which do not have the expected name
            def listColumnNames = datasource.getTable(outputTableName).columns
            def mapIndic2Change = ["FLOOR_AREA_DENSITY"     : "BUILDING_FLOOR_AREA_DENSITY",
                                   "VOLUME_DENSITY"         : "BUILDING_VOLUME_DENSITY",
                                   "LINEAR_ROAD_DENSITY_H0" : "GROUND_LINEAR_ROAD_DENSITY"]
            def query2ModifyNames = ""
            for (ind in mapIndic2Change.keySet()){
                if (listColumnNames.contains(ind)) {
                    query2ModifyNames += "ALTER TABLE $outputTableName RENAME COLUMN $ind TO ${mapIndic2Change[ind]};"
                }
            }
            if (query2ModifyNames != ""){
                datasource.execute query2ModifyNames
            }

            // Remove all intermediate tables (indicators alone in one table)
            // Recover all created tables in an array
            def interTabNames = intermediateJoin.keySet().toArray()
            def finTabNames = finalTablesToJoin.keySet().toArray()
            // Remove the RSU table from the list of "tables to remove" (since it needs to be conserved)
            interTabNames = interTabNames - rsuTable
            finTabNames = finTabNames - rsuTable
            datasource.execute """DROP TABLE IF EXISTS ${interTabNames.join(",")}, 
                                                    ${finTabNames.join(",")}, $SVF"""

            def tObis = System.currentTimeMillis() - to_start

            info "Geoindicators calculation time: ${tObis / 1000} s"
            [outputTableName: outputTableName]

        }
    })
}