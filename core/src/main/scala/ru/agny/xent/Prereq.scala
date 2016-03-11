package ru.agny.xent

trait Prereq
case class ScienceFrom(from: Science*) extends Prereq
