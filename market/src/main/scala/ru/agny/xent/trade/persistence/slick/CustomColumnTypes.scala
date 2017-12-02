package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.persistence.slick.DefaultProfile.api._
import ru.agny.xent.trade.LotType

object CustomColumnTypes {

  implicit val lotType = MappedColumnType.base[LotType, String](
    tpe => tpe.v,
    typeString => LotType(typeString)
  )


}
