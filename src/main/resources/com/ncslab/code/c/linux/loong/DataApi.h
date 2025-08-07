#ifndef DATA_API
#define DATA_API

#include "ncslabccode.h"

#define dataApiGetVersion(mp) "0.1"
#define dataApiGetNumBlocks(mp) ((mp)->blockNum)
#define dataApiGetNumSignals(mp) ((mp)->signalNum)
#define dataApiGetNumParameters(mp) ((mp)->parameterNum)
#define dataApiGetSignals(mp) ((mp)->signals)
#define dataApiGetSignal(signals,n) (signals[(n)])
#define dataApiGetParameters(mp) ((mp)->parameters)
#define dataApiGetParameter(parameters,n) (parameters[(n)])

#endif