package ru.agny.xent.trade

import ru.agny.xent.core.utils.UserType.UserId

case class Bid(owner: UserId, price: Price)