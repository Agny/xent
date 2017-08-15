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
          val parsedTree = helper.getConstructorParamsExpr(companionName, constr.map(_.toString), constr, names)
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
            import ru.agny.xent.persistence.tokens.Nodes._
            import ru.agny.xent.persistence.MessageHandler

            def create(v:String):ru.agny.xent.persistence.RedisMessage = {
              val result = TokenParser.tokenize(v)
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
  val containers = List("Vector")

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
    * @return tuple2 of lists
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
            case singleTyped@tq"$_[$_]" =>
              val containerTyped = c.typecheck(tq"$singleTyped", c.TYPEmode).tpe
              if (containers.contains(containerTyped.typeSymbol.name.toString)) {
                (complexTypes :+ containerTyped, names :+ name)
              } else {
                c.abort(c.enclosingPosition, s"Typed class $name:$singleTyped is not supported yet. Is Vector not sufficient?")
              }
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

  def getConstructorParamsExpr(constructor: TermName, basicTypes: Seq[String], types: Seq[Type], names: Seq[TermName]): Tree = {
    val params = for (i <- basicTypes.indices) yield {
      if (prims.contains(basicTypes(i))) {
        getPrimitiveFieldAssignment(basicTypes(i), names(i))
      } else if (containers.contains(types(i).typeSymbol.name.toString)) {
        getContainerExpr(types(i), names(i))
      } else {
        val tpe = types(i)
        val shortName = typeToTerm(tpe)
        val defaultConstructor = extractParametersFromMembers(tpe.members, termNames.CONSTRUCTOR).map(_.unzip3 match {
          case (s, bt, tn) if s.nonEmpty => getConstructorParamsExpr(shortName, s, bt, tn)
          case _ => c.abort(c.enclosingPosition, s"$constructor has a type class parameter which is not supported yet: ${names(i)}:${tpe.toString}")
        })
        defaultConstructor.get
      }
    }
    q"$constructor(..$params)"
  }

  private def getContainerExpr(constructorType: Type, fieldName: TermName) = {
    val elementType = constructorType.typeArgs.head
    val containerName = typeToTerm(constructorType)
    val elementName = typeToTerm(elementType)
    val companionConstructor = extractParametersFromMembers(constructorType.erasure.companion.members, TermName("apply")).map(_.unzip3 match {
      case (_, _, _) =>
        if (prims.contains(elementType.toString)) {
          getContainerFieldAssignment(containerName, fieldName, getPrimitiveExpr(elementType.toString))
        } else {
          val defaultConstructor = extractParametersFromMembers(elementType.members, termNames.CONSTRUCTOR).map(_.unzip3 match {
            case (s, bt, tn) if s.nonEmpty =>
              getContainerFieldAssignment(containerName, fieldName, getConstructorParamsExpr(elementName, s, bt, tn))
            case _ => c.abort(c.enclosingPosition, s"$constructorType has a type class parameter which is not supported yet: $fieldName:${elementType.toString}")
          })
          defaultConstructor.get
        }
      case _ => c.abort(c.enclosingPosition, s"Container $constructorType doesn't have method apply")
    })
    companionConstructor.get
  }

  private def getPrimitiveFieldAssignment(exprType: String, fieldName: TermName) = {
    q"$fieldName = ${getPrimitiveExpr(exprType)}"
  }

  private def getPrimitiveExpr(exprType: String) = {
    val code = s"result(${globalId.incrementAndGet()}).materialize[$exprType]"
    q"${c.parse(code)}"
  }

  private def getContainerFieldAssignment(container: TermName, fieldName: TermName, params: Tree) = {
    q"$fieldName = $container(..$params)"
  }

  private def typeToTerm(tpe: Type): TermName = tpe.typeSymbol.name match {
    case TypeName(v) => TermName(v)
  }
}