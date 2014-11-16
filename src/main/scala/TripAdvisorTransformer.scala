import java.net.{HttpURLConnection, URL, URLEncoder}
import java.util.logging.{Level, Logger}

import de.fuberlin.wiwiss.silk.linkagerule.input.Transformer
import de.fuberlin.wiwiss.silk.runtime.plugin.Plugin

import scala.io.Source

/**
 *
 * @since 1.0.0
 *
 * @param appKey
 * @param limit
 */
@Plugin(
  id = "tripadvisor",
  categories = Array("API"),
  label = "TripAdvisor",
  description = "Returns the TripAdvisor location Id."
)
case class TripAdvisorTransformer(appKey: String, prefix: String = "", limit: Int = 1) extends Transformer {

  private val logger = Logger.getLogger(getClass.getName)

  /**
   *
   * @since 1.0.0
   *
   * @param values
   * @return
   */
  override def apply(values: Seq[Set[String]]): Set[String] = {

    // Get a vector from the Seq[Set[...]] in order to access the elements by their index to build the query string.
    val parameters = values.reduce(_ union _).toVector

    parameters.size match {

      // Return an empty set if the parameters size is less than 3.
      case size if size < 2 =>
        logger.log(Level.WARNING, s"Expected 2 parameters [ parameters size :: $size ]")
        Set()

      // Process the parameters.
      case _ =>

        TripAdvisorClient.mapLocation(appKey, parameters(0), parameters(1))
          .getOrElse(Set())
          .map(prefix + _) // add the prefix.
          .slice(0, limit)

    }

  }

}

/**
 * @since 1.0.0
 */
object TripAdvisorTransformer {}

/**
 * @since 1.0.0
 */
object TripAdvisorClient {

  import play.api.libs.json._

  private val logger = Logger.getLogger(TripAdvisorClient.getClass.getName)

  val API_URL = "http://api.tripadvisor.com/api/partner/2.0/"

  def mapLocation(appKey: String, latLng: String, name: String): Option[Set[String]] = {

    val path = s"location_mapper/$latLng"
    val queryString = "?key=" + appKey + "-mapper&q=" + URLEncoder.encode(name, "UTF-8")

    request(path, queryString)

  }

  /**
   * Perform a request to MapQuest.
   *
   * @since 1.0.0
   *
   * @param path
   * @param queryString
   * @return
   */
  def request(path: String, queryString: String): Option[Set[String]] = {

    logger.log(Level.FINE, s"Going to perform a request to TripAdvisor [ path :: $path ][ query-string :: $queryString ]")

    // Combine the request in one URL.
    val url = new URL(API_URL + path + queryString)
    val connection = url.openConnection().asInstanceOf[HttpURLConnection]
    connection.setRequestMethod("GET")
    connection.setRequestProperty("Accept", "application/json")
    connection.connect()

    // Process only successful responses.
    connection.getResponseCode match {

      // Error response code.
      case code: Int if code < 200 || code >= 300 =>
        val responseMessage = connection.getResponseMessage
        logger.log(Level.WARNING, s"An error occurred [ code :: $code ][ url :: $url ][ response :: $responseMessage ].") // error
        None // Return no results.

      // Success (2xx)
      case _ =>

        // Get the response string using the specified encoding (or use UTF-8 by default).
        val encoding = if (null != connection.getContentEncoding) connection.getContentEncoding else "UTF-8"
        val responseString = Source.fromInputStream(connection.getInputStream, encoding).mkString

        // Decode the JSON.
        val json: JsValue = Json.parse(responseString)
        logger.log(Level.FINER, s"[ response-string :: $responseString ]")


        // Check Foursquare response.
        (json \ "data").as[JsArray] match {

          case results if 0 == results.value.size =>
            // No results / no locations.
            logger.log(Level.FINE, "No results.")
            None

          case results =>
            val location_id = (results(0) \ "location_id").as[String]

            logger.log(Level.FINE, s"Found a result [ location id :: $location_id ].")

            Some(Set(location_id))

        }

    }

  }

}
