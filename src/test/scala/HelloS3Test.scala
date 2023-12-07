import java.time.Instant
import java.util.Arrays.asList
import java.io.InputStream
import java.nio.file.{Files, Path, StandardCopyOption}

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._

import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{ListObjectsV2Request, ListObjectsV2Response, S3Object, GetObjectRequest}
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.services.s3.model.GetObjectResponse

import com.aws.utils.HelloS3

// Uma abstração para operações de arquivo
trait FileOperations {
  def copyFile(inputStream: InputStream, destination: Path, copyOption: StandardCopyOption): Long
}

class HelloS3Test extends AnyFlatSpec with Matchers {

  "listObjects" should "return a list of S3Objects" in {
    val mockS3Client = mock(classOf[S3Client])
    val bucketName = "test-bucket"
    val prefixName = "test-prefix"
    val currentDate = "20230101"

    val mockResponse = ListObjectsV2Response.builder()
      .contents(asList(S3Object.builder().key("test-key").build()))
      .build()

    when(mockS3Client.listObjectsV2(any[ListObjectsV2Request])).thenReturn(mockResponse)

    val result = HelloS3.listObjects(mockS3Client, bucketName, prefixName, currentDate)

    result.size() shouldBe 1
    result.get(0).key() shouldBe "test-key"

    verify(mockS3Client).listObjectsV2(any[ListObjectsV2Request])
  }

  "findMaxDateObject" should "return the object with the most recent modification date" in {
    val objectList = asList(
      S3Object.builder().key("obj1").lastModified(Instant.now.minusSeconds(3600)).build(),
      S3Object.builder().key("obj2").lastModified(Instant.now).build(),
      S3Object.builder().key("obj3").lastModified(Instant.now.minusSeconds(7200)).build()
    )

    val result = HelloS3.findMaxDateObject(objectList)

    result shouldBe "obj2"
  }
}
