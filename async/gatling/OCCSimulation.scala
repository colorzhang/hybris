package hybris

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class OCCSimulation extends Simulation {

    val rampUpTimeSecs = 20
    val testTimeSecs   = 60
    val noOfUsers      = 1000

    val baseURL      = "http://localhost:8088"
    val baseName     = "occ"
    val requestName  = baseName + "-request"
    val scenarioName = baseName + "-scenario"
    val URI          = "/electronics/products/493683"

    val httpConf = http
		.baseURL(baseURL)
		.disableCaching
		//.disableWarmUp

    val http_headers = Map(
      "Accept-Encoding" -> "gzip,deflate",
      "Content-Type" -> "text/json;charset=UTF-8",
      "Keep-Alive" -> "115")

    val scn = scenario(scenarioName)
      .during(testTimeSecs) {
        exec(
          http(requestName)
            .get(URI)
            .headers(http_headers)
            .check(status.is(200))
        )
      }

   setUp(
	  scn.inject(
		  rampUsers(noOfUsers) over(rampUpTimeSecs)
		  //rampUsersPerSec(100) to 200 during(rampUpTimeSecs) randomized
		  //atOnceUsers(1)
		  //constantUsersPerSec(200) during(rampUpTimeSecs)
		  )
   ).protocols(httpConf)
		
		//.throttle(reachRps(100) in rampUpTimeSecs, holdFor(testTimeSecs))
}
