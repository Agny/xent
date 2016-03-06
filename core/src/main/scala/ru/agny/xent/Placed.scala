package ru.agny.xent

/**
  * Trait indicates where "something" should be placed: somewhere in world map or internally in colony
  */

sealed trait Placed
sealed trait Global extends Placed
sealed trait Local extends Placed
