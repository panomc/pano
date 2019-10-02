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
    }

    @Inject
    lateinit var logger: Logger

    @Inject
    lateinit var router: Router

    override fun start(startFuture: Future<Void>) {
        vertx.executeBlocking<Any>({ future ->
            init().setHandler { init ->
                future.complete(init.result())
            }
        }, {
            startWebServer(startFuture)
        })
    }

    private fun init() = Future.future<Boolean> { init ->
        getComponent().inject(this)

        init.complete(true)
    }

    private fun startWebServer(startFuture: Future<Void>) {
        val port = 8088

        vertx
            .createHttpServer()
            .requestHandler(router)
            .listen(port) { result ->
                if (result.succeeded()) {
                    logger.info("Started listening port $port")
                    startFuture.complete()
                } else {
                    logger.error("Failed to listen port $port")
                    startFuture.fail(result.cause())
                }
            }
    }
}