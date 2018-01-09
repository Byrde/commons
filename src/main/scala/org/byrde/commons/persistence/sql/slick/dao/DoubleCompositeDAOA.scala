package org.byrde.commons.persistence.sql.slick.dao

import scala.concurrent.{ExecutionContext, Future}

abstract class DoubleCompositeDAOA[A, B, C](implicit ec: ExecutionContext)  {
	def toSeqView(f: () => Future[Seq[(B, C)]]): Future[Seq[A]] =
		f.apply.map(convert)
	def toView(f: () => Future[(B, C)]): Future[A] =
		f.apply.map(convert)
	def convert(view: Seq[(B, C)]): Seq[A] =
		view.map(convert)
	def convert(view: (B, C)): A
}
