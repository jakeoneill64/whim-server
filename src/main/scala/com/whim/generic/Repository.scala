package com.whim.generic


sealed trait OperationResult
case object OperationSuccess extends OperationResult
case class OperationFailure(failure: Throwable) extends OperationResult

trait Modifiable [StoredType] {
  def createOrUpdate(value: StoredType): OperationResult
}

trait Deletable [IndexType] {
  def delete(id: IndexType): OperationResult
}

trait Readable [StoredType, IndexType] {
  def get(id: IndexType): Option[StoredType]
}

trait Containable [IndexType] {
  def contains(id: IndexType): Boolean
}

trait Transactional {

  def transaction(step: () => Unit): OperationResult

}