package org.byrde.commons.persistence.dao

import scala.concurrent.{ExecutionContext, Future}

abstract class TertiaryCompositeDAOA[A, B, C, D](implicit ec: ExecutionContext) {
	def toSeqView(f: () => Future[Seq[(B, C, D)]]): Future[Seq[A]] =
		f.apply.map(convert)
	def toView(f: () => Future[(B, C, D)]): Future[A] =
		f.apply.map(convert)
	def convert(view: Seq[(B, C, D)]): Seq[A] =
		view.map(convert)
	def convert(view: (B, C, D)): A
}


