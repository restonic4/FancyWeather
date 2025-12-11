<img src="https://cdn.modrinth.com/data/VBy0B3ST/images/e3822acced31c9a05cab8d0b290a439aa4283edc.png" alt="Rain should extinguish campfires banner"></img>

<h1>What is it about?</h1>

This mod enhances the vanilla weather system, fixing bugs and adding awesome new features. But that's not all, we're also introducing brand-new fancy custom weather events (coming soon).

_This mod replaces our old mod called <a href="https://modrinth.com/mod/rain-should-extinguish-campfires">Rain should extinguish campfires</a>_

<h1>Features</h1>
<ul>
    <li>
        Rain extinguishes campfires when they're exposed to it.
    </li>
    <li>
        When the night is skipped because all players sleep, the rain now continues, only the time spent sleeping is skipped.
    </li>
    <li>
        Campfires can now ignite nearby unlit campfires.
    </li>
    <li>
        Rain makes crops grow faster.
    </li>
    <li>
        Fire arrows now spread fire.
    </li>
    <li>
        Option to sync real life time and weather with Minecraft (Uses Geolocation to access your location's weather, more info bellow)
    </li>
</ul>

<h3>Planned features</h3>
<ul>
    <li>
        Cloudy and fog weather effects compatibility wih shaders.
    </li>
    <li>
        Sandstorms (Soon)
    </li>
</ul>

<h1>Configurable</h1>
You can pretty much enable or disable every single feature in the mod. Don't like sandstorms? Turn them off. Hate rain not skipping at sleep? Disable it. Want to remove custom items or anything else? Go for it. You can even disable everything and make the mod completely useless if that's what you're into.

<h2>Real Life Sync / Geolocation</h2>
If what worries you is geolocation, don’t worry, it’s turned off by default.
We use geolocation to get your latitude and longitude so we can know what precipitation is happening in your area.

This data will only be requested once in the background and will be stored in your world, in the "overworld" folder, in the file "DONT_OPEN_ON_PUBLIC.json". Here you can freely manipulate your latitude and longitude to simulate being in another location or correct inaccuracies, since your coordinates may not be precise depending on your internet provider.

This data is not sent anywhere.

What this system does is synchronize real-world time with Minecraft, counting the days since the world was created, not the traditional Minecraft days.

It also synchronizes the following states:
- CLEAR
- CLOUDY
- FOG
- RAINING
- SNOWING
- THUNDERSTORM

And the following intensities:
- SLIGHT
- MODERATE
- INTENSE

You can check this data with the command: ``/fancyweather weather get status``

<h1>Compatibility</h1>
<ul>
  <li>
    Lithium: Has explicit support. (mixin.world.block_entity_ticking.sleeping.campfire & mixin.alloc.chunk_random)
  </li>
</ul>

<hr>

<i>Fancy, isn't it?</i>
<br></br>
<i>We are currently adding support for versions 1.20.1 up to 1.21.10</i>
<br>
<i>Forge will only be available for 1.20.1, the rest of versions is replaced by NeoForge</i>