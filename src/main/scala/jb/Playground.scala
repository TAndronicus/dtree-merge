package jb


case class FooResponse(foos: Seq[Foo])

case class Foo(id: String, typek: String, color: String)

object Playground {

  def matchmail(str: String): Unit = {
    str match {
      case Email(a, b) => println("login: " + a + ", domena: " + b)
      case _ => println("Nielegitymacyjny mail")
    }
  }

  def main(args: Array[String]): Unit = {
    val str = "jedrzus"
    val str2 = "jedrzus@gmail"
    matchmail(str)
    matchmail(str2)
  }

}

object Email {

  def apply(name: String, domain: String) = name + "@" + domain

  def unapply(email: String): Option[(String, String)] = {
    val parts = email.split("@")
    if (parts.length == 2) {
      Some((parts(0), parts(1)))
    } else {
      None
    }
  }

}
