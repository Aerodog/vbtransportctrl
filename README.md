[The Voxel Box]: http://thevoxelbox.com
[MetroVox]: http://voxelwiki.com/minecraft/MetroVox
[WorldGuard]: http://dev.bukkit.org/bukkit-plugins/worldguard/
[License]: http://opensource.org/licenses/MIT

MetroVox Control
======

This is a little plugin I made for [The Voxel Box][The Voxel Box] to control their "[MetroVox][MetroVox]" network. Vanilla minecart systems only work so well, but I discovered that they are a bit of a pain space-wise. I like to make subway systems, but the redstone required to make a proper subway system takes up too much space to stack rail lines.

How It Works
------

The setup for the plugin is simple. All stops are controlled by signs (almost no external configuration needed!). All you need is a sign under a track with some key information (this is a stop):

```
===============
|  #railstop  |
|  [timeout]  |
| [direction] |
|    [y/n]    |
===============
       |
       |
       |
```

The format information should be as follows:

1. "#railstop" -> Tells the plugin that this sign is a stop
2. "timeout" -> An integer value of the seconds a cart (with a player onboard) will wait at a station before continuing to the next stop.
3. "direction" -> The direction of travel the stop will send the player ("north", "east", "west", and "south" are accepted values).
4. "y/n" -> If "y" (for yes), when the block under the sign is powered and the stop doesn't already have a waiting cart, a cart will be spawned. If "n" (for no), then the stop will just be a stop, not a cart-providing service.

FAQ
------

### May I use this plugin?
Absolutely, but keep in mind that I design this exclusively for the fine folks at **The Voxel Box**.

### Will this have Permissions support?
I thought about it, but since the people on TVB are trusted will not breaking stuff, I decided not to directly add support. Of course, you can always use a tool like [WorldGuard][WorldGuard] to prevent signs from being destroyed by your run-of-the-mill denizens.

### May I contribute to this project?
YES YES YES! I love contribution by anyone, and since this licensed under the [MIT License][License], you may fork your own version and tinker (but if you submit your code back here in the form of a pull request, that'd make me happy :) ).

Obligatory License Spam
-----

```
MIT License

Copyright (c) 2013 Patrick Anker and contributors
 
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.
 
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```