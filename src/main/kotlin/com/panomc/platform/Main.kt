package com.panomc.platform

import com.panomc.platform.di.component.ApplicationComponent
import com.panomc.platform.di.component.DaggerApplicationComponent
import com.panomc.platform.di.module.LoggerModule
import com.panomc.platform.di.module.RouterModule
import com.panomc.platform.di.module.VertxModule
import io.vertx.core.AbstractVerticle
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.core.logging.Logger
import io.vertx.core.logging.LoggerFactory
import io.vertx.ext.web.Router
import javax.inject.Inject

class Main : AbstractVerticle() {

    companion object {
        private val mOptions = VertxOptions()
        private val mVertx = Vertx.vertx(mOptions)
        private val mLogger = LoggerFactory.getLogger("Pano Platform")

        val ENVIRONMENT =
            if (System.getenv("EnvironmentType").isNullOrEmpty())
                EnvironmentType.RELEASE
            else
                EnvironmentType.DEVELOPMENT

        const val PORT = 8088

        @JvmStatic
        fun main(args: Array<String>) {
            mVertx.deployVerticle(Main())
        }

        private val mComponent: ApplicationComponent by lazy {
            DaggerApplicationComponent
                .builder()
                .vertxModule(VertxModule(mVertx))
                .loggerModule(LoggerModule(mLogger))
                .routerModule(RouterModule(mVertx))
                .build()
        }

        internal fun getComponent() = mComponent

        enum class EnvironmentType {
            DEVELOPMENT, RELEASE
        }
    }

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var router: Router

    override fun start(startFuture: Future<Void>) {
        vertx.executeBlocking<Any>({ future ->
            init().onComplete { init ->
                future.complete(init.result())
            }
        }, {
            startWebServer()
        })
    }

    private fun init() = Future.future<Boolean> { init ->
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