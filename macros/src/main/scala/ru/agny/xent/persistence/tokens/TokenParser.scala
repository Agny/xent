package ru.agny.xent.persistence.tokens

import scala.util.parsing.combinator.{PackratParsers, RegexParsers}

object TokenParser extends RegexParsers with PackratParsers {
  override val skipWhitespace = false

  private val string = """[\w\s]+""".r ^^ (x => List(Token(x)))
  private val number = """[-]?\d+|[-]?\d+\.\d+""".r ^^ (x => List(Token(x)))

  private def program(implicit withClassName: Boolean) = repsep(expression, ")") ^^ (_.flatten)

  private def expression(implicit withClassName: Boolean): Parser[List[Token]] = repsep(caseClass | number | string, ",") ^^ (_.flatten)

  private def caseClass(implicit withName: Boolean): Parser[List[Token]] = string ~ "(" ~ expression ~ ")" ^^ {
    case name ~ _ ~ list ~ _ if withName => name ++ list
    case _ ~ _ ~ list ~ _ => list
  }

  def tokenize(src: String, omitClassNames: Boolean = true): List[Token] = {
    implicit val withClassName = !omitClassNames
    parseAll(program, src) match {
      case Success(result, _) => result
      case _ => List.empty
    }
  }
}