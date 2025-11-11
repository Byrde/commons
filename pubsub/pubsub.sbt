name := """pubsub"""

description := "Pub/sub messaging abstraction with Google Cloud Pub/Sub implementation."

homepage := Some(url("https://github.com/Byrde/commons/tree/main/pubsub"))

libraryDependencies ++=
  Dependencies.GooglePubSubDependencies ++ Dependencies.CirceDependencies
