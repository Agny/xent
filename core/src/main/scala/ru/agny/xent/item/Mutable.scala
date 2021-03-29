package ru.agny.xent.item

import ru.agny.xent.TimeInterval

trait Mutable { self =>
  def tick(time: TimeInterval): self.type 
}


