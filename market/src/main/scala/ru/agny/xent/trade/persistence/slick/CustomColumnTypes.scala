package ru.agny.xent.trade.persistence.slick

import ru.agny.xent.trade.LotType
import slick.jdbc.PostgresProfile.api._

object CustomColumnTypes {

  implicit val lotType = MappedColumnType.base[LotType, String](
    tpe => tpe.v,
    typeString => LotType(typeString)
  )


}
