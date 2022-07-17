#ifndef __NCSLABDEFINES_H
#define __NCSLABDEFINES_H

//Input and Output
#define ssSetNumInputPorts(S, num)  
#define ssGetNumInputPorts(S) (S->parentBlock->inputPortNum)
#define ssSetInputPortWidth(S, idx, width) blk->inputPorts[idx]=(INPUT_PORT*)malloc(sizeof(INPUT_PORT)*width)
#define ssSetInputPortRequiredContiguous(S, idx, value) /*direct input signal access*/
#define ssSetInputPortDirectFeedThrough(S, idx, value) ;

#define ssSetNumOutputPorts(S, num) 0
#define ssGetNumOutputPorts(S) (S->parentBlock->outputPortNum) 
#define ssSetOutputPortWidth(S, idx, width) ;

#define ssGetInputPortSignal(S, idx) (S->parentBlock->inputPorts[idx]->vp)
#define ssGetOutputPortSignal(S, idx) (S->parentBlock->outputPorts[idx]->vp)

#define ssGetGlobalT() (mp->tv.tv_sec * 1000000.0 + mp->tv.tv_usec)


#define sfcnStart(S) if(S.start){S.start((struct SimStruct_tag *)&S);}

#define sfcnInitializeSizes(S) if(S.initializeSize){S.initializeSize((struct SimStruct_tag *)&S); }
#define sfcnUpdate(S) if(S.update){S.update((struct SimStruct_tag *)&S);}
#define sfcnOutputs(S,tid) if(S.outputs){S.outputs((struct SimStruct_tag *)&S, tid);}
#define sfcnDerivatives(S) if(S.derivatives){S.derivatives((struct SimStruct_tag *)&S); }

#define sfcnGetT() (mp->time)

#define sfcnIsMajorStep() (mp->majorStep)


#endif // __NCSLABDEFINES_H

