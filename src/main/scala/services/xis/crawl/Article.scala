import scala.collection.mutable.Buffer
import scala.collection.JavaConverters

case class Article(
  board: String,
  id: String,
  title: String,
  author: String, 
  department: String,
  time: String,
  hits: Int,
  files: List[String],
  links: List[String],
  content: String,
  images: List[String]
) {
  def fileList = JavaConverters.bufferAsJavaList(files.to[Buffer])
  def linkList = JavaConverters.bufferAsJavaList(links.to[Buffer])
  def imageList = JavaConverters.bufferAsJavaList(images.to[Buffer])
}
