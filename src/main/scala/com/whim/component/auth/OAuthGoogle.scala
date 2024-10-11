package com.whim.component.auth


import com.github.scribejava.core.builder.ServiceBuilder
import java.util.Random
import java.util.Scanner
import java.io.IOException
import java.util
import java.util.concurrent.ExecutionException
import com.github.scribejava.core.model.Response
import com.github.scribejava.core.model.Verb
import com.github.scribejava.core.oauth.OAuth20Service
import com.github.scribejava.apis.GoogleApi20
import com.whim.component.config.given

import scala.concurrent.Future
import scala.jdk.CollectionConverters._

object OAuthGoogle extends OAuthService{

  private val authConfiguration: Future[AuthConfiguration] = AuthConfiguration()
  private val oauthService: Future[OAuth20Service] = authConfiguration.map {
    config =>
      val GoogleAuthConfig(clientId, clientSecret) = config.googleConfig
      new ServiceBuilder(clientId)
            .apiSecret(clientSecret)
            .callback("http://localhost/auth/google/callback")
            .defaultScope("profile")
            .build(GoogleApi20.instance());

  }


  override def getRedirect: Future[String] =
    oauthService.map {
      service =>

      val additionalParams: Map[String, String] = Map(
        "access_type" -> "offline",
        "prompt" -> "consent"
      )

      service.createAuthorizationUrlBuilder.additionalParams(additionalParams.asJava).build()

    }

}

//object Google20Example {
//  private val NETWORK_NAME = "Google"
//  private val PROTECTED_RESOURCE_URL = "https://www.googleapis.com/oauth2/v3/userinfo"
//
//  def main(args: String*): Unit = {
//    val clientId = "your_client_id"
//    val clientSecret = "your_client_secret"
//    val secretState = "secret" + new Random().nextInt(999_999)
//    val service = new ServiceBuilder(clientId).apiSecret(clientSecret).defaultScope("profile") // replace with desired scope.callback("http://example.com/callback").build(GoogleApi20.instance)
//    val in = new Scanner(System.in, "UTF-8")
//    System.out.println("=== " + NETWORK_NAME + "'s OAuth Workflow ===")
//    System.out.println()
//    // Obtain the Authorization URL
//    System.out.println("Fetching the Authorization URL...")
//    //pass access_type=offline to get refresh token
//    //https://developers.google.com/identity/protocols/OAuth2WebServer#preparing-to-start-the-oauth-20-flow
//    val additionalParams = new util.HashMap[String, String]
//    additionalParams.put("access_type", "offline")
//    //force to reget refresh token (if user are asked not the first time)
//    additionalParams.put("prompt", "consent")
//    val authorizationUrl = service.createAuthorizationUrlBuilder.state(secretState).additionalParams(additionalParams).build
//    System.out.println("Got the Authorization URL!")
//    System.out.println("Now go and authorize ScribeJava here:")
//    System.out.println(authorizationUrl)
//    System.out.println("And paste the authorization code here")
//    System.out.print(">>")
//    val code = in.nextLine
//    System.out.println()
//    System.out.println("And paste the state from server here. We have set 'secretState'='" + secretState + "'.")
//    System.out.print(">>")
//    val value = in.nextLine
//    if (secretState == value) System.out.println("State value does match!")
//    else {
//      System.out.println("Ooops, state value does not match!")
//      System.out.println("Expected = " + secretState)
//      System.out.println("Got      = " + value)
//      System.out.println()
//    }
//    System.out.println("Trading the Authorization Code for an Access Token...")
//    var accessToken = service.getAccessToken(code)
//    System.out.println("Got the Access Token!")
//    System.out.println("(The raw response looks like this: " + accessToken.getRawResponse + "')")
//    System.out.println("Refreshing the Access Token...")
//    accessToken = service.refreshAccessToken(accessToken.getRefreshToken)
//    System.out.println("Refreshed the Access Token!")
//    System.out.println("(The raw response looks like this: " + accessToken.getRawResponse + "')")
//    System.out.println()
//    // Now let's go and ask for a protected resource!
//    System.out.println("Now we're going to access a protected resource...")
//    while (true) {
//      System.out.println("Paste fieldnames to fetch (leave empty to get profile, 'exit' to stop example)")
//      System.out.print(">>")
//      val query = in.nextLine
//      System.out.println()
//      var requestUrl: String = null
//      if ("exit" == query) break //todo: break is not supported
//      else if (query == null || query.isEmpty) requestUrl = PROTECTED_RESOURCE_URL
//      else requestUrl = PROTECTED_RESOURCE_URL + "?fields=" + query
//      val request = new Nothing(Verb.GET, requestUrl)
//      service.signRequest(accessToken, request)
//      System.out.println()
//      try {
//        val response = service.execute(request)
//        try {
//          System.out.println(response.getCode)
//          System.out.println(response.getBody)
//        } finally if (response != null) response.close()
//      }
//      System.out.println()
//    }
//  }
//}
