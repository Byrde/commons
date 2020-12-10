# PubSub

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
