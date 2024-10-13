package com.whim.component.auth.repository

import com.whim.generic.{Containable, Deletable, Modifiable, OperationResult, OperationSuccess}

import scala.collection.mutable
import scala.collection.mutable.Set


//This is a temporary-solution. The real solution will utilise a caching db.
object OAuthStateRepository extends Modifiable[String], Deletable[String], Containable[String]{

  private val internalSet: mutable.Set[String] = mutable.Set()

  override def createOrUpdate(value: String): OperationResult = {
    internalSet += value
    OperationSuccess
  }

  override def delete(value: String): OperationResult = {
    internalSet -= value
    OperationSuccess
  }

  override def contains(value: String): Boolean = internalSet(value)

}
