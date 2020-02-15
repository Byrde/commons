package org.byrde.client.http.play

import play.api.libs.ws.{StandaloneWSRequest, StandaloneWSResponse}

import org.byrde.client.http.HttpClient

trait PlayHttpClient extends HttpClient[PlayService, StandaloneWSRequest, StandaloneWSResponse] with PlayExecutor