package ru.agny.xent.persistence.tokens

//TODO replace by scala parser combinators?
object Parser {

  val caseClass = """^(\w+)\((.*\))""".r
  val string = """^(\w+),?(.*)""".r
  val long = """^([-]?\d+),?(.*)""".r
  val double = """^([-]?\d+\.\d+),?(.*)""".r
  val caseClassTale = """\),*(.*)""".r

  def extract(src: String, acc: Seq[Node]): (Seq[Node], String) = {
    src match {
      case caseClass(name, params) =>
        val (r, tail) = extract(params, Seq.empty)
        extract(tail, acc ++ r)
      case double(value, tail) =>
        val pValue = DoubleValue(value.toDouble)
        extract(tail, acc :+ pValue)
      case long(value, tail) =>
        val pValue = LongValue(value.toLong)
        extract(tail, acc :+ pValue)
      case string(value, tail) =>
        val pValue = StringValue(value)
        extract(tail, acc :+ pValue)
      case caseClassTale(tail) => (acc, tail)
      case s if s.isEmpty => (acc, s)
    }
  }
}
