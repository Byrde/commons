package org.byrde.http

package object server {
  type AnyMaterializedEndpoint = MaterializedEndpoint[_, _, _, _, _]
  
  type AnyMaterializedEndpoints = Seq[AnyMaterializedEndpoint]
}
