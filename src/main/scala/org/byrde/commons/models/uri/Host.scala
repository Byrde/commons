package org.byrde.commons.models.uri

case class Host(protocol: Protocol = Protocol.http, host: String, port: Option[Port] = None) {
	override def toString: String =
		protocol.protocol + host + port.fold("")(p => s":${p.port}")
}
