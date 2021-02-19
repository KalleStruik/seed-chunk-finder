#!/bin/python3
from subprocess import Popen, PIPE
import os, json, sys

seeds = []
seedsDone = []

with open("results.json", "r") as results_file:
    for seed in json.load(results_file):
        seedsDone.append(seed)

with open("seeds.txt", "r") as seed_file:
    for line in seed_file:
        # Skip comments in the seeds.txt file.
        if line.startswith("#"):
            continue
        
        seeds.append(line.split(":")[0].strip())

if len(seeds) == 0:
    print("Please put some seeds into the seeds.txt file.")
    sys.exit()


print("===========================================")
print("For some people the program hangs randomly.")
print("If you think this has happened to you. You")
print("can press ctrl+C to kill the program and")
print("then restart it. It will start again where")
print("you left off.")
print("")
print("Below will be one or more questions for you")
print("Please answer them with the possibilities")
print("specified between the [brackets].")
print("===========================================")

os_question = input("Are you on windows (not wsl)? [Y/n] ").lower()

if os_question == "n":
    os_name = "nix"
else:
    os_name = "win"

print("")
print("")
print("")

if not os.path.isfile("server/eula.txt"):
    eula = input("Do you agree to the mojang EULA? [y/N] ").lower()
    if eula == "y":
        with open("server/eula.txt", 'w') as eula_file:
            eula_file.write("eula=true")
    else:
        print("You have to agree to the EULA to use this program!")

for seed in seeds:
    if seed in seedsDone:
        print(f"{seed} already checked. Skipping.")
        continue
    
    print(f"Checking seed: {seed}")
    new_properties = ""

    with open("server/server.properties", 'r') as properties:
        for line in properties:
            if line.startswith("level-seed"):
                new_properties += f"level-seed={seed}\n"
            else:
                new_properties += line

    with open("server/server.properties", 'w') as properties:
        properties.write(new_properties)

    os.chdir("server")

    if os_name == "nix":
        process = Popen(['java', '-jar', 'server.jar', seed], stdin=PIPE, stdout=PIPE, stderr=PIPE)
    else:
        process = Popen(['java', '-jar', 'server.jar', seed], stdin=PIPE, stdout=PIPE, stderr=PIPE, shell=True)

    stdout, stderr = process.communicate()

    with open(f"{seed}/data.json") as data_file:
        data = json.load(data_file)

        if(data['area']<5000):
            print("="*10+"SUB 5K"+"="*10)
            print(f"{seed}: {data}")
            print("="*26)
        
    
    os.chdir("../")

    with open("results.json", "r") as results_file:
        currentData = json.load(results_file)

    currentData[seed] = data
    with open("results.json", "w") as results_file:
        json.dump(currentData, results_file)
        
