# RRPathGen

RRPathGen is a tool to generate [Road Runner](https://github.com/acmerobotics/road-runner) paths.

## Installation (Jar)

1. Download the jar from the releases page.
2. Run the jar from the command line `java -jar .\RRPathGen-1.0.0.jar`


## Installation (Intellij)

1. Clone the repo `git clone https://github.com/Jarhead20/RRPathGen.git`
2. Run the Main file
3. Copy your previous autonomous java files to resources and rename the file to import to import the previous splines (might not work unless it only has splineTo's and displacement markers.

## Usage

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

## Acknowledgements 
The inspiration from this project came from Technic Bots' [Blitz](https://technicbots.com/Blitz) app.<br />
The field images were aquired from [MeepMeep](https://github.com/NoahBres/MeepMeep).<br />
And a big thank you to [Ryan Brott](https://github.com/rbrott) for helping me with the spline implementation.

## Contributing
Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License
[MIT](https://choosealicense.com/licenses/mit/)
