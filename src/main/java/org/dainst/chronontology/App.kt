package org.dainst.chronontology

import org.apache.logging.log4j.LogManager
import org.dainst.chronontology.config.*

import java.util.Properties

/**
 * Main class. Handles wiring of application components.

 * @author Daniel de Oliveira
 */
class App(controller: Controller) {

    var controller: Controller? = null

    init {
        this.controller = controller
    }

    companion object {

        internal val logger = LogManager.getLogger(App::class.java)
        private val DEFAULT_PROPERTIES_FILE_PATH = "config.properties"

        @JvmStatic
        fun main(args: Array<String>) {

            AppConfigurator().configure(
                    makeAppConfigFrom(properties(DEFAULT_PROPERTIES_FILE_PATH)))
        }

        private fun properties(path: String): Properties {

            val props = PropertiesLoader.loadConfiguration(path)
            if (props == null) {
                logger.error("Could not load properties from file: config.properties.")
                System.exit(1)
            }
            return props
        }

        private fun makeAppConfigFrom(props: Properties): AppConfig {

            val appConfig = AppConfig()
            if (appConfig.validate(props) == false) {
                for (err in appConfig.constraintViolations) {
                    logger.error(err)
                }
                System.exit(1)
            }
            return appConfig
        }
    }
}
