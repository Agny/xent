package ru.agny.xent

sealed trait Placed
sealed trait Global extends Placed
sealed trait Local extends Placed
