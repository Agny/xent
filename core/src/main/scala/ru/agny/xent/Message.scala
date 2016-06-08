package ru.agny.xent

trait Message
case class ResourceClaimMessage(user: User, layer: String, facility: String, resourceId: Long) extends Message
