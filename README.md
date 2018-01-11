# xent
This is a server-side part of a multiplayer game, which mechanics (by design) resembles one in MMO strategies like Destiny Sphere, Travian, Settlers Online:
- players are given village to control, where they can build facilities, gather resources, hire armies
- they can attack each other to get additional resources
- all events in the game world occur regardless of player's presence online

The very idea behind this project is to test out and look, how good a functional programming in the game development is. I'm not using akka-actors at the game's core cause I find it counter-functional in that sense: you send a message and don't care about what result it gives back (if any).

The next features are more or less complete at this moment:

- items system
- resources handling
- units movement
- battle calculation
- building mechanics
- trading module

###Brief overview
Game world basically is a function from initial state iteratively mapped to received actions. All game logic is done during action processing.

Almost all code is written in pure functions. Maybe I'm little too obsessed with pureness, but I'm aware about possible performance issues and consider to use variables, when it's appropriate.

I like scala strong type system and eager user of implicits and type classes. Whole inventory system is based on type classes at this moment.