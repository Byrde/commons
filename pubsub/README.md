# PubSub [![Latest Version @ Cloudsmith](https://api-prd.cloudsmith.io/badges/version/byrde/libraries/maven/pubsub_2.13/latest/x/?render=true)](https://cloudsmith.io/~byrde/repos/libraries/packages/detail/maven/pubsub_2.13/latest/)

Wrapper for the PubSub Alpakka connector.

## How to install

* add to your dependencies library dependencies:
```libraryDependencies += "org.byrde" %% "pubsub" % "VERSION"```

* add this resolver to your resolvers dependencies:
```resolvers += "byrde-libraries" at "https://dl.cloudsmith.io/public/byrde/libraries/maven/"```

## Config Sample
```yaml
{
  project-id = "projectId"
  client-email = "client@email.com"
  private-key = ${privateKey}
}
```
