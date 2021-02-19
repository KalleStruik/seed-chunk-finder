import json
with open("results.json") as f:
    data = json.load(f)

newarr = {}
print("seeds done: "+str(len(data)))

for x in data:
    newarr[data[x]['area']] = {"seed":x,"x":data[x]['x'],"z":data[x]["z"]}

for x in sorted(newarr)[:10]:
    print(str(x)+": "+newarr[x]['seed']+" ("+str(newarr[x]['x'])+','+str(newarr[x]['z'])+")")
