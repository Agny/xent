package ru.agny.xent.core.city

import ru.agny.xent.core.inventory.Item.ItemWeight
import ru.agny.xent.core.inventory.Progress.ProgressTime
import ru.agny.xent.core.inventory._
import ru.agny.xent.core.utils.ErrorCode

case class Storage(holder: ItemHolder) extends InventoryLike[Storage, Item] {

  import Item.implicits._
  import ItemMerger.implicits._

  val asInventory = this

  def tick(period: ProgressTime, producers: Vector[Building]): (Storage, Vector[Building]) =
    producers.foldLeft(this, Vector.empty[Building])((s, f) => {
      val (building, items) = f.tick(period)
      val (storage, notAdded) = s._1.add(items)
      (storage, building +: s._2)
    })

  def add(r: Vector[Item]): (Storage, Vector[Slot[Item]]) = r match {
    case h +: t =>
      val (store, remainder) = add(h)
      val (storeAcc, remainderAcc) = store.add(t)
      (storeAcc, remainder +: remainderAcc)
    case _ => (this, Vector.empty)
  }

  def spend(recipe: Cost): Either[ErrorCode.Value, Storage] = {
    recipe.v.find {
      case x: ItemStack => !resources.exists(y => x.id == y.id && y.stackValue >= x.stackValue)
      case x => !items.exists(y => x.id == y.id)
    } match {
      case Some(v) => Left(ErrorCode.RESOURCE_CANT_BE_PRODUCED)
      case None =>
        val afterSpend = recipe.v.foldRight(items)((a, b) => b.flatMap {
          case ItemStack(st, id, w) if a.id == id => Some(ItemStack(st - a.asInstanceOf[ItemStack].stackValue, a.id, w))
          case item if a.id == item.id => None
          case x => Some(x)
        })
        Right(Storage(afterSpend))
    }
  }

  def loadInWeight(weight: ItemWeight): Vector[Item] = {
    var weightAcc = 0
    items.flatMap {
      case x: ItemStack if weightAcc + x.singleWeight <= weight =>
        val (acc, is) = weightStackPart(x, weightAcc, weight)
        weightAcc = acc
        Some(is)
      case x if weightAcc + x.weight <= weight =>
        weightAcc += x.weight
        Some(x)
      case _ => None
    }
  }

  private lazy val resources: Vector[ItemStack] = holder.slots.flatMap {
    case ItemSlot(v) => v match {
      case ru: ItemStack => Some(ru)
      case _ => None
    }
    case EmptySlot => None
  }

  lazy val items: Vector[Item] = holder.slots.flatMap {
    case ItemSlot(v) => Some(v)
    case EmptySlot => None
  }

  private def weightStackPart(is: ItemStack, acc: ItemWeight, limit: ItemWeight): (ItemWeight, ItemStack) = {
    if (acc + is.weight < limit) (acc + is.weight, is)
    else {
      val n = (limit - acc) / is.singleWeight
      (acc + is.singleWeight * n, is.copy(stackValue = n))
    }
  }

  override def apply(slots: Vector[Slot[Item]]): Storage = Storage(slots)
}

object Storage {
  val empty: Storage = Storage(Vector.empty)

  def apply(slots: Vector[Slot[Item]]): Storage = Storage(ItemHolder(slots))
}
