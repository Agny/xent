package ru.agny.xent.battle.unit

import ru.agny.xent.battle.core.{Skill, LevelBar, Speed}

case class Unit(level: LevelBar, spirit: SpiritBar, equip: Equipment, speed: Speed, skills: Seq[Skill])






