package org.byrde.akka.http.guice

import com.google.inject.assistedinject.FactoryModuleBuilder
import com.google.inject.{ Binder, Module }

import net.codingwell.scalaguice.InternalModule

import scala.reflect.{ ClassTag, _ }

trait AssistedInjectFactoryScalaModule[B <: Binder] extends Module {
  self: InternalModule[B] =>

  protected[this] def bindFactory[C <: Any: ClassTag, F: ClassTag](): Unit =
    bindFactory[C, C, F]()

  protected[this] def bindFactory[I: ClassTag, C <: I: ClassTag, F: ClassTag](): Unit =
    binderAccess
      .install(
        new FactoryModuleBuilder()
          .implement(classTag[I].runtimeClass.asInstanceOf[Class[I]], classTag[C].runtimeClass.asInstanceOf[Class[C]])
          .build(classTag[F].runtimeClass.asInstanceOf[Class[F]])
      )

}