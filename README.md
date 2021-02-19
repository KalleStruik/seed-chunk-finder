# Seed Chunk Checker

Checks the frequencies of blocks in a given chunk of a given seed.


## Usage
1. Build shadow (fat) jar with `./gradlew shadowJar` (`gradlew.bat shadowJar` on windows)
2. Create folder `server` and move the `build/libs/seed-chunk-checker-0.1.0-all.jar` to that folder.
3. Rename the jar you just moved to `server.jar`
4. Create a file in the `server` directory called `eula.txt` with the contents `eula=true`
5. Create a file called `seeds.txt` and fill it with one seed per line (See accepted seed formats).
6. Run `python3 main.py` to start generating worlds and finding optimal chunks.

### Accepted seed formats
The program can handle the following formats:  
`seed`  
`seed: x,y`  