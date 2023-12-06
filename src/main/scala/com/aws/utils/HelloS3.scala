package com.aws.utils

// Java Imports
import java.time.Instant
import java.text.SimpleDateFormat
import java.util._
import java.nio.file.{Files, Paths, StandardCopyOption}

// Amazon Imports
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, ListObjectsV2Request, ListObjectsV2Response, S3Exception, S3Object}

object HelloS3 {
  def main(args: Array[String]): Unit = {
    val region = Region.US_EAST_1
    val s3 = S3Client.builder.region(region).build
    val bucketName = "<bucketName>"
    val prefixName = "raw"

    val format = new SimpleDateFormat("yMMdd")
    val dateFormated = format.format(Calendar.getInstance().getTime())

    val objectList = listObjects(s3, bucketName, prefixName, dateFormated)
    val lastFile = findMaxDateObject(objectList)

    if (lastFile != null && !lastFile.isEmpty()){
      downloadObject(s3, bucketName, lastFile, "files_downloaded/")
    }
    else{
      println("O arquivo esta vazio ou nao existe.")
    }
  }

  def listObjects(s3: S3Client, bucketName: String, prefixName: String, currentDate: String): java.util.List[S3Object] = {
    val prefixDateCurrentDate = s"${prefixName}/${currentDate}/"

    println(prefixDateCurrentDate)

    try {
      val listObjectsRequest = ListObjectsV2Request.builder.bucket(bucketName).prefix(prefixDateCurrentDate).build
      val response = s3.listObjectsV2(listObjectsRequest)
      response.contents
    } catch {
      case e: S3Exception =>
        System.err.println(e.awsErrorDetails.errorMessage)
        System.exit(1)
        new java.util.ArrayList[S3Object]()
    }
  }

    def findMaxDateObject(objectList: java.util.List[S3Object]): String = {
      var maxDate: Option[Instant] = None
      var maxDateObjectName: String = null

      objectList.forEach((obj) => {
        val lastModified = Option(obj.lastModified())

//        println(s"Objeto: ${obj.key()}, Modificado em: $lastModified") // Log para depuração

        lastModified match {
          case Some(date) =>
            if (maxDate.isEmpty || date.isAfter(maxDate.get)) {
              maxDate = Some(date)
              maxDateObjectName = obj.key()
//              println(s"Novo objeto mais recente: $maxDateObjectName com data: $maxDate") // Log para depuração
            }
          case None =>
            println(s"Objeto ${obj.key()} não tem data de modificação") // Log para depuração
        }
      })

        maxDateObjectName
      }

  def downloadObject(s3: S3Client, bucketName: String, objectKey: String, baseDestinationPath: String): Unit = {
    try {
      val getObjectRequest = GetObjectRequest.builder.bucket(bucketName).key(objectKey).build

      // Construindo o caminho de destino completo
      val destinationPath = Paths.get(baseDestinationPath, objectKey)

      // Criando os diretórios necessários se eles não existirem
      val parentDir = destinationPath.getParent
      if (parentDir != null && !Files.exists(parentDir)) {
        Files.createDirectories(parentDir)
      }

      // Baixando o objeto
      val inputStream = s3.getObject(getObjectRequest)
      java.nio.file.Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING)
      inputStream.close()

      System.out.println(s"Object $objectKey downloaded to $destinationPath")
    } catch {
      case e: S3Exception =>
        System.err.println(e.awsErrorDetails.errorMessage)
        System.exit(1)
    }
  }
}
