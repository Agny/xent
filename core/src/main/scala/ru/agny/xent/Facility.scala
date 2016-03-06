package ru.agny.xent

sealed trait Facility[T <: Placed]
case class FacilityWorld() extends Facility[Global]
case class FacilityLocal() extends Facility[Local]