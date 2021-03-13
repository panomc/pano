package com.panomc.platform

import com.panomc.platform.config.ConfigManager
import com.panomc.platform.di.component.ApplicationComponent
import com.panomc.platform.di.component.DaggerApplicationComponent
import com.panomc.platform.di.module.*
import io.vertx.core.*
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import java.net.URLClassLoader
import java.util.jar.Manifest
import javax.inject.Inject

class Main : AbstractVerticle() {
    private val mLogger = LoggerFactory.getLogger("Pano Platform")
    private val mConfigManager by lazy {
        ConfigManager(mLogger, vertx)
    }

    companion object {
        private val mOptions by lazy {
            VertxOptions()
        }
        private val mVertx by lazy {
            Vertx.vertx(mOptions)
        }

        private val mURLClassLoader = Main::class.java.classLoader as URLClassLoader
        private val mMode by lazy {
            try {
                val manifestUrl = mURLClassLoader.findResource("META-INF/MANIFEST.MF")
                val manifest = Manifest(manifestUrl.openStream())

                manifest.mainAttributes.getValue("MODE").toString()
            } catch (e: Exception) {
                ""
            }
        }

        val ENVIRONMENT =
            if (mMode != "DEVELOPMENT" && System.getenv("EnvironmentType").isNullOrEmpty())
                EnvironmentType.RELEASE
            else
                EnvironmentType.DEVELOPMENT

        const val PORT = 8088

        @JvmStatic
        fun main(args: Array<String>) {
            mVertx.deployVerticle(Main())
        }

        private lateinit var mComponent: ApplicationComponent

        internal fun getComponent() = mComponent

        enum class EnvironmentType {
            DEVELOPMENT, RELEASE
        }
    }

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var router: Router

    override fun start(startPromise: Promise<Void>?) {
        vertx.executeBlocking<Any>({ future ->
            init().onComplete { init ->
                future.complete(init.result())
            }
        }, {
            startWebServer()
        })
    }

    private fun init() = Future.future<Boolean> { init ->
        mComponent = DaggerApplicationComponent
            .builder()
            .vertxModule(VertxModule(vertx))
            .loggerModule(LoggerModule(mLogger))
            .routerModule(RouterModule(vertx))
            .configManagerModule(ConfigManagerModule(mConfigManager))
            .mailClientModule(MailClientModule(mConfigManager))
            .build()

        getComponent().inject(this)

        init.complete(true)
    }

    private fun startWebServer() {
        vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(PORT) { result ->
                if (result.succeeded())
                    logger.info("Started listening port $PORT")
                else
                    logger.error("Failed to listen port $PORT")
            }
    }
}