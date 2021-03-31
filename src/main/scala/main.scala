import java.io.FileReader
import org.apache.commons.csv.CSVRecord
import scala.collection.JavaConverters._
import webapi.WebApiBaseUnit
import webapi.Raml10
import webapi.Oas30
import java.util.concurrent.TimeUnit
import scala.util.Try
import scala.util.Failure
import scala.util.Success
import scala.concurrent.java8.FuturesConvertersImpl


object Main extends App {

  case class Api(name: String, version: String, ramlPath : Option[String])

  def createRecord(record: CSVRecord) : Api = {

    def ramlPath = 
      if (record.size() > 4) {
        Some(record.get(4))
      } else None

    Api(
      record.get(0),
      record.get(2),
      ramlPath
    )
  }

  val in = new FileReader("apis-to-import.csv")

  val apisToProcess = 
    org.apache.commons.csv.CSVFormat.EXCEL
      .withFirstRecordAsHeader()
      .withDelimiter(';')
      .parse(in).getRecords().asScala.toSeq
      .map(createRecord)

      
      
  // def getGitCloneCommands(apisToProcess : Seq[Api]) = {
  //   apisToProcess
  //     .map(api => s"git clone git@github.com:hmrc/${api.name}.git")
  //     .mkString("\n")
  // }

  // println(getGitCloneCommands(apisToProcess))

  def parseRamlFromGitRepo(apis: Seq[Api]) : Unit = {

    def tryParseFile(api: Api, filename: String) : Try[Unit] = {
      Try({
        // println(s"Starting API: ${api.name}, ${api.version} filename:\n${filename}")

        val model : WebApiBaseUnit = Raml10.parse(filename).get()

        val outputFilepath = s"file://generated/${api.name}-${api.version}.yaml"

        val f = Oas30.generateYamlFile(model, outputFilepath)
        f.get(60, TimeUnit.SECONDS)

        // Oas30.generateFile(model, outputFilepath)

        // println("Generated Oas30 YAML file at: " + outputFilepath); 
      })
    }

    apis.foreach(api => {

      val ramlPath = api.ramlPath.getOrElse("resources/public/api/conf")

      val filename = s"file://api-repos/${api.name}/$ramlPath/${api.version}/application.raml"

      val result = tryParseFile(api,filename);

      result match {
        case Failure(exception) => {
          println(s"failed: ${api.name}, ${api.version} - filename: ${filename} ${exception.toString}")
        }
        case Success(value) => Unit 
      }
    })
  }

  parseRamlFromGitRepo(apisToProcess)

  // ApiPlatformOasExport.export()
}

