package ru.agny.xent.persistence.tokens

//TODO replace by scala parser combinators?
object Parser {

  val caseClass = """^(\w+)\((.*\))""".r
  val primitive = """^([\s\w]+|[-]?\d+|[-]?\d+\.\d+),?(.*)""".r
  val caseClassTale = """\),*(.*)""".r

  def extract(src: String, acc: Vector[Primitive]): (Vector[Primitive], String) = {
    src match {
      case caseClass(name, params) =>
        val (r, tail) = extract(params, Vector.empty)
        extract(tail, acc ++ r)
      case primitive(value, tail) =>
        val pValue = Primitive(value)
        extract(tail, pValue +: acc)
      case caseClassTale(tail) => (acc, tail)
      case s if s.isEmpty => (acc, s)
    }
  }
}
