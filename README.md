# gonav - GO Navigator

This is an Android app for Pokémon GO players who want to track Pokémon in a fairly enjoyable way.
By watching your nearby area, gonav keeps you updated with live compass/distance metrics via the Android notifications API.

## Get the APK

Download the latest version from the [Releases page](https://github.com/Fiskie/gonav/releases).

## Features

* Automated background scanning in a radius of about ~400m around you.
* Set up alerts for live distance and orientation notifications on specific encounters.
* Heads-up display tracks all seen encounters.
* Decentralised; no middleman service needed.

## Requirements

* A PTC account - it is **highly recommended** that you do not use the account that you play the actual game with. 
* Android 5.0 Lollipop or higher (tested on Android N, built for L *but not tested at all outside of a VM*)
 
## Known issues

* Unidentified crash while switching networks (?)

## Contributing
 
Pull requests, etc. Please do not directly submit feature PRs without opening an issue first; we want to avoid bloating the application and adding in features not related to the core functionality.

### Wanted

* Android Wear support -- anyone got a Smartwatch?
* There are no unit tests. I wanted to set up some instrumental test cases first thing, but I couldn't figure out how to get it working. If someone can figure that out so we can keep this automatically tested to a degree, that would be nice.
* Could we get this working on KitKat? Something worth investigating.

## License

GNU GPL v3. Read LICENSE.md.
