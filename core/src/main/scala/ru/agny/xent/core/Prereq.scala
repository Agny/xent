package ru.agny.xent.core

trait Prereq
case class ScienceFrom(from: Science*) extends Prereq
