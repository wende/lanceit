import play.api.libs.concurrent.Akka

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

val a = Future(Option(Future(Option(10))))

a.map { b =>
  b.map { c =>
    c.map { d =>
      d.map { res =>
        res + 10
      }
    }
  }
}

for {
  b <- a
  c <- b.get
} yield c.get + 10

List(1,2,3,4).dropRight(1)