#!/bin/python3
from subprocess import Popen, PIPE
import os


seeds = []

with open("seeds.txt", "r") as seed_file:
    for line in seed_file:
        seeds.append(line.split(":")[0].strip())



for seed in seeds:
    print(f"Creating world for seed: {seed}")
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

    process = Popen(['/usr/bin/java', '-jar', 'server.jar', seed], stdin=PIPE, stdout=PIPE, stderr=PIPE)
    stdout, stderr = process.communicate()
    
    os.chdir("../")
        
