abstract class Chain[T](){
  val cells : List[ChainCell[T]];

  def perform(s: T): Unit ={
    cells.filter {_.isDefinedAt(s)}(0).perform(s)
  }
}
abstract class ChainCell[T]{
  def isDefinedAt(a : T) : Boolean
  def perform(a: T) : Unit
}

abstract class MessageHandler extends  ChainCell[String] {

}

class ImportantMessage extends MessageHandler {
  override def isDefinedAt(a: String): Boolean = a.indexOf("a") != -1
  override def perform(a: String): Unit = println("Important")
}
class NormalMessage extends  MessageHandler {
  override def isDefinedAt(a: String) = true
  override def perform(a: String) =  println("Normal")
}

class ConcreteChainHandler extends Chain[String] {
  override val cells: List[ChainCell[String]] = List(new ImportantMessage, new NormalMessage)
}

val chain = new ConcreteChainHandler()
chain.perform("Yo mna")