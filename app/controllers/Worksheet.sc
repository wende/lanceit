import scala.pickling._         // This imports names only
import scala.pickling.json._    // Imports PickleFormat
import scala.pickling.static._  // Avoid runtime pickler

case class MyPickle(a : Int)

println("Picklikng")
val pickle = MyPickle(10).pickle.value
println("Picklikng " + pickle)
println(JSONPickle(pickle).unpickle[MyPickle])

