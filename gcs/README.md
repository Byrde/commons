# GCS

Wrapper for the GCS Alpakka connector.

## How to install

* add to your dependencies library dependencies:
```libraryDependencies += "org.byrde" %% "gcs" % "VERSION"```

* add this resolver to your resolvers dependencies:
```resolvers += "byrde-libraries" at "https://dl.cloudsmith.io/public/byrde/libraries/maven/"```

## Config Sample
```yaml
alpakka.google.cloud.storage {
  project-id = "projectId"
  client-email = "client@email.com"
  private-key = ${privateKey}
}
```