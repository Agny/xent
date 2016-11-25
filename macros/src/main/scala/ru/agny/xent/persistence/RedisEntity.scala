package ru.agny.xent.persistence

import scala.annotation.StaticAnnotation
import scala.language.experimental.macros
import scala.reflect.api.Trees
import scala.reflect.macros.blackbox

class RedisEntity(collection: String, idField: String, key: String) extends StaticAnnotation {
  def macroTransform(annottees: Any*): Unit = macro RedisEntity.impl
}

object RedisEntity {

  var prims = List("String", "Long", "Double")

  def impl(c: blackbox.Context)(annottees: c.Expr[Any]*) = {
    import c.universe._

    def extractAnnotationParameters(tree: Tree) = tree match {
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
      * @param params
      * @return tuple3 of lists
      *         1 - name of basic types (like a "Long", "Double" etc.) if parameter value of such type,
      *         2 - actual type (like Long, Double, x.y.Z - i.e. Type),
      *         3 - name of parameter as it stands in constructor definition
      */
    def extractParametersFromTrees(params: Seq[Seq[Trees#Tree]]) =
      params.flatten.foldLeft(Seq.empty[String], Seq.empty[Type], Seq.empty[TermName]) {
        case ((basicTypes, complexTypes, names), p) => p match {
          case ValDef(_, name, tp, _) =>
            tp match {
              case Ident(TypeName(v)) =>
                val baseType = c.typecheck(tq"$tp", c.TYPEmode).tpe.erasure
                (basicTypes :+ baseType.toString, complexTypes :+ baseType, names :+ name)
              case tq"$tpt[..$tpts]" => c.abort(c.enclosingPosition,"sffup" + tpt.toString)  //TODO think about it
            }
        }
      }

    def extractParametersFromMembers(params: MemberScope) =
      (params.find(_.name == termNames.CONSTRUCTOR).get match {
        case m: MethodSymbol =>
          m.paramLists.flatten.map(x => {
            val t = x.asTerm
            (t.info.toString, t.info, t.name)
          })
      }).toSeq

    def getConstructorParamsExpr(constructor: TermName, basicTypes: Seq[String], types: Seq[Type], names: Seq[TermName], shift: Int): Tree = {
      var throughIdx = shift
      val params = for (i <- basicTypes.indices) yield {
        if (prims.contains(basicTypes(i))) {
          getPrimitiveExpr(basicTypes, names, i, throughIdx)
        } else {
          throughIdx = i + shift
          val (s, bt, tn) = extractParametersFromMembers(types(i).members).unzip3
          val shortName = types(i).typeSymbol.name match {
            case TypeName(v) => TermName(v)
          }
          getConstructorParamsExpr(shortName, s, bt, tn, throughIdx)
        }
      }
      q"$constructor(..$params)"
    }

    def getPrimitiveExpr(basicTypes: Seq[String], names: Seq[TermName], localIdx: Int, globalIdx: Int) =
      q"${names(localIdx)} = ${c.parse(s"result(${localIdx + globalIdx}).asInstanceOf[ru.agny.xent.persistence.tokens.Primitive[${basicTypes(localIdx)}]].k")}"

    val (collection, idField, key) = extractAnnotationParameters(c.prefix.tree)
    val result = {
      annottees.map(_.tree).toList match {
        case q"$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents { ..$body }" :: Nil =>
          val companionName = tpname.toTermName
          val (basic, constr, names) = extractParametersFromTrees(paramss)

          q"""$mods class $tpname[..$tparams] $ctorMods(...$paramss) extends { ..$earlydefns } with ..$parents {
            ..$body
            val collectionId = $collection + "#" + $idField
            val key = $key
            val toPersist = toString()
          }
          object $companionName {
            def gen(v:String):$tpname = {
              val result = ru.agny.xent.persistence.tokens.Parser.extract(v, Seq.empty)._1
              ${getConstructorParamsExpr(companionName, basic, constr, names, 0)}
            }
          }"""
        case b => c.abort(c.enclosingPosition, "Annotation @RedisEntity can be used only with classes")
      }
    }

    result
  }
}