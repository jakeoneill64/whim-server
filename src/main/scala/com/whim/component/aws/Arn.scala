package com.whim.component.aws

import com.whim.component.Parser

import scala.util.Try

case class Arn(
  partition: String,
  service: String,
  region: String,
  accountId: String,
  resource: String,
  resourceType: String,
  qualifier: String
)

case object Arn extends Parser[Try[Arn]]{

  private val pattern = "arn:([^:]+):([^:]+):([^:]*):([^:]*):(.+)".r

  override def parse(toParse: String): Try[Arn] =

    Try {
      toParse match {
        case pattern(partition, service, region, accountId, resourceStr) =>
          val (resType, resource, qualifier) = resourceStr.split("[/:]").toList match {
            case resType :: res :: Nil => (resType, res, "")
            case resType :: res :: qual :: Nil => (resType, res, qual)
            case res :: Nil => ("", res, "")
            case _ => throw new IllegalArgumentException("Invalid resource format in ARN")
          }

          Arn(partition, service, region, accountId, resource, resType, qualifier)

        case _ =>
          throw new IllegalArgumentException("Invalid ARN format")
      }
    }

}

given arnParser: Parser[Try[Arn]] = Arn