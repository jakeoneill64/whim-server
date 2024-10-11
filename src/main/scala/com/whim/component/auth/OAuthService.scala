package com.whim.component.auth

import scala.concurrent.Future

trait OAuthService {

  def getRedirect: Future[String]

}
