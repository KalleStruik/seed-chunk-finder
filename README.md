# Seed Chunk Checker

Checks the frequencies of blocks in a given chunk of a given seed.


## Usage
1. Build shadow (fat) jar with `./gradlew shadowJar` (`gradlew.bat shadowJar` on windows)
2. Move the file at `build/libs/seed-chunk-checker-0.1.0-all.jar` to that folder `server/`.
3. Rename the jar you just moved to `server.jar`
5. Add your seeds to `seeds.txt`. (See accepted seed formats).
6. Run `python3 main.py` to start generating worlds and finding optimal chunks.
7. Run `python3 analyze.py` to analyze your results.

### Accepted seed formats
The program can handle the following formats for seeds (One per line):  
`seed`  
`seed: x,y`  


## Credits
@eutropius225 - For the original code  
@JurreJelle - For helping to make this work on Windows and the analyze.py script  
@JelleJurre - For grid searching code and suggesting user experience improvements  
@KalleStruik - For the main.py script and modifications to the java code  