package ru.agny.xent.persistence

import java.util.concurrent.atomic.AtomicLong

import scala.annotation.{compileTimeOnly, StaticAnnotation}
import scala.language.experimental.macros
import scala.reflect.api.Trees
import scala.reflect.macros.blackbox

@compileTimeOnly("enable macro paradise to expand macro annotations")
class RedisEntity(collection: String, idField: String, key: String) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Unit = macro RedisEntity.impl
}

object RedisEntity {

  def impl(c: blackbox.Context)(annottees: c.Expr[Any]*) = {
    import c.universe._

    val helper = new Helper[c.type](c)

    val (collection, idField, key) = helper.extractAnnotationParameters(c.prefix.tree)
    val result = {
      annottees.map(_.tree).toList match {
        //TODO add companion object check
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { ..$body }" :: Nil =>
          val companionName = tpname.toTermName
          val (constr, names) = helper.extractParametersFromTrees(paramss)
          val parsedTree = helper.getConstructorParamsExpr(companionName, constr.map(_.toString), constr, names, 0)
          //TODO missing #toPersist actual implementation
          q"""$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents {
            ..$body
            val collectionId = $collection + "#" + $idField
            val key = $key
            val toPersist = toString()
            private val forcedCompanion = $companionName
          }
          object $companionName {
            import ru.agny.xent.persistence.tokens._
            import ru.agny.xent.persistence.MessageHandler

            def create(v:String):RedisMessage = {
              val result = Parser.extract(v, Seq.empty)._1
              $parsedTree
            }

            MessageHandler.register(${companionName.toString}, create)
          }"""
        case b => c.abort(c.enclosingPosition, s"Annotation @RedisEntity can be used only with classes without companion objects")
      }
    }
    result
  }
}

class Helper[C <: blackbox.Context](val c: C) {

  import c.universe._

  val prims = List("String", "Long", "Double", "Int")

  def extractAnnotationParameters(tree: Trees#Tree) = tree match {
    case q"new $name($col, $idf, $key)" => (
      col,
      idf match {
        case Literal(Constant(field: String)) => TermName(field)
        case _ => c.abort(c.enclosingPosition, "Identifier should point to a field name")
      },
      key)
    case _ => throw new Exception("Annotation @RedisEntity must have all parameters specified")
  }

  /**
    * @param params seq of seq (presumably) valDefs
    * @return tuple3 of lists
    *         1 - Type of parameter
    *         2 - name of parameter as it stands in constructor definition
    */
  def extractParametersFromTrees(params: Seq[Seq[Tree]]) =
    params.flatten.foldLeft(Seq.empty[Type], Seq.empty[TermName]) {
      case ((complexTypes, names), p) => p match {
        case ValDef(_, name, tp, _) =>
          tp match {
            case Ident(TypeName(v)) =>
              val baseType = c.typecheck(tq"$tp", c.TYPEmode).tpe.erasure
              (complexTypes :+ baseType, names :+ name)
            case m@tq"$tpt[..$tpts]" => c.abort(c.enclosingPosition, s"Type classes are not supported yet $name:$m") //TODO think about it
          }
        case _ => c.abort(c.enclosingPosition, s"Can't extract ValDef from $params")
      }
    }

  def extractParametersFromMembers(params: MemberScope, methodName: TermName) =
    params.find(_.name == methodName).map {
      case m: MethodSymbol =>
        m.paramLists.flatten.map(x => {
          val t = x.asTerm
          (t.info.erasure.toString, t.info, t.name)
        })
    }

  val globalId = new AtomicLong(-1)

  def getConstructorParamsExpr(constructor: TermName, basicTypes: Seq[String], types: Seq[Type], names: Seq[TermName], shift: Int): Tree = {
    val params = for (i <- basicTypes.indices) yield {
      if (prims.contains(basicTypes(i))) {
        getPrimitiveExpr(basicTypes, names, i)
      } else {
        val tpe = types(i)
        val shortName = tpe.typeSymbol.name match {
          case TypeName(v) => TermName(v)
        }
        val defaultConstructor = extractParametersFromMembers(tpe.members, termNames.CONSTRUCTOR).map(_.unzip3 match {
          case (s, bt, tn) if s.nonEmpty => getConstructorParamsExpr(shortName, s, bt, tn, shift + i)
          case _ => c.abort(c.enclosingPosition, s"$constructor has a type class parameter which is not supported yet: ${names(i)}:${tpe.toString}")
        })
        defaultConstructor.get
        //        val companionConstructor = extractParametersFromMembers(tpe.companion.members, TermName("apply")).flatMap(_.unzip3 match {
        //          case (_, _, tn) if tn.nonEmpty =>
        //            val s = tpe.typeArgs.map(_.toString)
        //            val bt = tpe.typeArgs
        //            Some(getConstructorParamsExpr(shortName, s, bt, tn, shift + i))
        //          case _ => None
        //        })
      }
    }
    q"$constructor(..$params)"
  }

  def getPrimitiveExpr(basicTypes: Seq[String], names: Seq[TermName], i: Int) = {
    //https://issues.scala-lang.org/browse/SI-4388
    val toIntBridge = if (basicTypes(i) == "Int") {
      ".asInstanceOf[Primitive[Long]].k.toInt"
    } else {
      s".asInstanceOf[Primitive[${basicTypes(i)}]].k"
    }
    val code = s"result(${globalId.incrementAndGet()})$toIntBridge"
    q"${names(i)} = ${c.parse(code)}"
  }
}