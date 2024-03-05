import scipy.io as scio
import json
import numpy as np

dataFile = 'D:\\Project\\react_antd\\faker\\NetConTop\\ncslablink\\src\\octaveserver\\workspaceData.mat'
 

data = scio.loadmat(dataFile)



del data['__header__']
del data['__version__']
del data['__globals__']

del data['ZhouXWOctaveCode']
del data['ZhouXWOctaveCom']
del data['ZhouXWOctaveResult']
del data['ZhouXWOctaveSock']
del data['ZhouXWOctavehFig1']
del data['ZhouXWOctavehFig2']
del data['ZhouXWOctavefp']

if 'ZhouXWOctaveI' in data:
    #print(data['ZhouXWOctaveI'])
    del data['ZhouXWOctaveI']


if 'ErrorInfo' in data:
    #print(data['ZhouXWOctaveI'])
    del data['ErrorInfo']

for key in data:
    #print(key)
    if type(data[key])==np.ndarray:
        data[key]=str(data[key].tolist())
    else:
        data[key]=str(data[key])

matData = [];
i=0;
for key in data:
    matData.append({"id":i+1 ,"name":key, "value":data[key]});
    i+=1;
    
matDataStr = str(matData);   


dataJson = json.dumps(matData)

print(dataJson)
# return dataJson

