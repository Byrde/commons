package org.byrde.pubsub

import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.{FixedTransportChannelProvider, TransportChannelProvider}
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder

object PubSubChannelProvider {
  private val pubsubEmulatorHost = "PUBSUB_EMULATOR_HOST"
  def apply(): Option[TransportChannelProvider] = {
    val emulatorHostPort = System.getenv().getOrDefault(pubsubEmulatorHost, "")
    if (emulatorHostPort.isEmpty) {
      None
    } else {
      val channel: ManagedChannel = ManagedChannelBuilder.forTarget(emulatorHostPort).usePlaintext().build()
      Some(FixedTransportChannelProvider.create(GrpcTransportChannel.create(channel)))
    }
  }
}
