package org.byrde.commons.utils.concurrent

case class Timer(t0: Long = System.currentTimeMillis) {
	val startTime: Long = System.currentTimeMillis
	def currentTime: Long = System.currentTimeMillis
	def elapsedTime: Long = System.currentTimeMillis - t0
}
