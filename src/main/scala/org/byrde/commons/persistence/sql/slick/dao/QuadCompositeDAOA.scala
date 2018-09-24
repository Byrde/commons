package org.byrde.commons.persistence.sql.slick.dao

import scala.concurrent.{ExecutionContext, Future}

abstract class QuadCompositeDAOA[A, B, C, D, E](implicit ec: ExecutionContext)  {
	def toSeqView(f: () => Future[Seq[(B, C, D, E)]]): Future[Seq[A]] =
		f.apply.map(convert)

	def toView(f: () => Future[(B, C, D, E)]): Future[A] =
		f.apply.map(convert)

	def convert(view: Seq[(B, C, D, E)]): Seq[A] =
		view.map(convert)

	def convert(view: (B, C, D, E)): A
}
