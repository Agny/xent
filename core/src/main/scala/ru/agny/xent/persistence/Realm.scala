package ru.agny.xent.persistence

import ru.agny.xent._

case class Realm(id: RealmId) {
//  def find(id: RealmId): ConnectionIO[Option[Realm]] =
//    sql"select id from realm where id = $id".query[Realm].option
}