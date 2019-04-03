package org.orbisgis.osm

import org.orbisgis.processmanager.ProcessManager
import org.orbisgis.processmanagerapi.IProcessFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory

abstract class PrepareData extends Script {

    public static IProcessFactory processFactory = ProcessManager.getProcessManager().factory("prepareData")

    public static Logger logger = LoggerFactory.getLogger(PrepareData.class)


    public static OSMGISLayers = new OSMGISLayers()
}
