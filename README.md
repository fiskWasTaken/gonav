# gonav - GO Navigator

This is an Android app for Pokémon GO players who want to track Pokémon in a fairly enjoyable way.
By watching your nearby area, gonav keeps you updated with live compass/distance metrics via the Android notifications API.

## Features

* Automated background scanning in a radius of about ~400m around you.
* Set up alerts for live distance and orientation notifications for specific encounters
* Heads-up display tracks all seen encounters.
* Decentralised; no middleman service needed.
* No ads.

## Requirements

* A PTC account - it is **highly recommended** that you do not use the account that you play the actual game with. 
* Android 5.0 Lollipop or higher (tested on Android N, built for L *but not tested at all outside of a VM*)

## Get the APK

F-Droid link goes here.
 
## Known issues

* Unidentified crash while switching networks (?) Can't reproduce; app sometimes crashes when I'm not trying to debug it.
* Authentication with PTC always fails the first time around. Why?
* Could we get this working on KitKat? Something worth investigating.

## Contributing
 
Pull requests, etc. Please do not directly submit feature PRs; we want to avoid bloating the application and adding in features not related to the core functionality.

### Wanted

* Android Wear support -- anyone got a Smartwatch?
* There are no unit tests. I wanted to set up some instrumental test cases first thing, but I couldn't figure out how to get it working. If someone can figure that out so we can keep this automatically tested to a degree, that would be nice.

## License

GNU GPLv3. Read LICENSE.md.