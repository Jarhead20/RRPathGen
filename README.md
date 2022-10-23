# RRPathGen

RRPathGen is a tool to generate [Road Runner](https://github.com/acmerobotics/road-runner) paths.

## Installation (Jar)

1. Download the jar from the releases page.
2. Check that you have at least java 8 installed `java --version`
3. Run the jar from the command line `java -jar .\RRPathGen-1.1.0.jar` if you do not run from the command line you c


## Installation (Intellij)

1. Clone the repo `git clone https://github.com/Jarhead20/RRPathGen.git`
2. Setup a run configuration
3. Copy your previous autonomous java files to resources and rename the file to import.java to import the previous splines (might not work unless it only has splineTo's and displacement markers.

## Usage

Generate your paths using the key binds below and once you are done export the path with the export button and copy paste it into your autonomous program.

| Key Bind            | Action                  |
|---------------------|-------------------------|
| Left Click          | Add New Point           |
| Left Drag (Point)   | Drags Selected Point    |
| Right Click (Point) | Edits The Point         |
| Alt Left Click      | Change Heading          |
| Left Arrow          | Next Path               |
| Right Arrow         | Previous Path           |
| R                   | Reverse Robot Direction |
| Esc                 | Escape                  |
| Delete              | Delete Selected Node    |
| Ctrl + Z            | Undo Previous Action    |

## Acknowledgements 
The inspiration from this project came from Technic Bots' [Blitz](https://technicbots.com/Blitz) app.<br />
The field images were aquired from [MeepMeep](https://github.com/NoahBres/MeepMeep).<br />
And a big thank you to [Ryan Brott](https://github.com/rbrott) for helping me with the spline implementation.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[MIT](https://choosealicense.com/licenses/mit/)
