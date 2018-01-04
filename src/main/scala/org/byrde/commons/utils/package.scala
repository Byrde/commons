package org.byrde.commons

package object utils {
	def usingResource[B <: {def close(): Unit}, C](resource: B)(f: B => C): C =
		try {
			f(resource)
		} finally {
			resource.close()
		}
}



