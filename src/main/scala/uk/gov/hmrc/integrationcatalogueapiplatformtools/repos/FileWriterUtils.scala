package uk.gov.hmrc.integrationcatalogueapiplatformtools.repos

trait FileWriterUtils {

    // $COVERAGE-OFF$
  def writeToFile(filename: String, content: String): Unit = {
    import java.io.{BufferedWriter, File, FileWriter}

    val file = new File(filename)
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(content)
    bw.close()
  }
  // $COVERAGE-ON$

}