package ru.agny.xent.persistence.tokens

//TODO replace by scala parser combinators?
object Parser {

  val caseClass = """^(\w+)\((.*\))""".r
  val primitive = """^([\s\w]+|[-]?\d+|[-]?\d+\.\d+),?(.*)""".r
  val caseClassTale = """\),*(.*)""".r

  def extract(src: String, acc: Seq[Primitive]): (Seq[Primitive], String) = {
    src match {
      case caseClass(name, params) =>
        val (r, tail) = extract(params, Seq.empty)
        extract(tail, acc ++ r)
      case primitive(value, tail) =>
        val pValue = Primitive(value)
        extract(tail, acc :+ pValue)
      case caseClassTale(tail) => (acc, tail)
      case s if s.isEmpty => (acc, s)
    }
  }
}
